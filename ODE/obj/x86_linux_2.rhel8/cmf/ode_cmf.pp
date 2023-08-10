ODE : InstallEntity {
Common :
  EntityInfo {
        entityName = "ode";
        imageName = "ode";
        version = '050';
        release = '';
        maintLevel = '0';
        fixLevel = 'ODE';
        copyright = "Copyright IBM, 2000";
        copyrightKeys = ["%%_IBMa" "%%_MITb"];
        copyrightMap = < /usr/pkgtest/copyright.map >;
        language = 'en_us';
        content = 'USR';
        fullEntityName = ["The IBM ODE software development/packaging system."];
        description = "The IBM Open Development Environment (ODE) provides a
        method for developers to simultaneously and independently create code
        for various releases of a program. This development process works in
        conjuction with, and does not interfere with, established releases
        controlled by release administrators. Developers can perform builds to
        test the functioning of their code against established program levels
        (sndboxes and backing builds). Release administrators can use ODE to
        create new backing builds and, ultimately, new releases of code for
        completely different platforms.";
        category = 'Development/Tools';
        }
  LinkInfo {
         immChildEntities = [ "bin" ];
         immChildFiles =;
         parent = NULL;
         }
  VendorInfo {
         vendorName = "IBM";
         vendorDesc = "At IBM, we strive to lead in the creation, development and manufacture of the industry's most advanced information technologies, including computer systems, software, networking systems, storage devices and microelectronics.";
         }
  ArchitectureInfo
         {
           osName = [ "Linux" ];
           osVersion = [ "6" ];
           osRelease = [ "1" ];
         }
  InstallStatesInfo
         {
         packageFlags = "-L";
         selection = "-Y";
         mediaId = '9709261004';
         installStates = "3";
         removableStates = "3";
         installDir = "/usr/ode";
         }
  RequisitesInfo
         {
         }
  ServiceInfo
         {
         contactName = "Wayne Mathis";
         }
  }
Bin_fileset :
InstallEntity {
  Common :
  EntityInfo {
         entityName = "bin";
         entityId = "bin";
         versionDate = "0150";
         version = '050';
         release = '';
         maintLevel = '0';
         fixLevel = 'ODE';
         copyright = "Copyright IBM 2000";
         language = 'en_us';
         content = 'USR';
         insList = [ < ode.bin.il > ];
         fullEntityName = ["The executables for the IBM ODE."];
         description = "Install these if you are a developer or packager
         interested in having consistent and reproduceable software builds.";
         category = 'Development/Tools';
         }
  LinkInfo {
         immChildFiles = [
         < /usr/ >
         < /usr/ode/ >
         < /usr/ode/bin/ >
         < /usr/ode/bin/lib0500.so >
         < /usr/ode/bin/Spti_mkinstall >
         < /usr/ode/bin/Spti_pkgmk >
         < /usr/ode/bin/Spti_rpm >
         < /usr/ode/bin/Spti_swpackage >
         < /usr/ode/bin/Spti_isbuild.cmd >
         < /usr/ode/bin/Spti_pftwwiz.cmd >
        < /usr/ode/bin/Spti_buildpatch >
  < /usr/ode/bin/050_bODE_tools.jar >
         < /usr/ode/bin/build >
         < /usr/ode/bin/crlfcon >
         < /usr/ode/bin/currentsb >
         < /usr/ode/bin/gendep >
         < /usr/ode/bin/genpath >
         < /usr/ode/bin/mk >
         < /usr/ode/bin/mkbb >
         < /usr/ode/bin/mkdep >
         < /usr/ode/bin/mklinks >
         < /usr/ode/bin/mkpath >
         < /usr/ode/bin/mksb >
         < /usr/ode/bin/resb >
         < /usr/ode/bin/sbinfo >
         < /usr/ode/bin/sbmerge >
         < /usr/ode/bin/sbls >
         < /usr/ode/bin/workon >
         < /usr/ode/doc/ >
         < /usr/ode/doc/readme.txt >
         < /usr/ode/doc/fixes.txt >
         < /usr/ode/doc/known_bugs.txt >
         < /usr/ode/doc/ODELicense_ILA.txt >
         < /usr/ode/doc/ODELicense_3rdParty.txt >
         < /usr/ode/examples/ >
         < /usr/ode/examples/050_bODE_bbexample.zip >
         < /usr/ode/examples/050_bODE_rules.zip >
         < /usr/ode/examples/050_bODE_confs.zip >
                               ];
         parent = "ode";
         }
  InstallStatesInfo
         {
         }
}
End_bin_fileset :
file {
         fileType = 'dir';
         sourceDir = "/usr/";
         targetDir = "/usr/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'dir';
         sourceDir = "/usr/ode/";
         targetDir = "/usr/ode/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'dir';
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "build";
         targetFile = "build";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "crlfcon";
         targetFile = "crlfcon";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "currentsb";
         targetFile = "currentsb";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "gendep";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         targetFile = "gendep";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "genpath";
         targetFile = "genpath";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "lib0500.so";
         targetFile = "lib0500.so";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "mk";
         targetFile = "mk";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "mkbb";
         targetFile = "mkbb";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "mkdep";
         targetFile = "mkdep";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "mklinks";
         targetFile = "mklinks";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "mkpath";
         targetFile = "mkpath";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "mksb";
         targetFile = "mksb";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "resb";
         targetFile = "resb";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "sbinfo";
         targetFile = "sbinfo";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "sbmerge";
         targetFile = "sbmerge";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "sbls";
         targetFile = "sbls";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
      }
file {
         fileType = 'file';
         sourceFile = "Spti_mkinstall";
         targetFile = "Spti_mkinstall";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "Spti_pkgmk";
         targetFile = "Spti_pkgmk";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "Spti_rpm";
         targetFile = "Spti_rpm";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "Spti_swpackage";
         targetFile = "Spti_swpackage";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "Spti_isbuild.cmd";
         targetFile = "Spti_isbuild.cmd";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "Spti_pftwwiz.cmd";
         targetFile = "Spti_pftwwiz.cmd";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "Spti_buildpatch";
         targetFile = "Spti_buildpatch";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "050_bODE_tools.jar";
         targetFile = "050_bODE_tools.jar";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "workon";
         targetFile = "workon";
         sourceDir = "/usr/ode/bin/";
         targetDir = "/usr/ode/bin/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'dir';
         sourceDir = "/usr/ode/doc/";
         targetDir = "/usr/ode/doc/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "readme.txt";
         targetFile = "readme.txt";
         sourceDir = "/usr/ode/doc/";
         targetDir = "/usr/ode/doc/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "fixes.txt";
         targetFile = "fixes.txt";
         sourceDir = "/usr/ode/doc/";
         targetDir = "/usr/ode/doc/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "known_bugs.txt";
         targetFile = "known_bugs.txt";
         sourceDir = "/usr/ode/doc/";
         targetDir = "/usr/ode/doc/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "ODELicense_ILA.txt";
         targetFile = "ODELicense_ILA.txt";
         sourceDir = "/usr/ode/doc/";
         targetDir = "/usr/ode/doc/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "ODELicense_3rdParty.txt";
         targetFile = "ODELicense_3rdParty.txt";
         sourceDir = "/usr/ode/doc/";
         targetDir = "/usr/ode/doc/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'dir';
         sourceDir = "/usr/ode/examples/";
         targetDir = "/usr/ode/examples/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "050_bODE_bbexample.zip";
         targetFile = "050_bODE_bbexample.zip";
         sourceDir = "/usr/ode/examples/";
         targetDir = "/usr/ode/examples/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "050_bODE_rules.zip";
         targetFile = "050_bODE_rules.zip";
         sourceDir = "/usr/ode/examples/";
         targetDir = "/usr/ode/examples/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
file {
         fileType = 'file';
         sourceFile = "050_bODE_confs.zip";
         targetFile = "050_bODE_confs.zip";
         sourceDir = "/usr/ode/examples/";
         targetDir = "/usr/ode/examples/";
         permissions = "755";
         userId = "root";
         groupId = "sys";
     }
