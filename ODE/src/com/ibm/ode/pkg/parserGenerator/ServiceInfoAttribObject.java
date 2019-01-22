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
 * A class for representing the attributes and their of the ServiceInfo
 * stanza in the CMF. Any new stanza added to the CMF must be represented 
 * in similar fashion as this class
 * @version	1.4	
 * @author 	Kurt Shah
 ****************************************************************************/
public class ServiceInfoAttribObject extends AttributeObject
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
  
  public ServiceInfoAttribObject( ParserGeneratorEnumType pgEnum )
  {			  
    ListIterator attribTokenIterator;

    attribTokenArray_ = new ArrayList();
    
    // Add tokens list for each of the attribute in the stanza represented by this object
    
    attribTokenArray_.add( new Integer( pgEnum.getInt( "retainChangeTeam" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "retainComponent" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "retainRelease" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "contactName" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "contactPhone" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "contactNode" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "contactUserId" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "memoToUsers" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "labelText1" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "labelText2" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "ciaProductIdentification" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "allApars" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "autoIfreq" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "SMSSupercede" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "pkgPtf" ) ) );
    
    // Construct an Array that equals the size of the attribTokenArray_
    attribTypeArray_ = new ArrayList( attribTokenArray_.size() );
    attribTokenIterator = attribTokenArray_.listIterator();
    
    while( attribTokenIterator.hasNext() )
      {
	Integer arrayValue = (Integer)attribTokenIterator.next();
	
	if( arrayValue.intValue() == ( pgEnum.getInt( "retainChangeTeam" ) ) )
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
	  }
	else if( arrayValue.intValue() == ( pgEnum.getInt( "retainComponent" ) ) )
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "retainRelease" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "contactName" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "contactPhone" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "contactNode" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "contactUserId" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "memoToUsers" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "labelText1" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "labelText2" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "ciaProductIdentification" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "allApars" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "autoIfreq" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "SMSSupercede" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );	     
	  }
	else if( arrayValue.intValue() ==  pgEnum.getInt( "pkgPtf" ) ) 
	  {
	    attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );	     
	  }
   }  //while   
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



