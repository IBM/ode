//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 2000.  All Rights Reserved.
//*
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;

/*****************************************************************************
 * A class for representing the attributes and their of the file stanza
 * in the CMF. Any new stanza added to the CMF must be represented in
 * similar fashion as this class
 *
 * @author 	Prem Bala
 ****************************************************************************/
public class EntityInfoAttribObject extends AttributeObject
{
  /**
   * An array to hold token values for the attributes of the stanza
   * represented by this Class
   **/
  private ArrayList attribTokenArray_;
  /**
   * An array to hold token values for the type of the attributes present
   * in the stanza. This has a ONE-to-ONE correspondence with the above
   * array
   **/
  private ArrayList attribTypeArray_;

  public EntityInfoAttribObject( ParserGeneratorEnumType pgEnum )
  {			  
     ListIterator attribTokenIterator;

     attribTokenArray_ = new ArrayList();

     // Add tokens list for each of the attribute in the stanza represented
     // by this object

     attribTokenArray_.add( new Integer( pgEnum.getInt( "entityName" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "fullEntityName" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "entityId" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "description" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "imageName" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "version" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "release" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "distribution" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "serialNumber" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "vendorName" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "maintLevel" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "fixLevel" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "versionDate" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "hiddenState" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "category" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "categoryTitle" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "copyright" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "copyrightKeys" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "copyrightMap" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "copyrightFlags" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "language" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "content" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "insList" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "changeLog" ) ) );

     // Construct an Array that equals the size of the attribTokenArray_
     attribTypeArray_ = new ArrayList( attribTokenArray_.size() );
     attribTokenIterator = attribTokenArray_.listIterator();

     while( attribTokenIterator.hasNext() )
     {
       Integer arrayValue = (Integer)attribTokenIterator.next();
	 
	     if( arrayValue.intValue() == pgEnum.getInt( "entityName" ) )
       {
	       attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
	     }
	     else if( arrayValue.intValue() == pgEnum.getInt( "fullEntityName" ) )
	     {
	      attribTypeArray_.add(new Integer(pgEnum.getInt("ListOfString")));
	     }
	     else if( arrayValue.intValue() == pgEnum.getInt( "entityId" ) )
	     {
	       attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
	     }
	     else if( arrayValue.intValue() == pgEnum.getInt( "description" ) )
	     {
	       attribTypeArray_.add(new Integer(pgEnum.getInt("PGSpecialType")));
	     }
	     else if( arrayValue.intValue() == pgEnum.getInt( "imageName" ) )
	     {
	       attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
	     }
	     else if( arrayValue.intValue() == pgEnum.getInt( "version" ) )
	     {
	       attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
	     }
	     else if( arrayValue.intValue() == pgEnum.getInt( "release" ) )
	     {
	      attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
	     }
	     else if( arrayValue.intValue() == pgEnum.getInt( "maintLevel" ) )
	     {
	       attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
	     }
	     else if( arrayValue.intValue() == pgEnum.getInt( "fixLevel" ) )
	     {
	       attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
	     }
       else if( arrayValue.intValue() == pgEnum.getInt( "vendorName" ))
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "serialNumber" ))
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "distribution" ))
       { 
         attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "versionDate" ) )
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "hiddenState" ) )
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "category" ) )
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "categoryTitle" ) )
	     {
	       attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "copyright" ) )
	     {
	       attribTypeArray_.add(new Integer(pgEnum.getInt("PGSpecialType")));
	     }
       else if( arrayValue.intValue() == pgEnum.getInt( "changeLog" ) )
       {
         attribTypeArray_.add(new Integer(pgEnum.getInt("PGSpecialType")));
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "copyrightKeys" ) )
       {
	       attribTypeArray_.add(new Integer(pgEnum.getInt("ListOfString")));
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "copyrightMap" ) )
       {
         attribTypeArray_.add(new Integer(pgEnum.getInt("FilenameDataType")));
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "copyrightFlags" ) )
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "language" ) )
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
	     }
       else if( arrayValue.intValue() == pgEnum.getInt( "content" ) )
	     {
	       attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
	     }
	     else if( arrayValue.intValue() == pgEnum.getInt( "insList" ) )
	     {
	       attribTypeArray_.add(
                       new Integer(pgEnum.getInt("ListOfFilenameDataType") ) );
	     }
     }     
  }

  /*****************************************************************************
   * Called by the parser which passes in a token when an attribute in a stanza
   * is encountered. This method checks whether the attribute is valid within 
   * this stanza( which it represents )  and returns back the type of the 
   * attribute.
   *
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
	      Integer tokenValue = (Integer)attribTypeArray_.get( attribTokenIterator.nextIndex() - 1 );
        return tokenValue.intValue() ;
	    }
    }
    return -1;
  }
}  



