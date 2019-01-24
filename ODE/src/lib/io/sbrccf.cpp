using namespace std;
#define _ODE_LIB_IO_SBRCCF_CPP_

#include "lib/io/sbrccf.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/env.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/string/pattern.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/string/variable.hpp"

#include "lib/exceptn/exceptn.hpp"
#include "lib/exceptn/parseexc.hpp"
#include "lib/io/cmdline.hpp"



/******************************************************************************
 *
 */
const String SandboxRCConfigFile::DEFAULT_BASENAME = "*";



/******************************************************************************
 * Add a sandbox to the rc file, which uses the specified
 * base directory.
 *
 * @param sandbox The name of the sandbox.
 * @param basedir The base directory of the sandbox.  Null
 * should not be passed (use the version of this method
 * without this parameter if the default base directory
 * is desired).
 * The basedir can have variables in it.
 * @param is_default If true, OR if no other sandboxes exist
 * in the rc file, the sandbox will be designated
 * as the default one.  If false, the default sandbox will
 * not be changed.
 * @return True on success, false on failure.
 */
boolean SandboxRCConfigFile::add( const String &sandbox,
    const String &base_dir, boolean is_default )
{
  String basedir = base_dir;
  String basedirEval; // basedir after variable evaluation

  if (basedir == StringConstants::EMPTY_STRING &&
      (basedir = getDefaultBaseDir()) == StringConstants::EMPTY_STRING)
    basedir = SandboxConstants::getDEFAULT_SANDBOXRCDIR();

  basedirEval = normalizeBaseDir( Variable::envVarEval( basedir ) );

  if (sandbox == StringConstants::EMPTY_STRING ||
      basedirEval == StringConstants::EMPTY_STRING)
    return (false);

  if (!Path::exists( basedirEval ))
    return (false);

  if (getIndex( sandbox ) >= file_info->sandboxes.firstIndex()) // exists!
    return (false);

  if (file_info->sandboxes.size() < 1) // first sandbox, so it must be default
      is_default = true;

  if (addSandboxToMemory( sandbox, true, false ) >=
      file_info->sandboxes.firstIndex() &&
      setSandboxBase( sandbox, basedir, false ) &&
      (!is_default || setDefaultSandbox( sandbox, false )))
    return (rewriteFile());
  else
    return (false);
}



/******************************************************************************
 * Remove a sandbox (and any associated lines) from the
 * rc file.  May cause reassignment of the default sandbox
 * (if the one being deleted is the current default).
 *
 * @param sandbox The sandbox to delete.
 * @return True on success, false on failure.
 */
boolean SandboxRCConfigFile::del( const String &sandbox )
{
  int index;

  if (sandbox == StringConstants::EMPTY_STRING ||
      (index = getIndex( sandbox )) == ELEMENT_NOTFOUND)
    return (false);

  SmartCaseString sb( sandbox );

  file_info->sandboxes.removeAtPosition( index );
  file_info->bases.removeAtPosition( index );
    // if a basename equal to the sandbox name exists,
    // get rid of it too.
  file_info->baselist.remove( sb );

  if (sb.equals( file_info->default_sandbox )) // have to reset
  {
    file_info->default_sandbox = ""; // so verifyDefault() will reset properly
    verifyDefault();
  }
  return (rewriteFile());
}


/******************************************************************************
 *
 */
void SandboxRCConfigFile::addMKSBToMemory( const String &str )
{
  file_info->mksblist.addElement( str );
}



/******************************************************************************
 *
 */
void SandboxRCConfigFile::saveDefaultToFile()
{
  file_info->outputLines.add( "default " + file_info->default_sandbox );
}


/******************************************************************************
 *
 */
void SandboxRCConfigFile::setBases()
{
  for (int i = file_info->sandboxes.firstIndex();
      i <= file_info->sandboxes.lastIndex(); ++i)
    setBase( i );
}


/******************************************************************************
 * Write all sandbox bases to the config file.
 */
void SandboxRCConfigFile::saveBasesToFile()
{
  String lines="", basename, default_basedir = "";
  HashKeyEnumeration< SmartCaseString, SmartCaseString >
      basenames( &(file_info->baselist) );

  while (basenames.hasMoreElements())
  {
    basename = *(basenames.nextElement());

    if (basename.equals( DEFAULT_BASENAME ))
      default_basedir = getSandboxBaseDir( basename );
    else
    {
      file_info->outputLines.add( "base " + basename + " " + 
                                  getSandboxBaseDir( basename ) );
    }
  }

  if (default_basedir != StringConstants::EMPTY_STRING)
  {
    file_info->outputLines.add( "base " + DEFAULT_BASENAME + " " + 
                                default_basedir );
  }

}



/******************************************************************************
 * Write all sandbox names to the config file.
 */
void SandboxRCConfigFile::saveSandboxesToFile()
{

  for (int i = file_info->sandboxes.firstIndex();
      i <= file_info->sandboxes.lastIndex(); ++i)
    file_info->outputLines.add( "sb " + file_info->sandboxes[i] );

}



/******************************************************************************
 * Write all MKSB lines to the config file.
 */
void SandboxRCConfigFile::saveMKSBToFile()
{
  String lines="";

  for (int i = file_info->mksblist.firstIndex();
      i <= file_info->mksblist.lastIndex(); ++i)
    file_info->outputLines.add( "mksb " + file_info->mksblist[i] );

}


/******************************************************************************
 * Regenerate (or remove) the config file.
 *
 * @return True if the file was recreated (or removed if no
 * sandboxes exist) successfully.  False otherwise.
 */
boolean SandboxRCConfigFile::rewriteFile()
{
  boolean rc;
  int ix;
  String defaultToken( "default" );
  String baseToken( "base" );
  String sbToken( "sb" );
  String mksbToken( "mksb" );
  StringArray tmpSandboxes;
  StringArray tmpBases;
  Vector< String > tmpMksblist = file_info->mksblist;

  if (file_info->sandboxes.size() < 1)
    rc = remove();
  else
  {
    // write file contents to outputLines StringArray
    file_info->outputLines.clear();
    if (file_info->inputLines.length() == 0)
    { // write out file for the first time
      file_info->outputLines.add(
          "\t# sandbox rc file created by mksb/mkbb\n\n\t# default sandbox" );
      saveDefaultToFile();
      file_info->outputLines.add( "\n\t# base directories to sandboxes" );
      saveBasesToFile();
      file_info->outputLines.add( "\n\t# list of sandboxes" );
      saveSandboxesToFile();
      file_info->outputLines.add( "\n\t# mksb/mkbb config specific" );
      saveMKSBToFile();
      file_info->outputLines.add( "" );
    }
    else // rewrite an already existing file
    {
      boolean doDefault = false;
      int defaultIx = 0;
      int baseIx = 0;
      int sbIx = 0;
      int mksbIx = 0;
      StringArray parts;
  
      // copy sandboxes for output
      for (ix = file_info->sandboxes.firstIndex();
          ix <= file_info->sandboxes.lastIndex(); ++ix)
        tmpSandboxes.add( file_info->sandboxes[ix] );
      copyBasesForOutput( tmpBases );
  
      // Find the last line of each type of line; that is where we insert
      // new lines of that type.
      for (ix = file_info->inputLines.firstIndex();
           ix <= file_info->inputLines.lastIndex(); ++ix)
      {
        if (!isComment( file_info->inputLines[ix] ))
        {
          parts.clear();
          file_info->inputLines[ix].trim().split( " \t", 2, &parts );
          if ( parts[parts.firstIndex()] == defaultToken )
            defaultIx = ix;
          else if ( parts[parts.firstIndex()] == baseToken )
            baseIx = ix;
          else if ( parts[parts.firstIndex()] == sbToken )
            sbIx = ix;
          else if ( parts[parts.firstIndex()] == mksbToken )
            mksbIx = ix;
        }
      }

      // complain if required lines are missing
      // and write them out at the beginning of the file
      if (defaultIx == 0)
      {
        Interface::printWarning( CommandLine::getProgramName() + 
                                 ": did not find 'default' line in " +
                                 getPathname() );
        file_info->outputLines.add(
            "\t# sandbox rc file created by mksb/mkbb\n\n\t# default sandbox" );
        saveDefaultToFile();
      }
      if (baseIx == 0)
      {
        Interface::printWarning( CommandLine::getProgramName() + 
                                 ": did not find 'base' line in " +
                                 getPathname() );
        file_info->outputLines.add( "\n\t# base directories to sandboxes" );
        saveBasesToFile();
      }
      if (sbIx == 0)
      {
        Interface::printWarning( CommandLine::getProgramName() + 
                                 ": did not find 'sb' line in " +
                                 getPathname() );
        file_info->outputLines.add( "\n\t# list of sandboxes" );
        saveSandboxesToFile();
      }
  
      for (ix = file_info->inputLines.firstIndex();
           ix <= file_info->inputLines.lastIndex(); ++ix)
      {
        if (isComment( file_info->inputLines[ix] ))
          file_info->outputLines.add( file_info->inputLines[ix] );
        else
        {
          // The following assumes that there are always at least two tokens
          // per line, for "default", "sb", "base" or "mksb", and that the
          // parser caught the error if it is not so.
          parts.clear();
          file_info->inputLines[ix].trim().split( " \t", 3, &parts );
          if ( parts[parts.firstIndex()] == defaultToken )
            saveDefaultToFile();
          else if ( parts[parts.firstIndex()] == baseToken )
            writeBaseLine( ix >= baseIx, tmpBases,
                           parts[parts.firstIndex() + 1] );
          else if ( parts[parts.firstIndex()] == sbToken )
            writeSbLine( ix >= sbIx, tmpSandboxes,
                         parts[parts.firstIndex() + 1] );
          else if ( parts[parts.firstIndex()] == mksbToken )
            writeMksbLine( ix >= mksbIx, tmpMksblist,
                           parts[parts.firstIndex() + 1] );
          else
            file_info->outputLines.add( file_info->inputLines[ix] );
        }
      }
    }

    rc = purge();
    for (ix = file_info->outputLines.firstIndex();
         rc && ix <= file_info->outputLines.lastIndex(); ++ix)
      rc = putLine( file_info->outputLines[ix], true );
    if (!rc)
      Interface::printWarning( CommandLine::getProgramName() + 
                               ": could not erase or write " +
                               getPathname() );
  }

  this->close();
  return (rc);
}


/******************************************************************************
 */
void SandboxRCConfigFile::copyBasesForOutput( StringArray &baseLines )
{
  String lines="", basename, default_basedir = "";
  HashKeyEnumeration< SmartCaseString, SmartCaseString >
      basenames( &(file_info->baselist) );

  baseLines.clear();
  while (basenames.hasMoreElements())
  {
    basename = *(basenames.nextElement());

    if (basename.equals( DEFAULT_BASENAME ))
      default_basedir = getSandboxBaseDir( basename );
    else
      baseLines.add( basename + " " + getSandboxBaseDir( basename ) );
  }

  if (default_basedir != StringConstants::EMPTY_STRING)
    baseLines.add( DEFAULT_BASENAME + " " + default_basedir );
}


/******************************************************************************
 */
void SandboxRCConfigFile::writeBaseLine( boolean lastOne, 
                                         StringArray &baseLines,
                                         String &sandbox )
{
  int i;
  for (i = baseLines.firstIndex(); i <= baseLines.lastIndex(); ++i)
  {
    if ( baseLines[i].startsWith( sandbox ))
    {
      file_info->outputLines.add( "base " + baseLines[i] );
      baseLines.removeAtPosition( i );
      break;
    }
  }
  if ( lastOne )
  {
    for (i = baseLines.firstIndex(); i <= baseLines.lastIndex(); ++i)
      file_info->outputLines.add( "base " + baseLines[i] );
  }
}


/******************************************************************************
 */
void SandboxRCConfigFile::writeSbLine( boolean lastOne,
                                       StringArray &sbList,
                                       String &sandbox )
{
  int i;
  if (sbList.length() > 0)
    for (i = sbList.firstIndex(); i <= sbList.lastIndex(); ++i)
      if (sbList[i] == sandbox)
      {
        file_info->outputLines.add( "sb " + sbList[i] );
        sbList.removeAtPosition( i );
        break;
      }
  if ( lastOne )
    for (i = sbList.firstIndex(); i <= sbList.lastIndex(); ++i)
      file_info->outputLines.add( "sb " + sbList[i] );
}


/******************************************************************************
 */
void SandboxRCConfigFile::writeMksbLine( boolean lastOne,
                                         Vector< String > &mksbList,
                                         String &flag )
{
  int i;
  for (i = mksbList.firstIndex(); i <= mksbList.lastIndex(); ++i)
  {
    if ( mksbList[i].startsWith( flag ))
    {
      file_info->outputLines.add( "mksb " + mksbList[i] );
      mksbList.removeAtPosition( i );
      break;
    }
  }
  if ( lastOne )
  {
    for (i = mksbList.firstIndex(); i <= mksbList.lastIndex(); ++i)
      file_info->outputLines.add( "mksb " + mksbList[i] );
  }
}


/******************************************************************************
 * Test if a line is a comment, namely optional initial tabs and/or blanks,
 * followed by '#'. A line that is zero length or contains
 * only whitespace is also considered to be a comment.
 */
boolean SandboxRCConfigFile::isComment( String line )
{
  line.trimThis();
  if (line.length() == 0)
    return true;
  else
    return (line.charAt(STRING_FIRST_INDEX) == '#');
}


/******************************************************************************
 *
 */
int SandboxRCConfigFile::addSandboxToMemory( const String &sandbox,
                                             boolean findbase,
                                             boolean replace )
{
  int index = getIndex( sandbox );

  if (!replace && index >= file_info->sandboxes.firstIndex())
    return ( ELEMENT_NOTFOUND ); // already exists, but shouldn't replace

  if (index < file_info->sandboxes.firstIndex())
  {
    file_info->sandboxes.addElement( SmartCaseString( sandbox ) );
    file_info->bases.addElement( SmartCaseString( "" ) );
    index = file_info->sandboxes.lastIndex(); // index of what we just added
  }
  if (findbase)
    setBase( index );

  return (index);
}



/******************************************************************************
 *
 */
boolean SandboxRCConfigFile::addBaseToMemory( const String &base,
                                              const String &basedir,
                                              boolean replace )
{
  if (base == StringConstants::EMPTY_STRING ||
      basedir == StringConstants::EMPTY_STRING)
    return (false);

  SmartCaseString b( base );

  if (getSandboxBaseDir( base ) != StringConstants::EMPTY_STRING) // exists
  {
    if (!replace)
      return (false);
    else
      file_info->baselist.remove( b );
  }

  file_info->baselist.put( b, SmartCaseString( basedir ) );

  return (true);
}



/******************************************************************************
 *
 */
int SandboxRCConfigFile::getIndex( const String &sandbox ) const
{
  if (sandbox.length() <= 0)
    return ( ELEMENT_NOTFOUND );

  return (file_info->sandboxes.indexOf( SmartCaseString( sandbox ) ));
}



/******************************************************************************
 * Set the base name for the sandbox at the
 * given sandboxes index.
 * Updates the bases vector.
 *
 * @param index The index into the sandboxes vector
 * for the sandbox to find the base of.
 * @return True on success, false on failure.
 */
boolean SandboxRCConfigFile::setBase( int index )
{
  String base;

  if ((base = findBase( index )) == StringConstants::EMPTY_STRING)
    return( false );

  file_info->bases.setElementAt( SmartCaseString( base ), index );

  return (true);
}



/******************************************************************************
 * Find the appropriate base name for the sandbox
 * at the given index.
 *
 * @param index The index into the sandboxes vector
 * for the sandbox to find a base for.
 * @return The base name string.  Null may be returned
 * if the index is out of bounds or if there is no
 * default base in memory.
 */
String SandboxRCConfigFile::findBase( int index ) const
{
  String base = "";
  SmartCaseString b;

  if (index >= file_info->sandboxes.firstIndex() &&
      index <= file_info->sandboxes.lastIndex())
  {
    SmartCaseString sb( file_info->sandboxes[index] );

    if (file_info->baselist.containsKey( sb ))
      base = sb.toString();
    else
    {
      // first try all other bases EXCEPT "*"
      HashKeyEnumeration< SmartCaseString, SmartCaseString >
          enum_( &(file_info->baselist) );
      String testbase;
      while (base == StringConstants::EMPTY_STRING && enum_.hasMoreElements())
      {
        b = *enum_.nextElement();

        if (!b.equals( DEFAULT_BASENAME ) &&
            Pattern::isMatching( b.toString(), sb.toString(),
            PlatformConstants::onCaseSensitiveOS() ))
          base = b.toString();
      }
    }

    if (base == StringConstants::EMPTY_STRING &&
        getDefaultBaseDir() != StringConstants::EMPTY_STRING)
      base = DEFAULT_BASENAME;
  }

  return ( base );
}



/******************************************************************************
 *
 */
void SandboxRCConfigFile::readAll()
{
  String line;

  try
  {
    while (getLine( false, false, &line, false ) != 0)
    {
      parseLine( line );
    }
  }
  catch (IOException &e)
  { // file not found - maybe create a new file...
  }

  this->close();
  setBases();
  verifyDefault();
}



/******************************************************************************
 *
 */
void SandboxRCConfigFile::parseLine( const String &str )
{
  String trimmed_str = str.trim();
  if (trimmed_str.length() < 1 ||
      trimmed_str.startsWith( StringConstants::POUND_SIGN ))
  {
    file_info->inputLines.add( str ); // save untrimmed comment
    return;
  }
  else
  {
    file_info->inputLines.add( trimmed_str );
  }

  StringArray str_split;
  str.split( " \t", 2 , &str_split );

  try
  {
    if (str_split.length() < 2)
      throw Exception();

    if (str_split[str_split.firstIndex()].equalsIgnoreCase( "default" ))
      parseDefault( str_split[str_split.firstIndex() + 1] );
    else if (str_split[str_split.firstIndex()].equalsIgnoreCase( "base" ))
      parseBase( str_split[str_split.firstIndex() + 1] );
    else if (str_split[str_split.firstIndex()].equalsIgnoreCase( "sb" ))
      parseSandbox( str_split[str_split.firstIndex() + 1] );
    else if (str_split[str_split.firstIndex()].equalsIgnoreCase( "mksb" ))
      parseMKSB( str_split[str_split.firstIndex() + 1] );
    else // error
      throw Exception(); // goes into catch block below
  }
  catch (...) // NullPtr, ArrayOOB, StrOOB, etc.
  {
    Interface::printWarning( CommandLine::getProgramName() + ": " +
                             "Error caused by line " +
                             String( getLastLineNumber() ) + " in " +
                             getPathname() + " (ignored)" );
  }
}



/******************************************************************************
 *
 */
void SandboxRCConfigFile::parseDefault( const String &str ) // throws Exception
{
  StringArray str_split;
  str.split( " \t", 2 , &str_split );

  if (str_split.length() < 1)
    throw Exception("Invalid DEFAULT line.");
  else
    file_info->default_sandbox = str_split[str_split.firstIndex()];
}



/******************************************************************************
 *
 */
boolean SandboxRCConfigFile::parseBase( const String &str ) // throws Exception
{
  StringArray str_split;
  str.split( " \t", 2, &str_split );

  if (str_split.length() < 2)
    throw Exception( "Invalid BASE line." );

  // we don't call normalizeBaseDir, since we assume the .sandboxrc
  // was created via add() in the past (and thus is already normalized).
  // also, we do this so that OS2/Windows users can remove the drive
  // letter from the base so the drive is determined at runtime.

  return (addBaseToMemory( str_split[str_split.firstIndex()],
      str_split[str_split.firstIndex() + 1], true ));
}



/******************************************************************************
 *
 */
void SandboxRCConfigFile::parseSandbox( const String &str ) // throws Exception
{
  StringArray str_split;
  str.split( " \t", 2, &str_split );

  if (str_split.length() < 1)
    throw Exception("Invalid SANDBOX line.");

  int old_size = file_info->sandboxes.size();

  if (addSandboxToMemory( str_split[str_split.firstIndex()],
                          false, false ) > old_size)
    file_info->bases.addElement( SmartCaseString( "" ) );
}



/******************************************************************************
 *
 */
void SandboxRCConfigFile::verifyDefault()
{
  if ((getIndex( file_info->default_sandbox ) == ELEMENT_NOTFOUND) &&
      (file_info->sandboxes.size() > 0))
    setDefaultSandbox(
        file_info->sandboxes[file_info->sandboxes.firstIndex()], true );
}



/******************************************************************************
 *
 */
boolean SandboxRCConfigFile::setDefaultSandbox( const String &sandbox,
                                                boolean rewrite )
{
  if (getIndex( sandbox ) == ELEMENT_NOTFOUND)
    return (false);

  if (SmartCaseString( sandbox ).equals( file_info->default_sandbox ))
    return (true);

  file_info->default_sandbox = sandbox;

  if (rewrite)
    return (rewriteFile());
  else
    return (true);
}



/******************************************************************************
 *
 */
String SandboxRCConfigFile::getSandboxBaseDir( const String &basename ) const
{
  if (basename == StringConstants::EMPTY_STRING)
    return ("");

  const SmartCaseString *base = file_info->baselist.get(
      SmartCaseString( basename ) );

  if (base == 0)
    return ("");

  return (*base);
}



/**
 *
**/
String SandboxRCConfigFile::normalizeBaseDir( const String &basedir ) const
{
  String temp_basedir = basedir;
  if (!Path::absolute( temp_basedir ))
    Path::canonicalizeThis( temp_basedir, false );
  Path::unixizeThis( temp_basedir );
  return (temp_basedir);
}



/**
 *
**/
boolean SandboxRCConfigFile::setSandboxBase( const String &sandbox,
                                             const String &basedir,
                                             boolean rewrite )
{
  String basename, def_base;

  // basedir should not be evaluated or "normalized"
  if (sandbox == StringConstants::EMPTY_STRING ||
      basedir == StringConstants::EMPTY_STRING)
    return (false);

  if (getIndex( sandbox ) == ELEMENT_NOTFOUND) // sandbox doesn't exist
    return (false);

  String oldbasedir = getSandboxBaseDir( sandbox );
  SmartCaseString ob( oldbasedir);
  SmartCaseString sb( sandbox );
  SmartCaseString b( basedir );

  if ((def_base = getDefaultBaseDir()) == StringConstants::EMPTY_STRING)
    basename = DEFAULT_BASENAME;
  else
    basename = sandbox; // maybe

  if (def_base != StringConstants::EMPTY_STRING && b.equals( def_base ))
  {
    if (oldbasedir == StringConstants::EMPTY_STRING)
      return (true);
    else
      file_info->baselist.remove( sb );
  }
  else
  {
    if (oldbasedir != StringConstants::EMPTY_STRING && ob.equals( basedir ))
      return (true); // was already set the same
    if (!addBaseToMemory( basename, basedir, true ))
      return (false);
  }

  if (rewrite)
    return (rewriteFile());
  else
    return (true);
}



/**
 * Returns the base directory of the specified sandbox, with variables
 * evaluated, and then canonicalized.
 *
 * @param sandbox The sandbox to find the base directory for.
 * If null is passed, the default sandbox is used.
 * @return The base directory of the specified sandbox.  An empty
 * String is returned if sandbox doesn't exist or if it has no
 * base directory (this may happen if the user has removed the
 * default base specification from the rc file).
**/
String SandboxRCConfigFile::getSandboxBase( const String &sandbox,
                                            boolean eval ) const
{
  int index;

  if (sandbox == StringConstants::EMPTY_STRING)
    index = getIndex( getDefaultSandbox() );
  else
    index = getIndex( sandbox );

  if (index == ELEMENT_NOTFOUND)
    return "";

  String result = getSandboxBaseDir( findBase( index ) );
  if (eval)
  {
    result = Variable::envVarEval( result );
    Path::canonicalizeThis( result, false );
  }

  return (result);
}



/******************************************************************************
 * Return a list of all sandboxes in the rc file.
 *
 * @return An array containing all sandbox names in the
 * rc file (i.e., all unique lines starting with "sb ").
 */
StringArray *SandboxRCConfigFile::getSandboxList( StringArray *buf ) const
{
  StringArray *list = (buf == 0) ? new StringArray() : buf;

  for (int i = file_info->sandboxes.firstIndex();
      i <= file_info->sandboxes.lastIndex(); ++i)
  {
    list->add( file_info->sandboxes[i] );
  }

  return (list);
}



/******************************************************************************
 * Return a list of all of the full sandbox directories.
 * First it obtains the list of sandboxes, then it finds
 * their base directories, then it creates the full path
 * for each one (by concatenating the former to the latter)
 * and puts this in an array.
 *
 * @return An array containing all sandbox names in the
 * rc file (i.e., all unique lines starting with "sb ")
 * appended to their respective base directories.
 */
StringArray *SandboxRCConfigFile::getSandboxPathList(
    StringArray *buf ) const
{
  StringArray *list = getSandboxList( buf );

  for (int i = list->firstIndex(); i <= list->lastIndex(); ++i)
  {
    list->elementAtPosition( i ) = getSandboxBase( (*list)[i] ) + "/" +
        (*list)[i];
  }

  return (list);
}



/******************************************************************************
 * Set the "mksb" lines in the rc file to the specified
 * strings.
 *
 * @param list An array of strings, each of which will have
 * the text "mksb " prepended automatically, and then
 * output at the end of the rc file.
 * @param rewrite If true, the rc file will be rewritten with
 * the new information immediately.  If false, the caller is
 * responsible for updating the rc file.
 * @return True on success, false on failure.
 */
boolean SandboxRCConfigFile::setMKSBList( const StringArray &list,
                                          boolean rewrite )
{
  if (list.length() == 0)
    return (false);

  file_info->mksblist.removeAllElements();

  for (int i = list.firstIndex(); i <= list.lastIndex(); ++i)
    addMKSBToMemory( list[i] );

  if (rewrite)
    return (rewriteFile());
  else
    return (true);
}



/******************************************************************************
 * Return the lines from the rc file that were prepended
 * with "mksb ".  The "mksb " portion of each line is NOT
 * included.
 *
 * @return An array consisting of each line in the rc file
 * that began with "mksb ".  The "mksb " text is removed
 * before being returned.
 */
StringArray *SandboxRCConfigFile::getMKSBList( StringArray *buf ) const
{
  StringArray *list = (buf == 0) ? new StringArray() : buf;

  for (int i = file_info->mksblist.firstIndex();
      i <= file_info->mksblist.lastIndex(); ++i)
  {
    list->add( file_info->mksblist[i] );
  }

  return (list);
}
