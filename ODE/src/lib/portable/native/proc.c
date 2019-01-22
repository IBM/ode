/**
 * procfork()
 * procwait()
 * procpid()
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

#ifdef UNIX
#include <sys/wait.h>
#include <unistd.h>
#include <signal.h>
#ifdef VMS
#include <lib$routines.h>
#include <ssdef.h>
#include <descrip.h>
#include <libclidef.h>
#endif
#ifdef OS400
#include <spawn.h>
#endif /* OS400 */
#else /* non-UNIX follows... */
#include <process.h>
#ifdef OS2
#define INCL_DOSMISC
#define INCL_DOSPROCESS
#include <os2.h>
#endif /* OS2 */
#endif /* non-UNIX */

#define _ODE_LIB_PORTABLE_NATIVE_PROC_C_
#include "lib/portable/native/proc.h"
#include "lib/portable/native/platver.h"


#if !defined(NO_PIPES_FOR_OUTPUT)

#define ODE_PIPEBUF_INCREMENT 1024
char *pipe_buffer = 0; /* buffer that holds result of ODEforkWithPipeOutput() */
unsigned int pipe_buf_size = 0;
unsigned int pipe_buf_maxsize = 0;

void ODEaddStringToPipeBuffer( char *buf, unsigned int size )
{
  unsigned int count = 0;

  /* Extend the buffer, if I need more space */
  if ((pipe_buf_size + size) >= pipe_buf_maxsize)
  {
    pipe_buf_maxsize += (size >= ODE_PIPEBUF_INCREMENT) ?
        size + 1 : ODE_PIPEBUF_INCREMENT;
    pipe_buffer = (char *)realloc( (void*)pipe_buffer, pipe_buf_maxsize );
  }

  /* Unfortunately, I cannot rely on buf to be null terminated */
  while (count < size)
    *(pipe_buffer + pipe_buf_size + count++) = *buf++;

  *(pipe_buffer + pipe_buf_size + count) = '\0';
  pipe_buf_size += size;
}

void ODEfreePipeBuffer( void )
{
  if (pipe_buffer)
  {
    free( (void*)pipe_buffer );
    pipe_buffer = 0;
    pipe_buf_size = 0;
    pipe_buf_maxsize = 0;
  }
}

char *ODEgetPipeBuffer( void )
{
  return (pipe_buffer);
}

#endif /* !defined(NO_PIPES_FOR_OUTPUT) */


/**
 *
 * WIN32 section
 *
 * All "Windows-only" code follows in this section.
 *
**/

#ifdef WIN32

#define ODE_HANDLE_ARRAY_SIZE_INCREMENT 8

unsigned int runningProcs = 0;
HANDLE*  handleArray      = 0;
unsigned int maxHandleArraySize = 0;

void ODEextendHandleArray( void )
{
  maxHandleArraySize += ODE_HANDLE_ARRAY_SIZE_INCREMENT;
  handleArray = (HANDLE *)realloc( (void*)handleArray,
      maxHandleArraySize * sizeof( HANDLE ) );
}

void ODEfreeHandleArray( void )
{
  if (handleArray && runningProcs == 0)
  {
    free( (void*)handleArray );
    handleArray = 0;
    maxHandleArraySize = 0;
  }
}

void ODEadjustHandleArray( ODEPROC_ID_TYPE process_ID )
{
  unsigned int i;

  /* If it is an invalid process, there is nothing to adjust */
  if (process_ID == INVALID_PROCESS)
    return;

  /* Decrement the number of processes from the list of processes
   * and reorganize indexing into the handleArray structure.
   */
  runningProcs--;

  for (i = 0; i < runningProcs; i++)
  {
    if (process_ID == *(handleArray + i))
    {
      *(handleArray + i) = *(handleArray + runningProcs);
      break;
    }
  }
}

void ODEregisterNewProcess( ODEPROC_ID_TYPE process_ID )
{
  /* If there is nothing to register, just return */
  if (process_ID == INVALID_PROCESS)
    return;

  /* If the process size has reached max, extend it */
  if (runningProcs == maxHandleArraySize)
    ODEextendHandleArray();

  /* Add this new process to the array and increase the number of processes */
  *(handleArray + runningProcs) = process_ID;
  runningProcs++;
}

/**
 * Calculates the total size in bytes (including null
 * terminators) of all the strings in args.
**/
unsigned int ODEgetCharStarArraySize( char** args )
{
  unsigned int size = 0;

  while (*args)
  {
    size += strlen( *args ) + 1;
    args++;
  }

  return (size + 1);
}


LPTSTR ODEcmdToLPTSTR( char** args, unsigned int size )
{
  LPTSTR cmd  = (LPTSTR)malloc( size );
  LPTSTR help = cmd;
  unsigned int j;

  while (args && *args)
  {
    j = 0;
    while ((*args)[j] != '\0')
      *help++ = (*args)[j++];

    *help++ = ' ';
    args++;
  }
  *help = '\0';

 return cmd;
}

LPVOID ODEenvToLPVOID( char **args, unsigned int size )
{
  char *drives = 0, *ptr = 0;
  static char *drives_local_copy = 0;
  static unsigned int drive_len = 0;
  char *env, *help;
  unsigned int j;
  ODE_VERSION_DATA vd;

  ODEuname( &vd );

  /**
   *  We must prepend the environment with special environment variables
   *  for Windows NT and Windows 2000
   *  This is NOT true for Win 95/98
  **/
  if (vd.platform == WINDOWSNT_PLATFORM)
  {
    if (drives_local_copy == 0)
    {
      drives = GetEnvironmentStrings();
      ptr = drives;
      while (*ptr)
      {
        if (*ptr != '=')
          break;
        while (*(ptr++))
          ++drive_len;
        ++drive_len; /* this is for the null char */
      }
      /**
       *  Lets keep a local copy of the special environment variables.
       *  It has been discovered that it is possible (on Win 95/98 at least)
       *  that the variable "drives" may have changed since the previous call
       *  to this routine.  In this case, the variable "drive_len" is no longer
       *  valid.
      **/
      drives_local_copy = (char*)malloc( drive_len );
      memcpy( drives_local_copy, drives, drive_len );
    }
    env = (char*)malloc( size + drive_len );
    memcpy( env, drives_local_copy, drive_len );
  }
  else
  {
    env = (char*)malloc( size );
  }

  help = env + drive_len;

  while (args && *args)
  {
    j = 0;
    while( (*args)[j] != '\0' )
      *help++ = (*args)[j++];

    *help++ = '\0';
    args++;
  }
  *help = '\0';

  return (LPVOID)env;
}

ODEPROC_ID_TYPE ODEwaitForAny( ODERET_CODE_TYPE *ex_code )
{
  DWORD ret;
  HANDLE process_ID;

  /* Nothing to wait for */
  if (runningProcs == 0)
    return (INVALID_PROCESS);

  /*Wait until a process terminates, don't wait for all to terminate */
  ret = WaitForMultipleObjects( runningProcs, handleArray,
      FALSE, INFINITE );

  if (ret == WAIT_FAILED)
    return (INVALID_PROCESS);

  /* get the pid by indexing into handleArray. */
  process_ID = *(handleArray + ret);
  GetExitCodeProcess( process_ID, ex_code );
  CloseHandle( process_ID );

  /* Decrease the number of runing processes */
  ODEadjustHandleArray(process_ID);

  return (process_ID);
}


ODEPROC_ID_TYPE ODEfork( char **args, char **envs )
{
  PROCESS_INFORMATION processInfo;
  STARTUPINFO         startupInfo;
  ODEPROC_ID_TYPE        process_ID;
  BOOL rc_code;

  /*
   * args is a char**, need to make it a space
   * separated string, and null terminate it.
   *
   * envs is a char**, need to make it a NULL
   * separated and NULL terminated string.
  */

  LPTSTR cmdLine =  ODEcmdToLPTSTR( args, ODEgetCharStarArraySize( args ) );
  LPVOID envir =  ODEenvToLPVOID( envs, ODEgetCharStarArraySize( envs ) );

  memset( &startupInfo, 0, sizeof( STARTUPINFO ) );

  /*
   * Create a new process that executes 'cmdLine'
   * cmdLine: Command Line String.
   * envir: Pointer to environment block
   * Run the new command line on the current directory.
   * (third to last argument set to NULL )
   * Process information is filled in.
   *
   */
  rc_code = CreateProcess( NULL, cmdLine, NULL, NULL, TRUE,
      NORMAL_PRIORITY_CLASS, envir, NULL, &startupInfo, &processInfo );

  /*
   * Make sure I print out a warning and return
   * INVALID_PROCESS, in case the creation of the process fails.
   *
   */
  if (!rc_code || processInfo.hProcess == INVALID_HANDLE_VALUE)
  {
    fprintf( stderr, ">> ERROR: CreateProcess(%s ...) failure (%d)\n",
        *args, GetLastError() );
    process_ID = INVALID_PROCESS;
  }
  else
    process_ID = processInfo.hProcess;

  CloseHandle( processInfo.hThread );

  /* Clean up allocated memory before returning */
  if (cmdLine)
    free( cmdLine );

  if (envir)
    free( envir );

  /* Register the new process (if it started) in the array of processes */
  if (process_ID != INVALID_PROCESS)
    ODEregisterNewProcess( process_ID );

  return (process_ID);
}


ODERET_CODE_TYPE ODEwait( ODEPROC_ID_TYPE pid )
{
  ODERET_CODE_TYPE ret = ODEDEF_ERROR_CODE;

  /* make sure there's something to wait for */
  if (runningProcs != 0 && pid != INVALID_PROCESS)
  {
    /* wait indefinitely for the process to terminate */
    if (WaitForSingleObject( pid, INFINITE ) != WAIT_FAILED)
    {
      GetExitCodeProcess( pid, &ret );
      ODEadjustHandleArray( pid ); /* decrease number of running processes */
    }
  }

  return (ret);
}

/**
 *
 * NOTE: Using the MS-Windows API, it is documented as being unsafe
 * to use TerminateProcess, so there are three choices...
 * 1. use it anyway
 * 2. don't do anything
 * 3. kill all children by generating SIGBREAK
 * Currently, #3 is implemented, since if we're doing something as
 * drastic as killing children, then we're probably shutting down
 * the parent process as well.  This method is recommended as the
 * alternative to TerminateProcess.  However, there is one caveat:
 *
 * WARNING: The calling process must ignore or
 * catch SIGBREAK before calling this function.
 * Otherwise, it too will die.
 *
**/
void ODEkill( ODEPROC_ID_TYPE pid )
{
  GenerateConsoleCtrlEvent( CTRL_BREAK_EVENT, 0 );
}


/* END WIN32 SECTION */


/**
 *
 * OS2 section
 *
 * All "OS2-only" code follows in this section.
 *
**/

#elif defined(OS2)

/**
 * Puts the desired extension library path into buf,
 * which must already be big enough to hold the results
 * (use ODE_LIBPATH_LEN).  The string copied into buf is
 * of the form "xLIBPATH=path", where "x" is either BEGIN
 * or END, and "path" is the actual path.
**/
void ODEgetExtLibPath( char *buf, int libpath_type )
{
  if (buf == 0)
    return;
  if (libpath_type == ODE_BEGIN_LIBPATH)
  {
    strcpy( buf, "BEGINLIBPATH=" );
    buf += 13;
    DosQueryExtLIBPATH( buf, BEGIN_LIBPATH );
  }
  else
  {
    strcpy( buf, "ENDLIBPATH=" );
    buf += 11;
    DosQueryExtLIBPATH( buf, END_LIBPATH );
  }
  if (*buf == '\0')
    strcpy( buf, ";" );
}

void ODEkill( ODEPROC_ID_TYPE pid )
{
  if (pid >= 0)
    DosKillProcess( DKP_PROCESSTREE, pid );
}

ODEPROC_ID_TYPE ODEfork( char **args, char **envs,
    char *beginlibpath, char *endlibpath )
{
  ODEPROC_ID_TYPE child = INVALID_PROCESS;

  if (beginlibpath != 0)
    DosSetExtLIBPATH( beginlibpath, BEGIN_LIBPATH );
  if (endlibpath != 0)
    DosSetExtLIBPATH( endlibpath, END_LIBPATH );

  child = _spawnvpe( P_NOWAIT, *args, args, envs );

  if (child < (ODEPROC_ID_TYPE)0) /* error */
  {
    child = INVALID_PROCESS;
    fprintf( stderr, ">> ERROR: spawn(%s ...) failure (%d)\n",
        *args, errno );
  }

  return (child);
}

ODEPROC_ID_TYPE ODEwaitForAny( ODERET_CODE_TYPE *result )
{
  return wait( result );
}

int ODEwait( ODEPROC_ID_TYPE pid )
{
  int result = ODEDEF_ERROR_CODE;

  if (pid < 0)
    return (result);

  _cwait( &result, pid, WAIT_CHILD );

  return (result);
}


/* END OS2 SECTION */


/**
 *
 * OS400 section
 *
 * All "OS400-only" code follows in this section.
 *
**/

#elif defined(OS400)

ODEPROC_ID_TYPE ODEfork( char **args, char **envs )
{
  ODEPROC_ID_TYPE child = INVALID_PROCESS;
  struct inheritance inherit;

  inherit.flags = SPAWN_SETTHREAD_NP;
  inherit.pgroup = 0;

  child = spawn( *args, 0, 0, &inherit, args, envs );
  if (child == -1) /* error */
  {
    child = INVALID_PROCESS;
    fprintf( stderr, ">> ERROR: spawn() failure (%d)\n", errno );
  }

  return (child);
}

#if !defined(NO_PIPES_FOR_OUTPUT)
ODEPROC_ID_TYPE ODEforkWithPipeOutput( char **args, char **envs,
    int keep_output )
{
  ODEPROC_ID_TYPE child = INVALID_PROCESS;
  int cc, fds[2], stdio_fds[3], wait_stat_loc;
  static char result[BUFSIZ]; /* arbitrary reasonable size */
  struct inheritance inherit;

  inherit.flags = 0;
  inherit.pgroup = 0;

  /* Open pipe for fetching the output */
  pipe( fds );

  stdio_fds[0] = fds[1];
  stdio_fds[1] = fds[1];
  stdio_fds[2] = fds[1];

  child = spawn( *args, 3, stdio_fds, &inherit, args, envs );
  close( fds[1] );

  if (child < (ODEPROC_ID_TYPE)0) /* fork failed */
  {
    fprintf( stderr, "WARNING: spawn() failure (%d)\n", errno );
    child = INVALID_PROCESS;
  }
  else
  {
    for (;;)
    {
      if ((cc = read( fds[0], result, sizeof( result ) )) < 0)
      {
        fprintf( stderr,"WARNING: Could not read child process output "
            "from pipe (%d)\n", errno );
        break;
      }

      if (cc == 0)
          break;

      if (keep_output)
        ODEaddStringToPipeBuffer( result, cc );
    }
  }

  close( fds[0] );

  return (child);
}
#endif /* !defined(NO_PIPES_FOR_OUTPUT) */


/* END OS400 SECTION */


#endif /* end platform-specific section */


/**
 *
 * UNIX section
 *
 * All "UNIX-only" code follows in this section.  May need to
 * "ifndef" specific UNIX platforms out if they appear above
 * and have their own versions of these functions!
 *
**/

#ifdef UNIX

void ODEkill( ODEPROC_ID_TYPE pid )
{
  if (pid >= 0)
    kill( pid, SIGTERM );
}

#if !defined(OS400)
ODEPROC_ID_TYPE ODEfork( char **args, char **envs )
{
  ODEPROC_ID_TYPE child = INVALID_PROCESS;

#if defined(VMS)
  ODEsetSymbols( envs );
  child = vfork();
#else
  child = fork();
#endif

  if (child == 0) /* child */
  {
#if defined(VMS)
    execvp( *args, args );
#else
    execve( *args, args, envs );
#endif
    fprintf( stderr, ">> ERROR: exec(%s ...) failure (%d)\n",
        *args, errno );
#if defined(VMS)
    exit( SS$_ABORT ); /* in case execve fails */
#else
    exit( -1 ); /* in case execve fails */
#endif
  }
  else if (child < (ODEPROC_ID_TYPE)0) /* error */
  {
    child = INVALID_PROCESS;
    fprintf( stderr, ">> ERROR: fork() failure (%d)\n", errno );
  }

  return (child);
}
#endif

#if !defined(NO_PIPES_FOR_OUTPUT)
#if !defined(OS400)
ODEPROC_ID_TYPE ODEforkWithPipeOutput( char **args, char **envs,
    int keep_output )
{
  ODEPROC_ID_TYPE child = INVALID_PROCESS;
  int cc, fds[2];
  static char result[BUFSIZ]; /* arbitrary reasonable size */

  /* Open pipe for fetching the output */
  pipe( fds );

  child = fork();
  if (child == (ODEPROC_ID_TYPE)0) /* child */
  {
    /* Close input side of pipe */
    close( fds[0] );

    /* Duplicate the output stream to the shell's output.
     * Shut down unneeded stuff.
     */
    dup2( fds[1], STDOUT_FILENO );
    close( fds[1] );

    execve( *args, args, envs );
    exit( -1 ); /* in case execve fails */
  }
  else if (child < (ODEPROC_ID_TYPE)0) /* fork failed */
  {
    fprintf( stderr, "WARNING: fork() failure (%d)\n", errno );
    close( fds[0] );
    close( fds[1] );
    return (INVALID_PROCESS);
  }

  /* Close writing part */
  close( fds[1] );
  for (;;)
  {
    if ((cc = read( fds[0], result, sizeof( result ) )) < 0)
    {
      fprintf( stderr,"WARNING: Could not read child process output "
          "from pipe (%d)\n", errno );
      break;
    }

    if (cc == 0)
        break;

    if (keep_output)
      ODEaddStringToPipeBuffer( result, cc );
  }

  close( fds[0] );

  return (child);
}
#endif /* non-OS400 */
#endif /* !defined(NO_PIPES_FOR_OUTPUT) */

ODEPROC_ID_TYPE ODEwaitForAny( ODERET_CODE_TYPE *result )
{
  ODEPROC_ID_TYPE pid;

  pid = waitpid( -1, result, 0 );
  *result = WEXITSTATUS( *result );

#ifdef VMS
  if (*result & 1)
    *result = 0;
#endif

  return (pid);
}

int ODEwait( ODEPROC_ID_TYPE pid )
{
  int result = ODEDEF_ERROR_CODE;

  if (pid < 0)
    return (result);

  waitpid( pid, &result, 0 ) ;
  result = WEXITSTATUS( result );

#ifdef VMS
  if (result & 1)
    return (0);
#endif

  return (result);
}


/* END UNIX SECTION */


#endif /* UNIX */


/**
 *
 * VMS section
 *
**/
#ifdef VMS
void ODEsetSymbols( char **envs )
{
  struct dsc$descriptor symdsc, valdsc;
  char **envptr, *chptr;

  symdsc.dsc$b_dtype = DSC$K_DTYPE_T;
  symdsc.dsc$b_class = DSC$K_CLASS_S;
  valdsc.dsc$b_dtype = DSC$K_DTYPE_T;
  valdsc.dsc$b_class = DSC$K_CLASS_S;

  for (envptr = envs; envptr && *envptr; ++envptr)
  {
    chptr = strchr( *envptr, '=' );
    if (chptr)
    {
      *chptr = '\0';
      symdsc.dsc$w_length  = strlen( *envptr );
      symdsc.dsc$a_pointer = *envptr;
      valdsc.dsc$w_length  = strlen( chptr + 1 );
      valdsc.dsc$a_pointer = chptr + 1;
      lib$set_symbol( &symdsc, &valdsc, &LIB$K_CLI_GLOBAL_SYM );
      *chptr = '=';
    }
  }
}
#endif /* VMS */


/**
 *
 * Shared section.  The code that follows is for ALL platforms.
 *
**/

int ODEgetpid( void )
{
  return (getpid());
}
