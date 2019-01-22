package com.ibm.ode.pkg.parserGenerator;  
  
import java.util.*;
  
//LVW  FileEntity class has been changed to reflect the addition of the  
//  SourceData and PackageData stanzas.  Since the PackageData stanza is a   
//  multiply occurring stanza, a PackageData object has been created and the  
//  attributes of the stanza have been moved to that object.  A packageData   
//  array to contain the multiple stanzas is instantiated here.  Since the   
//  SourceData stanza occurs only once, its attributes are still contained here.  
  
public class FileEntity  
             implements SecondLevelStanza  
{  
  
// The following attributes are under the SourceData stanza  
  
  private String sourceDir_;  
  private String sourceFile_;  
  
// The following attributes belong to the multiple stanza category.  Separate classes are drawn and here they are represented as objects of those classes.  
  
  private ArrayList packageData_;  
  
  
  public String toString()  
  {  
    if (sourceDir_ != null)  
      if (sourceFile_ != null)  
        return (Package.checkForFileSeparator( sourceDir_ + sourceFile_ ));  
      else  
        return (sourceDir_);  
    else  
      if (sourceFile_ != null)  
        return (Package.checkForFileSeparator( sourceFile_ ));  
  
    return new String();  
  }  
  
  public int hashCode()  
  {      
    return toString().hashCode(); 
  }  
  
  public boolean equals( Object obj )  
  {  
    return ( this.toString().equals( obj.toString() ));
  }  
  
//constructor  
  
  public FileEntity()  
  {  
  //do initialization here  
  packageData_ = new ArrayList();  
  }  
  
  public void populate(int parentToken, int stanzaToken,  
		       int attribToken, int type, String value)  
              throws PackageException  
  {  
    String   modifiedDirectory;  
  
    if (parentToken == ParserGeneratorEnumType.FILE)  
        
      switch(stanzaToken)  
      {  
        //multiple times occurring stanzas - within FE here          
        case ParserGeneratorEnumType.PACKAGEDATA	:  
          PackageData curPackageData = (PackageData)packageData_.get( packageData_.size()-1 );   
          switch(attribToken)  
          {  
            //list out cases for all attributes of packageData stanza  
            //which occur directly within Stanza as strings/constants  
            case ParserGeneratorEnumType.TARGETDIR    :  
            case ParserGeneratorEnumType.DISTLIB      :  
              modifiedDirectory = Package.checkForFileSeparator(value);  
              curPackageData.setTargetDir(modifiedDirectory);  
              break;  
            case ParserGeneratorEnumType.TARGETFILE	:  
              curPackageData.setTargetFile(value);  
              break;  
            case ParserGeneratorEnumType.PERMISSIONS:  
              curPackageData.setPermissions(value);  
              break;  
            case ParserGeneratorEnumType.USERID	:  
              curPackageData.setUserId(value);  
              break;  
            case ParserGeneratorEnumType.GROUPID	:  
              curPackageData.setGroupId(value);  
              break;  
            case ParserGeneratorEnumType.MAJORDEVNUM:  
              curPackageData.setMajorDevNum(value);  
              break;  
            case ParserGeneratorEnumType.MINORDEVNUM	:  
              curPackageData.setMinorDevNum(value);  
              break;  
            case ParserGeneratorEnumType.FLAGS        :  
              curPackageData.setFlags(value);  
              break;
            case ParserGeneratorEnumType.PARTNUM	:  
              curPackageData.setPartNum(value);  
              break;  
            case ParserGeneratorEnumType.FILETYPE	:  
              curPackageData.setFileType(value);  
              break;  
            case ParserGeneratorEnumType.COMP		:  
              curPackageData.setComp(value);  
              break;  
            case ParserGeneratorEnumType.VPLSECURITY	:  
              curPackageData.setVplSecurity(value);  
              break;  
            case ParserGeneratorEnumType.VPLPARTQUAL	:  
              curPackageData.setVplPartqual(value);  
              break;  
            case ParserGeneratorEnumType.HFSCOPYTYPE :  
              curPackageData.setHfsCopyType(value);  
              break;  
            case ParserGeneratorEnumType.EXTATTR :  
              curPackageData.setExtAttr(value);  
              break;  
            case ParserGeneratorEnumType.SETCODE :  
              curPackageData.setSetCode(value);  
              break;  
            case ParserGeneratorEnumType.ENTRY :  
              curPackageData.setEntry(value);  
              break;
            case ParserGeneratorEnumType.JCLINMODE :  
              curPackageData.setJclinMode(value);  
              break;
            case ParserGeneratorEnumType.ORDER :  
              curPackageData.setOrder(value);  
              break;
            case ParserGeneratorEnumType.LKEDTO :  
              curPackageData.setLkedTo(value);  
              break;
            case ParserGeneratorEnumType.LKEDRC :  
              curPackageData.setLkedRc(value);  
              break;
            case ParserGeneratorEnumType.HFSLKEDNAME :  
              curPackageData.setHfsLkedName(value);  
              break;  
            case ParserGeneratorEnumType.PDSLKEDNAME :  
              curPackageData.setPdsLkedName(value);  
              break;
            case ParserGeneratorEnumType.JCLINLKEDPARMS :  
              curPackageData.setJclinLkedParms(value);  
              break;
            case ParserGeneratorEnumType.LKEDPARMS :  
              curPackageData.setLkedParms(value);  
              break;
            case ParserGeneratorEnumType.LIBRARYDD :  
              curPackageData.setLibraryDD(value);
              break;
            case ParserGeneratorEnumType.LKEDCOND :  
              curPackageData.setLkedCond(value);  
              break;
            case ParserGeneratorEnumType.SIDEDECKAPPENDDD :  
              curPackageData.setSideDeckAppendDD(value);  
              break;
	  }//end switch attribtoken  
          break;      
  
        //zero or one time occurring stanzas - within FE here  
        case ParserGeneratorEnumType.SOURCEDATA		:  
          switch(attribToken)  
          {  
            //list out cases for all attributes of sourceData stanza  
            //which occur directly within Stanza as strings/constants  
  
            case ParserGeneratorEnumType.SOURCEDIR	:  
              modifiedDirectory = Package.checkForFileSeparator( value );  
              setSourceDir(modifiedDirectory);  
              break;    
            case ParserGeneratorEnumType.SOURCEFILE  	:  
              setSourceFile(value);  
              break;  
            default :  
              throw new PackageException("PackageData:  Invalid Current "
                            + "Stanza token passed to populate method.\n\n");  
	  }//end switch on attribToken  
      }//end switch stanzaToken  
  }//end populate String  
  
  
//----------  
//----------  
  
  public void populate(int parentToken, int stanzaToken,  
		       int attribToken, int type, ReqType value)  
    throws PackageException  
  {  
    if (parentToken == ParserGeneratorEnumType.FILE)  
        
	switch(stanzaToken)  
	{  
	    //multiple times occurring stanzas - within FE here  
	      
	    //currently, no stanza occurring multiple times has an  
	    //attribute of type ReqType - else, corr. code comes here.  
  
	    //zero or one time occurring stanzas - within FE here  
  
	    //currently, no stanza occurring zero or one times has an  
	    //attribute of type ReqType - else, corr. code comes here.  
	    //note - List of ReqType attribs are present, but they  
	    //are categorized as Array of objects and appear in the  
	    //List section of populate method elsewhere in this class  
  
	  default :  
	    throw new PackageException("FileEntity: Invalid Current "  
			  + "Stanza token passed to populate method.\n\n");  
	      
	}//end switch on stanzaToken  
           
  }//end populate ReqType  
  
  public void populate(int parentToken, int stanzaToken, 
                       int attribToken, int type, ArrayList value)  
              throws PackageException  
  {  
    if (parentToken == ParserGeneratorEnumType.FILE)  
      switch(stanzaToken)  
      {  
        //multiple times occurring stanzas within FE here  
        case ParserGeneratorEnumType.PACKAGEDATA  :  
  
          PackageData curPackageData = (PackageData)packageData_.get(packageData_.size()-1);  
          switch(attribToken)  
          {  
	    //list out cases for all attributes of package data  
            //stanza which occur directly within packageData as arrays(lists)  
  
	    case ParserGeneratorEnumType.SHIPTYPE    :  
              curPackageData.setShipType(value);  
              break;  
            case ParserGeneratorEnumType.PARTINFO    :  
              curPackageData.setPartInfo(value);  
              break;  
            case ParserGeneratorEnumType.INCLUDE     :  
              curPackageData.setInclude(value);  
              break;  
            case ParserGeneratorEnumType.HFSALIAS    :  
              curPackageData.setHfsAlias(value);  
              break;  
            case ParserGeneratorEnumType.PDSALIAS    :  
              curPackageData.setPdsAlias(value);  
              break;  
            case ParserGeneratorEnumType.SYSLIBS     :  
              curPackageData.setSysLibs(value);  
              break;  
            case ParserGeneratorEnumType.FILEDIRECTIVES  :  
              curPackageData.setFileDirectives(value);  
              break;
	    case ParserGeneratorEnumType.SYSLIBS_LIBRARYDD :
              curPackageData.setSysLibsLibraryDD(value);
              break;  
            }//end switch on attribToken  
          break;  
        default :  
          throw new PackageException("FileEntity: Invalid Current " 
                             + "Stanza token passed to populate method.\n\n");  
      }//end switch on stanzaToken  
  }//end populate array  
  
//get and set functions for the packageData_   
  public void setPackageData(ArrayList packageData)  
  {  
    packageData_ = new ArrayList(packageData) ;  
  }  
  
  public ArrayList getPackageData()  
  {  
   return packageData_;  
  }  
	       
//get and set functions for sourceFile_ attribute  
  public void setSourceFile(String sourceFile)  
  {  
    sourceFile_ = sourceFile;  
    if (sourceFile_ != null)  
      sourceFile_ = sourceFile_.trim();  
  }  
   
  public String getSourceFile()  
  {  
    return sourceFile_;  
  }  
    
  //get and set functions for sourceDir_ attribute  
  public void setSourceDir(String sourceDir)  
  {  
    sourceDir_ = sourceDir;  
    if (sourceDir_ != null)  
      sourceDir_ = sourceDir_.trim();  
  }  
    
  public String getSourceDir()  
  {  
    return sourceDir_;  
  }  
  
// Methods needed by the Package object for populating the FileEntity  
// and the classes it contains  
// implements the interface SecondLevelStanza  
  
  public void constructChildStanza( int stanzaToken ) throws PackageException  
  {  
    switch( stanzaToken )  
    {          
      case  ParserGeneratorEnumType.PACKAGEDATA :  
	      packageData_.add( new PackageData() );  
	      break;  
      default :  
	      throw new PackageException("FileEntity : "   
				      + "Invalid Stanza Name passed to "  
				      + "FileEntity.constructChildStanza() "  
				    );  
    }//end switch  
  }//end method constructChildStanza  
}//end class  
