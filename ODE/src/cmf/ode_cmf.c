/*************************************************************************/
//  ode_cmf.c
//  Product %RELEASE_NAME%
//  Tested on sunode1 (sparc_solaris), sunode2(x86_solaris),
//                     hpode1(hp9000_ux), ode3(rios_aix), ode9(x86_sco)
//  Known bugs:
//        Hp aCC preprocessor concatenates strings, fix was to use gnu.
//        Sco - some problem, same fix.
//
/*************************************************************************/

/*
  Flags defined in the cmf/Makefile.
//
   **************    DO NOT UNCOMMENT       **************
//
  EXAMPLELS   = ${REL_NAME}_${BUILD_NUM}_bbexample.zip
  RULES       = ${REL_NAME}_${BUILD_NUM}_rules.zip
  CONFS       = ${REL_NAME}_${BUILD_NUM}_confs.zip
  TOOLS       = ${REL_NAME}_${BUILD_NUM}_tools.jar
  BASE_NAME   = ${IDIR_PREF}  = Directory where files are installed
                 /opt, /usr, /usr/lpp, or user defined.
*/

// utility macros
#define QUOTE(s2) #s2
#define STRING(s) QUOTE(s)
#define APPEND(value1, value2)  value1 ## value2
#define MKPATH(path, filename)  APPEND(path, filename)

#define PARENT ode
#define PARENT_DIR /ode
#define ODE_DIR MKPATH(BASE_NAME,MKPATH(PARENT_DIR,/))
#define INSTALLDIR MKPATH(BASE_NAME,PARENT_DIR)
#define EXAMPLE_TGT MKPATH(ODE_DIR,MKPATH(examples,/))
#define DOC_TGT MKPATH(ODE_DIR,MKPATH(doc,/))
#define BIN_TGT MKPATH(ODE_DIR,MKPATH(bin,/))
#define TOOLS_TGT MKPATH(ODE_DIR,MKPATH(bin,/))

#if defined(_linux_) || defined(_aix_) || defined(RPM)
   #define BIN_SRC MKPATH(ODE_DIR,MKPATH(bin,/))
   #define LIB_SRC MKPATH(ODE_DIR,MKPATH(bin,/))
   #define LIB_TGT MKPATH(MKPATH(BASE_NAME,/),MKPATH(lib,/))
   #define EXAMPLE_SRC MKPATH(ODE_DIR,MKPATH(examples,/))
   #define TOOLS_SRC MKPATH(ODE_DIR,MKPATH(bin,/))
   #define DOC_SRC MKPATH(ODE_DIR,MKPATH(doc,/))
#else
   #define BIN_SRC  /bin/
   #define EXAMPLE_SRC  ../ship/
   #define TOOLS_SRC ../ship/
   #define DOC_SRC ../doc/
#endif

#ifdef MKINSTALL
   #define BOOT_RQMT  'No'
   #define F_TYPE  'F'
   #define D_TYPE  'D'
   #define S_TYPE  'S'
   #define MODE  "755"
   #define ENTITY_1  "ode.bin"
   #define ENTITY_2  "ode.doc"
   #define ENTITY_3  "ode.example"
#elif defined(PKGMK)
   #define BOOT_RQMT  'No'
   #define F_TYPE  'f'
   #define D_TYPE  'd'
   #define MODE  "755"
   #define ENTITY_1  "ode.bin"
   #define ENTITY_2  "ode.doc"
   #define ENTITY_3  "ode.example"
   #define PKG_CONSTANT_1  "!BIN=/usr/bin"
   #define PKG_CONSTANT_2  "!PROJDIR=/usr/projects"
#elif defined(RPM)
   #define F_TYPE  'file'
   #define D_TYPE  'dir'
   #define MODE  "755"
   #define ENTITY_1  "bin"
#elif defined(SWPACKAGE)
   #define BOOT_RQMT  'n'
   #define F_TYPE  'F'
   #define D_TYPE  'D'
   #define MODE  "-m 755"
   #define ENTITY_1  "odebin"
   #define ENTITY_2  "odedoc"
   #define ENTITY_3  "odeexample"
#endif

/*
   In order for the build process to work as before we must ignore
   maintLevel and fixLevel on HP so undefine them.
*/
#ifdef SWPACKAGE
   #undef MINOR_MINOR_VERSION
   #undef BUILD_NUMBER
   #define MINOR_MINOR_VERSION 
   #define BUILD_NUMBER 
#endif

// Representing the product ode
ODE :  InstallEntity {

Common  :
  EntityInfo {
        entityName      =  "ode";
        imageName       =  "ode";  // Only used by AIX platform.
        version         =  MAJOR_VERSION;
        release         =  MINOR_VERSION;
        maintLevel      =  MINOR_MINOR_VERSION;
        fixLevel        =  BUILD_NUMBER;
        copyright       =  "Copyright IBM, 2000";
        copyrightKeys   = ["%%_IBMa" "%%_MITb"];
        copyrightMap    = < /usr/pkgtest/copyright.map >;
        language        =  'en_us';
        content         =  'USR';
     #ifdef RPM
        fullEntityName = ["The IBM ODE software development/packaging system."];
        description    = "The IBM Open Development Environment (ODE) provides a
        method for developers to simultaneously and independently create code
        for various releases of a program.  This development process works in
        conjuction with, and does not interfere with, established releases
        controlled by release administrators.  Developers can perform builds to
        test the functioning of their code against established program levels
        (sndboxes and backing builds).  Release administrators can use ODE to
        create new backing builds and, ultimately, new releases of code for
        completely different platforms.";
        category        = 'Development/Tools';
     #else
        fullEntityName   = ["ode"]; // Used on hp platform.
        description     = "ODE Package";
        category         = 'application';
     #endif
        }

  LinkInfo {
     #ifdef RPM
         immChildEntities =  [ ENTITY_1 ];
     #else
         immChildEntities =  [ ENTITY_1 ENTITY_2 ENTITY_3 ];
     #endif
         immChildFiles    =;
         parent           = NULL;
         }

  VendorInfo {
         vendorName       = "IBM";
         vendorDesc       = "At IBM, we strive to lead in the creation, development and manufacture of the industry's most advanced information technologies, including computer systems, software, networking systems, storage devices and microelectronics."; 
         }

  ArchitectureInfo
         {
      #ifdef RPM
        #if defined(_linux_)
           osName       = [ "Linux" ];
           osVersion    = [ "6" ];
           osRelease    = [ "1" ];
        #elif defined(_solaris_)
           osName       = [ "Solaris" ];
           osVersion    = [ "2" ];
           osRelease    = [ "6" ];
        #elif defined(_rios_aix_)
           osName       = [ "Aix" ];
           osVersion    = [ "4" ];
           osRelease    = [ "2" ];
        #endif
      #elif !defined(SWPACKAGE)
         machineType  = "R";
         osName       = [ "sparc_solaris" "rios_aix" "ia64_aix" "hp9000_ux" 
                                                     "x86_nt" "ia64_hpux" ];
         osRelease    = [ "2" "4" "4" "10" "4" "11" ];
         osVersion    = [ "3" "1.5.0" "3" "0.0" "0" "23" ];
      #endif
         }

  InstallStatesInfo
         {
  #ifndef RPM
         bootReqmt       = BOOT_RQMT;
  #endif
         packageFlags    =  "-L";
  #ifdef PKGMK
         constantList    = [ PKG_CONSTANT_1  PKG_CONSTANT_2 ];
         selection       = "1";
  #else
         selection       =  "-Y";
  #endif
         mediaId         = '9709261004';
         installStates   = "3";
         removableStates = "3";
  #ifdef RPM
         installDir      =  STRING(INSTALLDIR);
  #else
         installDir      = "/opt/ode/";
         installSpace    = "data 500  1";
  #endif
         }

  RequisitesInfo
         {
  #ifdef MKINSTALL
         requisites    = [("C" "pkgtools.core 1.0.1.0") ];
  #endif
         }
         
  ServiceInfo
         {
         contactName   = "Wayne Mathis";
         }
  }  // End of ODE install entity.

  // Representing the 'bin' fileset
Bin_fileset :
InstallEntity {
  Common :
  EntityInfo {
         entityName      =  ENTITY_1;  // Only used by HP
         entityId        =  ENTITY_1;      
         versionDate     =  "0150";
         version         =  MAJOR_VERSION;
         release         =  MINOR_VERSION;
         maintLevel      =  MINOR_MINOR_VERSION;
         fixLevel        =  BUILD_NUMBER;
         copyright       =  "Copyright IBM 2000";
         language        =  'en_us';
         content         =  'USR';
         insList         =  [ < ode.bin.il > ];
      #ifdef RPM
         fullEntityName  = ["The executables for the IBM ODE."];
         description     = "Install these if you are a developer or packager
         interested in having consistent and reproduceable software builds.";
         category        = 'Development/Tools';
      #else
         description     = "The executables in ODE";
      #endif
         }
  LinkInfo {
         immChildFiles   =  [
         < MKPATH(BASE_NAME,/) >
         < ODE_DIR >
         < BIN_SRC >
         < MKPATH(BIN_SRC,LIBODE) >
         < MKPATH(BIN_SRC,Spti_mkinstall) >
         < MKPATH(BIN_SRC,Spti_pkgmk) >
         < MKPATH(BIN_SRC,Spti_rpm) >
         < MKPATH(BIN_SRC,Spti_swpackage) >
         < MKPATH(BIN_SRC,Spti_isbuild.cmd) >
         < MKPATH(BIN_SRC,Spti_pftwwiz.cmd) >
        < MKPATH(BIN_SRC,Spti_buildpatch) >
	 < MKPATH(TOOLS_SRC,TOOLS) >
         < MKPATH(BIN_SRC,build) >
         < MKPATH(BIN_SRC,crlfcon) >
         < MKPATH(BIN_SRC,currentsb) >
         < MKPATH(BIN_SRC,gendep) >
         < MKPATH(BIN_SRC,genpath) >
         < MKPATH(BIN_SRC,mk) >
         < MKPATH(BIN_SRC,mkbb) >
         < MKPATH(BIN_SRC,mkdep) >
         < MKPATH(BIN_SRC,mklinks) >
         < MKPATH(BIN_SRC,mkpath) >
         < MKPATH(BIN_SRC,mksb) >
         < MKPATH(BIN_SRC,resb) >
         < MKPATH(BIN_SRC,sbinfo) >
         < MKPATH(BIN_SRC,sbmerge) >
         < MKPATH(BIN_SRC,sbls) >
         < MKPATH(BIN_SRC,workon) >
      #ifdef MKINSTALL
         < MKPATH(LIB_TGT,LIBODE) >
      #endif
      #ifdef RPM
         < DOC_SRC >
         < MKPATH(DOC_SRC,readme.txt) >
         < MKPATH(DOC_SRC,fixes.txt) >
         < MKPATH(DOC_SRC,known_bugs.txt) >
         < MKPATH(DOC_SRC,ODELicense_ILA.txt) >
         < MKPATH(DOC_SRC,ODELicense_3rdParty.txt) >
         < EXAMPLE_SRC >
         < MKPATH(EXAMPLE_SRC, EXAMPLES) >
         < MKPATH(EXAMPLE_SRC, RULES) >
         < MKPATH(EXAMPLE_SRC, CONFS) >
      #endif
                               ];
         parent          =  STRING(PARENT);
         }
  InstallStatesInfo
         {
      #if !defined(RPM)
               bootReqmt       = BOOT_RQMT;
      #endif
         }
}
End_bin_fileset :
// Representing directory (/opt|/usr) using file stanza
file {
         fileType          = D_TYPE;
         sourceDir         = STRING(MKPATH(BASE_NAME,/));
         targetDir         = STRING(MKPATH(BASE_NAME,/));
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing directory (/opt|/usr)/ode using file stanza
file {
         fileType          = D_TYPE;
         sourceDir         = STRING(MKPATH(BASE_NAME,MKPATH(PARENT_DIR,/)));
         targetDir         = STRING(MKPATH(BASE_NAME,MKPATH(PARENT_DIR,/)));
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing directory (/opt|/usr)/ode/bin using file stanza
file {
         fileType          = D_TYPE;
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/build using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "build";
         targetFile        = "build";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/crlfcon using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "crlfcon";
         targetFile        = "crlfcon";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/currentsb using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "currentsb";
         targetFile        = "currentsb";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/gendep using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "gendep";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         targetFile        = "gendep";
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/genpath using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "genpath";
         targetFile        = "genpath";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/LIBODE using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(LIBODE);
         targetFile        = STRING(LIBODE);
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
#ifdef MKINSTALL
// Representing link (/opt|/usr)/lib/LIBODE using file stanza
file {
         fileType          = S_TYPE;
         sourceFile        = STRING(LIBODE);
         targetFile        = STRING(LIBODE);
         sourceDir         = STRING(LIB_TGT);
         targetDir         = STRING(LIB_SRC);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
#endif
// Representing file (/opt|/usr)/ode/bin/mk using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "mk";
         targetFile        = "mk";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/mkbb using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "mkbb";
         targetFile        = "mkbb";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/mkdep using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "mkdep";
         targetFile        = "mkdep";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/mklinks using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "mklinks";
         targetFile        = "mklinks";
         sourceDir         =  STRING(BIN_SRC);
         targetDir         =  STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/mkpath using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "mkpath";
         targetFile        = "mkpath";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/mksb using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "mksb";
         targetFile        = "mksb";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/resb using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "resb";
         targetFile        = "resb";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/sbinfo using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "sbinfo";
         targetFile        = "sbinfo";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/sbmerge using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "sbmerge";
         targetFile        = "sbmerge";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/sbls using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "sbls";
         targetFile        = "sbls";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
      }
// Representing file (/opt|/usr)/ode/bin/Spti_mkinstall using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(Spti_mkinstall);
         targetFile        = STRING(Spti_mkinstall);
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/Spti_mkinstall using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(Spti_pkgmk);
         targetFile        = STRING(Spti_pkgmk);
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/Spti_mkinstall using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(Spti_rpm);
         targetFile        = STRING(Spti_rpm);
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/Spti_mkinstall using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(Spti_swpackage);
         targetFile        = STRING(Spti_swpackage);
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/Spti_isbuild.cmd using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(Spti_isbuild.cmd);
         targetFile        = STRING(Spti_isbuild.cmd);
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/Spti_pftwwiz.cmd using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(Spti_pftwwiz.cmd);
         targetFile        = STRING(Spti_pftwwiz.cmd);
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/Spti_buildpatch using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(Spti_buildpatch);
         targetFile        = STRING(Spti_buildpatch);
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/TOOLS using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(TOOLS);
         targetFile        = STRING(TOOLS);
         sourceDir         = STRING(TOOLS_SRC);
         targetDir         = STRING(TOOLS_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing file (/opt|/usr)/ode/bin/workon using file stanza
file {
         fileType          = F_TYPE;
         sourceFile        = "workon";
         targetFile        = "workon";
         sourceDir         = STRING(BIN_SRC);
         targetDir         = STRING(BIN_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
#if !defined(RPM)
// Representing the 'doc' fileset
Doc_fileset :
InstallEntity {
  Common :
  EntityInfo {
         entityName      = ENTITY_2;   // Only used by HP
         entityId        = ENTITY_2;
         versionDate     = "0150";
         version         =  MAJOR_VERSION;
         release         =  MINOR_VERSION;
         maintLevel      =  MINOR_MINOR_VERSION;
         fixLevel        =  BUILD_NUMBER;
         copyright       = "Copyright IBM 2000";
         insList         = [ < ode.doc.il > ];
         content         = 'USR';
      #ifdef RPM
         fullEntityName  = ["Documentation for ODE."];
         description     = "The README, FIXES, KNOWN_BUGS and license agreement text files.";
         category        = 'Development/Tools';
      #else
         description     = "Documentation for ODE.";
      #endif
         }

  LinkInfo {
         immChildFiles   =   [
         < DOC_SRC >
         < MKPATH(DOC_SRC,readme.txt) >	
         < MKPATH(DOC_SRC,fixes.txt) >
         < MKPATH(DOC_SRC,known_bugs.txt) >
         < MKPATH(DOC_SRC,ODELicense_ILA.txt) >
         < MKPATH(DOC_SRC,ODELicense_3rdParty.txt) >
		              ];

         parent          =  STRING(PARENT);

         }

  InstallStatesInfo
         {
  #ifndef RPM
         bootReqmt       = BOOT_RQMT;
  #endif
         }
}
End_Doc_fileset :
#endif

// Representing directory (/opt|/usr)/ode/doc using file Stanza
file {
         fileType          = D_TYPE;
         sourceDir         = STRING(DOC_SRC);
         targetDir         = STRING(DOC_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing the file (/opt|/usr)/ode/doc/readme.txt using file stanza 
file {
         fileType          = F_TYPE;
         sourceFile        = "readme.txt";
         targetFile        = "readme.txt";
         sourceDir         = STRING(DOC_SRC);
         targetDir         = STRING(DOC_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     } 
// Representing the file (/opt|/usr)/ode/doc/fixes.txt using file stanza 
file {
         fileType          = F_TYPE;
         sourceFile        = "fixes.txt";
         targetFile        = "fixes.txt";
         sourceDir         = STRING(DOC_SRC);
         targetDir         = STRING(DOC_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     } 
// Representing the file (/opt|/usr)/ode/doc/known_bugs.txt using file stanza 
file {
         fileType          = F_TYPE;
         sourceFile        = "known_bugs.txt";
         targetFile        = "known_bugs.txt";
         sourceDir         = STRING(DOC_SRC);
         targetDir         = STRING(DOC_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     } 
// Representing the file (/opt|/usr)/ode/doc/ODELicense_ILA.txt using file stanza 
file {
         fileType          = F_TYPE;
         sourceFile        = "ODELicense_ILA.txt";
         targetFile        = "ODELicense_ILA.txt";
         sourceDir         = STRING(DOC_SRC);
         targetDir         = STRING(DOC_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     } 
// Representing the file (/opt|/usr)/ode/doc/ODELicense_3rdParty.txt using file stanza 
file {
         fileType          = F_TYPE;
         sourceFile        = "ODELicense_3rdParty.txt";
         targetFile        = "ODELicense_3rdParty.txt";
         sourceDir         = STRING(DOC_SRC);
         targetDir         = STRING(DOC_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     } 
#if !defined(RPM)
// Representing the 'examples' fileset
Examples_fileset :
InstallEntity {
  Common :
  EntityInfo {
         entityName      = ENTITY_3;   // Only used by HP
         entityId        = ENTITY_3;
         versionDate     = "0150";
         version         =  MAJOR_VERSION;
         release         =  MINOR_VERSION;
         maintLevel      =  MINOR_MINOR_VERSION;
         fixLevel        =  BUILD_NUMBER;
         copyright       = "Copyright IBM 2000";
         insList         = [ < ode.examples.il > ];
         content         = 'USR';
      #ifdef RPM
         fullEntityName  = ["The bbexample source and rules files compressed"];
         description     = "Install these to build the example backing build.";
         category        = 'Development/Tools';
      #else
         description    = "The bbexample source and rules files (compressed).";
      #endif
         }

  LinkInfo {
         immChildFiles   =   [
         < EXAMPLE_SRC >
         < MKPATH(EXAMPLE_SRC, EXAMPLES) >	
         < MKPATH(EXAMPLE_SRC, RULES) >
         < MKPATH(EXAMPLE_SRC, CONFS) >
		              ];

         parent          =  STRING(PARENT);

         }

  InstallStatesInfo
         {
  #ifndef RPM
         bootReqmt       = BOOT_RQMT;
  #endif
         }
}
End_Eamples_fileset :
#endif

// Representing directory (/opt|/usr)/ode/examples using file Stanza
file {
         fileType          = D_TYPE;
         sourceDir         = STRING(EXAMPLE_SRC);
         targetDir         = STRING(EXAMPLE_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     }
// Representing the file (/opt|/usr)/ode/examples/EXAMPLES using file stanza 
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(EXAMPLES);
         targetFile        = STRING(EXAMPLES);
         sourceDir         = STRING(EXAMPLE_SRC);
         targetDir         = STRING(EXAMPLE_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     } 
// Representing the file (/opt|/usr)/ode/examples/RULES using file stanza 
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(RULES);
         targetFile        = STRING(RULES);
         sourceDir         = STRING(EXAMPLE_SRC);
         targetDir         = STRING(EXAMPLE_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     } 
// Representing the file (/opt|/usr)/ode/examples/CONFS using file stanza 
file {
         fileType          = F_TYPE;
         sourceFile        = STRING(CONFS);
         targetFile        = STRING(CONFS);
         sourceDir         = STRING(EXAMPLE_SRC);
         targetDir         = STRING(EXAMPLE_TGT);
         permissions       = MODE;
         userId            = "root";
         groupId           = "sys";
     } 
