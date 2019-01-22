package com.ibm.ode.lib.string;

// this class should not import any ODE classes,
// since mutually dependent initializers may result.

import java.io.File;

/**
 * OS/machine constants.  For performance reasons, this contains
 * a kludgey way of determining if you're on a case-sensitive
 * or Unix-based machine.  For both cases, it checks if the
 * file/directory separator character is a forward slash (/).
 * If so, it returns true.  This is only used for the functions
 * that take no arguments (onUnixOS, onCaseSensitiveOS, etc.).
 * If a machine/OS name is based, the actual machine/OS names are
 * used to determine the return value.
**/
public class PlatformConstants
{
  private static final char SEP_CHAR = File.separatorChar;

  // The value of the "os.name" property.
  public static final String ALPHA_LINUX_2_OSNAME="Linux";
  public static final String HP9000_UX_10_OSNAME="HP-UX";
  public static final String MVS390_OE_2_OSNAME="OS/390";
  public static final String PPC_LINUX_2_OSNAME="Linux";
  public static final String IA64_AIX_5_OSNAME="AIX"; // ???
  public static final String MIPS_IRIX_6_OSNAME="Irix";
  public static final String RIOS_AIX_4_OSNAME="AIX";
  public static final String SPARC_LINUX_2_OSNAME="Linux";
  public static final String SPARC_SOLARIS_2_OSNAME="Solaris";
  public static final String SPARC_SOLARIS_2_OSNAME2="SunOS";
  public static final String X86_95_4_OSNAME="Windows 95";
  public static final String X86_FREEBSD_3_OSNAME="FreeBSD";
  public static final String X86_LINUX_2_OSNAME="Linux";
  public static final String X86_NETBSD_1_OSNAME="NetBSD";
  public static final String X86_NT_4_OSNAME="Windows NT";
  public static final String X86_NT_5_OSNAME="Windows 2000";
  public static final String X86_XP_5_OSNAME="Windows XP";
  public static final String X86_OPENBSD_2_OSNAME="OpenBSD";
  public static final String X86_OS2_4_OSNAME="OS/2";
  public static final String X86_SCO_5_OSNAME="OpenServer";
  public static final String X86_SCO_7_OSNAME="UnixWare";
  public static final String X86_SOLARIS_2_OSNAME="Solaris";
  public static final String MVS390_OE_2Z_OSNAME="z/OS";

  // The value of the "os.arch" property.
  public static final String ALPHA_LINUX_2_OSARCH="alpha";
  public static final String HP9000_UX_10_OSARCH="PA-RISC";
  public static final String MVS390_OE_2_OSARCH="390";
  public static final String PPC_LINUX_2_OSARCH="ppc";
  public static final String IA64_AIX_5_OSARCH="IA64"; // ???
  public static final String MIPS_IRIX_6_OSARCH="mips";
  public static final String RIOS_AIX_4_OSARCH="POWER_PC";
  public static final String SPARC_LINUX_2_OSARCH="sparc";
  public static final String SPARC_SOLARIS_2_OSARCH="sparc";
  public static final String SPARC_SOLARIS_2_OSARCH2="sparc";
  public static final String X86_95_4_OSARCH="x86";
  public static final String X86_FREEBSD_3_OSARCH="x86";
  public static final String X86_LINUX_2_OSARCH="x86";
  public static final String X86_NETBSD_1_OSARCH="x86";
  public static final String X86_NT_4_OSARCH="x86";
  public static final String X86_NT_5_OSARCH="x86";
  public static final String X86_XP_5_OSARCH="x86";
  public static final String X86_OPENBSD_2_OSARCH="i386";
  public static final String X86_OS2_4_OSARCH="x86";
  public static final String X86_SCO_5_OSARCH="IA32";
  public static final String X86_SCO_7_OSARCH="IA32";
  public static final String X86_SOLARIS_2_OSARCH="x86";
  public static final String MVS390_OE_2Z_OSARCH="390";

  // The value of the "os.version" property.
  // NOT USED
  public static final String ALPHA_LINUX_2_OSVERSION="";
  public static final String HP9000_UX_10_OSVERSION="";
  public static final String MVS390_OE_2_OSVERSION="";
  public static final String PPC_LINUX_2_OSVERSION="";
  public static final String IA64_AIX_5_OSVERSION="";
  public static final String MIPS_IRIX_6_OSVERSION="";
  public static final String RIOS_AIX_4_OSVERSION="";
  public static final String SPARC_LINUX_2_OSVERSION="";
  public static final String SPARC_SOLARIS_2_OSVERSION="";
  public static final String SPARC_SOLARIS_2_OSVERSION2="";
  public static final String X86_95_4_OSVERSION="";
  public static final String X86_FREEBSD_3_OSVERSION="";
  public static final String X86_LINUX_2_OSVERSION="";
  public static final String X86_NETBSD_1_OSVERSION="";
  public static final String X86_NT_4_OSVERSION="";
  public static final String X86_NT_5_OSVERSION="";
  public static final String X86_XP_5_OSVERSION="";
  public static final String X86_OPENBSD_2_OSVERSION="";
  public static final String X86_OS2_4_OSVERSION="";
  public static final String X86_SCO_5_OSVERSION="";
  public static final String X86_SCO_7_OSVERSION="";
  public static final String X86_SOLARIS_2_OSVERSION="";
  public static final String MVS390_OE_2Z_OSVERSION="";

  // ODE machine names
  public static final String ALPHA_LINUX_2_MACHINE="alpha_linux_2";
  public static final String HP9000_UX_10_MACHINE="hp9000_ux_10";
  public static final String MVS390_OE_1_MACHINE="mvs390_oe_1";
  public static final String MVS390_OE_2_MACHINE="mvs390_oe_2";
  public static final String PPC_LINUX_2_MACHINE="ppc_linux_2";
  public static final String IA64_AIX_5_MACHINE="ia64_aix_5";
  public static final String MIPS_IRIX_6_MACHINE="mips_irix_6";
  public static final String RIOS_AIX_4_MACHINE="rios_aix_4";
  public static final String SPARC_LINUX_2_MACHINE="sparc_linux_2";
  public static final String SPARC_SOLARIS_2_MACHINE="sparc_solaris_2";
  public static final String SPARC_SOLARIS_2_MACHINE2="sparc_solaris_2";
  public static final String X86_95_4_MACHINE="x86_95_4";
  public static final String X86_FREEBSD_3_MACHINE="x86_freebsd_3";
  public static final String X86_LINUX_2_MACHINE="x86_linux_2";
  public static final String X86_NETBSD_1_MACHINE="x86_netbsd_1";
  public static final String X86_NT_4_MACHINE="x86_nt_4";
  public static final String X86_NT_5_MACHINE="x86_nt_4";
  public static final String X86_XP_5_MACHINE="x86_nt_4";
  public static final String X86_OPENBSD_2_MACHINE="x86_openbsd_2";
  public static final String X86_OS2_4_MACHINE="x86_os2_4";
  public static final String X86_SCO_5_MACHINE="x86_sco_7"; // use 7 for now
  public static final String X86_SCO_7_MACHINE="x86_sco_7";
  public static final String X86_SOLARIS_2_MACHINE="x86_solaris_2";
  public static final String MVS390_OE_2Z_MACHINE="mvs390_oe_2";

  /**
   * Array of all the OSNAMEs.
   *
   * This array must be arranged in the same order as OSARCHS,
   * OSVERSIONS, and MACHINES.
  **/
  public static final String[] OSNAMES = {
      ALPHA_LINUX_2_OSNAME, HP9000_UX_10_OSNAME, MVS390_OE_2_OSNAME,
      PPC_LINUX_2_OSNAME, IA64_AIX_5_OSNAME, MIPS_IRIX_6_OSNAME,
      RIOS_AIX_4_OSNAME, SPARC_LINUX_2_OSNAME, SPARC_SOLARIS_2_OSNAME,
      SPARC_SOLARIS_2_OSNAME2, X86_95_4_OSNAME, X86_FREEBSD_3_OSNAME,
      X86_LINUX_2_OSNAME, X86_NETBSD_1_OSNAME,
		X86_NT_4_OSNAME, X86_NT_5_OSNAME, X86_XP_5_OSNAME,
      X86_OPENBSD_2_OSNAME, X86_OS2_4_OSNAME, X86_SCO_7_OSNAME,
      X86_SOLARIS_2_OSNAME, X86_SCO_5_OSNAME, MVS390_OE_2Z_OSNAME
      };

  /**
   * Array of all the OSARCHs.
   *
   * This array must be arranged in the same order as OSNAMES,
   * OSVERSIONS, and MACHINES.
  **/
  public static final String[] OSARCHS = {
      ALPHA_LINUX_2_OSARCH, HP9000_UX_10_OSARCH, MVS390_OE_2_OSARCH,
      PPC_LINUX_2_OSARCH, IA64_AIX_5_OSARCH, MIPS_IRIX_6_OSARCH,
      RIOS_AIX_4_OSARCH, SPARC_LINUX_2_OSARCH, SPARC_SOLARIS_2_OSARCH,
      SPARC_SOLARIS_2_OSARCH2, X86_95_4_OSARCH, X86_FREEBSD_3_OSARCH,
      X86_LINUX_2_OSARCH, X86_NETBSD_1_OSARCH,
		X86_NT_4_OSARCH, X86_NT_5_OSARCH, X86_XP_5_OSARCH,
      X86_OPENBSD_2_OSARCH, X86_OS2_4_OSARCH, X86_SCO_7_OSARCH,
      X86_SOLARIS_2_OSARCH, X86_SCO_5_OSARCH, MVS390_OE_2Z_OSARCH
      };

  /**
   * Array of all the OSVERSIONs.
   *
   * This array must be arranged in the same order as OSNAMES,
   * OSARCHS, and MACHINES.
  **/
  public static final String[] OSVERSIONS = {
      ALPHA_LINUX_2_OSVERSION, HP9000_UX_10_OSVERSION, MVS390_OE_2_OSVERSION,
      PPC_LINUX_2_OSVERSION, IA64_AIX_5_OSVERSION, MIPS_IRIX_6_OSVERSION,
      RIOS_AIX_4_OSVERSION, SPARC_LINUX_2_OSVERSION, SPARC_SOLARIS_2_OSVERSION,
      SPARC_SOLARIS_2_OSVERSION2, X86_95_4_OSVERSION, X86_FREEBSD_3_OSVERSION,
      X86_LINUX_2_OSVERSION, X86_NETBSD_1_OSVERSION,
      X86_NT_4_OSVERSION, X86_NT_5_OSVERSION, X86_XP_5_OSVERSION,
      X86_OPENBSD_2_OSVERSION, X86_OS2_4_OSVERSION, X86_SCO_7_OSVERSION,
      X86_SOLARIS_2_OSVERSION, X86_SCO_5_OSVERSION, MVS390_OE_2Z_OSVERSION
      };

  /**
   * Array of all the MACHINEs.
   *
   * This array must be arranged in the same order as OSNAMES,
   * OSARCHS, and OSVERSIONS.
  **/
  public static final String[] MACHINES = {
      ALPHA_LINUX_2_MACHINE, HP9000_UX_10_MACHINE, MVS390_OE_2_MACHINE,
      PPC_LINUX_2_MACHINE, IA64_AIX_5_MACHINE, MIPS_IRIX_6_MACHINE,
      RIOS_AIX_4_MACHINE, SPARC_LINUX_2_MACHINE, SPARC_SOLARIS_2_MACHINE,
      SPARC_SOLARIS_2_MACHINE2, X86_95_4_MACHINE, X86_FREEBSD_3_MACHINE,
      X86_LINUX_2_MACHINE, X86_NETBSD_1_MACHINE,
      X86_NT_4_MACHINE, X86_NT_5_MACHINE, X86_XP_5_MACHINE,
      X86_OPENBSD_2_MACHINE, X86_OS2_4_MACHINE, X86_SCO_7_MACHINE,
      X86_SOLARIS_2_MACHINE, X86_SCO_5_MACHINE, MVS390_OE_2Z_MACHINE
      };

  public static final String CURRENT_OSNAME = System.getProperty( "os.name" );
  public static final String CURRENT_OSARCH = System.getProperty( "os.arch" );
  public static final String CURRENT_OSVERSION = System.getProperty(
      "os.version" );
  public static final String CURRENT_MACHINE = getMachineName( CURRENT_OSNAME,
      CURRENT_OSARCH, CURRENT_OSVERSION );

  /**
   * Get the ODE machine name for the specified Java os.name,
   * os.arch, and os.version.
   *
   * @return The machine name for the Java os.name.
   * If os_name is null, the current platform's
   * os.name value is used to determine the machine name.
   * If the os_name has no matching machine name, an
   * empty string is returned.
  **/
  public static String getMachineName( String os_name, String os_arch,
      String os_version )
  {
    String best_guess = "unknown";

    if (os_name == null)
      return (CURRENT_MACHINE);
    if (os_arch == null)
      os_arch = "";
    if (os_version == null)
      os_version = "";
    for (int i = 0; i < OSNAMES.length; ++i)
    {
      if (OSNAMES[i].equals( os_name ))
      {
        best_guess = MACHINES[i];
        if (OSARCHS[i].equals( os_arch ))
          break; // best_guess is the right one, so stop looking
      }
    }
    return (best_guess);
  }

  /**
   * Get the Java os.name of the specified ODE machine name.
   *
   * @return The Java os.name name for the machine_name.
   * If machine_name is null (or it has no matching os.name),
   * the current platform's os.name value is returned.
  **/
  public static String getOSName( String machine_name )
  {
    if (machine_name != null)
      for (int i = 0; i < MACHINES.length; ++i)
        if (MACHINES[i].equals( machine_name ))
          return (OSNAMES[i]);
    return (CURRENT_OSNAME);
  }

  /**
   * Get the Java os.arch of the specified ODE machine name.
   *
   * @return The Java os.arch name for the machine_name.
   * If machine_name is null (or it has no matching os.arch),
   * the current platform's os.arch value is returned.
  **/
  public static String getOSArch( String machine_name )
  {
    if (machine_name != null)
      for (int i = 0; i < MACHINES.length; ++i)
        if (MACHINES[i].equals( machine_name ))
          return (OSARCHS[i]);
    return (CURRENT_OSARCH);
  }

  /**
   * Get the Java os.version of the specified ODE machine name.
   *
   * @return The Java os.version name for the machine_name.
   * If machine_name is null (or it has no matching os.version),
   * the current platform's os.version value is returned.
  **/
  public static String getOSVersion( String machine_name )
  {
    if (machine_name != null)
      for (int i = 0; i < MACHINES.length; ++i)
        if (MACHINES[i].equals( machine_name ))
          return (OSVERSIONS[i]);
    return (CURRENT_OSVERSION);
  }

  public static boolean onCaseSensitiveMachine()
  {
      return (SEP_CHAR == '/');
  }

  /**
   * Checks if the specified system is case sensitive with
   * regards to filenames and environment variables.
  **/
  public static boolean isCaseSensitiveMachine( String machine_name )
  {
    if (X86_95_4_MACHINE.equals( machine_name ) ||
        X86_NT_4_MACHINE.equals( machine_name ) ||
        X86_NT_5_MACHINE.equals( machine_name ) ||
        X86_XP_5_MACHINE.equals( machine_name ) ||
        X86_OS2_4_MACHINE.equals( machine_name ))
      return (false);
    else
      return (true);
  }

  public static boolean onCaseSensitiveOS()
  {
      return (SEP_CHAR == '/');
  }

  /**
   * Checks if the specified system is case sensitive with
   * regards to filenames and environment variables.
  **/
  public static boolean isCaseSensitiveOS( String os_name )
  {
    if (X86_95_4_OSNAME.equals( os_name ) ||
        X86_NT_4_OSNAME.equals( os_name ) ||
        X86_NT_5_OSNAME.equals( os_name ) ||
        X86_XP_5_OSNAME.equals( os_name ) ||
        X86_OS2_4_OSNAME.equals( os_name ))
      return (false);
    else
      return (true);
  }

  public static boolean onUnixMachine()
  {
      return ( SEP_CHAR == '/' );
  }

  /**
   * Checks if the specified system is a Unix machine.
  **/
  public static boolean isUnixMachine( String machine_name )
  {
    if (X86_95_4_MACHINE.equals( machine_name ) ||
        X86_NT_4_MACHINE.equals( machine_name ) ||
        X86_NT_5_MACHINE.equals( machine_name ) ||
        X86_XP_5_MACHINE.equals( machine_name ) ||
        X86_OS2_4_MACHINE.equals( machine_name ))
      return (false);
    else
      return (true);
  }

  public static boolean onUnixOS()
  {
      return ( SEP_CHAR == '/' );
  }

  /**
   * Checks if the specified system is a Unix OS.
  **/
  public static boolean isUnixOS( String os_name )
  {
    if (X86_95_4_OSNAME.equals( os_name ) ||
        X86_NT_4_OSNAME.equals( os_name ) ||
        X86_NT_5_OSNAME.equals( os_name ) ||
        X86_XP_5_OSNAME.equals( os_name ) ||
        X86_OS2_4_OSNAME.equals( os_name ))
      return (false);
    else
      return (true);
  }

  /**
   * Checks if the specified system is an Windows machine.
  **/
  public static boolean isWindowsMachine( String machine_name )
  {
    if (X86_95_4_MACHINE.equals( machine_name ) ||
        X86_NT_4_MACHINE.equals( machine_name ) ||
        X86_NT_5_MACHINE.equals( machine_name ) ||
        X86_XP_5_MACHINE.equals( machine_name ))
      return (true);
    else
      return (false);
  }

  /**
   * Checks if the specified system is an Linux machine.
  **/
  public static boolean isLinuxMachine( String machine_name )
  {
    if ( X86_LINUX_2_MACHINE.equals( machine_name )    ||
         ALPHA_LINUX_2_MACHINE.equals( machine_name )  ||
         PPC_LINUX_2_MACHINE.equals( machine_name )    ||
         SPARC_LINUX_2_MACHINE.equals( machine_name ) )
      return (true);
    else
      return (false);
  }


  /**
   * Checks if the specified system is an AIX machine.
  **/
  public static boolean isAixMachine( String machine_name )
  {
    if (RIOS_AIX_4_MACHINE.equals( machine_name ) ||
        IA64_AIX_5_MACHINE.equals( machine_name ))
      return (true);
    else
      return (false);
  }

  /**
   * Checks if the specified system is an HP machine.
  **/
  public static boolean isHpMachine( String machine_name )
  {
    if (HP9000_UX_10_MACHINE.equals( machine_name ) )
      return (true);
    else
      return (false);
  }

  /**
   * Checks if the specified system is an MVS machine.
  **/
  public static boolean isMvsMachine( String machine_name )
  {
    if (MVS390_OE_2_MACHINE.equals( machine_name ) )
      return (true);
    else
      return (false);
  }

  /**
   * Checks if the specified system is a Sun machine.
  **/
  public static boolean isSunMachine( String machine_name )
  {
    if (SPARC_SOLARIS_2_MACHINE.equals( machine_name ) ||
        X86_SOLARIS_2_MACHINE.equals( machine_name ))
      return (true);
    else
      return (false);
  }

  /**
   * Checks if the specified system is a SCO/Unixware machine.
  **/
  public static boolean isScoMachine( String machine_name )
  {
    if (X86_SCO_5_MACHINE.equals( machine_name ) ||
        X86_SCO_7_MACHINE.equals( machine_name ))
      return (true);
    else
      return (false);
  }

  /**
   * Checks if the specified system is an IRIX machine.
  **/
  public static boolean isIrixMachine( String machine_name )
  {
    if (MIPS_IRIX_6_MACHINE.equals( machine_name ) )
      return (true);
    else
      return (false);
  }
}
