package com.ibm.ode.pkg.parserGenerator;

import java.util.*;

/**
 * This class is the storage class for all the objects whic hold info about the data
 * in the CMF. It contains the  reference to the InstallEntities and the FileEntities
 *  which  act as storage classes. It also acts as the gateway for instantiating and
 *  and populating the InstallEntity and it's subclasses and the FileEntity.  It
 *  provides methods to search for a particular InstallEntity or FileEntity based on 
 * a key value.
 * @version     1.16 98/01/22
 * @author      Prem Bala
 **/
public class Package
{
  static final private int FILETYPE          = 1;
  static final private int INSTALLENTITYTYPE = 2;
  private int     objType_;
  private Hashtable   installEntityHashtable_;
  private Hashtable   fileEntities_;
  
  /**
   * These two repesent the top level objects which are represented
   * in CMF as the top-level stanza InstallEntity and file. The
   * Package holds the refernce to the current entities being filled.
   **/
  private InstallEntity tempInstallEntity_;
  private FileEntity    tempFileEntity_;

  /*****************************************************************************
   * Constructor for Package
   **/
  public Package()			
  {				     
     objType_ = ParserGeneratorEnumType.NULL;				
     installEntityHashtable_ = new Hashtable();	
     fileEntities_ = new Hashtable();     
  }					
  
  
  /*****************************************************************************
   * sets the type of Object which is to be  populated
   * @param int :- flagType which indicates whether the current stanza is IE or FE
   * @return void
   **/
  private void setObjType( int flagType )	
  {				       
    objType_ = flagType;			
  }					

  /*****************************************************************************
   * This is called as soon as the parser encounters a openBrace in CMF. It will
   * instantiate ( if neccessary the appropriate object based on the ParentStanzaToken
   * and StanzaToken. It also sets the objType ( IE or FE )
   * @param int :- curParentStanzaToken the token of the parent stanza 
   * @param int :- curStanzaToken  the name of the stanza encountered
   * @return void 
   * @exception  PackageException :- If an invalid token is passed
   **/
  public void openBrace( int curParentStanzaToken, 
			 int curStanzaToken ) 
               throws PackageException
  {
    if( objType_ == ParserGeneratorEnumType.NULL )
      {
	if( curParentStanzaToken == ParserGeneratorEnumType.INSTALLENTITY 
	    && curStanzaToken == ParserGeneratorEnumType.NULL ) 
	  {
	    tempInstallEntity_ = new InstallEntity();
            setObjType(  ParserGeneratorEnumType.INSTALLENTITY );
	  }
	else if( curParentStanzaToken == ParserGeneratorEnumType.FILE
		 && curStanzaToken == ParserGeneratorEnumType.NULL )
	  {
	    tempFileEntity_ = new FileEntity();
	    setObjType( ParserGeneratorEnumType.FILE );				  
	  }
	else
	  {
	    throw new PackageException("PackageException : \n"
				       + " An invalid parent stanza has been encountered: "
				       + " Package unable to create the object" );
	  }
      }
    else if( objType_ == ParserGeneratorEnumType.INSTALLENTITY )
      {
	switch( curParentStanzaToken )
	  {
	     // Handle the top-level Entity here
	   case  ParserGeneratorEnumType.INSTALLENTITY :
	     switch( curStanzaToken ) 

	       {  
		 // Handle all single occurrence stanza here whose
		 // parent stanza is InstallEntity
	       case ParserGeneratorEnumType.ENTITYINFO        :
	       case ParserGeneratorEnumType.LINKINFO          :
	       case ParserGeneratorEnumType.VENDORINFO        :
	       case ParserGeneratorEnumType.ARCHITECTUREINFO  :
	       case ParserGeneratorEnumType.SUPPORTINFO       :
	       case ParserGeneratorEnumType.INSTALLSTATESINFO :
	       case ParserGeneratorEnumType.PATHINFO          :
	       case ParserGeneratorEnumType.REQUISITESINFO    : 
	       case ParserGeneratorEnumType.MVSINFO           :
	       case ParserGeneratorEnumType.VPLINFO           :
	       case ParserGeneratorEnumType.SERVICEINFO       :
	       case ParserGeneratorEnumType.PACKAGEINFO       :  return;
		 
		 // Handle all multiple occurring stanzas here
		 // whose parent stanza is InstallEntity
	       case ParserGeneratorEnumType.ENTITYSUBSETINFO  :	   
		 tempInstallEntity_.constructChildStanza( curStanzaToken );
		 break;
	       default                                        :
		 throw new PackageException( "PackageException : \n "
					     + "Invalid stanza name passed to the Package Object's "
					     + " openBrace method. " );
	      }
	     break;
	}
    } 

//LVW Add File as a parent stanza including PackageData and SourceData
    else if (objType_ == ParserGeneratorEnumType.FILE)
	 {
	   switch( curParentStanzaToken )
	   {
	       case ParserGeneratorEnumType.FILE  :
                 switch(curStanzaToken)

	   { //Handle all single occurrence stanza here whose
             //parent stanza is File

	       case ParserGeneratorEnumType.SOURCEDATA         :  return;

             //Handle all multiple occurring stanzas here
             //whose parent stanza is File

	       case ParserGeneratorEnumType.PACKAGEDATA         :
                 tempFileEntity_.constructChildStanza (curStanzaToken );
                 break;
	   



                                
	     /*************************************************************************************
	       This is currently just an example to show how to handle stanzas containing
	       stanzas in the CMF. Since support for OS2 is removed this below shown example is 
	       not included in the code 
	       // Handle all stanzas which contain stanzas here
	       case ParserGeneratorEnumType.MEDIASETINFO  :     //BP-F1004
	       switch( curStanzaToken )
	       {	       
	       case  ParserGeneratorEnumType.MEDIAINFO             :
	       // BP-F1004 End
	       tempInstallEntity_.constructChildrenOfChildStanza( curParentStanzaToken,
	                                                          curStanzaToken );
	        break;
	        default                                          :
		    throw new PackageException("PackageException : \n " 
			         	       + " Invalid stanza name passed to the Package Object's "
					       + " openBrace method. " );
	         }
	         break;
		 ****************************************************************************************/
	  default                                     :
	    throw new PackageException("PackageException : \n" 
				       + "Invalid stanza name passed to the Package Object's "
				       + " openBrace method. The stanzaName within File"
				       + "is invalid ");
		 }
            break;
	   }
    }
  
   else
     {
       throw new PackageException( "PackageException : \n" 
				   +  " Invalid stanza name passed to the Package Object's "
				   + " openBrace method. " );
     }	    
  }
  
  
  /*****************************************************************************
   * This method is called as soon as a close Brace is encountered in the CMF.
   * It resets the reference of the tempInstallEntity ( or tempFIleEntity )
   * @param int :- curParentStanzaToken the token of the parent stanza 
   * @param int :- curStanzaToken  the name of the stanza that was populated
   * @return void 
   * @exception  PackageException :- If an invalid token is passed
   **/
  public void closeBrace( int curParentStanzaToken, 
			  int curStanzaToken )
              throws PackageException
  {
    if( objType_ == ParserGeneratorEnumType.INSTALLENTITY )
      {
	switch( curParentStanzaToken )
	  {
	  case  ParserGeneratorEnumType.INSTALLENTITY :
	      switch( curStanzaToken )
		{
		case ParserGeneratorEnumType.NULL       :
		  objType_ = ParserGeneratorEnumType.NULL;
		String keyValue = (String)tempInstallEntity_.getEntityName();
		if( keyValue == null )
     		 {
             	 throw new
         	PackageException( "PackageException : \n "
         	 + "entityName is not specified for "
         	 + " the current InstallEntity. This is"
         	 + " a required field in the CMF. \n" );

	      }
		if(installEntityHashtable_.containsKey(keyValue)){
		throw new
         PackageException("PackageException : \n "
          + " Duplicate Entities found in "
          + " list of InstallEntities."
          +  keyValue + " InstallEntity has"
          + " occured multiple times " );	
		}
		else
	installEntityHashtable_.put(tempInstallEntity_.getEntityName(),tempInstallEntity_);

	if( (ArrayList)tempInstallEntity_.getImmChildEntities() == null
        && (ArrayList)tempInstallEntity_.getImmChildFiles() == null )
      {
        throw new
 	PackageException("PackageException : \n "         
	+ " The link information for  the "
          + "InstallEntity " + keyValue
          + " is not specified "+ " The InstallEntity must have"
          + " a child InstallEntity or a childFile " );
      }
	
		  tempInstallEntity_ = null;
		  break;
		default :
		  return;
		}
	      break;
	  default  :
	    return;
	    
	  }
      }
    else if( objType_ == ParserGeneratorEnumType.FILE )
    {
      //LVW add case for FILE as ParentToken
      switch( curParentStanzaToken )
      {
        case ParserGeneratorEnumType.FILE          :
          switch( curStanzaToken )
          {
            case ParserGeneratorEnumType.NULL      :
              objType_ = ParserGeneratorEnumType.NULL;
	String 	 keyValue = (String)tempFileEntity_.getSourceDir() 
 	+	 (String)tempFileEntity_.getSourceFile() ;

            if( (String)tempFileEntity_.getSourceDir() == null
              && (String)tempFileEntity_.getSourceFile() == null )
            {
               throw new 
                    PackageException("EntitySynthesizerException : \n "
                                               + "sourceDir + sourceFile is not specified for \n "
                                               + " the current FileEntity. This is \n "
                                               + " a required field in the CMF. At least \n "
                                               + " sourceDir or sourceFile must be specified \n"
                                              );
            }
	if(fileEntities_.containsKey(tempFileEntity_))
            {
        throw new 
                PackageException("EntitySynthesizerException : \n" 
                                    + " Duplicate Entities found in "
                                    + "list of FileEntities."
            + keyValue + " FileEntity has"
            + " occured multiple times " );                  
            }
		else { 
		
              fileEntities_.put( tempFileEntity_, tempFileEntity_ );
		}
	            tempFileEntity_ = null;
	            break;
	          default  :
              return;
	        }
          break;
	      default   :
          return;
	    }
    }
    else 
      throw new PackageException("PackageException : \n"  
				 + " Invalid call to the Package.closeBrace() "
				 + " method. No stanza is currently being populated." );
  }

  public void populate( int curParentStanzaToken, int curStanzaToken ,
			int curAttribToken, int type, String value ) 
               throws PackageException
  {
	switch( curParentStanzaToken )
	  {
	       case ParserGeneratorEnumType.FILE		:
		 if (tempFileEntity_ == null)
		 {
		   throw new PackageException("Package: Invalid call " +
		     "to method populate-Expecting File attribute\n\n");
		 }

		 tempFileEntity_.populate(curParentStanzaToken, 
                        curStanzaToken , curAttribToken, type, value);

		 break;
	       case ParserGeneratorEnumType.INSTALLENTITY	:
		 if (tempInstallEntity_ == null)
		 {
		   throw new PackageException("Package: Invalid call " +
		     "to method populate-Expecting InstallEntity " +
		     "attribute\n\n");
		 }

		 tempInstallEntity_.populate( curParentStanzaToken, 
			curStanzaToken , curAttribToken, type, value);
		 break;
	       default                                          :
		 throw new PackageException("Invalid stanza name passed" +
			" to the Package Object's populate method. " );
	}//end switch
  }

//-------------------------

  public void populate( int curParentStanzaToken, int curStanzaToken ,
			int curAttribToken, int type, ReqType value)
               throws PackageException
  {
	switch( curParentStanzaToken )
	  {
	       case ParserGeneratorEnumType.FILE	:
		 if (tempFileEntity_ == null)
		 {
		   throw new PackageException("Package: Invalid call " +
		     "to method populate-Expecting File attribute\n\n");
		 }

		 tempFileEntity_.populate( curParentStanzaToken, 
                                           curStanzaToken , curAttribToken, type, value);

		 break;	  
	       case ParserGeneratorEnumType.INSTALLENTITY	:
		 if (tempInstallEntity_ == null)
		 {
		   throw new PackageException("Package: Invalid call " +
		     "to method populate-Expecting InstallEntity " +
		     "attribute\n\n");
		 }

		 tempInstallEntity_.populate( curParentStanzaToken, 
			                      curStanzaToken , curAttribToken, type, value);
		 break;
	       default                                          :
		 throw new PackageException("Invalid stanza name passed" +
			" to the Package Object's populate method. " );
	}//end switch
  }

//-------------------------


  public void populate( int curParentStanzaToken, int curStanzaToken ,
			int curAttribToken, int type, ArrayList value ) 
               throws PackageException
  {
	switch( curParentStanzaToken )
	  {
	       case ParserGeneratorEnumType.FILE		:
		 if (tempFileEntity_ == null)
		 {
		   throw new PackageException("Package: Invalid call " +
		     "to method populate-Expecting File attribute\n\n");
		 }

		 tempFileEntity_.populate( curParentStanzaToken, 
                                           curStanzaToken , curAttribToken, type, value);

		 break;
	       case ParserGeneratorEnumType.INSTALLENTITY	:
		 if (tempInstallEntity_ == null)
		 {
		   throw new PackageException("Package: Invalid call " +
		     "to method populate-Expecting InstallEntity " +
		     "attribute\n\n");
		 }

		 tempInstallEntity_.populate( curParentStanzaToken, 
			                      curStanzaToken , curAttribToken, type, value);
		 break;
	       default                                          :
		 throw new PackageException("Invalid stanza name passed" +
			" to the Package Object's populate method. " );
	}//end switch
  }

//-------------------------


  /************************************************************************
   * getter for installEntityArray_
   * @return Array 
   **/  
  public Hashtable  getInstallEntityArray()
  {
    return installEntityHashtable_;
  }

  /************************************************************************
   * getter for fileEntityArray_
   * @return Hashtable
   **/  
  public Hashtable getFileEntities()
  {
    return fileEntities_;
  }
  
  /************************************************************************
   * This method searches the installEntityArray_ for an IE based on it's entityName
   * This method sets a flag value that a IE has been referenced
   * @param String :- entityName of the InstallEntity
   * @return InstallEntity :- the installEntity corresponding to the entityName 
   *                       :-  null if it is not found
   * @exception PackageException :- if the installEntity has already been referenced
   **/  
  public InstallEntity searchInstallEntityFromArray( String ieName )
                       throws PackageException
  {
	Enumeration InstallEntityEnum;

	for (InstallEntityEnum = installEntityHashtable_.elements();
	    InstallEntityEnum.hasMoreElements();)
      {
	InstallEntity curInstallEntity = (InstallEntity)InstallEntityEnum.nextElement();
	String entityName = curInstallEntity.getEntityName();
	
	if( entityName != null )
	  {
	    if( entityName.trim().equals( ieName.trim() ) )
	      {
		if( !curInstallEntity.getMarkReferenced() )
		  return curInstallEntity;
		else
		  throw new PackageException( "PackageException : \n "
					      + "The current InstallEntity "
					      + " " + entityName + "  has been already referenced:"
					      + "Error in forming the Package heirarchy."
					      );			 
	      }
	  }
      }
       
    // if it is not found just return null
    return null;	 
  }

  /************************************************************************
   * Thos method checks if a / or \ is present at the end of the directory Name
   * Depending on it it appends the directoryName with \ or /  appropriately
   * @param dirName :- the director Name 
   * @return String :- directoryName with \ or / at the end
   **/
  static public String checkForFileSeparator( String dirName )
  {
    if( dirName != null )
    {
      if( dirName.endsWith( ParserGeneratorInitiator._fileSeparator_ ) )
        return dirName;
      else
        return dirName.concat( ParserGeneratorInitiator._fileSeparator_ ); 
    }
    else
      return dirName;
  }

  /************************************************************************
   * This method searches the fileEntities_ for an FE based on it's keyValue
   * The keyValue for a FE is sourceDir + sourceFile
   * @param String :- entityName of the InstallEntity
   * @return  FileEntity :- the FileEntity corresponding to the keyValue 
   *                       :-  null if it is not found
   **/  
  public FileEntity searchFileEntity( String feName )                                  
  {
    FileEntity tempFileEntity = new FileEntity();

    // We need to add the additional "/" since the key value has the trailing "/".
    // We don't know whether `feName' is a directory or not, so we add the "/" to
    // make the search fast.

    // We only need to use sourceDir in tempFileEntity, for both dirs and files, 
    // since both will always end with "/", and therefore the resulting hash 
    // code will be independent of the file type.

    tempFileEntity.setSourceDir( checkForFileSeparator( feName.trim() ) );

    return (FileEntity) fileEntities_.get( tempFileEntity );

  }

}




