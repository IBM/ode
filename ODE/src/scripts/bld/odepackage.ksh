#!/bin/ksh
# basic script for packaging ODE

read RELEASE < release.name
read RELEASE_NUM < release.num
read LEVEL < build.name

OSTYPE=`uname`
ARCHTYPE=`uname -m`

BUILD_ROOT=/ode/build
BB_BASE=$BUILD_ROOT/$RELEASE_NUM/$LEVEL

# Determine context for logfile name
if   [[ $OSTYPE = "AIX"      ]]; then
  unset CLASSPATH
  context=rios_aix_4
  pkg_tgts='-DPACKAGE /parse_all /install_all /runpkgtool_all'
  export LIBPATH=$BB_BASE/inst.images/$context/bin:$LIBPATH
elif [[ $OSTYPE = "Monterey64"    ]] ; then
  unset CLASSPATH
  context=ia64_aix_4
  pkg_tgts='-DPACKAGE /parse_all /install_all /runpkgtool_all'
  export LD_LIBRARY_PATH=$BB_BASE/inst.images/$context/bin:$LD_LIBRARY_PATH
elif [[ $OSTYPE = "SunOS"    ]] ; then
  pkg_tgts='/parse_all /runpkgtool_all'
  if [[   $ARCHTYPE = "i86pc"    ]]; then
    context=x86_solaris_2
  else
    context=sparc_solaris_2
  fi
  export LD_LIBRARY_PATH=$BB_BASE/inst.images/$context/bin:$LD_LIBRARY_PATH
elif [[ $OSTYPE = "HP-UX"    ]] ; then
  pkg_tgts='/parse_all /runpkgtool_all'
  context=hp9000_ux_10
  export SHLIB_PATH=$BB_BASE/inst.images/$context/bin:$SHLIB_PATH
elif [[ $OSTYPE = "Linux"    ]] ; then
  pkg_tgts='-DPACKAGE /install_all /parse_all /runpkgtool_all'
  context=x86_linux_2
  export LD_LIBRARY_PATH=$BB_BASE/inst.images/$context/bin:$LD_LIBRARY_PATH
elif [[ $OSTYPE = "UnixWare" ]] ; then
  pkg_tgts='/parse_all /runpkgtool_all'
  context=x86_sco_7
  export LD_LIBRARY_PATH=$BB_BASE/inst.images/$context/bin:$LD_LIBRARY_PATH
else
  print "Non-supported packaging platform *$OSTYPE*"
  return 1
fi

export CLASSPATH=$BB_BASE/inst.images/ship/ode${RELEASE_NUM}_b${LEVEL}_tools.jar:$CLASSPATH
export PATH=$BB_BASE/inst.images/$context/bin:$PATH
export TOOLSBASE=$BB_BASE/inst.images/$context/bin/

LOGFILE=$BB_BASE/logs/${context}.pkglog

buildcmd="build -rc $BUILD_ROOT/$RELEASE_NUM/.sbrc -sb $LEVEL -k $pkg_tgts"
print $buildcmd
print "Logging to $LOGFILE"
if [[ $SIMULATE != "yes" ]]; then
  $buildcmd > $LOGFILE 2>&1
fi
if [[ $? -eq 0 ]] ; then
  print "Level *$LEVEL* packaged for $RELEASE"
else
  print "Level *$LEVEL* package failed for $RELEASE"
  return 1
fi

return 0
