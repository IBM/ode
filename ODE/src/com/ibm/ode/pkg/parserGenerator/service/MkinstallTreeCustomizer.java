package com.ibm.ode.pkg.parserGenerator.service;

/**********************************************************************
 * This class is responsible handling the mkinstall plaform specific differences 
 * extraction of metatdata from the CMF. 
 * <p>Different platforms have file stanzas at diffenent locations 
 * in the tree. See the packaging 
 * reference for more details. 
 * 
 * @see TreeHandler
 * @see TreeCustomizer
 * @see NodeHandler
 * @see B390Formatter 
 *
 * @version 0.1   9/19/2000 Created 
 * @version 0.11 11/06/2000 Split the FileListCustomizer in to two classes.
 *  The TreeCustomizer handles only the platform differences associated
 *  with extracting the CMF data. The MVSTreeCustomizer handles the
 *  customizations required for MVS platform metadata. 
 * @author Bruce Gibbons
**/

public class MkinstallTreeCustomizer implements TreeCustomizer
{
  /**********************************************************************
   * This method returns the level in the tree that contains product 
   * information. 
   * @return int The level in the tree that contains the product. 
   * <p>
   * <p>
   * <B>See Also:</B> Documents on <a href = "http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_ship_data.html">Metadata Summary</a> and <a href="http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_cmf_req.html">CMF to MVS response file Translation</a>.
   **/
  public int getProductLevel()
  {
    return 0;
  }
  /**********************************************************************
   * This method returns the level in the tree that contains file entity 
   * information. 
   * @return int The level in the tree that contains the product. 
   * <p>
   * <p>
   * <B>See Also:</B> Documents on <a href = "http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_ship_data.html">Metadata Summary</a> and <a href="http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_cmf_req.html">CMF to MVS response file Translation</a>.
   **/
  public int getFileLevel()
  {
    return 2;
  }
}  //end class definition
