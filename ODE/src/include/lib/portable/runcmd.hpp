/**
 * RunSystemCommand.
 *
**/

#ifndef _ODE_LIB_PORTABLE_RUNCMD_HPP_
#define _ODE_LIB_PORTABLE_RUNCMD_HPP_

#define DEFAULT_CWD "."

#include <base/odebase.hpp>
#include "lib/string/strarray.hpp"
#include "lib/string/string.hpp"
#include "lib/portable/platcon.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/strcon.hpp"
#include "lib/portable/native/proc.h"

/**
 * Use start() to start the command off after construction,
 *
 * Use waitFor() to wait for the process to terminate.
**/
class RunSystemCommand
{
  public:

    RunSystemCommand( const StringArray &cmd,
        boolean perform_input = true, boolean show_output = true,
        boolean save_text = false, boolean prepend_shell = true,
        boolean stay_in_shell = false );

    //
    // WARNING: on NT and OS/2, the env pointers (although not the
    // contents of the strings) may be altered!
    //
    RunSystemCommand( const StringArray &cmd, char **env,
        int env_size = -1, boolean perform_input = true,
        boolean show_output = true, boolean save_text = false,
        boolean prepend_shell = true, boolean deallocate_env = false,
        boolean stay_in_shell = false );

    inline ~RunSystemCommand();

    // just so we can stick this class in a vector/hashtable
    inline boolean operator==( const RunSystemCommand &cmp ) const;

    boolean start();
    ODERET_CODE_TYPE run();
    void stopChild();
    ODERET_CODE_TYPE waitFor();
    inline ODERET_CODE_TYPE waitForChild();
    inline static ODEPROC_ID_TYPE waitForAny( ODERET_CODE_TYPE &return_code );
    inline ODEPROC_ID_TYPE getChildPID() const;
    inline const String &getInputText() const; // returns empty string
    inline const String &getOutputText() const;
    inline const String &getErrorText() const; // currently not supported
    inline ODERET_CODE_TYPE getExitCode() const;
    String getFullCmdString() const; // includes shell prepend stuff
    static StringArray *getShellPrependString( StringArray *buf = 0,
                                               String *kflag_cmd = 0);
    static StringArray *buildShellCmdArray( const String &cmd,
        const String &cwd = DEFAULT_CWD, StringArray *buf = 0,
        boolean stay_in_shell = false, String *kflag_cmd = 0);
    inline static StringArray *buildShellCmdArray(
        const StringArray &cmd, const String &cwd = DEFAULT_CWD,
        StringArray *buf = 0, boolean stay_in_shell = false,
        String *kflag_cmd = 0 );
    inline void setChildRC( ODERET_CODE_TYPE ) ;
    static String cmdArrayToString( const StringArray &cmdarray );


  private:

    static const String ODEDLLPORT CMDARG_SEP_STRING;
    static const String ODEDLLPORT STDIN_REDIRECTION_SYMBOL;
    static const String ODEDLLPORT STDOUT_REDIRECTION_SYMBOL;
    static const String ODEDLLPORT STDERR_REDIRECTION_SYMBOL;
    static const String ODEDLLPORT STDOUT_FILENAME;
    static const String ODEDLLPORT STDERR_FILENAME;
    StringArray cmd;
    StringArray *envtmp; // only used temporarily
    char **env;
#ifdef OS2
    String begin_libpath;
    String end_libpath;
    char *begin_libpath_str, *end_libpath_str;
    char **begin_libpath_loc, **end_libpath_loc;
    char **begin_libpath_rep_loc, **end_libpath_rep_loc;
#endif
    boolean perform_input;
    boolean show_output;
    boolean save_text;
    boolean prepend_shell;
    boolean deallocate_env;
    boolean stay_in_shell;
    String shell_with_k_flag; // non-empty string only if we should use it
    String output_text;
    String error_text;
    String out_tempfile;
    String err_tempfile;

    ODERET_CODE_TYPE child_rc;
    ODEPROC_ID_TYPE child_pid;

    void appendCmdRedirection();
    void checkKFlag();
    inline boolean cmdHasInputRedirection() const;
    inline boolean cmdHasOutputRedirection() const;
    boolean charExistsInStringArray( const StringArray &arr, char ch ) const;
    ODEPROC_ID_TYPE runChild( char **cmdArgs );
    void gatherSavedText();
    void gatherText( const String &filename, String &buf ) const;
#ifdef OS2
    String removeLibPath( const String &name );
    void resetEnv();
#endif
};


inline RunSystemCommand::~RunSystemCommand()
{
  if (deallocate_env)
    delete[] env;
  delete envtmp;
}

inline boolean RunSystemCommand::operator==(
    const RunSystemCommand &cmp ) const
{
  return false;
}


inline void RunSystemCommand::setChildRC( ODERET_CODE_TYPE val ) 
{
  child_rc = val;
}


inline ODEPROC_ID_TYPE RunSystemCommand::getChildPID() const
{
  return (child_pid);
}


/**
 * Return an empty string.  This is for compatibility
 * with the method in the Java class by this name.
**/
inline const String &RunSystemCommand::getInputText() const
{
  return (StringConstants::EMPTY_STRING);
}


/**
 * Return the text the child wrote to the stdout
 * stream as a single string (all linefeeds are
 * preserved unmodified).
**/
inline const String &RunSystemCommand::getOutputText() const
{
  return (output_text);
}


/**
 * Return the text the child wrote to the stdout
 * stream as a single string (all linefeeds are
 * preserved unmodified).
 *
 * NOTE: This will always return the empty string,
 * since capturing stderr is not currently supported.
**/
inline const String &RunSystemCommand::getErrorText() const
{
  return (error_text);
}


/**
 * Return the exit code of the child process.
**/
inline ODERET_CODE_TYPE RunSystemCommand::getExitCode() const
{
  return (child_rc);
}

inline StringArray *RunSystemCommand::buildShellCmdArray(
    const StringArray &cmd, const String &cwd,
    StringArray *buf, boolean stay_in_shell, String *kflag_cmd )
{
  return (buildShellCmdArray( cmdArrayToString( cmd ), cwd, buf,
                              stay_in_shell, kflag_cmd ));
}

inline boolean RunSystemCommand::cmdHasInputRedirection() const
{
  return (charExistsInStringArray( cmd, '<' ));
}

inline boolean RunSystemCommand::cmdHasOutputRedirection() const
{
  return (charExistsInStringArray( cmd, '>' ));
}

/**
 * Waits for any child process to terminate.
 * Will return the process ID of the process that just terminated.
 * return_code will be set to the error code.
**/
inline ODEPROC_ID_TYPE RunSystemCommand::waitForAny(
    ODERET_CODE_TYPE &return_code )
{
  return ODEwaitForAny( &return_code );
}


/**
 * Same as waitFor().
**/
inline ODERET_CODE_TYPE RunSystemCommand::waitForChild()
{
  return waitFor();
}

#endif /* _ODE_LIB_PORTABLE_RUNCMD_HPP_ */
