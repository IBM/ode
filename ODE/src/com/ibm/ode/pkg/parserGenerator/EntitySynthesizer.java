package com.ibm.ode.pkg.parserGenerator;

import java.util.*;

/**
 * This is a Control Class that creates the Tree Structure from the pool of
 * InstallEntities and the FileEntities. This class organizes the installEntities
 * and fileEntities in terms of levels which will be used by the generator to access
 * information to write out to the control files. These levels are created so that
 * a specific generator can understand which installEntity refers to a product,
 * a fileset or a file.
 * @see         EntityTreeRoot
 * @see         EntityTreeObject
 * @version	1.5 97/05/09
 * @author 	Prem Bala
 **/

public class EntitySynthesizer
{
  /**
   * indicates the currentLevel being filled
   **/
  private int curLevel_;
 int count;

  /*****************************************************************************
   * Constructor for EntitySynthesizer
   **/
  public EntitySynthesizer()
  {
    curLevel_ = 0;
  }

 /******************************************************************************
   * Checks whether the installEntities and FileEntities are unique.
   * @param packageObject the Package Object Reference
   * @return void
   * @exception EntitySynthesizerException if duplicates are found
   **/
  private void checkForUniqueEntities( Package packageObject )
               throws EntitySynthesizerException
  {
    Hashtable  installEntityhashtable = packageObject.getInstallEntityArray();
    Hashtable fileEntities   = packageObject.getFileEntities();
    ArrayList  packageDataArray;
    ListIterator packageDataIterator;

    HashSet  uniquePackageDataKeySet = new HashSet();

    Enumeration entityEnum;
    for( entityEnum = fileEntities.elements();
	       entityEnum.hasMoreElements(); )
    {
	    FileEntity curFileEntity = (FileEntity)entityEnum.nextElement();
	
			// Check uniqueness of PackageData entries in FileEnity stanzas
      uniquePackageDataKeySet.clear();
      packageDataArray = curFileEntity.getPackageData();
      String targetFile;
      String targetDir;
      String packageDataKeyValue;
      packageDataIterator = packageDataArray.listIterator();
      while( packageDataIterator.hasNext() )
      {
        PackageData curPackageData = (PackageData)packageDataIterator.next();
        targetFile =  (String)curPackageData.getTargetFile();
        targetDir =  (String)curPackageData.getTargetDir();
        if ((targetFile == null) && (targetDir == null))
          packageDataKeyValue = "";
        else if (targetFile == null)
          packageDataKeyValue = targetDir;
        else if (targetDir == null)
          packageDataKeyValue = targetFile;
        else
          packageDataKeyValue = targetDir + targetFile;

	      if( uniquePackageDataKeySet.contains( packageDataKeyValue ) )
	      {
          throw new
	          EntitySynthesizerException("EntitySynthesizerException : \n"
				    + " Duplicate PackageData entries found."
            + "  PackageData entry \"" + packageDataKeyValue + "\" has"
            + " occured multiple times." );	
	      }
        else
        {
          uniquePackageDataKeySet.add( packageDataKeyValue );
        }
      }
    }
  }

  /******************************************************************************
   * Creates the tree structure from the pool of installEntities and fileEntities
   * @param packageObject the Package Object Reference
   * @param entityTreeRoot  the EntityTreeRoot Object Refernce
   * @return void
   * @exception EntitySynthesizerException if creating the Tree structure fails
   **/

  public void synthesizeEntities( Package packageObject,
				  EntityTreeRoot entityTreeRoot )
                 throws EntitySynthesizerException
  {
    Hashtable  installEntityhashtable ;

    installEntityhashtable = packageObject.getInstallEntityArray();

    if( installEntityhashtable == null )
      throw new EntitySynthesizerException( " EntitySynthesizerException : \n "
					    + "Unable to obtain any "
					    + " installEntities from the Package Class ."
					    + "There should be atlease one installEntity specified in the CMF. "		
					    + " Error in EntitySynthesizer.synthesizeEntities. \n"
					    );



    // First check to make sure all the entities stored in the Package
    // are unique. If not an exception is thrown,
    checkForUniqueEntities( packageObject );

    // create the First level of entity tree objects
 	Enumeration InstallEntityEnum;
        for (InstallEntityEnum = installEntityhashtable.elements();
                InstallEntityEnum.hasMoreElements();)
      {
	InstallEntity curInstallEntity = (InstallEntity)InstallEntityEnum.nextElement();

	String parent = curInstallEntity.getParent();
	if( parent == null )
	  {
	    curInstallEntity.setMarkReferenced();

	    entityTreeRoot.insertAsLevelOneInstallEntity( curInstallEntity );
      try
      {
        entityTreeRoot.checkAndSetSwpackageParamsIfPresent( curInstallEntity );
      }
      catch( EntityTreeRootException e )
      {
        throw new EntitySynthesizerException(
            "EntitySynthesizerException : \n " + e.toString() );
      }
	  }
      }
    try
      {
	while( entityTreeRoot.checkIfNextLevelExists( curLevel_ ) )
	  {
	    entityTreeRoot.addAnotherLevel();	
	    entityTreeRoot.extendTreeAfterCurrentLevel( curLevel_, packageObject );	
	    incrementCurLevel();
	  }
      }
    catch( EntityTreeRootException e )
      {
	throw
	  new EntitySynthesizerException( "EntitySynthesizerException : \n"
					  + " " +  e.toString() );
      }

  }


  /************************************************************************
   * increments the currentLevel being filled
   * @return void
   **/
  private void incrementCurLevel()
  {
    curLevel_++;
  }

}
