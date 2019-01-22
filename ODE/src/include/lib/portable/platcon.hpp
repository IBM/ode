/**
 * PlatformConstants
 *
**/
#ifndef _ODE_LIB_PORTABLE_PLATCON_HPP_
#define _ODE_LIB_PORTABLE_PLATCON_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"


/**
 * Platform names and such.
 */
class PlatformConstants
{
  public:

    static const String ODEDLLPORT WINDOWSNT_4_MACHINE; // x86_nt_4
    static const String ODEDLLPORT WINDOWSNT_5_MACHINE; // x86_nt_5
    static const String ODEDLLPORT WINDOWS95_4_MACHINE; // x86_95_4
    static const String ODEDLLPORT OS2_4_MACHINE; // x86_os2_4
    static const String ODEDLLPORT AS400_OS400_4_MACHINE; // as400_os400_4
    static const String ODEDLLPORT HPUX_10_MACHINE; // hp9000_ux_10
    static const String ODEDLLPORT HPUX_11_MACHINE; // hp9000_ux_11
    static const String ODEDLLPORT MVSOE_2_MACHINE; // mvs390_oe_2
    static const String ODEDLLPORT POWERPC_LINUX_2_MACHINE; // ppc_linux_2
    static const String ODEDLLPORT POWERPC_AIX_4_MACHINE; // rios_aix_4
    static const String ODEDLLPORT POWERPC_AIX_5_MACHINE; // rios_aix_5
    static const String ODEDLLPORT INTEL_LINUX_2_MACHINE; // x86_linux_2
    static const String ODEDLLPORT INTEL_SCO_7_MACHINE; // x86_sco_7
    static const String ODEDLLPORT INTEL_SCO_5_MACHINE; // x86_sco_5
    static const String ODEDLLPORT INTEL_NETBSD_1_MACHINE; // x86_netbsd_1
    static const String ODEDLLPORT INTEL_OPENBSD_2_MACHINE; // x86_openbsd_2
    static const String ODEDLLPORT INTEL_FREEBSD_3_MACHINE; // x86_freebsd_3
    static const String ODEDLLPORT INTEL_SOLARIS_2_MACHINE; // x86_solaris_2
    static const String ODEDLLPORT INTEL_SOLARIS_7_MACHINE; // x86_solaris_7
    static const String ODEDLLPORT INTEL_SOLARIS_8_MACHINE; // x86_solaris_8
    static const String ODEDLLPORT INTEL_SOLARIS_9_MACHINE; // x86_solaris_9
    static const String ODEDLLPORT INTEL_DYNIXPTX_4_MACHINE; // x86_ptx_4
    static const String ODEDLLPORT INTEL_BEOS_4_MACHINE; // x86_beos_4
    static const String ODEDLLPORT INTEL64_AIX_5_MACHINE; // ia64_aix_5
    static const String ODEDLLPORT INTEL64_LINUX_2_MACHINE; // ia64_linux_2
    static const String ODEDLLPORT INTEL64_WINDOWSNT_5_MACHINE; // ia64_nt_5
    static const String ODEDLLPORT INTEL64_HPUX_11_MACHINE; // ia64_hpux_11
    static const String ODEDLLPORT INTEL64_HPOSS_6_MACHINE; // ia64_hposs_6
    static const String ODEDLLPORT ALPHA_LINUX_2_MACHINE; // alpha_linux_2
    static const String ODEDLLPORT ALPHA_TRU64_5_MACHINE; // alpha_tru64_5
    static const String ODEDLLPORT ALPHA_OPENVMS_7_MACHINE; // alpha_openvms_7
    static const String ODEDLLPORT SPARC_SOLARIS_2_MACHINE; // sparc_solaris_2
    static const String ODEDLLPORT SPARC_SOLARIS_7_MACHINE; // sparc_solaris_7
    static const String ODEDLLPORT SPARC_SOLARIS_8_MACHINE; // sparc_solaris_8
    static const String ODEDLLPORT SPARC_SOLARIS_9_MACHINE; // sparc_solaris_9
    static const String ODEDLLPORT SPARC_LINUX_2_MACHINE; // sparc_linux_2
    static const String ODEDLLPORT S390_LINUX_2_MACHINE; // s390_linux_2
    static const String ODEDLLPORT ZSERIES_LINUX_2_MACHINE; // zseries_linux_2
    static const String ODEDLLPORT MIPS_IRIX_6_MACHINE; // mips_irix_6
    static const String ODEDLLPORT MIPS_HPOSS_6_MACHINE; // mips_hposs_6
    static const String ODEDLLPORT IA32_INTERIX_35_MACHINE; // x86_interix_35
    static const String ODEDLLPORT UNKNOWN_MACHINE; // unknown

    static const String ODEDLLPORT CMD_EXE_SHELL; // CMD.EXE
    static const String ODEDLLPORT COMMAND_COM_SHELL; // COMMAND.COM
    static const String ODEDLLPORT SH_SHELL; // /bin/sh
    static const String ODEDLLPORT KSH_SHELL; // /bin/ksh
    static const String ODEDLLPORT CSH_SHELL; // /bin/csh
    static const String ODEDLLPORT QSH_SHELL; // /usr/bin/qsh

    static const String ODEDLLPORT CMD_EXE_SHELL_CMDFLAG; // /C
    static const String ODEDLLPORT COMMAND_COM_SHELL_CMDFLAG; // /C
    static const String ODEDLLPORT SH_SHELL_CMDFLAG; // -c
    static const String ODEDLLPORT KSH_SHELL_CMDFLAG; // -c
    static const String ODEDLLPORT CSH_SHELL_CMDFLAG; // -c
    static const String ODEDLLPORT QSH_SHELL_CMDFLAG; // -c

    static const String ODEDLLPORT CURRENT_MACHINE;

    static const String ODEDLLPORT EOL_STRING;

    inline static boolean onCaseSensitiveMachine();
    inline static boolean onCaseSensitiveOS();

    static void setMACHINE();
    static void setShellInfo();


  private:

    static const String &getMachine();
};

inline boolean PlatformConstants::onCaseSensitiveMachine()
{
#ifndef CASE_INSENSITIVE_OS
  return (true);
#else
  return (false);
#endif
}

inline boolean PlatformConstants::onCaseSensitiveOS()
{
#ifndef CASE_INSENSITIVE_OS
  return (true);
#else
  return (false);
#endif
}

#endif /* _ODE_LIB_PORTABLE_PLATCON_HPP_ */
