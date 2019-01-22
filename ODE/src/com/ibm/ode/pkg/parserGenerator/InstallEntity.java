package com.ibm.ode.pkg.parserGenerator;

import java.util.*;
import com.ibm.ode.lib.io.Interface;

/**
 * InstallEntity class is one among the group of classes which act as
 * a repository where parsed information from the CMF file is stored.
 * Generator generates a control file by extracting necessary information
 * stored in this repository.
 *
 * Single occurance of stanza are implemented in this class.
 *
 * Stanza which occurs multiple times has been represented as objects and are
 * put in a seperate class.
 *
 * @version     1.22 98/01/22
 * @author      Krisv
**/

public class InstallEntity
             implements FirstLevelStanza, SecondLevelStanza
{

  /**
   * to indicate whether this entity has been accessed
   **/

  private boolean markReferenced_;

  //following attributes are under EntityInfo stanza
  private String entityName_;
  private ArrayList fullEntityName_;
  private String entityId_;
  private PgSpecialType description_;
  private String imageName_;
  private String version_;
  private String release_;
  private String maintLevel_;
  private String fixLevel_;
  private String serialNumber_;
  private String distribution_;
  private String versionDate_;
  private String hiddenState_;
  private String category_;
  private String categoryTitle_;
  private PgSpecialType copyright_;
  private ArrayList copyrightKeys_;
  private String copyrightMap_;
  private String copyrightFlags_;
  private String language_;
  private String content_;
  private ArrayList insList_;
  private PgSpecialType changeLog_;

  //following attributes are under LinkInfo stanza
  private ArrayList immChildEntities_;
  private ArrayList immChildFiles_;
  /**
   *if there is no parent then parent_ attribute
   *should be specified as NULL.
  **/
  private String parent_;

  //following attributes are under VendorInfo stanza
  private String vendorName_;
  private String vendorTitle_;
  private PgSpecialType vendorDesc_;

  //following attributes are under ArchitectureInfo stanza
  private ArrayList excludeArch_;
  private ArrayList exclusiveArch_;
  private ArrayList excludeOS_;
  private ArrayList exclusiveOS_;
  private String machineSeries_;
  private String machineType_;
  private ArrayList osName_;
  private ArrayList osRelease_;
  private ArrayList osVersion_;

  //following attributes are under SupportInfo stanza
  private PgSpecialType readme_;
  private PgSpecialType manpage_;
  private ArrayList hotline_;
  private ArrayList email_;
  private ArrayList packageOrder_;
  private ArrayList Url_;

  //following attributes are under InstallStatesInfo stanza
  private String interactive_;
  private String mediaId_;
  private String totalMediaUsed_;
  private String installStates_;
  private String removableStates_;
  private ArrayList constantList_;
  private String isLocatable_;
  private String isKernel_;
  private PgSpecialType installDir_;
  private PgSpecialType installSpace_;
  private String bootReqmt_;
  private String adePackageFlags_;
  private String adeInvFlags_;
  private String arFlags_;
  private String maxInst_;
  private String mode_;
  private String compidTable_;

  //following attributes are under PathInfo stanza
  private ArrayList inputPath_;
  private ArrayList configFiles_;
  private ArrayList odmAddFiles_;
  private String odmClassDef_;
  private ArrayList rootControlFiles_;
  private ArrayList rootAddFiles_;

  //following attributes are under RequisitesInfo stanza
  private ArrayList requisites_;
  private String autoreqprov_;
  private String userPrereq_;

  //following attributes are under PackageInfo stanza
  private String pkgName_;
  private String fullPkgName_;
  private PgSpecialType pkgDesc_;
  private PgSpecialType pkgCopyright_;
  private String pkgSerialNumber_;
  private String pkgVendorName_;
  private String pkgVendorTitle_;
  private PgSpecialType pkgVendorDesc_;

  //following attributes belong to the multiple stanza
  //category.  Seperate classes are drawn and here they are
  //represented as objects of those classes.

  // EntitySubsetInfo stanza
  private ArrayList entitySubsetInfo_;


  //MVS support tags added

  // MvsInfo stanza
  private String applid_;
  private String rework_;
  private ArrayList distlibs_;
  private String srel_;
  private ArrayList type_;

  private String delete_;
  private String future_;
  private String previous_;
  private String ctldefinFile_;
  private String extraSmpeFile_;
  private String versionReq_;
  private String jclinLib_;
  private String dsnHlq_;
  private String lkedUnit_;

  private String fesn_;
  private ArrayList sep_;

  private ArrayList featureFmids_;

  // VplInfo stanza
  private String vplAuthCode_;
  private String vplFromSys_;
  private String createVpl_;
  private String vplAckn_;
  private String vplAvailDate_;

  // ServiceInfo stanza
  private String retainChangeTeam_;
  private String retainComponent_;
  private String retainRelease_;
  private String contactName_;
  private String contactPhone_;
  private String contactNode_;
  private String contactUserId_;
  private String memoToUsers_;
  private String labelText1_;
  private String labelText2_;
  private String ciaProductIdentification_;
  private String allApars_;
  private String autoIfreq_;
  private String SMSSupercede_;
  private String pkgPtf_;

  private String  baseVersion_;
  private String  baseRelease_;
  private String  baseMaintLevel_;
  private String  baseFixLevel_;

  //constructor
  public InstallEntity()
  {
    //do all initialisation here
    entitySubsetInfo_ = new ArrayList();
  }
  public String toString()
  {
    if(entityName_!=null){
      return entityName_;}
    else
      return new String();
  }

  public int hashCode()
  {
    // The hash code for FileEntity cannot be the standard String hash code, in
    // order to prevent long, but similar, file paths hashing to the same code.
    // This hash algorithm has obtained from the java.lang.String.hashCode(),
    // but does not handle long (> 16 chars) strings any differently.

    String value = toString();
    int h = 0;
    int off = 0;
    int len = value.length();
    char val[] = new char[len];

    value.getChars(off, len, val, 0);

    for (int i = len; i > 0; i--)
    {
      h = (h * 37) + val[off++];
    }
    return h;
  }

  public boolean equals( Object obj )
  {
    return (hashCode() == obj.hashCode());
  }

  public void populate(int parentToken, int stanzaToken,
       int attribToken, int type, String value)
              throws PackageException
  {
    if (parentToken == ParserGeneratorEnumType.INSTALLENTITY)
    {
      switch(stanzaToken)
      {
        //multiple times occuring stanzas - within IE here
        case ParserGeneratorEnumType.ENTITYSUBSETINFO:

          EntitySubsetInfo curEntSubset =
                                (EntitySubsetInfo)entitySubsetInfo_.get(entitySubsetInfo_.size()-1);
          switch(attribToken)
          {
            case ParserGeneratorEnumType.SUBSETNAME:
              curEntSubset.setSubsetName(value);
              break;
            case ParserGeneratorEnumType.FULLSUBSETNAME:
              curEntSubset.setFullSubsetName(value);
              break;
            case ParserGeneratorEnumType.SUBSETDESCRIPTION:
              curEntSubset.setSubsetDescription(new PgSpecialType(type,value) );
              break;
          }//end switch attribtoken
          break;

        //zero or one time occuring stanzas - within IE here
        case ParserGeneratorEnumType.ENTITYINFO:

          switch(attribToken)
          {
            //list out cases for all attributes of entityinfo stanza
            //which occur directly within Stanza as strings/constants

            case ParserGeneratorEnumType.ENTITYNAME:
              setEntityName(value);
              break;
            case ParserGeneratorEnumType.ENTITYID  :
              setEntityId(value);
              break;
            case ParserGeneratorEnumType.DESCRIPTION:
              setDescription(new PgSpecialType(type,value) );
              break;
            case ParserGeneratorEnumType.IMAGENAME:
              setImageName(value);
              break;
            case ParserGeneratorEnumType.VERSION:
              setVersion(value);
              break;
            case ParserGeneratorEnumType.RELEASE:
              setRelease(value);
              break;
            case ParserGeneratorEnumType.MAINTLEVEL:
              setMaintLevel(value);
              break;
            case ParserGeneratorEnumType.FIXLEVEL:
              setFixLevel(value);
              break;
            case ParserGeneratorEnumType.SERIALNUMBER:
              setSerialNumber(value);
              break;
            case ParserGeneratorEnumType.VERSIONDATE:
              setVersionDate(value);
              break;
            case ParserGeneratorEnumType.HIDDENSTATE:
              setHiddenState(value);
              break;
            case ParserGeneratorEnumType.CATEGORY:
              setCategory(value);
              break;
            case ParserGeneratorEnumType.CATEGORYTITLE:
              setCategoryTitle(value);
              break;
            case ParserGeneratorEnumType.COPYRIGHT:
              setCopyright(new PgSpecialType(type,value) );
              break;
            case ParserGeneratorEnumType.CHANGELOG:
              setChangeLog(new PgSpecialType(type,value) );
              break;
            case ParserGeneratorEnumType.COPYRIGHTMAP:
              setCopyrightMap(value);
              break;
            case ParserGeneratorEnumType.COPYRIGHTFLAGS:
              setCopyrightFlags(value);
              break;
            case ParserGeneratorEnumType.LANGUAGE:
              setLanguage(value);
              break;
            case ParserGeneratorEnumType.CONTENT:
              setContent(value);
              break;
            case ParserGeneratorEnumType.DISTRIBUTION:
              setDistribution(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "ENTITYINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.LINKINFO:

          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.PARENT          :
              setParent(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "LINKINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.VENDORINFO:

          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.VENDORNAME:
              setVendorName(value);
              break;
            case ParserGeneratorEnumType.VENDORTITLE:
              setVendorTitle(value);
              break;
            case ParserGeneratorEnumType.VENDORDESC:
              setVendorDesc(new PgSpecialType(type,value) );
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "VENDORINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.ARCHITECTUREINFO:

          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.MACHINESERIES:
              setMachineSeries(value);
              break;
            case ParserGeneratorEnumType.MACHINETYPE:
              setMachineType(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "ARCHITECTUREINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.SUPPORTINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.README:
              setReadme(new PgSpecialType(type,value) );
              break;
            case ParserGeneratorEnumType.MANPAGE:
              setManpage(new PgSpecialType(type,value) );
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "SUPPORTINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.INSTALLSTATESINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.INTERACTIVE:
              setInteractive(value);
              break;
            case ParserGeneratorEnumType.MEDIAID:
              setMediaId(value);
              break;
            case ParserGeneratorEnumType.TOTALMEDIAUSED:
              setTotalMediaUsed(value);
              break;
            case ParserGeneratorEnumType.INSTALLSTATES:
              setInstallStates(value);
              break;
            case ParserGeneratorEnumType.REMOVABLESTATES:
              setRemovableStates(value);
              break;
            case ParserGeneratorEnumType.ISLOCATABLE:
              setIsLocatable(value);
              break;
            case ParserGeneratorEnumType.ISKERNEL:
              setIsKernel(value);
              break;
            case ParserGeneratorEnumType.INSTALLDIR:
              setInstallDir(new PgSpecialType(type,value) );
              break;
            case ParserGeneratorEnumType.INSTALLSPACE:
              setInstallSpace(new PgSpecialType(type,value) );
              break;
            case ParserGeneratorEnumType.BOOTREQMT:
              setBootReqmt(value);
              break;
            case ParserGeneratorEnumType.ADEPACKAGEFLAGS:
            case ParserGeneratorEnumType.PACKAGEFLAGS:
              setADEPackageFlags(value);
              break;
            case ParserGeneratorEnumType.MODE:
              setMode(value);
              break;
            case ParserGeneratorEnumType.SELECTION:
              setADEInvFlags(value);
              setMaxInst(value);
              //setSelection(value);
              break;
            case ParserGeneratorEnumType.ADEINVFLAGS:
              setADEInvFlags(value);
              break;
            case ParserGeneratorEnumType.MAXINST:
              setMaxInst(value);
              break;
            case ParserGeneratorEnumType.ARFLAGS:
              setARFlags(value);
              break;
            case ParserGeneratorEnumType.COMPIDTABLE:
              setCompidTable(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "INSTALLSTATESINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.PATHINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings
            case ParserGeneratorEnumType.ODMCLASSDEF:
              setOdmClassDef(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "PATHINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.REQUISITESINFO:
          switch(attribToken)
          {
            // List out all cases for all attributes of the file stanza
            // wich occur directly within the RequisitesInfo file stanza
            // as strings.

            case ParserGeneratorEnumType.AUTOREQPROV:
              setAutoreqprov(value);
              break;
            case ParserGeneratorEnumType.USERPREREQ     :
              setUserPrereq(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
              + "Stanza token passed to populate method.\nPATHINFO\n");
          }
          break;

        case ParserGeneratorEnumType.PACKAGEINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            // For the otherwise unaware these methods are called from
            // com.ibm.ode.pkg.parserGenerator.InstallEntity.
            case ParserGeneratorEnumType.PACKAGENAME:
              setPackageName(value);
              break;
            case ParserGeneratorEnumType.FULLPACKAGENAME:
              setFullPackageName(value);
              break;
            case ParserGeneratorEnumType.PACKAGEDESC:
              setPackageDesc(new PgSpecialType(type,value) );
              break;
            case ParserGeneratorEnumType.PACKAGECOPYRIGHT:
              setPackageCopyright(new PgSpecialType(type,value) );
              break;
            case ParserGeneratorEnumType.PACKAGESERIALNUMBER:
              setPackageSerialNumber(value);
              break;
            case ParserGeneratorEnumType.PACKAGEVENDORNAME:
              setPackageVendorName(value);
              break;
            case ParserGeneratorEnumType.PACKAGEVENDORTITLE:
              setPackageVendorTitle(value);
              break;
            case ParserGeneratorEnumType.PACKAGEVENDORDESC:
              setPackageVendorDesc(new PgSpecialType(type,value) );
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "PACKAGEINFO " + attribToken + "\n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.MVSINFO            :
          switch(attribToken)
          {
            case ParserGeneratorEnumType.APPLID         :
              setApplid(value);
              break;
            case ParserGeneratorEnumType.REWORK         :
              setRework(value);
              break;
            case ParserGeneratorEnumType.SREL           :
              setSrel(value);
              break;
            case ParserGeneratorEnumType.FESN           :
              setFesn(value);
              break;
            case ParserGeneratorEnumType.DELETE         :
              setDelete(value);
              break;
            case ParserGeneratorEnumType.PREVIOUS       :
              setPrevious(value);
              break;
            case ParserGeneratorEnumType.FUTURE:
              setFuture(value);
              break;
            case ParserGeneratorEnumType.CTLDEFINFILE   :
              setCtldefinFile(value);
              break;
            case ParserGeneratorEnumType.EXTRASMPEFILE  :
              setExtraSmpeFile(value);
              break;
            case ParserGeneratorEnumType.VERSIONREQ     :
              setVersionReq(value);
              break;
            case ParserGeneratorEnumType.JCLINLIB       :
              setJclinLib(value);
              break;
            case ParserGeneratorEnumType.DSNHLQ       :
              setDsnHlq(value);
              break;
            case ParserGeneratorEnumType.LKEDUNIT       :
              setLkedUnit(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "MVSINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.VPLINFO:
          switch(attribToken)
          {
            case ParserGeneratorEnumType.VPLAUTHCODE     :
              setVplAuthCode(value);
              break;
            case ParserGeneratorEnumType.VPLFROMSYS      :
              setVplFromSys(value);
              break;
            case ParserGeneratorEnumType.CREATEVPL       :
              setCreateVpl(value);
              break;
            case ParserGeneratorEnumType.VPLACKN         :
              setVplAckn(value);
              break;
            case ParserGeneratorEnumType.VPLAVAILDATE    :
              setVplAvailDate(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "VPLINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.SERVICEINFO:
          switch(attribToken)
          {
            case ParserGeneratorEnumType.RETAINCHANGETEAM:
              setRetainChangeTeam(value);
              break;
            case ParserGeneratorEnumType.RETAINCOMPONENT:
              setRetainComponent(value);
              break;
            case ParserGeneratorEnumType.RETAINRELEASE:
              setRetainRelease(value);
              break;
            case ParserGeneratorEnumType.CONTACTNAME:
              setContactName(value);
              break;
            case ParserGeneratorEnumType.CONTACTPHONE:
              setContactPhone(value);
              break;
            case ParserGeneratorEnumType.CONTACTNODE:
              setContactNode(value);
              break;
            case ParserGeneratorEnumType.CONTACTUSERID:
              setContactUserId(value);
              break;
            case ParserGeneratorEnumType.MEMOTOUSERS:
              setMemoToUsers(value);
              break;
            case ParserGeneratorEnumType.LABELTEXT1:
              setLabelText1(value);
              break;
            case ParserGeneratorEnumType.LABELTEXT2:
              setLabelText2(value);
              break;
            case ParserGeneratorEnumType.CIAPRODUCTIDENTIFICATION :
              setCiaProductIdentification(value);
              break;
            case ParserGeneratorEnumType.ALLAPARS:
              setAllApars(value);
              break;
            case ParserGeneratorEnumType.AUTOIFREQ:
              setAutoIfreq(value);
              break;
            case ParserGeneratorEnumType.SMSSUPERCEDE:
              setSMSSupercede(value);
              break;
            case ParserGeneratorEnumType.PKGPTF:
              setPkgPtf(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "SERVICEINFO \n");
          }//end switch on attribToken
          break;

        default :
          throw new PackageException("InstallEntity: Invalid Current "
                             + "Stanza token passed to populate method.\n"
                             + "end switch attribToken " + attribToken + "\n");
      }//end switch on stanzaToken

    }//end if check parenttoken = installentitytoken
    else
    {
      throw new PackageException("InstallEntity: Invalid ParentStanza"
                                  + " token passed to method populate.\n\n");
    }
  }//end populate String


  public void populate(int parentToken, int stanzaToken,
       int attribToken, int type, ReqType value)
               throws PackageException
  {
  if (parentToken == ParserGeneratorEnumType.INSTALLENTITY)
  {
    switch(stanzaToken)
    {
      //multiple times occuring stanzas - within IE here

      //currently, no stanza occuring multiple times has an
      //attribute of type ReqType - else, corr. code comes here.

      //zero or one time occuring stanzas - within IE here

      //currently, no stanza occuring zero or one times has an
      //attribute of type ReqType - else, corr. code comes here.
      //note - List of ReqType attribs are present, but they
      //are categorized as ArrayList of objects and appear in the
      //List section of populate method elsewhere in this class

      default :
        throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "end switch on stanzaToken \n");

    }//end switch on stanzaToken

  }//end if check parenttoken = installentitytoken
  else
  {
    throw new PackageException("InstallEntity: Invalid ParentStanza"
                                  + " token passed to method populate.\n\n");
      }
  }//end populate ReqType

  public void populate(int parentToken, int stanzaToken,
       int attribToken, int type, ArrayList value)
        throws PackageException
  {
    if (parentToken == ParserGeneratorEnumType.INSTALLENTITY)
    {
      switch(stanzaToken)
      {
        //multiple times occuring stanzas - within IE here
        case ParserGeneratorEnumType.ENTITYSUBSETINFO:
          EntitySubsetInfo curEntSubset =
                                  (EntitySubsetInfo)entitySubsetInfo_.get(entitySubsetInfo_.size()-1);
          switch(attribToken)
          {
            case ParserGeneratorEnumType.SUBSETCONTENT:
              curEntSubset.setSubsetContent(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current"
                               + " Stanza token passed to populate method.\n"
                               + "SUBSETCONTENT \n");

          }//end switch attribtoken
          break;

        //zero or one time occuring stanzas - within IE here
        case ParserGeneratorEnumType.ENTITYINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of entityinfo stanza
            //which occur directly within Stanza as strings/constants
            case ParserGeneratorEnumType.FULLENTITYNAME:
              setFullEntityName(value);
              break;
            case ParserGeneratorEnumType.COPYRIGHTKEYS:
              setCopyrightKeys(value);
              break;
            case ParserGeneratorEnumType.INSLIST:
              setInsList(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                              + "Stanza token passed to populate method.\n"
                              + "ENTITYINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.LINKINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.IMMCHILDENTITIES:
              setImmChildEntities(value);
              break;
            case ParserGeneratorEnumType.IMMCHILDFILES:
              setImmChildFiles(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                              + "Stanza token passed to populate method.\n"
                              + "LINKINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.ARCHITECTUREINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.OSNAME:
              setOsName(value);
              break;
            case ParserGeneratorEnumType.OSRELEASE:
              setOsRelease(value);
              break;
            case ParserGeneratorEnumType.OSVERSION:
              setOsVersion(value);
              break;
            case ParserGeneratorEnumType.EXCLUDEOS:
              setExcludeOS(new ArrayList(value));
              break;
            case ParserGeneratorEnumType.EXCLUSIVEOS:
              setExclusiveOS(new ArrayList(value));
              break;
            case ParserGeneratorEnumType.EXCLUDEARCH:
              setExcludeArch(new ArrayList(value));
              break;
            case ParserGeneratorEnumType.EXCLUSIVEARCH:
              setExclusiveArch(new ArrayList(value));
              break;
            default :
              throw new PackageException("InstallEntity: Invalid "
             + "Architecture Info Stanza token passed to populate method.\n"
             + "ARCHITECTURINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.SUPPORTINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.HOTLINE:
              setHotline(value);
              break;
            case ParserGeneratorEnumType.EMAIL :
              setEmail(value);
              break;
            case ParserGeneratorEnumType.URL:
              setUrl(new ArrayList(value));
              break;
            case ParserGeneratorEnumType.PACKAGEORDER:
              setPackageOrder(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "SUPPORTINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.INSTALLSTATESINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.CONSTANTLIST:
              setConstantList(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "CONSTANTLIST \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.PATHINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.INPUTPATH:
              setInputPath(value);
              break;
            case ParserGeneratorEnumType.CONFIGFILES:
              setConfigFiles(value);
              break;
            case ParserGeneratorEnumType.ODMADDFILES:
              setOdmAddFiles(value);
              break;
            case ParserGeneratorEnumType.ROOTCONTROLFILES:
              setRootControlFiles(value);
              break;
            case ParserGeneratorEnumType.ROOTADDFILES:
              setRootAddFiles(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "PATHINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.REQUISITESINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.REQUISITES:
              setRequisites(value);
              break;
              // case ParserGeneratorEnumType.AUTOREQPROV:
              // System.out.println("AttribToken " + attribToken + "\n" );
              // setAutoreqprov ( value );
              // break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "REQUISITESINFO \n");
          }//end switch on attribToken
          break;

        case ParserGeneratorEnumType.MVSINFO:
          switch(attribToken)
          {
            //list out cases for all attributes of file stanza
            //which occur directly within file Stanza as strings

            case ParserGeneratorEnumType.DISTLIBS:
              setDistlibs(value);
              break;
            case ParserGeneratorEnumType.TYPE:
              setType(value);
              break;
            case ParserGeneratorEnumType.SEP:
              setSep(value);
              break;
            case ParserGeneratorEnumType.FEATUREFMIDS:
              setFeatureFMIDs(value);
              break;
            default :
              throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "MVSINFO \n");
          }//end switch on attribToken
          break;

        default :
          throw new PackageException("InstallEntity: Invalid Current "
                                + "Stanza token passed to populate method.\n"
                                + "End switch on stanzaToken \n");
      }//end switch on stanzaToken

    }//end if check parenttoken = installentitytoken
    else
    {
      throw new PackageException("InstallEntity: Invalid ParentStanza"
                                      + " token passed to method populate.\n"
                                      + "end if check parenttoken \n");
    }

  }//end populate ArrayList

  //get and set functions for the entitySubsetInfo_
  public void setEntitySubsetInfo(ArrayList entitySubsetInfo)
  {
    entitySubsetInfo_ = new ArrayList(entitySubsetInfo) ;
  }

  public ArrayList getEntitySubsetInfo()
  {
    return entitySubsetInfo_;
  }

  //get and set functions for the attribute entityName_
  public void setEntityName(String entityName)
  {
    entityName_ = entityName;
  }

  public String getEntityName()
  {
    return entityName_;
  }

  //get and set functions for the attribute fullEntityName_
  public void setFullEntityName(ArrayList fullEntityName)
  {
    fullEntityName_ = new ArrayList(fullEntityName);
  }

  public ArrayList getFullEntityName()
  {
    return fullEntityName_;
  }

  //get and set functions for the attribute entityId_
  public void setEntityId(String entityId)
  {
    entityId_ = entityId;
  }

  public String getEntityId()
  {
    return entityId_;
  }

  //get and set functions for the attribute description_
  public void setDescription(PgSpecialType description)
  {
    description_ = new PgSpecialType(description);
  }

  public PgSpecialType getDescription()
  {
    return description_;
  }

  //get and set functions for the attribute imageName_
  public void setImageName(String imageName)
  {
    imageName_ = imageName;
  }

  public String getImageName()
  {
    return imageName_;
  }

  //get and set functions for the attribute version_
  public void setVersion(String version)
  {
    version_ = version;
  }

  public String getVersion()
  {
    return version_;
  }

  //get and set functions for the attribute release_
  public void setRelease(String release)
  {
    release_ = release;
  }

  public String getRelease()
  {
    return release_;
  }

  //get and set functions for the attribute maintLevel_
  public void setMaintLevel(String maintLevel)
  {
    maintLevel_ = maintLevel;
  }

  public String getMaintLevel()
  {
    return maintLevel_;
  }

  //get and set functions for the attribute fixLevel_
  public void setFixLevel(String fixLevel)
  {
    fixLevel_ = fixLevel;
  }

  public String getFixLevel()
  {
    return fixLevel_;
  }

  //get and set functions for the attribute versionDate_
  public void setVersionDate(String versionDate)
  {
    versionDate_ = versionDate;
  }

  public String getVersionDate()
  {
    return versionDate_;
  }

  //get and set functions for the attribute hiddenState_
  public void setHiddenState(String hiddenState)
  {
    hiddenState_ = hiddenState;
  }

  public String getHiddenState()
  {
    return hiddenState_;
  }

  //get and set functions for the attribute category_
  public void setCategory(String category)
  {
    category_ = category;
  }

  public String getCategory()
  {
    return category_;
  }

  //get and set functions for the attribute categoryTitle_
  public void setCategoryTitle(String categoryTitle)
  {
    categoryTitle_ = categoryTitle;
  }

  public String getCategoryTitle()
  {
    return categoryTitle_;
  }

  //get and set functions for the attribute copyright_
  public void setCopyright(PgSpecialType copyright)
  {
    copyright_ = new PgSpecialType(copyright);
  }

  public PgSpecialType getCopyright()
  {
    return copyright_;
  }

  // get and set functions for the attribute changeLog_
  public void setChangeLog(PgSpecialType changeLog)
  {
    changeLog_ = new PgSpecialType(changeLog);
  }

  public PgSpecialType getChangeLog()
  {
    return changeLog_;
  }

  //get and set functions for the attribute copyrightKeys_
  public void setCopyrightKeys(ArrayList copyrightKeys)
  {
    copyrightKeys_ = new ArrayList(copyrightKeys);
  }

  public ArrayList getCopyrightKeys()
  {
    return copyrightKeys_;
  }

  //get and set functions for the attribute copyrightMap_
  public void setCopyrightMap(String copyrightMap)
  {
    copyrightMap_ = copyrightMap;
  }

  public String getCopyrightMap()
  {
    return copyrightMap_;
  }

  //get and set functions for the attribute copyrightFlags_
  public void setCopyrightFlags(String copyrightFlags)
  {
    copyrightFlags_ = copyrightFlags;
  }

  public String getCopyrightFlags()
  {
    return copyrightFlags_;
  }

  //get and set functions for the attribute language_
  public void setLanguage(String language)
  {
    language_ = language;
  }

  public String getLanguage()
  {
    return language_;
  }

  //get and set functions for the attribute content_
  public void setContent(String content)
  {
    content_ = content;
  }

  public String getContent()
  {
    return content_;
  }

  //get and set functions for the attribute insList_
  public void setInsList(ArrayList insList)
  {
    insList_ = new ArrayList(insList);
  }

  public ArrayList getInsList()
  {
    return insList_;
  }

  //get and set functions for the attribute immChildEntities_
  public void setImmChildEntities(ArrayList immChildEntities)
  {
    immChildEntities_ = new ArrayList(immChildEntities);
  }

  public ArrayList getImmChildEntities()
  {
    return immChildEntities_;
  }

  //get and set functions for the attribute immChildFiles
  public void setImmChildFiles(ArrayList immChildFiles)
  {
    immChildFiles_ = new ArrayList(immChildFiles);
  }

  public ArrayList getImmChildFiles()
  {
    return immChildFiles_;
  }

  //get and set functions for the attribute parent_
  public void setParent(String parent)
  {
    parent_ = parent;
  }

  public String getParent()
  {
    return parent_;
  }

  //get and set functions for the attribute vendorName_
  public void setVendorName(String vendorName)
  {
    vendorName_ = vendorName;
  }

  public String getVendorName()
  {
    return vendorName_;
  }

  //get and set functions for the attribute vendorTitle
  public void setVendorTitle(String vendorTitle)
  {
    vendorTitle_ = vendorTitle;
  }

  public String getVendorTitle()
  {
    return vendorTitle_;
  }

  //get and set functions for the attribute vendorDesc_
  public void setVendorDesc(PgSpecialType vendorDesc)
  {
    vendorDesc_ = new PgSpecialType(vendorDesc);
  }

  public PgSpecialType getVendorDesc()
  {
    return vendorDesc_;
  }

  //get and set functions for the attribute machineSeries_
  public void setMachineSeries(String machineSeries)
  {
    machineSeries_ = machineSeries;
  }

  public String getMachineSeries()
  {
    return machineSeries_;
  }

  //get and set functions for the attribute machineType_
  public void setMachineType(String machineType)
  {
    machineType_ = machineType;
  }

  public String getMachineType()
  {
    return machineType_;
  }

  //get and set functions for the attribute excludeArch_
  public void setExcludeArch(ArrayList excludeArch)
  {
    excludeArch_ = new ArrayList(excludeArch);
  }

  public ArrayList getExcludeArch()
  {
    return excludeArch_;
  }

  //get and set functions for the attribute exclusiveArch_
  public void setExclusiveArch(ArrayList exclusiveArch)
  {
    exclusiveArch_ = new ArrayList(exclusiveArch);
  }

  public ArrayList getExclusiveArch()
  {
    return exclusiveArch_;
  }

  //get and set functions for the attribute excludeOS_
  public void setExcludeOS(ArrayList excludeOS)
  {
    excludeOS_ = new ArrayList(excludeOS);
  }

  public ArrayList getExcludeOS()
  {
    return excludeOS_;
  }

  //get and set functions for the attribute exclusiveOS_
  public void setExclusiveOS(ArrayList exclusiveOS)
  {
    exclusiveOS_ = new ArrayList(exclusiveOS);
  }

  public ArrayList getExclusiveOS()
  {
    return exclusiveOS_;
  }

  //get and set functions for the attribute osName_
  public void setOsName(ArrayList osName)
  {
    osName_ = new ArrayList(osName);
  }

  public ArrayList getOsName()
  {
    return osName_;
  }

  //get and set functions for the attribute osRelease_
  public void setOsRelease(ArrayList osRelease)
  {
    osRelease_ = new ArrayList(osRelease);
  }

  public ArrayList getOsRelease()
  {
    return osRelease_;
  }

  //get and set functions for the attribute osVersion_
  public void setOsVersion(ArrayList osVersion)
  {
    osVersion_ = new ArrayList(osVersion);
  }

  public ArrayList getOsVersion()
  {
    return osVersion_;
  }

  //get and set functions for the attribute readme_
  public void setReadme(PgSpecialType readme)
  {
    readme_ = new PgSpecialType(readme);
  }

  public PgSpecialType getReadme()
  {
    return readme_;
  }

  //get and set functions for the attribute manpage_
  public void setManpage(PgSpecialType manpage)
  {
    manpage_ = new PgSpecialType(manpage);
  }

  public PgSpecialType getManpage()
  {
    return manpage_;
  }

  //get and set functions for the attribute hotline_
  public void setHotline(ArrayList hotline)
  {
    hotline_ = new ArrayList(hotline);
  }

  public ArrayList getHotline()
  {
    return hotline_;
  }

  //get and set functions for the attribute email_
  public void setEmail(ArrayList email)
  {
    email_ = new ArrayList(email);
  }

  public ArrayList getEmail()
  {
    return email_;
  }

  //get and set functions for the attribute packageOrder_
  public void setPackageOrder(ArrayList packageOrder)
  {
    packageOrder_ = new ArrayList(packageOrder);
  }

  public ArrayList getPackageOrder()
  {
    return packageOrder_;
  }

  //get and set functions for the attribute Url_
  public void setUrl(ArrayList Url)
  {
    Url_ = new ArrayList(Url);
  }

  public ArrayList getUrl()
  {
    return Url_;
  }

  //get and set functions for the attribute interactive_
  public void setInteractive(String interactive)
  {
    interactive_ = interactive;
  }

  public String getInteractive()
  {
    return interactive_;
  }

  //get and set functions for the attribute mediaId_
  public void setMediaId(String mediaId)
  {
    mediaId_ = mediaId;
  }

  public String getMediaId()
  {
    return mediaId_;
  }

  //get and set functions for the attribute totalMediaUsed_
  public void setTotalMediaUsed(String totalMediaUsed)
  {
    totalMediaUsed_ = totalMediaUsed;
  }

  public String getTotalMediaUsed()
  {
    return totalMediaUsed_;
  }

  //get and set functions for the attribute installStates_
  public void setInstallStates(String installStates)
  {
    installStates_ = installStates;
  }

  public String getInstallStates()
  {
    return installStates_;
  }

  //get and set functions for the attribute removableStates_
  public void setRemovableStates(String removableStates)
  {
    removableStates_ = removableStates;
  }

  public String getRemovableStates()
  {
    return removableStates_;
  }

  //get and set functions for the attribute constantList_
  public void setConstantList(ArrayList constantList)
  {
    constantList_ = new ArrayList(constantList);
  }

  public ArrayList getConstantList()
  {
    return constantList_;
  }

  //get and set functions for the attribute isLocatable_
  public void setIsLocatable(String isLocatable)
  {
    isLocatable_ = isLocatable;
  }

  public String getIsLocatable()
  {
    return isLocatable_;
  }

  //get and set functions for the attribute isKernel_
  public void setIsKernel(String isKernel)
  {
    isKernel_ = isKernel;
  }

  public String getIsKernel()
  {
    return isKernel_;
  }

  //get and set functions for the attribute installDir_
  public void setInstallDir(PgSpecialType installDir)
  {
    installDir_ = new PgSpecialType(installDir);
  }

  public PgSpecialType getInstallDir()
  {
    return installDir_;
  }

  //get and set functions for the attribute installSpace_
  public void setInstallSpace(PgSpecialType installSpace)
  {
    installSpace_ = new PgSpecialType(installSpace);
  }

  public PgSpecialType getInstallSpace()
  {
    return installSpace_;
  }

  //get and set functions for the attribute bootReqmt_
  public void setBootReqmt(String bootReqmt)
  {
    bootReqmt_ = bootReqmt;
  }

  public String getBootReqmt()
  {
    return bootReqmt_;
  }

  //get and set functions for the attribute adePackageFlags_
  public void setADEPackageFlags(String adePackageFlags)
  {
    if (adePackageFlags_!=null) {
      Interface.printWarning("Changing adePackageFlags from "
                                 +adePackageFlags_+" to "+adePackageFlags);
    }
    adePackageFlags_ = adePackageFlags;
  }

  public String getADEPackageFlags()
  {
    return adePackageFlags_;
  }

  //get and set functions for the attribute mode_
  public void setMode(String mode)
  {
    mode_ = mode;
  }

  public String getMode()
  {
    return mode_;
  }

  //get and set functions for the attribute adeInvFlags_
  public void setADEInvFlags(String adeInvFlags)
  {
    if (adeInvFlags_!=null) {
      Interface.printWarning("Changing adeInvFlags from "
                                           +adeInvFlags_+" to "+adeInvFlags);
    }
    adeInvFlags_ = adeInvFlags;
  }

  public String getADEInvFlags()
  {
    return adeInvFlags_;
  }

  //get and set functions for the attribute maxInst_
  public void setMaxInst(String maxInst)
  {
    if (maxInst_!=null) {
      Interface.printWarning("Changing maxInst from "+maxInst_+" to "+maxInst);
    }
    maxInst_ = maxInst;
  }

  public String getMaxInst()
  {
    return maxInst_;
  }

  //get and set functions for the attribute arFlags_
  public void setARFlags(String arFlags)
  {
    if (arFlags_!=null) {
      Interface.printWarning("Changing arFlags from "+arFlags_+" to "+arFlags);
    }
    arFlags_ = arFlags;
  }

  public String getARFlags()
  {
    return arFlags_;
  }

  //get and set functions for the attribute compidTable_
  public void setCompidTable(String compidTable)
  {
    compidTable_ = compidTable;
  }

  public String getCompidTable()
  {
    return compidTable_;
  }

  //get and set functions for the attribute inputPath_
  public void setInputPath(ArrayList inputPath)
  {
    inputPath_ = new ArrayList(inputPath);
  }

  public ArrayList getInputPath()
  {
    return inputPath_;
  }

  //get and set functions for the attribute configFiles_
  public void setConfigFiles(ArrayList configFiles)
  {
    configFiles_ = new ArrayList(configFiles);
  }

  public ArrayList getConfigFiles()
  {
    return configFiles_;
  }

  //get and set functions for the attribute odmAddFiles_
  public void setOdmAddFiles(ArrayList odmAddFiles)
  {
    odmAddFiles_ = new ArrayList(odmAddFiles);
  }

  public ArrayList getOdmAddFiles()
  {
    return odmAddFiles_;
  }

  //get and set functions for the attribute odmClassDef_
  public void setOdmClassDef(String odmClassDef)
  {
    odmClassDef_ = odmClassDef;
  }

  public String getOdmClassDef()
  {
    return odmClassDef_;
  }

  //get and set functions for the attribute rootControlFiles_
  public void setRootControlFiles(ArrayList rootControlFiles)
  {
    rootControlFiles_ = new ArrayList(rootControlFiles);
  }

  public ArrayList getRootControlFiles()
  {
    return rootControlFiles_;
  }

  //get and set functions for the attribute rootAddFiles_
  public void setRootAddFiles(ArrayList rootAddFiles)
  {
    rootAddFiles_ = new ArrayList(rootAddFiles);
  }

  public ArrayList getRootAddFiles()
  {
    return rootAddFiles_;
  }

  //get and set functions for the attribute requisites_
  public void setRequisites(ArrayList requisites)
  {
    requisites_ = new ArrayList(requisites);
  }

  public ArrayList getRequisites()
  {
    return requisites_;
  }

  //get and set functions for the attribute userPrereq_
  public void setUserPrereq(String userPrereq)
  {
    userPrereq_ = userPrereq;
  }

  public String getUserPrereq()
  {
    return userPrereq_;
  }

  //get and set functions for the attribute autoreqprov_
  public void setAutoreqprov( String autoreqprov )
  {
    autoreqprov_ = autoreqprov;
  }

  public String getAutoreqprov()
  {
    return autoreqprov_;
  }

  //get and set functions for the attribute pkgName
  public void setPackageName(String packageName)
  {
    pkgName_ = packageName;
  }

  public String getPackageName()
  {
    return pkgName_;
  }

  //get and set functions for the attribute fullPkgName
  public void setFullPackageName(String fullPkgName)
  {
    fullPkgName_ = fullPkgName;
  }

  public String getFullPackageName()
  {
    return fullPkgName_;
  }

  //get and set functions for the attribute pkgDesc
  public void setPackageDesc(PgSpecialType pkgDesc)
  {
    pkgDesc_ = new PgSpecialType(pkgDesc);
  }

  public PgSpecialType getPackageDesc()
  {
    return pkgDesc_;
  }

  //get and set functions for the attribute pkgCopyright_
  public void setPackageCopyright(PgSpecialType copyright)
  {
    pkgCopyright_ = new PgSpecialType(copyright);
  }

  public PgSpecialType getPackageCopyright()
  {
    return pkgCopyright_;
  }

  //get and set functions for the attribute pkgSerialNumber_
  public void setPackageSerialNumber(String serialNumber)
  {
    pkgSerialNumber_ = serialNumber;
  }

  public String getPackageSerialNumber()
  {
    return pkgSerialNumber_;
  }

  //get and set functions for the attribute pkgVendorName_
  public void setPackageVendorName(String pkgVendorName)
  {
    pkgVendorName_ = pkgVendorName;
  }

  public String getPackageVendorName()
  {
    return pkgVendorName_;
  }

  //get and set functions for the attribute pkgVendorTitle_
  public void setPackageVendorTitle(String pkgVendorTitle)
  {
    pkgVendorTitle_ = pkgVendorTitle;
  }

  public String getPackageVendorTitle()
  {
    return pkgVendorTitle_;
  }

  //get and set functions for the attribute pkgVendorDesc_
  public void setPackageVendorDesc(PgSpecialType pkgVendorDesc)
  {
    pkgVendorDesc_ = new PgSpecialType(pkgVendorDesc);
  }

  public PgSpecialType getPackageVendorDesc()
  {
    return pkgVendorDesc_;
  }

  //get and set functions for the attribute distribution_
  public void setDistribution(String distribution)
  {
    distribution_ = new String(distribution);
  }

  public String getDistribution()
  {
    return distribution_;
  }

  public boolean getMarkReferenced()
  {
    return markReferenced_;
  }

  public void setMarkReferenced()
  {
    markReferenced_ = true;
  }

  public String getApplid()
  {
    return applid_;
  }
  public void setApplid(String value)
  {
    applid_ = value;
  }

  public String getRework()
  {
    return rework_;
  }
  public void setRework(String value)
  {
    rework_ = value;
  }
  public ArrayList getDistlibs()
  {
    return distlibs_;
  }
  public void setDistlibs(ArrayList value)
  {
    distlibs_ = new ArrayList(value);
  }

  public String getSrel()
  {
    return srel_;
  }
  public void setSrel(String value)
  {
    srel_ = value;
  }

  public ArrayList getType()
  {
    return type_;
  }
  public void setType(ArrayList value)
  {
    type_ = new ArrayList(value);
  }

  public String getDelete()
  {
    return delete_;
  }
  public void setDelete(String delete)
  {
    delete_ = delete;
  }

  public String getFuture()
  {
    return future_;
  }
  public void setFuture(String future)
  {
    future_ = future;
  }

  public String getPrevious()
  {
    return previous_;
  }
  public void setPrevious(String previous)
  {
    previous_ = previous;
  }

  public String getCtldefinFile()
  {
    return ctldefinFile_;
  }
  public void setCtldefinFile(String ctldefinFile)
  {
    ctldefinFile_ = ctldefinFile;
  }

  public String getExtraSmpeFile()
  {
    return extraSmpeFile_;
  }
  public void setExtraSmpeFile(String extraSmpeFile)
  {
    extraSmpeFile_ = extraSmpeFile;
  }

  public String getVersionReq()
  {
    return versionReq_;
  }
  public void setVersionReq(String versionReq)
  {
    versionReq_ = versionReq;
  }

  public String getLkedUnit()
  {
    return lkedUnit_;
  }
  public void setLkedUnit(String lkedUnit)
  {
    lkedUnit_ = lkedUnit;
  }

  //get and set functions for the attribute featureFmids_
  public ArrayList getFeatureFMIDs()
  {

    return featureFmids_;
  }
  public void setFeatureFMIDs(ArrayList value)
  {

    featureFmids_ = new ArrayList(value);
  }

  public String getJclinLib()
  {
    return jclinLib_;
  }
  public void setJclinLib(String jclinLib)
  {
    jclinLib_ = jclinLib;
  }

  public String getDsnHlq()
  {
    return dsnHlq_;
  }
  public void setDsnHlq(String dsnHlq)
  {
    dsnHlq_ = dsnHlq;
  }

  public String getFesn()
  {
    return fesn_;
  }
  public void setFesn(String value)
  {
    fesn_ = value;
  }

  public ArrayList getSep()
  {
    return sep_;
  }
  public void setSep(ArrayList value)
  {
    sep_ = new ArrayList(value);
  }

  public String getVplAuthCode()
  {
    return vplAuthCode_;
  }
  public void setVplAuthCode(String value)
  {
    vplAuthCode_ = value;
  }

  public String getVplFromSys()
  {
    return vplFromSys_;
  }
  public void setVplFromSys(String value)
  {
    vplFromSys_ = value;
  }

  public String getCreateVpl()
  {
    return createVpl_;
  }
  public void setCreateVpl(String value)
  {
    createVpl_ = value;
  }

  public String getVplAckn()
  {
    return vplAckn_;
  }
  public void setVplAckn(String value)
  {
    vplAckn_ = value;
  }

  public String getVplAvailDate()
  {
    return vplAvailDate_;
  }
  public void setVplAvailDate(String value)
  {
    vplAvailDate_ = value;
  }

  public String getRetainChangeTeam()
  {
    return retainChangeTeam_;
  }
  public void setRetainChangeTeam(String value)
  {
    retainChangeTeam_ = value;
  }

  public String getRetainComponent()
  {
    return retainComponent_;
  }
  public void setRetainComponent(String value)
  {
    retainComponent_ = value;
  }

  public String getRetainRelease()
  {
    return retainRelease_;
  }
  public void setRetainRelease(String value)
  {
    retainRelease_ = value;
  }

  public String getContactName()
  {
    return contactName_;
  }

  public void setContactName( String value )
  {
    contactName_ = value;
  }

  public String getContactPhone()
  {
    return contactPhone_;
  }

  public void setContactPhone( String value )
  {
    contactPhone_ = value;
  }

  public String getContactNode()
  {
    return contactNode_;
  }
  public void setContactNode(String value)
  {
    contactNode_ = value;
  }

  public String getContactUserId()
  {
    return contactUserId_;
  }
  public void setContactUserId(String value)
  {
    contactUserId_ = value;
  }

  public String getMemoToUsers()
  {
    return memoToUsers_;
  }
  public void setMemoToUsers(String value)
  {
    memoToUsers_ = value;
  }

  public String getLabelText1()
  {
    return labelText1_;
  }
  public void setLabelText1(String value)
  {
    labelText1_ = value;
  }

  public String getLabelText2()
  {
    return labelText2_;
  }
  public void setLabelText2(String value)
  {
    labelText2_ = value;
  }

  public String getCiaProductIdentification()
  {
    return ciaProductIdentification_;
  }
  public void setCiaProductIdentification(String value)
  {
    ciaProductIdentification_ = value;
  }

  public String getAllApars()
  {
    return allApars_;
  }
  public void setAllApars(String value)
  {
    allApars_ = value;
  }

  public String getAutoIfreq()
  {
    return autoIfreq_;
  }
  public void setAutoIfreq(String value)
  {
    autoIfreq_ = value;
  }


  public String getSMSSupercede()
  {
    return SMSSupercede_;
  }
  public void setSMSSupercede(String value)
  {
    SMSSupercede_ = value;
  }

  public String getPkgPtf()
  {
    return pkgPtf_;
  }
  public void setPkgPtf(String value)
  {
    pkgPtf_ = value;
  }
  public String getBaseVersion()
  {
    return baseVersion_;
  }

  // get and set functions for the attribute serialNumber
  public void setSerialNumber( String serialNumber )
  {
    serialNumber_ = serialNumber ;
  }

  public String getSerialNumber()
  {
    return serialNumber_ ;
  }
  public void setBaseVersion( String version )
  {
    baseVersion_ = version;
  }

  public String getBaseRelease()
  {
    return baseRelease_;
  }

  public void setBaseRelease( String release )
  {
    baseRelease_ = release;
  }
  public String getBaseMaintLevel()
  {
    return baseMaintLevel_;
  }

  public void setBaseMaintLevel( String maintLevel )
  {
    baseMaintLevel_ = maintLevel;
  }

  public String getBaseFixLevel()
  {
    return baseFixLevel_;
  }

  public void setBaseFixLevel( String fixLevel )
  {
    baseFixLevel_ = fixLevel;
  }

  // Methods needed by the Package object for populating the InstallEntity
  // and the classes it contains
  // implements the interface SecondLevelStanza

  public void constructChildStanza( int stanzaToken )
              throws PackageException
  {
    switch( stanzaToken )
    {
      case  ParserGeneratorEnumType.ENTITYSUBSETINFO :
        entitySubsetInfo_.add( new EntitySubsetInfo() );
        break;
      default :
        throw new PackageException("InstallEntity : "
               + "Invalid Stanza Name passed to "
               + "InstallEntity.constructChildStanza() " );
    }
  }

  // implements the interface FirstLevelStanza
  public void constructChildrenOfChildStanza( int parentStanzaToken,
      int stanzaToken )
              throws PackageException
  {
    /* Should be used for stanza contained withing stanzas.
     * Currently there is no such stanza
     * ( after renoval of os2 support.
    **/
    //if( parentStanzaToken == ParserGeneratorEnumType.CONFIGUREINFO )
    //{
    //  ConfigureInfo curConfigureInfo = (ConfigureInfo)configureInfo_.back();
    //  if( curConfigureInfo == null )
    //    throw new PackageException( "InstallEntity : "
    //          + " Invalid reference for ConfigureInfo found in "
    //          + " InstallEntity.constructChildrenOfChildStanza "
    //          + " Make sure you have defined the ConfigureInfo "
    //          + " Stanza " );
    //  curConfigureInfo.constructChildStanza( stanzaToken );
    //}

  }

}//end of InstallEntity class

