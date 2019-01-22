/**
 * RunSystemCommand
 *
**/
#include <fstream.h>

#define _ODE_LIB_PORTABLE_RUNCMD_CPP_
#include "lib/portable/runcmd.hpp"
#include "lib/io/path.hpp"
#include "lib/util/signal.hpp"


/**
 *
**/
const String RunSystemCommand::CMDARG_SEP_STRING = " \t";
const String RunSystemCommand::STDIN_REDIRECTION_SYMBOL = " <";
const String RunSystemCommand::STDOUT_REDIRECTION_SYMBOL = " >";
const String RunSystemCommand::STDERR_REDIRECTION_SYMBOL = " 2>";
const String RunSystemCommand::STDOUT_FILENAME = "&1";
const String RunSystemCommand::STDERR_FILENAME = "&2";


RunSystemCommand::RunSystemCommand( const StringArray &cmd,
    boolean perform_input, boolean show_output, boolean save_text,
    boolean prepend_shell, boolean stay_in_shell ) :
    cmd( cmd ), env( 0 ), perform_input( perform_input ),
    show_output( show_output ), save_text( save_text ),
    prepend_shell( prepend_shell ), deallocate_env( true ),
    stay_in_shell( stay_in_shell ),
    child_pid( INVALID_PROCESS ), child_rc( ODEDEF_ERROR_CODE )
{
  appendCmdRedirection();

  envtmp = Env::getenv();
#ifdef WIN32
  env = envtmp->toSortedCharStarArray();
#else
  env = envtmp->toCharStarArray();
#endif
  checkKFlag();
}


RunSystemCommand::RunSystemCommand( const StringArray &cmd, char **env,
    int env_size, boolean perform_input, boolean show_output,
    boolean save_text, boolean prepend_shell, boolean deallocate_env,
    boolean stay_in_shell ) :
    cmd( cmd ), envtmp( 0 ), env( env ), perform_input( perform_input ),
    show_output( show_output ), save_text( save_text ),
    prepend_shell( prepend_shell ), deallocate_env( deallocate_env ),
    stay_in_shell( stay_in_shell ),
    child_pid( INVALID_PROCESS ), child_rc( ODEDEF_ERROR_CODE )
{
  appendCmdRedirection();

#ifdef WIN32
  ::sortCharStarArray( env, env_size );
#endif
  checkKFlag();
}

/** 
 * If we use the /K flag for the shell, we leave the shell running after
 * executing the cmd. Set use_k_flag true if we will use /K.
**/
void RunSystemCommand::checkKFlag()
{
  if (stay_in_shell)
  {
    const String *shellptr = Env::getenv( StringConstants::SHELL_VAR );
    const String *kflag = Env::getenv( StringConstants::SHELL_K_FLAG_VAR );
    if (kflag && *kflag != StringConstants::EMPTY_STRING)
      shell_with_k_flag = *shellptr + " " + *kflag + " ";
  }
}

/**
 *
**/
ODERET_CODE_TYPE RunSystemCommand::run() 
{
  if (!start())
    return (ODEDEF_ERROR_CODE);
  return (waitForChild());
}


boolean RunSystemCommand::start() 
{
  boolean rc = true;

  if (cmd.length() > 0 && child_pid == INVALID_PROCESS)
  {
    StringArray cmdargs;
    output_text = StringConstants::EMPTY_STRING;
    error_text  = StringConstants::EMPTY_STRING;
    char **cmdptr;

    if (prepend_shell)
    {
      buildShellCmdArray( cmd, StringConstants::PERIOD, &cmdargs,
                          stay_in_shell, &shell_with_k_flag );
      cmdptr = cmdargs.toCharStarArray();
    }
    else
      cmdptr = cmd.toCharStarArray();
    
    if ((child_pid = runChild( cmdptr )) == INVALID_PROCESS)
    {
      child_rc = ODEDEF_ERROR_CODE;
      rc = false;
    }

    delete[] cmdptr;
  }

  return (rc);
}

String RunSystemCommand::getFullCmdString() const
{
  if (prepend_shell)
  {
    StringArray cmdargs;
    buildShellCmdArray( cmd, StringConstants::PERIOD, &cmdargs );
    return (cmdargs.join( StringConstants::SPACE ));
  }
  else
    return (cmd.join( StringConstants::SPACE ));
}

#ifdef OS2
String RunSystemCommand::removeLibPath( const String &name )
{
  String rc( ';' );
  char **switch_ptr, **arr = env;

  while (*arr)
  {
    if (strncmp( *arr, name.toCharPtr(), name.length() ) == 0)
    {
      rc = String( (*arr) + name.length() );
      // now switch this pointer with the last one
      switch_ptr = arr;
      while (*arr)
        ++arr;
      --arr;
      if (name[STRING_FIRST_INDEX] == 'B') // BEGINLIBPATH
      {
        begin_libpath_str = *switch_ptr;
        begin_libpath_loc = switch_ptr;
        begin_libpath_rep_loc = arr;
      }
      else
      {
        end_libpath_str = *switch_ptr;
        end_libpath_loc = switch_ptr;
        end_libpath_rep_loc = arr;
      }
      *switch_ptr = *arr;
      *arr = 0;
    }
    else
      ++arr;
  }
  return (rc);
}

void RunSystemCommand::resetEnv()
{
  if (begin_libpath_loc != 0)
  {
    *begin_libpath_rep_loc = *begin_libpath_loc;
    *begin_libpath_loc = begin_libpath_str;
  }
  if (end_libpath_loc != 0)
  {
    *end_libpath_rep_loc = *end_libpath_loc;
    *end_libpath_loc = end_libpath_str;
  }
}
#endif

void RunSystemCommand::appendCmdRedirection()
{
  if (!perform_input && !cmdHasInputRedirection())
    cmd[cmd.lastIndex()] += STDIN_REDIRECTION_SYMBOL + Path::nullFilename();

  if (!cmdHasOutputRedirection())
  {
    if (save_text)
    {
#ifdef NO_PIPES_FOR_OUTPUT
      out_tempfile = Path::tempFilename();
#ifdef DEFAULT_SHELL_IS_VMS
      Path::unix2vmsThis( out_tempfile );
#endif
      cmd[cmd.lastIndex()] += STDOUT_REDIRECTION_SYMBOL + out_tempfile;
#endif
    }
    else if (!show_output)
    {
      cmd[cmd.lastIndex()] += STDOUT_REDIRECTION_SYMBOL + Path::nullFilename();
    }
  }
}

boolean RunSystemCommand::charExistsInStringArray(
    const StringArray &arr, char ch ) const
{
  boolean quoted;

  for (int i = arr.firstIndex(); i <= arr.lastIndex(); ++i)
  {
    const String &str = arr[i];
    quoted = false;
    for (int j = str.firstIndex(); j <= str.lastIndex(); ++j)
    {
      if (str[j] == '\"') 
        quoted = !quoted;
      else if (!quoted && str[j] == ch)
        return (true);
    }
  }
  return (false);
}

void RunSystemCommand::stopChild()
{
  if (child_pid != INVALID_PROCESS)
  {
    ODEkill( child_pid );
    child_pid = INVALID_PROCESS;
  }
  child_rc = ODEDEF_ERROR_CODE;
}


ODERET_CODE_TYPE RunSystemCommand::waitFor()
{
  if( child_pid != INVALID_PROCESS )
  {
    child_rc = ODEwait( child_pid );

    if (save_text)
      gatherSavedText();

    if( !Signal::isInterrupted() )
      child_pid = INVALID_PROCESS; // so user can restart process
  }

  return (child_rc);
}


void RunSystemCommand::gatherSavedText()
{
#ifndef NO_PIPES_FOR_OUTPUT

  if (ODEgetPipeBuffer() == 0)
    output_text = String();
  else
    output_text = String( ODEgetPipeBuffer() );
  error_text = StringConstants::EMPTY_STRING; //not implemented.

  ODEfreePipeBuffer();

#else

  if (out_tempfile != StringConstants::EMPTY_STRING)
  {
    gatherText( out_tempfile, output_text );
    Path::deletePath( out_tempfile );
  }
  if (err_tempfile != StringConstants::EMPTY_STRING)
  {
    gatherText( err_tempfile, error_text );
    Path::deletePath( err_tempfile );
  }

#endif
}


/**
 *
**/
ODEPROC_ID_TYPE RunSystemCommand::runChild( char **args )
{
#if defined(OS2)
  begin_libpath_loc = end_libpath_loc = 0;
  begin_libpath = removeLibPath( "BEGINLIBPATH=" );
  end_libpath = removeLibPath( "ENDLIBPATH=" );
  ODEPROC_ID_TYPE rc = ODEfork( args, env, begin_libpath.toCharPtr(),
      end_libpath.toCharPtr() );
  resetEnv();
  return (rc);
#else /* non-OS2 follows... */
#ifndef NO_PIPES_FOR_OUTPUT
  if (save_text)
    return (ODEforkWithPipeOutput( args, env, true ));
#endif /* NO_PIPES_FOR_OUTPUT */
  return (ODEfork( args, env ));
#endif
}


/**
 * Build the full command array.  Prepends the shell and a cd
 * command to the cmd string.
 *
 * @param cmd The command to run.
 * @param cwd The directory in which to run the command.  A dot
 * (.) or the empty string will prevent a "cd" command from
 * being prepended.
 * @return The command string array.  Returns a null pointer
 * if the specified working directory doesn't exist.
**/
StringArray *RunSystemCommand::buildShellCmdArray( const String &cmd,
    const String &cwd, StringArray *buf, boolean stay_in_shell, 
    String *kflag_cmd )
{
  String cmd_joiner = " " + SandboxConstants::getUSER_CMD_SEPARATOR() + " ";
  StringArray *cmdarrayptr = getShellPrependString( buf, kflag_cmd ),
      &cmdarray = *cmdarrayptr;

  if (cwd.length() > 0 && !Path::exists( cwd ))
  {
    return (0);
  }
  else
  {
    if (cwd == StringConstants::EMPTY_STRING || cwd.equals( DEFAULT_CWD ))
      cmdarray[cmdarray.lastIndex()] = cmd;
    else
      cmdarray[cmdarray.lastIndex()] = "cd " +
          Path::normalize( cwd ) + cmd_joiner + cmd;
  }

  // To mimic the effects of /K in the shells which don't support it, we
  // must force ";" as the command separator to ensure the shell remains
  // even if the command fails.
  if (stay_in_shell &&
      !(kflag_cmd && *kflag_cmd != StringConstants::EMPTY_STRING))
  {
    cmdarray[cmdarray.lastIndex()] += " ; " +
      *Env::getenv( StringConstants::SHELL_VAR );
  }

  return (&cmdarray);
}

/**
 *
**/
String RunSystemCommand::cmdArrayToString( const StringArray &cmdarray )
{
  String result;
  if (cmdarray.length() > 0)
  {
    result = cmdarray[cmdarray.firstIndex()];
    for (int i = cmdarray.firstIndex() + 1; i <= cmdarray.lastIndex(); ++i)
    {
      result += StringConstants::SPACE;
      result += cmdarray[i];
    }
  }
  return (result);
}

/**
 * Get the command shell that should be prepended to
 * commands that use I/O redirection.  The array that
 * is returned has the final element filled with a
 * pair of quotes...this element should be replaced
 * by the command string you wish to execute.
 *
 * @return The command string array.
 *
**/
StringArray *RunSystemCommand::getShellPrependString( StringArray *buf,
                                                      String *kflag_cmd )
{
  const String *cmdptr;
  String cmd;

  if (kflag_cmd && *kflag_cmd != StringConstants::EMPTY_STRING)
    cmd = *kflag_cmd;
  else if ((cmdptr = Env::getenv( StringConstants::ODEMAKE_SHELL_VAR )) != 0)
    cmd = *cmdptr;
  else
#ifdef DEFAULT_SHELL_IS_VMS
    cmd = "oderun pipe";
#else
    cmd = "unknown_shell";
#endif

  cmd += " \"\"";
  return (cmd.split( CMDARG_SEP_STRING, 0, buf ));
}


/**
 * If specified the output text of the child process is stored in a String.
 * This text has been stored in a temp file.
**/
void RunSystemCommand::gatherText( const String &filename, String &buf ) const
{
#ifndef NO_PIPES_FOR_OUTPUT
  buf= output_text;
#else
  const int BLOCK_SIZE = 1024;
  int count;
  char temp[BLOCK_SIZE + 1];
  ifstream ip( filename.toCharPtr() );

  while (!ip.eof() && !ip.fail())
  {
    ip.read( temp, BLOCK_SIZE );
    count = ip.gcount();
    if (count > 0)
    {
      temp[count] = '\0';
      buf += temp;
    }
  }
#endif
}
