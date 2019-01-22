/**
 * Makefile
 *
**/
#define _ODE_BIN_MAKE_MAKEFILE_CPP_

#include <base/binbase.hpp>
#include "bin/make/makefile.hpp"
#include "bin/make/mkcmdln.hpp"
#include "bin/make/dir.hpp"
#include "bin/make/passnode.hpp"
#include "bin/make/makec.hpp"
#include "bin/make/mkfilcac.hpp"

const int Makefile::hashsize = 20;

void Makefile::instantiate( PassNode &pass )
{
  try
  {
    PassNode::parse(pass, *this);
  }
  catch ( ParseException &e )
  {
    Make::error(e.toString());
    Make::quit("Fatal errors encountered -- cannot continue", 1);
  }
}
/**
 * This method loads a makefile but doesn't parse it into a target/dep graph.
 * @return Makefile, the caller shouldn't deallocate the returned pointer
 * @exception FileNotFoundException The exception description.
**/
//change the method parameters so that it take CondEvaluator as extra parameter
//and it will evalute all the global and command line variables for the if block
#ifdef __WEBMAKE__
Makefile *Makefile::load( const String &filename, const String &cwd,
                          const Dir &searchpath,CondEvaluator *condeval )
#else
Makefile *Makefile::load( const String &filename, const String &cwd,
                          const Dir &searchpath, boolean replContin )
#endif // __WEBMAKE__
{
  if (MkCmdLine::dDirs())
    Interface::printAlways( "Makefile cache: Trying to find " + filename );
#ifdef __WEBMAKE__
  boolean force_stat=false;
  //if we extract the include makefile, then we need to set force_stat flag
  //so that cachefile will use stat to verify the file exists
  const String *noExtractFlag=Env::getenv("noExtract");
  if (noExtractFlag==0 ||
     (noExtractFlag!=0 && ((*noExtractFlag).startsWith("0"))))
     force_stat=true;
#endif // __WEBMAKE__

#ifdef __WEBMAKE__
  CachedFile *filepath = searchpath.find(filename, cwd, force_stat);
#else
  CachedFile *filepath = searchpath.find(filename, cwd);
#endif // __WEBMAKE__

  if (filepath == 0)
    return ( 0 );

  Makefile *mf = MakefileCache::get(filepath->getUnixPath());
  if (mf == 0)
  {
    if (MkCmdLine::dDirs())
      Interface::printAlways( "Makefile cache: caching Makefile " +
          filepath->getUnixPath() );

    mf = new Makefile( filepath->getUnixPath() );
#ifdef __WEBMAKE__
    if (mf->loadInCache( condeval ) == 0)
#else
    if (mf->loadInCache( replContin ) == 0)
#endif // __WEBMAKE__
    {
      delete mf;
      return ( 0 );
    }
    MakefileCache::put( filepath->getUnixPath(), mf );
  }
  else if ( MkCmdLine::dDirs() )
    Interface::printAlways("Makefile cache: found Makefile in cache as " +
        filepath->getUnixPath() );

  return ( mf );
}

//change the method parameters so that it take CondEvaluator as extra parameter
//and it will evalute all the global and command line variables for the if block
#ifdef __WEBMAKE__
Makefile *Makefile::loadInCache( CondEvaluator *condeval )
#else
Makefile *Makefile::loadInCache( boolean replContin )
#endif // __WEBMAKE__
{
  MakefileStatement * mfs = 0;
  while (true)
  {
    try
    {
      if (mfs == 0)
      {
        addStatement( MakefileStatement( ) );
        mfs = (MakefileStatement *)lines.lastElement();
      }
#ifdef __WEBMAKE__
      if (getLine( condeval, false, false, mfs->getStringPtr() ) == 0)
        break;
#else
      if (getLine( false, false, mfs->getStringPtr(),
                   true, replContin ) == 0)
        break;
#endif // __WEBMAKE__

      if (mfs->getLineString()->length() == 0)
        continue;

      mfs->setData( getLastLineNumber(), getNextLineNumber(), this );

      // Reset the last MakefileStatement
      mfs = 0;
    }
    catch (IOException &e)
    {
      return ( 0 );
    }
  }
  close();
  return ( this );
}


