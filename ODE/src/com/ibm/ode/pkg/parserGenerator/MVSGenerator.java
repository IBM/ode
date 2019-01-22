/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 5/10/03 15:27:47
 * Date and Time File was extracted/checked out: 06/04/13 16:45:48
 *******************************************************************************/
package com.ibm.ode.pkg.parserGenerator ;

import java.io.FileOutputStream;
import java.util.*;
import com.ibm.ode.pkg.pkgMvs.MvsValidation ;
import com.ibm.ode.lib.io.Interface;

/**
 * This class is responsible for extracting appropriate information from
 *   the installEntities and FileEntities which are present in the
 *   EntityTree at appropriate levels . It writes out the extracted
 *   information to certain files which will be the control files that
 *   act as input to the packaging tool. The name for the packaging tool
 *   considered here has not been finalized. Assumed to be <I>MVSTest</I>
 *
 * Class is only valid for IPP - see Build/390 generators for service info
 *
 * One file 'pcd.mvs' is generated.
 *
 * This generator can be invoked as follows
 * For example:
 * <pre>
 *      MVSGenerator mvsGenerator = new MVSGenerator();
 *      mvsGenerator.generateTargetMetadataFiles(
 *                      curEntityTreeRoot,
 *                      curPackageReference )
 * </pre>
 *
 *
 *
 *
 * @see Generator
 * @see GeneratorInterface
 *
 * @version 1.2
 * @author Kurt Shah
 */
public class MVSGenerator extends Generator
    implements GeneratorInterface
{

  /**********************************************************************
   * Private MVSGenerator variable, used to maintain a handle to the
   *   FileOutputStream, which is used to write the Control file on disk
   **/
  private FileOutputStream outFile_ ;

  /**********************************************************************
   * Private MVSGenerator variable, maintains a reference to EntityTreeRoot
   *   used for retreiving the logical tree structure binding entities
   **/
  private EntityTreeRoot etr_ ;

  /**********************************************************************
   * Private MVSGenerator variable, maintains a reference to Package instance
   *   used for accessing entities and retreiving data stored within.
   **/
  private Package packageRef_ ;

  /**********************************************************************
   * Private MVSGenerator variable, keeps track of current level in the
   *   nested data structure. Used to write as many no. of tabs while
   *   generating data to ensure a properly indented control file.
   **/
  private int noOfTabs;

  /**
   * Some static final variables which will be used in generating error
   * and formatted strings
   **/
  private static final String REQD_FIELD_ERROR =
    " is a required field for generating the package.";
  private static final String REQUISITE_DELETE_FORMAT_ERROR =
    "Requisite type \"delete\" is not in a valid format.";

  private final static int REQD = 10;
  private final static int NOTREQD = 11;

  /**
   * These three variables are used to get the year and the day of year
   * to be used by rework
   **/
  private Calendar cal;
  private Integer year;
  private Integer dofy;

  /**
   * If CTLDEFINFILE and EXTRASMPEFILE exist in the CMF file,
   * this variable will be used to print a warning message.
   **/
  private boolean ctldef_and_extraSmpe = true;

  /**
   * Flag used to denote whether it is necessary to generate JCLIN
   * for this FMID
   **/
  private boolean generateJCLIN;

  /**
   * Flag used to denote whether it is necessary to create JCLINLIB
   * for this FMID
   **/
  private boolean createJCLINLIB;

  /**********************************************************************
   * Creates a new instance of type MVSGenerator.
   **/
  public MVSGenerator(String shipRootDir,
                      String context,
                      String pkgControlDir,
                      String pkgType,
                      String pkgClass,
                      String pkgFixStrategy )
{
  super( shipRootDir,
         context,
         pkgControlDir,
         pkgType,
         pkgClass,
         pkgFixStrategy ) ;
  noOfTabs = 0;
}//end constructor


  /**********************************************************************
   * Primary method used to invoke logic to generate Target Metadata files
   *   for OS/2 using 'MVSTest' as the packaging tool.
   *
   * @param etr Reference to EntityTreeRoot object which holds reference to l
   ogical structure of Entities as a tree.
   * @param pkgRef Reference to Package object which holds reference to Parse
   d Entities and their data.
   *
   *
   * @exception GeneratorException if an error occurs in generating control f
   iles, or information about structure of entities (mutual links binding them
   ) are incorrectly specified with respect to the acceptable structure for HP
   /UX.
   * <p>
   * <p>
   **/
  public void generateTargetMetadataFiles( EntityTreeRoot etr,
                                           Package pkgRef )
    throws GeneratorException
  {
    ArrayList levelArray, productArray;
    Object obj;
    EntityTreeObject curETO;
    int noOfProducts = 0;

    try
    {
      this.etr_ = etr;
      this.packageRef_ = pkgRef;
      this.outFile_ = openFile(pkgControlDir_, "pcd.mvs");

      levelArray = etr_.getLevelArray();

      // level Array not instantiated or not populated
      if (levelArray == null || levelArray.isEmpty())
      {
        throw new GeneratorException(
          "At least one InstallEntity must be specified.");
      }
      obj = levelArray.get(0); //products exist at level one only
      if ( (obj instanceof ArrayList) == false )
      {
        throw new GeneratorException(
          "Product array not encountered at level 0 in level array.");
      }
      productArray = (ArrayList)obj;


      noOfProducts = productArray.size();
      for (int idx = 0; idx < noOfProducts; idx++)
      {
        generateJCLIN = false;
        obj = productArray.get(idx); //products exist at level one only
        if ( (obj instanceof EntityTreeObject) == false )
        {
          throw new GeneratorException(
            "EntityTreeObject type objects expected in product array.");
        }
        curETO = (EntityTreeObject)obj;

        if (!genProduct(curETO))
            throw new GeneratorException(
              "Unable to generate control file 'pcd.mvs'.");

        // Begin JCLIN calls here - one per fmid/product
        if (generateJCLIN)
        {
           JCLINGenerator jclinGenerator = new JCLINGenerator(curETO);
           jclinGenerator.generateJCLIN();
        }
      }
      closeFile(outFile_);
    }
    catch (Exception ex)
    {
       throw new GeneratorException(
         "MVSGenerator: Exception encountered. The message is as follows:\n" +
         ex.getMessage());
    }
  } //end method

/**
 * This method generates one product at a time.
 * Private MVSGenerator method.
 *
 * @param productETO a valid EntityTreeObject instance which is expected to
 *        hold a reference to the product InstallEntity.
 *
 * @see GeneratorException
 */
private boolean genProduct( EntityTreeObject productETO )
  throws GeneratorException
{
  ArrayList childArray;
  EntityTreeObject childETO;
  Object obj;
  InstallEntity curIE = productETO.getInstallEntityReference();

  ArrayList tempArray;
  String tempStr, value;
  PgSpecialType tempSpc;
  ReqType tempReq;

  String        shipListInfo = null;
  StringBuffer  tempBuffer = new StringBuffer();

  createJCLINLIB = false;

  if  (curIE == null ||
       productETO.getFileEntityReference() != null ||
       productETO.getType() != ParserGeneratorEnumType.INSTALLENTITY)
  {
    throw new GeneratorException("Invalid Product ETO encountered.");
  }

  writeString(outFile_, "package:\n");
  noOfTabs++;

  // the following tags are used to determine if the current
  // product is used to package ipp/vpl so that tag values which
  // are required for these may be enforced as required.
  int ippReqd = NOTREQD;
  int vplReqd = NOTREQD;
  int ptfReqd = NOTREQD;

  tempArray = curIE.getType();
  value = "";

  if (tempArray == null || tempArray.isEmpty())
  {
    throw new GeneratorException("Type not specified for product.");
  }

  for (int i = 0; i < tempArray.size(); i++)
  {
    try
    {
      tempStr = (String)tempArray.get(i);
      value = value + " " + tempStr;
    }
    catch (Exception ex)
    {
      throw new GeneratorException(ex.toString());
    }

    if (tempStr.equalsIgnoreCase("ipp") )
    {
      ippReqd = REQD;
    }
    if (tempStr.equalsIgnoreCase("vpl") )
    {
      vplReqd = REQD;
    }
    if (tempStr.equalsIgnoreCase("ptf") )
    {
      ptfReqd = REQD;
    }

  }//end for

  writeMVS("type", "type", value, "\n", REQD);

  // Remaining Product Tags
  writeMVS("applid", "applid", curIE.getApplid(), "\n", REQD);
  
  tempSpc = curIE.getCopyright();
  if (tempSpc == null ||
      tempSpc.getType() != ParserGeneratorEnumType.FILENAMEDATATYPE)
  {
    if (ippReqd == REQD)
    {
      throw new GeneratorException(
        "Required field - copyright - incorrectly defined or not found.");
    }
  }
  tempStr = tempSpc.getValue();
  writeMVS("copyright", "copyright", tempStr, "\n", ippReqd);

  writeMVS("cpydate", "versionDate" , curIE.getVersionDate(), "\n", ippReqd);

  tempArray = curIE.getDistlibs();
  value = "";

  if (tempArray == null || tempArray.isEmpty())
  {
    throw new GeneratorException("distlibs not specified for product.");
  }

  for (int i = 0; i < tempArray.size(); i++)
  {
    try
    {
      tempStr = (String)tempArray.get(i);
      value = value + "\n" + tempStr;
    }
    catch (Exception ex)
    {
      throw new GeneratorException(ex.toString());
    }
  }//end for
  writeMVS("distlibs", "distlibs", value, "\n", ippReqd);

  writeMVS("fesn", "fesn", curIE.getFesn(), "\n", NOTREQD);
  writeMVS("fmid", "entityId", curIE.getEntityId(), "\n", ippReqd);
  writeMVS("function", "entityName", curIE.getEntityName(), "\n", REQD);

  tempArray = curIE.getFeatureFMIDs();
  value = "";

  if (tempArray != null)
  {
    for (int i = 0; i < tempArray.size(); i++)
    {
      tempStr = (String)tempArray.get(i);
      value = value + " " + tempStr;
    }//end for

    writeMVS("featureFmids", "featureFmids", value, "\n", NOTREQD);
  }

  if (ippReqd == REQD)
  {
    if (curIE.getDescription() == null)
      throw new GeneratorException("Required field - description - not found.");
    else if (((curIE.getDescription()).getValue()).length() > 64)
      throw new GeneratorException(
        "Description must not be more than 64 characters long.");
  }
  writeMVS("description", "description", curIE.getDescription().getValue(),
              "\n", ippReqd);

  writeMVS("delete", "delete", curIE.getDelete(), "\n", NOTREQD);
  writeMVS("future", "future", curIE.getFuture(), "\n", NOTREQD);
  writeMVS("previous", "previous", curIE.getPrevious(), "\n", NOTREQD);
  writeMVS("versionReq", "versionReq", curIE.getVersionReq(), "\n", NOTREQD);
  writeMVS("jclinLib", "jclinLib", curIE.getJclinLib(), "\n", NOTREQD);
  writeMVS("dsnHlq", "dsnHlq", curIE.getDsnHlq(), "\n", NOTREQD);
  writeMVS("lkedUnit", "lkedUnit", curIE.getLkedUnit(), "\n", NOTREQD);

  if (curIE.getRework() == null)
  {
    Calendar cal = Calendar.getInstance();
    year = new Integer( cal.get(Calendar.YEAR) );
    dofy = new Integer( cal.get(Calendar.DAY_OF_YEAR) );

    // if the day of year is less than 3 digits long, stuff leading 0's
    // so that the whole date is 7 digits long
    if (dofy.toString().length() == 3)
      tempStr = "";
    else if (dofy.toString().length() == 2)
      tempStr = "0";
    else if (dofy.toString().length() == 1)
      tempStr = "00";
    writeMVS("rework", "rework", (year.toString() + tempStr + dofy.toString()),
             "\n", NOTREQD);
  }
  else
  {
    if (curIE.getRework().length() != 7)
      throw new GeneratorException("rework must be 7 digits long.");
    for (int j = 0; j < 7; j++)
      if (!Character.isDigit(curIE.getRework().charAt(j)))
        throw new GeneratorException("Invalid value " +
          curIE.getRework().charAt(j) +
          " at position " + j + " in rework");

    dofy = new Integer(curIE.getRework().substring(4, 7));
    if (dofy.intValue() == 0 || dofy.intValue() > 365)
      throw new GeneratorException(
        "The last 3 digit part of rework must be greater than 0 and " +
        "less than 366.");
    writeMVS("rework", "rework", curIE.getRework(), "\n", NOTREQD);
  }

  if (curIE.getCtldefinFile() != null && curIE.getExtraSmpeFile() != null)
  {
     if (ctldef_and_extraSmpe == true)
     {
        Interface.printWarning(
          "Found CTLDEFINFILE and EXTRASMPEFILE in the CMF file!!!\n" +
          "            Only the EXTRASMPE file will be used. " );

        ctldef_and_extraSmpe = false;
     }

     tempStr = curIE.getExtraSmpeFile();
  }
  else if (curIE.getCtldefinFile() != null)
          tempStr = curIE.getCtldefinFile();
  else
    tempStr = curIE.getExtraSmpeFile();

  writeMVS("extraSmpeFile", "extraSmpeFile" , tempStr, "\n", NOTREQD);

  writeMVS("srel", "srel", curIE.getSrel(), "\n", ippReqd);

  tempArray = curIE.getSep();
  value = "";

  if (tempArray != null)
  {
    if (tempArray.isEmpty() == false)
    {
      for (int i = 0; i < tempArray.size(); i++)
      {
        try
        {
          tempStr = (String)tempArray.get(i);
          value = value + " " + tempStr;
        }
        catch (Exception ex)
        {
          throw new GeneratorException(ex.toString());
        }
      }//end for
      writeMVS("sep", "sep", value, "\n", NOTREQD);
    }
  }

  tempArray = curIE.getRequisites();
  if (tempArray != null)
  {
    if (tempArray.isEmpty() == false)
    {
      for (int i = 0; i < tempArray.size(); i++)
      {
        obj = tempArray.get(i);
        if (obj instanceof ReqType)
        {
          tempReq = (ReqType)obj;
          tempStr = tempReq.getType();
          tempStr = tempStr.trim();
          if (tempStr.length() > 0)
          {
            if (tempStr.equalsIgnoreCase("P") || tempStr.equalsIgnoreCase("Pre"))
              tempStr = "PRE" ; //prerequisite
            else if (tempStr.equalsIgnoreCase("S") || tempStr.equalsIgnoreCase("Sup"))
              tempStr = "SUP" ;  //supercedes requisite
            else if (tempStr.equalsIgnoreCase("C") || tempStr.equalsIgnoreCase("Co"))
              tempStr = "REQ" ; //co-req - interprete as req for MVS
            else if (tempStr.equalsIgnoreCase("X") || tempStr.equalsIgnoreCase("Neg"))
              tempStr = "NPRE" ; //x-req - interprete as neg. prereq
            else if (tempStr.equalsIgnoreCase("I") || tempStr.equalsIgnoreCase("If"))
              tempStr = "IF" ; //if-req - used for ++IF MCS
            else if (tempStr.equalsIgnoreCase("D") || tempStr.equalsIgnoreCase("Del"))
              tempStr = "DEL" ;  //delete - used for ++DELETE MCS
            else
              throw new GeneratorException("Invalid requistite type specified"
                + ".\nValid values are 'P' or 'Pre', 'S' or 'Sup', 'C' or 'Co'"
                + " 'X' or 'Neg', 'I' or 'If' and 'D' or 'Del'.\n");

            //else write whatever is passed on by user.
            if (tempStr.equals("DEL"))
            {
              if (tempReq.getDescription() != null )
              {
                writeMVS(tempStr, "requisites", tempReq.getValue() +
                         formatDelete(tempReq.getDescription()), "\n", NOTREQD);
              }
              else
                writeMVS(tempStr, "requisites", tempReq.getValue() + " all", "\n", NOTREQD);
            }
            else
              writeMVS(tempStr, "requisites", tempReq.getValue(), "\n", NOTREQD);
          }//end if tempStr != null
        }
      }
    }
  }

  //generate vpl tags
  if (vplReqd == REQD) //indicates vpl product is needed
  {
    writeMVS("vplver", "version", curIE.getVersion(), "\n", vplReqd);
    writeMVS("vplrel", "release", curIE.getRelease(), "\n", vplReqd);
    writeMVS("vplmod", "maintLevel", curIE.getMaintLevel(), "\n", vplReqd);
    writeMVS("vplfromsys", "vplFromSys", curIE.getVplFromSys(), "\n", vplReqd);
    writeMVS("vplauthcode", "vplAuthCode", curIE.getVplAuthCode(), "\n", vplReqd);
    writeMVS("vpl", "createVpl", curIE.getCreateVpl(), "\n", NOTREQD);
    writeMVS("vplackn", "vplAckn", curIE.getVplAckn(), "\n", NOTREQD);
    writeMVS("vplavaildate", "vplAvailDate", curIE.getVplAvailDate(),"\n",NOTREQD);
  }//end generating vpl product tags

  // ServiceInfo stanza tags
  if (this.pkgClass_.equalsIgnoreCase("SP") || 
      this.pkgClass_.equalsIgnoreCase("ST"))      
  {
    tempStr = curIE.getRetainRelease();
    writeMVS("retainRelease", "retainRelease", tempStr, "\n", REQD);
    tempStr = curIE.getRetainComponent();
    writeMVS("retainComponent", "retainComponent",  tempStr, "\n", REQD);
    tempStr = curIE.getRetainChangeTeam();
    writeMVS("changeTeam", "retainChangeTeam",  tempStr, "\n", REQD);  
  }
  else
  {
    tempStr = curIE.getRetainRelease();
    writeMVS("retainRelease", "retainRelease", tempStr, "\n", NOTREQD);
    tempStr = curIE.getRetainComponent();
    writeMVS("retainComponent", "retainComponent",  tempStr, "\n", NOTREQD);
    tempStr = curIE.getRetainChangeTeam();
    writeMVS("changeTeam", "retainChangeTeam",  tempStr, "\n", NOTREQD);  
  } // end pkgClass

  childArray = productETO.getChildReferenceArray();

  if (childArray == null || childArray.isEmpty())
  {
    throw new GeneratorException(
      "At least one File Entity must be a child of every Install Entity.");
  }

  for (int i = 0; i < childArray.size(); i++)
  {
    obj = childArray.get(i);
    if ( (obj instanceof EntityTreeObject) == false)
    {
      throw new GeneratorException(
        "Instances of type EntityTreeObject expected in childArray in " +
        " product ETO.");
    }
    childETO = (EntityTreeObject)obj;
    shipListInfo = genFile(childETO);

    if (shipListInfo != null)
       tempBuffer.append(shipListInfo);
    else
    {
      throw new GeneratorException(
        "Instance of File Entity expected but not found.");
    }

  }

  if (createJCLINLIB)
    writeString(outFile_, "<createJclinLib> yes\n");
  else
    writeString(outFile_, "<createJclinLib> no\n");

  noOfTabs--;

  writeString(outFile_, "ship_list:\n");

  noOfTabs++;

  // write out ship list;
  if (tempBuffer.toString().length() > 0 )
  {
     writeString(outFile_, tempBuffer.toString());
  }

  noOfTabs--; //for files
  noOfTabs--; //for package

  writeString(outFile_, "end_package:\n");
  return true;
}//end method genProduct

/**
 * This method formats SYSLIB information for MCS ++DELETE statement
 * Delete Requisite format from cmf: "D" "filename" "syslib:lib1,lib2,alias:a1"
 * Private MVS Generator method.
 *
 * @param syslibInfo  Input String with syslib/alias information
 *                    example:  syslib:lib1,lib2,alias:a1
 * @return outString  Output String in syslib/alias format
 *                    example:  " lib1,lib2 a1"
 *
 * @exception GeneratorException if an invalid format occurs
 */
private String formatDelete( String syslibInfo ) throws GeneratorException
{
  String attribStr;
  String aliasStr  = "";
  String syslibStr = "";
  String outString = "";
  int aliasPosition;
  int syslibPosition;
  boolean foundString = false;
  boolean syslibFound = false;
  boolean aliasFound  = false;

  attribStr = syslibInfo.trim().toLowerCase();

  if (attribStr.startsWith("syslib:"))
  {
    syslibPosition = attribStr.indexOf(":");

    foundString = true;
    syslibFound = true;

    if ((aliasPosition = attribStr.indexOf("alias:")) != -1 )
    {
      aliasFound = true;

      syslibStr = attribStr.substring((syslibPosition + 1), aliasPosition);

      // check for comma separator between syslib list and alias
      if ((syslibStr.endsWith(",")) == false)
         throw new GeneratorException(REQUISITE_DELETE_FORMAT_ERROR +
                                      " A comma is missing before keyword \"alias\".");

      aliasStr  = attribStr.substring(aliasPosition + 6);
    }
    else if (attribStr.indexOf("alias") != -1 )
      throw new GeneratorException(REQUISITE_DELETE_FORMAT_ERROR +
                                   " A colon is missing after keyword \"alias\".");
    else  // no alias string , only syslib
      syslibStr = attribStr.substring(syslibPosition + 1);

  }
  else if (attribStr.startsWith("alias:"))
  {
    aliasPosition  = attribStr.indexOf(":");

    foundString = true;
    aliasFound  = true;

    if ((syslibPosition = attribStr.indexOf("syslib:")) != -1)
    {
      syslibFound = true;

      aliasStr  = attribStr.substring((aliasPosition + 1), syslibPosition);

      // check for comma separator between alias list and syslib
      if ((aliasStr.endsWith(",")) == false)
         throw new GeneratorException(REQUISITE_DELETE_FORMAT_ERROR +
                                      " A comma is missing before keyword \"syslib\".");

      syslibStr = attribStr.substring(syslibPosition + 7);
    }
    else if (attribStr.indexOf("syslib") != -1)
       throw new GeneratorException(REQUISITE_DELETE_FORMAT_ERROR +
                                    " A colon is missing after keyword \"syslib\".");

    else // no syslib string , only alias
       aliasStr  = attribStr.substring(aliasPosition + 1);
  }
  else
     throw new GeneratorException(REQUISITE_DELETE_FORMAT_ERROR);

  // Validation Check
  if (foundString)
  {
    if (syslibFound)
    {
      // Check syslib for null string or string ending with comma
      if (syslibStr.endsWith(",")) // strip trailing comma
         syslibStr = syslibStr.substring(0, syslibStr.length() - 1);

      if ((syslibStr == null) || (syslibStr.trim().length() == 0))
         syslibStr = "all";

    }
    else  // set syslib to "all"
       syslibStr = "all";

    if (aliasFound)
    {
      // Check alias for null string or string ending with comma
      if (aliasStr.endsWith(",")) // strip trailing comma
         aliasStr = aliasStr.substring(0, aliasStr.length() - 1);

      if ((aliasStr == null) || (aliasStr.trim().length() == 0))
         outString = " " + syslibStr;
      else
         outString = " " + syslibStr + " " + aliasStr;
    }
    else
      outString = " " + syslibStr;
  }

  return outString;
}

/**
 * This method generates information about one file stanza
 * Private MVS Generator method.
 *
 * @param fileETO EntityTreeObject holding reference to a valid file entity
 * @return tempBuffer  Information from ship list
 *
 * @see GeneratorException
 */
private  String genFile( EntityTreeObject fileETO )
  throws GeneratorException
{
  FileEntity curFE = fileETO.getFileEntityReference();

  String value, tempStr, sep;
  boolean isBinaryOrText = false;
  ReqType tempReq;
  int ippReqd = NOTREQD;
  int vplReqd = NOTREQD;
  int ptfReqd = NOTREQD;

  String        createdStr;
  StringBuffer  tempBuffer = new StringBuffer();

  if (curFE == null ||
      fileETO.getInstallEntityReference() != null ||
      fileETO.getType() != ParserGeneratorEnumType.FILE)
  {
    throw new GeneratorException(
      "Instance of File Entity expected but not found.");
  }

  ArrayList tempArray = fileETO.getChildReferenceArray();
  if (tempArray != null )
  {
    if (tempArray.isEmpty() == false)
    {
      throw new GeneratorException(
        "Child Array of ETO referencing File Entity expected to be null.");
    }
  }

  //Iterate through multiple PackageData stanzas

  ArrayList pDataArray = curFE.getPackageData();
  ListIterator pDataIterator;

  if (pDataArray != null)
  {
    if (pDataArray.isEmpty() == false )
    {
      pDataIterator = pDataArray.listIterator();
      while ( pDataIterator.hasNext() )
      {
        PackageData curPD  = (PackageData)pDataIterator.next();
        boolean genLkedParms = false;
        boolean isPdsPart = false;

        String srcfile = curFE.getSourceFile();
        if ( (srcfile != null) && (srcfile.indexOf("(") != -1))
          isPdsPart = true;

        value = curFE.getSourceDir();
        if (value != null)
        {
          if (!isPdsPart)
          {
            //if last character is not a '/' , append one at end
            if ( value.charAt( value.length() - 1 ) != '/' )
            {
              value = value+"/";
            }
          }
          else
          {
            // remove any trailing slash for PDS parts
            if ( value.charAt( value.length() - 1 ) == '/' )
            {
              value = value.substring( 0, value.length()-1 );
            }
          }
        }
        value = value + srcfile;
        if (value == null)
        {
          throw new GeneratorException(
            "SourceDir + SourceFile cannot be null - It forms a required field.");
        }

        value = value + " " + "NONE" + " " ; //standard value required
        tempBuffer.append(value); //writeString used to avoid < .. >

        tempArray = curPD.getShipType();
        if (tempArray == null || tempArray.isEmpty())
        {
          throw new GeneratorException(
            "Missing required field shipType in file stanza.");
        }
        value = "";

        for (int i = 0; i < tempArray.size(); i++)
        {
          tempStr = (String)tempArray.get(i);
          value = value + " " + tempStr;

          if (tempStr.equalsIgnoreCase("vpl") )
          {
            vplReqd = REQD;
          }
          else if (tempStr.equalsIgnoreCase("ipp") )
          {
            ippReqd = REQD;
          }
          else if (tempStr.equalsIgnoreCase("ptf") )
          {
            ptfReqd = REQD;
          }
        }
        if ( ( (ippReqd == REQD) || (ptfReqd == REQD) )
                && (vplReqd == REQD) )
        {
          throw new GeneratorException(
            "Ship type in any file Stanza cannot be of ipp or ptf type " +
            "if vpl is specified as a shiptype simultaneously.");
        }
        createdStr = createTagStr("type", "shipType", value, " ", REQD);
        if ( createdStr != null )
          tempBuffer.append ( createdStr );

        tempStr = curPD.getTargetDir();
        if (tempStr != null)
        {
          //check if last char is a '/' - if so, strip it
          if (tempStr.charAt(tempStr.length() - 1) == '/')
          {
            tempStr = tempStr.substring(0, tempStr.length() - 1) ;
          }
          createdStr = createTagStr("distlib", "targetDir", tempStr, " ", ippReqd);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
        }
        else
        {
          if (ippReqd == REQD)
          {
            throw new GeneratorException(
              "Required field targetDir missing for ipp file stanza.\n" +
              "Add the distLib or targetDir attribute in the CMF file");
          }
        }

        // This field is optional for IPP and SP.
        createdStr = createTagStr("hfscopytype", "hfsCopyType", curPD.getHfsCopyType(), " ", NOTREQD);
        if ( createdStr != null )
          tempBuffer.append ( createdStr );

        tempStr = curPD.getTargetFile();
        if (vplReqd == REQD || ptfReqd == REQD)
        {
          createdStr = createTagStr("distname", "targetFile", tempStr, " ", REQD); //reqd for ipp&vpl
          if ( createdStr != null )
            tempBuffer.append ( createdStr );

          createdStr = createTagStr("parttype", "fileType", curPD.getFileType(), " ", ptfReqd);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
        }
        else
        {
          createdStr = createTagStr("distname", "targetFile", tempStr, " ", ippReqd);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );

          createdStr = createTagStr("parttype", "fileType", curPD.getFileType(), " ", ippReqd);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
        }

        tempArray = curPD.getPartInfo();
        if (tempArray != null)
        {
          if (tempArray.isEmpty() == false)
          {
            for (int i = 0; i < tempArray.size(); i++)
            {
              tempReq = (ReqType)tempArray.get(i);
              tempStr = tempReq.getType();
              if (tempStr == null)
              {
                throw new GeneratorException(
                  "First value in partInfo in file stanza is a required field.");
              }
              if (tempStr.equals("binary") || tempStr.equals("text"))
                isBinaryOrText = true;
              tempBuffer.append ( "<"+tempStr+"> ");

              tempStr = tempReq.getValue();
              if (tempStr != null)
              {
                tempBuffer.append( tempStr+" " );
              }
              else
              {
                tempBuffer.append( " " );
              }

              // if partInfo is not defined as binary or text then use
              // the value of hfsCopyType if part is an HFS file
              // Also ensure hfsCopyType is binary or text
              if (MvsValidation.isHfsPartType( curPD.getFileType() ))
              {
                if (i == (tempArray.size() - 1) && !isBinaryOrText &&
                    curPD.getHfsCopyType() != null)
                    if ( curPD.getHfsCopyType().equalsIgnoreCase( "text" ) ||
                         curPD.getHfsCopyType().equalsIgnoreCase( "binary" ))
                      tempBuffer.append("<"+curPD.getHfsCopyType()+"> ");

              }
            }
          }
        }

        createdStr = createTagStr("vplsecurity", "vplSecurity",
                                  curPD.getVplSecurity(), " ", vplReqd);
        if ( createdStr != null )
          tempBuffer.append ( createdStr );

        createdStr = createTagStr("vplpartqual", "vplPartqual",
                                  curPD.getVplPartqual(), " ", NOTREQD);
        if ( createdStr != null )
          tempBuffer.append ( createdStr );

        createdStr = createTagStr("permissions", "permissions",
                                  curPD.getPermissions()," ", NOTREQD);
        if ( createdStr != null )
          tempBuffer.append ( createdStr );

        // Write out optional attributes for JCLIN generation
        //   only valid for ++MOD parttypes
        tempStr = curPD.getFileType();

        // has to write lkedParms if the fileType is mod or program
        // we will write it at the end of other tags, so just set the flag now
        if (tempStr != null && (tempStr.equalsIgnoreCase("mod") ||
                                tempStr.equalsIgnoreCase("program")))
          genLkedParms = true;

        if (tempStr != null && tempStr.equalsIgnoreCase("mod"))
        {
          createdStr = createTagStr( "userId", "userId", curPD.getUserId(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr( "groupId", "groupId", curPD.getGroupId(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr( "extAttr", "extAttr", curPD.getExtAttr(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr( "setCode", "setCode", curPD.getSetCode(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr( "entry", "entry", curPD.getEntry(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr("jclinMode", "jclinMode",
                                    curPD.getJclinMode(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr( "lkedTo", "lkedTo", curPD.getLkedTo(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr( "lkedRc", "lkedRc", curPD.getLkedRc(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr( "hfsLkedName", "hfsLkedName", curPD.getHfsLkedName(),
                                     " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr( "pdsLkedName", "pdsLkedName", curPD.getPdsLkedName(),
                    " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr("jclinLkedParms", "jclinLkedParms",
                                    curPD.getJclinLkedParms(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr( "order", "order", curPD.getOrder(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
          createdStr = createTagStr("libraryDD", "libraryDD",
                                    curPD.getLibraryDD(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );

          createdStr = createTagStr("sideDeckAppendDD", "sideDeckAppendDD",
                                    curPD.getSideDeckAppendDD(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );

          // process Array values, hfsAlias
          tempArray = curPD.getHfsAlias();
          if (tempArray != null && !tempArray.isEmpty())
          {
            value = "";
            sep   = "";
            for (int i = 0; i < tempArray.size(); i++)
            {
              try
              {
                tempStr = (String)tempArray.get(i);
                value += sep + tempStr;
                sep = ",";
              }
              catch (Exception ex)
              {
                throw new GeneratorException(ex.toString());
              }
            }//end for
            createdStr = createTagStr("hfsAlias", "hfsAlias", value, " ", NOTREQD);
            if ( createdStr != null )
              tempBuffer.append ( createdStr );
          }
          // pdsAlias
          tempArray = curPD.getPdsAlias();
          if (tempArray != null && !tempArray.isEmpty())
          {
            value = "";
            sep   = "";
            for (int i = 0; i < tempArray.size(); i++)
            {
              try
              {
                tempStr = (String)tempArray.get(i);
                value += sep + tempStr;
                sep = ",";
              }
              catch (Exception ex)
              {
                throw new GeneratorException(ex.toString());
              }
            }//end for
            createdStr = createTagStr("pdsAlias", "pdsAlias", value, " ", NOTREQD);
            if ( createdStr != null )
              tempBuffer.append ( createdStr );
          }
          // includes
          tempArray = curPD.getInclude();
          if (tempArray != null && !tempArray.isEmpty())
          {
            value = "";
            sep   = "";
            for (int i = 0; i < tempArray.size(); i++)
            {
              try
              {
                tempStr = (String)tempArray.get(i);
                value += sep + tempStr;
                sep = ",";
              }
              catch (Exception ex)
              {
                throw new GeneratorException(ex.toString());
              }
            }//end for
            createdStr = createTagStr("include", "include", value, " ", NOTREQD);
            if ( createdStr != null )
              tempBuffer.append ( createdStr );
          }
          // syslibs
          tempArray = curPD.getSysLibs();
          if (tempArray != null && !tempArray.isEmpty())
          {
            value = "";
            sep   = "";
            for (int i = 0; i < tempArray.size(); i++)
            {
              try
              {
                tempStr = (String)tempArray.get(i);
                value += sep + tempStr;
                sep = ",";
              }
              catch (Exception ex)
              {
                throw new GeneratorException(ex.toString());
              }
            }//end for
            createdStr = createTagStr("sysLibs", "sysLibs", value, " ", NOTREQD);
            if ( createdStr != null )
              tempBuffer.append ( createdStr );
          }

          // The parameter lkedTo in a ++MOD part signifies we need
          // to process JCLIN Generation for this FMID.
          if (!generateJCLIN && curPD.getLkedTo() != null)
          {
            generateJCLIN = true;

            if (curPD.getJclinLkedParms() != null)
              createJCLINLIB = true;
          }
        }
        // write lkedParms
        if (genLkedParms)
        {
          createdStr = createTagStr( "lkedParms", "lkedParms",
                                    curPD.getLkedParms(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
            
          createdStr = createTagStr( "lkedCond", "lkedCond", curPD.getLkedCond(), " ", NOTREQD);
          if ( createdStr != null )
            tempBuffer.append ( createdStr );
        }
        
        
            
        tempBuffer.append( "  " );
        tempBuffer.append( "\n" );
        
      } //end for
    } //end if
  }

  if ( tempBuffer.toString().length() > 0)
    return tempBuffer.toString();
  else
    return null;

} //end method genFile

/**
  * create an <tag>: <value> pair according to format required by
  * the pcd.mvs file.
  *
  * @param attribname :- attribute name
  * @param value :- String value to be formatted
  * @param endstring :- blank space
  * @param required  :- to indicate whether this is a required field or not.
  * @return String :- formatted String
  * @exception GeneratorException :- if a required field is found to be missing
  **/
private String createTagStr( String attribname,
                     String cmfAttribName,
                     String value,
                     String endString,
                     int required )
throws GeneratorException
{
  String createdStr = "";
  if( value == null && required == REQD )
  {
    throw new GeneratorException("MVSGenerator :\n"
    + "The CMF attribute " + cmfAttribName
    + "  < " + attribname + " > " + REQD_FIELD_ERROR + "\n");
  }

  if( value != null )
  {
    createdStr = "<"+attribname+">"+" "+value+endString;
    return createdStr;
  }
  else
    return null;
}

/**
 * This is used to form the output string format using parameters passed to it.
 * It calls the writeString method of its superclass with this format string.
 *
 * @param attribname Name of attribute/stanza to be written to file
 * @param value Value of attribute to be written to file
 * @param required integer value specifying whether this attribute is required or not.
 *
 * @exception GeneratorException if a required field is found to be missing or if an
 * error occurs in writing to file on disk
 *
 * @ see Generator#writeString
 */
private void writeMVS( String attribname,
                       String cmfAttribName,
                       String value,
                       String endString,
                       int required )
  throws GeneratorException
{
  if (value == null)
  {
    if (required == REQD)
    {
      throw new GeneratorException("The CMF attribute " + cmfAttribName
                                   + "  < " + attribname + " > " + REQD_FIELD_ERROR);

    }
  }
  else
  {
    writeString(outFile_, "<"+attribname+">"+" "+value+endString);
  }
}//end method writeMVS
}  //end class definition
