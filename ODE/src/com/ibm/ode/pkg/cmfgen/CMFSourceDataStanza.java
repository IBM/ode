/*******************************************************************************
 *
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:04
 * Date and Time File was extracted/checked out: 06/04/13 16:45:14
 ******************************************************************************/

package com.ibm.ode.pkg.cmfgen;

/**
 * This class represents the SourceData stanza in the CMF
 * It stores all the SourceData attributes in a buffer and
 * returns it in the CMF format.
 */
public class CMFSourceDataStanza {

  //represents the CMF attribute sourceFile 
  private String sourceFile;
  //represents the CMF attribute sourceDir
  private String sourceDir;

  /**
   * Constructor
   *
   * @param A string representing the sourceFile
   * @param A string representing the sourceDir
   */
  public CMFSourceDataStanza( String sourcefile, String sourcedir )
  {
    sourceFile = sourcefile;
    sourceDir = sourcedir;
  }

  /**
   * Returns a StringBuffer representing the SourceData in the CMF format
   *
   * @param An integer indicating the number of PackageData stanzas in this 
   *        file stanza
   *
   * @return A stringbuffer
   */
  public StringBuffer getSourceDataInCMFFormat( int noOfPkgDataStanzas )
  {
    StringBuffer sourceStanzaBuffer = new StringBuffer();
    String cmfString;

    //enclose sourceFile and sourceData in "SourceData" if there are more
    //than one PackageData stanzas in this file stanza
    if (noOfPkgDataStanzas > 1)
    {
      writeString( sourceStanzaBuffer,"SourceData", 2 );
      writeString( sourceStanzaBuffer,"{", 2 ); 
    }

    if ((sourceFile != null) && (sourceFile.trim().length() != 0))
    {
      cmfString = "sourceFile = \"" + sourceFile + "\";";
      writeString( sourceStanzaBuffer, cmfString, 4 );
    }
    cmfString = "sourceDir = \"" + sourceDir + "\";";
    writeString( sourceStanzaBuffer, cmfString, 4 );

    if (noOfPkgDataStanzas > 1)
      writeString( sourceStanzaBuffer, "}", 2 ); 

    return sourceStanzaBuffer;
  }

  /**
   * Writes the input string to the input buffer with proper indentation
   *
   * @param The StringBuffer to write to
   * @param The string to write to
   * @indent An integer indicating the number of spaces this string needs to
   *         be indented
   */
  private void writeString( StringBuffer sb, String cmfString, int indent )
  {
    //sourceFile and sourceDir should be indented by four spaces
    //while SourceData should be indented by two spaces
    for (int i=0; i<indent; i++)
      sb.append( " " );
    sb.append( cmfString + "\n" );
  }

}
