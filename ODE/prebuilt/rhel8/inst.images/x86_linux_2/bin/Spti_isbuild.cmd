echo off
rem 
rem  This is a front-end to Installshield's ISBuild tool
rem  Arguments:
rem        %1: Package Control Directory (absolute path)
rem        %2: Package Output Directory (absolute path)
rem        %3: InstallShield Media Name
rem        %4: Path to isbuild
rem        %5: Any extra flags to be used by isbuild
rem 
rem  Notes:
rem       The media must have been pre-created using 
rem       Installshield's IDE.
rem
echo ------------------------------------------------------------------
echo Package Control Directory      : %1
echo Package Output Directory       : %2
echo Media Name                     : %3
echo Path to isbuild                : %4 
echo Package Flag                   : %5
echo ------------------------------------------------------------------
echo Packaging...
echo %4 -m%3 -p%1 -b%2 %5
%4 -m%3 -p%1 -b%2 %5
