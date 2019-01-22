//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//*****************************************************************************
    
package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;

/*****************************************************************************
 * This class holds the reference to the InstallEntity or FileEntity and 
 * has information of all its children entities in the Tree Structure
 * created.  This obect will represent each node of a tree structure.
 * @version	1.4 97/04/29
 * @author 	Prem Bala
 ****************************************************************************/

public class EntityTreeObject extends java.lang.Object
{
  // define name and a parent reference for the ETO     
  private String            name_;
  private EntityTreeObject  parent_;
  
  private InstallEntity installEntityReference_;
  private FileEntity    fileEntityReference_;
  private ArrayList     childReferenceArray_;
  private ArrayList     spChildReferenceArray_;

  private int   type_;
  private boolean fixedFlag_;

  /***************************************************************************
   * Constructor for the EntityTreeObject with no arguments
   **/
  public EntityTreeObject()
  {
    childReferenceArray_ = new ArrayList();
    spChildReferenceArray_ = new ArrayList();
    installEntityReference_ = null;
    fileEntityReference_    = null;    
    type_ = 0;
    fixedFlag_ = false;
  }
  
  /*******************************************************************************
   * Constructor for the EntityTreeObject with installEntity reference being passed
   * @param installEntity reference to the InstallEntity object
   **/ 
  public EntityTreeObject( InstallEntity installEntity )
  {
    childReferenceArray_ = new ArrayList();
    spChildReferenceArray_ = new ArrayList();
    installEntityReference_ = installEntity;
    fileEntityReference_    = null;        
    type_ = ParserGeneratorEnumType.INSTALLENTITY;
    fixedFlag_ = false;
    setName( installEntityReference_.getEntityName() );
  }
  
  /*********************************************************************************
   * Constructor for the EntityTreeObject with fileEntity reference being passes
   * @param fileEntity reference to the FileEntity object
   **/
  public EntityTreeObject( FileEntity fileEntity )
  {
    String  keyValue = null;
    String sourceDir;
    String sourceFile;
    
    childReferenceArray_ = new ArrayList();
    spChildReferenceArray_ = new ArrayList();
    installEntityReference_ = null;
    fileEntityReference_    = fileEntity;    
    type_ = ParserGeneratorEnumType.FILE;
    fixedFlag_ = false;
    
    sourceDir = fileEntityReference_.getSourceDir();
    sourceFile =  fileEntityReference_.getSourceFile();
    
    if( sourceDir != null )	
    {
	    if( sourceFile!= null )
        keyValue = sourceDir + sourceFile;
      else
        keyValue =  sourceDir;
    }
    else if( sourceDir == null )
    {
      keyValue = sourceFile;      	     	     
    }	
    setName( keyValue ); 
  }
  
  /*********************************************************************************
   * setter for name_
   * @params String :- keyValue name of the entity
   **/
  public void setName( String name )
  {
    if( name != null )
      name_ = name.trim();
  }
  
  /*********************************************************************************
   * getter for name_
   * @return String :- keyValue name of the entity
   **/
  public String getName()
  {
    return name_;
  }
  
  /*********************************************************************************
   * setter for parent_
   * @param  EntityTreeObject :- Parent EntityTreeObject
   **/
  void setParent( EntityTreeObject parent )
  {  
    parent_ = parent;
  }
  
  /*********************************************************************************
   * getter for parent
   * @return EntityTreeObject :- Reference to the parent EntityTreeObject
   **/
  EntityTreeObject getParent()
  {
    return parent_;
  }
  
  
  /*********************************************************************************
   * getter for childReferenceArray_
   * @return Array
   **/
  public ArrayList getChildReferenceArray()
  {
    return childReferenceArray_;
  }
  
  /*********************************************************************************
   * getter for spChildReferenceArray_
   * @return Array
   **/
  public ArrayList getSpChildReferenceArray()
  {
    return spChildReferenceArray_;
  }
  /*********************************************************************************
   * getter for type_
   * @return int
   **/
  public int getType()
  {
    return type_;
  }

  /*********************************************************************************
   * getter for installEntityReference_
   * @return InstallEntity
   **/
  public InstallEntity getInstallEntityReference()
  {
    return installEntityReference_;
  }
  
  /*********************************************************************************
   * getter for fileEntityReference_
   * @return fileEntityReference_
   **/
  public FileEntity getFileEntityReference()
  {
    return fileEntityReference_;   
  }
  
  /*********************************************************************************
   * sets the Entity Refernce to installEntity and sets the type of this Entity
   * @param installEntity InstallEntity object reference
   * @param type type of this EntityRefernce
   * @return void
   **/
  public void setEntityReferenceAndType( InstallEntity installEntity, int type )
  {
    type_ = type;
    installEntityReference_ = installEntity;
  }

  /*********************************************************************************
   * sets the Entity Refernce to fileEntity and sets the type of this Entity
   * @param fileEntity FileEntity object reference
   * @param type type of this EntityRefernce
   * @return void
   **/ 
  public void setEntityReferenceAndType( FileEntity fileEntity, int type )
  {
    type_ = type;
    fileEntityReference_ = fileEntity;
  }
  
  /*********************************************************************************
   * inserts the child EntityTreeObject in to the childReferenceArray_
   * @param entityTreeObject EntityTreeObject reference
   * @return void
   **/
  public void insertAsChild( EntityTreeObject entityTreeObject )
  {
    childReferenceArray_.add( entityTreeObject );
  }
  /*********************************************************************************
   * inserts the child EntityTreeObject in to the spChildReferenceArray_ for Service Packaging
   * @param entityTreeObject EntityTreeObject reference
   * @return void
   **/
  public void insertIntoSpAsChild( EntityTreeObject childEto )
  {
    if( ! spChildReferenceArray_.contains( childEto ) )
      spChildReferenceArray_.add( childEto );
  }
  
  /*********************************************************************************
   * marks the Entity as being fixed  in the current service package run
   * @return void
   **/
  public void markEntityAsFixed()
  {
    fixedFlag_ = true;
  }
  
  /*********************************************************************************
   * checks if the Entity is being fixed  in the current service package run
   * @return boolean :-  true if fixed
   *                     false if not being fixed
   **/
  public boolean checkIfEntityIsFixed()
  {
    return fixedFlag_;
  }
  /*********************************************************************************
   * marks the Parent Entity as being fixed  in the current service package run
   * @return void
   **/ 
  public void markParentAsFixed()
  {
    if( parent_ != null )
      parent_.markEntityAsFixed();
  }  

  /*********************************************************************************
   * checks whether  the Parent Entity is being fixed  in the current service package run
   * @return boolean :- true if parent is fixed                       
   **/ 
  public boolean checkIfParentIsFixed()
  {
    if( parent_ != null )
      {
	if( parent_.checkIfEntityIsFixed()  )
	  return true;
      }
    return false;
  }  
  
  /*********************************************************************************
   * adds this ETO as being fixed to the spChildRefernceArray of the parent
   * @param none
   **/ 
  
  void addToCurrentlyFixedEntityInParent()
  {  
    parent_.insertIntoSpAsChild( this );
  }
  
}
  

