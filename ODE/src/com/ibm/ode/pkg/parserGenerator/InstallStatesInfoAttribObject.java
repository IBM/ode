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
 * @version	1.6 97/05/07
 * @author 	Prem Bala
 ****************************************************************************/
public class InstallStatesInfoAttribObject extends AttributeObject
{
  private ArrayList attribTokenArray_;
  private ArrayList attribTypeArray_;

  public InstallStatesInfoAttribObject( ParserGeneratorEnumType pgEnum )
  {			  
    /**
     * An array to hold token values for the attributes of the stanza
     * represented by this Class
     **/
     ListIterator attribTokenIterator;

     attribTokenArray_ = new ArrayList();

     // Add tokens list for each of the attribute in the stanza represented by this object

     attribTokenArray_.add( new Integer( pgEnum.getInt( "interactive" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "mediaId" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "totalMediaUsed" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "installStates" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "removableStates" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "constantList" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "isLocatable" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "isKernel" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "installDir" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "installSpace" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "bootReqmt" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "adePackageFlags" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "packageFlags" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "mode" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "selection" ) ) );  
     attribTokenArray_.add( new Integer( pgEnum.getInt( "adeInvFlags" ) ) );  
     attribTokenArray_.add( new Integer( pgEnum.getInt( "maxInst" ) ) );  
     attribTokenArray_.add( new Integer( pgEnum.getInt( "arFlags" ) ) );  
     attribTokenArray_.add( new Integer( pgEnum.getInt( "compidTable" ) ) );
   
     // Construct an Array that equals the size of the attribTokenArray_
     attribTypeArray_ = new ArrayList( attribTokenArray_.size() );
     attribTokenIterator = attribTokenArray_.listIterator();

     while( attribTokenIterator.hasNext() )
     {
	     Integer arrayValue = (Integer)attribTokenIterator.next();
	 
	 if( arrayValue.intValue() == pgEnum.getInt( "interactive" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "mediaId" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "totalMediaUsed" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "installStates" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "removableStates" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "constantList" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "isLocatable" ) ) 
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "isKernel" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "installDir" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("PGSpecialType") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "installSpace" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("PGSpecialType") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "bootReqmt" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "adePackageFlags" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "packageFlags" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "mode" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "selection" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "adeInvFlags" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "maxInst" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "arFlags" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );	     
	   }
	 else if( arrayValue.intValue() == pgEnum.getInt( "compidTable" ) )
	   {
	     attribTypeArray_.add( new Integer( pgEnum.getInt("FilenameDataType") ) );	     
	   }				    
    } //while     
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



