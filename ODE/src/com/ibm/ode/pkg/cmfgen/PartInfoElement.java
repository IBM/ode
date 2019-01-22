/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:52
 * Date and Time File was extracted/checked out: 06/04/13 16:45:22
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.util.*;

/**
 * This represents partInfo XML Element.
 *
 * @author Anil Ambati
 * @version 1.1
 */
public class PartInfoElement extends Element
  implements Constants
{
  /**
   * Represents names of the subelements of a partInfo element
   */
  static Vector mySubElements = new Vector();

  /**
   * Returns xml form of this object. The return value could be null.
   */
  public String toXML()
  {
    StringBuffer sb = new StringBuffer();
    String myIndent = getMyIndent();
    int subElementIndentPos = getIndentPosition() + 1;

    // Start forming the XML
    StringBuffer subElements = new StringBuffer();
    PartInfoSubElement elemObj = null;
    for (int idx = 0; idx < PartInfoElement.mySubElements.size(); idx++)
    {
      String elemName = (String)PartInfoElement.mySubElements.elementAt(idx);
      elemObj =
        PartInfoSubElement.newPartInfoSubElement(elemName, parent,
                                                 subElementIndentPos,
                                                 pkgDataIdx);
      String xmlStr = elemObj.toXML();
      if (xmlStr != null)
        subElements.append(xmlStr).append(NEW_LINE);
      elemObj = null;
    }
    if (subElements.length() > 0)
    {
      sb.append(myIndent);
      sb.append(getBeginTag()).append(NEW_LINE);
      sb.append(subElements.toString());
      sb.append(myIndent).append(getEndTag());
      return sb.toString();
    }
    else
      return null;
  }
}
