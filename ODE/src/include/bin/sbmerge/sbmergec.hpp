#ifndef _ODE_BIN_SBMERGE_SBMERGE_HPP
#define _ODE_BIN_SBMERGE_SBMERGE_HPP


#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/io/path.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"


class SbMerge : public Tool
{
  public:
    SbMerge( const char **argv, const char **envp )
        : Tool( envp ), args( argv ), cmdLine( 0 ),
          origEntry( "" ), merge_args( "" ), autoMerge( false ),
          mergeAll( false ), origProvided( false ), infoOnly( false )  {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~SbMerge() {};

  private:
    const char    **args;
    CommandLine   *cmdLine;
    StringArray   backingChain;
    StringArray   merge_files;
    String        origEntry;
    String        merge_args;
    boolean       autoMerge;
    boolean       infoOnly;
    boolean       mergeAll;
    boolean       origProvided;


    boolean userRequestMerge( String fileA, String fileB, String origFile );
    String  getOriginalPath( String filename );
    void    separateFilesAndMergeArgs();
    boolean processMerge( const String &mergeFile );
    void    processCommandLine();
    void    getBackingChain();
    String  getSbPath( const String &path );
};


#endif //_ODE_BIN_SBMERGE_SBMERGE_HPP
