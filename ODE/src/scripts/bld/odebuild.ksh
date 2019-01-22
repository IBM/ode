#!/bin/ksh
#basic script for building, installing and zipping ODE

read RELEASE < release.name
read RELEASE_NUM < release.num
read LEVEL < build.name

OSTYPE=`uname`
ARCHTYPE=`uname -m`

#
# Determine context for logfile name
#
if   [[ $OSTYPE = "AIX"      ]]; then
  unset CLASSPATH
  context=rios_aix_4
elif [[ $OSTYPE = "Monterey64"   ]] ; then
  unset CLASSPATH
  context=ia64_aix_4
elif [[ $OSTYPE = "SunOS"    ]] ; then
  if [[   $ARCHTYPE = "i86pc"    ]]; then
    context=x86_solaris_2
  else
    context=sparc_solaris_2
  fi
elif [[ $OSTYPE = "HP-UX"    ]] ; then
  context=hp9000_ux_10
elif [[ $OSTYPE = "Linux"    ]] ; then
  if [[   $ARCHTYPE = "ppc"    ]]; then
    context=ppc_linux_2
  elif [[ $ARCHTYPE = "sparc"  ]]; then
    context=sparc_linux_2
  elif [[ $ARCHTYPE = "alpha"  ]]; then
    context=alpha_linux_2
  elif [[ $ARCHTYPE = "s390"  ]]; then
    context=s390_linux_2
  elif [[ $ARCHTYPE = "s390x"  ]]; then
    context=zseries_linux_2
  elif [[ $ARCHTYPE = "ia64" ]]; then
    context=ia64_linux_2
  else
    context=x86_linux_2
  fi
elif [[ $OSTYPE = "UnixWare" ]] ; then
  context=x86_sco_7
elif [[ $OSTYPE = "NetBSD" ]] ; then
  context=x86_netbsd_1
elif [[ $OSTYPE = "FreeBSD" ]] ; then
  context=x86_freebsd_3
elif [[ $OSTYPE = "OpenBSD" ]] ; then
  context=x86_openbsd_2
elif [[ $OSTYPE = "IRIX" ]] ; then
  context=mips_irix_6
elif [[ $OSTYPE = "OSF1" ]] ; then
  context=alpha_tru64_5
else
  print "Unknown platform *$OSTYPE*"
  return 1
fi

BUILD_ROOT=/ode/build
BB_BASE=$BUILD_ROOT/$RELEASE_NUM/$LEVEL
LOGFILE=$BB_BASE/logs/${context}.bldlog

if [[ $context = "sparc_solaris_2" ]]; then
  DEFAULT_PARM="/unjarXMLParser_all /build_all /install_all /zip_all"
else
  DEFAULT_PARM="/build_all /install_all /zip_all"
fi

if [[ $# -eq 0 ]] ; then
  buildcmd="build -rc $BUILD_ROOT/$RELEASE_NUM/.sbrc -sb $LEVEL -k $DEFAULT_PARM"
else
  buildcmd="build -rc $BUILD_ROOT/$RELEASE_NUM/.sbrc -sb $LEVEL -k $*"
fi
print $buildcmd
print "Logging to $LOGFILE"
if [[ $SIMULATE != "yes" ]]; then
  $buildcmd > $LOGFILE 2>&1
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* built for $RELEASE"
else
  print "Level *$LEVEL* build failed for $RELEASE"
  return 1
fi

return 0

