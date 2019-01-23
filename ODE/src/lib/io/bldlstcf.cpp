using namespace std;
#define _ODE_LIB_IO_BLDLSTCF_CPP_

#include "lib/io/bldlstcf.hpp"
#include "lib/io/cfgf.hpp"
#include "lib/io/path.hpp"
#include "lib/portable/platcon.hpp"


/******************************************************************************
 * Add a new build to the config file.
 *
 * @param build The build name.
 * @param builddir The build directory.
 * @param replace If true, replace the build directory
 * if the name already exists.  If false, do not modify
 * anything if the build name already exists.
 * @param is_default If true, the build becomes the default
 * (the first build in the list).  If false, the build becomes
 * the second build in the list (builds 2..n get shifted to
 * become builds 3..n+1).
 * @return True on success, false on failure.
 */
boolean BuildListConfigFile::add( const String &build, const String &builddir,
                                  boolean replace, boolean is_default )
{
  int index, old_lastindex;
  boolean rc;

  if (build == StringConstants::EMPTY_STRING ||
      builddir == StringConstants::EMPTY_STRING)
    return (false);

  old_lastindex = file_info->builds.lastIndex();
  index = addToMemory( build, Path::unixize( builddir ),
                       replace, is_default, false );

  if (index == ELEMENT_NOTFOUND)
    rc = false;
  else if (index <= old_lastindex) // a replacement, so have to rewrite
    rc = rewriteFile();
  else // just append
  {
    rc = saveToFile( index, true );
    this->close();
  }
  return (rc);
}


/******************************************************************************
 * Remove a build from the config file.
 *
 * @param build The build name to remove.
 * @return True if the build was successfully removed.
 * False if not, or if the build didn't exist in the file.
 */
boolean BuildListConfigFile::del( const String &build )
{
  int index = getIndex( build );

  if (index == ELEMENT_NOTFOUND)
    return (false);

  file_info->builds.removeElementAt( index );
  file_info->builddirs.removeElementAt( index );

  return (rewriteFile());
}


/******************************************************************************
 * test if sandbox is in the build list file
 *
 * @param sandbox
 * @return True if the sandbox is in the build list file
 * False if not
 */
boolean BuildListConfigFile::inBuildList( const String &sandbox )
{
  return (getIndex( sandbox ) != ELEMENT_NOTFOUND);
}


/******************************************************************************
 *
 */
int BuildListConfigFile::addToMemory( const String &build,
                                      const String &builddir,
                                      boolean replace, boolean is_default,
                                      boolean append )
{
  int index = getIndex( build ), insert_index;

  if (index != ELEMENT_NOTFOUND)
  {
    if (!replace)
      return (ELEMENT_NOTFOUND);

    file_info->builds.removeElementAt( index );
    file_info->builddirs.removeElementAt( index );

    index = ELEMENT_NOTFOUND; // as if it was never there
  }

  if (is_default || file_info->builds.isEmpty())
    insert_index = file_info->builds.firstIndex();
  else if (!append)
    insert_index = file_info->builds.firstIndex() + 1;
  else
    insert_index = file_info->builds.lastIndex() + 1;

  file_info->builds.insertElementAt( SmartCaseString( build ),
      insert_index );
  file_info->builddirs.insertElementAt( SmartCaseString( builddir ),
      insert_index );

  return (insert_index);
}


/******************************************************************************
 *
 */
boolean BuildListConfigFile::saveToFile( int index, boolean append )
{
  if (index >= file_info->builds.firstIndex() &&
      index <= file_info->builds.lastIndex())
  {
    String tosave = file_info->builds[index] + "\t*\t" +
        file_info->builddirs[index];

    return (putLine( tosave, append ));
  }
  
  return (false);
}


/******************************************************************************
 *
 */
boolean BuildListConfigFile::rewriteFile()
{
  boolean rc=true;

  if (file_info->builds.isEmpty())
    rc = remove();
  else
  {
    for (int i = file_info->builds.firstIndex();
        rc && i <= file_info->builds.lastIndex(); ++i)
      if (!saveToFile( i, (i > file_info->builds.firstIndex()) ))
        rc = false;
    this->close();
  }
  return (rc);
}


/**
 * Change the default build.  Returns true if build
 * was made the new default, or false if the build
 * doesn't exist in the build list file.
**/
boolean BuildListConfigFile::setDefaultBuild( const String &build )
{
  SmartCaseString build_data, builddir_data;
  int index = getIndex( build );

  if (index == ELEMENT_NOTFOUND) // no such build in the build list
    return (false);
  if (index == ARRAY_FIRST_INDEX) // already the default
    return (true);

  // save the default info, remove it from its current index,
  // then re-insert at the proper index

  build_data = file_info->builds[index];
  file_info->builds.removeElementAt( index );
  file_info->builds.insertElementAt( build_data, ARRAY_FIRST_INDEX );

  builddir_data = file_info->builddirs[index];
  file_info->builddirs.removeElementAt( index );
  file_info->builddirs.insertElementAt( builddir_data, ARRAY_FIRST_INDEX );

  return (rewriteFile());
}


/******************************************************************************
 *
 */
int BuildListConfigFile::getIndex( const String &build ) const
{
  if (build == StringConstants::EMPTY_STRING)
    return (ELEMENT_NOTFOUND);

  return (file_info->builds.indexOf( SmartCaseString( build ) ));
}


/******************************************************************************
 *
 */
void BuildListConfigFile::readAll()
{
  String *line;

  try
  {
    while ((line = getLine()) != 0)
    {
      parseLine( *line );
      delete line;
    }
  }
  catch (IOException &e)
  {
    // file not found - maybe create it later...
  }

  this->close();
}


/******************************************************************************
 *
 */
void BuildListConfigFile::parseLine( const String &str )
{
  StringArray str_split;
  String build, builddir;

  str.split( " \t", 0, &str_split );
  if (str_split.length() < 3)
    return;

  build = str_split[str_split.firstIndex()];
  // str_split[str_split.firstIndex() + 1] is ignored
  builddir =  str_split[str_split.firstIndex() + 2];
  Path::unixizeThis( builddir );

  addToMemory( build, builddir, true, false, true );
}

/******************************************************************************
 * Return the list of build names.
 *
 * @return An array of strings, each of which represents
 * a build name.
 */
StringArray *BuildListConfigFile::getBuildList( StringArray *buf ) const
{
  int count = file_info->builds.size();
  StringArray *result = (buf == 0) ? new StringArray( count ) : buf;

  for (int i = file_info->builds.firstIndex();
      i <= file_info->builds.lastIndex(); ++i)
    result->add( file_info->builds[i] );
    
  return (result);
}


/******************************************************************************
 * Get the directory associated with a build name.
 *
 * @param build The build name.
 * @return The directory associated with the build.
 * Returns null if the build name doesn't exist.
 */
String BuildListConfigFile::getBuildDir( const String &build ) const
{
  int index;

  if ((index = getIndex( build )) == ELEMENT_NOTFOUND)
    return "";
  else
    return file_info->builddirs[index];
}

