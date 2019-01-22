/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:15
 * Date and Time File was extracted/checked out: 06/04/13 16:45:16
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.util.*;

/**
 * This class represents an subelement of packageData element.
 *
 * @author Anil Ambati
 * @version 1.1
 * @see com.ibm.ode.pkg.cmfgen.FileStanza
 * @see com.ibm.ode.pkg.cmfgen.PackageData
 */
public class Element extends XMLElement
  implements Constants
{
  /**
   * This represents the name of a shippable file.
   */
  protected String parent = null;

  /**
   * This represents if this element belongs to a pkgData element. If the value
   * is more than 0 then the shippable file is associated with more than
   * one package data. The value of this variable indicates the index of the
   * packageData element that this element belongs to.
   */
  protected int pkgDataIdx = -1;

  /**
   * Default Constructor
   */
  protected Element() {}

  /**
   * Constructor
   *
   * @param name tag name of this element
   * @param parent represents the name of a shippable file
   * @param indentPos position of this element in the XML document hierarchy
   * @param pkgDataIdx index of the packageData element that this element
   *                   belongs to
   */
  protected Element( String name, String parent, int indentPos, int pkgDataIdx )
  {
    setName(name);
    setIndentPosition(indentPos);
    setPkgDataIndex(pkgDataIdx);
    this.parent = parent;
  }

  /**
   * Returns the value for this element. This method should be overriden if
   * an element needs to calculate its value using a different approach.
   * Returns the value returned by the method getValue(string, string, string)
   *
   * @return the value of this element
   */
  public String getValue()
  {
    return Element.getValue(getName(), this.parent, this.pkgDataIdx);
  }

  public String getParent()
  {
    return this.parent;
  }
  public void setParent( String parentFileStanzaName )
  {
    this.parent = parentFileStanzaName;
  }

  public void setPkgDataIndex( int pkgDataIdx )
  {
    this.pkgDataIdx = pkgDataIdx;
  }

  /**
   * Factory method
   *
   * @param type a valid tag name
   * @param fileName a valid shippable file name
   */
  public static Element newElement( String type, String fileName )
  {
    return Element.newElement(type, fileName, 0, -1);
  }

  /**
   * Factory method
   *
   * @param type a valid tag name
   * @param fileName a valid shippable file name
   * @param indentPos indent position of the new element in the XML document
   *                  hierarchy
   */
  public static Element newElement( String type, String fileName, int indentPos )
  {
    return Element.newElement(type, fileName, indentPos, -1);
  }

  /**
   * Factory method
   *
   * @param type a valid tag name
   * @param fileName a valid shippable file name
   * @param indentPos indent position of the new element in the XML document
   *                  hierarchy
   * @param pkgDataIdx index of the packageData element that the new element
   *                   belongs to
   */
  public static Element newElement( String type, String fileName,
                                    int indentPos, int pkgDataIdx )
  {
    // Form the class name from the tag name and the name of this class.
    char[] chars = type.toCharArray();
    chars[0] = Character.toUpperCase(chars[0]);
    String myClassName = (new Element()).getClass().getName();
    int idxOfLastPkgSep = myClassName.lastIndexOf(".");
    String pkgName = myClassName.substring(0, idxOfLastPkgSep + 1);
    StringBuffer className = new StringBuffer(pkgName);
    className.append(String.valueOf(chars));
    className.append(myClassName.substring(idxOfLastPkgSep + 1, myClassName.length()));

    // If there is not special class that corresponds to the specified package
    // data subelement tag name, then return the Element object.
    try
    {
      Class classObj = Class.forName(className.toString());
      Element elemObj = (Element)classObj.newInstance();
      elemObj.setParent(fileName);
      elemObj.setName(type);
      elemObj.setIndentPosition(indentPos);
      if (pkgDataIdx >= 0)
        elemObj.setPkgDataIndex(pkgDataIdx);
      return elemObj;
    }
    catch (ClassNotFoundException ex)
    {
      return new Element(type, fileName, indentPos, pkgDataIdx);
    }
    catch (InstantiationException ex)
    {
      return new Element(type, fileName, indentPos, pkgDataIdx);
    }
    catch (IllegalAccessException ex)
    {
      return new Element(type, fileName, indentPos, pkgDataIdx);
    }
  }

  /**
   * Returns the value of the element with specified name. Return could be a
   * null value. The value of the element is derived from the properties
   * that are represented by props object that is returned by getProperties
   * method of PropertyFileInterpreter class.
   *
   * @param name name of an element
   * @param parent a valid shippable file name
   * @param pkgDataIdx index of the packageData element
   */
  public static String getValue( String name, String parent, int pkgDataIdx )
  {
    String retVal = PropertyFileInterpreter.getInstance().getValue(name, parent);

    // If the property corresponding to this package data element contains
    // more than one value then it means there are more than one package
    // data for the parent (shippable file). So, we would like to extract
    // the value for this package data. We get the value using the index of
    // this pacakge data.
    if (retVal != null &&
        retVal.indexOf(PACKAGEDATA_SEPARATOR) >= 0 &&
        pkgDataIdx >= 0)
      retVal = getPkgDataValue(retVal, pkgDataIdx);

    // If the value contains a empty string then return null, since
    // an empty string is not a valid value. So, by returning a null
    // the XML corresponding to this element will not be generated in
    // the XML file.
    if (retVal != null && retVal.trim().length() == 0)
      retVal = null;
    return retVal;
  }

  /**
   * Returns the value at the specified index from the specified
   * list of values
   *
   * @param value a string that contains | separated values
   * @param pkgDataIdx index of the value that needs to be returned
   * @return the value at the index specified by plgDataIdx parameter
   */
  private static String getPkgDataValue( String value, int pkgDataIdx )
  {
    String retValue = null;
    StringTokenizer st =
      new StringTokenizer(value, PACKAGEDATA_SEPARATOR);
    int idx = 0;
    while (st.hasMoreTokens())
    {
      String token = st.nextToken();
      if (idx == pkgDataIdx)
      {
        retValue = token;
      }
      if (idx == pkgDataIdx)
        break;
      idx++;
    }
    return retValue;
  }

  /**
   * Returns true if the specified file type is a directory
   *
   * @param fileType a valid file type value
   */
  public static boolean isDirectory( String fileType )
  {
    if (fileType != null && fileType.equalsIgnoreCase("D"))
      return true;
    else
      return false;
  }

  /**
   * Returns true if the specified file type is a link
   *
   * @param fileType a valid file type value
   */
  public static boolean isLink( String fileType )
  {
    if (fileType != null &&
        (fileType.equalsIgnoreCase("H") ||
         fileType.equalsIgnoreCase("S") ||
         fileType.equalsIgnoreCase("symlink")))
      return true;
    else
      return false;
  }
}
