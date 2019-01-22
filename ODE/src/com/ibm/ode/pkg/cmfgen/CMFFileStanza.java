/*******************************************************************************
 *
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:34:51
 * Date and Time File was extracted/checked out: 06/04/13 16:45:12
 ******************************************************************************/

package com.ibm.ode.pkg.cmfgen;

import java.util.Vector;

/** 
 * This class represents the file stanza in the CMF. It contains one SourceData 
 * stanza and various PackageData stanzas. It's job is to call various methods
 * in CMFSourceDataStanza and CMFPackageDataStanza which represent SourceData
 * and PackageData stanzas and also to return the information representing
 * this file stanza in the CMF format
 */
public class CMFFileStanza {

  //buffer to hold all the file stanza information in the CMF format
  public StringBuffer fileStanzaBuffer;
  //an object representing the SourceData stanza
  public CMFSourceDataStanza sourceDataStanza;
  //an object representing the PackageData stanza
  public CMFPackageDataStanza packageDataStanza;
  //a vector containing all the PackageData objects part of this file stanza
  public Vector packageDataStanzaList;

  /**
   * Constructor
   *
   * @param  the name of the source file
   * @param  the path to the source directory
   */
  public CMFFileStanza( String sourceFile, String sourceDir )
  {
    fileStanzaBuffer = new StringBuffer();
    sourceDataStanza = new CMFSourceDataStanza( sourceFile, sourceDir );
    packageDataStanza = null;
    packageDataStanzaList = new Vector();
  }

  /**
   * Creates an object of type CMFPackageDataStanza. This will be called
   * multiple times from MetaDataContentHandler based on the number
   * of PackageData stanzas in the file stanza
   */
  public void createPackageDataStanza()
  {
    packageDataStanza = new CMFPackageDataStanza();
    packageDataStanzaList.addElement( packageDataStanza );
  }

  /**
   * Inserts values into the PackageData object by calling the appropriate
   * method in CMFPackageDataStanza
   *
   * @param A string representing the cmf attribute
   * @param A string representing the list of attributes for this 
   *         cmf attribute such as the type of the cmf attribute
   * @param A string the value of the cmf attribute
   * @param A boolean value indicating if it is a list of requisites type
   */
  public void insertValuesIntoPackageData( String cmfKey, String attributes,
                          String cmfKeyValue, boolean isListOfRequisitesType )
  {
    if (!cmfKey.equalsIgnoreCase( "filemetadata" ) &&
        !cmfKey.equalsIgnoreCase( "filestanza" ) &&
        !cmfKey.equalsIgnoreCase( "packagedata" ))
      packageDataStanza.insertValues( cmfKey, attributes, 
                                      cmfKeyValue, isListOfRequisitesType );
  }

  /**
   * Combines the buffers representing the SourceData and PackageData stanzas
   * in the appropriate CMF format
   *
   * @return A string representing the file stanza
   */
  public String formatToCMF()
  {
    //enclose the SourceData and PackageData information in "file" tag
    fileStanzaBuffer.append("file\n{\n");
    fileStanzaBuffer.append(
       sourceDataStanza.getSourceDataInCMFFormat( 
                                    packageDataStanzaList.size() ).toString() );
    for (int i=0; i<packageDataStanzaList.size(); i++)
    {
      fileStanzaBuffer.append( ((CMFPackageDataStanza)
       packageDataStanzaList.elementAt(i)).getPackageDataInCMFFormat( 
                                     packageDataStanzaList.size()).toString() );
    }
    fileStanzaBuffer.append("}\n");
    return fileStanzaBuffer.toString();
  }

  /**
   * Signals to the PackageData that the end of an element is reached
   *
   * @param A string representing the name of the element
   */
  public void signalEndOfElement( String elementName )
  {
    // This tells the package data object that an element has ended.
    // This is required to close sub stanzas in packageData stanza such as
    // partInfo
    // This is not needed in source data object because there are no
    // such substanzas in sourceData stanza
    packageDataStanza.signalEndOfElement( elementName );    
  }

  /**
   * Initialises the buffer to hold the CMF data of type lost of requisites
   * through a appropriate call to the method in CMFPackageDataStanza
   *
   * @param A string representing the name of the element of this type
   */

  public void initialiseListOfReqTypesInPackageData( String elementName )
  {
    packageDataStanza.openRequisitesBuffer( elementName );    
  }
}
