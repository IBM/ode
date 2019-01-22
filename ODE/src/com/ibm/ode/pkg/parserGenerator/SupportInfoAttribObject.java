/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 5/10/03 15:29:12
 * Date and Time File was extracted/checked out: 06/04/13 16:46:10
 *******************************************************************************/
package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;

/**
 * A class for representing the attributes and their of the file stanza
 * in the CMF. Any new stanza added to the CMF must be represented in
 * similar fashion as this class
 *
 * @version  1.2
 * @author   Prem Bala
 */
public class SupportInfoAttribObject extends AttributeObject
{
  private ArrayList attribTokenArray_;
  private ArrayList attribTypeArray_;

  public SupportInfoAttribObject( ParserGeneratorEnumType pgEnum )
  {
    /**
     * An array to hold token values for the attributes of the stanza
     * represented by this Class
     **/
     ListIterator attribTokenIterator;

     attribTokenArray_ = new ArrayList();

     // Add tokens list for each of the attribute in the stanza
     // represented by this object

     attribTokenArray_.add( new Integer( pgEnum.getInt( "readme" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "manpage" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "hotline" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "email" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "packageOrder" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "url") ) );

     // Construct an Array that equals the size of the attribTokenArray_
     attribTypeArray_ = new ArrayList( attribTokenArray_.size() );
     attribTokenIterator = attribTokenArray_.listIterator();

     while( attribTokenIterator.hasNext() )
     {
       Integer arrayValue = (Integer)attribTokenIterator.next();

       if( arrayValue.intValue() == pgEnum.getInt( "readme" ) )
       {
        attribTypeArray_.add( new Integer( pgEnum.getInt("PGSpecialType") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "manpage" ) )
       {
        attribTypeArray_.add( new Integer( pgEnum.getInt("PGSpecialType") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "hotline" ) )
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "email" ) )
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "packageOrder" ) )
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "url" ) )
       {
         attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
       }
    }
  }

  /**
   * Called by the parser which passes in a token when an attribute in a stanza
   * is encountered. This method checks whether the attribute is valid within
   * this stanza( which it represents )  and returns back the type of the attribute
   *
   * @param  token value of the attribute
   * @return token value of the type of attribute, -1 if not present
   */
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



