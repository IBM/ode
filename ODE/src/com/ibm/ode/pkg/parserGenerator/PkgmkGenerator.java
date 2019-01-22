/*******************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 5/10/03 15:28:40
 * Date and Time File was extracted/checked out: 06/04/13 16:46:01
 ******************************************************************************/
package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;
import com.ibm.ode.lib.io.*;
import com.ibm.ode.pkg.pkgCommon.*;

/**
 * This class is responsible for extracting appropriate information from the
 * installEntities and FileEntities which are present in the EntityTree at
 * appropriate levels. It writes out the extracted information to certain files
 * which will be the control files that act as input to the packaging tool.
 * The packaging tool considered here is <I> pkgmk </I>.
 *
 * @author
 * @see com.ibm.ode.pkg.parserGenerator.Generator
 * @see com.ibm.ode.pkg.parserGenerator.GeneratorInterface
 */
public class PkgmkGenerator extends Generator
  implements GeneratorInterface
{
  /**
   * Some static final variables which will be used in generating error
   * and formatted strings
   */
  static final private String REQD_FIELD_ERROR =
    " is a required attribute for generating the package.";

  static final private String LEVEL_ARRAY_ERROR =
    "The information at the given index is not available. Please check if the" +
    " Link information is specified correctly";

  static final private int REQD     = 10;
  static final private int NOT_REQD = 11;

  /**
   * File output handle for writing the pkginfo file
   */
  private FileOutputStream pkginfo_;

  /**
   * File output handle for witing the pcd.proto file ( main control file )
   */
  private FileOutputStream prototype_;

  /**
   * This will hold the reference to the array of EntityTreeObjects
   * at level One. These ETO's represent the product info in the
   * package heirarchy for Sun Solaris. For sun there should be
   * only one ETO at this level since Sun doesn't support multiple
   * products in a package
   */
  private ArrayList levelOneArray_;

  /**
   * Variable to hold refrence of a installEntity
   */
  private InstallEntity ieRef_;

  /**
   * Constructor
   *
   * @param shipRootDir Env Var => root dir. of the ship tree.
   * @param context Env Var => Context
   * @param pkgControlDir Env Var => Output directory
   * @param pkgType
   * @param pkgClass
   * @param pkgFixStrategy
   */
  public PkgmkGenerator( String shipRootDir,
                         String context,
                         String pkgControlDir,
                         String pkgType,
                         String pkgClass,
                         String pkgFixStrategy )
  {
    super(shipRootDir,
          context,
          pkgControlDir,
          pkgType,
          pkgClass,
          pkgFixStrategy);
  }

  /**
   * Generates the Metadata Files for a particular platform and packaging tool
   *
   * @param entityTreeRoot refernce to the object which holds the structured IE
   * @param packageObject  refernce to the object which contain the IE's and FE's
   * @exception GeneratorException if any error is encountered
   */
  public void generateTargetMetadataFiles( EntityTreeRoot entityTreeRoot,
                                           Package packageObject )
    throws GeneratorException
  {
    ArrayList levelArray = entityTreeRoot.getLevelArray();

    // get the level one array which contains entities containing product
    // definition
    try
    {
      levelOneArray_ = (ArrayList)levelArray.get( 0 );
      if (levelOneArray_.size() > 1)
      {
         throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
           "Only one InstallEntity representing the product information with" +
           " parent\nfield NULL should be present.");
      }
    }
    catch (IndexOutOfBoundsException ex)
    {
       throw new GeneratorException("GeneratorException : PkgmkGenerator\n"
                                    + "Level 1 : " + LEVEL_ARRAY_ERROR);
    }
    try
    {
      EntityTreeObject entityTreeObjRef =
        (EntityTreeObject)levelOneArray_.get( 0 );
      if (entityTreeObjRef == null)
      {
        throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
          "Unable to obtain the Entity Reference. No product definition" +
          " found in First Level.");
      }
      // once you get a valid installEntity, start writing out to the file
      // as required by the packaging tool
      ieRef_ = entityTreeObjRef.getInstallEntityReference();
      if (ieRef_ == null)
      {
        throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
          "Unable to obtain the InstallEntity Reference. No product" +
          " definition has been given.");
      }

      // first create the main Control File in the pkgControlDir
      // as specified in the ode pass. The name of the file is prototype
      // which is the name give for the sun control file in ode
      prototype_ = openFile(pkgControlDir_, "prototype");
      pkginfo_   = openFile(pkgControlDir_, "pkginfo");

      // populate pkginfo file
      generatePkginfoFile();

      String copyrightFileLocation = this.pkgControlDir_;

      // If in Initial Product packaging. There is no need to create entries
      // of type "i" in prototype file for service packaging.
      if (this.pkgClass_.equalsIgnoreCase(PackageConstants.IPP_PKG_CLASS))
      {
        // These are the info files and their path need to be written
        // to the prototype file : format is : i   fileName
        writeString(prototype_, "i        pkginfo\n");

        // Prereq - must already be installed.
        // Reverse - Another package requires this package but cant prereq it.
        // Conflicts - can not be installed or the package won't work.
        ArrayList requisites = ieRef_.getRequisites();
        if (requisites != null)
        {
          // create and populate depend & compver files
          generateCompverAndDependFiles(requisites);
        }

        // Create and populate space file
        if (generateSpaceFile())
          writeString(prototype_, "i        space\n");
      }
      else // if service packaging
      {
        generateDeletesFile();

        // In service packaging using buildpatch, reguires the copyright
        // file should be created in the install directory of control directory
        String fs = System.getProperty("file.separator");
        copyrightFileLocation += fs + "install" + fs;

        // create the 'install' directory if it does not exist
        File file = new File(copyrightFileLocation);
        if (!file.exists())
          file.mkdir();
      }

      // Create and populate copyright file
      if (generateCopyrightFile(copyrightFileLocation) &&
          this.pkgClass_.equalsIgnoreCase(PackageConstants.IPP_PKG_CLASS))
        writeString(prototype_, "i        copyright\n");

      // constant and config file info
      writeConstantAndConfigFileInfo(ieRef_);

      // populate the prototype file
      generatePrototypeFile(levelArray);
    }
    finally
    {
      // after writing out everything !!! close all the files
      if (prototype_ != null)
        closeFile(pkginfo_);
      if (pkginfo_ != null)
        closeFile(prototype_);
    }
  }

  /**
   * Creates and Populates the pkginfo file.
   *
   * @exception GeneratorException
   */
  protected void generatePkginfoFile()
    throws GeneratorException
  {
    String value;

    // PKG
    String tempString = ieRef_.getEntityName();
    String className = getValue("PKG", "entityName", tempString, REQD);
    writeString(pkginfo_, className);

    // NAME
    ArrayList tempArray = ieRef_.getFullEntityName();
    if (tempArray != null)
    {
      tempString = (String)tempArray.get( 0 );
      value = getValue("NAME", "fullEntityName", tempString, REQD);
    }
    else
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
        "fullEntityName ( NAME ) in InstallEntity is a required field.");
    }

    if (value != null)
      writeString(pkginfo_, value);

    // Add an entry in the pkginfo file for defining the patch number
    String patchNumber = System.getProperty("PATCH_NAME");
    if ((patchNumber == null || patchNumber.length() == 0) &&
        this.pkgClass_.equalsIgnoreCase(PackageConstants.SP_PKG_CLASS))
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
        "Patch number needs to be specified by " +
        "defining the PATCH_NAME makefile variable.");
    }
    if (patchNumber != null && patchNumber.length() > 0)
    {
      if (this.pkgClass_.equalsIgnoreCase(PackageConstants.SP_PKG_CLASS))
        writeString(pkginfo_, "PATCH=" + patchNumber + "\n");
      else
        writeString(pkginfo_, "PATCHLIST=" + patchNumber + "\n");
    }

    // DESC
    PgSpecialType tempSpecialType = ieRef_.getDescription();
    if (tempSpecialType != null)
    {
      // check to see whether it is a file or a string
      tempString = this.getValue(tempSpecialType);
      value = this.getValue("DESC", "description", tempString, NOT_REQD);
      if (value != null)
        writeString(pkginfo_, value);
    }

    // version and release has a slightly different way of writing out

    String version      = ieRef_.getVersion();
    String release      = ieRef_.getRelease();
    String maintLevel   = ieRef_.getMaintLevel();
    String fixLevel     = ieRef_.getFixLevel();

    if ( version == null )
    {
        throw new GeneratorException("GeneratorException : PkgmkGenerator\n"
                           + "version field " + REQD_FIELD_ERROR);
    }

    if (( release != null) && (maintLevel != null) && (fixLevel != null))
    {
        writeString( pkginfo_, "VERSION=" + version + "." +release + "." + maintLevel + "." + fixLevel + "\n" );
    }
    else if ( release != null)
    {
        writeString( pkginfo_, "VERSION=Version " + version + ", Revision " + release + "\n" );
    }
    else if ( release == null )
    {
        writeString( pkginfo_, "VERSION=" + version + "\n" );
    }
    // category
    tempString = ieRef_.getCategory();
    value =  getValue( "CATEGORY", "category", tempString, REQD );
    writeString(pkginfo_, value);

    // VENDOR
    tempString =  ieRef_.getVendorName();
    value = getValue("VENDOR", "vendorName", tempString, NOT_REQD );
    if (value != null)
      writeString( pkginfo_, value );

    // vstock = vendor desc
    tempSpecialType = ieRef_.getVendorDesc();

    // check to see whether it is a file or a string
    if (tempSpecialType != null)
    {
      tempString = this.getValue(tempSpecialType);
      value = this.getValue("VSTOCK", "vendorDesc", tempString, NOT_REQD);
      if (value != null)
          writeString(pkginfo_, value);
    }

    // hotline
    tempArray = ieRef_.getHotline();
    value =  getValue("HOTLINE", "hotline", tempArray, NOT_REQD);
    if (value != null)
      writeString(pkginfo_, value);

    // email
    tempArray =  ieRef_.getEmail();
    value =  getValue("EMAIL", "email", tempArray, NOT_REQD);
    if (value != null)
      writeString(pkginfo_, value);

    // intonly
    tempString =  ieRef_.getInteractive();
    value =  getValue("INTONLY", "interactive", tempString, NOT_REQD);
    if (value != null)
      writeString(pkginfo_, value);

    // pstamp
    tempString =  ieRef_.getMediaId();
    value =  getValue("PSTAMP", "mediaId", tempString, NOT_REQD);
    if (value != null)
      writeString(pkginfo_, value);

    // istates
    tempString =  ieRef_.getInstallStates();
    value =  getValue("ISTATES", "installStates", tempString, NOT_REQD);
    if (value != null)
      writeString(pkginfo_, value);

    // rstates
    tempString =  ieRef_.getRemovableStates();
    value =  getValue("RSTATES", "removableStates", tempString, NOT_REQD);
    if (value != null)
      writeString(pkginfo_, value);

    // maxinst
    tempString =  ieRef_.getMaxInst();
    value =  getValue("MAXINST", "maxInst", tempString, NOT_REQD);
    if (value != null)
      writeString(pkginfo_, value);

    // basedir : have a question here
    tempSpecialType = ieRef_.getInstallDir();
    if (tempSpecialType != null)
    {
      // NOTE : It doen't matter whether the user has specified a
      // fileName or as String . no need to check for type.
      // This attrib has been made special type for HP reasons
      tempString = tempSpecialType.getValue();
      value = getValue("BASEDIR", "installDir", tempString, NOT_REQD);
      if (value != null)
        writeString(pkginfo_, value);
    }

    // ncmpman
    tempSpecialType = ieRef_.getManpage();
    if (tempSpecialType != null)
    {
      // NOTE : It doesn't matter whether the user has specified a
      // fileName or as String . no need to check for type.
      // This attrib has been made special type for HP reasons
      tempString = tempSpecialType.getValue();
      value = getValue("NCMPMAN", "manpage", tempString, NOT_REQD);
      if (value != null)
        writeString(pkginfo_, value);
    }

    // order
    tempArray =  ieRef_.getPackageOrder();
    value =  getValue("ORDER", "packageOrder", tempArray, NOT_REQD);
    if (value != null)
      writeString(pkginfo_, value);

    // ARCH info : Note that this needs to be written out in a diff. way
    // a comma seperated list of osName+osRelease+osVersion.
    // BP D-2321 Check for reqd field error
    ArrayList osNames = ieRef_.getOsName();
    ArrayList osReleases = ieRef_.getOsRelease();
    ArrayList osVersions = ieRef_.getOsVersion();
    if (osNames == null)
    {
      throw new GeneratorException(
        "GeneratorException : PkgmkGenerator \nosName ( ARCH ) ::"
        + REQD_FIELD_ERROR);
    }

    if (( osReleases != null && osNames.size() != osReleases.size() ) ||
        ( osVersions != null && osNames.size() != osVersions.size() ))
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n"
                 + "Inconsistency found while reading "
                 + "osRelease and osVersion. These two attributes,\n"
                 + "if present, must have same array size as osName.");
    }

    writeString(pkginfo_, "ARCH=");
    String osName = "";
    String osRelease = "";
    String osVersion = "";
    for (int index = 0;
         index < osNames.size();
         index++)
    {
      osName = (String)osNames.get(index);
      if (osReleases != null)
      {
        osRelease = (String)osReleases.get(index);
      }
      else
      {
        osRelease = "";
      }

      if (osVersions != null)
      {
        osVersion = (String)osVersions.get(index);
      }
      else
      {
        osVersion = "";
      }

      StringBuffer archStr = new StringBuffer(osName.trim());
      osRelease = osRelease.trim();
      osVersion = osVersion.trim();
      if (osRelease.length() != 0 && osVersion.length() != 0)
      {
        archStr.append(osRelease);
        archStr.append(".");
        archStr.append(osVersion);
      }
      writeString(pkginfo_, archStr.toString());

      if (index == osNames.size() - 1)
        writeString(pkginfo_, "\n");
      else
        writeString(pkginfo_, ",");
    }

    // CLASSES : space separated list
    tempArray =  ieRef_.getImmChildEntities();
    value =  getValue("CLASSES", "immChildEntities", tempArray, REQD);
    writeString(pkginfo_, value);
  }

  /**
   * Creates and populates deletes file. Deletes file will be created in the
   * package control directory.
   *
   * @exception GeneratorException
   */
  protected void generateDeletesFile()
    throws GeneratorException
  {
    String deletesList = System.getProperty("PATCH_DELETED_FILES");
    if (deletesList == null) return;
    StringTokenizer tokenizer = new StringTokenizer(deletesList, ",");
    FileOutputStream deletesStream = null;
    try
    {
      if (tokenizer.hasMoreTokens())
        deletesStream = openFile(this.pkgControlDir_, "deletes");

      while (tokenizer.hasMoreTokens())
      {
        writeString(deletesStream, tokenizer.nextToken());
      }
    }
    finally
    {
      if (deletesStream != null)
        closeFile(deletesStream);
    }
  }

  /**
   * Creates and populates copyright file at the specified location
   *
   * @param location path to the copyright file
   * @return true if a copyright file is created
   * @exception GeneratorException
   */
  protected boolean generateCopyrightFile( String location )
    throws GeneratorException
  {
    FileOutputStream copyrightStream = null;
    try
    {
      // get copyright information from CMF
      PgSpecialType tempSpecialType = ieRef_.getCopyright();
      if (tempSpecialType != null)
      {
        String copyrightStr = this.getValue(tempSpecialType);
        if (copyrightStr != null)
        {
          copyrightStream = openFile(location, "copyright");
          writeString(copyrightStream, copyrightStr);
          return true;
        }
      }
      return false;
    }
    finally
    {
      if (copyrightStream != null)
        closeFile(copyrightStream);
    }
  }

  /**
   * Creates and populates space file.
   *
   * @return true if space file is created
   * @exception GeneratorException
   */
  protected boolean generateSpaceFile()
    throws GeneratorException
  {
    FileOutputStream spaceStream = null;
    try
    {
      // space info
      PgSpecialType tempSpecialType = ieRef_.getInstallSpace();
      if (tempSpecialType != null)
      {
        String spaceStr = this.getValue(tempSpecialType);
        if (spaceStr != null)
        {
          spaceStream = openFile(pkgControlDir_, "space");
          writeString(spaceStream, spaceStr);
          return true;
        }
      }
      return false;
    }
    finally
    {
      if (spaceStream != null)
        closeFile(spaceStream);
    }
  }

  /**
   * Creates and populates compver and depend files. This method also adds
   * an entry in the prototype file for the compver and depend files.
   *
   * @param requisites Array containing requisites info
   * @exception GeneratorException
   */
  protected void generateCompverAndDependFiles( ArrayList requisites )
    throws GeneratorException
  {
    ArrayList tmpArray;
    ReqType reqType;
    FileOutputStream dependStream = null, compverStream = null;
    String tmpStr = "", tmpType = "", tmpFileName = "", type, value, desc;
    StringBuffer tmpBuffer = new StringBuffer();

    try
    {
      int rc = this.validateRequisites(requisites);
      if (rc == 0 || rc == 2)
      {
        dependStream = openFile(pkgControlDir_, "depend");
        tmpArray  =  ieRef_.getFullEntityName();
        tmpStr = (String)tmpArray.get( 0 );
        tmpStr = "#ident \"@(#)" + tmpStr + ":depend 1.1\"\n";
        writeString(prototype_, "i        depend\n");
        writeString(dependStream, tmpStr);
      }
      if (rc == 1 || rc == 2)
      {
        writeString(prototype_, "i        compver\n");
        compverStream = openFile(pkgControlDir_ , "compver");
      }

      String reverse = "" , conflicts = "", prereq = "", compver = "";
      ListIterator i = requisites.listIterator();
      while (i.hasNext())
      {
        tmpBuffer.append(" ");
        reqType = (ReqType)i.next();
        type = reqType.getType().trim();
        value = reqType.getValue();
        desc = reqType.getDescription();

        if (value == null)
          throw new GeneratorException(reqType +
                      "is an improperly formated requisite.");

        // join value and desc for all types other than "S" or "Sup" as
        // we want to consider only the first parameter for them
        if (!(type.equalsIgnoreCase("S") ||
              type.equalsIgnoreCase("Sup")))
        {
          if (value != null && desc != null)
            value = value + " " + desc;
        }

        if (type.equalsIgnoreCase("P") ||
            type.equalsIgnoreCase("Pre"))
        {
          prereq = "P   " + value + "\n" ;
          writeString(dependStream, prereq);
        }
        else if (type.equalsIgnoreCase("X") ||
                 type.equalsIgnoreCase("Neg"))
        {
          conflicts = "I   " + value + "\n" ;
          writeString(dependStream, conflicts);
        }
        else if (type.equalsIgnoreCase("R") ||
                 type.equalsIgnoreCase("Rev"))
        {
          reverse = "R   " + value + "\n" ;
          writeString(dependStream, reverse);
        }
        else if (type.equalsIgnoreCase("S") ||
                 type.equalsIgnoreCase("Sup"))
        {
          compver = "Version " + value + "\n" ;
          writeString(compverStream, compver);
        }
        else
        {
          // should not have been here
        }
      }
    }
    finally
    {
      if (compverStream != null)
        closeFile(compverStream);
      if (dependStream != null)
        closeFile(dependStream);
    }
  }

  /**
   * Validates the requisites. If an invalid requisite is found,
   * GeneratorException is thrown. It also returns an int value based on type of
   * requisites found in the specified requisites array.
   *
   * @param requisites an Array of requisites.
   * @return 2 if requisites of type Sup/S and (Pre/P or Rev/R or Neg/X) are found
   *         0 if only requisites of type Pre/P or Rev/R or Neg/X are found
   *         1 if only requisites of type Sup/S are found
   * @exception GeneratorException is thrown if an invalid requisite is found.
   */
  private int validateRequisites( ArrayList requisites )
    throws GeneratorException
  {
    boolean preRevNeg = false;
    boolean sup = false;
    ReqType reqType;
    String type;

    ListIterator i = requisites.listIterator();
    while ( i.hasNext() )
    {
      Object tmpvalue = (Object)i.next();
      if (tmpvalue.getClass().equals(ReqType.class))
      {
        reqType = (ReqType)tmpvalue;
        type = (String)reqType.getType();
        if (type != null)
        {
          type = type.trim();
          if (type.equalsIgnoreCase("P") || type.equalsIgnoreCase("Pre") ||
              type.equalsIgnoreCase("R") || type.equalsIgnoreCase("Rev") ||
              type.equalsIgnoreCase("X") || type.equalsIgnoreCase("Neg"))
          {
            preRevNeg = true;
          }
          else if (type.equalsIgnoreCase("S") || type.equalsIgnoreCase("Sup"))
          {
            sup = true;
          }
          else
          {
            throw new GeneratorException("GeneratorException : PkgmkGenerator :\n"
              + type.trim() + " is an invalid requisite type.\n" +
              "Valid types are 'P' or 'Pre', 'R' or 'Rev', 'S' or 'Sup'" +
              " and 'X' or 'Neg'");
          }
        }
      }
    }

    if (preRevNeg && sup)
     return 2;
    else if (preRevNeg)
     return 0;
    else if (sup)
     return 1;

    // to satisfy the compiler
    return -1;
  }

  /**
   * Extracts constant and config file info and writes it to the control file
   *
   * @param installEntity reference to the IE which holds this info.
   * @exception GeneratorException If error is encountered
   */
  protected void writeConstantAndConfigFileInfo( InstallEntity installEntity )
    throws GeneratorException
  {
    ArrayList arrayObj = installEntity.getConstantList();
    ListIterator i;
    String tempString, configString, absCfgFile, cpyCommand;
    String configFileName, tmpType = "";
    ReqType tempReq;
    File configFile;             // supplied configFile

    // write out the constant-value pairs to the prototype file
    if (arrayObj != null)
    {
      i = arrayObj.listIterator();
      while ( i.hasNext() )
      {
        tempString = (String)i.next();
        writeString(prototype_, tempString);
        writeString(prototype_, "\n");
      }
    }

    if (this.pkgClass_.equalsIgnoreCase(PackageConstants.IPP_PKG_CLASS))
    {
      // write configFile info to the prototype file
      arrayObj = installEntity.getConfigFiles();
      if (arrayObj!= null)
      {
        i = arrayObj.listIterator();
        while ( i.hasNext() )
        {
          ReqType rt = (ReqType)i.next();
          if (rt == null)
          {
             throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
               "Incorrect format for specifying the control file information.");
          }

          tempReq = rt;
          configFileName = tempReq.getValue();
          if (configFileName == null ||
              configFileName.trim().length() == 0)
          {
            throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
              "No value specified for filename of 'configFiles' attribute.");
          }

          // configFileName is the name of the configFile described.
          // Check to see if its an absolute path ( starts with a forward
          // slash) or assume its a relative path and append tostage
          if (configFileName.trim().startsWith(
              ParserGeneratorInitiator._fileSeparator_))
            configFile = new File(configFileName);
          else
            configFile = new File(insertFileSeparator(shipRootDir_,
                                                      configFileName));
          tmpType = tempReq.getType().trim();
          if (!configFile.exists())
            throw new GeneratorException("GeneratorException : PkgmkGenerator :\n" +
                "The file in the CMF attribute \"" + tmpType + "   " + configFile.toString() +
                "\" does not exist.");
      
          configString = "i"  + "     " + tmpType + "=" + configFile.toString() + "\n";
          writeString(prototype_, configString);

          cpyCommand = "cp " + configFile.toString() + " " + pkgControlDir_;
          try
          {
            // execute the copy command
            Runtime.getRuntime().exec(cpyCommand);
          }
          catch (IOException ex)
          {
            throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
              "Error occurred while issuing the command '" + cpyCommand + "'");
          }
        }
      }
    }
  }

  /**
   * Extracts IE and FE info from second and third level and writes to protoype
   * file
   *
   * @param levelArray  the array holding the EntityTreeObjects
   * @exception GeneratorException if error is encountered
   */
  protected void generatePrototypeFile( ArrayList levelArray )
    throws GeneratorException
  {
    // This will hold the reference to the array of EntityTreeObjects at level
    // Two. These ETO's represent the CLASS (Fileset) info in the package
    // heirarchy for Sun
    ArrayList levelTwoArray = null;

    // variables to hold various type of attribute values
    String tempString;
    ArrayList  tempArray;

    try
    {
      levelTwoArray = (ArrayList)levelArray.get(1);
    }
    catch (IndexOutOfBoundsException ex)
    {
      throw new GeneratorException("PkgmkGenerator : Level 2 : " +
                                   LEVEL_ARRAY_ERROR);
    }

    InstallEntity ieRef;
    EntityTreeObject entityTreeObjRef;
    ArrayList childEto;
    ListIterator levelTwoIterator = levelTwoArray.listIterator();
    while ( levelTwoIterator.hasNext() )
    {
      childEto = null;
      entityTreeObjRef = (EntityTreeObject)levelTwoIterator.next();
      ieRef = entityTreeObjRef.getInstallEntityReference();

      if (ieRef == null)
      {
        throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
          "Unable to obtain the InstallEntity Reference. No Class definition" +
          " has been given.");
      }

      // first get the CLASS or fileset name
      String className = getValue(ieRef.getEntityName(), "entityName", REQD);
      className = className.trim();
      if (className.length() > 12)
      {
        Interface.printWarning("Class Name " + className +
          " has more than 12 characters and truncated to " +
          className.substring(0, 12));
        className = className.substring(0, 12);
      }
      className = className + "   ";

      // write the search path info for that particular
      // fileset : class ( sun terminology )
      tempArray = ieRef.getInputPath();
      if (tempArray != null)
      {
        ListIterator i = tempArray.listIterator();
        while (i.hasNext())
        {
           tempString = (String)i.next();
           writeString(prototype_, "\n" + "!search    " + tempString);
        }
        writeString(prototype_, "\n");
      }

      // write config file info for that fileset : class
      writeConstantAndConfigFileInfo(ieRef);

      childEto = entityTreeObjRef.getChildReferenceArray();
      ListIterator i = childEto.listIterator();
      while ( i.hasNext() )
      {
        EntityTreeObject curEto = (EntityTreeObject)i.next();
        FileEntity fileReference = curEto.getFileEntityReference();
        if (fileReference == null)
        {
          continue;
        }

        ArrayList pDataArray = fileReference.getPackageData();
        if (pDataArray == null || pDataArray.isEmpty() == true)
        {
          continue;
        }

        if (pDataArray.size() > 1)
        {
          String srcdir = fileReference.getSourceDir();
          String srcfile = fileReference.getSourceFile();
          String srcpath;
          if (srcfile != null)
            srcpath = srcdir + srcfile;
          else
            srcpath = srcdir;
          Interface.printWarning(
            "Multiple PackageData entries not supported on Solaris, " +
            "using first entry from File stanza \"" + srcpath + "\"");
        }

        // Use first PackageData array element
        PackageData curPD = (PackageData)pDataArray.get(0);
        if (this.pkgClass_.equalsIgnoreCase(PackageConstants.SP_PKG_CLASS))
        {
          if (curPD.getFileType().trim().equalsIgnoreCase("s"))
          {
            if (!isNewLink(fileReference))
              continue;
          }
          else
          {
            File file = new File(insertFileSeparator(shipRootDir_,
                                                 getSourceFile(fileReference)));

            // In case of service packaging create an entry in the prototype file
            // for only the files that exist in the shiptree. Only exception is
            // for the symbolic links
            if (!file.exists())
            {
              continue;
            }
          }
        }

        // partNum
        tempString = getValue(curPD.getPartNum(), "partNum", NOT_REQD);
        writeString(prototype_, tempString);

        // file_type
        tempString = getValue(curPD.getFileType(), "fileType", REQD);
        writeString(prototype_, tempString);

        // class name
        writeString(prototype_, className);

        // write path information
        writeString(prototype_, getPathValue(fileReference, curPD));

        // permissions
        tempString = getValue(curPD.getPermissions(), "permissions", REQD);
        writeString(prototype_, tempString);

        // user_id
        tempString = getValue(curPD.getUserId(), "userId", REQD);
        writeString(prototype_, tempString);

        // group_id
        tempString = getValue(curPD.getGroupId(), "groupId", REQD);
        writeString(prototype_, tempString);

        //majorDevNum
        tempString = getValue(curPD.getMajorDevNum(), "majorDevNum",
                              NOT_REQD);
        writeString(prototype_, tempString);

        //minorDevNum
        tempString = getValue(curPD.getMinorDevNum(), "minorDevNum",
                              NOT_REQD);
        writeString(prototype_, tempString);

        // End all this by writing out a new line
        writeString(prototype_, "\n");
      }
    }
  }

  /**
   * Returns true if the link represented by the specified FileEntity object
   * is new.
   *
   * @param fileReference FileEntity object
   */
  private boolean isNewLink( FileEntity fileReference )
  {
    String newLinks = System.getProperty("PATCH_NEW_LINKS");
    if (newLinks == null) return false;
    String fileName = this.getSourceFile(fileReference);
    StringTokenizer st = new StringTokenizer(newLinks, ",");
    while (st.hasMoreTokens())
    {
      if (st.nextToken().equals(fileName))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the path value of an prototype entry.
   *
   * @param fileReference FileEntity object
   * @param curPD PackageData object
   * @exception GeneratorException if both soruce directory and source filename
   *            are not specified for the specified File Entity
   */
  private String getPathValue( FileEntity fileReference, PackageData curPD )
    throws GeneratorException
  {
    StringBuffer pathValue = new StringBuffer();
    String fileType = curPD.getFileType().trim();
    if (fileType.equalsIgnoreCase("s"))
    {
      String sourceFile = this.getSourceFile(fileReference);
      if (sourceFile.length() != 0)
      {
        pathValue.append(sourceFile);
      }
    }
    else
    {
      String targetFile = this.getTargetFile(curPD);
      if (targetFile.length() != 0)
      {
        pathValue.append(targetFile);
      }
      else
      {
        String sourceFile = this.getSourceFile(fileReference);
        if (sourceFile.length() != 0)
        {
          pathValue.append(sourceFile);
        }
      }
    }

    if (pathValue.length() == 0)
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n"
              + "sourceDir and sourceFile not specified. "
              + "At least one of them must be specified.");
    }
    pathValue.append("=");

    // generate the right side of the '=' of path attribute
    StringBuffer pathValue1 = new StringBuffer();
    if (fileType.equalsIgnoreCase("s"))
    {
      String targetFile = getTargetFile(curPD);
      if (targetFile.length() != 0)
      {
        pathValue1.append(targetFile);
      }
      else
      {
        String sourceFile = this.getSourceFile(fileReference);
        if (sourceFile.length() != 0)
        {
          if (this.pkgClass_.equalsIgnoreCase(PackageConstants.IPP_PKG_CLASS))
            pathValue1.append(insertFileSeparator(shipRootDir_, sourceFile));
          else
            pathValue1.append(sourceFile);
        }
      }
    }
    else
    {
      String sourceFile = this.getSourceFile(fileReference);
      if (sourceFile.length() != 0)
      {
        if (this.pkgClass_.equalsIgnoreCase(PackageConstants.IPP_PKG_CLASS))
          pathValue1.append(insertFileSeparator(shipRootDir_, sourceFile));
        else
          pathValue1.append(sourceFile);
      }
    }

    if (pathValue1.length() == 0)
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n"
              + "sourceDir and sourceFile not specified. "
              + "At least one of them must be specified.");
    }

    pathValue1.append("   ");
    pathValue.append(pathValue1.toString());
    return pathValue.toString();
  }

  /**
   * Returns a string that contains the appended value of target directory
   * and target file of the specified package data, in that order.
   *
   * @param pdObj PackageData object
   */
  private String getTargetFile( PackageData pdObj )
  {
    String targetDir = pdObj.getTargetDir();
    String targetFile = pdObj.getTargetFile();
    StringBuffer sb = new StringBuffer();
    if (targetDir != null)
      sb.append(targetDir.trim());
    if (targetFile != null)
      sb.append(targetFile.trim());
    return sb.toString();
  }

  /**
   * Returns a string that contains the appended value of target directory
   * and target file of the specified package data, in that order.
   *
   * @param pdObj PackageData object
   */
  private String getSourceFile( FileEntity fileEntityObj )
  {
    String sourceDir = fileEntityObj.getSourceDir();
    String sourceFile = fileEntityObj.getSourceFile();
    StringBuffer sb = new StringBuffer();
    if (sourceDir != null)
      sb.append(sourceDir.trim());
    if (sourceFile != null)
      sb.append(sourceFile.trim());
    return sb.toString();
  }

  /**
   * Returns the value associated with the specified PgSpecialType object. This
   * method returns null if the type of the PgSpecialType object is neither
   * ParserGeneratorEnumType.FILENAMEDATATYPE or ParserGeneratorEnumType.STRING.
   *
   * @param spTypeObj PgSpecialType object
   * @exception GeneratorException
   */
  private String getValue( PgSpecialType spTypeObj )
    throws GeneratorException
  {
    String value = null;
    if (spTypeObj.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
    {
      // NOTE : If the type is a file read from a file and write it out as a
      // string
      value = copyFileToString(spTypeObj.getValue().trim());
    }
    else if (spTypeObj.getType() == ParserGeneratorEnumType.STRING)
    {
      value = spTypeObj.getValue();
    }
    return value;
  }

  /**
   * construct a string value according to format required by prototype file
   *
   * @param value String value to be formatted
   * @param type to indicate whether this is a required field or not.
   * @return String formatted String
   * @exception GeneratorException If error is encountered
   */
  private String getValue( String value, String cmfAttribName, int type )
    throws GeneratorException
  {
    if (type == REQD && value == null)
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
        "The CMF attribute '" + cmfAttribName + "' in the File Stanza " +
        REQD_FIELD_ERROR);
    }
    if (value != null)
      return value + "   ";
    else
      return "    ";
  }

  /**
   * construct a attrib-value pair according to format required by pkginfo file
   *
   * @param attrib attribute name
   * @param tempString String value to be formatted
   * @param reqd to indicate whether this is a required field or not.
   * @return String formatted String
   * @exception GeneratorException If error is encountered
   */
  private String getValue( String attrib, String cmfAttribName,
                          String tempString, int reqd )
    throws GeneratorException
  {
    String value = "";
    if (reqd == REQD &&  tempString == null )
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
        " Error : The CMF attribute " + cmfAttribName +
        " < " + attrib + " > "  +  REQD_FIELD_ERROR);
    }
    if (tempString != null)
    {
      value = attrib + "=" + tempString + "\n";
      return value;
    }
    else
      return null;
  }

  /**
   * construct a attrib-arrayValue pair according to format required by pkginfo
   * file
   *
   * @param attrib attribute name
   * @param tempArray array value to be formatted
   * @param reqd to indicate whether this is a required field or not.
   * @return String formatted String
   * @exception GeneratorException If error is encountered
   **/
  private String getValue( String attrib, String cmfAttribName,
                           ArrayList tempArray, int reqd )
    throws GeneratorException
  {
    StringBuffer tempBuffer = new StringBuffer();
    String tempString = "";
    if (reqd == REQD &&  tempArray == null)
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n" +
        "The CMF attribute " + cmfAttribName + " < " + attrib + " > "  +
        REQD_FIELD_ERROR);
    }
    if (tempArray != null)
    {
      ListIterator i = tempArray.listIterator();
      while ( i.hasNext() )
      {
        Object tmpvalue = (Object)i.next();
        tempBuffer.append("  ");
        if (tmpvalue.getClass().equals(String.class))
        {
          tempString = (String)tmpvalue;
          tempString = tempString.trim();
          //don't truncate HOTLINE and EMAIL fields
          if ((!attrib.equals("HOTLINE")) && (!attrib.equals("EMAIL")) && 
              (tempString.length() > 12))
          {
            tempString = tempString.substring(0, 12);
          }
          tempBuffer.append( tempString + "  " );
        }
        else if (tmpvalue.getClass().equals(PgSpecialType.class))
        {
          PgSpecialType tmpPgSpecialType = (PgSpecialType)tmpvalue;
          tempString = tmpPgSpecialType.getValue();
          tempBuffer.append(tempString + "\n" + "  ");
        }
      }
      return  attrib + "=" + tempBuffer.toString() + "\n";
    }
    else
      return null;
  }

  /**
   * Utility function for reading from a file and writing it to a string
   *
   * @param fileName name of the file to be read
   * @return String formatted String
   * @exception GeneratorException If error is encountered
   */
  private String copyFileToString( String fileName )
    throws GeneratorException
  {
    int nr_read = 0;
    StringBuffer valueBuffer = new StringBuffer();
    byte  b[] = new byte[10000];

    try
    {
      FileInputStream fin = new FileInputStream(fileName);
      nr_read = fin.read(b);
      while (nr_read != -1)
      {
        String value = new String(b, 0, nr_read);
        valueBuffer.append(value);
        nr_read = fin.read(b);
      }
      fin.close();
      return valueBuffer.toString().trim();
    }
    catch (SecurityException ex)
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n"
        + fileName + " file cannot be opened for reading.");
    }
    catch (IOException ex)
    {
      throw new GeneratorException("GeneratorException : PkgmkGenerator\n"
        + fileName + " file cannot be opened for reading.");
    }
  }
}
