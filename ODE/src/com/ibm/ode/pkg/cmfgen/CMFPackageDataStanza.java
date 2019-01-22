/*******************************************************************************
*
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:34:59
 * Date and Time File was extracted/checked out: 06/04/13 16:45:13
 ******************************************************************************/

package com.ibm.ode.pkg.cmfgen;

import java.util.StringTokenizer;

/**
 * This class represents the PackageData stanza in the CMF.
 * It stores all the PackageData attribues in a buffer and returns it
 * in the CMF format.
 */
public class CMFPackageDataStanza {

  //holds all the PackageData attributes which are not of 
  //"list of requisites" type
  private StringBuffer packageDataBuffer;
  //holds all the attributes of type "list of requisites"
  private StringBuffer requisitesTypeBuffer;

  /**
   * Constructor
   *
   */
  public CMFPackageDataStanza()
  {
    packageDataBuffer = new StringBuffer();
    //create this buffer only when there is a list of requisites type
    requisitesTypeBuffer = null;
  }

  /**
   * Inserts the CMF attributes and it's values into the buffer
   *
   * @param A string representing the CMF attribute
   * @param A string representing the attributes
   * @param A string representing the value of this CMF attribute
   * @param A boolean indicating if this attribute is a list of requisites type
   *
   */
  public void insertValues( String cmfKey, String attributes, 
                            String cmfKeyValue, boolean isListOfRequisitesType )
  {
    String entryInCMFFormat;
    String cmfKeyType = "string";

    if (!isListOfRequisitesType)
    {
      if ((attributes != null) && (attributes.length() != 0))
      {
        // any CMF value is assumed to be of type "string" unless specified
        StringTokenizer st = new StringTokenizer( attributes, ";" ); 
        String token;
        while (st.hasMoreTokens())
        {
          token = st.nextToken();
          if (token.startsWith( "cmfType" ))
          {
            cmfKeyType = token.substring( token.indexOf( "=" ) + 1 );
            break;
          }
        }
      }
      //attributes are not passed into each of these format methods
      //as there can be only one attribute "cmfType" specified for each
      //entry in packageData stanza which is already known through 
      //cmfKeyType at this point.
      //any new attribute should be passed to these methods as needed
      if (cmfKeyType.equalsIgnoreCase( "listofstrings" ))
        entryInCMFFormat = formatListOfStrings( cmfKey, cmfKeyValue );
      else if (cmfKeyType.equalsIgnoreCase( "listofconstants" ))
        entryInCMFFormat = formatListOfConstants( cmfKey, cmfKeyValue );
      else if (cmfKeyType.equalsIgnoreCase( "constant" ))
        entryInCMFFormat = formatConstant( cmfKey, cmfKeyValue );
      else
        entryInCMFFormat = formatString( cmfKey, cmfKeyValue );

      writeStringIntoBuffer( entryInCMFFormat );
    }
    else
    {
      entryInCMFFormat = formatRequisite( cmfKey, cmfKeyValue );
      writeStringIntoRequisitesBuffer( entryInCMFFormat );
    }

  }

  /**
   * Creates a string representing the CMF attribute and its value
   * in the listOfStrings format
   *
   * @param A string representing the CMF attribute
   * @param A string representing the CMF value
   *
   * @return A string in the required CMF format
   */
  private String formatListOfStrings( String cmfKey, String cmfKeyValue )
  {
    String entryInCMFFormat = cmfKey + " = [ ";
    StringTokenizer st = new StringTokenizer( cmfKeyValue, " " );
    String temp = "";
    boolean foundOpenBrace = false;
    boolean foundQuotes = false;
    int indexOfQuotes = -1;
    while (st.hasMoreTokens())
    {
      temp += st.nextToken() + " ";
      //this is needed as each token can contain an open brace which means that 
      //it should not be terminated before processing a corresponding closing 
      //brace
      //eg: if the value being read is dir verify(ode, group)
      //it should be formatted as "dir" "verify(ode, group)"
      //and not "dir" "verify(ode," "group)"
      if (temp.indexOf("(") != -1)
        foundOpenBrace = true;
      if (temp.indexOf(")") != -1)
        foundOpenBrace = false;

      //quotes will be present in the metadata file to preserve spaces, in this
      //case we need to preserve the spaces too. But these extra quotes will be 
      //removed because the value will be enclosed in quotes while generating 
      //the CMF
      indexOfQuotes = temp.indexOf("\"");
      if (indexOfQuotes != -1)
      {
        foundQuotes = true;
        if (temp.indexOf("\"", indexOfQuotes+1) != -1)
          foundQuotes = false;
      }
      if (!foundOpenBrace && !foundQuotes)
      {
        temp = removeQuotes( temp );
        entryInCMFFormat += "\"" + temp.trim() + "\" ";
        temp = "";
      }
    }
    entryInCMFFormat += "];";
    return entryInCMFFormat;
  }

  /**
   * Creates a string representing the CMF attribute and its value
   * in the listOfConstants format
   *
   * @param A string representing the CMF attribute
   * @param A string representing the CMF value
   *
   * @return A string in the required CMF format
   */
  private String formatListOfConstants( String cmfKey, String cmfKeyValue )
  {
    String entryInCMFFormat = cmfKey + " = [ ";
    StringTokenizer st = new StringTokenizer( cmfKeyValue, " " );
    while (st.hasMoreTokens())
    {
      entryInCMFFormat += "'" + st.nextToken() + "' ";
    }
    entryInCMFFormat += "];";
    return entryInCMFFormat;
  }

  /**
   * Creates a string representing the CMF attribute and its value
   * in the constant format
   *
   * @param A string representing the CMF attribute
   * @param A string representing the CMF value
   *
   * @return A string in the required CMF format
   */
  private String formatConstant( String cmfKey, String cmfKeyValue )
  {
    String entryInCMFFormat = cmfKey + " = '" + cmfKeyValue + "';";
    return entryInCMFFormat;
  }

  /**
   * Creates a string representing the CMF attribute and its value
   * in the string format
   *
   * @param A string representing the CMF attribute
   * @param A string representing the CMF value
   *
   * @return A string in the required CMF format
   */
  private String formatString( String cmfKey, String cmfKeyValue )
  {
    cmfKeyValue = removeQuotes( cmfKeyValue );
    String entryInCMFFormat = cmfKey + " = \"" + cmfKeyValue + "\";";
    return entryInCMFFormat;
  }

  /**
   * Creates a string representing the CMF attribute and its value
   * in the listOfRequisites format
   *
   * @param A string representing the CMF attribute
   * @param A string representing the CMF value
   *
   * @return A string in the required CMF format
   */
  private String formatRequisite( String cmfKey, String cmfKeyValue )
  {
    String entryInCMFFormat;
    cmfKeyValue = removeQuotes( cmfKeyValue );
    if ((cmfKeyValue == null) || (cmfKeyValue.trim().length() == 0))
      entryInCMFFormat = "(\"" + cmfKey + "\")";
    else
      entryInCMFFormat = "(\"" + cmfKey + "\" \"" + cmfKeyValue + "\")";
    return entryInCMFFormat; 
  }

  /**
   * Writes the input string into the buffer in a pre-defined format
   *
   * @param The string to write
   *
   */
  private void writeStringIntoBuffer( String entry )
  {
    //each packageData entry should be indented by four spaces
    packageDataBuffer.append("    " + entry + "\n");
  }

  /**
   * Writes the input string into the buffer holding the listOfRequisites types
   * in a pre-defined format
   *
   * @param The string to write
   *
   */
  private void writeStringIntoRequisitesBuffer( String entry )
  {
    //17 spaces are added before each entry because of the length of "partInfo"
    //This might have to be changed if a new attribute of this type is 
    //added to the CMF
    for (int i=0; i<17; i++)
      requisitesTypeBuffer.append( " " );
    requisitesTypeBuffer.append( entry + "\n" );
  }

  /**
   * Creates the buffer to hold the listOfRequisites types
   *
   * @param A string representing the CMF attribute of this type
   *
   */
  public void openRequisitesBuffer( String cmfKey )
  {
    //create a new buffer for requisites only once
    if (requisitesTypeBuffer == null)
      requisitesTypeBuffer = new StringBuffer();
    requisitesTypeBuffer.append( "    " + cmfKey + " = [\n" ); 
  }

  /**
   * Closes the buffer holding the listOfRequisites types
   */
  private void closeRequisitesBuffer()
  {
    //prepend 15 spaces before the end character, 15 because of the format
    //of the opening character
    for (int i=0; i<15; i++)
      requisitesTypeBuffer.append( " " );
    requisitesTypeBuffer.append("];\n");
  }

  /**
   * Formats and returns a buffer with all the attributes of the PackageData
   * stanza in the CMF format
   *
   * @param A string representing the number of such PackageData stanzas
   *        in the enclosing file stanza
   *
   * @return A StringBuffer with all the attributes in the CMF format
   *
   */
  public StringBuffer getPackageDataInCMFFormat( int noOfPkgDataStanzas )
  {
    //if there are more than one PackageDatas in this fileStanza, then the
    //attributes of each PackageData should be wrapped in its tags.
    //Each PackageData tag, it's opening and closing braces should be
    //indented by two spaces
    if (noOfPkgDataStanzas > 1)
      packageDataBuffer.insert( 0, "  PackageData\n  {\n" );

    if (requisitesTypeBuffer != null)
      packageDataBuffer.append( requisitesTypeBuffer );

    if (noOfPkgDataStanzas > 1)
      packageDataBuffer.append( "  }\n" );

    return packageDataBuffer;
  }

  /**
   * Closes the buffer holding the listOfRequisites types upon receiving the
   * signal that the pasring of such an element has ended
   *
   * @param A string representing the CMF attribute of such type
   *
   */
  public void signalEndOfElement( String elementName )
  {
    if (MetaDataContentHandler.KNOWN_LIST_OF_REQUISITE_TYPES.contains(
                                                             elementName ))
      closeRequisitesBuffer();
  }

  /**
   * Removes the quotes from the input string
   *
   * @param A string whose quotes need to be removed
   *
   * @return A string with no quotes
   */
  private String removeQuotes( String value )
  {
    //remove the existing quotes if present, since the string will be
    //enclosed in quotes anyway
    //could use replace method to do it, but this method does not allow
    //the use of null character in this method like replace('\"', '')
    //hence the following logic
    int indexOfQuotes = -1;
    if ((value == null) || (value.trim().length() == 0))
      return value;

    for (int i=0; i<value.length(); i++)
    {
      indexOfQuotes = value.indexOf("\"");
      if (indexOfQuotes != -1)
      {
        if (indexOfQuotes == 0)
          value = value.substring(1);
        else if (indexOfQuotes == value.length()-1)
          value = value.substring(0, indexOfQuotes);
        else
          value = value.substring(0, indexOfQuotes) + 
                  value.substring(indexOfQuotes+1);
        i = indexOfQuotes;
      }
      else
        break;
    }
    return value;
  } 
}


