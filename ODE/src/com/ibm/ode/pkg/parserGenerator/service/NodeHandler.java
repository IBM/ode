package com.ibm.ode.pkg.parserGenerator.service;

import com.ibm.ode.pkg.parserGenerator.EntityTreeObject;
import com.ibm.ode.pkg.parserGenerator.FileEntity;
import com.ibm.ode.pkg.parserGenerator.InstallEntity;
import com.ibm.ode.pkg.parserGenerator.Generator;
import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import java.util.*;

/**********************************************************************
 * This class is responsible handling the plaform specific differences 
 * in the processing of metadata such as the generation of the 
 * list of file to be packaged. 
 * 
 * <p>File stanzas may represent files, links, or directories. 
 * System    file   link   directory
 * Solaris    f       s       d
 * hp         F       S       D 
 * nt         -       -       -
 * AIX        F       S       D 
 * MVS        mod/hfs -       -
 * 
 * The handler will work in 2 modes:
 *    1) List all the file stanzas in the CMF 
 *    2) List only the file stanzas in the CMF that have cooresponding real files in $TOSTAGE
 *
 * @see TreeHandler
 * @see TreeCustomizer
 * @see B390Formatter
 *
 * @version 0.1    9/19/2000
 * @version 0.11  11/06/2000 Split the FileListCustomizer in to two classes.
 *  The TreeCustomizer handles only the platform differences associated
 *  with extracting the CMF data. The NodeHandler processes the data 
 *  after it is extracted. 
 * @author Bruce Gibbons
**/

public abstract class NodeHandler 
{

  /**********************************************************************
   * Private filter variable, used keep a reference to the generator 
   * driving the file list. The generator reference is needed because 
   * the customizer may need data from the generator that is not 
   * passed as a parameter. 
   **/
  TreeHandler treeHandler;             // Reference to the generator.  

  /**********************************************************************
   * FileListGenerator variable, used to maintain a handle to the
   *   product InstallEntity.
   **/
  InstallEntity productIE;

  /**********************************************************************
   * ArrayList variable, used to maintain a handle to distribuition Libraries
   *   for the product.
   **/
  ArrayList distLibs;

  /**********************************************************************
   * FileEntity variable, used to maintain a handle to the
   *   fileEntity.
   **/
  private FileEntity fileEntity;

  /**********************************************************************
   * ArrayList variable, used to maintain a handle to the
   *   packageData for the current file Entity.
   **/
  private ArrayList packageData;

  /**********************************************************************
   * ServiceHandler constructor. 
   * @param int filter The type of filtering to be used when generating output. 
   **/
  public NodeHandler() throws GeneratorException
  {
    super();
  }

  /**********************************************************************
   * This method sets the TreeHandler. The TreeHandler provides information 
   * from the current tree node and its parent nodes. 
   * @param int filter The type of file list filtering to be used when 
   * generating output. 
   * @see B390LSTFormatter  
   * @see B390UPDFormatter  
   **/
  void setTreeHandler(TreeHandler treeHandler)  
    throws GeneratorException 
  {
    if (treeHandler==null) {
      throw new GeneratorException("The treeHandler can not be null. ");
    }
    this.treeHandler=treeHandler;
  }

  /**********************************************************************
   * This method sets the product install entity. 
   * @param InstallEntity productIE The product 
   * intall entity.
   * @throws GeneratorException
   **/
  private void setProductInstallEntity(InstallEntity productIE)
    throws GeneratorException
  {
    if (productIE==null) {
      throw new GeneratorException("The product can not be null. ");
    }
    this.productIE=productIE;
    this.distLibs=null;
  }

  /**********************************************************************
   * This method sets the file entity. 
   * @param FileEntity fileEntity The file entity.
   * @throws GeneratorException
   **/
  private void setFileEntity(FileEntity fileEntity)
    throws GeneratorException
  {
    if (productIE==null) {
      throw new GeneratorException("The product can not be null. ");
    }
    if (fileEntity==null) {
      throw new GeneratorException("The file can not be null. ");
    }
    this.fileEntity=fileEntity;
    packageData=fileEntity.getPackageData();;
  }

 /********************************************************************
  * This method gets all package data for this file.
  * @throws GeneratorException
  **/
  ArrayList getPackageData()
		throws GeneratorException
  {
    if (fileEntity == null)
    {
  	throw new GeneratorException("NodeHandler: Instance of File "
         + "Entity expected but not found.");
    }
    if (packageData==null) 	
  	throw new GeneratorException("NodeHandler: Instance of PackageData "
         + "expected but not found.");
    return packageData;
  }

  /**********************************************************************
   * This method returns the toStageDir. 
   * @return String the toStageDir. 
   **/
  public String getToStageDir()
  {
    return treeHandler.getShipRootDir();
  }
  /**********************************************************************
   * This method returns the package control directory. 
   * @return String the pkgControlDir. 
   **/
  public String getPkgControlDir()
  {
    return treeHandler.getPackageControlDir();
  }
 /**********************************************************************
   * This method returns the source directory. The source directory
   * contains the class files.
   * @return String The source directory.
   * @throws GeneratorException
   * <p>
   * <p>
   * <B>See Also:</B> Documents on <a href = "http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_ship_data.html">Metadata Summary</a> and <a href="http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_cmf_req.html">CMF to MVS response file Translation</a>.
   **/
  public String getSourceDir() throws GeneratorException
  {
    String sourceDir = fileEntity.getSourceDir();
    if (sourceDir != null)
    {
      //if first character is not a '/' , prepend one
      if ( sourceDir.charAt( 0 ) != '/' )
      {
        sourceDir="/" + sourceDir;
      }
      //if last character is not a '/' , append one at end
      if ( sourceDir.charAt( sourceDir.length() - 1 ) != '/' )
      {
        sourceDir=sourceDir+"/";
      }
    }
    return sourceDir;
  }
  /**********************************************************************
   * This method returns the source file. Source files are class files.
   * @return String The source file.
   * @throws GeneratorException
   * <p>
   * <p>
   * <B>See Also:</B> Documents on <a href = "http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_ship_data.html">Metadata Summary</a> and <a href="http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_cmf_req.html">CMF to MVS response file Translation</a>.
   **/
  public String getSourceFile() throws GeneratorException
  {
    String sourceFile = fileEntity.getSourceFile();
    if (sourceFile == null)
    {
      sourceFile=""; //The source file will be null if the fileEntity is a directory.
      //throw new GeneratorException("FileListGenerator: " +
      //            "SourceFile cannot be null - It forms a required field");
    }
    return sourceFile;
  }

  /**********************************************************************
   * This method returns the distlibs information from the cmf 
   * for the current product  install entity. 
   * @return String The distlibs information for the current product 
   * intall entity.
   * @throws GeneratorException
   **/
  ArrayList getDistLibs() throws GeneratorException
  {
    if (distLibs == null)
    {
      distLibs = productIE.getDistlibs();
    }
    if ((distLibs == null)||distLibs.isEmpty())
    {
      throw new GeneratorException("ServiceHandler: DistLibs + " +
                    "distLibs cannot be null - It forms a required field");
    }
    return distLibs;
  }

/********************************************************************
 * This method generates information about one file stanza
 * @param productIE InstallEntity The product install entity
 * @param fileEntity FileEntity the current file entity. 
 * @see GeneratorException
 **/
 void handleFile(FileEntity fileEntity)
		throws GeneratorException
  {
    setFileEntity(fileEntity);
  } //end method handleFile

/******************************************************************
 * This method generates one product at a time.
 * Private FileListGenerator method.
 *
 * @param productETO a valid EntityTreeObject instance which is
 * expected to hold a reference to the product InstallEntity.
 *
 * @see GeneratorException
 **/
 boolean handleProduct(EntityTreeObject productETO)
  throws GeneratorException
  {
    InstallEntity productIE = productETO.getInstallEntityReference();
    if (this.productIE==productIE) 
    {
      throw new GeneratorException("New product can not be the same as the old product");
    }
    setProductInstallEntity(productIE);
    this.setProductInstallEntity(productIE);
    ArrayList childArray;
    EntityTreeObject childETO;
    Object obj;
    childArray = productETO.getChildReferenceArray();

    if (childArray == null)
    {
  	throw new GeneratorException("FileListGenerator: At least one file "
               + "Entity must be a child of every Install Entity");
    }

    if (childArray.isEmpty())
    {
  	throw new GeneratorException("FileListGenerator: At least one file "
               + "Entity must be a child of every Install Entity");
    }

    for (int i=0; i<childArray.size(); i++)
    {
      obj = childArray.get(i);
      if ( (obj instanceof EntityTreeObject) == false)
      {
        throw new GeneratorException("FileListGenerator: Instances of type"
                    + " EntityTreeObject expected in childArray in " +
                      " product ETO");
      }

      childETO = (EntityTreeObject)obj;
    }

    return true;
  }//end method handleProduct
/********************************************************************
 * This method generates information about one file stanza
 * @param productIE InstallEntity The product install entity
 * @param curFE FileEntity the current file entity. 
 * @see GeneratorException
 **/
 void init()
    throws GeneratorException
  {
  } //end method handleFile

/********************************************************************
 * This method generates information about one file stanza
 * @param productIE InstallEntity The product install entity
 * @param curFE FileEntity the current file entity. 
 * @see GeneratorException
 **/
 void finish()
    throws GeneratorException
  {
  } //end method handleFile

 /********************************************************************
  * This method returns the tree customizer that will be used with
  * this NodeHandler.
  * @return TreeCustomizer The tree customizer that will be used with
  * this NodeHandler
  * @see TreeHandler
  * @see TreeCustomizer
  **/
  abstract TreeCustomizer getTreeCustomizer();
}  //end class definition
