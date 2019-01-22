/*******************************************************************************
 *
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:36:16
 * Date and Time File was extracted/checked out: 06/04/13 16:45:27
 ******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.lang.Exception;
import java.io.File;

/**
 * This class defines all the makefile variables needed to run this tool.
 * It also implements some error handling methods. All the other methods in
 * all the classes call these methods to handle the error conditions. 
 */
public class VariablesAndMessageHandler {

  //directory containing all the metadata files. All the metadata files
  //under this directory will be parsed
  public static final String FILESTANZA_METADATA_DIR = 
                                               "FILESTANZA_METADATA_DIR";
  //name of the metadata file. It is set by default to "fileMetadata.xml" 
  //in the rules
  public static final String FILESTANZA_METADATA_FILE = 
                                               "FILESTANZA_METADATA_URI";
  //path to the cmf file to be created
  public static final String PKG_CMF_FILE = "PKG_CMF_FILE";
  //path to the file containing the product information in the CMF format
  public static final String PKG_CMF_PRODUCT_FILE = "PKG_CMF_PRODUCT_FILE";

  public static String METADATA_DIR = null;
  public static String METADATA_FILE = null;
  public static String CMF_FILE = null;
  public static String CMF_PRODUCT_FILE = null;

  /**
   * Constructor
   */
  public VariablesAndMessageHandler()
  {
    try
    {
      METADATA_DIR = System.getProperty( FILESTANZA_METADATA_DIR );
      METADATA_FILE = System.getProperty( FILESTANZA_METADATA_FILE );
      CMF_FILE = System.getProperty( PKG_CMF_FILE );
      CMF_PRODUCT_FILE = System.getProperty( PKG_CMF_PRODUCT_FILE );
    }
    catch( Exception e )
    {
      printException( e, "VariablesAndMessageHandler", 
                      "An error occured while getting the values of " +
                      "the required environment variables" );
    }

    //the following checks should be done here so as not to wait until the
    //parsing is started to report the errors
    if ((METADATA_DIR == null) || (METADATA_DIR.length() == 0))
      printErrorMessage( "VariablesAndMessageHandler", "Makefile variable " +
                         FILESTANZA_METADATA_DIR + " is not set properly. " +
                         "Hence exiting..." );
    if ((METADATA_FILE == null) || (METADATA_FILE.length() == 0))
      printErrorMessage( "VariablesAndMessageHandler", "Makefile variable " +
                         FILESTANZA_METADATA_FILE + " is not set properly. " +
                         "Hence exiting..." );
    if ((CMF_FILE == null) || (CMF_FILE.length() == 0))
      printErrorMessage( "VariablesAndMessageHandler", "Makefile variable " +
                         PKG_CMF_FILE + " is not set properly. " +
                         "Hence exiting..." );
    if ((CMF_PRODUCT_FILE == null) || (CMF_PRODUCT_FILE.length() == 0))
      printErrorMessage( "VariablesAndMessageHandler", "Makefile variable " +
                         PKG_CMF_PRODUCT_FILE + " is not set properly. " +
                         "Hence exiting..." );
    
    File file = new File( METADATA_DIR );
    if ((!file.exists()) || (!file.isDirectory()))
      printErrorMessage( "VariablesAndMessageHandler", METADATA_DIR +
                    " doesn't exist or is not a directory. Hence exiting..." );

    file = new File( CMF_PRODUCT_FILE );
    if ((!file.exists()) || (!file.isFile()))
      printErrorMessage( "VariablesAndMessageHandler", CMF_PRODUCT_FILE +
                    " doesn't exist or is not a file. Hence exiting..." );

  }

 /**
  * Prints the error message and exits
  *
  * @param the file name sending this error
  * @param the error message
  */

  public static void printErrorMessage( String fileName, String msg )
  {
    System.err.println( "ERROR: " + fileName + ": " + msg );
    System.exit(-1);
  }

 /**
  * Prints the exception with the error message and exits
  *
  * @param the exception
  * @param the file name sending this error
  * @param the error message
  */
  public static void printException( Exception e, String fileName, String msg )
  {
    System.err.println( fileName + ": " + msg );
    e.printStackTrace();
    System.exit(-1);
  }

}
    
