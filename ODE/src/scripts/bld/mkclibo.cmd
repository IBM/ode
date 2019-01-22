
rem THIS SCRIPT CREATES THE ODE C LIBRARY FOR OS/2

@set odelib_name=odeclibo

@if exist %odelib_name%.def goto create
@if not exist cpprsi36.def copy %CXXMAIN%\LIB\cpprsi36.def .
@echo YOU MUST EDIT CPPRSI36.DEF FIRST!
@echo CHANGE THE LIBRARY NAME INSIDE TO %odelib_name%
@echo THEN RENAME THE FILE TO %odelib_name%.def WHEN FINISHED.
@echo FINALLY, RE-RUN THIS SCRIPT.
@goto done

:create
touch empty.c
icc /C /Ge- /Gn /Gd empty.c
ilib /gi %odelib_name%.def
ilink /NOD /NOE /DLL /OUT:%odelib_name%.dll %odelib_name%.def empty.obj %CXXMAIN%\lib\cpprss36.lib %CXXMAIN%\lib\cpprso36.lib %TKMAIN%\som\lib\somtk.lib %TKMAIN%\lib\os2386.lib
@echo %odelib_name%.lib AND %odelib_name%.dll SHOULD NOW BE CREATED.

:done
