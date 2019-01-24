#include <iostream>
using namespace std ;
#include "base/binbase.hpp"
#include "lib/intcmds/mkdep.hpp"
#include "lib/exceptn/mkdepexc.hpp"
#include "lib/intcmds/misc.hpp"
#include "lib/intcmds/body.hpp"
#include "lib/intcmds/target.hpp"
#include "lib/intcmds/depmkfil.hpp"

#include "lib/portable/env.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/io/path.hpp"
#include "lib/io/ui.hpp"
#include "lib/io/cfgf.hpp"
#include "lib/io/file.hpp"
#include "lib/string/pattern.hpp"

String DEPLINE_SEPARATOR=":";
const String MkDep::WORD_SPLIT_STRING = " \t\r\n";

/******************************************************************************
 *  Intialize all const variables
 */
static const char * ODEDLLPORT SUFFIX_ARRAYTMP[] = {".u", ".d", 0};
const String      MkDep::DEFAULT_DEPMK_NAME     = "depend.mk";
const StringArray MkDep::DEFAULT_DEPFILE_SUFFIX =
    StringArray( SUFFIX_ARRAYTMP );
const String      MkDep::BACKED_SANDBOXDIR      = "BACKED_SANDBOXDIR";
const String      MkDep::MAKEOBJDIR             = "MAKEOBJDIR";
const String      MkDep::OBJECTDIR              = "OBJECTDIR";

/******************************************************************************
 *  MkDep::classMain (normally used by the mkdep command)
 */
int MkDep::classMain( const char **argv, const char **envp )
{
  MkDep md( argv, envp );
  return( md.run() );
}

/******************************************************************************
 * MkDep::run   (normally used by the mkdep command)
 */
int MkDep::run()
{
  try
  {
    parseCmdLine();
    if (depFileNames != 0 && depFileNames->size() > 0)
    {
      getEnvVariables();
      updateDepMk();
      removeDepFiles();
    }
  }
  catch (MkDepException &e)
  {
    Interface::printError( mkdepName + ": " + e.getMessage() );
    return 1;
  }
  return 0;
}

/******************************************************************************
 * MkDep::run   (normally used by the library)
 */
boolean MkDep::run( StringArray *pargs, const SetVars *const envs )
{
  MkDep mkdep;
  mkdep.args = pargs;
  try
  {
    mkdep.parseCmdLine();
    if (mkdep.depFileNames != 0 && mkdep.depFileNames->size() > 0)
    {
      mkdep.getEnvVariables();
      mkdep.updateDepMk();
      mkdep.removeDepFiles();
    }
  }
  catch (MkDepException &e)
  {
    Interface::printError( mkdep.mkdepName + ": " + e.getMessage() );
    return false;
  }
  return true;
}

/**
 *
**/
void MkDep::parseCmdLine()
{
  const char *s[] = {"-t", "-top", "-e", "-elxdep", "-qb",
      "-r", "-rm", "-abs", 0};
  StringArray  states( s );
  const char *qs[] = {"-file", "-suff", "-I", "-K", "-subst", "-elpdep",
                      "-E", 0};
  StringArray  qvars( qs );

  StringArray  depfilepaths;
  StringArray  *tmpvar=0;
  StringArray  splitter;

  // initialize commandline class
  cmdLine = new CommandLine( &states, &qvars, true, *args, true, *this,
                             !isCommand, isRuntimeCommand );
  cmdLine->process();

  // get all states
  topKey    = cmdLine->isState( "-top" )    | cmdLine->isState( "-t" );
  elxdepKey = cmdLine->isState( "-elxdep" ) | cmdLine->isState( "-e" );
  rmKey     = cmdLine->isState( "-rm" )     | cmdLine->isState( "-r" );
  absKey    = cmdLine->isState( "-abs" );     
  quoteBlanks = cmdLine->isState( "-qb" );

  // get all option's value
  depMkName = DEFAULT_DEPMK_NAME;

  if ((tmpvar = cmdLine->getQualifiedVariables( "-file" )) != 0)
  {
    depMkName = (*tmpvar)[tmpvar->firstIndex()];
    Path::normalizeThis( depMkName );
  }

  if ((tmpvar = cmdLine->getQualifiedVariables( "-subst" )) != 0)
  {
    StringArray tmpsubst;
    for (int i = tmpvar->firstIndex(); i <= tmpvar->lastIndex(); ++i)
    {
      SetVars::separateVarFromVal( (*tmpvar)[i], &tmpsubst );
      if (tmpsubst[ARRAY_FIRST_INDEX] != StringConstants::EMPTY_STRING)
        substStrings.add( tmpsubst );
    }
  }

  // get -E targetPattern:sourcePattern pairs.
  // The ith targetPattern:sourcePattern pair is stored in
  // patterns[i][ARRAY_FIRST_INDEX] and patterns[i][ARRAY_FIRST_INDEX+1].
  eKey = false;
  if ((tmpvar = cmdLine->getQualifiedVariables( "-E" )) != 0)
  {
    StringArray tmppats( 2, 2 );
    for (int i = tmpvar->firstIndex(); i <= tmpvar->lastIndex(); ++i)
    {
      // unixize pattern first (backslashes are NOT escape characters)
      Path::unixizeThis( (*tmpvar)[i] );
      (*tmpvar)[i] = Pattern::backslashQuotedWildcards( (*tmpvar)[i] );
      // multiple args can be delimited by commas...
      (*tmpvar)[i].split( ',', 0, &splitter );

      for (int j = splitter.firstIndex(); j <= splitter.lastIndex(); ++j)
      {
        splitter[j].split( ':', 2, &tmppats );
        if (tmppats.size() < 1)
          continue;
        else if (tmppats.size() < 2)
          tmppats.prepend( StringConstants::EMPTY_STRING );
        else
          tmppats[ARRAY_FIRST_INDEX].dequoteThis();
        tmppats[ARRAY_FIRST_INDEX+1].dequoteThis();
        patterns.add( tmppats );
        eKey = true;
        if (Interface::isDebug())
        {
          Interface::printDebug( mkdepName + ": -E pattern added - " +
              tmppats[ARRAY_FIRST_INDEX] + ":" +
              tmppats[ARRAY_FIRST_INDEX+1] );
        }
      }
    }
  }

  // get depfile suffix
  if ((tmpvar = cmdLine->getQualifiedVariables( "-suff" )) == 0)
  {
    for (int i = DEFAULT_DEPFILE_SUFFIX.firstIndex();
        i <= DEFAULT_DEPFILE_SUFFIX.lastIndex(); i++)
      depFileSuffix.addElement( SmartCaseString( DEFAULT_DEPFILE_SUFFIX[i] ) );
  }
  else
  {
    Misc::copyArrayToVector( *tmpvar, depFileSuffix );
  }
  // get keep directories
  kKey = false;
  if ((tmpvar = cmdLine->getQualifiedVariables( "-K" )) != 0)
  {
    kKey = true;
    Misc::normalizePaths( *tmpvar );
    for (int i = ARRAY_FIRST_INDEX; i <= tmpvar->lastIndex(); ++i)
      if (!(*tmpvar)[i].endsWith( Path::DIR_SEPARATOR ))
        (*tmpvar)[i] += Path::DIR_SEPARATOR;
    Misc::copyArrayToVector( *tmpvar, keepDirs );
  }

  // get remove directories
  iKey = false;
  if ((tmpvar = cmdLine->getQualifiedVariables( "-I" )) != 0)
  {
    iKey = true;

    Misc::normalizePaths( *tmpvar );
    for (int i = ARRAY_FIRST_INDEX; i <= tmpvar->lastIndex(); ++i)
      if (!(*tmpvar)[i].endsWith( Path::DIR_SEPARATOR ))
        (*tmpvar)[i] += Path::DIR_SEPARATOR;
    Misc::copyArrayToVector( *tmpvar, removeDirs );
  }

  // get elpdep suffixes
  elpdepKey = false;
  if ((tmpvar = cmdLine->getQualifiedVariables( "-elpdep" )) != 0)
  {
    for (int i = tmpvar->firstIndex(); i <= tmpvar->lastIndex(); ++i)
    {
      (*tmpvar)[i].split( ',', 0, &splitter );
      for (int j =splitter.firstIndex(); j <= splitter.lastIndex(); ++j)
      {
        splitter[j].dequoteThis();
        if (splitter[j].length() < 2 || !splitter[j].startsWith( "." ))
        {
          if (isCommand)
            printUsage();
          throw MkDepException( "incorrect -elpdep suffix '" +
                                splitter[j] + "'" );
        }
        else
        {
          elpdepKey = true;
          removeSuffs.addElement( splitter[j] );
          if (Interface::isDebug())
          {
            Interface::printDebug( mkdepName + ": -elpdep suffix added - " +
                splitter[j] );
          }
        }
      }
    }
  }

  //get dependent file names or directories
  depfilepaths = cmdLine->getUnqualifiedVariables();
  if (depfilepaths.length() == 0)
  {
    if (isCommand)
      printUsage();
    throw MkDepException( "No directory or file given!" );
  }
  else
    Misc::normalizePaths( depfilepaths );

  getDepFileNames( depfilepaths );

  // simplify -K -I -elxdep combination
  if (kKey && !iKey && !elxdepKey)
    kKey = false;
  if (kKey &&  iKey && !elxdepKey)
    kKey = false;
  if (kKey && iKey && elxdepKey)
  {
    for (int index=keepDirs.firstIndex(); index<=keepDirs.lastIndex(); index++)
    {
      if (removeDirs.contains( *keepDirs.elementAt( index ) ))
      {
        keepDirs.removeElementAt( index );
        if (index == keepDirs.lastIndex()) // at the end
          break;
        index--;
      }
    }
  }
  if (keepDirs.isEmpty())
    kKey = false;

  // tmpvar is intentionally leaked for speed
}

/**
 *
**/
void MkDep::getDepFileNames( const StringArray &paths )
    // throw (MkDepException)
{
  String this_suffix;
  Array< File > *content_test;

  if (paths.length() == 0)  
    return;

  int index = paths.firstIndex();
  for (; index <= paths.lastIndex(); index++)
  {
    if (!Path::exists( paths[index] ))
    {
      Interface::printWarning( mkdepName + ": " + paths[index] + 
          " doesn't exist." );
    }
    else if (Path::isFile( paths[index] ))
    {
      if (depFileSuffix.contains( SmartCaseString(
          Path::fileSuffix( paths[index], true ) ) ))
      {
        if (depFileNames == 0)
          depFileNames = new Array< File >;
        depFileNames->append( File( paths[index], true ) );
      }
      else
        Interface::printWarning( mkdepName + ": the suffix of " + paths[index] + 
            " is not supported." );
    }
    else /* directory */
    {
      for (int i = ARRAY_FIRST_INDEX; i <= depFileSuffix.lastIndex(); i++)
      {
        content_test = Path::getDirContentsAsFiles( paths[index],
            Path::CONTENTS_FILES, depFileNames, 0,
            StringConstants::STAR_SIGN + depFileSuffix[i] );
        if (depFileNames == 0)
          depFileNames = content_test;
      }
    }
  } // endfor

  if (depFileNames != 0)
    filterDepFiles();
}

/**
 *
**/
void MkDep::filterDepFiles()
{
  String suffix1, suffix2;
  String root1, root2;
  int order1, order2;

  if (depFileNames->length() <= 1)
    return;

  for (int i=depFileNames->firstIndex(); i<=depFileNames->lastIndex(); i++)
  {
    suffix1 = Path::fileSuffix( (*depFileNames)[i], true );
    root1 = Path::fileRoot( (*depFileNames)[i], true );
    order1 = depFileSuffix.indexOf( SmartCaseString( suffix1 ) );
    for (int j=i+1; j<=depFileNames->lastIndex(); j++)
    {
      suffix2 = Path::fileSuffix( (*depFileNames)[j], true );
      root2 = Path::fileRoot( (*depFileNames)[j], true );
      order2 = depFileSuffix.indexOf( SmartCaseString( suffix2 ) );
      if (SmartCaseString( root1 ).equals( root2 ))
      {
        if (order1 >= order2) // the larger the order, the smaller the priority
          (*depFileNames)[i] = (*depFileNames)[j];

        depFileNames->removeAtPosition( j );
        j--; // loop will increment this
      } /* end if */
    } /* end for */
  } /* end for */
}

/******************************************************************************
 */
void MkDep::getEnvVariables()
{
  String  sourcedir;
  const String  *backedsbdirs = Env::getenv( BACKED_SANDBOXDIR );
  StringArray *strarrtmp=0;
  const String &currentdir = Path::getcwd();

  if (backedsbdirs == 0) // not in sandbox and backing build envrionment
  {
    sandboxBase = StringConstants::EMPTY_STRING;
    relCurDir = currentdir;
    return;
  }

  // In the sandbox environment
  strarrtmp = Path::separatePaths( *backedsbdirs );
  if (strarrtmp == 0)
    backedDirs = StringArray();
  else
    backedDirs = *strarrtmp;

  // srcsuff is added to  backedDirs
  String srcsuff = Path::DIR_SEPARATOR;
  srcsuff += SandboxConstants::getSRCNAME();
  srcsuff += Path::DIR_SEPARATOR;

  // objsuff is used to make backedObjDirs unless objectDir is absolute
  // and is not inside the sandbox. 
  String objsuff;
  objectDir = Env::getenv( MAKEOBJDIR );
  if (objectDir == 0)
    objectDir = Env::getenv( OBJECTDIR );
  if (objectDir != 0)
    objsuff = srcsuff + *objectDir + Path::DIR_SEPARATOR;

  // build backedSrcDirs and backedObjDirs from backedDirs and ensure that all
  // end in DIR_SEPARATOR (this eases and quickens dirsep boundary
  // comparisons later on).
  backedSrcDirs = StringArray( backedDirs.length() );
  boolean objectDirIsAbsolute = (objectDir != 0 ?
                                 Path::absolute( *objectDir ) : true);
  backedObjDirs = StringArray( objectDirIsAbsolute ? 1 : backedDirs.length() );
  if (objectDirIsAbsolute && objectDir != 0)
  {
    backedObjDirs.add( Path::normalize( *objectDir ) );
    if (!backedObjDirs[backedObjDirs.firstIndex()].endsWith(
                                                      Path::DIR_SEPARATOR ))
      backedObjDirs[backedObjDirs.firstIndex()] += Path::DIR_SEPARATOR;
  }
  for (int i = backedDirs.firstIndex(); i <= backedDirs.lastIndex(); i++)
  {
    Path::normalizeThis( backedDirs[i] );
    if (!backedDirs[i].endsWith( Path::DIR_SEPARATOR ))
      backedDirs[i] += Path::DIR_SEPARATOR;
    backedSrcDirs.add( Path::normalize( backedDirs[i].substring(
        STRING_FIRST_INDEX, backedDirs[i].lastIndex() ) + srcsuff ) );
    if (! objectDirIsAbsolute && objectDir != 0)
    {
      backedObjDirs.add( Path::canonicalize( backedDirs[i].substring(
          STRING_FIRST_INDEX, backedDirs[i].lastIndex() ) + objsuff, false ) );
      if (!backedObjDirs[i].endsWith( Path::DIR_SEPARATOR ))
        backedObjDirs[i] += Path::DIR_SEPARATOR;
    }
  }
  
  if (Interface::isDebug())
  {
    Interface::printDebug( mkdepName + ": backedDirs:" );
    Interface::printArray( backedDirs, Path::PATH_SEPARATOR );
    Interface::printDebug( mkdepName + ": backedSrcDirs:" );
    Interface::printArray( backedSrcDirs, Path::PATH_SEPARATOR );
    if (backedObjDirs.length() > 0)
    {
      Interface::printDebug( mkdepName + ": backedObjDirs:" );
      Interface::printArray( backedObjDirs, Path::PATH_SEPARATOR );
    }
  }

  // first backed_sourcedir is sandbox or backing build
  if (backedDirs.length() != 0)
    sandboxBase = backedDirs[backedDirs.firstIndex()];
  SmartCaseString sbbase_canon( sandboxBase );
  Path::fullyCanonicalize( sbbase_canon );
  if (sbbase_canon.length() > 0 && currentdir.startsWith( sbbase_canon ) &&
      (currentdir.length() == sbbase_canon.length() ||
      currentdir.charAt( sbbase_canon.lastIndex() + 1 ) ==
      Path::DIR_SEPARATOR.charAt( STRING_FIRST_INDEX )))
  {
    if (currentdir.length() == sbbase_canon.length())
      relCurDir = ".";
    else
      relCurDir = currentdir.substring( sbbase_canon.lastIndex() + 2 );
  }
  else
  {
    // Assume we are working outside the sandbox
    sandboxBase = StringConstants::EMPTY_STRING;
    relCurDir = currentdir;
    return;
  }

  // strarrtmp is intentionally leaked for speed
}

/************************************************************************
 *
 * updateDepMk()
 *
 *  Get the contents of current depend.mk file.
 *  Store it in form of Targets in the TargetCollection
 *
 *  Builds targets  from new dependency files and update the 
 *  collection of all targets. depmkfile holds the vector of targets.
 *
 *
 * ********************************************************************* */
void MkDep::updateDepMk( )
{
  if (depFileNames==0)
    return;

  DepMkFile depmkfile( depMkName, mkdepName );

  depmkfile.load( sandboxBase, relCurDir, backedDirs );

  for (int i=depFileNames->firstIndex(); i<=depFileNames->lastIndex(); i++)
    buildTarget( (*depFileNames)[i], depmkfile );
 
  if (cmdLine->isState( "-info" ))
    Interface::printAlways( mkdepName + ": Would create " + depMkName );
  else
    depmkfile.save();
}

/************************************************************************
 *
 * buildTarget()
 * 
 * Build targets from dependency files.
 *
 * **********************************************************************/
void MkDep::buildTarget( const String&  depFileName , DepMkFile& dependMkFile)
{

  ConfigFile depfile( depFileName );
  int    index;
  String line;
  String targetName, savedTarget, depline, subline;
  Target* tgt=0;
  Target* const* ctgt;
  
  try {

  while (depfile.getLine( true, false, &line ) != 0)
  {
    //Make sure I read at least a line, that is not a comment line
    if (line.length() != 0)
    {
      //Try to find DEPLINE_SEPARATOR
      index = line.indexOf(DEPLINE_SEPARATOR);

      // Don't allow a dependency line without a ':' and don't allow a
      // dependency to start with a ':'.
      //
      if ((index == STRING_NOTFOUND) || (index == line.firstIndex()) )
        throw MkDepException( 
          String("Format of dependent file: '" + depFileName +
               "' is wrong ! Either missing a '" + DEPLINE_SEPARATOR + 
               "' or '" + DEPLINE_SEPARATOR + 
               "' is first character in dependency line " +
               String( depfile.getLineNumber() ) + ".") );

      // ':' can be used in drive names on non-UNIX systems.
      // Make sure I'm not confusing DEPLINE_SEPARATOR with drive letter.
      // Only handles cases like C:\foo.exe: foo.obj
      if (index == STRING_FIRST_INDEX+1)
      {
        subline = line.substring( index+1 );
        if ((index = subline.indexOf( DEPLINE_SEPARATOR )) == STRING_NOTFOUND)
          throw MkDepException( 
              String("Format of dependent file: '" + depFileName +
              "' is wrong ! Either missing a '" + DEPLINE_SEPARATOR + 
              "' or '" + DEPLINE_SEPARATOR + 
              "' is first character in dependency line " +
              String( depfile.getLineNumber() ) + ".") );

        index +=2;
      }

      // This is a valid target line: 
      // Now seperate targetName from dependencies.

      targetName = line.substring( STRING_FIRST_INDEX, index );
#if FILENAME_BLANKS
      targetName.dequoteThis();
#endif
      targetName.trimThis();
    
      if ((Path::absolute( targetName )) && (!absKey))
        targetName = Path::fileName( targetName );
      else
        Path::normalizeThis( targetName ); // Convert '\' to '/'

      // Add ${MAKETOP}, if necessary
      targetName = normalizeLine( targetName, StringConstants::EMPTY_STRING );
      savedTarget = targetName;
      Path::unixizeThis( targetName );

      targetName += DEPLINE_SEPARATOR;
      ctgt = dependMkFile.get( targetName );
      if (ctgt == 0)
      {
        tgt = new Target( targetName, new Body(), false );
        dependMkFile.appendTarget( tgt );
        ctgt = &tgt;
      }
      else if ((*ctgt)->isFromOldFile())
        (*ctgt)->purgeForUpdate();
      StringArray deplines;
#if FILENAME_BLANKS
      // Assume that blanks are part of the file name, so there must only
      // be one filename.  If *.u file came from gendep, it would be quoted.
      // If the *.u file came from Visual Age icc /qmakedep then it would
      // not be quoted.
      if (quoteBlanks) // if all filename blanks will be doublequoted
        deplines.add(line.substring( index + 1 ).dequoteThis().trimThis());
      else
        line.substring( index + 1 ).split( " \t", 0, &deplines );
#else
      line.substring( index + 1 ).split( " \t", 0, &deplines );
#endif

      for (int i = deplines.firstIndex(); i <= deplines.lastIndex(); ++i)
      {
        // Convert '\' to '/'
        depline = deplines[i];
#if FILENAME_BLANKS
        depline.dequoteThis().trimThis();
#endif
        Path::normalizeThis( depline );

        // Add ${MAKETOP}, if necessary
        depline = normalizeLine( depline, savedTarget );

        if (depline.length() !=0)
        {
          Path::unixizeThis( depline );
          for (int j = substStrings.firstIndex();
              j <= substStrings.lastIndex(); ++j)
          {
            depline.replaceThis( substStrings[j][ARRAY_FIRST_INDEX],
                substStrings[j][ARRAY_FIRST_INDEX + 1] );
          }
          depline = targetName + "  " +  depline;
          // Store the target into the collection.
          ((*ctgt)->getBody())->addElement( depline );
        }
      }
    }
  } /* while */
  } /* try */
  catch (FileNotFoundException &e1)
  {
   //throw a MkDepException. 
   throw MkDepException( 
      String("Dependent file: "+depFileName+" doesn't exist!") );
  }
  catch (IOException &e2)
  {
   throw MkDepException( 
     String("Reading Dependent file: "+depFileName+" failed!") );
  }
}


/**********************************************************************
 *
 *  normalizeLine()
 * 
 *  Adds ${MAKETOP} if necessary.
 *  Can eliminate target:source pair depending upon command line options.
 * 
 * ******************************************************************** */
String MkDep::normalizeLine( const String &line, const String &target )
{
  int i;

  // -elpdep option
  if (elpdepKey && target != StringConstants::EMPTY_STRING &&
      !elpdepTargs.contains( target ) &&
      SmartCaseString( Path::fileRoot( target, true ) ) ==
          Path::fileRoot( line, true ))
  {
    SmartCaseString sourceSuffix( Path::fileSuffix( line, true ) );
    for (i = removeSuffs.firstIndex(); i <= removeSuffs.lastIndex(); ++i)
    {
      if (sourceSuffix == removeSuffs[i])
      {
        if (Interface::isDebug())
        {
          Interface::printDebug( mkdepName + ": removing " + target +
              " : " + line + " due to -elpdep suffix " + removeSuffs[i] );
        }
        elpdepTargs.addElement( target );
        return (StringConstants::EMPTY_STRING);
      }
    }
  }

  // -E option
  // If patterns are unixized by the user and '/' is treated like
  // ordinary characters when matching, we only need to match a filepath
  // against a single pattern, rather than matching subparts of a filepath
  // against subpatterns. It gives a much clearer semantics for matching.
  // We will need to unixize the target and line (source) before comparison.
  if (eKey && target != StringConstants::EMPTY_STRING)
  {
    String trg = Path::unixize( target );
    String src = Path::unixize( line);
    StringArray pat( 2, 2 );
    for (i = patterns.firstIndex(); i <= patterns.lastIndex(); ++i)
    {
      pat = patterns[i];
      if (pat[ARRAY_FIRST_INDEX].length() > 0)
      {
        if (!Pattern::isMatching( pat[ARRAY_FIRST_INDEX], trg,
                                  PlatformConstants::onCaseSensitiveOS() ))
          continue;
      }
      if (Pattern::isMatching( pat[ARRAY_FIRST_INDEX+1], src,
                                PlatformConstants::onCaseSensitiveOS() ))
      {
        if (Interface::isDebug())
        {
          Interface::printDebug( mkdepName + ": removing " + target +
              " : " + line + " due to -E " + pat[ARRAY_FIRST_INDEX] + ":" +
              pat[ARRAY_FIRST_INDEX+1] );
        }
        return (StringConstants::EMPTY_STRING);
      }
    }
  }

  // -Top option
  if (topKey)
  {
    if ((i = lineStartsWith( backedSrcDirs, line )) != STRING_NOTFOUND)
      return (String( "${MAKETOP}" ) + line.substring(
          backedSrcDirs[i].lastIndex() + 1 ));

    if (objectDir != 0 && 
         (i = lineStartsWith( backedObjDirs, line )) != STRING_NOTFOUND)
      return (String( "${MAKETOP}" ) + line.substring(
          backedObjDirs[i].lastIndex() + 1 ));

    if ((i = lineStartsWith( backedDirs, line )) != STRING_NOTFOUND)
      return (String( "${MAKETOP}.." ) + line.substring(
          backedDirs[i].lastIndex() ));
  }

  // -I option
  if (iKey)
  {
    if ((i = lineStartsWith( removeDirs, line )) != STRING_NOTFOUND)
      return (line.substring( removeDirs.elementAt( i )->lastIndex() + 1 ) );
  }

  if (!Path::absolute( line ))
    return (line);

  //  -elxdep option
  if (elxdepKey && !kKey)
  {
    if (lineStartsWith( backedSrcDirs, line ) != STRING_NOTFOUND)
      return (line);
    else
      return (StringConstants::EMPTY_STRING);
  }

  // -elxdep and -K options
  if (elxdepKey && kKey)
  {
    if (lineStartsWith( backedSrcDirs, line ) != STRING_NOTFOUND)
      return (line);
    else
    {
      if (lineStartsWith( keepDirs, line ) != STRING_NOTFOUND)
        return (line);
      else
        return (StringConstants::EMPTY_STRING);
    }
  }

  // no -top -K -I -elxdep options
  return (line);
}

/**********************************************************************
 */
int MkDep::lineStartsWith( const StringArray &heads, const String &line )
{
  if (heads.length() == 0) return STRING_NOTFOUND;

  for (int i=heads.firstIndex(); i<=heads.lastIndex(); i++) {
    if (Misc::stringStartsWith( line, heads[i]) )
      return i;
  }

  return STRING_NOTFOUND;
}

/**********************************************************************
 */
int MkDep::lineStartsWith( const Vector<String> &heads, const String &line )
{
  if (heads.length() == 0) return STRING_NOTFOUND;

  for ( int i=heads.firstIndex(); i<=heads.lastIndex(); i++)
    if (Misc::stringStartsWith( line, *heads.elementAt(i) ))
      return i;

  return STRING_NOTFOUND;
}


/**********************************************************************
 *
 *  removeDepFiles()
 * 
 *  removes the depend files if necessary.
 *
 * 
 * ******************************************************************** */
void MkDep::removeDepFiles()
{
  if (rmKey && depFileNames)
  {
    for (int i = ARRAY_FIRST_INDEX; i <= depFileNames->lastIndex(); i++)
    {
      if (cmdLine->isState( "-info" ))
        Interface::printAlways( mkdepName + ": Would delete " +
                                (*depFileNames)[i] );
      else
        Path::deletePath( (*depFileNames)[i] );
    }
  }
}

/***********************************************************************
 *
 * printUsage()
 *
 * sends  usage output to stdout.
 *
 *
 * ********************************************************************* */
void MkDep::printUsage() const
{
  Interface::printAlways( "Usage: " + mkdepName + " [-I include_directory] "
      "[-K include_directory] [-r[m]] [-t[op]]" );
  Interface::printAlways( "             [-e[lxdep]] [-abs] [-file filename] "
      "[-suff target_suffix] [-qb]" );
  Interface::printAlways( "             [-elpdep suffix[, ...]] " 
      "[-E [targ_pattern:]dep_pattern[, ...]]" );
  Interface::printAlways( "             [-subst oldval=newval] [ODE options] "
      "file..." );
  Interface::printAlways( StringConstants::EMPTY_STRING );
  if (isCommand)
  {
    Interface::printAlways( "   ODE options:" ); 
    Interface::printAlways( "       -quiet -normal -verbose -debug -usage "
        "-version -rev -info -auto" );
    Interface::printAlways( StringConstants::EMPTY_STRING );
  }
  Interface::printAlways( "       file: file or directory to process" );
}

/***********************************************************************
 *
 * printOptions()
 *
 * Sends options to stdout.
 *
 *
 *
 * ********************************************************************* */
void MkDep::printOptions()
{
  const String truestr ="true";
  const String falsestr="false";
  Interface::printAlways( "top:    " + ((topKey)    ? truestr : falsestr ));
  Interface::printAlways( "rm:     " + ((rmKey)     ? truestr : falsestr ));
  Interface::printAlways( "elxdep: " + ((elxdepKey) ? truestr : falsestr ));
  Interface::printAlways( "abs: " + ((absKey) ? truestr : falsestr ));
  Interface::printAlways( "file:   " + depMkName);
}
