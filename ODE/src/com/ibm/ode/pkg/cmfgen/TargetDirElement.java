/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:36:08
 * Date and Time File was extracted/checked out: 06/04/13 16:45:25
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

/**
 * This class represents the 'targetDir' element
 *
 * @author Anil Ambati
 * @version 1.1
 */
public class TargetDirElement extends Element
{
  /**
   * Overrides the <code>getValue</code> method of
   * <code>com.ibm.ode.pkg.cmfgen.Element</code>.
   *
   * @return returns the value of sourceDir element if the value of this
   *         element is not set.
   */
  public String getValue()
  {
    String val = super.getValue();
    if (val == null)
    {
      val = PropertyFileInterpreter.getInstance().
        getValue(SOURCEDIR_XML_TAG_NAME, this.parent);
    }
    return val;
  }
}
