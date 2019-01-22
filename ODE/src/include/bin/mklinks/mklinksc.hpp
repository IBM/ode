#ifndef _ODE_BIN_MKLINKS_MKLINKSC_HPP_
#define _ODE_BIN_MKLINKS_MKLINKSC_HPP_

#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/string/strarray.hpp"
#include "lib/io/cmdline.hpp"
#include "bin/mklinks/mklinkex.hpp"

/**
 * A Class to copy or link code from a backing build or shared sandbox to
 * the development sandbox. It can creates the necessary tree structure for
 * any files.
**/
class MkLinks : public Tool
{
  public:

    MkLinks( const char **argv, const char **envp ) :
        Tool( envp ), PROGRAM_NAME( "mklinks" ),
        args( argv ), envs( envp ), cmdline( 0 ) {};

    // this constructor is to used only by mksb
    MkLinks( const String &sbname, const String &rcname,
             const String &predir, const String &reldir,
             const String &mode, boolean automatic ); // throws MkLinksException

    static int classMain( const char **argv, const char **envp );
    int run();
    String toString() const;
    void printUsage() const;

    ~MkLinks()
    {
      delete cmdline;
      cmdline = 0;
    };


  private:

    const String PROGRAM_NAME;
    const char **args;
    const char **envs;
    CommandLine *cmdline;

    // keys set on command line, usually they just are states
    boolean copyKey;
    boolean overKey;
    boolean timecmpKey;
    boolean norecurseKey;
    boolean norefreshKey;
    boolean infoKey;
    boolean autoKey;
    boolean linkFromKey;
    boolean linkToKey;
    boolean queryKey;
    boolean renameKey;

    // variables following key on command line
    String sboxName;
    String rcFileName;
    String linkFromEntry; // following with -link_from
    String linkToEntry;   // following with -link_to
    StringArray dirEntry;      // the directory entry
    String renameName;    // follows -rename
    unsigned long renameLastDirsep; // substitute renameName after this

    // shell and sandbox variables
    String        sboxDir;       // current development sandbox dir
    String        relDir;        // linkToPath = sboxDir/relDir
    String        backingDir;    // first upper layer backing build
    String        printDir;
    StringArray   backingChainDir;

    // other variables
    String linkFromPath;  // the directory that link from
    String linkToPath;    // the directory that link to
    Hashtable< SmartCaseString, SmartCaseString > linkFromList;
                                  // store the relative path and full
                                  // path of all files in backing build
                                  // that satisfies -over, -refresh, -timecmp,
                                  // requirement. Key is relative path
                                  // after backingDir of each full path
                                  // Value is full path
    Hashtable< SmartCaseString, int > filePromptList;
                    // Store the relative path of just the files
                    // that are specified on the command line, which
                    // therefore always will have a prompt done for them.
    String preDir;  // the previous directory before dirEntry
                    // if dirEntry is absolute path, so that
                    // relDir = preDir/dirEntry.
                    // It will also be used by mksb.
                    // It can be "src", "obj", "tools",
                    // but default is SRCNAME used by mklinks
                    // its own.

    void parseCmdLine();
    void getSBoxInfo();
    void getLinkFromToPaths( const String &ip_param);
    String renameSubst( const String &ip_param );
    boolean verifyAction( const String &build_path, const String &file_path);
    void populateBackingBuild( boolean &copyflag, boolean &errorflag);
    void populateTree( const String &base, const String &reldir,
        int layerlevel, boolean &srcexist );
    void createFiles();
    void createFilesPass( boolean twoPass, int passNum );
    void printOptions();
};

#endif /* _ODE_BIN_MKLINKS_MKLINKSC_HPP_ */
