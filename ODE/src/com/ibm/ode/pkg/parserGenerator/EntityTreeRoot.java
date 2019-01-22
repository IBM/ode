package com.ibm.ode.pkg.parserGenerator;

import java.util.*;

/*****************************************************************************
 * This class holds references to the EntityTreeObjects which represent the
 * level information of the installEntities and the fileEntities. It
 * is responsible for creating the EntityTreeObjects and holding them in
 * appropriate location in the Tree-Level struture. It also  is responsible
 * for providing the access path to the InstallEntities and FileEntities to
 * the Generators. This class holds SWPACKAGE specific package stanza
 * information and acts as the root for all the InstallEntity-FileEntity
 * heirarchy.
 *
 * @version     1.25 98/01/22
 * @author Prem Bala
 ****************************************************************************/

public class EntityTreeRoot
{
   // Variables to hold SWPACKAGE specific package stanza information
   private String        packageName_;
   private String        fullPackageName_;
   private PgSpecialType packageDescription_;
   private PgSpecialType packageCopyright_;
   private String        packageSerialNumber_;
   private String        packageVendorName_;
   private String        packageVendorTitle_;
   private PgSpecialType packageVendorDesc_;

   private boolean       SWPACKAGESET;
   /************************************************************************
    * Variables to hold the tree-level structure
    **/
   private ArrayList levelArray_;

   /************************************************************************
    *  a variable which gives the status of the next level
    **/
   private boolean nextLevelExists_;

   /************************************************************************
    * Variable to hold the list of InstallableItems : 
    **/
   private ArrayList installableItemsArray_;
   private ArrayList repackagableFilesArray_ ;
  
   /************************************************************************
    * Constructor for the EntityTreeRoot
    **/
   public EntityTreeRoot()
   {
      SWPACKAGESET                = false;
      nextLevelExists_            = true;
      levelArray_                 = new ArrayList();
      repackagableFilesArray_     = new ArrayList();
      installableItemsArray_      = new ArrayList();

      // create the first level in the constructor itself
      levelArray_.add( new ArrayList() );
   }

   /**********************************************************************
    * Checks whether the next level in the tree structure exists.
    *
    * @param curLevl the currentLevel 
    * @return boolean to indicate whether it exits or not
    **/
   public boolean checkIfNextLevelExists( int curLevel )
                 throws EntityTreeRootException
   {
      ArrayList    curLevelArray = (ArrayList)levelArray_.get( curLevel );
      ListIterator curLevelIterator;

      nextLevelExists_ = false;

      /* For each of the EntityTreeObject in the last level
       * check to see if any of them refers to an installEntity
       * if so, the the next level exists else this is the last 
       * level
       */
    
      if( curLevelArray == null )
      {
         throw new EntityTreeRootException("EntityTreeRootException : \n"
                      + "EntityTreeRoot.checkIfNextLevelExists : \n "
                      + "Unable to obtain entities at the present level "
                      + " " + curLevel + ". " 
                      + "No Entities found at this level. \n"
                      + "There should be atleast one installEntity with its\n"
                      + "parent field set to NULL ( or not specified ).\n"
                      + "This installEntity should refer to the Product that "
                      + "is to be packaged ." );
      }
    
      if(  curLevelArray != null  &&  curLevelArray.isEmpty() )
      { 
         throw  new EntityTreeRootException("EntityTreeRootException :\n"
               + "EntityTreeRoot.checkIfNextLevelExists  : \n " 
               + " Unable to obtain entities at the  present  level  "  
               + "  "  +  curLevel + ". \n" 
               + " No entities hve been specified at this level.\n " 
               + " There should be atleast one installEntity with it's "  
               + "parent  field set to NULL ( or not specified ). \n " 
               + "This installEntity should refer to the Product that " 
               + " is to be packaged " );        
      }
    
      curLevelIterator = curLevelArray.listIterator();
      while( curLevelIterator.hasNext() )
      {
         EntityTreeObject curEto;        
         int type;
        
         curEto = (EntityTreeObject)curLevelIterator.next();
         type = curEto.getType(); 

         if( type == ParserGeneratorEnumType.INSTALLENTITY  )  
         { 
            nextLevelExists_ = true; 
            break; 
         } 
      }
      return nextLevelExists_; 
   }  
 
   /************************************************************************
    * Inserts the InstallEntity into the First Level
    *
    * @param installEntity InstallEntity object reference
    * @return void
    **/
  
   public void insertAsLevelOneInstallEntity( InstallEntity installEntity )
   {
      ArrayList levelOneArray = (ArrayList)levelArray_.get( 0 );
      EntityTreeObject   entityTreeObject = new EntityTreeObject() ;
    
      levelOneArray.add( entityTreeObject );
      entityTreeObject.setEntityReferenceAndType( installEntity,
                                       ParserGeneratorEnumType.INSTALLENTITY );    
   }
  
   /************************************************************************
    * Sets swpackage Package Info in Entity Tree Root if the params are present
    *
    * @param installEntity InstallEntity object reference
    * @return void
    **/
  
   public void checkAndSetSwpackageParamsIfPresent(InstallEntity installEntity)
         throws EntityTreeRootException
   {     
      String         pkgName = installEntity.getPackageName() ;
      PgSpecialType  pkgDescription = installEntity.getPackageDesc() ;
      String         fullPkgName = installEntity.getFullPackageName();
      PgSpecialType  pkgCopyright = installEntity.getPackageCopyright();
      String         pkgSerialNumber = installEntity.getPackageSerialNumber();
      String         pkgVendorName = installEntity.getPackageVendorName();
      String         pkgVendorTitle = installEntity.getPackageVendorTitle();
      PgSpecialType  pkgVendorDesc = installEntity.getPackageVendorDesc();

      if ( pkgName == null && pkgDescription  == null   
        && fullPkgName == null && pkgCopyright == null
        && pkgSerialNumber == null && pkgVendorName == null
        && pkgVendorTitle == null && pkgVendorDesc == null )    
      {
         return ;
      }
      else
      {
         if( SWPACKAGESET )
         {
            throw new EntityTreeRootException( "EntityTreeRootException : \n " 
               + " swpackage Package Stanza can be declared only once in the "
               + " InstallEntity. " );
         }
 
         if( pkgName != null )
            this.setPackageName( pkgName );
         if( fullPkgName != null )
            this.setFullPackageName( fullPkgName );
         if( pkgDescription != null )
            this.setPackageDescription( pkgDescription );
         if( pkgCopyright != null ) 
            this.setPackageCopyright( pkgCopyright );
         if( pkgSerialNumber != null )
            this.setPackageSerialNumber( pkgSerialNumber );
         if( pkgVendorName != null ) 
            this.setPackageVendorName( pkgVendorName );
         if( pkgVendorTitle != null ) 
            this.setPackageVendorTitle( pkgVendorTitle );
         if( pkgVendorDesc != null )
            this.setPackageVendorDesc( pkgVendorDesc );

         SWPACKAGESET = true;
      }
      
   }

   /**
    * Adds another level to the levelArray_ which represents the level in the 
    * tree structure.
    *
    * @return void
    **/
   public void addAnotherLevel()
   {
      levelArray_.add( new ArrayList() );
   }

   /**
    * Extends the tree structure after the first level/
    *
    * @param  curLevl  the current level being filled
    * @param  packageObject Package object reference
    * @exception EntityTreeRootException if one of the childrence refernce is 
    * not found 
    **/
   public void extendTreeAfterCurrentLevel(int curLevel, Package packageObject)
         throws EntityTreeRootException
   {
      ArrayList      curLevelArray = (ArrayList)levelArray_.get( curLevel );
      ListIterator   curLevelIterator;

      // For each EntityTreeObject in the current level, get all its
      // children and form the next level of the tree structure

      curLevelIterator = curLevelArray.listIterator();
      while( curLevelIterator.hasNext() )
      {
         EntityTreeObject curEntityTreeObject;
         int type;

         curEntityTreeObject = (EntityTreeObject)curLevelIterator.next();
         type = curEntityTreeObject.getType();

         if( type == ParserGeneratorEnumType.INSTALLENTITY )
         {
            InstallEntity installEntity;
            ArrayList  childInstallEntities;
            ArrayList  childFileEntities;

            installEntity =  curEntityTreeObject.getInstallEntityReference();

            // check to see if the IE has any children IE
            childInstallEntities = installEntity.getImmChildEntities();
    
            if( childInstallEntities != null )
            {
               ListIterator childEntityIterator = childInstallEntities.listIterator();
               
               while( childEntityIterator.hasNext() )
               {
                  String curIeName = (String)childEntityIterator.next();
                  InstallEntity curChildIeReference;
                  EntityTreeObject childEto;
                  
                  try
                  {
                     curChildIeReference = 
                     packageObject.searchInstallEntityFromArray( curIeName );

                     if( curChildIeReference == null )
                        throw new EntityTreeRootException( "EntityTreeRoot"
                        + "Exception :\nUnable to get a reference for child "
                        + "InstallEntity " + curIeName
                        + "\nImmChildEntities field in installEntity stanza < "
                        + installEntity.getEntityName() + " > has a invalid "
                        + "child InstallEntity < " + curIeName + " > specified "
                        + "nt information is incorrect." );
                        
                     if( !( curChildIeReference.getParent().equals( 
                                             installEntity.getEntityName() ) ) )
                     {
                        throw new EntityTreeRootException( "EntityTreeRoot"
                        + "Exception : \nParent-Child reference incorrectly "
                        + "specified . \nMake sure the parent field is "
                        + "correctly specified in the installEntity stanza for" 
                        + " < " + curIeName + " > "
                        + "\nPossible value is " + installEntity.getEntityName()
                        + "\nCurrent value is " 
                        + curChildIeReference.getParent() );
                     }
                               
                  }
                  catch( PackageException e )
                  {
                     throw new EntityTreeRootException("EntityTreeRootException"
                     + " : \n" + "Unable to form the level structure\n"
                     + e.toString() ) ;
                  }
               
                  curChildIeReference.setMarkReferenced();
                  childEto = new EntityTreeObject( curChildIeReference );
                  curEntityTreeObject.insertAsChild( childEto );     
                  childEto.setParent( curEntityTreeObject );
                  insertEntityTreeObjectAtLevel( curLevel + 1, childEto );
               }
            }

            // checkto see if the IE has any children FE

            childFileEntities = installEntity.getImmChildFiles();
            if( childFileEntities != null )
            {
               ListIterator childEntityIterator = childFileEntities.listIterator();

               while( childEntityIterator.hasNext() )
               {
                  String curFeName = (String)childEntityIterator.next();
                  FileEntity    curChildFeReference;
                  EntityTreeObject childEto;

                  curChildFeReference = 
                              packageObject.searchFileEntity( curFeName );
                  if( curChildFeReference == null )
                  {
                     throw new 
                        EntityTreeRootException( " EntityTreeRootException : \n" 
                        + " Unable to get a reference for child FileEntity "
                        + curFeName + "\nImmChildFiles field in installEntity "
                        + "stanza < " + installEntity.getEntityName() 
                        + " > has a invalid child fileEntity < " 
                        + curFeName + " > specified \n" ) ;
                  }

                  childEto = new EntityTreeObject( curChildFeReference );
                  curEntityTreeObject.insertAsChild( childEto );
                  childEto.setParent( curEntityTreeObject );
                  insertEntityTreeObjectAtLevel( curLevel + 1, childEto ); 
               }
            }    
         }
      }        
   }

   /**
     * searches for a EntityTreeObject in a particular level based on the name 
     *
     * @param int :-level number 
     * @param String :-name of the EntityTreeObject 
     * @return EntityTreeObject :- reference to the EntityTreeObject if found
     *                             else null
     **/
   public EntityTreeObject searchForEntityInLevel( int level, String name )
   {
      ArrayList requiredLevel ;
      ListIterator requiredLevelIterator;
    
      if( levelArray_ != null )
         requiredLevel =  (ArrayList)levelArray_.get( level );
      else
         return null;

      if( requiredLevel == null )
         return null;

      requiredLevelIterator = requiredLevel.listIterator();
      while(  requiredLevelIterator.hasNext() )
      {
         EntityTreeObject curEto=(EntityTreeObject)requiredLevelIterator.next();

         // if found return the ETO
         if( curEto.getName().equals( name ) )
            return curEto ;
      }

      // else return null
      return null;   
   }
  
   /**
     * marks all the ETO's in a particular level as fixed
     *
     * @param int :-level number  
     **/
   public void markEntitiesAsFixedInLevel( int level )
              throws EntityTreeRootException
   {
      ArrayList    requiredLevel ;
      ListIterator requiredLevelIterator;
    
      if( levelArray_ == null )
         throw new EntityTreeRootException( "EntityTreeRootException : "
         + "No InstallableEntities found \n"
         + "in levelArray_. Make sure you have atleast one product definied\n");

      requiredLevel =  (ArrayList)levelArray_.get( level );

      if(  requiredLevel == null )
         throw new EntityTreeRootException( "EntityTreeRootException : "
         + "Invalid level passes to markInstallableEntities \n"
         + "There are no InstallableEntities in " + level + " level \n" ) ;
      
      requiredLevelIterator = requiredLevel.listIterator();
      while( requiredLevelIterator.hasNext() )
      {
         EntityTreeObject curEto = 
                              (EntityTreeObject)requiredLevelIterator.next();

         // mark the EntityTreeObject which has a refrence to 
         // InstallEntity as being fixed
         curEto.markEntityAsFixed();
      }    
   }

   
   /**
    * checks all the ETO's in a particular level AND if fixed it adds the 
    * IE to the list of InstallableItems
    *
    * @param int :-level number  
    **/
   public void createListOfInstallableItems( int level )
              throws EntityTreeRootException
   {
      ArrayList  requiredLevel ;
      ListIterator requiredLevelIterator;
    
      if( levelArray_ == null )
         throw new EntityTreeRootException( "EntityTreeRootException : "
         + "No InstallableEntities found in levelArray_.\n"
         + "Make sure you have atleast one product definied.\n" ) ;
         
      requiredLevel =  (ArrayList)levelArray_.get( level );
      
      if(  requiredLevel == null )
         throw new EntityTreeRootException( "EntityTreeRootException : "
         + "Invalid level passes to createListOfInstallableEntities \n"
         + "There are no InstallableEntities in " + level + " level \n" ) ;
         
      requiredLevelIterator = requiredLevel.listIterator();
      while( requiredLevelIterator.hasNext() )
      {
         EntityTreeObject curEto = 
                              (EntityTreeObject)requiredLevelIterator.next();

         // Check the EntityTreeObject which has a reference to 
         // InstallEntity if it has been  fixed
         if( curEto.checkIfEntityIsFixed() )
         {
            if( curEto.getType() == ParserGeneratorEnumType.INSTALLENTITY )
            // add the InstallEntity to the list of InstallableItems
            installableItemsArray_.add( curEto.getInstallEntityReference() );
         }
      }    
   }

   /**
    * Checks all the ETO's in a particular level AND if fixed it adds 
    * the FE to the list of Repackagable Files
    * @param int :-level number  
    **/
   public void createListOfRepackagableFiles( int level )
              throws EntityTreeRootException
   {
      ArrayList  requiredLevel ;
      ListIterator requiredLevelIterator;
    
      if( levelArray_ == null )
         throw new EntityTreeRootException( "EntityTreeRootException : "
         + "No FileEntities found in levelArray_.\n"
         + "Make sure you have atleast one file definied \n" ) ;
         
      requiredLevel =  (ArrayList)levelArray_.get( level );

      if(  requiredLevel == null )
         throw new EntityTreeRootException( "EntityTreeRootException : "
         + "Invalid level passes to createListOfRepackagableFiles \n"
         + "There are no InstallableEntities in " + level + " level \n" ) ;
         
      requiredLevelIterator = requiredLevel.listIterator();
      while(  requiredLevelIterator.hasNext() )
      {
         EntityTreeObject curEto = 
                              (EntityTreeObject)requiredLevelIterator.next();

         // check the EntityTreeObject which has a reference to 
         // InstallEntity if it has been  fixed
         if( curEto.checkIfEntityIsFixed() )
         {
            if( curEto.getType() == ParserGeneratorEnumType.FILE )     
            {
               FileEntity fileEntity = curEto.getFileEntityReference();
               repackagableFilesArray_.add( fileEntity.getSourceDir() 
                        + fileEntity.getSourceFile() );
            }
         }
      }    
   }

   /**
     * getter for installableItemsArray_
     *
     * @return ArrayList :- List of Installable Items
     **/
   public ArrayList getInstallableItemsArray()
   {
      return installableItemsArray_; 
   }

   /**
     * getter for repackagableFilesArray_
     *
     * @return ArrayList :- List of Installable Items
     **/
   public ArrayList getRepackagableFilesArray()
   {
      return repackagableFilesArray_;
   }
  
   /**
     * Insert the EntityObject at the given level
     *
     * @param level the level where the entityTreeObject needs to be inserted
     * @param entityTreeObject EntityTreeObject reference
     * @return void
     **/
   private void insertEntityTreeObjectAtLevel( int level, 
      EntityTreeObject entityTreeObject )
   {
      ArrayList curArray = (ArrayList)levelArray_.get( level );
      curArray.add( entityTreeObject );
   }

   /**
     * getter for packageName_
     *
     * @return String
     **/
   public String getPackageName()
   {
      return packageName_;
   }
  
   /**
     * setter for packageName_
     *
     * @param String  packageName_
     * @return void 
     **/
   public void setPackageName( String name )
   {
      packageName_ = name;
   }

   /**
     * getter for fullPackageName_
     *
     * @return String
     **/
   public String getFullPackageName()
   {
      return fullPackageName_;
   }
  
   /**
     * setter for fullPackageName_
     *
     * @param String  fullPackageName_
     * @return void 
     **/
   public void setFullPackageName( String name )
   {
      fullPackageName_ = name;
   }
  
   /**
     * getter for packageDescription_
     *
     * @return PgSpecialType
     **/   
   public PgSpecialType getPackageDescription()
   {
      return packageDescription_;
   }

   /**
     * setter for packageDescription_
     *
     * @param desc packageDescription_
     * @return void
     **/
   public void setPackageDescription( PgSpecialType description )
   {
      packageDescription_ = new PgSpecialType( description );
   }

   /**
     * getter for packageCopyright_
     * @return PgSpecialType
     **/
   public PgSpecialType getPackageCopyright()
   {
      return packageCopyright_;
   }

   /**
     * setter for packageCopyright_
     *
     * @param copyright packageCopyright_
     * @return void
     **/
   public void setPackageCopyright( PgSpecialType copyright )
   {
      packageCopyright_ = new PgSpecialType( copyright );
   }

   /**
     * getter for packageSerialNumber_
     *
     * @return String
     **/
   public String getPackageSerialNumber()
   {
      return packageSerialNumber_;
   }

   /**
     * setter for packageSerialNumber_
     *
     * @param String packageSerialNumber_ to be set
     **/
   public void setPackageSerialNumber( String serialNum )
   {
      packageSerialNumber_ = serialNum;
   }
  
   /**
   * getter for packageVendorName_
   * @return String
   **/
   public String getPackageVendorName()
   {
      return packageVendorName_;
   }
  
   /**
     * setter for packageVendorName_
     *
     * @param String packageVendorName_
     **/
   public void setPackageVendorName( String name )
   { 
      packageVendorName_ =  name;
   }

   /**
     * getter for packageVendorTitle_
     *
     * @return String 
     **/
   public String getPackageVendorTitle()
   {
      return packageVendorTitle_;
   }

   /**
     * setter for packageVendorTitle_
     *
     * @param String packageVendorTitle_
     **/
   public void setPackageVendorTitle( String title )
   {
      packageVendorTitle_ = title;
   }
  
   /**
     * getter for packageVendorDesc_
     *
     * @return PgSpecialType
     **/
   public PgSpecialType getPackageVendorDesc()
   {
      return packageVendorDesc_;
   }

   /**
     * setter for packageVendorDesc_
     *
     * @param PgSpecialType  packageVendorDesc_
     * @return void
     **/
   public void setPackageVendorDesc( PgSpecialType desc )
   {
      packageVendorDesc_ = new PgSpecialType( desc );
   }

   /**
     * getter for levelArray_
     *
     * @return ArrayList :- levelArray_
     **/ 
   public ArrayList getLevelArray()
   {
      return levelArray_;
   }
}
