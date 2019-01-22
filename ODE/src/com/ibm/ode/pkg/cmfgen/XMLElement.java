/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:36:21
 * Date and Time File was extracted/checked out: 06/04/13 16:45:27
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.util.*;
import com.ibm.ode.lib.string.StringTools;

/**
 * This class represents an XML element. It is an abstract class.
 *
 * @author Anil Ambati
 * @version 1.1
 */
public abstract class XMLElement extends Object
  implements Constants
{
  /**
   * Tag name of this XML element
   */
  protected String name = null;

  /**
   * Attributes of this XML element
   */
  protected Hashtable attributes = null;

  /**
   * Position of this element in the hierarchy of XML document
   */
  protected int indentPosition = 0;

  /**
   * Returns the value of this element. Only elements with no subelements
   * should override this method.
   *
   * @return value of this element. It could be null.
   */
  public String getValue()
  {
    return null;
  }

  /**
   * Returns XML form of this Object. Uses getValue() method to get the
   * value of this element. If getValue() returns a null, then this method
   * returns a null. Elements with no subelements should follow this
   * protocol.
   *
   * @return string that represents the xml form of this element
   */
  public String toXML()
  {
    String val = getValue();
    if (val == null) return null;

    StringBuffer sb = new StringBuffer(getMyIndent());
    sb.append(getBeginTag()).append(getValue());
    sb.append(getEndTag());
    return sb.toString();
  }

  /**
   * Returns the begin tag for this element
   */
  protected String getBeginTag()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<").append(this.getName());
    if (attributes != null)
    {
      Enumeration enumer = this.attributes.keys();
      while (enumer.hasMoreElements())
      {
        sb.append(" ");
        Object key = enumer.nextElement();
        sb.append(key.toString());
        sb.append("=\"").append(this.attributes.get(key).toString());
        sb.append("\"");
      }
    }
    sb.append(">");
    return sb.toString();
  }

  /**
   * Returns the end tag for this element
   */
  protected String getEndTag()
  {
    return "</" + getName() + ">";
  }

  /**
   * Returns the string that represents the indent of this element
   */
  protected String getMyIndent()
  {
    return StringTools.getStringWithSpaces(indentPosition * INDENT_SPACE);
  }

  /**
   * Returns the tag name of this element. If tag name is not set then
   * the name of the class with first letter in lower case is returned.
   */
  public String getName()
  {
    if (this.name == null)
    {
      String className = this.getClass().getName();
      className = className.substring(className.lastIndexOf(".") + 1,
                                      className.length());
      char[] chars = className.toCharArray();
      chars[0] = Character.toLowerCase(chars[0]);
      this.name = String.valueOf(chars);
    }
    return this.name;
  }

  public void setName( String name )
  {
    this.name = name;
  }
  public Hashtable getAttributes()
  {
    return this.attributes;
  }
  public void setAttributes( Hashtable attributes )
  {
    this.attributes = attributes;
  }
  public void setIndentPosition( int pos )
  {
    if (pos >= 0)
      this.indentPosition = pos;
  }
  public int getIndentPosition()
  {
    return this.indentPosition;
  }
}
