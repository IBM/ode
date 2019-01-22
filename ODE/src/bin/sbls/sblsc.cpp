#define _ODE_BIN_SBLS_SBLS_CPP


#include "bin/sbls/sblsc.hpp"
#include "lib/io/ui.hpp"
#include "lib/string/smartstr.hpp"


/******************************************************************************
 *
 */
int Sbls::classMain( const char **argv, const char **envp )
{
  Sbls sb( argv, envp );
  return( sb.run() );
}


/******************************************************************************
 *
 */
int Sbls::run()
{
  checkCommandLine();

  StringArray inputs;
  cmdLine->getUnqualifiedVariables( &inputs );
  getBackingChain();
  if (!processInputs( inputs ))
    Interface::quit( 1 );
  return (0);
}


/******************************************************************************
 *
 */
void Sbls::getBackingChain()
{
  Path::separatePaths( Path::normalize(
      SandboxConstants::getBACKED_SANDBOXDIR() ), &backingChain );
}


/******************************************************************************
 *
 */
boolean Sbls::processInputs( const StringArray &inputs )
{
  boolean rc = true;
  if (inputs.length() == 0)
    rc = listInput( "*" ); // list everything in present directory
  else
  {
    for (int i = inputs.firstIndex(); i <= inputs.lastIndex(); i++)
      if (!listInput( inputs[i] ))
        rc = false;
  }
  return (rc);
}


/******************************************************************************
 *
 */
boolean Sbls::listInput( const String &ip )
{
  boolean rc = true;
  String actualInput;

  if (ip.equals( "." ) || ip.equals( ".." ))
    actualInput = Path::getCwdFromEnviron() + Path::DIR_SEPARATOR + ip;
  else

#ifdef DEFAULT_SHELL_IS_CMD
// User might give a path like "C:foo\bar" (note lack of slash after the
// colon).  In that case, treat it like an absolute path here (it will
// be canonicalized later).
  {
    if (Path::absolute( ip ))
      actualInput = ip;
    else
    {
      if ( ip.length() > 1 && isalpha( ip[STRING_FIRST_INDEX] ) && 
           ip[STRING_FIRST_INDEX + 1] == ':')
        actualInput = ip;
      else
        actualInput = Path::getCwdFromEnviron() + Path::DIR_SEPARATOR + ip;
    }
  }
#else
  {
    actualInput = Path::absolute( ip ) ? ip :
                  Path::getCwdFromEnviron() + Path::DIR_SEPARATOR + ip;
  }
#endif

  if (Interface::isDebug())
  {
    Interface::printDebug( "Actual Input before canonicalize: " + actualInput );
  }
  Path::canonicalizeThis( actualInput, false );
  if (Interface::isDebug())
  {
    Interface::printDebug( "Input: " + ip );
    Interface::printDebug( "Actual Input: " + actualInput );
  }

  if (checkIfDir( actualInput ))
      actualInput += Path::DIR_SEPARATOR + "*";

  if (Interface::isDebug())
    Interface::printDebug( "Input after check if dir: " + actualInput );

  dirStack.push( actualInput );
  if (!display( true ) && !ip.equals( "*" ) && !Path::exists( ip ))
  {
    Interface::printError( "sbls: " + ip + ": No such file or directory" );
    rc = false;
  }
  while (!dirStack.empty())
    display( false );
  return (rc);
}


/******************************************************************************
 * Everytime we encounter a directory it is pushed onto the stack...
 */
boolean Sbls::display( boolean firstTime )
{
  String *popped = dirStack.pop();
  String path = (popped == 0) ? String( "" ) : *popped;
  delete popped;
  int j; // counter used in multiple for loops

  Vector< DirEntry > dir_contents;

  // look for wildcards before the fileName
  int wildIx = Path::filePath( path ).indexOfAny("*?[");
  if (wildIx != STRING_NOTFOUND)
  {
    StringArray retBuf;
    String sbPath = getSbPath( path );
    Hashtable< SmartCaseString, int > matches;
    if (backingChain.length() > 0)
    { // we are in a sandbox
      for (int i = backingChain.firstIndex();
          i <= backingChain.lastIndex(); i++)
      {
        StringArray retMatches;
        StringArray pathPart;
        pathPart.append( backingChain[i] );
        if (sbPath[STRING_FIRST_INDEX] == '/' ||
                  sbPath[STRING_FIRST_INDEX] == '\\')
          sbPath = sbPath.substring( STRING_FIRST_INDEX + 1 );
        Path::findFilesInChain( sbPath, pathPart, &retBuf, true, &retMatches );
        if (retMatches.length() > 0)
        {
          // Put retMatches contents into dir_contents, in alphabetic order.
          for (int rbi = retMatches.firstIndex();
              rbi <= retMatches.lastIndex(); ++rbi)
          {
            // check if the matched item was found earlier on backingChain
            if (!matches.containsKey( retMatches[rbi] ))
            {
              String newMatch = backingChain[i] + Path::DIR_SEPARATOR +
                                retMatches[rbi];
              String tempName = Path::fileName( newMatch );
              matches.put( retMatches[rbi], 1 );
              for (j = dir_contents.firstIndex();
                   j <= dir_contents.lastIndex(); j++)
                if (String( dir_contents[j].getEntry() ) >= tempName)
                  break;
              dir_contents.insertElementAt( 
                  DirEntry( tempName, Path::filePath( newMatch ),
                  (i == backingChain.firstIndex()), i - 1), j );
            }
          }
        }
      }
    }
    else
    { // we are not in a sandbox
      StringArray simpleChain;
      String simpleTarget, simpleChainItem;
      int dirSepIx;
      // There should be a previous DIR_SEPARATOR since path is absolute.
      for (dirSepIx = wildIx - 1; dirSepIx > path.firstIndex(); dirSepIx--)
        if (path[dirSepIx] == Path::DIR_SEPARATOR.charAt( STRING_FIRST_INDEX ))
          break;
      // put non-wildcard part into simpleChain and the rest as simpleTarget
      simpleTarget = path.substring( dirSepIx + 1 );
      if (dirSepIx > STRING_FIRST_INDEX)
        simpleChainItem = path.substring( STRING_FIRST_INDEX, dirSepIx );
#ifdef DEFAULT_SHELL_IS_CMD
      if (dirSepIx == STRING_FIRST_INDEX + 2 &&
          isalpha( path[STRING_FIRST_INDEX] ) &&
          path[STRING_FIRST_INDEX + 1] == ':' ||
          dirSepIx == STRING_FIRST_INDEX)
        simpleChainItem += Path::DIR_SEPARATOR;
#else
      if (dirSepIx == STRING_FIRST_INDEX)
        simpleChainItem = Path::DIR_SEPARATOR;
#endif
      simpleChain.add( simpleChainItem );
      
      Path::findFilesInChain( simpleTarget, simpleChain, &retBuf, true );
      if (retBuf.length() > 0)
      {
        for (int rbi = retBuf.firstIndex(); rbi <= retBuf.lastIndex(); ++rbi)
        {
          String tempName = Path::fileName( retBuf[rbi] );
          for (j = dir_contents.firstIndex();
               j <= dir_contents.lastIndex(); j++)
            if (String( dir_contents[j].getEntry() ) >= tempName)
              break;
          dir_contents.insertElementAt( 
              DirEntry( tempName, Path::filePath( retBuf[rbi] ), false, 0 ), j );
        }
      }
    }
  }
  else
  {
    getFiles( Path::filePath( path ), Path::fileName( path ),
              dir_contents );
  
    if (Interface::isDebug())
      Interface::printDebug( "Popped: " + path );
  
    String sbpath = getSbPath( path );
  
    if (sbpath != StringConstants::EMPTY_STRING) // get contents of backing builds
    {
      for (int i = backingChain.firstIndex() + 1;
          i <= backingChain.lastIndex(); i++)
      {
        String temppath = backingChain[i] + sbpath; // No DIR_SEPARATOR needed
        Vector< DirEntry > temp;
  
        getFiles( Path::filePath( temppath ), Path::fileName( temppath ),
                  temp, false, i - ARRAY_FIRST_INDEX );
  
        addAncestors( temp, dir_contents ); // contents of backing build
      }
    }
    else
    {
      if (Interface::isDebug())
        Interface::printDebug( "Apparently not running within a sandbox dir" );
    }
  }

  addToStack( Path::filePath( path ), dir_contents ); //dirs are to be stacked

  if (dir_contents.length() <= 0) // any files/dirs found?
    return false;

  print( Path::filePath( path ), dir_contents );
  return true;
}


/******************************************************************************
 * The inSb flag tells if the contents are from a sb
 *
 * Entries are inserted in alphabetical order by name.
 */
void Sbls::getFiles( const String &path, const String &pattern,
    Vector< DirEntry > &buffer, boolean inSb, int chain_level )
{
  int j;
  StringArray temp;
  Path::getDirContents( path, pattern, &temp );

  for (int i = temp.firstIndex(); i <= temp.lastIndex(); i++)
  {
    // do a case-sensitive comparison (yes, even on NT/OS2) so
    // the listings match Unix order.  Hence the wrapping in a
    // String object.
    for (j = buffer.firstIndex(); j <= buffer.lastIndex(); j++)
      if (String( buffer[j].getEntry() ) >= temp[i])
        break;
    buffer.insertElementAt( DirEntry( temp[i], path, inSb, chain_level ), j );
  }
}


/******************************************************************************
 * If we find the same file in the backing build - do not add them
 *
 * Entries are inserted in alphabetical order by name.
 */
void Sbls::addAncestors( const Vector< DirEntry > &ancestors,
                         Vector< DirEntry > &total )
{
  int j;

  for (int i = ancestors.firstIndex(); i <= ancestors.lastIndex(); i++)
  {
    for (j = total.firstIndex(); j <= total.lastIndex(); j++)
    {
      if (total[j].getEntry() == ancestors[i].getEntry())
      {
        j = ELEMENT_NOTFOUND; // don't add, already here!
        break;
      }
      else if (String( total[j].getEntry() ) > // wrap to insure sensitive cmp
          String( ancestors[i].getEntry() ))   // wrap to insure sensitive cmp
        break; // insert at index j now
    }
    if (j != ELEMENT_NOTFOUND)
      total.insertElementAt( ancestors[i], j );
  }
}


/******************************************************************************
 * If -R is specifed we have to recurse into the subdirs.
 */
void Sbls::addToStack( const String &path, const Vector< DirEntry > &dir_conts )
{
  if (!cmdLine->isState( "-R" ))
    return;

  for (int i = dir_conts.lastIndex(); i >= dir_conts.firstIndex(); i--)
    if (dir_conts[i].isDirectory())
      dirStack.push( path + Path::DIR_SEPARATOR + dir_conts[i].getEntry() +
                     Path::DIR_SEPARATOR + "*" );
}


/******************************************************************************
 *
 */
boolean Sbls::checkIfDir( const String &ip )
{
  if (Path::isDirectory( ip ))
      return true;
  else if( backingChain.length() != 0 )
  {
    String temp;
    for (int i = backingChain.firstIndex(); i <= backingChain.lastIndex(); i++)
    {
      temp = getSbPath( ip );

      // if sbpath returns null and ip == some dir in backing chain - it is ok.
      // if sbpath returns non null, check if dir exists
      // all other cases - we return false
      if ((temp == StringConstants::EMPTY_STRING && ip == backingChain[i]) ||
          (temp != StringConstants::EMPTY_STRING &&
          Path::isDirectory( backingChain[i] + Path::DIR_SEPARATOR + temp )))
        return true;
    }
  }

  return false;
}


/******************************************************************************
 * If in sandbox, this returns the path relative to the sandbox, else ""
 */
String Sbls::getSbPath( const String &path )
{
  SmartCaseString smartpath( path );
  if (backingChain.length() == 0 ||
      !smartpath.startsWith( backingChain[backingChain.firstIndex()] ))
    return "";
  else
    return smartpath.substring(
                  backingChain[backingChain.firstIndex()].length() + 1);
}


/******************************************************************************
 *
 */
void Sbls::print( const String &path, const Vector< DirEntry > &dir_conts )
{
  boolean longFormat = cmdLine->isState( "-l" );
  boolean attribs = cmdLine->isState( "-F" );
  boolean fullPath = cmdLine->isState( "-p");

  if (!fullPath)  // Do not print current directory if using full path option
    Interface::printAlways( Path::canonicalize( path, false ) );

  for (int i = dir_conts.firstIndex(); i <= dir_conts.lastIndex(); i++)
  {
    if (dir_conts[i].getEntry().startsWith( "." ) && !cmdLine->isState( "-a" ))
      continue;

    if (longFormat) // print date and size
      Interface::printnlnAlways( dir_conts[i].getSize() + " " +
                                 dir_conts[i].getTime() + " " );

    if (fullPath) // print out full file path   
      Interface::printnlnAlways( dir_conts[i].fullEntry() );
    else          // print out file name only
      Interface::printnlnAlways( dir_conts[i].getEntry() );
    

    if (attribs)
    {
      if (dir_conts[i].isLink())
        Interface::printnlnAlways( "@" );
      if (dir_conts[i].isDirectory())
        Interface::printnlnAlways( "/" );
      for (int j = 0; j < dir_conts[i].getChainLevel(); ++j)
        Interface::printnlnAlways( "^" );
    }

    Interface::printAlways( "" );
  }
  
  Interface::printAlways( "" ); // blank line to separate directories
}


/******************************************************************************
 *
 */
void Sbls::checkCommandLine()
{
  const char *sts[] = { "-a", "-l", "-R", "-F", "-p", 0 };
  StringArray states( sts );
  StringArray arguments( args );

  cmdLine = new CommandLine( &states, 0, true, arguments, false, *this );
  cmdLine->process();
}


/******************************************************************************
 *
 */
void Sbls::printUsage() const
{
  Interface::printAlways( "Usage: sbls [-alpRF] [ODE_opts] [file...]" );
  Interface::printAlways( "" );
  Interface::printAlways( "       -a: show entries that start with a period" );
  Interface::printAlways( "       -l: show long listing" );
  Interface::printAlways( "       -p: show full absolute file path" );
  Interface::printAlways( "       -R: show contents of all subdirectories" );
  Interface::printAlways( "       -F: show file types" );
  Interface::printAlways( "" );
  Interface::printAlways( "   ODE_opts:" );
  Interface::printAlways( "       -quiet -normal -verbose -debug "
      "-usage -version -rev -info -auto" );
  Interface::printAlways( "" );
  Interface::printAlways( "       file: file or directory to list" );
}
