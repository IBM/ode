/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:57
 * Date and Time File was extracted/checked out: 06/04/13 16:45:23
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

/**
 * This represents an subelement of partInfo XML Element.
 *
 * @author Anil Ambati
 * @version 1.1
 */
public class PartInfoSubElement extends Element
  implements Constants
{
  /**
   * Overrides the getValue method of Element.
   */
  public String getValue()
  {
    String nameWithPartInfo = PARTINFO_PROP_PREFIX + name;
    return Element.getValue(nameWithPartInfo, this.parent,
                            this.pkgDataIdx);
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
  public static PartInfoSubElement
    newPartInfoSubElement( String name, String parent,
                           int indentPos, int pkgDataIdx )
  {
    PartInfoSubElement elemObj = new PartInfoSubElement();
    elemObj.setName(name);
    elemObj.setParent(parent);
    elemObj.setIndentPosition(indentPos);
    if (pkgDataIdx >= 0)
      elemObj.setPkgDataIndex(pkgDataIdx);
    return elemObj;
  }
}
