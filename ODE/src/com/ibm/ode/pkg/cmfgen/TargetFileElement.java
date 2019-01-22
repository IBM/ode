/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:36:12
 * Date and Time File was extracted/checked out: 06/04/13 16:45:26
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

/**
 * This class represents the 'targetFile' element
 *
 * @author Anil Ambati
 * @version 1.1
 */
public class TargetFileElement extends Element
{
  /**
   * Overrides the <code>getValue</code> method of
   * <code>com.ibm.ode.pkg.cmfgen.Element</code>
   *
   * @return returns the value of sourceFile element if the value of this
   *         element is not set.
   */
  public String getValue()
  {
    Element fileType =
      Element.newElement(FILETYPE_XML_TAG_NAME, this.parent, 0, this.pkgDataIdx);
    String fileTypeVal = fileType.getValue();

    // If the file type is a directory or a link then we dont need to have a value
    // for targetFile, so no defaulting is done.
    String val = super.getValue();
    if (Element.isDirectory(fileTypeVal) ||
        Element.isLink(fileTypeVal))
    {
      return val;
    }

    if (val == null)
    {
      val = PropertyFileInterpreter.getInstance().
        getValue(SOURCEFILE_XML_TAG_NAME, this.parent);
    }
    return val;
  }
}
