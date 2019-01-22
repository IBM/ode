/*******************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 5/10/03 15:28:50
 * Date and Time File was extracted/checked out: 06/04/13 16:46:04
 ******************************************************************************/
package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;
import com.ibm.ode.lib.string.PlatformConstants ;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.io.Path;

/**
 * RPMGenerator : This class is responsible for extracting appropriate
 * information from the installEntities and FileEntities which are
 * present in the EntityTree at appropriate levels.  It writes out the
 * extracted information to certain files which will be the control files
 * that act as input to the packaging tool. The packaging tool considered here
 * is <I> RPM </I> .
 **/
public class RPMGenerator extends Generator implements GeneratorInterface
{

  // File output handle for the control file pcd.spec
  RandomAccessFile specFile_;
  private File fileName ;

  // this will hold the reference to the array of EntityTreeObjects
  // at level One. These ETO's represent the product info in the
  // package heirarchy for RPM.
  private ArrayList            levelOneArray_;

  // this will hold the reference to the array of EntityTreeObjects
  // at level Two. These ETO's represent the Fileset info in the
  // package heirarchy for RPM.
  private ArrayList            levelTwoArray_;

  // Some static final variables which will be used in generating error
  // and formatted strings
  static final private String REQD_FIELD_ERROR = " is a required attribute"
                                               + " for generating the package.";

  static final private int REQD     = 10;
  static final private int NOT_REQD = 11;

  static final private int STRINGTYPE = 12;
  static final private int SCALARTYPE = 13;

  public String prereq, requires, conflicts, provides, requiresList ;
  public String mainPkgName;

  String pkgControlDir = checkForFileSeparator( pkgControlDir_ );
  String shipRootDir = checkForFileSeparator( shipRootDir_ );

  /**
    * Constructor for RPMGenerator
    *
    * @param          shipRootDir :- Env Var => root dir. of the ship tree.
    * @param          context     :- Env Var => Context variable
    * @param          pkgControlDir  :- Env Var :- Output directory
    **/
  public RPMGenerator( String shipRootDir, String context, String pkgControlDir,
  String pkgType, String pkgClass, String pkgFixStrategy )
  {
    super( shipRootDir, context, pkgControlDir, pkgType, pkgClass,
    pkgFixStrategy ) ;

    // do any initialization here
  }

  /**
    * Generates the Metadata Files for a particular platform and packaging tool
    *
    * @param entityTreeRoot reference to an object which holds a structured IE
    * @param packageObject reference to an object which contains the IEs and FEs
    * @exception GeneratorException if any error is encountered
    **/
  public void generateTargetMetadataFiles( EntityTreeRoot entityTreeRoot ,
                                                        Package packageObject )
  throws GeneratorException
  {

    // Arrays to reference the EntityTreeObjects
    ArrayList levelArray;
    levelArray = entityTreeRoot.getLevelArray();

    // Get the level one array
    levelOneArray_ = (ArrayList)levelArray.get( 0 );

    // tmpVariables
    String tmpLog = null;
    try {

      // First create a new  control file, pcd.spec, in the pkgControlDir
      fileName = new File( pkgControlDir + "//pcd.spec" );
      fileName.delete();
      specFile_ = new RandomAccessFile( fileName, "rw" );

      specFile_.writeBytes("# File: " + fileName + "\n");
      specFile_.writeBytes("# Do not Modify!\n");
      specFile_.writeBytes("# This file has been automatically generated.\n");
      specFile_.writeBytes("# Any changes you make will be lost the next time");
      specFile_.writeBytes(" the CMF is parsed.\n");

      // write the Product definition information to the control file.
      tmpLog = writeProductInfoToSpecFile( );

      // get the level two array which contans entities
      // containing fileset information
      levelTwoArray_ = (ArrayList)levelArray.get( 1 );

      // write the FilesetAndinfo
      writeFilesetAndFileInfo();

      // write out the changelog;
      if ( tmpLog != null && tmpLog.toString().length() > 0 )
      {
         specFile_.writeBytes("\n# The log must appear at the bottom of the ");
         specFile_.writeBytes("# spec file and shoud be sorted descending ");
         specFile_.writeBytes("by date.\n");
         specFile_.writeBytes("# The only changelog appended to this file is ");
         specFile_.writeBytes("the one associated with the parent IE.\n");


         specFile_.writeBytes( "\n" + tmpLog + "\n" );
      }

      specFile_.close( ); // specFile_
    }
    catch ( IOException e )
    {
       throw new GeneratorException( "RPMGenerator :\n" + " " + e.toString() );
    }
      return;

  }


  // utility methods for writing tag : value; pairs

    /**
    * Write file directives to spec file.  Similar but different from
    * createTagScalar.
    *
    * @param attrib :- attribute name
    * @param tmpStr :- String value to be formatted
    * @param reqd  :- to indicate whether this is a required field or not.
    * @return String :- formatted String
    * @exception GeneratorException :- If error is encountered
    **/
  private String createDirective( String attrib, String cmfAttribName,
                                                      String tmpStr, int reqd )
  throws GeneratorException
  {
    String createdStr = "";
    if( reqd == REQD &&  tmpStr == null  )
    {
      throw new GeneratorException("RPMGenerator :\n "
                          + "Error : " + " The CMF attribute " + cmfAttribName
                          + "  < " + attrib + " > " + REQD_FIELD_ERROR );
    }
    if( tmpStr != null )
    {
      createdStr = "%" + tmpStr + " " ;
      return createdStr;
    }
    else
      return null;
  }

  /**
    * create an <tag> : <value> pair according to format required by the
    * spec file.
    *
    * @param attrib :- attribute name
    * @param tmpStr :- String value to be formatted
    * @param reqd  :- to indicate whether this is a required field or not.
    * @return String :- formatted String
    * @exception GeneratorException :- If error is encountered
    **/
  private String createTagScalar( String attrib, String cmfAttribName,
                                                      String tmpStr, int reqd )
  throws GeneratorException
  {
    String createdStr = "";
    if( reqd == REQD &&  tmpStr == null  )
    {
      throw new GeneratorException("RPMGenerator :\n "
                          + "Error : " + " The CMF attribute " + cmfAttribName
                          + "  < " + attrib + " > " + REQD_FIELD_ERROR );
    }
    if( tmpStr != null )
    {
      createdStr = attrib + ": " + tmpStr + "\n";
      return createdStr;
    }
    else
      return null;
  }

  /**
    * create an <tag>: <value> pair according to format required by
    * the spec file.
    *
    * @param attrib :- attribute name
    * @param tmpStr :- String value to be formatted
    * @param reqd  :- to indicate whether this is a required field or not.
    * @return String :- formatted String
    * @exception GeneratorException :- If error is encountered
    **/
  private String createTagStr( String attrib, String cmfAttribName,
                                                      String tmpStr, int reqd )
  throws GeneratorException
  {
    String createdStr = "";
    if( reqd == REQD &&  tmpStr == null  )
    {
      throw new GeneratorException("RPMGenerator :\n"
      + "The CMF attribute " + cmfAttribName
      + "  < " + attrib + " > " + REQD_FIELD_ERROR + "\n");
    }
    if( tmpStr != null )
    {
      createdStr = attrib + " " + tmpStr + "\n" ;
      return createdStr;
    }
    else
      return null;
  }

  /**
    * Write out the <b> %description </b> according to format required by
    * the spec file.
    *
    * @param attrib :- attribute name
    * @param tmpStr :- String value to be formatted
    * @param reqd  :- to indicate whether this is a required field or not.
    * @return String :- formatted String
    * @exception GeneratorException :- If error is encountered
    **/
  private String writeDescr( String attrib, String cmfAttribName,
                                    String tmpStr, int reqd, String entityName )
  throws GeneratorException
  {
    String createdStr = "";
    if( reqd == REQD &&  tmpStr == null  )
    {
      throw new GeneratorException("RPMGenerator : \n"
      + "             Error : " + " The CMF attribute " + cmfAttribName
      + "  < " + attrib + " > " + REQD_FIELD_ERROR + "\n");
    }
    if( tmpStr != null )
    {
      createdStr = "\n" + attrib + " " + entityName + "\n" + tmpStr + "\n" ;
      return createdStr;
    }
    else
      return null;
  }

  /**
    * create an <tag>: <value> pair according to format required by the
    * spec file  If <tag> is a configFile write it into the spec file
    * otherwise nothing happens.
    *
    * @param attrib :- attribute name
    * @param tmpStr :- String value to be formatted
    * @param reqd  :- to indicate whether this is a required field or not.
    * @return String :- formatted String
    * @exception GeneratorException :- If error is encountered
    **/
  private String createTagArray( String attrib, String cmfAttribName,
                                            ArrayList tmpArray, int reqd, int type )
  throws IOException, GeneratorException
  {
    ListIterator i;
    StringBuffer  tmpBuffer = new StringBuffer();
    String        tmpStr = "", tmpType = "", tmpFileName = "";
    File cfgFile, absCfgFile, relCfgFile ;

    if( reqd == REQD &&  tmpArray == null  )
      throw new GeneratorException("RPMGenerator :\n "
                        + " Error : " +  " The CMF attribute " + cmfAttribName
                        + "  < " + attrib + " > " + REQD_FIELD_ERROR );
    if( tmpArray != null )
    {
      i = tmpArray.listIterator();
      while( i.hasNext() )
      {
        Object tmpobj = i.next();
        tmpBuffer.append(" ");
        if( tmpobj instanceof String )
        {
          tmpStr = (String)tmpobj;
          if( type == SCALARTYPE )
            tmpBuffer.append( tmpStr.trim() );
          if(type == STRINGTYPE )
            tmpBuffer.append( tmpStr.trim() );
        }
        else if( tmpobj instanceof PgSpecialType )
        {
          PgSpecialType tmpPgSpecialType = (PgSpecialType)tmpobj;
          tmpStr = tmpPgSpecialType.getValue();
          if( type == SCALARTYPE )
            tmpBuffer.append( tmpStr.trim() );
        }
        else if( tmpobj instanceof ReqType )
        {
          ReqType tmpReqType = (ReqType)tmpobj;
          if(cmfAttribName.trim().equals("configFiles"))
          {
            if ( (( tmpFileName = (String)tmpReqType.getValue() ) != null)
              && ((((String)tmpReqType.getValue()).trim()).length() !=0)  )
            {
              // tmpType describes the configFile preinstall postinstall...
              // For subentities we append the entityName
              tmpType = ((String)tmpReqType.getType()).trim();
              //for some invalid keywords generate the appropriate keywords
              if (tmpType.equalsIgnoreCase("preinstall"))
                tmpType = "pre";
              else if (tmpType.equalsIgnoreCase("postinstall"))
                tmpType = "post";
              else if (tmpType.equalsIgnoreCase("preuninstall"))
                tmpType = "preun";
              else if (tmpType.equalsIgnoreCase("postuninstall"))
                tmpType = "postun";

              if ( attrib != "" )
                tmpType = tmpType+" "+attrib;

              // tmpFileName is the name of the configFile described.
              // Check to see if its an absolute path ( starts with a forward
              // slash) or assume its a relative path and append tostage
              if( tmpFileName.trim().startsWith( ParserGeneratorInitiator._fileSeparator_ ) )
                cfgFile = new File ( tmpFileName ) ;
              else
                cfgFile = new File ( insertFileSeparator( shipRootDir_, tmpFileName ) ) ;

              if ( cfgFile.exists() )
              {
                tmpStr = ("\n%"+tmpType+"\n\n"+copyFileToStr( cfgFile.toString() )+"\n\n");
                specFile_.writeBytes( tmpStr );
              }
              else
              {
                throw new GeneratorException("RPMGenerator : \n"
                    + "The file in the CMF attribute \"" + cmfAttribName + "   " + cfgFile
                    + "\" does not exist." );
              }
            }
          }
        } // End if "instanceof ReqType
      }  // end for each array element.
      if ( tmpBuffer.toString().trim().length() > 0)
        return attrib + ": " + tmpBuffer.toString().trim() + "\n";
      else
        return null;
    }
    else
      return null;
    // end if "tmpArray != null"
  }

  /* set requisites */
  public String setRequisites( String attrib, ArrayList requisites )
         throws GeneratorException
  {
    /* One for each type of requisite ODE supports.  Leave validation up
       to the the generators. Return the value of the requested attribute
       as an unformatted string or null.  Leave formatting up the the
       generators. */

   String        tmpStr = "";
   String        tmpType = "";
   String        tmpFileName = "";

   StringBuffer  tmpBuffer = new StringBuffer();
   ListIterator i ;
   String type, value, desc ;

    if( requisites == null )
      return null ;
   
    i = requisites.listIterator();
    while( i.hasNext() )
    {
      tmpBuffer.append(" ");
      ReqType reqType = (ReqType)i.next();

      if (attrib.trim().equals("Prereq"))
      {
        if( ( type = (String)reqType.getType() ) != null )
          if( type.trim().equalsIgnoreCase("P") ||
              type.trim().equalsIgnoreCase("Pre") )
          {
            if( ( value = (String)reqType.getValue() ) != null )
              tmpBuffer.append( value.trim() );
            if( ( desc = (String)reqType.getDescription() ) != null )
              tmpBuffer.append( desc.trim() );
          }
      }
      else if (attrib.trim().equals("Coreq"))
      {
        if( ( type = (String)reqType.getType() ) != null )
          if( type.trim().equalsIgnoreCase("C") ||
              type.trim().equalsIgnoreCase("Co") )
          {
            if( ( value = (String)reqType.getValue() ) != null )
              tmpBuffer.append( value.trim() );
            if( ( desc = (String)reqType.getDescription() ) != null )
              tmpBuffer.append( desc.trim() );
          }
      }
      else if (attrib.trim().equals("Conflicts"))
      {
        if( ( type = (String)reqType.getType() ) != null )
          if( type.trim().equalsIgnoreCase("X") ||
            type.trim().equalsIgnoreCase("neg") )
          {
            if( ( value = (String)reqType.getValue() ) != null )
              tmpBuffer.append( value.trim() );
            if( ( desc = (String)reqType.getDescription() ) != null )
              tmpBuffer.append( desc.trim() );
          }
      }
      else if (attrib.trim().equals("Provides"))
      {
        if( ( type = (String)reqType.getType() ) != null )
          if( type.trim().equalsIgnoreCase("V") ||
              type.trim().equalsIgnoreCase("Pro") )
          {
            if( ( value = (String)reqType.getValue() ) != null )
              tmpBuffer.append( value.trim() );
            if( ( desc = (String)reqType.getDescription() ) != null )
              tmpBuffer.append( desc.trim() );
          }
      }
      else
        throw new GeneratorException("RPMGenerator :\n "
            + "Sorry. " + tmpStr.trim() + " is an invalid requisite type.\n") ;
    }

    if ( tmpBuffer.toString().trim().length() > 0)
        return tmpBuffer.toString().trim() ;
      else
        return null;

  } /* end set requisiets */

  /* validate requisites */
  public void validateRequisites( ArrayList requisites )
      throws GeneratorException, IOException
  {
    ListIterator i ;
    String type ;
    ReqType reqType ;

    if (requisites != null )
    {
      i = requisites.listIterator();
      while( i.hasNext() )
      {
        Object tmpobj = i.next();
        if( tmpobj instanceof ReqType )
        {
          reqType = (ReqType)tmpobj;
          type = (String)reqType.getType() ;
          type = type.trim();
          if ( type != null )
            if (!( type.equalsIgnoreCase("P") || type.equalsIgnoreCase("Pre") ||
               type.equalsIgnoreCase("C") || type.equalsIgnoreCase("Co" ) ||
               type.equalsIgnoreCase("X") || type.equalsIgnoreCase("neg") ||
               type.equalsIgnoreCase("V") || type.equalsIgnoreCase("pro") ))
              throw new GeneratorException("RPMGenerator :\n "
                 + "Sorry. " + type.trim() + " is an invalid requisite type.\n") ;
        }
      }
    }
  }

  /* get requisites */
  public void getRequisites( ArrayList requisites, String entityName )
     throws GeneratorException, IOException
  {
     /* All we do here is get the requisites in an array and validate. If the
        array is not empty we set all non-null requisites to print
        them later.  If the array is empty there is nothing to validate
        or print.  Any platform specific issues such as formatting are
        handled here as well.
     */

      try
      {
        validateRequisites( requisites ) ;
      }
      catch ( GeneratorException e )
      {
        throw new GeneratorException( e.toString() );
      }
      catch ( IOException e )
      {
        throw new GeneratorException( e.toString() ) ;
      }

      // Prereq
      prereq =  setRequisites("Prereq", requisites );
      if ( prereq != null )
      {
         prereq = "Prereq: " + prereq + "\n" ;
      }

      // Requires
      if (!pkgClass_.equalsIgnoreCase("SP"))
      {
        requires = setRequisites("Coreq", requisites );
        if ( requires != null )
           requires = "Requires: " + requires + "\n" ;
      }

      // Conflicts
      conflicts = setRequisites("Conflicts", requisites );
      if ( conflicts != null )
      {
         conflicts = "Conflicts: " + conflicts + "\n" ;
      }

      // Provides - Capabilities that the entity provides when installed
      provides = setRequisites("Provides", requisites );
      if ( provides != null )
      {
         provides = "Provides: " + provides + "\n" ;
      }
  }

  /* print requisites */
  public void printRequisites( )
  throws IOException, GeneratorException
  {
      /* write out the requisites in the format required by the package tool */

      // Prereq
      if(  prereq != null  && prereq.toString().length() > 0 )
        specFile_.writeBytes( prereq.toString() );

      // Requires - makes RPM aware that the package needs certain capabilities
      //            to operate properly.
      if (pkgClass_.equalsIgnoreCase("SP"))
      {
        // RequiresList - must get values from PATCH_REQUIRES_LIST because
        // cmf values will be incorrect
        if (  requiresList != null)
           specFile_.writeBytes( requiresList );
      }
      else
      {
         if(  requires != null && requires.toString().length() > 0 )
           specFile_.writeBytes( requires.toString() );
      }

      // Conflicts
      if(  conflicts != null && conflicts.toString().length() > 0 )
        specFile_.writeBytes( conflicts.toString() );

      // Provides - Capabilities that the entity provides when installed.
      if(  provides != null && provides.toString().length() > 0 )
        specFile_.writeBytes( provides.toString() );

  } /* end printRequisites */

  /**
    * Utility function for reading from a file and writing the contents
    * to a string.
    *
    * @param fileName :- name of the file to be read
    * @return String :- formatted String
    * @exception GeneratorException :- If error is encountered
    **/
  public String copyFileToStr( String fileName )
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
      throw new GeneratorException("RPMGenerator : \n"
                                 + " " + fileName + " File " +
                                 " cannot be opened for reading.\n"
                                 );
    }
    catch (IOException e)
    {
      throw new GeneratorException("RPMGenerator : \n"
                                 + " " + fileName + " File "  +
                                 " cannot be opened for reading.\n"
                                 );
    }
  }

  /**
   * Extracts information from the IEs in First Level and writes out
   * productuct info.
   *
   * @return string
   * @exception GeneratorException if error is encountered
   */
  public String writeProductInfoToSpecFile()
    throws IOException, GeneratorException
  {
    ListIterator levelOneArrayIterator;

    // References to objects needed by the generator
    EntityTreeObject entityTreeObjRef;
    InstallEntity    installEntityRef;
    EntityTreeObject childEntityObjRef;
    ArrayList        entityChildren;
    ListIterator     entityChildrenIterator;

    // variables to hold various type of attribute values
    String        tmpStr;
    String        createdStr;
    String        entityName = "";
    String        tmpLog = "" ;
    String        pkgVer = "";
    String        pkgRel = "";
    StringBuffer  tmpStrBuffer;
    ArrayList     tmpArray;
    ArrayList     requisites ;
    ListIterator  i;
    PgSpecialType tmpSpclType;
    ReqType       tmpReqType;
    long          fp;

    // used in service packaging
    Hashtable prodRequiresTable = null;

    if (pkgClass_.equalsIgnoreCase("SP"))
      prodRequiresTable = populateHashtable("PATCH_REQUIRES_LIST");

    levelOneArrayIterator = levelOneArray_.listIterator();
    while( levelOneArrayIterator.hasNext() )
    {
      entityTreeObjRef = (EntityTreeObject)levelOneArrayIterator.next();

      installEntityRef = entityTreeObjRef.getInstallEntityReference();
      if( installEntityRef == null )
      {
        throw new GeneratorException( "RPMGenerator :\n "
        + "Unable to obtain the InstallEntity Reference. "
        + "No product definition has been given.\n" );
      }

      // Once you get a valid installEntity, start writing the control file
      // NOTE:  The control file is case-insensitive, but the CMF is not.

      // Summary:
      tmpArray  =  installEntityRef.getFullEntityName();
      if( tmpArray != null )
      {
        tmpStr = (String)tmpArray.get( 0 );
        createdStr = createTagScalar("Summary", "fullEntityName", tmpStr, REQD);
      }
      else
      {
        throw new GeneratorException("RPMGenerator :\n"
        + "fullEntityName in Entity Info Stanza is a required field\n");
      }
      if(  createdStr != null )
        specFile_.writeBytes( createdStr );

      // Name:
      if (pkgClass_.equalsIgnoreCase("SP"))
      {
        tmpStr = System.getProperty("PATCH_NAME");

        if (tmpStr == null || tmpStr.length() == 0)
          throw new GeneratorException("RPMGenerator :\nPATCH_NAME " +
              "not found : required value for service packaging with RPM.");

        mainPkgName = installEntityRef.getEntityName();

        if (mainPkgName == null || mainPkgName.length() == 0)
          throw new GeneratorException("RPMGenerator :\n" +
             "EntityName not found : required attribute for RPM");
      }
      else
      {
        tmpStr = installEntityRef.getEntityName();
      }

      createdStr =  createTagScalar( "Name", "entityName", tmpStr, REQD );
        specFile_.writeBytes( createdStr );

      // Version:
      tmpStr = installEntityRef.getVersion() + "."
      + installEntityRef.getRelease() + "." + installEntityRef.getMaintLevel();
      createdStr = createTagScalar( "Version", "version", tmpStr, REQD );
        specFile_.writeBytes( createdStr );

      if (pkgClass_.equalsIgnoreCase("SP"))
        pkgVer = tmpStr;

      // Release:
      tmpStr = installEntityRef.getFixLevel();
      createdStr = createTagScalar( "Release", "fixLevel",  tmpStr, REQD );
        specFile_.writeBytes( createdStr );

      if (pkgClass_.equalsIgnoreCase("SP"))
        pkgRel = tmpStr;

      // Serial
      tmpStr = installEntityRef.getSerialNumber();
      createdStr = createTagStr("Serial: ", "serialNumber", tmpStr, NOT_REQD);
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // Distribution
      tmpStr = installEntityRef.getDistribution();
      createdStr = createTagStr("Distribution: ", "distribution", tmpStr,
                                                                  NOT_REQD );
      if( createdStr != null )
         specFile_.writeBytes( createdStr );

      // Group:
      tmpStr =  installEntityRef.getCategory();
      createdStr =  createTagStr( "Group: ", "category", tmpStr, REQD );
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // packager:
      tmpStr =  installEntityRef.getContactName();
      createdStr = createTagStr("Packager: ", "contactName", tmpStr, NOT_REQD);
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // Vendor string
      tmpStr = installEntityRef.getVendorName();
      createdStr = createTagStr( "Vendor: ", "vendorName", tmpStr, NOT_REQD );
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // url:
      tmpArray = installEntityRef.getUrl();
      createdStr = createTagArray("URL", "url", tmpArray, NOT_REQD, SCALARTYPE);
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // copyright:
      tmpSpclType = installEntityRef.getCopyright();

      // check to see whether it is a file or a string
      if( tmpSpclType != null )
      {
        if(tmpSpclType.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
        {
          tmpStr = copyFileToStr( shipRootDir + tmpSpclType.getValue() );
          createdStr = createTagStr("Copyright: ", "copyright", tmpStr, REQD);
          specFile_.writeBytes( createdStr );
        }
        else if( tmpSpclType.getType() == ParserGeneratorEnumType.STRING )
        {
          tmpStr = tmpSpclType.getValue();
          createdStr =  createTagStr("Copyright: ", "copyright", tmpStr, REQD);
          specFile_.writeBytes( createdStr );
        }
      }

      // Prereq - must already be installed.
      // Requires - must be installed for the package to work properly.
      // Conflicts - can not be installed or the package won't work.
      // Provides - Capabilities that the entity provides when installed.

      if (pkgClass_.equalsIgnoreCase("SP"))
      {
        // RequiresList - gets its values from the PATCH_REQUIRES_LIST variable
        requiresList = genRequiresList(mainPkgName, pkgVer, pkgRel,
                                       entityName, prodRequiresTable);

        if ( requiresList != null )
           requiresList = "Requires: " + requiresList + "\n" ;
      }

      requisites = installEntityRef.getRequisites();

      if ( requisites != null )
      {
        getRequisites(requisites, entityName);
        printRequisites();
      }
      else
      {
        if (pkgClass_.equalsIgnoreCase("SP"))
           specFile_.writeBytes( requiresList );
      }

      // autoreqprov - turn on or off auto-dependency generation. Default is on
      tmpStr = installEntityRef.getAutoreqprov();
      createdStr = createTagStr("AutoReqProv:", "autoreqprov", tmpStr,NOT_REQD);
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // BuildRoot: - note that we overwrite the value with shipRootDir
      createdStr = createTagStr("BuildRoot:", "TOSTAGE", shipRootDir, REQD ) ;
      if(  createdStr != null )
        specFile_.writeBytes( createdStr );

      // %Prefix:
      tmpSpclType = installEntityRef.getInstallDir();
      if( (  tmpSpclType != null ) &&
      ( tmpSpclType.getType() == ParserGeneratorEnumType.STRING ) )
      {
        tmpStr = tmpSpclType.getValue();
        createdStr = createTagStr("Prefix:", "installDir", tmpStr, NOT_REQD );
        if ( createdStr != null )
          specFile_.writeBytes( createdStr );
      }

      // %exclusivearch:
      tmpArray = installEntityRef.getExclusiveArch();
      createdStr =  createTagArray("ExclusiveArch", "exclusiveArch", tmpArray,
      NOT_REQD, STRINGTYPE );
      if(  createdStr != null )
        specFile_.writeBytes( createdStr );

      // %excludearch:
      tmpArray = installEntityRef.getExcludeArch();
      createdStr = createTagArray("ExcludeArch", "excludeArch", tmpArray,
      NOT_REQD, STRINGTYPE );
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // %exclusiveos:
      tmpArray = installEntityRef.getExclusiveOS();
      createdStr = createTagArray("ExclusiveOs", "exclusiveOS", tmpArray,
      NOT_REQD, STRINGTYPE );
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // %excludeos:
      tmpArray = installEntityRef.getExcludeOS();
      createdStr = createTagArray("ExcludeOs", "excludeOS", tmpArray, NOT_REQD,
      STRINGTYPE );
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // %description
      tmpSpclType = installEntityRef.getDescription();

      if( tmpSpclType == null  )
      {
        throw new GeneratorException("RPMGenerator :\n"
        + "description in Entity Info Stanza is a required field\n");
      }

      // check to see whether it is a file or a string
      if( tmpSpclType.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE )
      {
        tmpStr = copyFileToStr( shipRootDir + tmpSpclType.getValue() );
        createdStr = writeDescr("%description", "description", tmpStr, REQD,"");
        specFile_.writeBytes( createdStr );
      }
      else if( tmpSpclType.getType() == ParserGeneratorEnumType.STRING )
      {
        tmpStr = tmpSpclType.getValue();
        createdStr = writeDescr("%description", "description", tmpStr, REQD,"");
        specFile_.writeBytes( createdStr );
      }

      // configFiles
      tmpArray = installEntityRef.getConfigFiles();

        createdStr =  createTagArray("", "configFiles", tmpArray,
        NOT_REQD, SCALARTYPE );

      if(  createdStr != null )
        specFile_.writeBytes( createdStr );

      // At the very end close this product stanza for each entityTreeObject
        specFile_.writeBytes( "\n" );

      tmpStr = entityTreeObjRef.getName();
      // The following code doesn't get executed when the tmpStr is null.
      // When the if condition is commented out, the files from the cmf in
      // the entity (product) stanza will be written to the SPEC file and
      // the %file entityname (i.e. - %file bin) will be written in the
      // SPEC file as %file (blank spaces) because no entityname exist.
      if (tmpStr != null)
      {
         entityChildren = entityTreeObjRef.getChildReferenceArray();
         if( entityChildren == null )
         {
            return null ;
         }

         ArrayList childrenArray = filesInShipTree(entityChildren);

         if( childrenArray != null )
         {
            // %files
            specFile_.writeBytes( "\n%files\n" ) ;

            i = childrenArray.listIterator();
            while( i.hasNext() )
            {
               childEntityObjRef = (EntityTreeObject)i.next();
               writeFileInfo( childEntityObjRef );
               // No more child files exit.
            }
         }
         return null ;
      }

      // %changelog - has to go at end of spec file.  Only one is printed out
      // And thats the one that is associated with the parent level IE.
      tmpSpclType = installEntityRef.getChangeLog();

      // check to see whether it is a file or a string
      if( tmpSpclType != null )
      {
        if(tmpSpclType.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
        {
          tmpStr = copyFileToStr( shipRootDir + tmpSpclType.getValue() );
          tmpLog = createTagStr( "%changelog\n", "changeLog", tmpStr, REQD ) ;
        }
        else if( tmpSpclType.getType() == ParserGeneratorEnumType.STRING )
        {
          tmpStr = tmpSpclType.getValue();
          tmpLog = createTagStr( "%changelog\n", "changeLog", tmpStr, REQD ) ;
        }
      }
    }
    return tmpLog ;
  }

  /**
    * Extracts IE and FE info from second and third level and writes
    * fileset and file info
    *
    * @return void
    * @exception GeneratorException if error is encountered
  **/
  public void writeFilesetAndFileInfo()
  throws IOException, GeneratorException
  {
    ListIterator levelTwoArrayIterator;

    // References to objects needed by the generator
    EntityTreeObject entityTreeObjRef;
    EntityTreeObject childEntityObjRef;
    ArrayList        entityChildren;
    ListIterator     entityChildrenIterator;
    InstallEntity    installEntityRef;

    // variables to hold various type of attribute values
    String        tmpStr;
    String        createdStr;
    String        entityName;
    StringBuffer  tmpStrBuffer;
    String        pkgVer = "";
    String        pkgRel = "";
    ArrayList     tmpArray;
    ArrayList     requisites;
    ListIterator  i;
    PgSpecialType tmpSpclType;
    ReqType       tmpReqType;

    // used in service packaging
    Hashtable requiresTable = null;

    if (pkgClass_.equalsIgnoreCase("SP"))
      requiresTable = populateHashtable("PATCH_REQUIRES_LIST");

    levelTwoArrayIterator = levelTwoArray_.listIterator();
    while( levelTwoArrayIterator.hasNext() )
    {
      entityTreeObjRef = (EntityTreeObject)levelTwoArrayIterator.next();

      entityChildren = entityTreeObjRef.getChildReferenceArray();
      if( entityChildren == null )
      {
         return;
      }

      installEntityRef = entityTreeObjRef.getInstallEntityReference();
      if( installEntityRef == null )
      {
         return;
      }

      // once you get a valid installEntity, start writing out to the file
      // as required by the packaging tool

      ArrayList childrenArray = filesInShipTree(entityChildren);
      if (childrenArray == null || childrenArray.isEmpty())
        return;

      // %package <package-name>
      tmpStr = installEntityRef.getEntityName();
      if( tmpStr != null )
      {
        entityName = tmpStr;
        specFile_.writeBytes("# Any tag placed after a %package directive ");
        specFile_.writeBytes("will apply only to that subpackage.\n");
        specFile_.writeBytes("# When the -n option is added to the %package directive, ");
        specFile_.writeBytes("it directs RPM to use \n");
        specFile_.writeBytes("# the name specified on the %package ");
        specFile_.writeBytes("line as the entire package name.\n");

        specFile_.writeBytes( "\n%package " + tmpStr + "\n" );
      }
      else
        throw new GeneratorException("RPMGenerator : \n "
        + " Error : The CMF attribute entityName  " + REQD_FIELD_ERROR + "\n");

      // Summary:
      tmpArray  =  installEntityRef.getFullEntityName();
      if( tmpArray != null )
      {
        tmpStr = (String)tmpArray.get( 0 );
        createdStr = createTagScalar("Summary", "fullEntityName", tmpStr, REQD);
      }
      else
      {
        throw new GeneratorException("RPMGenerator :\n "
        + "fullEntityName in Entity Info Stanza is a required field\n");
      }
      if(  createdStr != null )
        specFile_.writeBytes( createdStr );

      // Version:
      tmpStr = installEntityRef.getVersion() + "."
      + installEntityRef.getRelease() + "." + installEntityRef.getMaintLevel();
      createdStr = createTagScalar( "Version", "version", tmpStr, REQD );
      specFile_.writeBytes( createdStr  );

      if (pkgClass_.equalsIgnoreCase("SP"))
        pkgVer = tmpStr;

      // Release:
      tmpStr = installEntityRef.getFixLevel();
      createdStr = createTagScalar( "Release", "fixLevel",  tmpStr, REQD );
      specFile_.writeBytes( createdStr );

      if (pkgClass_.equalsIgnoreCase("SP"))
        pkgRel = tmpStr;

      // Serial
      tmpStr = installEntityRef.getSerialNumber();
      createdStr = createTagStr("Serial:", "serialNumber", tmpStr, NOT_REQD);
      if( createdStr != null )
      {
        specFile_.writeBytes("# This tag is used to help RPM determine ");
        specFile_.writeBytes("version number ordering.\n ");
        specFile_.writeBytes( createdStr );
      }

      // Distribution
      tmpStr = installEntityRef.getDistribution();
      createdStr = createTagStr("Distribution:", "distribution", tmpStr,
                                                                NOT_REQD );
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

     // Group:
     tmpStr =  installEntityRef.getCategory();
     createdStr =  createTagStr( "Group: ", "category", tmpStr, REQD );
     if(  createdStr != null )
       specFile_.writeBytes( createdStr );

      // packager:
      tmpStr =  installEntityRef.getContactName();
      createdStr = createTagStr( "Packager: ", "contactName", tmpStr,NOT_REQD);
      if(  createdStr != null )
        specFile_.writeBytes( createdStr );

      // Vendor string
      tmpStr = installEntityRef.getVendorName();
      createdStr = createTagStr( "Vendor: ", "vendorName", tmpStr, NOT_REQD );
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // url:
      tmpArray = installEntityRef.getUrl();
     createdStr = createTagArray("URL", "url", tmpArray, NOT_REQD, SCALARTYPE);
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // copyright
      tmpSpclType = installEntityRef.getCopyright();
      // check to see whether it is a file or a string
      if( tmpSpclType != null )
      {
        if( tmpSpclType.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE )
        {
          tmpStr = copyFileToStr( shipRootDir + tmpSpclType.getValue() );
          createdStr = createTagStr("Copyright: ", "copyright", tmpStr, NOT_REQD);
          specFile_.writeBytes( createdStr );
        }
        else if( tmpSpclType.getType() == ParserGeneratorEnumType.STRING )
        {
          tmpStr = tmpSpclType.getValue();
          createdStr = createTagStr("Copyright:", "copyright",tmpStr,NOT_REQD);
          specFile_.writeBytes( createdStr );
        }
      }

      // Prereq - must already be installed.
      // Requires - must be installed for the package to work properly.
      // Conflicts - can not be installed or the package won't work.
      // Provides - Capabilities that the entity provides when installed.

      if (pkgClass_.equalsIgnoreCase("SP"))
      {
        // RequiresList - must be installed for the package to work properly.
        //              - gets its values from the PATCH_REQUIRES_LIST variable
        requiresList = genRequiresList(mainPkgName, pkgVer, pkgRel,
                                       entityName, requiresTable);

        if ( requiresList != null )
           requiresList = "Requires: " + requiresList + "\n" ;
      }

      requisites = installEntityRef.getRequisites();

      if ( requisites != null )
      {
        getRequisites(requisites, entityName);
        printRequisites();
      }
      else
      {
        if (pkgClass_.equalsIgnoreCase("SP"))
           specFile_.writeBytes( requiresList );
      }

      // autoreqprov - turn on or off auto-dependency generation. Default is on
      tmpStr = installEntityRef.getAutoreqprov();
      createdStr = createTagStr("AutoReqProv:", "autoreqprov", tmpStr,NOT_REQD);
      if( createdStr != null )
        specFile_.writeBytes( createdStr );

      // %description
      tmpSpclType = installEntityRef.getDescription();

      if( tmpSpclType == null  )
      {
        throw new GeneratorException("RPMGenerator :\n"
        + "description in Entity Info Stanza is a required field\n");
      }
      // check to see whether it is a file or a string
      if( tmpSpclType.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE )
      {
        tmpStr = copyFileToStr( shipRootDir + tmpSpclType.getValue() );
        createdStr = writeDescr("%description", "description", tmpStr, REQD,
                                                                    entityName);
        specFile_.writeBytes( createdStr );
      }
      else if( tmpSpclType.getType() == ParserGeneratorEnumType.STRING )
      {
        tmpStr = tmpSpclType.getValue();
        createdStr = writeDescr("%description", "description", tmpStr, REQD,
                                                                    entityName);
        specFile_.writeBytes( createdStr );
      }

      // configFiles
      tmpArray = installEntityRef.getConfigFiles();
      createdStr =  createTagArray(entityName, "configFiles", tmpArray,
      NOT_REQD, SCALARTYPE );
      if(  createdStr != null )
        specFile_.writeBytes( createdStr );

      // get fileList and start writing out the files

      // %files <package-name>
      tmpStr = installEntityRef.getEntityName();
      if( tmpStr != null )
        specFile_.writeBytes( "\n%files " + tmpStr + "\n" );
      else
        throw new GeneratorException("RPMGenerator :\n "
        + " Error: The CMF attribute entityName  " + REQD_FIELD_ERROR + "\n");

      if( tmpStr != null )
      {
        i = childrenArray.listIterator();
        while( i.hasNext() )
        {
          childEntityObjRef = (EntityTreeObject)i.next();
          writeFileInfo( childEntityObjRef );
        }
      }
    }
  }

  /**
   * Returns a hashtable after populating it with the mappings of subpackage
   * to their corresponding values
   * Called from writeFilesetAndFileInfo
   *
   * @param makefileVar the makefile variable used for parsing
   * @see #writeFilesetAndFileInfo
   */
  private Hashtable populateHashtable( String makefileVar )
  {
     String fileSetList = System.getProperty(makefileVar);

     if (fileSetList == null || fileSetList.length() == 0)
       return null;

     String fileEntry, fileEntryLHS, fileEntryRHS;
     int openingDelim;
     Hashtable ht = new Hashtable();
     StringTokenizer colonTokenizer = new StringTokenizer(fileSetList, ":");

     while (colonTokenizer.hasMoreTokens())
     {
       fileEntry = colonTokenizer.nextToken();

       // @ is the delimiter separating the subpackage and its corresponding values
       // in a token
       openingDelim = fileEntry.indexOf("@");
       if (openingDelim != -1)
       {
         fileEntryLHS = fileEntry.substring(0, openingDelim);
         if (fileEntryLHS == null || fileEntryLHS.length() == 0)
         {
           // Skip this entry
           System.err.println("Warning: " + makefileVar +
                              " is formatted incorrectly");
           continue;
         }
         fileEntryRHS = fileEntry.substring(openingDelim + 1,
                                            fileEntry.length());
         if (fileEntryRHS == null && fileEntryRHS.length() == 0)
           fileEntryRHS = "";
         ht.put(fileEntryLHS, fileEntryRHS);
       }
       else
       {
         System.err.println("Warning: Invalid format in " + makefileVar);
       }
     }
     return ht;
  } // end method populateHastTable

  /**
   * Returns an array of files, links, directories that exist in the shiptree
   * or need to be included in the SPEC file for a Fileset
   * Called from within writeFilesetAndFileInfo
   *
   * @param childFileArray An array of references to child entities
   * @exception GeneratorException if an error occurs.
   * @see #writeFilesetAndFileInfo
   */
  private ArrayList filesInShipTree( ArrayList childFileArray )
     throws GeneratorException
  {
     EntityTreeObject childETO;
     FileEntity FERef;
     PackageData curPD;
     ListIterator fileIterator;
     ArrayList includeFiles = new ArrayList();
     ArrayList pDataArray;
     String sourceDir, sourceFile, fullSourceDir, fullSourceFile, fileType;
     File childFile;

     // If it's not SP, include all the files in the shiptree
     if (!pkgClass_.equalsIgnoreCase("SP"))
        return childFileArray;

     fileIterator = childFileArray.listIterator();
     while (fileIterator.hasNext())
     {
       try
       {
         childETO = (EntityTreeObject)fileIterator.next();
         FERef = childETO.getFileEntityReference();
         pDataArray = FERef.getPackageData();
         if (pDataArray == null || pDataArray.isEmpty())
           continue;
         curPD = (PackageData)pDataArray.get(0);
         fileType = curPD.getFileType();
         sourceDir = FERef.getSourceDir();
         if (sourceDir == null)
         {
           throw new GeneratorException("RPMGenerator :\n" +
              "Required attribute sourceDir for file Stanza not found.");
         }

         // get absolute path to the source directory
         fullSourceDir = insertFileSeparator(shipRootDir_, sourceDir);

         if( fileType.equalsIgnoreCase("f") ||
             fileType.equalsIgnoreCase("file") ||
             fileType.equalsIgnoreCase("H") ||
             fileType.equalsIgnoreCase("S") ||
             fileType.equalsIgnoreCase("Symlink") )
         {
           sourceFile = FERef.getSourceFile();
           if (sourceFile == null)
           {
              throw new GeneratorException("RPMGenerator :\n" +
                "Required field sourceFile not found in file stanza.");
           }

           // Check for the existence in the shiptree if it is a file
           if( fileType.equalsIgnoreCase("f") ||
               fileType.equalsIgnoreCase("file"))
           {
             // get the absolute path for the file
             fullSourceFile = insertFileSeparator(fullSourceDir, sourceFile);
             childFile = new File(fullSourceFile);
             if (childFile.isFile())
               includeFiles.add(childETO);
           }
           else
           {
             // It is a Symbolic link
             // append the values of sourceDir and sourceFile obtained from the
             // CMF and check if this entry is listed in PATCH_NEW_LINKS
             // If found, include it in the SPEC file or else ignore it
             fullSourceFile = insertFileSeparator(sourceDir, sourceFile);
             if (isNewLinkOrDir(fullSourceFile, "PATCH_NEW_LINKS"))
               includeFiles.add(childETO);
           }
         }
         else if( fileType.equalsIgnoreCase("d") ||
             fileType.equalsIgnoreCase("dir"))
         {
           // It is a directory
           // include this directory in the SPEC file if it is listed in
           // PATCH_NEW_DIRS
           if (isNewLinkOrDir(sourceDir, "PATCH_NEW_DIRS"))
             includeFiles.add(childETO);
         }
       }
       catch (Exception ex)
       {
         throw new GeneratorException("RPMGenerator :\nObject of " +
            "type ETO expected in ETO.ChildETOArray\n\n" + ex.toString());
       }

     }
     return includeFiles;
  } // end method filesInShipTree

  /**
   * Parses a makefile variable looking for a specific entry and returns true
   * if it is found, else returns false. Called from filesInShipTree
   *
   * @param fileEntry the entry to be searched
   * @param makefileVar the makefile variable used for parsing
   */
  private boolean isNewLinkOrDir( String fileEntry,
                                  String makefileVar )
  {
     String fileList = System.getProperty(makefileVar);
     String token;
     String tempString;

     // If fileEntry is a directory, remove trailing slash
     if (fileEntry.endsWith("/"))
     {
        tempString = fileEntry.substring(0, fileEntry.length()-1);
     }
     else
        tempString = fileEntry;

     if (fileList == null || fileList.length() == 0)
       return false;

     StringTokenizer st = new StringTokenizer(fileList, ":");
     while (st.hasMoreTokens())
     {
       token = st.nextToken();

       if (token != null && token.length() != 0 && token.equals(tempString))
       {
         return true;
       }
     }
     return false;
  }

  /**
   * Returns entryInSPEC, generates requires tags in the SPEC file
   * for a fileset. Used in service packaging
   * Called from within writeFilesetAndFileInfo
   *
   * @param mainPackage the base product name for this fileset
   * @param subPackage  the name of subpackage
   * @param fileSetTable An hashtable containing the mappings of the filset to
   * its corresponding list
   *
   * @exception GeneratorException if an error occurs.
   * @see #writeFilesetAndFileInfo
   */
  private String genRequiresList(String mainPackage, String pkgVersion,
                                 String pkgRelease, String subPackage,
                                 Hashtable fileSetTable )
     throws GeneratorException
  {
     String fileSetList, entryInSPEC = "";
     StringTokenizer semicolonTokenizer;
     int openingDelim;
     boolean foundFileSet = false;
     String makefileVar = "PATCH_REQUIRES_LIST";

     // check if the hashtable is null or empty
     if (fileSetTable != null && !fileSetTable.isEmpty())
     {
       if (fileSetTable.containsKey(mainPackage) ||
           fileSetTable.containsKey(subPackage))
       {
         // the hashtable contains this fileset as a key, so get its value
         if (mainPackage != null && subPackage == null ||
             subPackage.length() == 0)
         {
           fileSetList = (String) fileSetTable.get(mainPackage);
         }
         else
           fileSetList = (String) fileSetTable.get(subPackage);

         foundFileSet = true;

         if ((fileSetList != null) && (fileSetList.length() != 0))
         {
           // parse the value obtained from the hashtable with semi-colon
           // as the delimiter and generate the string to be written in the SPEC
           semicolonTokenizer = new StringTokenizer(fileSetList, ";");
           while (semicolonTokenizer.hasMoreTokens())
           {
             entryInSPEC += semicolonTokenizer.nextToken() + " ";
           }
           if (mainPackage != null && subPackage == null ||
               subPackage.length() == 0)
           {
             entryInSPEC += mainPackage + " = " + pkgVersion + "-" + pkgRelease;
           }
           else
             entryInSPEC += mainPackage + "-" + subPackage + " = " +
                            pkgVersion + "-" + pkgRelease;
         }
         else
         {
           // This means that the requires list is empty or missing for this fileset
           // Hence generate the default value
            if (mainPackage != null && subPackage == null ||
                subPackage.length() == 0)
            {
              System.err.println("Warning: The list is empty for " +
                                  mainPackage + " in " + makefileVar +
                                  ". \nHence using the default value");
              entryInSPEC = mainPackage + " = " + pkgVersion + "-" + pkgRelease;
            }
            else
            {
              System.err.println("Warning: The list is empty for " +
                                  subPackage + " in " + makefileVar +
                                  ". \nHence using the default value");
              entryInSPEC = mainPackage + "-" + subPackage + " = " +
                            pkgVersion + "-" + pkgRelease;
            }
         }
       }
     }

     // If the hastTable is null or empty, then generate the requires tag
     // Put in the default value
     if (foundFileSet == false)
     {
        if (subPackage == null || subPackage.length() == 0)
          entryInSPEC = mainPackage + " = " + pkgVersion + "-" + pkgRelease;
        else
          entryInSPEC = mainPackage + "-" + subPackage + " = " + pkgVersion +
                        "-" + pkgRelease;
     }

     return entryInSPEC;
  } // end of method genRequiresList

  /**
    * Extracts FE info from the EntityTreeObject writes out the file info
    *
    * @param curEntityTreeObject : EntityTreeObject which holds a refernce to
    * the FE
    * @return void
    * @exception GeneratorException if error is encountered
    **/
  public void writeFileInfo( EntityTreeObject curEntityTreeObject )
    throws IOException, GeneratorException
  {
    String tmpStr;
    String appendString;
    FileEntity curFileEntity;
    curFileEntity = curEntityTreeObject.getFileEntityReference();

    if( curFileEntity == null )
      throw new GeneratorException("RPMGenerator :\n "
      + " Unable to find the File Entity Reference : "
      + "Invalid fileset with no child files  found." );

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
      Interface.printWarning("Multiple PackageData entries not supported "
      + "with RPM.  Using first entry from File stanza. \n\"" + srcpath + "\"");
    }

    if (pDataArray != null && pDataArray.isEmpty() == false)
    {
      // Use first PackageData array element
      PackageData curPD  = (PackageData)pDataArray.get( 0 );

      String    createdStr = "";
      ArrayList tmpArray = null;
      String    perms = (curPD.getPermissions() != null)?curPD.getPermissions():"-";
      String    group = (curPD.getGroupId() != null)?curPD.getGroupId():"-";
      String    user = (curPD.getUserId() != null)?curPD.getUserId():"-";
      String    type = (curPD.getFileType() != null)?curPD.getFileType():"-";
      String    warning_msg;
      type = type.trim();

      // Known types are case-insensitive 'f|file' 'd|dir' 's|symlink' 'h'
      int j=(type.equalsIgnoreCase("d")||(type.equalsIgnoreCase("dir")))?1:
         ((type.equalsIgnoreCase("f")) ||(type.equalsIgnoreCase("file")))?2:
         ((type.equalsIgnoreCase("s")) ||(type.equalsIgnoreCase("symlink")))?3:
         (type.equalsIgnoreCase("h"))?4:0;

      switch (j)
      {
        case 1 : // directory
          type = ( "%dir " );
          break;
        case 2 : // file
        case 3 : // symbolic link
        case 4 : // symbolic link
          type = ("");
          break;
        default: // Deal with unknown file types here
          warning_msg = new String ("Unknown file type specified.\n"
                + "No action is being taken by the generator.\n");
          Interface.printWarning(warning_msg);
      }

      tmpStr = ( "%attr( "+perms+", "+user+", "+group+" ) "+ type );
      specFile_.writeBytes( tmpStr );

      tmpArray  =  curPD.getFileDirectives();
      if( tmpArray != null )
      {
        tmpStr = (String)tmpArray.get( 0 );
        createdStr = createDirective("fileDirectives", "fileDirectives", tmpStr,
                                                                      NOT_REQD);
        createdStr.toString();
        specFile_.writeBytes( createdStr );
      }

      // This has a slightly different approach : sourceDir + sourceFile
      tmpStr = curFileEntity.getSourceDir();
      if (tmpStr != null)
      {
        appendString = curFileEntity.getSourceFile();
        if (appendString == null)
        {
          // Insert the shipRoot dir and separator
          String sourcePath = insertFileSeparator( shipRootDir, tmpStr ) ;
          if ( !Path.exists( sourcePath ) &&
               (curPD.getFileType().equalsIgnoreCase("d") ||
                curPD.getFileType().equalsIgnoreCase("dir")))
          {
            if ( Path.createPath( sourcePath ) == false )
              throw new GeneratorException( "RPMGenerator :\n" +
              "\tUnable to create source directory\n" +
              "\t'" + sourcePath + "'" );
          }

          // write the directory name
          specFile_.writeBytes( tmpStr + "   " );
        }
        else
          specFile_.writeBytes( tmpStr + appendString + "   " );
      }
      else
      {
        appendString =  curFileEntity.getSourceFile();
        if( appendString == null )
          throw new GeneratorException("RPMGenerator : \n "
                                   + " sourceDir or sourceFile not specified.");
        else
          specFile_.writeBytes( appendString + "   " );
      }

      // If the File is a Link check for targetDir and targetFile
      // and write it accordingly.  The "H" is carried over from the
      // MkinstallGenerator as a way to specify a symbolic link.  For whatever
      // reason it is not documented in the packaging reference as a valid
      // file type.  It is here for compatiblity.
      if( curPD.getFileType().equalsIgnoreCase("H") ||
          curPD.getFileType().equalsIgnoreCase("S") ||
          curPD.getFileType().equalsIgnoreCase("Symlink") )
      {
        String targetDir = curPD.getTargetDir();
        String sourceDir = curFileEntity.getSourceDir();
        String sourceFile = curFileEntity.getSourceFile();
        if (targetDir != null)
        {
          String targetFile = curPD.getTargetFile();
          String targetPath = targetDir;
          if (targetFile != null)
            targetPath += targetFile;
          else
          {
            // Eliminate the trailing slash when creating a symbolic link to a
            // directory.
            targetPath = targetPath.substring(0, targetPath.length() - 1);
          }

          String fullSourceDir = insertFileSeparator(shipRootDir_, sourceDir);
          String sourcePath = fullSourceDir;
          if (sourceFile != null)
            sourcePath += sourceFile;
          else
          {
            // Eliminate the trailing slash when creating a symbolic link to a
            // directory. For some reason, RPM does not like it...
            sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
          }

          // The directory where the symlink is being created needs to exist before
          // symlink is created.
          Path.createPath(fullSourceDir);

          // Since RPM needs symbolic links and directories to exist
          // prior to packaging we'll have to create them.
          if (Path.symLink(targetPath, sourcePath, true) == false)
          {
            throw new GeneratorException("RPMGenerator :\n" +
              "Unable to create symlink at `" + sourcePath + "'");
          }
        }
        else
          throw new GeneratorException("RPMGenerator :\n" +
            "targetDir not specified for File Stanza '" + sourceDir +
            sourceFile + "'");
      }
      specFile_.writeBytes("\n");
    }
  }

  private String checkForFileSeparator( String dirName )
  {
    if( dirName != null )
      if( dirName.trim().endsWith( ParserGeneratorInitiator._fileSeparator_ ) )
        return dirName;
      else
        return dirName.trim().concat( ParserGeneratorInitiator._fileSeparator_);
    else
      return dirName;
  }

} //end class definition







