/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:28
 * Date and Time File was extracted/checked out: 06/04/13 16:45:18
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.util.*;
import java.io.*;

/**
 * This represents fileStanza XML Element.
 *
 * @author Anil Ambati
 * @version 1.1
 */
public class FileStanza extends XMLElement
  implements Constants
{
  /**
   * Represents names of the subelements & attributes of a fileStanza
   * element
   */
  static Vector mySubElements = new Vector();
  static Vector myAttributes = new Vector();

  /**
   * Represents the name of a shippable file
   */
  private String sourceFileName = null;

  /**
   * Default Constructor
   *
   * @param fileName name of a valid shippable file
   */
  public FileStanza( String fileName )
  {
    this.sourceFileName = fileName;
    this.setMyAttributeValues();
  }

  /**
   * Returns XML form of this object
   */
  public String toXML()
  {
    StringBuffer sb = new StringBuffer();
    String myIndent = getMyIndent();
    int subElementIndentPos = getIndentPosition() + 1;

    // Start forming the XML
    sb.append(myIndent);
    sb.append(getBeginTag()).append(NEW_LINE);
    PackageData[] pkgDatas =
      PackageData.newPackageData(this.sourceFileName, subElementIndentPos);
    for (int idx = 0; idx < pkgDatas.length; idx++)
    {
      sb.append(pkgDatas[idx].toXML()).append(NEW_LINE);
    }
    sb.append(myIndent).append(getEndTag());
    return sb.toString();
  }

  /**
   * Sets the values for the attributes of this element
   */
  private void setMyAttributeValues()
  {
    PropertyFileInterpreter props =
      PropertyFileInterpreter.getInstance();
    Enumeration enumer = FileStanza.myAttributes.elements();
    attributes = new Hashtable();
    while (enumer.hasMoreElements())
    {
      String attributeName = (String)enumer.nextElement();
      String value =
        props.getValue(attributeName, this.sourceFileName);
      if (value != null && value.length() != 0)
        attributes.put(attributeName, value);
    }
  }
}
