/**
 * PlatformConstants
 *
**/

#define _ODE_LIB_PORTABLE_PLATCON_CPP_
#include "lib/portable/native/platver.h"
#include "lib/portable/platcon.hpp"
#include "lib/portable/env.hpp"
#include <string.h>

const String PlatformConstants::WINDOWSNT_4_MACHINE = "x86_nt_4";
const String PlatformConstants::WINDOWSNT_5_MACHINE = "x86_nt_5";
const String PlatformConstants::WINDOWS95_4_MACHINE = "x86_95_4";
const String PlatformConstants::OS2_4_MACHINE = "x86_os2_4";
const String PlatformConstants::AS400_OS400_4_MACHINE = "as400_os400_4";
const String PlatformConstants::HPUX_10_MACHINE = "hp9000_ux_10";
const String PlatformConstants::HPUX_11_MACHINE = "hp9000_ux_11";
const String PlatformConstants::MVSOE_2_MACHINE = "mvs390_oe_2";
const String PlatformConstants::POWERPC_LINUX_2_MACHINE = "ppc_linux_2";
const String PlatformConstants::POWERPC_AIX_4_MACHINE = "rios_aix_4";
const String PlatformConstants::POWERPC_AIX_5_MACHINE = "rios_aix_5";
const String PlatformConstants::INTEL_LINUX_2_MACHINE = "x86_linux_2";
const String PlatformConstants::INTEL_SCO_7_MACHINE = "x86_sco_7";
const String PlatformConstants::INTEL_SCO_5_MACHINE = "x86_sco_5";
const String PlatformConstants::INTEL_NETBSD_1_MACHINE = "x86_netbsd_1";
const String PlatformConstants::INTEL_OPENBSD_2_MACHINE = "x86_openbsd_2";
const String PlatformConstants::INTEL_FREEBSD_3_MACHINE = "x86_freebsd_3";
const String PlatformConstants::INTEL_SOLARIS_2_MACHINE = "x86_solaris_2";
const String PlatformConstants::INTEL_SOLARIS_7_MACHINE = "x86_solaris_7";
const String PlatformConstants::INTEL_SOLARIS_8_MACHINE = "x86_solaris_8";
const String PlatformConstants::INTEL_SOLARIS_9_MACHINE = "x86_solaris_9";
const String PlatformConstants::INTEL_DYNIXPTX_4_MACHINE = "x86_ptx_4";
const String PlatformConstants::INTEL_BEOS_4_MACHINE = "x86_beos_4";
const String PlatformConstants::INTEL64_AIX_5_MACHINE = "ia64_aix_5";
const String PlatformConstants::INTEL64_LINUX_2_MACHINE = "ia64_linux_2";
const String PlatformConstants::INTEL64_WINDOWSNT_5_MACHINE = "ia64_nt_5";
const String PlatformConstants::INTEL64_HPUX_11_MACHINE = "ia64_hpux_11";
const String PlatformConstants::INTEL64_HPOSS_6_MACHINE = "ia64_hposs_6";
const String PlatformConstants::ALPHA_LINUX_2_MACHINE = "alpha_linux_2";
const String PlatformConstants::ALPHA_TRU64_5_MACHINE = "alpha_tru64_5";
const String PlatformConstants::ALPHA_OPENVMS_7_MACHINE = "alpha_openvms_7";
const String PlatformConstants::SPARC_SOLARIS_2_MACHINE = "sparc_solaris_2";
const String PlatformConstants::SPARC_SOLARIS_7_MACHINE = "sparc_solaris_7";
const String PlatformConstants::SPARC_SOLARIS_8_MACHINE = "sparc_solaris_8";
const String PlatformConstants::SPARC_SOLARIS_9_MACHINE = "sparc_solaris_9";
const String PlatformConstants::SPARC_LINUX_2_MACHINE = "sparc_linux_2";
const String PlatformConstants::S390_LINUX_2_MACHINE = "s390_linux_2";
const String PlatformConstants::ZSERIES_LINUX_2_MACHINE = "zseries_linux_2";
const String PlatformConstants::MIPS_IRIX_6_MACHINE = "mips_irix_6";
const String PlatformConstants::MIPS_HPOSS_6_MACHINE = "mips_hposs_6";
const String PlatformConstants::IA32_INTERIX_35_MACHINE = "x86_interix_35";
const String PlatformConstants::UNKNOWN_MACHINE = "unknown";

const String PlatformConstants::CURRENT_MACHINE =
#if defined(WIN32)
    getMachine(); // use runtime detection
#elif defined(AIX_PPC)
    getMachine(); // use runtime detection
#elif defined(SOLARIS_SPARC)
    getMachine(); // use runtime detection
#elif defined(SOLARIS_X86)
    getMachine(); // use runtime detection
#elif defined(HPUX_RISC)
    getMachine(); // use runtime detection
#elif defined(SCO)
    getMachine(); // use runtime detection
#elif defined(OS2)
    OS2_4_MACHINE;
#elif defined(AIX_IA64)
    INTEL64_AIX_5_MACHINE;
#elif defined(MVSOE)
    MVSOE_2_MACHINE;
#elif defined(OS400)
    AS400_OS400_4_MACHINE;
#elif defined(NETBSD)
    INTEL_NETBSD_1_MACHINE;
#elif defined(OPENBSD)
    INTEL_OPENBSD_2_MACHINE;
#elif defined(FREEBSD)
    INTEL_FREEBSD_3_MACHINE;
#elif defined(LINUX_X86)
    INTEL_LINUX_2_MACHINE;
#elif defined(LINUX_PPC)
    POWERPC_LINUX_2_MACHINE;
#elif defined(LINUX_ALPHA)
    ALPHA_LINUX_2_MACHINE;
#elif defined(LINUX_SPARC)
    SPARC_LINUX_2_MACHINE;
#elif defined(LINUX_S390)
    S390_LINUX_2_MACHINE;
#elif defined(LINUX_ZSERIES)
    ZSERIES_LINUX_2_MACHINE;
#elif defined(LINUX_IA64)
    INTEL64_LINUX_2_MACHINE;
#elif defined(DYNIXPTX_X86)
    INTEL_DYNIXPTX_4_MACHINE;
#elif defined(BEOS_X86)
    INTEL_BEOS_4_MACHINE;
#elif defined(IRIX)
    MIPS_IRIX_6_MACHINE;
#elif defined(TRU64)
    ALPHA_TRU64_5_MACHINE;
#elif defined(OPENVMS_ALPHA)
    ALPHA_OPENVMS_7_MACHINE;
#elif defined(HPUX_IA64)
    INTEL64_HPUX_11_MACHINE;
#elif defined(HPOSS_IA64)
    INTEL64_HPOSS_6_MACHINE;
#elif defined(HPOSS_MIPS)
    MIPS_HPOSS_6_MACHINE;
#elif defined(INTERIX)
    getMachine(); // use runtime detection
#else
    UNKNOWN_MACHINE;
#endif

// the following end-of-line terminator should only be used
// for streams opened in binary mode
const String PlatformConstants::EOL_STRING =
#ifdef UNIX
    "\n";
#else
    "\r\n";
#endif

const String PlatformConstants::CMD_EXE_SHELL = "CMD.EXE";
const String PlatformConstants::COMMAND_COM_SHELL = "COMMAND.COM";
const String PlatformConstants::SH_SHELL = "/bin/sh";
const String PlatformConstants::KSH_SHELL = "/bin/ksh";
const String PlatformConstants::CSH_SHELL = "/bin/csh";
const String PlatformConstants::QSH_SHELL = "/usr/bin/qsh";

const String PlatformConstants::CMD_EXE_SHELL_CMDFLAG = "/C";
const String PlatformConstants::COMMAND_COM_SHELL_CMDFLAG = "/C";
const String PlatformConstants::SH_SHELL_CMDFLAG = "-c";
const String PlatformConstants::KSH_SHELL_CMDFLAG = "-c";
const String PlatformConstants::CSH_SHELL_CMDFLAG = "-c";
const String PlatformConstants::QSH_SHELL_CMDFLAG = "-c";


/**
 * Set the MACHINE environment variable.
**/
void PlatformConstants::setMACHINE()
{
  Env::setenv( StringConstants::MACHINE_VAR, CURRENT_MACHINE, false );
}


/**
 * Ensure the SHELL environment variable is set.
**/
void PlatformConstants::setShellInfo()
{
// Set SHELL on OS/2 to OS2_SHELL if it's set
#ifdef OS2
  const String *shell = Env::getenv( "OS2_SHELL" );
  if (shell != 0)
    Env::setenv( StringConstants::SHELL_VAR, *shell, false );
  // by default, OS/2 will quote DCE commands for remote builds
  Env::setenv( StringConstants::ODEMAKE_RDCECMD_QUOTED_VAR, "1", false );
#endif

#ifndef UNIX
  Env::setenv( StringConstants::SHELL_K_FLAG_VAR, "/K", false );
#endif

// NOTE: Windows 95 is detectable at runtime only, so the
// #define it uses is Windows NT's: CMD.  Override that here.
#if defined(DEFAULT_SHELL_IS_CMD)
  if (CURRENT_MACHINE.equals( WINDOWS95_4_MACHINE ))
  {
    Env::setenv( StringConstants::SHELL_VAR, COMMAND_COM_SHELL, false );
    Env::setenv( StringConstants::ODEMAKE_SHELL_VAR,
        COMMAND_COM_SHELL + " " + COMMAND_COM_SHELL_CMDFLAG + " ",
        false );
  }
  else
  {
    Env::setenv( StringConstants::SHELL_VAR, CMD_EXE_SHELL, false );
    Env::setenv( StringConstants::ODEMAKE_SHELL_VAR,
        CMD_EXE_SHELL + " " + CMD_EXE_SHELL_CMDFLAG + " ",
        false );
  }
#elif defined(DEFAULT_SHELL_IS_SH)
  Env::setenv( StringConstants::SHELL_VAR, SH_SHELL, false );
  Env::setenv( StringConstants::ODEMAKE_SHELL_VAR,
      SH_SHELL + " " + SH_SHELL_CMDFLAG + " ",
      false );
#elif defined(DEFAULT_SHELL_IS_CSH)
  Env::setenv( StringConstants::SHELL_VAR, CSH_SHELL, false );
  Env::setenv( StringConstants::ODEMAKE_SHELL_VAR,
      CSH_SHELL + " " + CSH_SHELL_CMDFLAG + " ",
      false );
#elif defined(DEFAULT_SHELL_IS_VMS)
  Env::setenv( StringConstants::ODEMAKE_SHELL_VAR, "ODERUN PIPE ", false );
#elif defined(DEFAULT_SHELL_IS_QSH)
  Env::setenv( StringConstants::SHELL_VAR, QSH_SHELL, false );
  Env::setenv( StringConstants::ODEMAKE_SHELL_VAR,
      QSH_SHELL + " " + QSH_SHELL_CMDFLAG + " ",
      false );
#else /* default is ksh */
  Env::setenv( StringConstants::SHELL_VAR, KSH_SHELL, false );
  Env::setenv( StringConstants::ODEMAKE_SHELL_VAR,
      KSH_SHELL + " " + KSH_SHELL_CMDFLAG + " ",
      false );
#endif /* DEFAULT_SHELL_IS_xxx */
}


/**
 * For cases where the same executable can be run on
 * different OS versions and/or architectures, this function
 * can be called for runtime detection of the "real" OS.
 *
 * DO NOT call this if you don't want runtime detection!
 *
 * If a specific #ifdef doesn't appear here, UNKNOWN_MACHINE
 * will be returned!
 *
**/
const String &PlatformConstants::getMachine()
{
  ODE_VERSION_DATA vd;
  int uname_rc;
  
  uname_rc = ::ODEuname( &vd );

//
// Windows
//
// Used to differentiate between NT and 95/98.
// We can also use this to detect Windows 2000 (NT 5).
// Unknown if we can tell IA64 from X86 with this data.
//
#if defined(WIN32)

  if (uname_rc == 0)
  {
    switch (vd.platform)
    {
      case WINDOWSNT_PLATFORM:
        if (vd.major_version == 5)
          return (WINDOWSNT_5_MACHINE);
        else
          return (WINDOWSNT_4_MACHINE);
        break;
      case WINDOWS95_PLATFORM:
        return (WINDOWS95_4_MACHINE);
        break;
      case WINDOWS31_PLATFORM:
        // someday?
        break;
      default:
        break;
    }
  }

  return (WINDOWSNT_4_MACHINE);

//
// OS/2
//
// Not called yet, but could be used to detect newer versions.
//
#elif defined(OS2)

  if (uname_rc == 0)
  {
    // check for other versions?
    // Note: For OS/2 Warp 4, you will get:
    // major_version: 20
    // minor_version: 40
    // revision_version: 0
  }

  return (OS2_4_MACHINE);

#elif defined(SCO)

  if (uname_rc == 0)
  {
    if (vd.version[0] == '5')
      return (INTEL_SCO_5_MACHINE);
  }

  return (INTEL_SCO_7_MACHINE);

#elif defined(AIX_PPC)

  if (uname_rc == 0)
  {
    if (vd.version[0] == '5')
      return (POWERPC_AIX_5_MACHINE);
  }

  return (POWERPC_AIX_4_MACHINE);

#elif defined(SOLARIS_SPARC)

  if (uname_rc == 0)
  {
    if (vd.release[2] == '7')
      return (SPARC_SOLARIS_7_MACHINE);
    else if (vd.release[2] == '8')
      return (SPARC_SOLARIS_8_MACHINE);
    else if (vd.release[2] == '9')
      return (SPARC_SOLARIS_9_MACHINE);
  }

  return (SPARC_SOLARIS_2_MACHINE);

#elif defined(SOLARIS_X86)

  if (uname_rc == 0)
  {
    if (vd.release[2] == '7')
      return (INTEL_SOLARIS_7_MACHINE);
    else if (vd.release[2] == '8')
      return (INTEL_SOLARIS_8_MACHINE);
    else if (vd.release[2] == '9')
      return (INTEL_SOLARIS_9_MACHINE);
  }

  return (INTEL_SOLARIS_2_MACHINE);

#elif defined(HPUX)

  if (uname_rc == 0 && strncmp( &vd.release[2], "11", 2 ) == 0)
    return (HPUX_11_MACHINE);

  return (HPUX_10_MACHINE);

#elif defined(INTERIX)

  if (uname_rc == 0 && strncmp( vd.release, "3.5", 3 ) == 0)
    return (IA32_INTERIX_35_MACHINE);

  return (IA32_INTERIX_35_MACHINE);

#endif

  // if we've gotten here, we don't know what machine we're on!
  return (UNKNOWN_MACHINE);
} // end of the getMachine() function
