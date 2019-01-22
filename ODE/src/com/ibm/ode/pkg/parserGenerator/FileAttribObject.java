package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;

/*****************************************************************************
 * A class for representing the attributes and their of the file stanza
 * in the CMF. Any new stanza added to the CMF must be represented in
 * similar fashion as this class
 * 
 * @version 1.9 97/10/06
 * @author 	Prem Bala
 ****************************************************************************/
public class FileAttribObject extends AttributeObject
{
  
  private ArrayList attribTokenArray_;
  private ArrayList attribTypeArray_;

  public FileAttribObject( ParserGeneratorEnumType pgEnum )
  {
    /**
     * An array to hold token values for the attributes of the stanza
     * represented by this Class
     **/
     ListIterator attribTokenIterator;

     /**
      * An array to hold token values for the type of the attributes present
      * in the stanza. This has a ONE-to-ONE correspondence with the above
      * array
      **/ 
     attribTokenArray_ = new ArrayList();

     // Add tokens list for each of the attribute in the stanza represented by this object

     attribTokenArray_.add( new Integer( pgEnum.getInt( "partNum" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "fileType" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "fileDirectives" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "sourceFile" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "sourceDir" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "targetFile" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "targetDir" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "distLib" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "permissions" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "userId" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "groupId" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "majorDevNum" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "minorDevNum" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "flags" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "comp" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "shipType" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "partInfo" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "vplSecurity" )) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "vplPartqual" )) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "hfsCopyType")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "extAttr")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "hfsAlias")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "pdsAlias")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "setCode")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "entry")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "jclinMode")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "include")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "sysLibs")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "order")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "lkedTo")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "lkedRc")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "hfsLkedName")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "pdsLkedName")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "jclinLkedParms")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "lkedParms")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "libraryDD")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "lkedCond")) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "sideDeckAppendDD")) );
     
     // Construct an Array that equals the size of the attribTokenArray_
     // to hold the type of each token in the attribTokenArray_ array.
     attribTypeArray_ = new ArrayList( attribTokenArray_.size() );
     attribTokenIterator = attribTypeArray_.listIterator();
     
     while( attribTokenIterator.hasNext() )
     {
       Integer arrayValue = (Integer)attribTokenIterator.next();
       
       if( arrayValue.intValue() == pgEnum.getInt( "partNum" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "fileType" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "fileDirectives" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString")) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "sourceFile" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "sourceDir" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "targetFile" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "targetDir" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "distLib" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "permissions" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "userId" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "groupId" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "majorDevNum" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "minorDevNum" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }   
       else if( arrayValue.intValue() == pgEnum.getInt( "flags" ) )
	     {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
	     }   
       else if( arrayValue.intValue() == pgEnum.getInt( "comp" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "shipType" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfConstant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "partInfo" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfReqType") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "vplSecurity" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "vplPartqual" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "hfsCopyType" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "extAttr" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "hfsAlias" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "pdsAlias" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "setCode" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "entry" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "include" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "jclinMode" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "order" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "sysLibs" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "lkedTo" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "lkedRc" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "hfsLkedName" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "pdsLkedName" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "jclinLkedParms" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "lkedParms" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "libraryDD" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "lkedCond" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "sideDeckAppendDD" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
     }     
  } // FileAttribObject()
    
  /*****************************************************************************
   * Called by the parser which passes in a token when an attribute in a stanza
   * is encountered. This method checks whether the attribute is valid within 
   * this stanza( which it represents )  and returns back the type of the attribute
   * @param  int :- token value of the attribute
   * @return int :- token value of the type of attribute
   *                -1 if not present
   **/
  public int validateAndGetType( int token )
  {
    ListIterator attribTokenIterator = attribTokenArray_.listIterator();

    while( attribTokenIterator.hasNext() )
    {
	    Integer arrayValue = (Integer)attribTokenIterator.next();
      if( arrayValue.intValue() == token )
	    {
	      Integer typeValue = (Integer)attribTypeArray_.get( attribTokenIterator.nextIndex() - 1 );
	      return typeValue.intValue();
      }
   }
   return -1;
  }
}  



