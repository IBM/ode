/*******************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 5/10/03 15:27:42
 * Date and Time File was extracted/checked out: 06/04/13 16:45:47
 ******************************************************************************/
package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.rmi.*;
import java.util.*;
import com.ibm.ode.lib.io.Interface;

/**
 * This class is responsible for extracting appropriate information from the
 * installEntities and FileEntities which are present in the EntityTree at
 * appropriate levels . It writes out the extracted information to certain files
 * which will be the control files that act as input to the packaging tool. The
 * packaging tool considered here is <I> mkinstall </I> .
 */
public class MkinstallGenerator extends Generator
  implements GeneratorInterface
{
  // Some static final variables which will be used in generating error
  // and formatted strings
  private static final String REQD_FIELD_ERROR =
    "is required for generating the package.";
  private static final int REQD = 10;
  private static final int NOT_REQD = 11;
  private static final int STRINGTYPE = 12;
  private static final int SCALARTYPE = 13;

  /**
   * File output handle for the main control file pcd.pd
   */
  private FileOutputStream controlFile_;

  /**
   * File output handle for the *.il files to be generated
   */
  private FileOutputStream ilFile_;

  /**
   * This will hold the reference to the array of EntityTreeObjects
   * at level One. These ETO's represent the product info in the
   * package heirarchy for mkinstall.
   */
  private ArrayList levelOneArray_;

  /**
   * This will hold the reference to the array of EntityTreeObjects
   * at level Two. These ETO's represent the Fileset info in the
   * package heirarchy for mkinstall.
   */
  private ArrayList levelTwoArray_;

  /**
   * This will hold the fileset names that fixed for in a particular Service
   * Packaging run
   */
  private String listOfFilesetsBeingFixed_;

  public String supersedes = "";
  public StringBuffer attributes = new StringBuffer();

  /**
   * Constructor for MkinstallGenerator
   *
   * @param      shipRootDir :- Env Var => root dir. of the ship tree.
   * @param      context   :- Env Var => Context variable
   * @param      pkgControlDir  :- Env Var :- Output directory
   * @param      pkgType    :- indicate User or Official packaging
   * @param      pkgClass     :- indicate IPP or SP
   * @param      pkgFixClass  :- indicate type of fix strategy
   */
  public MkinstallGenerator( String shipRootDir, String context,
                             String pkgControlDir, String pkgType,
                             String pkgClass, String pkgFixStrategy )
  {
    super(shipRootDir, context, pkgControlDir, pkgType, pkgClass,
          pkgFixStrategy);

    // do any initialization here
    listOfFilesetsBeingFixed_ = "";
  }

  /**
   * Generates the Metadata Files for a particular platform and packaging
   * tool
   *
   * @param entityTreeRoot reference to the object which holds the
   * structured IE
   * @param packageObject  reference to the object which contain the IE's
   * and FE's
   * @exception GeneratorException if any error is encountered
   */
  public void generateTargetMetadataFiles( EntityTreeRoot entityTreeRoot ,
                                           Package packageObject )
    throws GeneratorException
  {
    // Arrays to reference the EntityTreeObjects
    ArrayList levelArray;

    // first create the main Control File *.pd in the pkgControlDir
    // as specified in the ode pass. The name of the file is pcd.pd
    // which is the name give for the aix control file in ode
    controlFile_ = openFile( pkgControlDir_ , "pcd.pd" );

    levelArray = entityTreeRoot.getLevelArray();

    // get the level one array which contains entities containing
    // product definition
    levelOneArray_ = (ArrayList)levelArray.get( 0 );

    // write the Product definition information to the
    // Product definition file
    writeProductInfoToProductDefinitionFile();

    // get the level two array which contans entities
    // containing fileset information
    levelTwoArray_ = (ArrayList)levelArray.get( 1 );

    // write the Fileset and File info
    writeFilesetAndFileInfo();

    closeFile( controlFile_ );

  }

  // utility methods for writing name=value; pairs

  /**
    * construct a attrib-scalarValue pair according to format required
    * by *.pd file
    *
    * @param attrib attribute name
    * @param tempString String value to be formatted
    * @param reqd to indicate whether this is a required field or not.
    * @return String formatted String
    * @exception GeneratorException :- If error is encountered
    **/
  private String constructAnAttribValueScalar( String attrib,
                       String cmfAttribName,
                       String tempString,
                       int reqd )
  throws GeneratorException
  {
    String constructedString = "";
    if( reqd == REQD &&  tempString == null  )
    {
      throw new GeneratorException("MkinstallGenerator :\n" +
                 "The CMF attribute " + cmfAttribName +
                 "  < " + attrib + " > " + REQD_FIELD_ERROR );
    }
    if( tempString != null )
    {
      constructedString = "    " + attrib + "  =  " + tempString + " ; " + "\n";
      return constructedString;
    }
    else
      return null;
  }

  /**
   * construct a attrib-stringValue pair according to format required
   * by *.pd file
   *
   * @param attrib attribute name
   * @param tempString String value to be formatted
   * @param reqd to indicate whether this is a required field or not.
   * @return String formatted String
   * @exception GeneratorException If error is encountered
   **/
  private String constructAnAttribValueString( String attrib,
                                               String cmfAttribName,
                                               String tempString,
                                               int reqd )
    throws GeneratorException
  {
    String constructedString = "";
    if( reqd == REQD &&  tempString == null  )
    {
      throw new GeneratorException("MkinstallGenerator :\n" +
                 "The CMF attribute " + cmfAttribName +
                 "  < " + attrib + " > " + REQD_FIELD_ERROR);
    }
    if( tempString != null )
    {
      constructedString = "    " + attrib + "  =  \"" + tempString + "\"" + ";" + "\n" ;
      return constructedString;
    }
    else
      return null;
  }

  /**
   * construct a attrib-arrayValue pair according to format required by *.pd file
   *
   * @param attrib attribute name
   * @param tempString String value to be formatted
   * @param reqd to indicate whether this is a required field or not.
   * @return String formatted String
   * @exception GeneratorException If error is encountered
   */
  private String constructAnAttribValueArray( String attrib, String cmfAttribName,
                                              ArrayList tempArray, int reqd,
                                              int type )
    throws GeneratorException
  {
    ListIterator i;
    StringBuffer  tempBuffer = new StringBuffer();
    String tempString = "";
    String returnString = "";

    if( reqd == REQD &&  tempArray == null  )
    {
      throw new GeneratorException("MkinstallGenerator :\n" +
       "The CMF attribute " + cmfAttribName + "  < " + attrib +
       " > " + REQD_FIELD_ERROR );
    }

    if( tempArray == null )
      return null ;

    i = tempArray.listIterator();
    while( i.hasNext() )
    {
      tempBuffer.append(" ");
      Object tmpobj = i.next();
      if( tmpobj instanceof String )
      {
        // Customize any attrib that refers to a filename since the filename
        // has to be modified to add the shipRoot diecrectory where it will
        // be moved by  the gather pass
        if( attrib == "odm_add_files" || attrib == "root_control_files" ||
            attrib == "root_add_files" )
        {
          tempString = (String)tmpobj;
          tempBuffer.append( insertFileSeparator( shipRootDir_,
                                                  tempString.trim() ));
        }
        else
        {
          tempString = (String)tmpobj;
          if( type == SCALARTYPE )
            tempBuffer.append( tempString.trim() );
          else
            tempBuffer.append("\"" + tempString.trim() + "\"" );
        }
      }
      else if( tmpobj instanceof PgSpecialType )
      {
        PgSpecialType tmpPgSpecialType = (PgSpecialType)tmpobj;
        tempString = tmpPgSpecialType.getValue();
        if( type == SCALARTYPE )
        {
          tempBuffer.append( tempString.trim() );
        }
        else
        {
          tempBuffer.append("\"" + tempString.trim() + "\"" );
        }
      }
      else if (cmfAttribName.trim().equals("configFiles"))
      {
        ReqType tmpReqType = (ReqType)tmpobj;
        if( (( tempString = (String)tmpReqType.getValue() ) != null)
           && ((((String)tmpReqType.getValue()).trim()).length() != 0) )
          tempBuffer.append(insertFileSeparator(shipRootDir_ ,
                                                tempString.trim()));
        else
          throw new GeneratorException("MkinstallGenerator :\n" +
            "The CMF attribute " + cmfAttribName +
            " does not have a valid file name." );
      } // End else if configFiles
    } // End for loop.

    returnString += "    " + attrib + "  =  " ;
    returnString += "[" + tempBuffer.toString() + " ] ; " + "\n";

    return returnString ;

  } // End constructAnAttribValueArray

  /**
   * Creates the value of v_reqs tag in pcd.pd file. The value of the v_reqs
   * tag is used to populate the <fileset>.prereq file. It returns
   * null if there are no requisites defined for the fileset in the CMF.
   *
   * @param attrib requisite type
   * @param requisites array of ReqType objects
   * @return the value of v_reqs tag
   */
  private String setRequisites( String attrib, ArrayList requisites )
    throws GeneratorException
  {
    // One for each type of requisite ODE supports. Leave validation up
    // to the the generators. Return the value of the requested attribute
    // as an unformatted string or null. Leave formatting up the the
    // generators.
    if (requisites == null || attrib == null)
      return null;

    StringBuffer tmpBuffer = new StringBuffer();
    StringBuffer requisiteStr = new StringBuffer();
    String type, value, desc;

    attrib = attrib.trim();
    ListIterator i = requisites.listIterator();
    while( i.hasNext() )
    {
      // Clear the contents of requistesStr
      requisiteStr.setLength(0);

      ReqType reqType = (ReqType)i.next();

      // If type is null, continue with other requisites
      type = (String)reqType.getType();
      if (type == null) continue;

      type = type.trim();
      if ((attrib.equals("prereq") && (type.equalsIgnoreCase("P") ||
                                       type.equalsIgnoreCase("Pre"))) ||
          (attrib.equals("coreq") && (type.equalsIgnoreCase("C") ||
                                      type.equalsIgnoreCase("Co"))) ||
          (attrib.equals("instreq") && (type.equalsIgnoreCase("L") ||
                                        type.equalsIgnoreCase("ins"))) ||
          (attrib.equals("ifreq") && (type.equalsIgnoreCase("I") ||
                                      type.equalsIgnoreCase("If"))))
      {
        if ((value = (String)reqType.getValue()) != null)
          requisiteStr.append(value.trim());
        if ((desc = (String)reqType.getDescription()) != null)
        {
          requisiteStr.append(" ");
          requisiteStr.append(desc.trim());
        }
        if (requisiteStr.length() > 0)
        {
          tmpBuffer.append("\"*").append(attrib).append(" ").
            append(requisiteStr.toString()).append("\" ");
        }
      }
    }

    if (tmpBuffer.length() > 0)
      return tmpBuffer.toString();
    else
      return null;
  }

  /**
   * Creates the value  of 'supersedes' tag  in pcd.pd file. The value of
   * supersedes tag is used to populate the <fileset>.supersede file. It returns
   * null if there are no supersedes defined for the fileset in the CMF.
   *
   * @param requisites an array of ReqType objects
   * @return the value of supersedes tag
   */
  private String setSupersedes( ArrayList requisites )
  {
    if (requisites == null)
      return null;

    StringBuffer supersedeStr = new StringBuffer();
    String type, value, desc;
    ListIterator i = requisites.listIterator();
    while( i.hasNext() )
    {
      // If type is null, continue with other requisites
      ReqType reqType = (ReqType)i.next();
      type = (String)reqType.getType();
      if (type == null) continue;

      type = type.trim();
      if (type.equalsIgnoreCase("S") ||
          type.equalsIgnoreCase("Sup"))
      {
        if ((value = (String)reqType.getValue()) != null)
          supersedeStr.append(value.trim());
        if ((desc = (String)reqType.getDescription()) != null)
        {
          supersedeStr.append(" ");
          supersedeStr.append(desc.trim());
        }

        // we are breaking from the loop here, because we are only expecting
        // one supersede. Having more than one supersede is not valid.
        break;
      }
    }
    if (supersedeStr.length() > 0)
      return supersedeStr.toString();
    else
      return null;
  }

  /**
   * Validates if the specified set of requisites. If the any of the specified
   * requisites are not valid then a GeneratorException is thrown.
   *
   * @param requisites an array of ReqType objects
   * @exception GeneratorException
   */
  private void validateRequisites( ArrayList requisites )
    throws GeneratorException
  {
    if (requisites == null)
    {
      return;
    }

    String type;
    ReqType reqType;
    ListIterator i = requisites.listIterator();
    while( i.hasNext() )
    {
      Object tmpobj = i.next();
      if (tmpobj instanceof ReqType)
      {
        reqType = (ReqType)tmpobj;
        type = (String)reqType.getType();
        type = type.trim();
        if (type != null)
        {
          if (!(type.equalsIgnoreCase("P") || type.equalsIgnoreCase("Pre") ||
                type.equalsIgnoreCase("C") || type.equalsIgnoreCase("Co")  ||
                type.equalsIgnoreCase("L") || type.equalsIgnoreCase("Ins") ||
                type.equalsIgnoreCase("S") || type.equalsIgnoreCase("Sup") ||
                type.equalsIgnoreCase("I") || type.equalsIgnoreCase("If")))
          {
            throw new GeneratorException("MkinstallGenerator :\n" +
                    type.trim() +
                    " is an invalid requisite type. Valid " +
                    "types are 'P' or 'Pre', 'C' or 'Co', 'L' or 'Ins', " +
                    "'S' or 'Sup', and 'I' or 'If'.");
          }
        }
      }
    }
  }

  /**
   * This method sets the values of attributes and supersedes member variables.
   * The values of these variables are used to set the v_req and supersedes
   * tags in pcd.pd file.
   *
   * @param requisites an array of ReqType objects
   */
  public void getRequisites( ArrayList requisites )
    throws GeneratorException
  {
    // All we do here is get the requisites in an array and validate. If the
    // array is not empty we set all non-null requisites to print
    // them later. If the array is empty there is nothing to validate
    // or print. Any platform specific issues such as formatting are
    // handled here as well.
    try
    {
      validateRequisites(requisites);
    }
    catch (GeneratorException ex)
    {
      throw new GeneratorException(ex.toString());
    }

    // prereq
    String reqs = setRequisites("prereq", requisites);
    if (reqs != null)
    {
      attributes.append(reqs);
    }

    // coreq
    reqs = setRequisites("coreq", requisites);
    if (reqs != null)
    {
      attributes.append(reqs);
    }

    // instreq -
    reqs = setRequisites("instreq", requisites);
    if (reqs != null)
    {
      attributes.append(reqs);
    }

    // ifreq
    reqs = setRequisites("ifreq", requisites);
    if (reqs != null)
    {
      attributes.append(reqs);
    }

    // supersedes -
    supersedes = setSupersedes(requisites);
  }

  /**
   * This method rites v_req and supersedes tags to the pcd.pd file
   */
  public void printRequisites()
    throws IOException, GeneratorException
  {
    // write out the requisites in the format required by the package tool

    // prereq, coreq, instreq, ifreq
    if (attributes.length() > 0)
    {
      writeString(controlFile_,
                  "    v_reqs = [ " + attributes.toString() + " ] ; \n");
    }

    // Supersedes
    if (supersedes != null && supersedes.length() > 0)
    {
      supersedes = "    supersedes  =  [ \"" + supersedes + "\" ] ;\n";
      writeString(controlFile_, supersedes);
    }

    attributes.setLength(0);
    supersedes = "";
  }

/**
 * construct a value according to format required by *.il file
 * @param value String value to be formatted
 * @param type to indicate whether this is a required field or not.
 * @return String formatted String
 * @exception GeneratorException If error is encountered
 **/
public String constructFileValue( String value, String cmfAttribName, int type )
    throws GeneratorException
{
  if( type == REQD && value == null )
  {
    throw new GeneratorException("MkinstallGenerator :\n"
            + "Writing the file info to the *.il file. "
            + "The CMF attribute " + cmfAttribName + " " + REQD_FIELD_ERROR );
  }
  if( value != null )
    return value + "   ";
  else
    return "  ";
}

  /**
   * Utility function for reading from a file and writing it to a string
   * @param fileName name of the file to be read
   * @return String formatted String
   * @exception GeneratorException If error is encountered
   */
  public String copyFileToString( String fileName )
    throws GeneratorException
  {
    int nr_read = 0;
    StringBuffer  valueBuffer = new StringBuffer();
    byte  b[] = new byte[10000];

    try
    {
      FileInputStream fin = new FileInputStream( fileName.trim() );

      nr_read = fin.read( b );
      while( nr_read != -1 )
      {
        String value = new String( b,0,nr_read );
        valueBuffer.append( value );
        nr_read = fin.read( b );
      }
      fin.close();
      return valueBuffer.toString().trim();
    }
    catch( SecurityException e )
    {
      throw new GeneratorException("MkinstallGenerator :\n" +
                                   fileName +
                                   " File cannot be opened for reading.");
    }
    catch (IOException e)
    {
      throw new GeneratorException("MkinstallGenerator :\n" +
                                   fileName +
                                   " File cannot be opened for reading.");
    }
  }

  /**
   * Extracts information from the IE's in First Level and writes out product info
   *
   * @exception GeneratorException if error is encountered
   */
  public void writeProductInfoToProductDefinitionFile()
    throws GeneratorException
  {
    ListIterator levelOneArrayIterator;

    // References to objects needed by the generator
    EntityTreeObject entityTreeObjectReference;
    InstallEntity  installEntityReference;

    // variables to hold various type of attribute values

    String tempString, constructedString;
    StringBuffer  tempStringBuffer;
    ArrayList tempArray;
    ListIterator i;
    PgSpecialType tempSpecialType;
    ReqType     tempReqType;

    levelOneArrayIterator = levelOneArray_.listIterator();
    while( levelOneArrayIterator.hasNext() )
    {
      entityTreeObjectReference = (EntityTreeObject)levelOneArrayIterator.next();

      installEntityReference = entityTreeObjectReference.getInstallEntityReference();

      if( installEntityReference == null )
      {
        throw new GeneratorException("MkinstallGenerator :\n" +
             "Unable to obtain the InstallEntity Reference. No product" +
             " definition has been given.");
      }


      // once you get a valid installEntity, start writing out to the file
      // as required by the packaging tool

      /**
       * Product Name
       **/
      tempString = installEntityReference.getEntityName();
      if( tempString == null )
      {
        throw new GeneratorException("MkinstallGenerator :\n" +
              "The CMF attribute entityName " + REQD_FIELD_ERROR);
      }

      // write product <name>
      writeString( controlFile_, "product   " + tempString + "\n" + "{" + "\n" );

      /**
       * Description
       **/
      tempSpecialType = installEntityReference.getDescription();

      // check to see whether it is a file or a string
      if( tempSpecialType == null )
      {
        throw new GeneratorException("MkinstallGenerator :\n" +
          "The CMF attribute description " + REQD_FIELD_ERROR );
      }
      if( tempSpecialType.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE )
      {
        tempString = copyFileToString( tempSpecialType.getValue() );
        if (tempString.length() > 60)
        {
          throw new GeneratorException("MkinstallGenerator :\n"
                   + "The CMF attribute description "
                   + "cannot exceeed 60 characters in length");
        }
            constructedString = constructAnAttribValueString( "description" ,
                            "description" ,
                            tempString,
                            REQD );
            writeString( controlFile_, constructedString );
    }
    else if( tempSpecialType.getType() == ParserGeneratorEnumType.STRING )
    {
        tempString = tempSpecialType.getValue();
        if (tempString.length() > 60)
        {
                throw new GeneratorException("MkinstallGenerator :\n"
                   + "The CMF attribute description "
                   + "cannot exceeed 60 characters in length" );
        }
            constructedString = constructAnAttribValueString( "description" ,
                            "description" ,
                            tempString,
                            REQD );
            writeString( controlFile_, constructedString );
    }
    if ( !( tempString.toString().length() < 60 ) || ( tempString == null ) )
    {
        throw new GeneratorException("MkinstallGenerator :\n" +
            "The CMF attribute description " + REQD_FIELD_ERROR +
            " and must be less than 60 characters." );
    }

  /**
   * image_name
   **/

  tempString = installEntityReference.getImageName();
  constructedString =  constructAnAttribValueScalar( "image_name",
                             "imageName" ,
                             tempString,
                             REQD );
  writeString( controlFile_, constructedString);

  /**
   *  version release maint_level fix_level
   **/
  // make sure the right vrmf gets in here for Service packaging

  tempString = installEntityReference.getVersion();
  constructedString = constructAnAttribValueScalar( "version",
                            "version",
                            tempString,
                            REQD );
  writeString( controlFile_, constructedString  );

  tempString = installEntityReference.getRelease();
  constructedString = constructAnAttribValueScalar( "release",
                            "release",
                            tempString,
                            REQD );
  writeString( controlFile_, constructedString  );

  tempString = installEntityReference.getMaintLevel();
  constructedString = constructAnAttribValueScalar( "maint_level",
                            "maintLevel",
                            tempString,
                            REQD );
  writeString( controlFile_,constructedString  );

  tempString = installEntityReference.getFixLevel();
  constructedString = constructAnAttribValueScalar( "fix_level",
                            "fixLevel",
                            tempString,
                            REQD );
  writeString( controlFile_, constructedString );

  /**
   * platform
   **/
  tempString = installEntityReference.getMachineType();
  constructedString =  constructAnAttribValueScalar("platform",
                            "machineType",
                            tempString,
                            REQD );
  writeString( controlFile_, constructedString );

  /**
   * copyright
   **/
  tempSpecialType = installEntityReference.getCopyright();
  // check to see whether it is a file or a string
  if( tempSpecialType != null )
          {
          if( tempSpecialType.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE )
    {
    tempString = copyFileToString( tempSpecialType.getValue() );
    constructedString = constructAnAttribValueString( "copyright" ,
                              "copyright",
                              tempString,
                              NOT_REQD );
    writeString( controlFile_, constructedString );
    }
          else if( tempSpecialType.getType() == ParserGeneratorEnumType.STRING )
    {
    tempString = tempSpecialType.getValue();
    constructedString =  constructAnAttribValueString("copyright",
                              "copyright",
                              tempString,
                              NOT_REQD );
    writeString( controlFile_, constructedString );
    }
          }

  /**
   * copyrightKeys
   **/
  tempArray =  installEntityReference.getCopyrightKeys();
  constructedString =  constructAnAttribValueArray( "copyright_keys",
                            "copyrightKeys",
                            tempArray,
                            NOT_REQD,
                            SCALARTYPE );
  if(  constructedString != null )
    writeString( controlFile_, constructedString );
  // copyright_map

  tempString =  installEntityReference.getCopyrightMap();
  if( tempString != null )
    tempString = insertFileSeparator( shipRootDir_, tempString.trim() );

  constructedString =  constructAnAttribValueScalar("copyright_map",
                            "copyrightMap",
                            tempString,
                            NOT_REQD );
  if(  constructedString != null )
    writeString( controlFile_, constructedString );

  // adecopyright_flags
  tempString =  installEntityReference.getCopyrightFlags();
  constructedString =  constructAnAttribValueString("adecopyright_flags",
                            "copyrightFlags",
                            tempString,
                            NOT_REQD );
  if(  constructedString != null )
    writeString( controlFile_, constructedString );

  // language
  tempString =  installEntityReference.getLanguage();
  constructedString =  constructAnAttribValueScalar("language",
                            "language",
                            tempString,
                            NOT_REQD );
  if(  constructedString != null )
    writeString( controlFile_, constructedString );

  // content
  tempString =  installEntityReference.getContent();
  constructedString =  constructAnAttribValueScalar("content",
                            "content",
                            tempString,
                            NOT_REQD );
  if(  constructedString != null )
    writeString( controlFile_, constructedString );

  // uid_table
  tempArray = installEntityReference.getConstantList();

  if( tempArray != null )
  {
    tempString = (String)tempArray.get( 0 ); // more check needs to be done here
    if( tempString != null )
    {
      // Check to see if it is an absolute path ( starts with a forward
      // slash) or assume it is a relative path and append tostage
      if( !(tempString.trim().startsWith( ParserGeneratorInitiator._fileSeparator_ )) )
        tempString = insertFileSeparator(shipRootDir_, tempString.trim() );
    }
    constructedString =  constructAnAttribValueScalar("uid_table",
                      "constantList",
                       tempString,
                       NOT_REQD );
    if(  constructedString != null )
       writeString( controlFile_, constructedString );
  }

  // boot_rqmt
  tempString =  installEntityReference.getBootReqmt();
  constructedString =  constructAnAttribValueScalar("boot_rqmt",
                            "bootReqmt",
                            tempString,
                            NOT_REQD );
  if(  constructedString != null )
    writeString( controlFile_, constructedString );


  // compids_table
  tempString = installEntityReference.getCompidTable();
  constructedString =  constructAnAttribValueScalar("compids_table",
                            "compidTable",
                            tempString,
                            NOT_REQD );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );

        // adepackage_flags
        tempString =  installEntityReference.getADEPackageFlags();
        constructedString =  constructAnAttribValueString("adepackage_flags",
                          "packageFlags",
                          tempString,
                          NOT_REQD );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );

        // adeinv_flags
        tempString =  installEntityReference.getADEInvFlags();
        constructedString =  constructAnAttribValueString("adeinv_flags",
                          "adeInvFlags",
                          tempString,
                          NOT_REQD );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );

        // ar_flags
        tempString =  installEntityReference.getARFlags();
        constructedString =  constructAnAttribValueString("ar_flags",
                          "arFlags",
                          tempString,
                          NOT_REQD );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );

        // input_path
        tempArray = installEntityReference.getInputPath();
        constructedString =  constructAnAttribValueArray("input_path",
                           "inputPath",
                           tempArray,
                           NOT_REQD ,
                           SCALARTYPE );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );

        // ship_path :- note that we overwrite the value with TOSTAGE
        constructedString =  constructAnAttribValueString("ship_path",
                          "TOSTAGE",
                          shipRootDir_,
                          REQD );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );

        // filesets
        tempArray = installEntityReference.getImmChildEntities();
        constructedString =  constructAnAttribValueArray("filesets",
                           "immChildEntities",
                           tempArray,
                           REQD,
                           SCALARTYPE );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );

        //file_name
        tempString =  installEntityReference.getEntityId();
        constructedString =  constructAnAttribValueString("file_name",
                          "entityId",
                          tempString,
                          NOT_REQD );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );

        // At the very end close this product stanza for each entityTreeObject
        writeString(controlFile_, "\n" + "  " + "}" + "\n" );
  }
}

  /**
   * Extracts IE and FE info from second and third level and writes
   * fileset and file info
   *
   * @exception GeneratorException if error is encountered
   */
  public void writeFilesetAndFileInfo()
    throws GeneratorException
  {
    ListIterator levelTwoArrayIterator;

    // References to objects needed by the generator
    EntityTreeObject entityTreeObjectReference;
    EntityTreeObject childEntityObjectReference;
    ArrayList entityChildren;
    ListIterator  entityChildrenIterator;
    InstallEntity  installEntityReference;

    // variables to hold various type of attribute values
    String    tempString;
    String    constructedString;
    StringBuffer  tempStringBuffer;
    ArrayList     tempArray;
    ArrayList     requisites ;
    ListIterator i;
    PgSpecialType tempSpecialType;
    ReqType     tempReqType;

    levelTwoArrayIterator = levelTwoArray_.listIterator();
    while( levelTwoArrayIterator.hasNext() )
    {
      entityTreeObjectReference = (EntityTreeObject)levelTwoArrayIterator.next();

      installEntityReference = entityTreeObjectReference.getInstallEntityReference();

      if( installEntityReference == null )
      {
        throw new GeneratorException("MkinstallGenerator :\n" +
          "Unable to obtain the " +
          "InstallEntity Reference. No Fileset " +
          "definition has been given." );
      }

      // once you get a valid installEntity, start writing out to the file
      // as required by the packaging tool

      // fileset name
      tempString = installEntityReference.getEntityName();
      if( tempString == null )
      {
        throw new GeneratorException("MkinstallGenerator :\n" +
          "The CMF attribute entityName " + REQD_FIELD_ERROR );
      }

      // write fileset <name> {
      writeString( controlFile_, "fileset   " + tempString + "\n" + "{" + "\n" );

      // description
      tempSpecialType = installEntityReference.getDescription();

      // check to see whether it is a file or a string
      if( tempSpecialType == null )
      {
        throw new GeneratorException("MkinstallGenerator :\n" +
          "The CMF attribute description " + REQD_FIELD_ERROR );
      }
      if( tempSpecialType.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE )
      {
        tempString = copyFileToString( tempSpecialType.getValue() );
        if (tempString.length() > 60)
        {
          throw new GeneratorException("MkinstallGenerator :\n" +
            "The CMF attribute description " +
            "cannot exceeed 60 characters in length" );
        }
        constructedString = constructAnAttribValueString( "description" ,
                                                          "description",
                                                          tempString,
                                                          REQD );
        writeString( controlFile_, constructedString );
      }
      else if( tempSpecialType.getType() == ParserGeneratorEnumType.STRING )
      {
        tempString = tempSpecialType.getValue();
        if (tempString.length() > 60)
        {
          throw new GeneratorException("MkinstallGenerator :\n" +
            "The CMF attribute description " +
            "cannot exceeed 60 characters in length" );
        }
        constructedString = constructAnAttribValueString( "description" ,
                                                          "description" ,
                                                          tempString,
                                                          REQD );
        if( constructedString != null )
          writeString( controlFile_, constructedString );
        else
          writeString( controlFile_," " );
      }

      if ( !( tempString.toString().length() < 60 ) || ( tempString == null ) )
      {
        throw new GeneratorException("MkinstallGenerator :\n" +
          "The CMF attribute description " + REQD_FIELD_ERROR +
          " and must be less than 60 characters." );
      }

      // content : note that this is a reqd field in fileset def.
      tempString = installEntityReference.getContent();
      constructedString =  constructAnAttribValueScalar( "content",
                                                         "content",
                                                         tempString,
                                                         REQD );
      if( constructedString != null )
        writeString( controlFile_, constructedString);

      // boot_rqmt : note that this is a reqd field in fileset definition
      tempString =  installEntityReference.getBootReqmt();
      constructedString =  constructAnAttribValueScalar("boot_rqmt",
                                                        "bootReqmt",
                                                        tempString,
                                                        REQD );
      writeString( controlFile_, constructedString );

      /**
       *  version release maint_level fix_level
       **/
      tempString = installEntityReference.getVersion();

      constructedString = constructAnAttribValueScalar( "version",
                                                        "version",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_, constructedString  );

      tempString = installEntityReference.getRelease();

      constructedString = constructAnAttribValueScalar( "release",
                                                        "release",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_, constructedString  );

      tempString = installEntityReference.getMaintLevel();
      constructedString = constructAnAttribValueScalar( "maint_level",
                                                        "maintLevel",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_,constructedString  );

      tempString = installEntityReference.getFixLevel();
      constructedString = constructAnAttribValueScalar( "fix_level",
                                                        "fixLevel",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

      /**
       * copyright
       **/
      tempSpecialType = installEntityReference.getCopyright();

      // check to see whether it is a file or a string
      if( tempSpecialType != null )
      {
        if( tempSpecialType.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE )
        {
          tempString = copyFileToString( tempSpecialType.getValue() );
          constructedString = constructAnAttribValueString( "copyright" ,
                                                            "copyright" ,
                                                            tempString,
                                                            NOT_REQD );
          writeString( controlFile_, constructedString );
        }
        else if( tempSpecialType.getType() == ParserGeneratorEnumType.STRING )
        {
          tempString = tempSpecialType.getValue();
          constructedString =  constructAnAttribValueString("copyright",
                                                            "copyright",
                                                            tempString,
                                                            NOT_REQD );
          writeString( controlFile_, constructedString );
        }
      }

      /**
   * copyrightKeys
   **/
      tempArray =  installEntityReference.getCopyrightKeys();
      constructedString =  constructAnAttribValueArray( "copyright_keys",
                                                        "copyrightKeys",
                                                        tempArray,
                                                        NOT_REQD,
                                                        SCALARTYPE );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );
      // copyright_map

      tempString =  installEntityReference.getCopyrightMap();
      if( tempString != null )
        tempString =  insertFileSeparator( shipRootDir_, tempString.trim() );

      constructedString =  constructAnAttribValueScalar("copyright_map",
                                                        "copyrightMap",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

  // language
      tempString =  installEntityReference.getLanguage();
      constructedString =  constructAnAttribValueScalar("language",
                                                        "language",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

  // comments : needs to be  checked
      tempArray  =  installEntityReference.getFullEntityName();

      if( tempArray != null )
      {
        tempString = (String)tempArray.get( 0 );
        constructedString =  constructAnAttribValueString("comments",
                                                          "fullEntityName",
                                                          tempString,
                                                          NOT_REQD );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );
      }

      // uid_table
      tempArray = installEntityReference.getConstantList();
      if( tempArray != null )
      {
        tempString = (String)tempArray.get( 0 ); // more check needs to be done here
        if( tempString != null )
        { 
          // Check to see if it is an absolute path ( starts with a forward
          // slash) or assume it is a relative path and append tostage
          if( !(tempString.trim().startsWith( ParserGeneratorInitiator._fileSeparator_ )) )
            tempString = insertFileSeparator(shipRootDir_, tempString.trim() );
        }
        constructedString =  constructAnAttribValueScalar("uid_table",
                                                          "contantList",
                                                          tempString,
                                                          NOT_REQD );
        if(  constructedString != null )
          writeString( controlFile_, constructedString );
      }


      // adeinv_flags
      tempString =  installEntityReference.getADEInvFlags();
      constructedString =  constructAnAttribValueString("adeinv_flags",
                                                        "adeInvFlags",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

  // ar_flags
      tempString =  installEntityReference.getARFlags();
      constructedString =  constructAnAttribValueString("ar_flags",
                                                        "arFlags",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

  // input_path
      tempArray = installEntityReference.getInputPath();
      constructedString =  constructAnAttribValueArray("input_path",
                                                       "inputPath",
                                                       tempArray,
                                                       NOT_REQD,
                                                       SCALARTYPE );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

  //control_files
      tempArray = installEntityReference.getConfigFiles();
      constructedString =  constructAnAttribValueArray("control_files",
                                                       "configFiles",
                                                       tempArray,
                                                       NOT_REQD,
                                                       SCALARTYPE );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

  //odm_add_files
      tempArray = installEntityReference.getOdmAddFiles();
      constructedString =  constructAnAttribValueArray("odm_add_files",
                                                       "odmAddFiles",
                                                       tempArray,
                                                       NOT_REQD,
                                                       SCALARTYPE );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

  //odm_class_def
      tempString =  installEntityReference.getOdmClassDef();
      constructedString =  constructAnAttribValueScalar("odm_class_def",
                                                        "odmClassDef",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

  //root_control_files
      tempArray =  installEntityReference.getRootControlFiles();
      constructedString =  constructAnAttribValueArray("root_control_files",
                                                       "rootControlFiles",
                                                       tempArray,
                                                       NOT_REQD,
                                                       SCALARTYPE );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

  // root_add_files
      tempArray = installEntityReference.getRootAddFiles();
      constructedString =  constructAnAttribValueArray("root_add_files",
                                                       "rootAddFiles",
                                                       tempArray,
                                                       NOT_REQD,
                                                       SCALARTYPE );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );


      requisites = installEntityReference.getRequisites();

      if ( requisites != null )
      {
        try
        {
          getRequisites(requisites);
          printRequisites();
        }
        catch ( IOException e )
        {
          throw new GeneratorException( e.toString() ) ;
        }

      }

      // user_prereq
      tempString =  installEntityReference.getUserPrereq();
      if( tempString != null )
        tempString = insertFileSeparator( shipRootDir_, tempString.trim() );

      constructedString =  constructAnAttribValueScalar("user_prereq",
                                                        "userPrereq",
                                                        tempString,
                                                        NOT_REQD );
      if(  constructedString != null )
        writeString( controlFile_, constructedString );

      // get inslist and start writing out the file information
      // This one is the most imp field and should have all the
      // checks
      tempArray = installEntityReference.getInsList();
      if( tempArray == null )
      {
        throw new GeneratorException("MkinstallGenerator :\n" +
          "The CMF attribute insList " + REQD_FIELD_ERROR );
      }
      if( tempArray.size() > 1 )
      {
        throw new GeneratorException("MkinstallGenerator :\n" +
          "The insList attribute should have only one " +
          "value. This attribute should contain " +
          "the name of the *.il file.");
      }
      tempString = (String)tempArray.get( 0 );
      constructedString = constructAnAttribValueScalar( "inslist",
                                                        "insList",
                                                        tempString,
                                                        REQD );
      if( constructedString != null )
        writeString( controlFile_, constructedString);

      if( tempString != null )
      {
        ilFile_ = openFile( pkgControlDir_ , tempString.trim() );
        entityChildren = entityTreeObjectReference.getChildReferenceArray();
        if( entityChildren != null )
        {
          entityChildrenIterator = entityChildren.listIterator();
          while( entityChildrenIterator.hasNext() )
          {
            childEntityObjectReference = (EntityTreeObject)entityChildrenIterator.next();
            writeFileInfo( childEntityObjectReference );
          }
        }
      }
      closeFile( ilFile_ );
      writeString(controlFile_, "\n" + "  " + "}" + "\n" );

    }
  }

/******************************************************************************
 * Extracts FE info from the EntityTreeObject writes out the file info
 *
 * @param curEntityTreeObject EntityTreeObject which holds th refernce to FE
 * @exception GeneratorException if error is encountered
 **/
public void writeFileInfo( EntityTreeObject curEntityTreeObject )
      throws GeneratorException
{
  String tempString;
  String appendString;
  FileEntity curFileEntity;

  curFileEntity = curEntityTreeObject.getFileEntityReference();

  if( curFileEntity == null )
  {
  throw new GeneratorException("MkinstallGenerator :\n"
                 + "Unable to find the File Entity Reference : "
                 + "Invalid fileset with no child files found." );
  }

  ArrayList pDataArray = curFileEntity.getPackageData();
  if (pDataArray.size() > 1)
  {
    String srcdir = curFileEntity.getSourceDir();
    String srcfile = curFileEntity.getSourceFile();
    String srcpath;
    if (srcfile != null)
      srcpath = srcdir + srcfile;
    else
      srcpath = srcdir;
    Interface.printWarning("Multiple PackageData entries not supported with mkinstall, " +
               "using first entry from File stanza \"" + srcpath + "\"");
  }
  if ((pDataArray != null) && (pDataArray.isEmpty() == false) )
        {
        // Use first PackageData array element
  PackageData curPD  = (PackageData)pDataArray.get( 0 );
  // file_type
  tempString = constructFileValue( curPD.getFileType(),
                   "fileType",
                   REQD );
  writeString( ilFile_, tempString );

  // user_id
  tempString = constructFileValue( curPD.getUserId(),
                   "userId",
                   REQD );
  writeString( ilFile_, tempString );

  // group_id
  tempString = constructFileValue( curPD.getGroupId(),
                   "groupId",
                   REQD );
  writeString( ilFile_, tempString );

  //permissions
  tempString = constructFileValue( curPD.getPermissions(),
                   "permissions",
                   REQD );
  writeString( ilFile_, tempString );

  // This has a slightly different approach : sourceDir + sourceFile
  tempString = curFileEntity.getSourceDir();
  if (tempString != null)
  {
    appendString = curFileEntity.getSourceFile();
    if (appendString == null)
    {
      // Eliminate the trailing slash when creating a symbolic link to a
      // directory. For some reason, installp does not like it... Also, smit
      // is not deleting directories after uninstall if directories are ending
      // in a forward slash
      if (tempString.endsWith("/"))
        tempString = tempString.substring(0, tempString.length()-1);
      writeString(ilFile_, tempString + "   "); // write just the directory name
    }
    else
    {
      writeString(ilFile_, tempString + appendString + "   ");
    }
  }
  else
  {
    appendString =  curFileEntity.getSourceFile();
    if( appendString == null )
          {
            throw new GeneratorException("MkinstallGenerator :\n"
                   + "sourceDir or sourceFile not specified.");
          }
    else
          {
            writeString(ilFile_, appendString + "   " );
          }
  }

  // If the File is a Link check for targetDir and targetFile and write it
  // accordingly
  if( curPD.getFileType().equalsIgnoreCase("H") ||
      curPD.getFileType().equalsIgnoreCase("S") )
  {
    tempString = curPD.getTargetDir();
      if (tempString != null)
      {
        appendString = curPD.getTargetFile();
        if (appendString == null)
        {
          // Eliminate the trailing slash when creating a symbolic link to a
          // directory.
          if (tempString.endsWith("/"))
            tempString = tempString.substring(0, tempString.length() - 1);
          writeString(ilFile_, tempString + "  "); // write just the directory name
        }
        else
        {
          writeString(ilFile_, tempString + appendString + "   ");
        }
      }
      else
      {
        throw new GeneratorException("MkinstallGenerator :\n"
                                     + "targetDir not specified.");
      }
  }
  writeString(ilFile_, "\n");
  }
}

private String addEscapeCharacters( String tempString)
{
  int i = 0;
  String formattedString = "";

  for(i = 0; i < tempString.length(); i++)
  {
  char curChar = tempString.charAt(i);
  formattedString = formattedString + curChar;
  if (curChar == '\n' || curChar == '\'' )
          {
          formattedString = formattedString  + "\\";

          }
  }
  return formattedString;
}

private String checkForFileSeparator( String dirName )
{
  if( dirName != null )
  {
  if( dirName.trim().endsWith( ParserGeneratorInitiator._fileSeparator_ ) )
    return dirName;
  else
    return dirName.trim().concat( ParserGeneratorInitiator._fileSeparator_ );
  }
  else
  return dirName;
}
}

