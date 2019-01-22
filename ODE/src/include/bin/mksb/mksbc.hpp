#ifndef _ODE_BIN_MKSB_MKSB_HPP_
#define _ODE_BIN_MKSB_MKSB_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/io/cmdline.hpp"
#include "lib/io/sandbox.hpp"


class Mksb : public Tool
{
  public:

    Mksb( const char **argv, const char **envp,
          const String program = "mksb", const boolean makebb = false )
        : Tool( envp ), args( argv ), envs( envp ), cmdLine( 0 ), sb( 0 ),
          program_name( program ), making_backing_build( makebb ) {};

    static int classMain( const char **argv, const char **envp );
    int        run();
    void       printUsage() const;

    ~Mksb()
    {
      delete cmdLine;
    };


  protected:  // mkbb needs access to all these

    const char   **args;
    CommandLine  *cmdLine;

    void        checkOperationMode( boolean make_backing_build );

  private:

    const char   **envs;
    String       program_name, backing_dir_uneval;
    boolean      making_backing_build;
    Sandbox      *sb;
    boolean      sandbox_blist;

    void        checkCommandLine();
    void        listSandboxes() const;
    void        deleteSandbox();
    StringArray *getSbListFromRCFile( StringArray *sandboxes = 0 ) const;
    boolean     removeFromBuildList( const Sandbox &sbox ) const;
    String      addToBuildList( const Sandbox &sbox ) const;
    void        upgradeSandbox();
    void        makeSandbox();
    String      getSbName() const;
    String      getBackingDir( const String *sandbox_path_ptr = 0 );
    String      getProperBackingBuild( const String &dir,
                                       const BuildListConfigFile *buildList,
                                       const String *sandbox_path_ptr );
    StringArray *makeMKSBList( const String &backingDir, const String &basedir,
                              StringArray *buffer = 0) const;
    String      getBaseDirForMKSB( SandboxRCConfigFile *sbrc,
        const StringArray &mksb_list ) const;
    String      getMachinesForMKSB( String machine,
                                    const String &backingDir,
                                    SandboxRCConfigFile *sbrc ) const;
    String      getMode( const String &mode ) const;
    String      fromMKSBList( const StringArray &mksb, const String &key,
                         int offset) const;
    void        buildDirStructure( const String &machineList) const;
    boolean     createLinks( const StringArray &mksbList,
        const String &rcfile, const String &machine );
    boolean     inSandbox( const Sandbox &sbox ) const;
    String      getBackingDirForUpgrade() const;
    void        createInstDirsForUpgrade() const;
    boolean     addMachineToExistingSandbox();
    boolean     makeExistingSandboxDefault();
    boolean     checkDependencyErrors( const Sandbox &sbox,
                                       const SandboxRCConfigFile *sbrc ) const;
};

#endif //_ODE_BIN_MKSB_MKSB_HPP_
