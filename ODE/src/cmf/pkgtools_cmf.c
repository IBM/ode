// This is the CMF for the AIX pkgtools package

// utility macros
#define QUOTE(s2) #s2
#define STRING(s) QUOTE(s)
#define APPEND(value1, value2)  value1 ## value2
#define MKPATH(path, filename)  APPEND(path, filename)
/*
  Flags defined in the Makefile.
  BASE_NAME   = {IDIR_PREF}  = Directory where files are installed
*/
#define TOP_DIR /opt/pkgtools/
#define PKG_DIR MKPATH(BASE_NAME,TOP_DIR)
#define BIN_DIR MKPATH(PKG_DIR,MKPATH(rte/bin,/))
#define LIB_DIR MKPATH(PKG_DIR,MKPATH(rte/lib,/))
#define README_DIR MKPATH(PKG_DIR,MKPATH(rte,/))
#define SYMREADME_DIR PKG_DIR 
#define LIBPKG_DIR MKPATH(LIB_DIR,MKPATH(Pkg,/))

#define SYMBIN_DIR MKPATH(PKG_DIR,MKPATH(bin,/))
#define SYMLIB_DIR MKPATH(PKG_DIR,MKPATH(lib,/))
#define SYMLIBPKG_DIR MKPATH(SYMLIB_DIR,MKPATH(Pkg,/))


// Representing the Product pkgtools
InstallEntity {

Common :
 EntityInfo {
                entityName      =  "pkgtools" ;
                fullEntityName  =  ["pkgtools"] ;
                description     =  "AIX Packaging Tools";
                imageName       =  "pkgtools";
                version         =  MAJOR_VERSION;
                release         =  MINOR_VERSION;
                maintLevel      =  MINOR_MINOR_VERSION;
                fixLevel        =  BUILD_NUMBER;
                copyright       =  "Copyright IBM, 2000" ;
                category        = 'application';
                copyrightKeys   = ["%%_IBMa" "%%_MITb"] ;
                copyrightMap    = < /usr/pkgtest/copyright.map >;
                language        =  'en_us' ;
                content         =  'USR' ;
            }

 LinkInfo   {
                immChildEntities =  [ "pkgtools.rte" ]  ;
                immChildFiles    = ;
                parent           = NULL ;
            }

 ArchitectureInfo
            {
                machineType     = "R" ;
                osName          = [ OS_NAME ];
                osRelease       = [ "4" ];
                osVersion       = [ "1.5.0" ];
            }

 RequisitesInfo
            {
                  requisites    = [("P" "pkgtools.core 1.0.1.0") ];
            }

  }



// Represent the fileset pkgtools.rte

InstallEntity {

Common :
 EntityInfo {
                entityName      =  "pkgtools.rte";
                entityId        =  "pkgtools.rte";      
                description     =  "AIX packaging tools";
                versionDate     =  "7302";
                version         =  MAJOR_VERSION;
                release         =  MINOR_VERSION;
                maintLevel      =  MINOR_MINOR_VERSION;
                fixLevel        =  BUILD_NUMBER;
                copyright       =  "Copyright IBM 2000";
                language        =  'en_us';
                content         =  'USR' ;
                insList         =  [ <pkgtools.rte.il> ];
            }
 LinkInfo   {

      immChildFiles   =  [
            < PKG_DIR >
            < BIN_DIR >
            < MKPATH(README_DIR,README_Aix_Pkg_Tools) >
            < MKPATH(BIN_DIR,mkinstall) >
            < MKPATH(BIN_DIR,mkupdate) >
            < LIB_DIR >
            < MKPATH(LIB_DIR,pkgtools.conf) >
            < MKPATH(LIB_DIR,ODM_stanza_file.pm) >
            < MKPATH(LIB_DIR,ODM_stanza.pm) >
            < LIBPKG_DIR >
            < MKPATH(LIBPKG_DIR,Config.pm) >
            < MKPATH(LIBPKG_DIR,Fileset.pm) >
            < MKPATH(LIBPKG_DIR,Log.pm) >
            < MKPATH(LIBPKG_DIR,Object.pm) >
            < MKPATH(LIBPKG_DIR,Packager.pm) >
            < MKPATH(LIBPKG_DIR,Parser.pm) >
            < MKPATH(LIBPKG_DIR,Product.pm) >
            < MKPATH(LIBPKG_DIR,Tokenizer.pm) >
            < MKPATH(LIBPKG_DIR,Update.pm) >       

            < SYMBIN_DIR >
            < MKPATH(SYMREADME_DIR,README_Aix_Pkg_Tools) >
            < MKPATH(SYMBIN_DIR,mkinstall) >
            < MKPATH(SYMBIN_DIR,mkupdate) >
            < SYMLIB_DIR >
            < MKPATH(SYMLIB_DIR,pkgtools.conf) >
            < MKPATH(SYMLIB_DIR,ODM_stanza_file.pm) >
            < MKPATH(SYMLIB_DIR,ODM_stanza.pm) >
            < SYMLIBPKG_DIR >
            < MKPATH(SYMLIBPKG_DIR,Config.pm) >
            < MKPATH(SYMLIBPKG_DIR,Fileset.pm) >
            < MKPATH(SYMLIBPKG_DIR,Log.pm) >
            < MKPATH(SYMLIBPKG_DIR,Object.pm) >
            < MKPATH(SYMLIBPKG_DIR,Packager.pm) >
            < MKPATH(SYMLIBPKG_DIR,Parser.pm) >
            < MKPATH(SYMLIBPKG_DIR,Product.pm) >
            < MKPATH(SYMLIBPKG_DIR,Tokenizer.pm) >
            < MKPATH(SYMLIBPKG_DIR,Update.pm) >  
                  
      ];

                parent          =  "pkgtools";
            }
InstallStatesInfo
            {
                bootReqmt       = 'No'  ;
            }

}


// Representing directory /usr/opt/pkgtools
file {
         partNum           = '1';
         fileType          = 'D' ;
         sourceDir         = STRING(PKG_DIR);
         targetDir         = STRING(PKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }


// Representing directory /usr/opt/pkgtools/rte/bin
file {
         partNum           = '1';
         fileType          = 'D' ;
         sourceDir         = STRING(BIN_DIR);
         targetDir         = STRING(BIN_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgt/ols/README_Aix_Pkg_tools
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "README_Aix_Pkg_Tools";
         targetFile        = "README_Aix_Pkg_Tools";
         sourceDir         = STRING(README_DIR);
         targetDir         = STRING(README_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/bin/mkinstall
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "mkinstall";
         targetFile        = "mkinstall";
         sourceDir         = STRING(BIN_DIR);
         targetDir         = STRING(BIN_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/bin/mkupdate
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "mkupdate";
         targetFile        = "mkupdate";
         sourceDir         = STRING(BIN_DIR);
         targetDir         = STRING(BIN_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing directory /usr/opt/pkgtools/rte/lib
file {
         partNum           = '1';
         fileType          = 'D' ;
         sourceDir         = STRING(LIB_DIR);
         targetDir         = STRING(LIB_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }


// Representing file /usr/opt/pkgtools/rte/lib/pkgtools.conf
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "pkgtools.conf";
         targetFile        = "pkgtools.conf";
         sourceDir         = STRING(LIB_DIR);
         targetDir         = STRING(LIB_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/lib/ODM_stanza_file.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "ODM_stanza_file.pm";
         targetFile        = "ODM_stanza_file.pm";
         sourceDir         = STRING(LIB_DIR);
         targetDir         = STRING(LIB_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/lib/ODM_stanza.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "ODM_stanza.pm";
         targetFile        = "ODM_stanza.pm";
         sourceDir         = STRING(LIB_DIR);
         targetDir         = STRING(LIB_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing directory /usr/opt/pkgtools/rte/lib/Pkg
file {
         partNum           = '1';
         fileType          = 'D' ;
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }


// Representing file /usr/opt/pkgtools/rte/lib/Pkg/Config.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "Config.pm";
         targetFile        = "Config.pm";
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }


// Representing file /usr/opt/pkgtools/rte/lib/Pkg/Fileset.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "Fileset.pm";
         targetFile        = "Fileset.pm";
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/lib/Pkg/Log.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "Log.pm";
         targetFile        = "Log.pm";
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/lib/Pkg/Object.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "Object.pm";
         targetFile        = "Object.pm";
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/lib/Pkg/Packager.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "Packager.pm";
         targetFile        = "Packager.pm";
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/lib/Pkg/Parser.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "Parser.pm";
         targetFile        = "Parser.pm";
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/lib/Pkg/Product.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "Product.pm";
         targetFile        = "Product.pm";
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/lib/Pkg/Tokenizer.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "Tokenizer.pm";
         targetFile        = "Tokenizer.pm";
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing file /usr/opt/pkgtools/rte/lib/Pkg/Update.pm
file {
         partNum           = '1';
         fileType          = 'F' ;
         sourceFile        = "Update.pm";
         targetFile        = "Update.pm";
         sourceDir         = STRING(LIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }


// Representing directory /usr/opt/pkgtools/bin
file {
         partNum           = '1';
         fileType          = 'D' ;
         sourceDir         = STRING(SYMBIN_DIR);
         targetDir         = STRING(SYMBIN_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }


// Representing symbolic link /usr/opt/pkgtools/README_Aix_Pkg_Tools
file {
         partNum           = '1';
         fileType          = 'S';
         sourceFile        = "README_Aix_Pkg_Tools";
         targetFile        = "README_Aix_Pkg_Tools";
         sourceDir         = STRING(SYMREADME_DIR);
         targetDir         = STRING(README_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
     }

// Representing symbolic link /usr/opt/pkgtools/bin/mkinstall
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "mkinstall";
         targetFile        = "mkinstall";
         sourceDir         = STRING(SYMBIN_DIR);
         targetDir         = STRING(BIN_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/bin/mkupdate
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "mkupdate";
         targetFile        = "mkupdate";
         sourceDir         = STRING(SYMBIN_DIR);
         targetDir         = STRING(BIN_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing directory /usr/opt/pkgtools/lib
file {
         partNum           = '1';
         fileType          = 'D' ;
         sourceDir         = STRING(SYMLIB_DIR);
         targetDir         = STRING(SYMLIB_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }


// Representing symbolic link /usr/opt/pkgtools/lib/pkgtools.conf
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "pkgtools.conf";
         targetFile        = "pkgtools.conf";
         sourceDir         = STRING(SYMLIB_DIR);
         targetDir         = STRING(LIB_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/lib/ODM_stanza_file.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "ODM_stanza_file.pm";
         targetFile        = "ODM_stanza_file.pm";
         sourceDir         = STRING(SYMLIB_DIR);
         targetDir         = STRING(LIB_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/lib/ODM_stanza.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "ODM_stanza.pm";
         targetFile        = "ODM_stanza.pm";
         sourceDir         = STRING(SYMLIB_DIR);
         targetDir         = STRING(LIB_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing directory /usr/opt/pkgtools/lib/Pkg
file {
         partNum           = '1';
         fileType          = 'D' ;
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(SYMLIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }


// Representing symbolic link /usr/opt/pkgtools/lib/Pkg/Config.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "Config.pm";
         targetFile        = "Config.pm";
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }


// Representing symbolic link /usr/opt/pkgtools/lib/Pkg/Fileset.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "Fileset.pm";
         targetFile        = "Fileset.pm";
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/lib/Pkg/Log.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "Log.pm";
         targetFile        = "Log.pm";
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/lib/Pkg/Object.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "Object.pm";
         targetFile        = "Object.pm";
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/lib/Pkg/Packager.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "Packager.pm";
         targetFile        = "Packager.pm";
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/lib/Pkg/Parser.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "Parser.pm";
         targetFile        = "Parser.pm";
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/lib/Pkg/Product.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "Product.pm";
         targetFile        = "Product.pm";
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/lib/Pkg/Tokenizer.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "Tokenizer.pm";
         targetFile        = "Tokenizer.pm";
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

// Representing symbolic link /usr/opt/pkgtools/lib/Pkg/Update.pm
file {
         partNum           = '1';
         fileType          = 'S' ;
         sourceFile        = "Update.pm";
         targetFile        = "Update.pm";
         sourceDir         = STRING(SYMLIBPKG_DIR);
         targetDir         = STRING(LIBPKG_DIR);
         permissions       = "555";
         userId            = "root";
         groupId           = "sys";
      }

