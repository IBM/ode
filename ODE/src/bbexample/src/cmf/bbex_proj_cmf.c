/*
 * This file is used to generate the Common Metadata File or CMF
 * It may be depricated in future versions of ODEi
*/

/* utility macros */
#define QUOTE(s2) #s2
#define STRING(s) QUOTE(s)
#define APPEND(value1, value2)  value1 ## value2
#define MKPATH(path, filename)  APPEND(path, filename)
#define MKSEP(pathsep) pathsep

#ifdef _x86_nt_
    #define PATHSEP MKSEP(\) 
    #define SHAREDLIB1 libexa.dll
    #define GROUPID "sys"
#else
    #define PATHSEP MKSEP(/)
#endif

#define PARENT_DIR     odehello
#ifdef _x86_sco_
    #define INSTALL_DIR    /opt/
    #define ODEHELLO_DIR   /opt/odehello/
    #define ODEHELLODIR   "/opt/odehello/"
    #define BINPATH        /opt/odehello/bin
    #define HTMLPATH       /opt/odehello/html
    #define LINKPATH       /opt/odehello/links
#else
    #define INSTALL_DIR    MKPATH(BASE_NAME,PATHSEP)
    #define ODEHELLO_DIR   MKPATH(INSTALL_DIR,MKPATH(PARENT_DIR,PATHSEP))
    #define ODEHELLODIR    STRING(ODEHELLO_DIR)
    #define BINPATH   MKPATH(ODEHELLO_DIR,MKPATH(bin,PATHSEP))
    #define HTMLPATH  MKPATH(ODEHELLO_DIR,MKPATH(html,PATHSEP))
    #define LINKPATH  MKPATH(ODEHELLO_DIR,MKPATH(links,PATHSEP))
#endif
#define INSTALLDIR   STRING(INSTALL_DIR)

#ifdef _linux_
    #define ENTITY1 "bin"
    #define ENTNAME1 bin
    #define ENTITY2 ""
    #define ENTITY3 ""
    #define OS_NAME        [ "Linux" ]
    #define OS_RELEASE     [ "6" ]
    #define OS_VERSION     [ "1" ]
    #define CATEGORY       'Application/Networking'
    #define CATEGORY2      'Applicatoin/Networking'
    #define REQUISITES
    #define PREFIX STRING(BASE_NAME)
#else
    #define OS_NAME        [ "mips_irix" "sparc_solaris" "x86_solaris" \
                             "rios_aix" "ia64_aix" "hp9000_ux" "x86_nt" ]
    #define OS_RELEASE     [ "6" "2" "2" "4" "5" "10" "4" ]
    #define OS_VERSION     [ "5" "3" "6" "1.5.0" "0" "0.0" "0" ]
    #define CATEGORY       'application'
    #define CATEGORY2     
    #define PREFIX STRING(MKPATH(INSTALL_DIR,PARENT_DIR))
#endif
#define USERID    "root"
#define BINSRC    STRING(BINPATH)
#define HTMLSRC   STRING(HTMLPATH)
#define LINKSRC   STRING(LINKPATH)
#ifdef _mvs390_oe_
    #define BINTGT   "abpsbin"
    #define HTMLTGT  "abpshtml"
    #define PARENT   NULL
    #define SHAREDLIB1 libexa.a
    #define ENTITY1 "hbps112"
    #define ENTITY2 "jbps112"
    #define ENTITY3 ""
    #define ENTNAME1 hbps112
    #define ENTNAME2 jbps112
    #define GROUPID "sys"
    #define COPYRIGHT < ./copyright.txt >
    #define REQUISITES
    #define REQUISITES1 [ ("S" "hbps111") ]
    #define REQUISITES2
#else
    #define BINTGT   BINSRC
    #define HTMLTGT  HTMLSRC
    #define LINKTGT  LINKSRC
    #define PARENT   STRING(PARENT_DIR)
    #define COPYRIGHT "Copyright IBM, 2000"
#endif

#ifdef _hp9000_ux_
    #define BOOTRQMT 'n'
    #define MODE "-m 755"
    #define ENTITY1 "odehellobin"
    #define ENTITY2 "odehellodoc"
    #define ENTITY3 ""
    #define ENTNAME1 odehellobin
    #define ENTNAME2 odehellodoc
    #define SHAREDLIB1 libexa.sl
    #define PKGCONSTANT1 ""
    #define PKGCONSTANT2 ""
    #define GROUPID "sys"
    #define FTYPE 'F'
    #define DTYPE 'D'
    #define LTYPE 'S'
    #define REQUISITES
    #define REQUISITES1
    #define REQUISITES2
    #define CONFIG_FILES [ ("postinstall" "post") \
                         ("preremove" "preremove") ]
    #define MACHINE_TYPE
#else
    #define MODE    "755"
    #define BOOTRQMT 'No' 
    #define CONFIG_FILES
    #define MACHINE_TYPE "R" 
    #ifdef _aix_
        #define PARENT STRING(PARENT_DIR)
        #define ENTITY1 "odehello.bin"
        #define ENTITY2 "odehello.doc"
        #define ENTITY3 "odehello.links"
        #define ENTNAME1 odehello.bin
        #define ENTNAME2 odehello.doc
        #define ENTNAME3 odehello.links
        #define PKGCONSTANT1 "" 
        #define PKGCONSTANT2 "" 
        #define GROUPID "sys"
        #define SHAREDLIB1 libexa.a
        #define FTYPE 'F'
        #define DTYPE 'D'
        #define LTYPE 'S'
        #define REQUISITES  [ ("P" "pkgtools.core 1.0.1.0") ("S" "1" "1") ]
        #define REQUISITES1 [ ("P" "pkgtools.core 1.0.1.0") ]
        #define REQUISITES2 [ ("P" "pkgtools.core 1.0.1.0") ]
        #define REQUISITES3 [ ("P" "pkgtools.core 1.0.1.0") ] 
    #elif !defined(_x86_nt_) && !defined(_mvs390_oe_)
        #define PARENT STRING(PARENT_DIR)
        #define PKGCONSTANT1 "!BIN=/usr/bin"
        #define PKGCONSTANT2 "!PROJDIR=/usr/projects"
        #define GROUPID "other"
        #define SHAREDLIB1 libexa.so
        #define FTYPE 'f'
        #define DTYPE 'd'
        #define LTYPE 's'
        #define REQUISITES1
        #define REQUISITES2
        #define REQUISITES3
    #endif
#endif
#define SHAREDLIB  STRING(SHAREDLIB1)

#if defined(_solaris_) || defined(_mips_irix_) || defined(_x86_sco_)
    #define REQUISITES [ ("P" "nsu" "Network Support Utilities") ("S" "1" "1") ]
    #define ENTITY1 "odehello.bin"
    #define ENTITY2 "odehello.doc"
    #define ENTITY3 "odehello.links"
    #define ENTNAME1 odehello.bin
    #define ENTNAME2 odehello.doc
    #define ENTNAME3 odehello.links
    #define SHAREDLIB1 libexa.so
#endif

#ifndef _mvs390_oe_ /* mvs only has two install entities and no product def. */
InstallEntity /* Representing the Product odehello */
{

Common :
    EntityInfo
    {
        entityName      =  "odehello" ;
        fullEntityName  =  ["odehello"] ;
        description     =  "BPS Test Package ";
        imageName       =  "odehello";
        version         =  '3'  ;
        release         =  '0' ;
        maintLevel      =  '0' ;
        fixLevel        =  '0' ;
        copyright       =  COPYRIGHT ;
        category        =  CATEGORY ;
        copyrightKeys   = ["%%_IBMa" "%%_MITb"] ;
        copyrightMap    = < /usr/pkgtest/copyright.map >;
        language        =  'en_us' ;
        content         =  'USR' ;
    }

    LinkInfo
    {
        immChildEntities =  [ ENTITY1 ENTITY2 ENTITY3 ]  ;
        parent           = NULL ;
    }

    VendorInfo
    {
        vendorName  =  "IBM";
        vendorDesc  =  "StockNumber::12345";
    }

    ArchitectureInfo
    {
        machineType     = MACHINE_TYPE ;
#ifndef _hp9000_ux_
        osName          = OS_NAME ;
        osRelease       = OS_RELEASE ;
        osVersion       = OS_VERSION ;
        exclusiveOS     = [ "Linux" ];
        excludeOS       = [ "NextStep" "BSD_OS" "FreeMiNT" ];
        excludeArch     = [ "mipsel" "armv4b" "armv4l" ];
#endif
    }

    InstallStatesInfo
    {
        bootReqmt       = BOOTRQMT  ;
        packageFlags    =  "-L" ;
        maxInst         =  "1";
        adeInvFlags     =  "-Y" ;
        mediaId         =  '9709261004';
        installStates   =  "3";
        removableStates =  "3";
        constantList    =  [ PKGCONSTANT1  PKGCONSTANT2 ];
        installDir      =  PREFIX;
        installSpace    =  "data 500  1";
    }

    RequisitesInfo
    {
        requisites    = REQUISITES ;
    }

    ServiceInfo
    { 
        retainComponent     = "7340VSE01";   
        retainRelease       = "G01" ;   
        contactName         = "Sandi";   
        contactPhone        = "111-111-1111";   
        contactNode         = "spa3";   
        contactUserId       = "spatest";   
        memoToUsers         = "This is a sample memo" ;  
        labelText1          = "This is labelText1"; 
        labelText2          = "This is labelText2";  
        ciaProductIdentification   =  "pid";  
    }
}

#endif /* end ifndef _mvs390_oe_ */

InstallEntity /* Representing the Fileset odehello.bin */
{

Common :
    EntityInfo 
    {
        entityName      =  ENTITY1;
        entityId        =  ENTITY1;      
        versionDate     =  "7302";
        version         =  '3' ;
        release         =  '0' ;
        maintLevel      =  '0';
        fixLevel        =  '0';
        copyright       =  COPYRIGHT ;
        category        =  CATEGORY2 ;
        language        =  'en_us';
        content         =  'USR' ;
        insList         =  [ <odehello.bin.il> ];
#if defined(_linux_)
        fullEntityName  =  [ "The executables in ODEHELLO" ];
#endif
        description     =  "The executables in ODEHELLO";
        
    }

    LinkInfo
    {
        immChildFiles   =  [ %ENTNAME1% ];
        parent          =  PARENT;
    }

    InstallStatesInfo
    {
        bootReqmt       = BOOTRQMT  ;
    }

    RequisitesInfo
    {
        requisites    = REQUISITES1 ; 
    }

    ServiceInfo
    {
        retainComponent     = "7340VSE01";
        retainRelease       = "G01" ;
        contactName         = "Sandi";
        contactPhone        = "111-111-1111";
        contactNode         = "spa3";
        contactUserId       = "spatest";
        memoToUsers         = "This is a sample memo" ;
        labelText1          = "This is labelText1";
        labelText2          = "This is labelText2";
        ciaProductIdentification   =  "pid";
    }

    MvsInfo
    {
        applid          = 'g1spa';
        distlibs        = [ "abpsbin,2,1,5,fb,80,6160" ];
        srel            = 'z038';
        type            = [ 'ipp' 'vpl' 'ptf' ];
        delete          = "hbps111";
        fesn            = '1234567';
    }

    VplInfo
    {
        vplAuthCode     = 'myau';
        vplFromSys      = 'ralvm12.smayo';
        createVpl       = 'Y';
        vplAckn         = 'error';
        vplAvailDate    = '1997-10-30';
    }
}

#if !defined(_linux_)
InstallEntity /* Representing the doc fileset odehello.doc */
{

Common :
    EntityInfo
    {
        entityName      =  ENTITY2;
        entityId        =  ENTITY1;          
        versionDate     =  "7302";
        version         =  '3' ;
        release         =  '0' ;
        maintLevel      =  '0';
        fixLevel        =  '0';
        copyright       =  COPYRIGHT ;
        insList         =  [ < odehello.doc.il > ];
        content         =  'USR' ;
        description     =  "ODE Development environment users guide.";
    }

    LinkInfo
    {
        immChildFiles   =   [ %ENTNAME2% ];
        parent          =  PARENT;
    }

    InstallStatesInfo
    {
        bootReqmt       = BOOTRQMT  ;
    }

    RequisitesInfo
    {
        requisites    = REQUISITES2 ; 
    }

    MvsInfo 
    {
        applid          = 'glspa';
        distlibs        = [ "abpshtml,2,1,5,vb,256,0" ];
        srel            = 'z038';
        type            = [ 'ipp' 'ptf'  ];
        fesn            = '1234567';
    }

    PathInfo 
    {
        configFiles  = CONFIG_FILES ;
    }
  
    ServiceInfo
    {
        retainComponent            = "7340VSE01";
        retainRelease              = "G01" ;
        contactName                = "Sandi";
        contactPhone               = "111-111-1111";
        contactNode                = "spa3";
        contactUserId              = "spatest";
        memoToUsers                = "This is a sample memo" ;
        labelText1                 = "This is labelText1";
        labelText2                 = "This is labelText2";
        ciaProductIdentification   =  "pid";
    }
}
#endif /* end !defined(_linux_) */ 

#if !defined(_linux_)
    #if !defined(_hp9000_ux_) && !defined(_mvs390_oe_)
        InstallEntity /* Representing the doc fileset odehello.links */
        {

        Common :
            EntityInfo 
            {
                entityName      = ENTITY3;
                entityId        = ENTITY1;          
                description     =  "ODEHELLO Links documentation";
                versionDate     =  "7302";
                version         =  '1' ;
                release         =  '1' ;
                maintLevel      =  '0';
                fixLevel        =  '0';
                copyright       =  COPYRIGHT ;
                insList         =  [ < odehello.links.il > ];
                content         = 'USR' ;
            }
 
            LinkInfo   
            {
                immChildFiles   =   [ %ENTNAME3% ];
                parent          =  PARENT;
            }
 
            InstallStatesInfo
            {
                bootReqmt       = BOOTRQMT  ;
            }
	       
            RequisitesInfo
            {
                requisites    = REQUISITES3 ; 
            }
                   
            MvsInfo   
            {
                applid          = 'glspa';
                distlibs        = [ "abpshtml,2,1,5,vb,256,0" ];
                srel            = 'z038';
                type            = [ 'ipp' 'ptf'  ];
                fesn            = '1234567';
            }

            ServiceInfo
            {
                retainComponent            = "7340VSE01";
                retainRelease              = "G01" ;
                contactName                = "Sandi";
                contactPhone               = "111-111-1111";
                contactNode                = "spa3";
                contactUserId              = "spatest";
                memoToUsers                = "This is a sample memo" ;
                labelText1                 = "This is labelText1";
                labelText2                 = "This is labelText2";
                ciaProductIdentification   =  "pid";
            }
        }
    #endif /* !defined(_hp9000_ux_) && !defined(_mvs390_oe_) */ 
#endif /* !defined(_linux_) */ 
