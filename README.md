# ode
OSF Development Environment IBM modified

ODE had tooling for building projects written in C, C++, and Java

As coded, you will need to install the 'ksh' package to run an ODE build.

You need to set TOOLSBASE environment variable to the directory where the ODE binaries can be found
(e.g. export TOOLSBASE=${HOME}/eclipse-workspace/ode/ODE/inst.images/x86_linux_2/bin/ )

You need to set LD_LIBRARY_PATH to the directory where the library lib0500.so can be found
(e.g. export LD_LIBRARY_PATH=${HOME}/eclipse-workspace/ode/ODE/inst.images/x86_linux_2/bin/:$LD_LIBTARY_PATH )

This package includes assistance for bootstrapping for x86_linux_2 in directory 'bootstrap'; it should be
easy to bootstrap for linux_2 on other hardware by reference to the `odecc` script and the 'iostream.h'
header file to help with following the instructions in the ODEManualCompile document.

A prebuilt ODE for x86_linux_2 is also supplied.

A sample project buildable with ODE is available with 'git clone git@github.com:IBM/BlueMatter.git'
Make a sandbox with a command like
mksb -back ${HOME}/eclipse-workspace/ode/ODE/ -dir ${HOME}/eclipse-workspace/BlueMatter/  -m x86_linux_2 svntrunk

Select the sandbox with
workon -sb svntrunk

Attempt the build with
TRY_LINUX_BUILD_ANYWAY=1 build -k

