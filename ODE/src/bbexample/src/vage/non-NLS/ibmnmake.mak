#*******************************************************************************
#* SAMPLE PROJECT: hello5 Makefile for Windows                                 *
#*                                                                             *
#* COPYRIGHT:                                                                  *
#* ----------                                                                  *
#* Copyright (C) International Business Machines Corp., 1992,1996.             *
#*                                                                             *
#* DISCLAIMER OF WARRANTIES:                                                   *
#* -------------------------                                                   *
#* The following [enclosed] code is sample code created by IBM                 *
#* Corporation. This sample code is not part of any standard IBM product       *
#* and is provided to you solely for the purpose of assisting you in the       *
#* development of your applications.  The code is provided "AS IS",            *
#* without warranty of any kind.  IBM shall not be liable for any damages      *
#* arising out of your use of the sample code, even if they have been          *
#* advised of the possibility of such damages.                                 *
#*                                                                             *
#*******************************************************************************

# Make file assumptions:
#    - Environment variable INCLUDE contains paths to:
#       IBM Compiler target_directory\include;
#       IBM Developer's Toolkit target_directory include paths
#    - Environment variable LIB contains paths to:
#       IBM Compiler target_directory\lib;
#       IBM Developer's Toolkit target_directory lib paths
#    - Current directory contains source files. Originals are in:
#        IBM Compiler target_directory\samples\ioc\hello5
#    - current directory will be used to store:
#        object, executable, and resource files
#
# RTF versus IPF:
#    - This makefile by default uses RTP source files to create RTF help.
#      By specifying USE_IPF=1, the makefile will use IPF sources files
#      to create IPF help.  Example: nmake USE_IPF=1 /a

# --- Tool defintions ---
ERASE=ERASE
GCPPC=ICC
GLINK=ICC
GRC=IRC
GRCV=IRCCNV
GIPFC=IPFC
GHCW=HCW
GIPFCVIEW=IVIEW
GIMAGE=IBMPCNV

# --- Tool flags ---
ICLCPPOPTS=/Gm+ /Gd+ /Gh+ /Ti+ /Fb+ /Q+
!IFDEF USE_IPF
ICLCPPOPTS=/Gm+ /Gd+ /Gh+ /Ti+ /Fb+ /Q+ /DUSE_IPF
!ENDIF
GCPPFLAGS=$(LOCALOPTS) $(ICLCPPOPTS)
GCPPLFLAGS=/Tdp /B"/pmtype:pm /debug /browse"
GPERFOBJ=cppwpa3.obj
GRCFLAGS=-DIC_WIN
GRCVFLAGS=
GIPFCFLAGS=/q
GHCWFLAGS=/c /e
GIMAGEFLAGS=

# --- Body ---
all:  hello5.exe ahellow5.hlp

hello5.exe:  ahellow5.obj adialog5.obj aearthw5.obj ahellow5.res
      $(GLINK) $(GCPPLFLAGS) $(GCPPFLAGS) /Fe"hello5.exe" \
      ahellow5.obj adialog5.obj aearthw5.obj $(GPERFOBJ) ahellow5.res

ahellow5.obj:  ahellow5.cpp ahellow5.hpp ahellow5.h
      $(GCPPC) /C+ $(GCPPFLAGS) ahellow5.cpp

adialog5.obj:  adialog5.cpp adialog5.hpp ahellow5.h ahellow5.hpp
      $(GCPPC) /C+ $(GCPPFLAGS) adialog5.cpp

aearthw5.obj:  aearthw5.cpp aearthw5.hpp ahellow5.h ahellow5.hpp
      $(GCPPC) /C+ $(GCPPFLAGS) aearthw5.cpp

ahellow5.res:  ahellow5.rc ahellow5.h ahellow5.ico
      $(GRC) $(GRCFLAGS) ahellow5.rc

ahellow5.rc:  ahellow5.rcx ahellow5.ico
      $(GRCV) $(GRCVFLAGS) ahellow5.rcx ahellow5.rc

ahellow5.ico:  ahellow5.icx
      $(GIMAGE) -I $(GIMAGEFLAGS) ahellow5.icx ahellow5.ico

# --- RTP Help ---
!IFNDEF USE_IPF
ahellow5.hlp:  ahellow5.hpj ahellow5.rtf
      $(GHCW) $(GHCWFLAGS) ahellow5.hpj
!ENDIF

# --- IPF Help ---
!IFDEF USE_IPF
ahellow5.hlp:  ahellow5.ipf
      $(GIPFC) $(GIPFCFLAGS) ahellow5.ipf
!ENDIF


# --- Cleanup ---
clean:
        -$(ERASE) hello5.exe
        -$(ERASE) ahellow5.obj
        -$(ERASE) adialog5.obj
        -$(ERASE) aearthw5.obj
        -$(ERASE) ahellow5.pdb
        -$(ERASE) adialog5.pdb
        -$(ERASE) aearthw5.pdb
        -$(ERASE) ahellow5.res
        -$(ERASE) ahellow5.ph
        -$(ERASE) ahellow5.hlp
#        -$(ERASE) ahellow5.rc
#        -$(ERASE) ahellow5.ico

