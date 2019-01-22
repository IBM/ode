
rem THIS SCRIPT CREATES THE ODE C LIBRARY FOR WINDOWS NT/95/98

@set odelib_name=odeclibw

@if exist %odelib_name%.def goto create
@if not exist cppws35.def copy %CPPMAIN%\LIB\cppws35.def .
@echo YOU MUST EDIT CPPWS35.DEF FIRST!
@echo CHANGE THE LIBRARY NAME INSIDE TO %odelib_name%.
@echo THEN RENAME THE FILE TO %odelib_name%.def WHEN FINISHED.
@echo FINALLY, RE-RUN THIS SCRIPT.
@goto done

:create
touch empty.c
icc /C /Ge- /Gn /Gd empty.c
ilib /gi %odelib_name%.def
ilink /NOD /NOE /DLL /OUT:%odelib_name%.dll %odelib_name%.exp empty.obj %CPPMAIN%\lib\cppws35.lib %CPPMAIN%\lib\cppws35o.lib %CPPMAIN%\sdk\lib\kernel32.lib
@echo %odelib_name%.lib AND %odelib_name%.dll SHOULD NOW BE CREATED.

:done
