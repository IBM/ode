********************************************************************************
*** %RELEASE_NAME% README ***

Sections in this README:
  - INSTALLATION
  - ABOUT THIS DISTRIBUTION
  - KNOWN PROBLEMS/LIMITATIONS
  - FIXES PER BUILD

********************************************************************************
*** INSTALLATION ***

This is release %RELEASE_NAME% of ODE.  Refer to Administrator's guide
(ODEAdminsGuide.htm) for installation instructions.

********************************************************************************
*** ABOUT THIS DISTRIBUTION ***

This is a "base" distribution of the IBM Open Development Environment (ODE).
It is intended to provide users with a subset of the entire ODE toolset that
enables them to do builds of software and developer builds in ODE sandboxes.

What is not available in the "base" distribution:

  Packaging Tools and Rules -
    ODE provides a set of platform independent tools to create installable
    images for the native platforms.  Platforms supported: AIX, WinNT, HP-UX,
    Solaris(Sparc&Intel), SCO UnixWare and MVS/USS.

  C/C++ Makefile Dependency Generator (gendep)-
    The tool "gendep" preprocesses C/C++ files to a point to generate reliable
    Makefile-type dependency information to provide for more reliable builds.
    This tools is only run when needed, some compilers already provide this
    feature so ODE exploits it.  An example of some compilers that don't 
    generate Makefile-type dependency information: MS Visual C++.

  Some Sandbox Utilities -
    currentsb - Sandbox Information
    resb      - Retarget Sandbox
    sbinfo    - Sandbox Configuration File Information
    sbls      - Sandbox file listing.  Similar to "ls" or "dir" but aware of
                files in the backing chain.
    
********************************************************************************
*** KNOWN PROBLEMS/LIMITATIONS ***
  See %release_name%_b%LEVEL_NAME%_known_bugs.txt for known limitations and bugs

********************************************************************************
*** FIXES PER BUILD ***
  See %release_name%_b%LEVEL_NAME%_fixes.txt for a list of fixes per build

********************************************************************************
*** EOF ***
