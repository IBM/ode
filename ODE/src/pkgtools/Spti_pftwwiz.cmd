echo off
rem 
rem  This is a front-end to Installshield's ISBuild tool
rem  Arguments:
rem        %1: Path to pftwwiz 
rem        %2: PackageForTheWeb project file name (absolute path)
rem 
rem  Notes:
rem       The related PFTW project file must have
rem       have been pre-created.
rem
echo ------------------------------------------------------------------
echo Path to ptfwwiz                : %1
echo PackageForTheWeb Project File  : %2
echo ------------------------------------------------------------------
echo Generating self-extracting executable ...
echo %1 %2 -a -s
%1 %2 -a -s
