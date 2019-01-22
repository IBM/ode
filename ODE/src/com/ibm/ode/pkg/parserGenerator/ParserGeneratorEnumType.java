/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 5/10/03 15:28:19
 * Date and Time File was extracted/checked out: 04/10/04 09:12:56
 *******************************************************************************/
package com.ibm.ode.pkg.parserGenerator;

import java.util.*;
import java.io.*;
import com.ibm.ode.lib.util.EnumBase ;

/**
 * Purpose :
 *  Provides a single Enumerated repository of valid tokens
 *  used in the Parser Generator and specification of the CMF.
 *
 *  It inherits the EnumBase class, which in turn provides
 *  functionalities of storing a two value (String and Integer)
 *  tuple, and methods to retrieve one value given the other.
 *
 *  This is a singleton class - with a bunch of calls to
 *  the addValuePair method of EnumBase class.
 */
public class ParserGeneratorEnumType extends EnumBase
{
  // All valid tokens that are used in the CMF have a corresponding
  // integer by which they are referred to within this package.

  /**
   * General Control Tokens - Range 0-9
   */
  public final static int NULL = 0; // Used to specify dummy/unknown/

  // initial token value
  public final static int ERROR = 1; //Error Symbol
  public final static int EXTENDSYMBOL = 2; //Extended symbol
  public final static int DUMMY = 3; //Dummy symbol
  public final static int START = 4; //Dummy symbol
  public final static int EOF = 9; //end of file character.


  /**
   * Tokens specifying valid Singular data types in the CMF - Range 10-19
   *    Special Symbol - Attr can be of diff type in diff. invocations
   */
  public final static int PGSPECIALTYPE = 10;
  public final static int STRING = 11; //String Symbol
  public final static int FILENAMEDATATYPE= 12; //Filename Symbol
  public final static int CONSTANT = 13; //Constant Symbol
  public final static int REQTYPE = 14; //ReqType Symbol

  /**
   * Tokens specifying valid List/Array data types in the CMF - Range 20-29
   */
  public final static int LISTOFPGSPECIALTYPE = 20; //List -Spc type Symb
  public final static int LISTOFSTRING = 21; //List -String Symbol
  public final static int LISTOFFILENAMEDATATYPE = 22; //List -Filename
  public final static int LISTOFCONSTANT = 23; //List -Constant Symbol
  public final static int LISTOFREQTYPE = 24; //List -ReqType Symbol

  //Token Range 30-49 available for future use

  /**
   * Token Range 50-69 for special characters and delimiting symbols
   */
  public final static int LEFTROUNDBRACE = 51; // '('
  public final static int RIGHTROUNDBRACE = 52; // ')'
  public final static int LEFTSQUAREBRACE = 53; // '['
  public final static int RIGHTSQUAREBRACE = 54; // ']'
  public final static int LEFTCURLYBRACE = 55; // '{'
  public final static int RIGHTCURLYBRACE = 56; // '}'
  public final static int COLON = 57; // ':'
  public final static int SCOLON = 58; //';'
  public final static int EQUAL = 59; //'='

  // Token Range 70-99 available for future use

  /**
   * List of Reserved words used in CMF - sorted alphabetically
   * for easier use
   * Token Range 100-999 for reserved words.
   */
  public final static int ARCHITECTUREINFO = 113;
  public final static int AUTOREQPROV = 114;
  public final static int BOOTREQMT = 116;
  public final static int CATEGORY = 119;
  public final static int CATEGORYTITLE = 120;
  public final static int CHANGELOG = 123;
  public final static int COMPIDTABLE = 126;
  public final static int CONFIGFILES = 130;
  public final static int CONSTANTLIST = 132;
  public final static int CONTENT = 133;
  public final static int COPYRIGHT = 134;
  public final static int COPYRIGHTFLAGS = 135;
  public final static int COPYRIGHTKEYS = 136;
  public final static int COPYRIGHTMAP = 137;
  public final static int DESCRIPTION = 141;
  public final static int DISTRIBUTION = 142;
  public final static int EMAIL = 148;
  public final static int ENTITYID = 149;
  public final static int ENTITYINFO = 150;
  public final static int ENTITYNAME = 151;
  public final static int ENTITYSUBSETINFO = 152;
  public final static int EXCLUDEARCH = 153;
  public final static int EXCLUSIVEARCH = 154;
  public final static int EXCLUDEOS = 155;
  public final static int EXCLUSIVEOS = 156;
  public final static int FILE = 158;
  public final static int FILEDIRECTIVES = 159;
  public final static int FILETYPE = 164;
  public final static int FIXLEVEL = 166;
  public final static int FLAGS    = 167;
  public final static int FULLENTITYNAME = 168;
  public final static int FULLSUBSETNAME = 170;
  public final static int GROUPID = 171;
  public final static int HIDDENSTATE = 172;
  public final static int HOTLINE = 173;
  public final static int IMAGENAME = 174;
  public final static int IMMCHILDENTITIES = 175;
  public final static int IMMCHILDFILES = 176;
  public final static int INPUTPATH = 178;
  public final static int INSLIST = 179;
  public final static int INSTALLDIR = 180;
  public final static int INSTALLENTITY = 181;
  public final static int INSTALLSPACE = 182;
  public final static int INSTALLSTATES = 183;
  public final static int INSTALLSTATESINFO = 184;
  public final static int INTERACTIVE = 185;
  public final static int ISKERNEL = 186;
  public final static int ISLOCATABLE = 187;
  public final static int LANGUAGE = 189;
  public final static int LINKINFO = 190;
  public final static int MACHINESERIES = 192;
  public final static int MACHINETYPE = 193;
  public final static int MAINTLEVEL = 194;
  public final static int MAJORDEVNUM = 195;
  public final static int MANPAGE = 196;
  public final static int MEDIAID = 199;
  public final static int MINORDEVNUM = 203;
  public final static int MODE = 204;
  public final static int ODMADDFILES = 209;
  public final static int ODMCLASSDEF = 210;
  public final static int OSNAME = 211;
  public final static int OSRELEASE = 212;
  public final static int OSVERSION = 213;
  public final static int PACKAGEFLAGS = 214;
  public final static int PACKAGEORDER = 217;
  public final static int PARENT = 223;
  public final static int PARTNUM = 224;
  public final static int PATHINFO = 225;
  public final static int PERMISSIONS = 226;
  public final static int README = 227;
  public final static int RELEASE = 228;
  public final static int REMOVABLESTATES = 229;
  public final static int REQUISITES = 230;
  public final static int REQUISITESINFO = 231;
  public final static int ROOTADDFILES = 236;
  public final static int ROOTCONTROLFILES = 237;
  public final static int SELECTION = 241;
  public final static int SERIALNUMBER = 242;
  public final static int SOURCEDIR = 248;
  public final static int SOURCEFILE = 249;
  public final static int SUBSETCONTENT = 251;
  public final static int SUBSETDESCRIPTION = 252;
  public final static int SUBSETNAME = 253;
  public final static int SUBTYPE = 255;
  public final static int SUPPORTINFO = 256;
  public final static int TARGETDIR = 258;
  public final static int TARGETFILE = 259;
  public final static int TOTALMEDIAUSED = 261;
  public final static int TYPE = 262;
  public final static int URL = 263;
  public final static int USERID = 266;
  public final static int USERPREREQ = 267;
  public final static int VENDORDESC = 275;
  public final static int VENDORINFO = 276;
  public final static int VENDORNAME = 277;
  public final static int VENDORTITLE = 278;
  public final static int VERSION = 279;
  public final static int VERSIONDATE = 280;
  public final static int DISTLIB = 281; //TARGETDIR FOR MVS
  public final static int ADEPACKAGEFLAGS = 282;
  public final static int ADEINVFLAGS = 283;
  public final static int MAXINST = 284;
  public final static int ARFLAGS = 285;

  public final static int PACKAGEINFO = 390;
  public final static int FULLPACKAGENAME = 400;
  public final static int PACKAGEDESC = 410;
  public final static int PACKAGENAME = 420;
  public final static int PACKAGECOPYRIGHT = 430 ;
  public final static int PACKAGESERIALNUMBER = 440 ;
  public final static int PACKAGEVENDORDESC = 450;
  public final static int PACKAGEVENDORNAME = 460;
  public final static int PACKAGEVENDORTITLE = 470;

  //MVS specific tokens added here
  public final static int VPLINFO = 287 ;
  public final static int MVSINFO = 288 ;

  public final static int COMP = 289 ;
  public final static int SHIPTYPE = 290 ;
  public final static int PARTINFO = 291 ;
  public final static int VPLSECURITY = 292 ;
  public final static int VPLPARTQUAL = 293 ;

  public final static int VPLAUTHCODE = 294 ;
  public final static int VPLFROMSYS = 295 ;
  public final static int CREATEVPL = 296 ;
  public final static int VPLACKN = 297 ;
  public final static int VPLAVAILDATE = 298 ;

  public final static int APPLID = 299 ;
  public final static int DISTLIBS = 300 ;
  public final static int SREL = 301 ;
  public final static int FESN = 302 ;
  public final static int SEP = 303 ;

  public final static int DELETE = 305 ;
  public final static int FUTURE = 306 ;
  public final static int PREVIOUS = 307 ;
  public final static int EXTRASMPEFILE = 308 ;
  public final static int VERSIONREQ = 309 ;

  public final static int SERVICEINFO = 310 ;
  public final static int RETAINCOMPONENT = 311 ;
  public final static int RETAINRELEASE = 312 ;
  public final static int RETAINCHANGETEAM = 313 ;
  //public final static int SPAVERSION = 313 ;
  public final static int CONTACTNODE = 315 ;
  public final static int CONTACTUSERID = 316 ;
  public final static int MEMOTOUSERS = 317 ;
  public final static int LABELTEXT1 = 318 ;
  public final static int LABELTEXT2 = 319 ;
  public final static int CIAPRODUCTIDENTIFICATION = 320 ;
  public final static int ALLAPARS = 321 ;
  public final static int AUTOIFREQ = 322 ;
  public final static int SMSSUPERCEDE = 323 ;
  public final static int PKGPTF = 324 ;
  public final static int CONTACTNAME = 325 ;
  public final static int CONTACTPHONE = 326 ;

  //public final static int SPANAME = 327 ;
  //public final static int SPACLASS = 328 ;

  public final static int HFSCOPYTYPE = 329 ;

  public final static int PACKAGEDATA = 330 ;
  public final static int SOURCEDATA = 331 ;

  public final static int REWORK = 332 ;

  public final static int CTLDEFINFILE = 333 ;

  public final static int EXTATTR      = 350;
  public final static int HFSALIAS     = 351;
  public final static int PDSALIAS     = 352;
  public final static int SETCODE      = 353;
  public final static int ENTRY        = 354;
  public final static int JCLINMODE    = 355;
  public final static int INCLUDE      = 356;
  public final static int LKEDTO       = 357;
  public final static int LKEDRC       = 358;
  public final static int PDSLKEDNAME  = 359;
  public final static int HFSLKEDNAME  = 360;
  public final static int JCLINLKEDPARMS = 361;
  public final static int SYSLIBS      = 362;
  public final static int ORDER        = 363;

  public final static int JCLINLIB     = 364;
  public final static int LKEDPARMS    = 365;
  public final static int DSNHLQ       = 366;
  public final static int LIBRARYDD    = 367;
  public final static int LKEDUNIT     = 368;
  public static final int SYSLIBS_LIBRARYDD = 369;
  public final static int CREATEJCLINLIB    = 370;
  public final static int FEATUREFMIDS = 371;
  public final static int LKEDCOND = 372;
  public final static int SIDEDECKAPPENDDD = 373;

  private static final String name_ = "ParserGeneratorEnumType";

  static
  {
    // Tokens for valid List/Array data types in the CMF and corr
    // string values
    addValuePair( ERROR , "error" );
    addValuePair( EXTENDSYMBOL , "extendsymbol" );
    addValuePair( DUMMY , "dummy" );
    addValuePair( START , "start" );
    addValuePair( EOF , "EOF" );
    addValuePair( PGSPECIALTYPE , "PGSpecialType" );
    addValuePair( STRING , "String" );
    addValuePair( FILENAMEDATATYPE , "FilenameDataType" );
    addValuePair( CONSTANT , "Constant" );
    addValuePair( REQTYPE , "ReqType" );
    addValuePair( LISTOFPGSPECIALTYPE , "ListOfPGSpecialType" );
    addValuePair( LISTOFSTRING , "ListOfString" );
    addValuePair( LISTOFFILENAMEDATATYPE , "ListOfFilenameDataType" );
    addValuePair( LISTOFCONSTANT , "ListOfConstant" );
    addValuePair( LISTOFREQTYPE , "ListOfReqType" );

    //Tokens for valid Stanza/Attrib names in the CMF and corr string values

    addValuePair( ADEINVFLAGS , "adeInvFlags" );
    addValuePair( ADEPACKAGEFLAGS , "adePackageFlags" );
    addValuePair( ARCHITECTUREINFO , "ArchitectureInfo" );
    addValuePair( ARFLAGS , "arFlags" );
    addValuePair( AUTOREQPROV, "autoreqprov" );
    addValuePair( BOOTREQMT , "bootReqmt" );
    addValuePair( CATEGORY , "category" );
    addValuePair( CATEGORYTITLE , "categoryTitle" );
    addValuePair( CHANGELOG, "changeLog" );
    addValuePair( COMPIDTABLE , "compidTable" );
    addValuePair( CONFIGFILES , "configFiles" );
    addValuePair( CONSTANTLIST , "constantList" );
    addValuePair( CONTENT , "content" );
    addValuePair( COPYRIGHT , "copyright" );
    addValuePair( COPYRIGHTFLAGS , "copyrightFlags" );
    addValuePair( COPYRIGHTKEYS , "copyrightKeys" );
    addValuePair( COPYRIGHTMAP , "copyrightMap" );
    addValuePair( DESCRIPTION , "description" );
    addValuePair( DISTLIB , "distLib" );
    addValuePair( DISTRIBUTION, "distribution" );
    addValuePair( EMAIL , "email" );
    addValuePair( ENTITYID , "entityId" );
    addValuePair( ENTITYINFO , "EntityInfo" );
    addValuePair( ENTITYNAME , "entityName" );
    addValuePair( ENTITYSUBSETINFO , "EntitySubsetInfo" );
    addValuePair( EXCLUDEARCH, "excludeArch" );
    addValuePair( EXCLUDEOS, "excludeOS" );
    addValuePair( EXCLUSIVEARCH, "exclusiveArch" );
    addValuePair( EXCLUSIVEOS, "exclusiveOS" );
    addValuePair( FEATUREFMIDS , "featureFmids" );
    addValuePair( FILE , "file" );
    addValuePair( FILEDIRECTIVES, "fileDirectives" );
    addValuePair( FILETYPE , "fileType" );
    addValuePair( FIXLEVEL , "fixLevel" );
    addValuePair( FLAGS, "flags" );
    addValuePair( FULLENTITYNAME , "fullEntityName" );
    addValuePair( FULLSUBSETNAME , "fullSubsetName" );
    addValuePair( GROUPID , "groupId" );
    addValuePair( HIDDENSTATE , "hiddenState" );
    addValuePair( HOTLINE , "hotline" );
    addValuePair( IMAGENAME , "imageName" );
    addValuePair( IMMCHILDENTITIES , "immChildEntities" );
    addValuePair( IMMCHILDFILES , "immChildFiles" );
    addValuePair( INPUTPATH , "inputPath" );
    addValuePair( INSLIST , "insList" );
    addValuePair( INSTALLDIR , "installDir" );
    addValuePair( INSTALLENTITY , "InstallEntity" );
    addValuePair( INSTALLSPACE , "installSpace" );
    addValuePair( INSTALLSTATES , "installStates" );
    addValuePair( INSTALLSTATESINFO , "InstallStatesInfo" );
    addValuePair( INTERACTIVE , "interactive" );
    addValuePair( ISKERNEL , "isKernel" );
    addValuePair( ISLOCATABLE , "isLocatable" );
    addValuePair( LANGUAGE , "language" );
    addValuePair( LINKINFO , "LinkInfo" );
    addValuePair( MACHINESERIES , "machineSeries" );
    addValuePair( MACHINETYPE , "machineType" );
    addValuePair( MAINTLEVEL , "maintLevel" );
    addValuePair( MAJORDEVNUM , "majorDevNum" );
    addValuePair( MANPAGE , "manpage" );
    addValuePair( MAXINST , "maxInst" );
    addValuePair( MEDIAID , "mediaId" );
    addValuePair( MINORDEVNUM , "minorDevNum" );
    addValuePair( MODE , "mode" );
    addValuePair( ODMADDFILES , "odmAddFiles" );
    addValuePair( ODMCLASSDEF , "odmClassDef" );
    addValuePair( OSNAME , "osName" );
    addValuePair( OSRELEASE , "osRelease" );
    addValuePair( OSVERSION , "osVersion" );
    addValuePair( PACKAGEFLAGS , "packageFlags" );
    addValuePair( PACKAGEORDER , "packageOrder" );
    addValuePair( PARENT , "parent" );
    addValuePair( PARTNUM , "partNum" );
    addValuePair( PATHINFO , "PathInfo" );
    addValuePair( PERMISSIONS , "permissions" );
    addValuePair( README , "readme" );
    addValuePair( RELEASE , "release" );
    addValuePair( REMOVABLESTATES , "removableStates" );
    addValuePair( REQUISITES , "requisites" );
    addValuePair( REQUISITESINFO , "RequisitesInfo" );
    addValuePair( ROOTADDFILES , "rootAddFiles" );
    addValuePair( ROOTCONTROLFILES , "rootControlFiles" );
    addValuePair( SELECTION , "selection" );
    addValuePair( SERIALNUMBER , "serialNumber" );
    addValuePair( SOURCEDIR , "sourceDir" );
    addValuePair( SOURCEFILE , "sourceFile" );
    addValuePair( SUBSETCONTENT , "subsetContent" );
    addValuePair( SUBSETDESCRIPTION , "subsetDescription" );
    addValuePair( SUBSETNAME , "subsetName" );
    addValuePair( SUBTYPE , "subType" );
    addValuePair( SUPPORTINFO , "SupportInfo" );
    addValuePair( TARGETDIR , "targetDir" );
    addValuePair( TARGETFILE , "targetFile" );
    addValuePair( TOTALMEDIAUSED , "totalMediaUsed" );
    addValuePair( TYPE , "type" );
    addValuePair( URL, "url" );
    addValuePair( USERID , "userId" );
    addValuePair( USERPREREQ , "userPrereq" );
    addValuePair( VENDORDESC , "vendorDesc" );
    addValuePair( VENDORINFO , "VendorInfo" );
    addValuePair( VENDORNAME , "vendorName" );
    addValuePair( VENDORTITLE , "vendorTitle" );
    addValuePair( VERSION , "version" );
    addValuePair( VERSIONDATE , "versionDate" );

    addValuePair( PACKAGEINFO , "PackageInfo" );
    addValuePair( PACKAGENAME , "pkgName" );
    addValuePair( FULLPACKAGENAME , "fullPkgName" );
    addValuePair( PACKAGEDESC, "pkgDesc" );
    addValuePair( PACKAGECOPYRIGHT, "pkgCopyright" );
    addValuePair( PACKAGESERIALNUMBER, "pkgSerialNumber" );
    addValuePair( PACKAGEVENDORDESC , "pkgVendorDesc" );
    addValuePair( PACKAGEVENDORNAME , "pkgVendorName" );
    addValuePair( PACKAGEVENDORTITLE , "pkgVendorTitle" );

    //other tokens of mediasetinfo stanza were defined in erstwhile
    //mediainfo stanza and hence need not be redefined

    //MVS specific tokens added here
    addValuePair( VPLINFO, "VplInfo" );
    addValuePair( MVSINFO, "MvsInfo" );
    addValuePair( COMP, "comp" );
    addValuePair( SHIPTYPE, "shipType" );
    addValuePair( PARTINFO, "partInfo" );
    addValuePair( VPLSECURITY, "vplSecurity" );
    addValuePair( VPLPARTQUAL, "vplPartqual" );

    addValuePair( VPLAUTHCODE, "vplAuthCode" );
    addValuePair( VPLFROMSYS, "vplFromSys" );
    addValuePair( CREATEVPL, "createVpl" );
    addValuePair( VPLACKN, "vplAckn" );
    addValuePair( VPLAVAILDATE, "vplAvailDate" );

    addValuePair( APPLID, "applid" );
    addValuePair( DISTLIBS, "distlibs" );
    addValuePair( SREL, "srel" );
    addValuePair( DELETE, "delete" );
    addValuePair( FUTURE, "future" );
    addValuePair( PREVIOUS, "previous" );
    addValuePair( CTLDEFINFILE, "ctldefinFile" );
    addValuePair( EXTRASMPEFILE, "extraSmpeFile" );
    addValuePair( VERSIONREQ, "versionReq" );
    addValuePair( JCLINLIB, "jclinLib" );
    addValuePair( DSNHLQ, "dsnHlq" );
    addValuePair( LKEDUNIT, "lkedUnit" );

    addValuePair( FESN, "fesn" );
    addValuePair( SEP, "sep" );
    //MVS specific tokens end here

    addValuePair( SERVICEINFO,  "ServiceInfo" );
    addValuePair( RETAINCOMPONENT,  "retainComponent" );
    addValuePair( RETAINRELEASE,  "retainRelease" );
    addValuePair( RETAINCHANGETEAM,  "retainChangeTeam" );
    //addValuePair( SPAVERSION,  "spaVersion" );
    addValuePair( CONTACTNODE,  "contactNode" );
    addValuePair( CONTACTUSERID,  "contactUserId" );
    addValuePair( MEMOTOUSERS,  "memoToUsers" );
    addValuePair( LABELTEXT1,  "labelText1" );
    addValuePair( LABELTEXT2,  "labelText2" );
    addValuePair( CIAPRODUCTIDENTIFICATION,"ciaProductIdentification" );
    addValuePair( ALLAPARS,  "allApars" );
    addValuePair( AUTOIFREQ,  "autoIfreq" );
    addValuePair( SMSSUPERCEDE,  "SMSSupercede" );
    addValuePair( PKGPTF,  "pkgPtf" );
    addValuePair( CONTACTNAME, "contactName" );
    addValuePair( CONTACTPHONE, "contactPhone" );

    //addValuePair( SPANAME, "spaName" );
    //addValuePair( SPACLASS, "spaClass" );

    addValuePair( HFSCOPYTYPE , "hfsCopyType" );
    addValuePair( SOURCEDATA , "SourceData" );
    addValuePair( PACKAGEDATA , "PackageData" );
    addValuePair( REWORK, "rework" );

    addValuePair( EXTATTR, "extAttr" );
    addValuePair( HFSALIAS, "hfsAlias" );
    addValuePair( PDSALIAS, "pdsAlias" );
    addValuePair( SETCODE, "setCode" );
    addValuePair( ENTRY, "entry" );
    addValuePair( JCLINMODE, "jclinMode" );
    addValuePair( INCLUDE, "include" );
    addValuePair( LKEDTO, "lkedTo" );
    addValuePair( LKEDRC, "lkedRc" );
    addValuePair( HFSLKEDNAME, "hfsLkedName" );
    addValuePair( PDSLKEDNAME, "pdsLkedName" );
    addValuePair( JCLINLKEDPARMS, "jclinLkedParms" );
    addValuePair( SYSLIBS, "sysLibs" );
    addValuePair( ORDER, "order" );
    addValuePair( LKEDPARMS, "lkedParms" );
    addValuePair( LIBRARYDD, "libraryDD" );
    addValuePair(SYSLIBS_LIBRARYDD, "sysLibsLibraryDD");
    addValuePair( CREATEJCLINLIB, "createJclinLib" );
    addValuePair( LKEDCOND, "lkedCond" );
    addValuePair( SIDEDECKAPPENDDD, "sideDeckAppendDD" );

    //add here whatever symbol has to be interpreted as null -
    //this string specified - without quotes - as value in any
    //attribute's name-value pair - will result in that attribute
    //being interpreted as not being specified at all.
    //i.e. - a developer can specify this as a value string without
    //quotes to get the same effect as if omitting this name-value
    //pair altogether.
    addValuePair( NULL, "NULL" );
  }

  /**
   * Constructs a newly allocated instance of this class, which
   *    makes several calls to the addValuePair method, to map
   *    valid CMF (strings) to static integers defined in this
   *    using which, other classes in this package would recognize
   *    these strings as valid.
   */
  public ParserGeneratorEnumType()
  {
  }

  /**
   * Constructs a newly allocated instance of this class, which
   *    makes several calls to the addValuePair method, to map
   *    valid CMF (strings) to static integers defined in this
   *    using which, other classes in this package would recognize
   *    these strings as valid.
   */
  public ParserGeneratorEnumType(int value)
  {
    this();
    setInt(value, name_);
  }

  /**
   * Constructs a newly allocated instance of this class, which
   *    makes several calls to the addValuePair method, to map
   *    valid CMF (strings) to static integers defined in this
   *    using which, other classes in this package would recognize
   *    these strings as valid.
   */
  public ParserGeneratorEnumType(String value)
  {
    this();
    setString(value, name_);
  }

  /**
   *copy constructor
   * <pre>
   * usage:
   * ParserGeneratorEnumType   ds = new ParserGeneratorEnumType( "action" );
   * ParserGeneratorEnumType   ds2 = new ParserGeneratorEnumType( ds1 );
   * </pre>
  **/
  public ParserGeneratorEnumType( ParserGeneratorEnumType orig )
  {
    this();
    copy( orig );
  }

  /**
   * used by the copy constuctor and clone()
  **/
  public synchronized void copy( ParserGeneratorEnumType orig )
  {
    // set actual int value
    setInt ( orig.getInt(), name_ );
  }

  /**
   * returns a shallow copy of myself
   * <pre>
   * usage:
   * ParserGeneratorEnumType  ds = new ParserGeneratorEnumType( "action" );
   * ParserGeneratorEnumType  ds2 = (ParserGeneratorEnumType) ds.clone();
   * ParserGeneratorEnumType  ds2 = (ParserGeneratorEnumType) ds.clone();
   * </pre>
  **/
  public Object clone()
  {
    return new ParserGeneratorEnumType( this );
  }

  /**
   * Wrapper method for addValuePair of super class.
   *    Uses this class name to hash all CMF related values to be
   *    Enumerated, using the mechanism provided by the super class.
   *
   * @param intValue Integer value to be enumerated
   * @param strValue String associated with this integer value in the Enumeration
   * @see COM.ibm.sdwb.bps.util.EnumBase#addValuePair
   **/
  public static void addValuePair(int intValue, String strValue)
  {
    addValuePair(intValue, strValue, name_ );
  }

  // The following methods are wrappers to EnumBase and are exactly the
  // same for all subclasses of EnumBase.

  /**
   * Calls the EnumBase method.
   *
   * @return the enumeration (in *Integer*) of all legal integer values
   */
  public static Enumeration getAllInts()
  {
    return getAllInts(name_);
  }

  /**
   * Get all the string values as an Enumeration of String.
   * Calls the EnumBase method.
   *
   * @return the enumeration (in String) of all legal String values
   */
  public static Enumeration getAllStrings()
  {
    return getAllStrings(name_);
  }

  /**
   * Given a string, return the mapped integer value. See exception comment
   * above.
   *
   * Calls the EnumBase method.
   *
   * @param string the enum String value
   * @return the mapped enum integer value
   * @exception NoSuchElementException if given an illegal enum String value
   */
  public static int getInt(String string)
    throws NoSuchElementException
  {
    return getInt(string, name_);
  }

  /**
   * Retrieve the String value based on the subclassName.  The subclassName
   * is needed to retrieve the appropriate intToString Hashtable.
   *
   * Calls the EnumBase method.
   *
   * @return the current String value
   */
  public String getString()
    throws NoSuchElementException
  {
    return getString(name_);
  }

  /**
   * Given an integer value, return the mapped string.
   *
   * We explicitly throw a NoSuchElementException (doesn't have to be
   * explicitly caught) even though Hashtable.get() method throws
   * NullPointerException. We think our exception makes more sense and since
   * it's in our control, it allows us to use a different implementation
   * for the mapping tables.
   *
   * Calls the EnumBase method.
   *
   * @return the mapped String value
   * @exception NoSuchElementException if given an illegal enum integer value
   */
  public static String getString(int value)
    throws NoSuchElementException
  {
    return getString(value, name_);
  }

  /**
   * Set the value with an integer
   *
   * Calls the EnumBase method.
   *
   * @param value the new enum integer value
   */
  public void setInt(int value)
  {
    setInt(value, name_);
  }

  /**
   * Set the string value
   *
   * Calls the EnumBase method.
   *
   * @param string the new String value
   */
  public void setString(String string)
  {
    setString(string, name_);
  }

  /**
   * Print all the value pairs. Overrides Object.toString()
   *
   * Calls the EnumBase method.
   *
   * @return all the int-String value pairs
   */
  public String toString()
  {
    return toString(name_);
  }

  /**
   * Unit Test
   *
   */
  public static void main(String[] args)
  {
  }
}
