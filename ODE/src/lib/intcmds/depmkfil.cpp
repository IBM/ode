using namespace std ;
#include <base/binbase.hpp>
#include "lib/intcmds/depmkfil.hpp"
#include "lib/exceptn/mkdepexc.hpp"

#include "lib/exceptn/ioexcept.hpp"
#include "lib/portable/hashtabl.hpp"
#include "lib/io/file.hpp"



/**********************************************************************
 *
 *
 * Static Variables:
 *
 * LINE1 is the format of a line read from depend.mk (# )
 *
 * LINE2 is the format of a line read from depend.mk (# dependents of )
 *
 * *******************************************************************/

const String DepMkFile::LINE1="#";
const String DepMkFile::LINE2="# dependents of ";



/**********************************************************************
 *
 * Constructor of DepMkFile:
 *
 * 
 *
 * ********************************************************************/

DepMkFile::DepMkFile( const String& name, String cname )
 : name(name), callerName(cname)
{
  ;
}

/**********************************************************************
 *
 * Destructor of DepMkFile:
 * It clears and destroys each of the elements in the Collection. 
 *
 *
 * ********************************************************************/
DepMkFile::~DepMkFile()
{
  Target* const *tgt;

  HashElementEnumeration< String, Target* > enumeration( &targets );
  
  while(enumeration.hasMoreElements())
  {
    tgt = enumeration.nextElement();
    if (*tgt != 0)
      delete *tgt;
  }
}

/**********************************************************************
 *
 * load() method.
 * 
 * Finds the depend.mk file, loads its content into the targets collection.
 *
 * Tries to find 'depend.mk' in sandbox first and then in backend builds.
 *
 * Throws exceptions, if no depend.mk file can be found.
 *
 *********************************************************************** */
void DepMkFile::load( const String &sandboxbase, const String &relcurdir,
  const StringArray  &backeddirs )
{
  String curdir;

  // If we're not in a sandbox look locally
  if (sandboxbase.length() == 0)
    curdir = relcurdir;
  else
    curdir = String( sandboxbase + Path::DIR_SEPARATOR + relcurdir );
  File curdepmk(curdir + Path::DIR_SEPARATOR + name, true);

  try
  {  
    if (curdepmk.doesExist())
      readTargets( curdepmk );
    else
    {
      for (int i=backeddirs.firstIndex()+1; i<=backeddirs.lastIndex(); i++)
      {
        File backeddepmk( backeddirs[i] + relcurdir +
            Path::DIR_SEPARATOR + name, true );

        if (backeddepmk.doesExist())
        {
          try
          {
            Path::copyFile( backeddepmk, curdepmk, true );
          }
          catch( IOException &e )
          {
            throw MkDepException( e.getMessage() );
          }
          readTargets( curdepmk );
          break;
        }
      }
    } // no depend.mk file in backing build
  }
  catch (...)
  {
    throw MkDepException( "Copying depend.mk file from backing build has failed!" );
  }
}

/**********************************************************************
 *
 * get() method.
 * 
 * returns a Target* looked up by key.
 *
 *********************************************************************** */
Target* const* DepMkFile::get( const String& key )
{
  return targets.get(key);

}


/**********************************************************************
 *
 * readTargets() method.
 * 
 * Opens depend.mk file.
 *
 * Read line by line and store content into the targets collection.
 *
 * Assumes the depend.mk file has a very specific format.
 *
 *********************************************************************** */
void DepMkFile::readTargets( const File& depmkpath )
{
  int numberOfDependencies = -1;
  String line, name;
  Target *target = 0;
  Body   *body   = 0;
  ifstream *depmkfile = Path::openFileReader( depmkpath);

  if (depmkfile == 0)
    return;

  if (Interface::isVerbose())
    Interface::printVerbose(" " + callerName + ":  Opening " + depmkpath + 
        " and looking for targets.");

  /**
   * Read a new line from the 'depend.mk' file.
   * Proceed, as long as it is not the end of file.
   * If a line starts with '#dependent of <target name>',
   * extract the target.
   *
   * If a line is '#', either I have finished reading targets
   * (in this case, create a Target object and insert it into the Collection)
   * or I'm reading the first '#' at the beginning of a file.
   * (in this case, just continue)
   * If neither of the above applies, and I have a target then
   * I have a body: continue appending the parts to the vector<String> 
   * of the body.
   *
   * If we are on a system where we support blanks in filenames,
   * we assume that there are doublequotes as needed, for normal
   * dependency lines.  For any line that starts with LINE2, we
   * assume that there are no quotes around the file name, even
   * if it has blanks.
  **/

    while( Path::readLine( *depmkfile, &line ) )
    {
      if (line.startsWith( LINE2 ))
      {
        // Extract the target name with the trailing ':'.
        
        name = (line.substring( LINE2.length() )).trim();
        if (Interface::isVerbose())
          Interface::printVerbose( "\tFound target \"" + name + "\"" );

        numberOfDependencies=0;
      }
      else
      {
        if (line.equals( LINE1 ))
        {
          //Just finished reading targets. 
          //or first '#' in 'depend.mk' file
          if (numberOfDependencies > 0)
          {
            // I have read all dependencies for this target.
            // Create a new Target object and add to the list of targets.
            // reset the number of dependencies to -1
            target = new Target( name, body );
            targets.put( name, target );
            numberOfDependencies=-1;
          }
        }
        else
        { //Just read a body.
#ifdef FILENAME_BLANKS
          line.dequoteThis();
#endif
          if (numberOfDependencies++ == 0)
            body = new Body(line);
          else
            body->addElement(line);
          } // End of else
        } //End else
     } // End of While


  if(numberOfDependencies > 0)
  {
    target = new Target( name, body );
    targets.put( name, target );
    numberOfDependencies=-1;
  }

  //Clean up.
  Path::closeFileReader(depmkfile);
}// End of readTargets()


/**********************************************************************
 *
 * save() method.
 * 
 * Write the content of the targets collection into the depend.mk file.
 * Throws exception, if it can't write in the depend.mk file.
 *
 *********************************************************************** */
void DepMkFile::save()
{
  Target * const *tgt;

  try {
    fstream *depmkfile = Path::openFileWriter( name, false, true );
    if (depmkfile == 0) throw IOException();

    HashElementEnumeration< String, Target* > enumeration( &targets );
  
    while(enumeration.hasMoreElements()) {
      tgt = enumeration.nextElement();
      *depmkfile << LINE1 << endl;
      *depmkfile << LINE2 << (*tgt)->getHeader() <<endl;
      (*tgt)->write(depmkfile);
    }

    Path::closeFileWriter( depmkfile );
  }
  catch ( IOException &e )
  {
    throw MkDepException( (String)("Writing to " + name + " has failed!") );
  }
}

/**********************************************************************
 *
 * containsKey() method.
 * 
 * Locate a specific Target, given a key.
 *
 * *******************************************************************/

boolean DepMkFile::containsKey(const String& key)
{
  return(targets.containsKey(key));
}

/**********************************************************************
 *
 * print() method.
 * 
 *  Prints all the elements in the collection.
 * 
 * 
 *
 *********************************************************************** */
void DepMkFile::print()
{
  if ( targets.isEmpty() ) return;

  HashElementEnumeration< String, Target* > enumeration( &targets );
  
  while(enumeration.hasMoreElements())
    ((Target*)enumeration.nextElement())->print();

}

/**********************************************************************
 *
 * replaceTarget() method.
 * 
 * The only reason we have to do a get & delete first is because
 * the element object (Target*) has to be deallocated by us.
 *
 *********************************************************************** */
void DepMkFile::replaceTarget( Target* target )
{
  Target* const *old = targets.get( target->getHeader() );
  if (old != 0)
    delete *old;
  targets.put( target->getHeader(), target );
}

/**********************************************************************
 *
 * appendTarget() method.
 * 
 * Add a target to the targets collection.
 *
 *********************************************************************** */
void DepMkFile::appendTarget( Target* target )
{
  targets.put( target->getHeader(), target );
}
