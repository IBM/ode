# ode
OSF Development Environment IBM modified

As coded, you will need to install the 'ksh' package to run an ODE build.

You need to set TOOLSBASE environment variable to the directory where the ODE binaries can be found
(e.g. export TOOLSBASE=/home/tjcw/eclipse-workspace/ode/ODE/inst.images/x86_linux_2/bin/ )

You need to set LD_LIBRARY_PATH to the directory where the library lib0500.so can be found
(e.g. export LD_LIBRARY_PATH=/home/tjcw/eclipse-workspace/ode/ODE/inst.images/x86_linux_2/bin/:$LD_LIBTARY_PATH )

This package includes assistance for bootstrapping for x86_linux_2 in directory 'bootstrap'; it should be
easy to bootstrap for linux_2 on other hardware by reference to the `odecc` script and the 'iostream.h'
header file to help with following the instructions in the ODEManualCompile document.

A prebuilt ODE for x86_linux_2 is also supplied.
