package com.ibm.ode.pkg.parserGenerator.service;

import com.ibm.ode.pkg.parserGenerator.EntityTreeObject;
import com.ibm.ode.pkg.parserGenerator.EntityTreeRoot;
import com.ibm.ode.pkg.parserGenerator.FileEntity;
import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import com.ibm.ode.pkg.parserGenerator.InstallEntity;
import com.ibm.ode.pkg.parserGenerator.Package;
import java.util.*;

/**********************************************************************
 * This class is responsible for extracting file stanza information from
 *   the CMF. It extracts the FileEntities that are present in the
 *   EntityTree and generates a list of files to be packaged.
 *   <p>This class traverses all
 *   nodes in the tree and process any file stanza nodes it finds during
 *   the tree search.
 *   <P>This class is designed to be platform independent. The 
 *   location of the data in the EntityTree and the data in the CMF may be
 *   different for each supported platform. The data extracted from the 
 *   CMF and the presentation of the data to the service process may be  
 *   different for each supported platform.
 *   <P>Differences in the CMF data and the entity tree are handled by the
 *   TreeCustomizer class. There is a subclass of TreeCustomizer for 
 *   each supported platform.
 *   <p>Differences in the processing of the data extracted from the CMF is
 *   handled by the MetaDataHandler. Each supported platform provides a 
 *   subclass of MetaDataHandler to handle the platform specific processing 
 *   of the meta data extracted from the EntityTree. 
 *
 * An example CMF is:
 *    file {
 *       sourceDir = /bin;
 *       sourceFile = sbls;
 *       targetDir = /usr/bin;
 *       targetFile = odesbls;
 *    }
 *
 * This generator can be invoked as follows
 * For example:
 * <pre>
 *   TreeHandler generator = new TreeHandler(
 *       new MVSNodeHandler(MVSNodeHandler.toStageFilter),
 *       shipRootDir_, context_, pkgControlDir_,
 *       pkgType_, pkgClass_, pkgFixStrategy_ );		
 *   generator.generateTargetMetadataFiles( entityTreeRoot, packageObject );
 * </pre>
 *
 * @see Generator
 * @see GeneratorInterface
 * @see TreeCustomizer
 * @see NodeHandler
 * @see B390Formatter
 *
 * @author Bruce Gibbons
 * @version 0.1     9/19/2000  Created
 * @version 0.11   10/30/2000  Make the output file customizable. Add the 
 *  metadata required by MVS to copy the file from HFS to TSO 
 * @version 0.12   11/6/2000  Update the FileListGenerator to support a 
 *  generic data handler(NodeHandler). Rename the FileListGenerator to
 *  TreeHandler. The NodeHandler will process the data extracted 
 *  by the TreeHandler. The data extraction cutomizations will be 
 *  handled by a new class TreeCutomizer which is provided by the 
 *  NodeHandler. The NodeHandler will be provided in the constructor.
**/

public class TreeHandler extends com.ibm.ode.pkg.parserGenerator.Generator
	implements com.ibm.ode.pkg.parserGenerator.GeneratorInterface
{

  /**********************************************************************
   * Private TreeHandler variable, maintains a reference to EntityTreeRoot
   *   used for retreiving the logical tree structure binding entities
   **/
  private EntityTreeRoot etr_ ;

  /**********************************************************************
   * Private NodeHandler variable, maintains a reference to the
   * operating system metadata handler.
   **/
  private NodeHandler nodeHandler;

  /**********************************************************************
   * Private TreeCustomizer variable, maintains a reference to the
   * operating system metadata customizer.
   **/
  private TreeCustomizer treeCustomizer;

  /**********************************************************************
   * Creates a new instance of type TreeHandler.
   **/
  public TreeHandler(NodeHandler nodeHandler,
                           String shipRootDir,
                           String context,
                           String pkgControlDir,
                           String pkgType,
                           String pkgClass,
                           String pkgFixStrategy,
                           String apar ) throws GeneratorException
  {
    super( shipRootDir,
           context,
           pkgControlDir,
           pkgType,
           pkgClass,
           pkgFixStrategy,
           apar ) ;
    setNodeHandler(nodeHandler);

  }//end constructor

  /**********************************************************************
   * This sets the NodeHandler. 
   * @parm NodeHandler nodeHandler The nodeHandler
   **/
  private void setNodeHandler(NodeHandler nodeHandler) throws GeneratorException
  {
    if (nodeHandler==null) throw new GeneratorException("TreeHander: nodeHandler=null");
    this.nodeHandler=nodeHandler;
    setTreeCustomizer(this.nodeHandler.getTreeCustomizer());
    this.nodeHandler.setTreeHandler(this);
  }

  /**********************************************************************
   * This sets the TreeCustomizer. 
   * @parm TreeCustomizer treeCustomizer The treeCustomizer
   **/
  private void setTreeCustomizer(TreeCustomizer treeCustomizer) throws GeneratorException
  {
    if (treeCustomizer==null) throw new GeneratorException("TreeHander: TreeCustomizer=null");
    this.treeCustomizer=treeCustomizer;
  }

  /**********************************************************************
   * Primary method used to invoke logic to generate Target Metadata files
   *
   * @param etr Reference to EntityTreeRoot object which holds reference to
   * logical structure of Entities as a tree.
   * @param pkgRef Reference to Package object which holds reference to
   * Parsed Entities and their data.
   *
   * @exception GeneratorException if an error occurs in generating control
   * files, or information about structure of entities (mutual links binding
   * them) are incorrectly specified with respect to the acceptable structure.
   **/
  public void generateTargetMetadataFiles(
          EntityTreeRoot etr,
          Package pkgRef )
	throws GeneratorException
  {
    ArrayList levelArray, productArray;
    Object obj;
    EntityTreeObject curETO;
    int noOfProducts = 0;
    this.nodeHandler.init();
    try
    {
      this.etr_ = etr;

      levelArray = etr_.getLevelArray();
      if (levelArray == null) //level Array not instantiated
      {
        throw new GeneratorException("TreeHandler: At least one "
                + "InstallEntity must be specified.\n");
      }
      if (levelArray.isEmpty() ) //level Array not populated
      {
        throw new GeneratorException("TreeHandler: At least one "
                + "InstallEntity must be specified.\n");
      }
      int level=0;
      obj = levelArray.get(level); //products exist at level one only
      if ( (obj instanceof ArrayList) == false )
      {
        throw new GeneratorException("TreeHandler: Product array"
                + " not encountered at level 0 in level array.\n");
      }
      productArray = (ArrayList)obj;


      noOfProducts = productArray.size();
      for (int i=0; i<noOfProducts; i++)
      {
        obj = productArray.get(i); //products exist at level one only
        if ( (obj instanceof EntityTreeObject) == false )
        {
          throw new GeneratorException("TreeHandler: EntityTreeObject"
                              + " type objects expected in product array.\n");
        }
        curETO = (EntityTreeObject)obj;
        parseTreeObject(level,curETO);
        this.nodeHandler.finish();
      }
    }
    catch(Exception e)
    {
      throw new GeneratorException("TreeHandler: Exception " +
              "encountered\n\n" + e.toString() );
    }

  }//end method
  /**********************************************************************
   * Primary method used to invoke logic to generate Target Metadata files
   *
   * @param etr Reference to EntityTreeRoot object which holds reference to
   * logical structure of Entities as a tree.
   * @param pkgRef Reference to Package object which holds reference to
   * Parsed Entities and their data.
   *
   * @exception GeneratorException if an error occurs in generating control
   * files, or information about structure of entities (mutual links binding
   * them) are incorrectly specified with respect to the acceptable structure.
   **/
  public void parseTreeObject(int level, EntityTreeObject treeObject)
    throws GeneratorException
  {
    if (level==this.treeCustomizer.getProductLevel()) {
      this.nodeHandler.handleProduct(treeObject);
    }
    if (level==this.treeCustomizer.getFileLevel()) {
      FileEntity fileEntity=treeObject.getFileEntityReference();
      if (fileEntity!=null) 
      {
        this.nodeHandler.handleFile(fileEntity);
      }
    }
    level++;
    ArrayList children=treeObject.getChildReferenceArray();
    int numberOfChildren=children.size();
    for(int i=0;i<numberOfChildren;i++)
    {
      parseTreeObject(level,(EntityTreeObject)children.get(i));
    }
  }

  /**********************************************************************
   * This method returns the ship root directory. 
   * @return String The ship root directory. 
   **/
  public String getShipRootDir()
  {
    return shipRootDir_;
  }

  /**********************************************************************
   * This method returns the package control directory. 
   * @return String The package control directory. 
   **/
  public String getPackageControlDir()
  {
    return pkgControlDir_;
  }

  /**********************************************************************
   * This method returns the apar. 
   * @return String The apar name. 
   **/
  public String getApar()
  {
    return apar_;
  }

}  //end class definition
