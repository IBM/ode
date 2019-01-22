/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:46
 * Date and Time File was extracted/checked out: 06/04/13 16:45:22
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.util.*;

/**
 * This represents packageData XML Element.
 *
 * @author Anil Ambati
 * @version 1.1
 */
public class PackageData extends XMLElement
  implements Constants
{
  /**
   * Represents names of the subelements of a packageData element
   */
  static Vector mySubElements = new Vector();

  /**
   * Name of a shippable file
   */
  private String parent = null;

  /**
   * Index of this subelement among the packageData subelements that belong to the
   * fileStanza element that represents the parent. The value of this is used to
   * figure out the values of the subelements.
   */
  private int myIndex = 0;

  /**
   * Constructor
   *
   * @param parent name of a shippable file
   * @param idx index of this pacakgeData subelement of the fileStanza element
   *            that represents the specified shippable file
   */
  protected PackageData( String parent, int idx )
  {
    this.parent = parent;
    this.myIndex = idx;
  }

  /**
   * Returns xml form of this object
   */
  public String toXML()
  {
    StringBuffer sb = new StringBuffer();
    String myIndent = getMyIndent();
    int subElementIndentPos = getIndentPosition() + 1;

    // Start forming the XML
    sb.append(myIndent);
    sb.append(getBeginTag()).append(NEW_LINE);
    Element elemObj = null;
    for (int idx = 0; idx < PackageData.mySubElements.size(); idx++)
    {
      String elemName = (String)PackageData.mySubElements.elementAt(idx);
      elemObj = Element.newElement(elemName, parent, subElementIndentPos,
                                   myIndex);
      String xmlStr = elemObj.toXML();
      if (xmlStr != null)
        sb.append(xmlStr).append(NEW_LINE);
      elemObj = null;
    }
    sb.append(myIndent).append(getEndTag());
    return sb.toString();
  }

  /**
   * Factory method. Returns an array of PackageData objects. Number of
   * PacakgeData objects returned depends on the number of target directories
   * and target files.
   *
   * @param parent name of a shippable file
   * @param indentPos position of the packageData element in the XML document hierarchy
   */
  public static PackageData[] newPackageData( String parent,
                                              int indentPos )
  {
    PropertyFileInterpreter props = PropertyFileInterpreter.getInstance();
    String targetFiles =
      Element.newElement(TARGETFILE_XML_TAG_NAME, parent).getValue();
    String targetDirs =
      Element.newElement(TARGETDIR_XML_TAG_NAME, parent).getValue();

    StringTokenizer st = null;
    StringTokenizer st1 = null;
    if (targetFiles != null &&
        targetFiles.indexOf(PACKAGEDATA_SEPARATOR) >= 0)
      st = new StringTokenizer(targetFiles, PACKAGEDATA_SEPARATOR);
    if (targetDirs != null &&
        targetDirs.indexOf(PACKAGEDATA_SEPARATOR) >= 0)
      st1 = new StringTokenizer(targetDirs, PACKAGEDATA_SEPARATOR);

    int stTokens = st != null ? st.countTokens() : 1;
    int st1Tokens = st1 != null ? st1.countTokens() : 1;
    int arraySize = Math.max(stTokens, st1Tokens);
    PackageData[] pkgDatas = new PackageData[arraySize];
    for (int idx = 0; idx < arraySize; idx++)
    {
      pkgDatas[idx] = new PackageData(parent, idx);
      pkgDatas[idx].setIndentPosition(indentPos);
    }
    return pkgDatas;
  }
}
