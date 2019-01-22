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
 * A class for representing the attributes and their of the file stanza
 * in the CMF. Any new stanza added to the CMF must be represented in
 * similar fashion as this class
 * @version	1.5 97/05/07
 * @author 	Prem Bala
 ****************************************************************************/
public class EntitySubsetInfoAttribObject extends AttributeObject
{
  
  private ArrayList attribTokenArray_;
  private ArrayList attribTypeArray_;

  public EntitySubsetInfoAttribObject( ParserGeneratorEnumType pgEnum )
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
    
    attribTokenArray_.add( new Integer( pgEnum.getInt( "subsetName" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "fullSubsetName" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "subsetDescription" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "subsetContent" ) ) );
    
    // Construct an Array that equals the size of the attribTokenArray_
    attribTypeArray_ = new ArrayList( attribTokenArray_.size() );
    attribTokenIterator = attribTokenArray_.listIterator();
    
    while( attribTokenIterator.hasNext() )
    {
      Integer arrayValue = (Integer)attribTokenIterator.next();
	
      if( arrayValue.intValue() == pgEnum.getInt( "subsetName" ) )
	    {
	      attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
	    }
    	else if( arrayValue.intValue() == pgEnum.getInt( "fullSubsetName" ) )
	    {
	      attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
	    }
    	else if( arrayValue.intValue() == pgEnum.getInt( "subsetDescription" ) )
  	  {
	      attribTypeArray_.add( new Integer( pgEnum.getInt("PGSpecialType") ) );
	    }
    	else if( arrayValue.intValue() == pgEnum.getInt( "subsetContent" ) )
	    {
	      attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
	    }		    
    }     
  }
  
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



