using namespace std;
using namespace std;
#define _ODE_BIN_SBINFO_MKLINKSC_CPP_


#include <base/binbase.hpp>
#include "bin/mklinks/mklinksc.hpp"
#include "bin/mklinks/mklinkex.hpp"
#include "lib/io/path.hpp"
#include "lib/io/sandbox.hpp"
#include "lib/io/ui.hpp"
#include "lib/portable/platcon.hpp"


#include "lib/exceptn/sboxexc.hpp"


/******************************************************************************
 *
 */
int MkLinks::classMain( const char **argv, const char **envp )
{
  MkLinks mklinks( argv, envp );
  return (mklinks.run());
}


/******************************************************************************
 *
 */
int MkLinks::run()
{
  boolean copyflag = false;
  boolean errorflag = false;
  try
  {
    parseCmdLine();
    getSBoxInfo();
    if (!linkFromKey)
      for (int i=dirEntry.firstIndex(); i<=dirEntry.lastIndex(); i++)
      {
        getLinkFromToPaths( dirEntry[i] );
        populateBackingBuild( copyflag, errorflag);
        createFiles();
      }
    else
    {
      getLinkFromToPaths( "" );
      populateBackingBuild( copyflag, errorflag);
      createFiles();
    }
       
  }
  catch (MkLinksException &e)
  {
    Interface::printError( CommandLine::getProgramName() + ": " +
                           e.getMessage() );
    Interface::quit( 1 );
  }
  if (!(copyflag) || (errorflag))  //if no file has been copied or the source
    Interface::quit( 1 );          //doesn't exist for one of the input files
                                   //return nonzero errorcode
  return (0);
}


/******************************************************************************
 *
 */
String MkLinks::toString() const
{
  return (PROGRAM_NAME);
}


/******************************************************************************
 * parse options combination in Command Line
 */
void MkLinks::parseCmdLine()
//    throws MkLinksException
{
  const char *statesp[] = { "-copy", "-over", "-norecurse", "-norefresh",
                            "-query", "-timecmp", 0 };
  StringArray states( statesp );
  const char *qvarsp[] = { "-sb", "-rc", "-link_from", "-link_to",
                           "-rename", 0 };
  StringArray qvars( qvarsp );

  boolean unqvars = true;
  boolean needarguments = true; // have to have some options on commandline
  String tmpvar;

  // initialize commandline class
  StringArray arguments( args );
  cmdline = new CommandLine( &states, &qvars, unqvars,
                             arguments, needarguments, *this );
  cmdline->process();

  // get all option's states to simplify programming
  copyKey = cmdline->isState( "-copy" );
  overKey = cmdline->isState( "-over" );
  timecmpKey = cmdline->isState( "-timecmp" );
  norecurseKey = cmdline->isState( "-norecurse" );
  norefreshKey = cmdline->isState( "-norefresh" );
  autoKey = cmdline->isState( "-auto" );
  infoKey = cmdline->isState( "-info" );
  queryKey = cmdline->isState( "-query" );
  linkFromKey = cmdline->isState( "-link_from" );
  linkToKey = cmdline->isState( "-link_to" );
  renameKey = cmdline->isState( "-rename" );

  printOptions();

  // check options combination
  if ( ( linkFromKey && !linkToKey ) || ( !linkFromKey && linkToKey ) )
    throw MkLinksException( "-link_from and -link_to options must be used together" );

  if ((tmpvar = cmdline->getQualifiedVariable( "-link_from" )) !=
      StringConstants::EMPTY_STRING)
  {
    linkFromEntry =  tmpvar;
    Path::normalizeThis( linkFromEntry );
    if (Path::exists(linkFromEntry))
      Path::canonicalizeThis( linkFromEntry );
    else
      throw MkLinksException( linkFromEntry + " doesn't exist ! " );
    if (!(Path::isDirectory( linkFromEntry )))
      throw MkLinksException( linkFromEntry + " must be a directory ! "); 
  }

  if ((tmpvar = cmdline->getQualifiedVariable( "-link_to" )) !=
      StringConstants::EMPTY_STRING)
  {
    linkToEntry = tmpvar;
    Path::normalizeThis( linkToEntry );
    if ((Path::isFile( linkToEntry )))
      throw MkLinksException( linkToEntry + " must be a directory ! "); 
  }
  cmdline->getUnqualifiedVariables( &dirEntry );
  if (dirEntry.length() != 0)
    for(int i=dirEntry.firstIndex(); i<=dirEntry.length(); i++)
      Path::normalizeThis( dirEntry[i] );
     

  if ((dirEntry.length() != 0) && linkFromKey)
    throw MkLinksException( String( "-link_from option and " ) +
    dirEntry[dirEntry.firstIndex()] +
        " cannot be used together" );

  if ((dirEntry.length() == 0) && !linkFromKey)
    throw MkLinksException( "should use either directory or -link_from" );

  if ((tmpvar = cmdline->getQualifiedVariable( "-rename" )) !=
      StringConstants::EMPTY_STRING)
  {
    if (linkFromKey && renameKey)
      throw MkLinksException(
                   "-link_from and -rename options cannot be used together" );
    renameName = Path::normalizeThis( tmpvar );
    if (Path::absolute( renameName ))
      throw MkLinksException( String( "-rename value " ) + renameName +
                              " must not be absolute ! "); 
  }

  if ((tmpvar = cmdline->getQualifiedVariable( "-rc" )) !=
      StringConstants::EMPTY_STRING)
    rcFileName = Path::normalizeThis( tmpvar );

  if ((tmpvar = cmdline->getQualifiedVariable( "-sb" )) !=
      StringConstants::EMPTY_STRING)
    sboxName = Path::normalizeThis( tmpvar );

#ifdef NO_SYMLINKS
  copyKey = true; // for non Unix machine, no link
#endif
}


/******************************************************************************
 *  get sandbox information by calling constructor of SandBox class. If user
 *  don't specify rcFileName with -rc option, we will use default
 *  rcFile (HOME/.sandboxrc). if user also don't specify sboxName, we will
 *  use default sandbox in default rcFile. Otherwise, we will use the
 *  specified sandbox and rcFile. But if user specified -link_from and
 *  -link_to options in command line, we just consider this is a special
 *  case in sandbox enviroment and tansfer them into sandbox information.
 *
 *  input: sboxName, rcFileName, linkFromKey, linkFromEntry, linkToEntry
 *  output: sboxDir, backingDir, backingChainDir:
 *
 */
void MkLinks::getSBoxInfo() // throws MkLinksException
{
  if (linkFromKey)
  {
    if (Interface::isDebug())
    {
      Interface::printDebug( "linkToEntry: " + linkToEntry );
      Interface::printDebug( "linkFromEntry: " + linkFromEntry );
    }
    sboxDir = linkToEntry;
    backingDir = linkFromEntry;
    backingChainDir.extendTo( 2 );
    backingChainDir[ARRAY_FIRST_INDEX] = linkToEntry;
    backingChainDir[ARRAY_FIRST_INDEX + 1] = linkFromEntry;
  }
  else
  {
    try
    {
      Sandbox sbox( false, rcFileName, sboxName );

      sboxName = sbox.getSandboxName();
      sboxDir = sbox.getSandboxBase();
      Path::normalizeThis( sboxDir );
      const String *backdirptr;
      if ((backdirptr = sbox.getBackingDir()) == 0)
        throw MkLinksException( "No Backing Build" );
      backingDir = *backdirptr;
      Path::normalizeThis( backingDir );
      if (sbox.getBackingChainArray( &backingChainDir ) == 0)
        throw MkLinksException( "No Backing Build Chain" );
      for (int i = backingChainDir.firstIndex();
          i <= backingChainDir.lastIndex(); ++i)
        Path::normalizeThis( backingChainDir[i] );
    }
    catch (SandboxException &e)
    {
      throw MkLinksException( e.getMessage() );
    }
  }
}


/******************************************************************************
 *  This method has two purpose. One is to get the original or destinative
 *  directory paths of the first upper backing build and the current
 *  development sandbox. That are linkFromPath and linkToPath:: The file tree
 *  structure under linkFromPath will be copied into linkToPath if they are
 *  satisfied the populating rules( see populateTree method ). Another is
 *  to get a reltive path( relDir ) that is common part under the sandbox
 *  dirctory and backing build directory. That means linkFromPath =
 *  backingDir/relDir, linkToPath = sboxDir/relDir. We will use it in
 *  populateTree method to decide each linkfrompath in each backing build.
 *  But if user specifies the -link_from and -link_to options, we convert
 *  it into a special sandbox environment so that we can use the same one
 *  populateTree and createFiles methods.
 *  input: sboxDir, backingDir, linkFromEntry, linkToEntry, linkFromKey.
 *  output: linkFromPath, linkToPath, relDir.
 *
 */
void MkLinks::getLinkFromToPaths( const String &ip_param ) // throws MkLinksException
{
  if (linkFromKey)
  {
    linkFromPath = linkFromEntry;
    linkToPath = linkToEntry;
    relDir = ".";
  }
  else if (Path::absolute( ip_param ))
  { // ip_param is absolute path,
    // All processing come done from the specified directory under
    // current development sandbox directory.
    linkToPath = sboxDir + Path::DIR_SEPARATOR + preDir + ip_param;
    Path::canonicalizeThis( linkToPath, false );
    linkFromPath = backingDir + Path::DIR_SEPARATOR + preDir + ip_param;
    Path::canonicalizeThis( linkFromPath, false );
    relDir = linkToPath.substring( sboxDir.lastIndex() + 1 );
  }
  else // ip_param is a relative path.
  {    // All processing come done from current directory
    SmartCaseString curdir( Path::getcwd() ), sboxdir( sboxDir );

    Path::fullyCanonicalize( curdir );
    Path::fullyCanonicalize( sboxdir );
    if (curdir.length() < 1)
      throw MkLinksException( "Can't get current directory" );

    if (sboxdir.length() < 1 || !curdir.startsWith( sboxdir ))
      throw MkLinksException(
          String( "Current directory is not in sandbox " ) + sboxDir );

    linkToPath = sboxDir + Path::DIR_SEPARATOR +
        curdir.substring( sboxdir.lastIndex() + 1 ) + Path::DIR_SEPARATOR +
        ip_param;
    Path::canonicalizeThis( linkToPath, false );
    linkFromPath = backingDir + Path::DIR_SEPARATOR +
        curdir.substring( sboxdir.lastIndex() + 1 ) + Path::DIR_SEPARATOR +
        ip_param;
    Path::canonicalizeThis( linkFromPath, false);
    relDir = linkToPath.substring( sboxDir.lastIndex() + 1 );
  }
  if (Interface::isDebug())
    Interface::printDebug( String( "Relative dir = " ) + relDir );
}


/******************************************************************************
 * Computes -rename substitution information.
 * Return the parameter value with the -rename operand substituted, if there
 * is any. If there is no -rename parameter, the parameter value is returned.
 * A side effect is to set the variable renameLastDirsep to the index of the
 * last dirsep character in the canonicalized path. The part after that is what
 * is replaced.
 */
String MkLinks::renameSubst( const String &ip_param )
{
  if (renameKey && !linkFromKey)
  {
    String newParam = ip_param;
    Path::canonicalizeThis( newParam, false );
    Path::normalizeThis( newParam );
    renameLastDirsep = newParam.lastIndexOf( Path::DIR_SEPARATOR );
    newParam = newParam.substring( newParam.firstIndex(), 
                                   renameLastDirsep + 1 ) + renameName;
    return newParam;
  }
  else
    return ip_param;
}


/******************************************************************************
 * display prompt information
 *   Linking:
 *      from: $linkFromEntry
 *        to: $linkToPath
 * asking user confirmation. if yes, then continue, if not, then stop.
 * input: linkFromPath, linkToPath
 */
boolean MkLinks::verifyAction( const String &build_path, const String
                               &file_path) // throws MkLinksException
{
  if ( copyKey )
    Interface::print( queryKey ? "Planning to copy:" : "Copying:" );
  else
    Interface::print( queryKey ? "Planning to link:" : "Linking:" );

  Interface::print( String( "   From: " ) +
     Path::canonicalize( build_path, false ) );
  Interface::print( String( "     To: " ) +
     Path::canonicalize( file_path , false ) );

  if ( !autoKey ) // if -auto is set, don't wait user confirmation
    if ( !Interface::getConfirmation( "Is this correct ? ([y]/n)", true ) )
     { 
      if ( copyKey )
        Interface::print( "This file/dir will not be copied" );      
      else
        Interface::print( "This file/dir will not be linked" );      
      return(false);
     }

  return(true);
}


/******************************************************************************
 * This call populateTree() to tranverse the whole backing build tree
 */
void MkLinks::populateBackingBuild( boolean &copyflag, boolean &errorflag)
// throws MkLinksException
{
  boolean srcexist = false;
  StringArray used_list;   //a list of copied/linked files/directories
  StringArray empty_list;  //list of empty directories to be copied/linked

  filePromptList.clear();
  linkFromList.clear();
  if (backingChainDir.length() <= 1)
    throw MkLinksException( "No backing build!");
  if (linkFromKey)
  {
    for (int i = backingChainDir.firstIndex() + 1; // skip sandbox
      i <= backingChainDir.lastIndex(); i++)
    {
      if (!verifyAction( linkFromPath, linkToPath ))
        return;
      else
      {
        copyflag = true;
        populateTree( backingChainDir[i], relDir, i, srcexist );
      }
    }
  }

  else
  {
    for (int i = backingChainDir.firstIndex() + 1; // skip sandbox
      i <= backingChainDir.lastIndex(); i++)
    {
      StringArray file_list;
      StringArray temp1;
      String temp_path = backingChainDir[i] + Path::DIR_SEPARATOR + relDir;
      const String path1 = Path::filePath( temp_path );
      const String path2 = Path::fileName( temp_path );
      Path::getDirContents( path1 ,path2, &temp1 ); 
                     //temp has all the files matching the pattern
      for (int p=temp1.firstIndex(); p<=temp1.lastIndex(); p++)
      {
        if (!timecmpKey &&
            used_list[used_list.firstIndex()] != StringConstants::EMPTY_STRING)
        {
          for (int q=used_list.firstIndex(); q<=used_list.length(); q++)
          {
          //avoiding the files already copied/linked from another backing build
            if (SmartCaseString( temp1[p] ) == SmartCaseString( used_list[q] ))       
              break;
            else if (q == used_list.length())
              file_list.add(temp1[p]);
          }
        }
        else
          file_list.add(temp1[p]); 
      }

      //remove directories from empty_list which exist in file_list
      for (int g=file_list.firstIndex(); g<=file_list.length(); g++)
      { 
        for (int h=empty_list.firstIndex(); h<=empty_list.lastIndex(); h++)
        {
          if (Path::fileName(empty_list[h]) == file_list[g])
          {
            empty_list.removeAtPosition( h );
            break;
          }
        }
      }
            
    //file_list contains the files to be copied/linked in the present
    //backingbuild
      for (int r=file_list.firstIndex(); r<=file_list.lastIndex(); r++)
      {
        if (file_list[r] == StringConstants::EMPTY_STRING)
          break;
        printDir = Path::filePath( relDir ) + Path::DIR_SEPARATOR +
                 file_list[r];
        if (Path::isDirectory( backingChainDir[i] + Path::DIR_SEPARATOR +
             Path::filePath( relDir ) + Path::DIR_SEPARATOR + file_list[r] ))
        {
          srcexist = true;
          StringArray empdir;
          Path::getDirContents( backingChainDir[i] + Path::DIR_SEPARATOR +
                printDir, "*", &empdir );
          //if directory is empty look for it in the next backing build
          if (empdir.length() != 0) 
          {
            if (!verifyAction( backingChainDir[i] + Path::DIR_SEPARATOR +
               printDir, renameSubst( sboxDir + Path::DIR_SEPARATOR +
                                      printDir ) ))
              continue;
            else
              copyflag = true;
            if (empty_list.length() != 0)
              for (int t=empty_list.firstIndex(); t<=empty_list.length(); t++)
              {
                if (empty_list[t] == file_list[r])
                {
                  empty_list.removeAtPosition( t );
                  break;
                }
                else
                  continue;
              }
          }
          else 
          {
            if (empty_list.length() != 0)
              for (int k=empty_list.firstIndex(); k<=empty_list.length(); k++)
              {
                if (Path::fileName(empty_list[k]) == file_list[r])
                  break;
                else if (k == empty_list.length())
                  empty_list.add( backingChainDir[i] + Path::DIR_SEPARATOR +
                  printDir );
              }
            else
              empty_list.add( backingChainDir[i] + Path::DIR_SEPARATOR +
              printDir );
          }
        }
              
        else if(Path::isFile( backingChainDir[i] + Path::DIR_SEPARATOR +
            Path::filePath( relDir ) + Path::DIR_SEPARATOR + file_list[r] ))
        {
          srcexist = true;
          used_list.add(file_list[r]);
          filePromptList.put( printDir, 0 );
          copyflag = true;
        }
        populateTree( backingChainDir[i], Path::filePath( relDir ) +
                      Path::DIR_SEPARATOR + file_list[r], i, srcexist );
      }
    }
  }

  if (empty_list.length() != 0)
  {
    errorflag = true;
    for (int x=empty_list.firstIndex(); x<=empty_list.length(); x++)
      Interface::printWarning( CommandLine::getProgramName() + ": " +
                               empty_list[x] + " is empty !" );
  } 

  if (!srcexist)
  {
    errorflag = true;
    Interface::printWarning( CommandLine::getProgramName() + ": " +
                             relDir + " doesn't exist anywhere in the "
        "backing chain!" );
  }
}   


/******************************************************************************
 * This is core part of this class. It will populate one backing build tree
 * starting from base/reldir directory and store all files and directories
 * full path and relative path which satisfy the special rules in a
 * hashtable. The rules are: if the file or directory in backing
 * build doesn't exist in the hashtable, then store it. else skip it. But one
 * special situation is if the backing build is the first upper backing build,
 */
void MkLinks::populateTree( const String &base, const String &rel_dir,
    int layerlevel, boolean &srcexist )
//    throws MkLinksException
{
  String startdir;      // start directory to populate file tree
  StringArray dirlist;  // all file or dirctory names in current
  StringArray filelist; // start directory
  String reldir = rel_dir;
  String relpath;       // relative path by striping full path
                        // with base( backingdir or sboxdir )
  String fullpath;      // full path = base/relpath
  String sboxfullpath;  // corresponded full path in sandbox
                        // sboxfullpath = sboxDir/relpath

  startdir = base + Path::DIR_SEPARATOR + reldir;
  if (!Path::exists( startdir ))
    return;
  srcexist = true;

  if (Interface::isDebug())
    Interface::printDebug( " Populate_Tree_Base: " + startdir );

  if (Path::isDirectory( startdir ))
    Path::getDirContents( startdir, false, Path::CONTENTS_FILES,
        &filelist ); // 2nd param false = not full path
  else
  {
    filelist.add( Path::fileName( startdir ) );
    reldir.substringThis( STRING_FIRST_INDEX,
        reldir.lastIndex() - filelist[ARRAY_FIRST_INDEX].length() );
  }

  if (filelist.length() > 0) 
  {
    if (Interface::isDebug())
    {
      Interface::printDebug( "FileList: " );
      Interface::printDebug( filelist );
    }

    for (int i = filelist.firstIndex(); i <= filelist.lastIndex(); i++)
    {
      relpath = reldir + Path::DIR_SEPARATOR + filelist[i];
      fullpath = base + Path::DIR_SEPARATOR + relpath;
      if (linkFromList.containsKey( SmartCaseString( relpath ) ))
      {
        if (timecmpKey)
        {
          const SmartCaseString *tmpfullpath =
              linkFromList.get( SmartCaseString( relpath ) );
          if (Path::timeCompare( fullpath, *tmpfullpath ) > 0)
          {
            linkFromList.put( SmartCaseString( relpath ),
                SmartCaseString( fullpath ) );
          }
        }
      }
      else   // backing build, to compare with sandbox
      {
        sboxfullpath = sboxDir + Path::DIR_SEPARATOR + relpath;
        if (!Path::exists( sboxfullpath ))  // the file is not in sandbox, keep in list
          linkFromList.put( SmartCaseString( relpath ),
              SmartCaseString( fullpath ) );
        else if (norefreshKey || Path::isDirectory( sboxfullpath ))
          continue;
        else if (Path::isLink( sboxfullpath ) && 
                 (!copyKey || overKey || timecmpKey))
        {
          linkFromList.put( SmartCaseString( relpath ),
              SmartCaseString( fullpath ) );
        }
        else if (overKey||timecmpKey)  // sboxfullpath is file
        {
          linkFromList.put( SmartCaseString( relpath ),
              SmartCaseString( fullpath ) );
        }
      }
    } // end for
  }

  // get all sub directories
  Path::getDirContents( startdir, false, Path::CONTENTS_DIRS,
      &dirlist ); // 2nd param false = not full path
  if (dirlist.length() > 0)
  {
    if (Interface::isDebug())
    {
      Interface::printDebug( "=> " );
      Interface::printDebug( dirlist );
    }

    for (int i = dirlist.firstIndex(); i <= dirlist.lastIndex(); i++)
    {
      relpath = reldir + Path::DIR_SEPARATOR + dirlist[i];
      fullpath = base + Path::DIR_SEPARATOR + relpath;
      sboxfullpath = sboxDir + Path::DIR_SEPARATOR + relpath;
      if (!linkFromList.containsKey( SmartCaseString( relpath ) ))
      {
        if (!Path::exists( sboxfullpath ))
        {
          linkFromList.put( SmartCaseString( relpath ),
              SmartCaseString( fullpath ) );
          if ( !norecurseKey )
            populateTree( base, relpath, layerlevel, srcexist );
          continue;
        }
        else
        {
          if ( Path::isDirectory(sboxfullpath) )
          {
            if ( !norecurseKey )
              populateTree( base, relpath, layerlevel, srcexist );
            continue;
          }
          if ( Path::isLink(sboxfullpath) )
          {
            if ( norefreshKey )
              continue;
            linkFromList.put( SmartCaseString( relpath ),
                SmartCaseString( fullpath ) );
            if ( !norecurseKey )
              populateTree( base, relpath, layerlevel, srcexist );
            continue;
          }
          else // it is file in sandbox
            continue;
        }
      }
      else
      {
        const SmartCaseString *tmpfullpath =
            linkFromList.get( SmartCaseString( relpath ) );
        if (tmpfullpath != 0 && Path::isDirectory( *tmpfullpath ) &&
            !norecurseKey)
          populateTree( base, relpath, layerlevel, srcexist );
      }
    }
  }
}


/******************************************************************************
 *
 */
void MkLinks::createFiles() //  throws MkLinksException
{
  if (queryKey && !filePromptList.isEmpty())
  {
    createFilesPass( true, 1 );
    createFilesPass( true, 2 );
  }
  else
    createFilesPass( false, 1 );
}


/******************************************************************************
 *
 */
void MkLinks::createFilesPass( boolean twoPass, int passNum )
                                                //  throws MkLinksException
{
  HashKeyEnumeration< SmartCaseString, SmartCaseString >
      srctable( &linkFromList );
  boolean queryThisPass = queryKey && (!twoPass || passNum == 2);
  boolean checkFilePromptList = !filePromptList.isEmpty();

  while (srctable.hasMoreElements())
  {
    const SmartCaseString *srcrelpathptr = srctable.nextElement();
    if (srcrelpathptr == 0)
      continue;
    SmartCaseString srcrelpath = *srcrelpathptr;
    const SmartCaseString *srcfullpathptr = linkFromList.get( srcrelpath );
    if (srcfullpathptr == 0)
      continue;
    File srcfullpath(*srcfullpathptr,true);
    if (Path::isPrefix( srcfullpath, sboxDir ))
      continue;

    String dstpath( sboxDir + Path::DIR_SEPARATOR + srcrelpath.toString() );
    if (renameKey && !linkFromKey)
    {
      Path::canonicalizeThis( dstpath, false );
      Path::normalizeThis( dstpath );
      if (checkFilePromptList && filePromptList.containsKey( srcrelpath ))
        dstpath = renameSubst( dstpath );
      else
      {
        unsigned long lastDirsep = dstpath.indexOf( Path::DIR_SEPARATOR,
                                                    renameLastDirsep + 1 );
        if (lastDirsep == STRING_NOTFOUND)
          dstpath = dstpath.substring( dstpath.firstIndex(),
                                       renameLastDirsep + 1 ) + renameName;
        else
          dstpath = dstpath.substring( dstpath.firstIndex(),
                                       renameLastDirsep + 1 ) + renameName +
                                       dstpath.substring( lastDirsep );
      }
    }
    File dstfullpath( dstpath, true);

    // The newest file on the backing chain may still be older than the
    // current sandbox file
    if (timecmpKey && Path::exists( dstfullpath ) && 
        Path::timeCompare( dstfullpath, srcfullpath ) >= 0)
      continue;

    if (checkFilePromptList) 
    {
      if (filePromptList.containsKey( srcrelpath )) 
      {
        if (passNum == 2 || !verifyAction( srcfullpath, dstfullpath ))
          continue;
      }
      else
      {
        if (twoPass && passNum == 1)
          continue; // copy files that are not in filePromptList in pass 2
      }
    }

    if (queryThisPass)
    {
      String prompt = String( "Link " ) + srcfullpath + " ([y]/n) ?";
      if (copyKey)
        prompt = String( "Copy " ) + srcfullpath + " ([y]/n) ?";
      if (!Interface::getConfirmation( prompt, true ))
        continue;
    }

    if (Interface::isDebug())
    {
      Interface::printDebug( " Source Path => " + srcfullpath );
      Interface::printDebug( " Dest Path => " + dstfullpath );
    }

    if (infoKey)
    {
      Interface::printAlways( String( "Would" ) +
          ((copyKey) ? " copy " : " link ") +
          srcfullpath + " to " + dstfullpath + "." );
      continue;
    }

    if ( !dstfullpath.doesExist() )
    {
      if (srcfullpath.isDir())
      {
        Path::createPath( dstfullpath ); // we should create dest directory
        continue;                        // but we don't need copy file in it
      }
      else if (!Path::exists( Path::filePath( dstfullpath ) ))
        Path::createPath( Path::filePath( dstfullpath ) );
    }
    else if (dstfullpath.isDir())
      continue; // if dest directory already exists, we can delete it.

    if (dstfullpath.doesExist() && (dstfullpath.isLink() || !copyKey))
      Path::deletePath( dstfullpath );

    // right now we can copy or link file
    if ( copyKey )
    {
      try
      {
        // Remember to copy in binary mode, since mklinks may be
        // run for files in the object/tools/inst.images trees.
        Path::copyFile( srcfullpath, dstfullpath, true, 0, true );
        Path::chModifiedTime( srcfullpath, dstfullpath );
        if (Interface::isDebug())
          Interface::printDebug( "copy file: " + srcfullpath +
              " -> " + dstfullpath + " success " );
      }
      catch (IOException &e)
      {
        Interface::printWarning( CommandLine::getProgramName() + ": " +
                                 e.getMessage() );
        if (Interface::isDebug())
          Interface::printDebug( "copy file: " + srcfullpath +
              " -> " + dstfullpath + " failed " );
      }
    }
    else
    {
      boolean symlink_rc = Path::symLink( srcfullpath, dstfullpath );
      if (Interface::isDebug())
        Interface::printDebug( String( "copy/link file: " ) + srcfullpath +
                               " -> " + dstfullpath +
                               (symlink_rc ? " success " : " failed ") );
    }
  } // endfor
}


/******************************************************************************
 * it will be used for mksb to call
 * @param sbname the current sandbox name
 * @param rcname the current rcfile name
 * @param predir it is used to decide the destinative path to copy
 *  or link to there. The absolute path equals /sboxbase/sbname/predir/reldir.
 *  it will be "src", "obj", "tools"
 * @param reldir the relative path ( see predir discription )
 * @param mode it is either "c" ( copy ) or "l" (link)
 * @return if something are wrong, it will throw a MkLinksException to caller
 */
MkLinks::MkLinks( const String &sbname, const String &rcname,
                  const String &predir, const String &reldir,
                  const String &mode, boolean automatic )
                :args( 0 ), envs( 0 ), cmdline( 0 )// throws MkLinksException
{
  String direntry;
  boolean copyflag = false;
  boolean errorflag = false;
  if (!Env::isInited())
    throw MkLinksException( "Env not initialized" );

  // check args and set states
  if ( predir.length() == 0 )
    throw MkLinksException( "No pre_directory specified" );
  else
  {
    preDir = predir;
    Path::normalizeThis( preDir );
  }

  if ( reldir.length() == 0 )   // for predir is "tools", no reldir entry
    throw MkLinksException( "No directory specified" );
  else
  {
    direntry = reldir;
    Path::normalizeThis( direntry );
  }

  if ( mode.equalsIgnoreCase( "c" ) )
    copyKey = true;
  else if ( mode.equalsIgnoreCase( "l" ) )
    copyKey = false;
  else
    throw MkLinksException( String( "Not recognized mode: " ) + mode );

#ifdef NO_SYMLINKS
  copyKey = true; // for non Unix machine, no link
#endif

  // init the rest of the variables
  overKey = false;
  timecmpKey = false;
  norecurseKey = false;
  norefreshKey = false;
  autoKey = automatic;
  infoKey = false;
  queryKey = false;
  linkFromKey = false;
  linkToKey = false;
  renameKey = false;
  cmdline = 0;

  sboxName = sbname;
  rcFileName = rcname;

  // call the other methods to copy or link from backing build tree
  getSBoxInfo();
  getLinkFromToPaths( direntry );
  populateBackingBuild( copyflag, errorflag);
  createFiles();
}


/******************************************************************************
 *
 */
void MkLinks::printUsage() const
{
  Interface::printAlways( "Usage: mklinks [-copy] [-over] [-timecmp] [-query] "
      "[-norecurse]" );
  Interface::printAlways( "               [-norefresh] [-rename <newname>]" );
  Interface::printAlways( "               [ODE opts] [sb opts] "
      "<file | directory | abs_options>" );
  Interface::printAlways( "" );
  Interface::printAlways( "   ODE opts:" ); 
  Interface::printAlways( "       -quiet -normal -verbose -debug -usage "
      "-version -rev -info -auto" ); 
  Interface::printAlways( "" );
  Interface::printAlways( "   sb opts:" ); 
  Interface::printAlways( "       -sb <sandbox>, -rc <rcfile>" );
  Interface::printAlways( "" );
  Interface::printAlways( "   abs_options:" );
  Interface::printAlways( "       -link_from source_directory "
      "-link_to new_directory" );
}


/******************************************************************************
 *
 */
void MkLinks::printOptions()
{
  if (!Interface::isDebug())
    return;

  Interface::printDebug( String( "-copy: " ) + copyKey );
  Interface::printDebug( String( "-over: " ) + overKey );
  Interface::printDebug( String( "-timecmp: " ) + timecmpKey );
  Interface::printDebug( String( "-norecurse: " ) + norecurseKey);
  Interface::printDebug( String( "-norefresh: " ) + norefreshKey );
  Interface::printDebug( String( "-info: " ) + infoKey );
  Interface::printDebug( String( "-auto: " ) + autoKey );
  Interface::printDebug( String( "-link_from: " ) + linkFromEntry );
  Interface::printDebug( String( "-link_to: " ) + linkToEntry );
  Interface::printDebug( String( "-query: " ) + queryKey );
  Interface::printDebug( String( "-sb: " ) + sboxName );
  Interface::printDebug( String( "-rc: " ) + rcFileName );
  Interface::printDebug( String( "-rename: " ) + renameName );
}
