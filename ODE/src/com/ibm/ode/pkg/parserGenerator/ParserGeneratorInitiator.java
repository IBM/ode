package com.ibm.ode.pkg.parserGenerator;
   
import java.io.*;
import java.rmi.*;
import java.util.*;
import com.ibm.ode.pkg.pkgCommon.PackageConstants;
import com.ibm.ode.lib.string.PlatformConstants;
import com.ibm.ode.pkg.parserGenerator.service.TreeHandler;
import com.ibm.ode.pkg.parserGenerator.service.LSTFormatter;
import com.ibm.ode.pkg.parserGenerator.service.MkinstallNodeHandler;
import com.ibm.sdwb.bps.api.servicepkg.*;

/**
 * ParserGeneratorInitiator : This class holds the main application routine.
 * It is responsible for initiating the parse action, initiate the
 * synthesizer to form the package tree structure  and initiate
 * the generator to generate appropriate control files for the packaging
 * tool.
 * @version     1.40 98/03/31
 * @author      Prem Bala
 **/
public class ParserGeneratorInitiator
{
  // variables defined to hold the ode environment variables
  private String tostage_;
  private String pkgType_;
  private String pkgControlDir_;
  private String pkgClass_;
  private String pkgFixStrategy_;
  private String pkgCmfFile_;
  private String apar_;
  
  // References to hold various objects
  private ParserGeneratorEnumType pgEnum_;
  private Parser parser_;
  private Package package_;
  private EntitySynthesizer entitySynthesizer_;
  private EntityTreeRoot entityTreeRoot_;
  private Generator generator_;
    
  // static String to be used in concatenation of directory and fileName
  public static String _fileSeparator_;
  
  private static String context_;

  // static Strings to describe the standard names of platform
  // hardware and install tool
  private static String pkgTool_ = null;
  private static String pkgToolVersion_ = null;

  // Arrays to hold predefined env. variables for different platform
  // will be used for error checking
  private static Vector predefinedEnvVariables_ = new Vector();
  
  // Packaging Tools
  public static final String _mkinstallTool_ = "mkinstall"; // AIX
  public static final String _pkgmkTool_     = "pkgmk"; // Solaris & SCO
  public static final String _swpackageTool_ = "swpackage"; // HP-UX
  public static final String _mvsTool_       = "mvs"; // MVS
  public static final String _ispeTool_      = "ispe"; // InstallShield Pro
  public static final String _isjeTool_      = "isje"; // InstallShield Java
  public static final String _rpmTool_       = "rpm"; // Red Hat Package Manager
  
  public static Vector _mkinstallToolMachines_ = new Vector();
  public static Vector _pkgmkToolMachines_     = new Vector();
  public static Vector _swpackageToolMachines_ = new Vector();
  public static Vector _mvsToolMachines_       = new Vector();
  public static Vector _ispeToolMachines_      = new Vector();
  public static Vector _isjeToolMachines_      = new Vector();
  public static Vector _rpmToolMachines_       = new Vector();

  // Packaging Description
  public static final String _ippPkg_            = "IPP";
  public static final String _servicePkg_        = "SP";
  public static final String _initService_       = "ST";
  public static final String _refreshStrategy_   = "REFRESH";
  public static final String _cumStrategy_       = "CUMULATIVE";
  public static final String _userPkgType_       = "USER";
  public static final String _aparPkgType_       = "APAR";
  public static final String _ptfPkgType_        = "PTF";
  public static final String _usermodPkgType_    = "USERMOD";
  
  
  // ERROR Banner
  public static final String startBanner_ = "/*********************************************/"
                                           + "\n"
                                           + "         Parser-Generator Error            "
                                           + "\n"
                                           + "/*********************************************/ "
                                           + "\n" ;
  public static final String endBanner_   =   "\n" 
                                            + "/********************************************/"
                                            + "\n" ;

  public static final String usageString_ = "\n Usage : java -DTOSTAGE=$TOSTAGE "
                                            + " -DPKG_TYPE=$PKG_TYPE\n "
                                            + " \t -DPKG_CONTROL_DIR=$PKG_CONTROL_DIR -DPKG_CLASS=$PKG_CLASS \n"
                                            + " \t -DPKG_FIX_STRATEGY=$PKG_FIX_STRATEGY -DPKG_CMF_FILE=$PKG_CMF_FILE \n"
                                            + " \t -DPKG_API_URL=$PKG_API_URL \n "
                                            + " \t ParserGeneratorInitiator \n ";  

  static
  {
    context_ = PlatformConstants.CURRENT_MACHINE;
    
    // add packaging information
    predefinedEnvVariables_.addElement( _ippPkg_ );
    predefinedEnvVariables_.addElement( _servicePkg_ );
    predefinedEnvVariables_.addElement( _initService_ );
    predefinedEnvVariables_.addElement( _refreshStrategy_ ); 
    predefinedEnvVariables_.addElement( _cumStrategy_ );
    predefinedEnvVariables_.addElement( _userPkgType_ );
    predefinedEnvVariables_.addElement( _aparPkgType_ );
    predefinedEnvVariables_.addElement( _ptfPkgType_ );
    predefinedEnvVariables_.addElement( _usermodPkgType_ );

    // set the _fileSeparator based on context_ env variable
    setFileSeparator();

    // set package tool type and version
    setPackageToolInfo();
  }

  /*****************************************************************************
   * Constructor for ParserGeneratorInitiator
   **/
  public ParserGeneratorInitiator()
  {
    tostage_        = null;
    pkgType_        = null;
    pkgControlDir_  = null;
    pkgClass_       = null;
    pkgFixStrategy_ = null;
    pkgCmfFile_     = null;
    apar_           = null;
  }

  public static String getPackagingTool()
  {
    return (pkgTool_);
  }

  public static void setPackagingTool( String toolname )
  {
    pkgTool_ = toolname;
  }

  public static String getPackagingToolVersion()
  {
    return (pkgToolVersion_);
  }

  public static void setPackagingToolVersion( String toolver )
  {
    pkgToolVersion_ = toolver;
  }

  /**
   * Gets the environment variables from the calling environment ( ode for example ).
   * 
   * @exception ParserGeneratorInitiatorException :-> If any error is encountered
   */   
  public void getAndSetEnvFromOde()
    throws ParserGeneratorInitiatorException
  {           
    // get root directory of the ship_tree - aka TOSTAGE
    tostage_ = System.getProperty( "TOSTAGE" );
    if (tostage_ == null || tostage_.trim().length() == 0)
      throw new ParserGeneratorInitiatorException( startBanner_  
              + "The TOSTAGE environment variable has not been"
              + " specified. \n" + usageString_ + endBanner_ );

    // get the location of the CMF
    pkgCmfFile_ = System.getProperty( "PKG_CMF_FILE" );
    if (pkgCmfFile_ == null || pkgCmfFile_.trim().length() == 0)
      throw new ParserGeneratorInitiatorException( startBanner_ 
                + "The PKG_CMF_FILE environment variable has not been"
                + " specified. \n" + usageString_ + endBanner_ );

    // get the output directory where the control files will be written out
    pkgControlDir_  = System.getProperty( "PKG_CONTROL_DIR" );
    if (pkgControlDir_ == null || pkgControlDir_.trim().length() == 0)
      throw new ParserGeneratorInitiatorException( startBanner_
                + "The PKG_CONTROL_DIR environment variable has not been"
                + " specified. \n" + usageString_ + endBanner_ );
    
    // USERMOD is handled exactly the same as APAR by the parser.
    apar_ = System.getProperty( "APAR" );
    if (apar_ == null) 
    { 
      apar_ = System.getProperty("USERMOD");
    }
    if (apar_ != null) 
    { 
      apar_ = apar_.trim().toUpperCase();
      if (apar_.length() == 0)
        apar_ = null;
    }

    // get the type of package ( user, apar, or ptf )
    pkgType_ = System.getProperty("PKG_TYPE");
    if (pkgType_ == null)
    {
      if (apar_ != null)
        pkgType_ = _aparPkgType_;
      else 
        throw new ParserGeneratorInitiatorException( startBanner_
                  + "The PKG_TYPE environment variable has not been"
                  + " specified. \n" + usageString_ + endBanner_ );

    } 
    else if (apar_ != null && pkgType_ != _aparPkgType_)
    {
      if (pkgType_==_usermodPkgType_)
        pkgType_=_aparPkgType_;
    }
    
    if (!predefinedEnvVariables_.contains(pkgType_.toUpperCase()))
      throw new ParserGeneratorInitiatorException( startBanner_
                + "The PKG_TYPE environment variable has been "
                + "incorrectly specified. Please check documentation "
                + "for appropriate values.\n" + endBanner_ );

    // get info whether it is ipp or service package
    pkgClass_ = System.getProperty( "PKG_CLASS" );
    if (pkgClass_ == null)
    {
      if (apar_!= null)
       pkgClass_ = _servicePkg_;
      else 
       throw new ParserGeneratorInitiatorException( startBanner_
                 + "The PKG_CLASS environment variable has not been"
                 + " specified. \n" + usageString_ + endBanner_ );

    } 
    else if (apar_ != null && !pkgClass_.equalsIgnoreCase( _servicePkg_ ))
    {
        throw new ParserGeneratorInitiatorException( startBanner_
                  + "The PKG_CLASS " + pkgClass_ + " must be null or " 
                  + _servicePkg_ + " if APAR="+apar_+". \n" 
                  + usageString_ + endBanner_ );
    }
    
    if (!predefinedEnvVariables_.contains(pkgClass_.toUpperCase()))
      throw new ParserGeneratorInitiatorException( startBanner_
                + "The PKG_CLASS environment variable has been"
                + " incorrectly specified. Please check documentation "
                + "for appropriate values.\n" + endBanner_ );
        

    // Get the PKGFAMILY and make sure its not null or native.
    pkgTool_ = System.getProperty( "PKGFAMILY" );
    if (pkgTool_ == null || pkgTool_.equalsIgnoreCase("native"))
      throw new ParserGeneratorInitiatorException( startBanner_
                + "The \"PKGFAMILY\" environment variable has not been "
                + "specified.\nThis is a required property.\n" + endBanner_ );
  }

  static private void setPackageToolInfo()
  {
    pkgToolVersion_ = System.getProperty( "PKGVERSION" );
    if (pkgToolVersion_ != null &&
        pkgToolVersion_.equalsIgnoreCase( "default" ))
      pkgToolVersion_ = null;

    _mkinstallToolMachines_.addElement( PlatformConstants.RIOS_AIX_4_MACHINE );
    _mkinstallToolMachines_.addElement( PlatformConstants.IA64_AIX_5_MACHINE );
    _pkgmkToolMachines_.addElement( PlatformConstants.SPARC_SOLARIS_2_MACHINE );
    _pkgmkToolMachines_.addElement( PlatformConstants.X86_SOLARIS_2_MACHINE );
    _pkgmkToolMachines_.addElement( PlatformConstants.X86_SCO_7_MACHINE );
    _swpackageToolMachines_.addElement(
        PlatformConstants.HP9000_UX_10_MACHINE );
    _mvsToolMachines_.addElement( PlatformConstants.MVS390_OE_1_MACHINE );
    _mvsToolMachines_.addElement( PlatformConstants.MVS390_OE_2_MACHINE );
    _ispeToolMachines_.addElement( PlatformConstants.X86_NT_4_MACHINE );
    _ispeToolMachines_.addElement( PlatformConstants.X86_95_4_MACHINE );
    _rpmToolMachines_.addElement( PlatformConstants.SPARC_SOLARIS_2_MACHINE );
    _rpmToolMachines_.addElement( PlatformConstants.X86_SOLARIS_2_MACHINE );
    _rpmToolMachines_.addElement( PlatformConstants.X86_LINUX_2_MACHINE );
    _rpmToolMachines_.addElement( PlatformConstants.SPARC_LINUX_2_MACHINE );
    _rpmToolMachines_.addElement( PlatformConstants.RIOS_AIX_4_MACHINE );
  }
  
  private static void setFileSeparator()
  {
    if (PlatformConstants.isUnixMachine( context_ ))
      _fileSeparator_ = "/";
    else
      _fileSeparator_ = "\\";
  }

  /******************************************************************************
   * builds the base objects of the ParserGenerator. 
   * @exception ParserGeneratorInitiatorException :-> If any error is encountered
   **/     
  public void buildInfrastructure()
    throws ParserGeneratorInitiatorException,
           Exception
  {
    pgEnum_ = new ParserGeneratorEnumType();
    try
    {
      parser_ = new Parser( pgEnum_ );
    }
    catch ( ParserException e )
    {
      e.printStackTrace();
      throw new ParserGeneratorInitiatorException();
    }     
    
    // construct the Package object
    package_ = new Package();
    
    // construct the EntitySynthesizer object
    entitySynthesizer_ = new EntitySynthesizer();
    
    // construct the EntityTreeRoot object
    entityTreeRoot_ = new EntityTreeRoot();
    

    // construct the Generator Object
    generator_ = new Generator( tostage_,
                                context_,
                                pkgControlDir_,
                                pkgType_,
                                pkgClass_,
                                pkgFixStrategy_,
                                apar_ );
  }

  /******************************************************************************
   * calls the Parser, Synthesizer and the Generator to generate the output 
   * @exception ParserGeneratorInitiatorException :-> If any error is encountered
   **/  
  public void parseAndCreateTheControlFiles() 
    throws ParserGeneratorInitiatorException
  {

    HashMap packageBom;

    // get all the environment variables from ode and set it locally
    // also validate all the values
    this.getAndSetEnvFromOde();

    // If the pkgtool is ISPE, then return (since there isn't anything to do)    
    // If the pkgtool is MKINSTALL and package class is _servicePkg_,
    // then throw exception    
    if (pkgTool_.equalsIgnoreCase( _ispeTool_ ))
    {
       System.out.println("The parse action is not supported with " +
                          _ispeTool_);
       return;
    }    
    else if (pkgTool_.equalsIgnoreCase( _mkinstallTool_ ) &&
       pkgClass_.equalsIgnoreCase( _servicePkg_ ))
    {
       throw new ParserGeneratorInitiatorException( startBanner_
                 + "Service Packaging not supported  \n"
                 + endBanner_ );
    }

    try
    { 
      this.buildInfrastructure();
        
      // call the parseCMF which parses the CMF and populates the
      // package objects
      System.out.print("Parsing the CMF at " + pkgCmfFile_ + 
                       "  <=> ........");
      if (parser_.parseCMF(package_, pkgCmfFile_))
      {
        // The system.out's can be removed after testing
        System.out.println("successful");
        entitySynthesizer_.synthesizeEntities(package_, entityTreeRoot_);
        System.out.println("Package Tree Structure created by EntitySynthesizer"
                            + " <=> .........successful");
                    
        // Nothing to generate for ISPE.
        if (!pkgTool_.equalsIgnoreCase( _ispeTool_ ))
        {
          generator_.invokeTargetGenerator( entityTreeRoot_, package_ );              
            
          System.out.println("Control Files created in " + pkgControlDir_ + 
                             " by the Generator <=>  ............successful" );
        }
      }
      else 
      {
        // normally should never be here except when system failure occurs
        System.err.println( startBanner_ +
            "\nUnerecoverable error while parsing the CMF." +
            "Please retry your command again. " + endBanner_);
        throw new ParserGeneratorInitiatorException();
      }
    }
    catch (ParserException ex)
    {
      System.err.println(startBanner_);
      System.err.println(ex.getMessage());
      System.err.println(endBanner_);
      System.exit(-1);  
    }
    catch (PackageException ex)
    {
      System.err.println(startBanner_);
      System.err.println(ex.getMessage());
      System.err.println(endBanner_);
      System.exit(-1);  
    }
    catch (EntitySynthesizerException ex)
    {
      System.err.println(startBanner_);
      System.err.println(ex.getMessage());
      System.err.println(endBanner_);
      System.exit(-1); 
    }                   
    catch (GeneratorException ex)
    {
      System.err.println(startBanner_);
      System.err.println(ex.getMessage());
      System.err.println(endBanner_);
      System.exit(-1);
    }
    // Any other exception
    catch (Exception ex) 
    { 
      System.err.println(startBanner_);
      System.err.println("Unknown Error:\n" + ex.getMessage());
      System.err.println(endBanner_);   
      System.exit(-1);
    }
  }
  
  /**
   *
         */
  public static void main( String args[] )
  {
    try
    {
       ParserGeneratorInitiator parserGenerator = new ParserGeneratorInitiator();
       parserGenerator.parseAndCreateTheControlFiles();
    }
    catch (ParserGeneratorInitiatorException ex)
    {      
      System.err.println(startBanner_);
      System.err.println(ex.getMessage());
      System.err.println(endBanner_);   
      System.exit(-1);
    }
    catch (Throwable ex)
    {
      System.err.println(startBanner_ + "\nUnrecoverable Error: \n" + 
                         ex.getMessage() );
      System.err.println(endBanner_);
      ex.printStackTrace();
      System.exit(-1);
    }
    System.exit(0);
  }        
}


