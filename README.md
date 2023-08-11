# ode
OSF Development Environment IBM modified

ODE has tooling for building projects written in C, C++, and Java. 

It is used for building on many platforms, including Linux and Windows.

As coded, you will need to install the 'ksh' package to run an ODE build.

You need to set TOOLSBASE environment variable to the directory where the ODE binaries can be found
(e.g. export TOOLSBASE=${HOME}/eclipse-workspace/ode/ODE/inst.images/x86_linux_2/bin/ )

You need to set PATH environment variable to the directory where the ODE binaries can be found
(e.g. export PATH=${HOME}/eclipse-workspace/ode/ODE/inst.images/x86_linux_2/bin/:$PATH )

You need to set LD_LIBRARY_PATH to the directory where the library lib0500.so can be found
(e.g. export LD_LIBRARY_PATH=${HOME}/eclipse-workspace/ode/ODE/inst.images/x86_linux_2/bin/:$LD_LIBRARY_PATH )

This package includes assistance for bootstrapping for x86_linux_2 in directory 'bootstrap'; there is a script bin/bootstrap_linux which should bootstrap for linux on other processor architectures. This script has been tested on Fedora 38 and RHEL8, both on x86_64. Part of the bootstrap process writes to ~/.sandboxrc ; if you want to retry the bootstrap process you should remove or rename this file.

A prebuilt ODE for x86_linux_2 is also supplied. 
The files under bootstrap/prebuilt/fedora38 were built by Fedora 38, and the
files under bootstrap/prebuilt/inst.images/x86_linux_2.rhel8 were built by Red Hat Enterprise Linux 8. It should not be necessary to use this prebuilt ODE as the bootstrap_linux should succeed.

A sample project buildable with ODE is available with 'git clone git@github.com:IBM/BlueMatter.git'

Make a sandbox with a command like

mksb -back ${HOME}/eclipse-workspace/ode/ODE/ -dir ${HOME}/eclipse-workspace/BlueMatter/  -m x86_linux_2 svntrunk

Select the sandbox with

workon -sb svntrunk

Attempt the build with

TRY_LINUX_BUILD_ANYWAY=1 build -k

