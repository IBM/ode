/*******************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 *
 * Version: 1.2
 *
 * Date and Time File was last checked in: 5/10/03 15:28:29
 * Date and Time File was extracted/checked out: 06/04/13 16:45:58
 *******************************************************************************/
package com.ibm.ode.pkg.parserGenerator;

import java.util.*;
import java.io.*;

/**
 * A class for representing the attributes and their of the file stanza
 * in the CMF. Any new stanza added to the CMF must be represented in
 * similar fashion as this class
 *
 * @version 1.2
 * @author Prem Bala
 */
public class PathInfoAttribObject extends AttributeObject
{
  private ArrayList attribTokenArray_;
  private ArrayList attribTypeArray_;

  /**
   *
   */
  public PathInfoAttribObject( ParserGeneratorEnumType pgEnum )
  {
    // An array to hold token values for the attributes of the stanza
    // represented by this Class
    ListIterator attribTokenIterator;

    attribTokenArray_ = new ArrayList();

    // Add tokens list for each of the attribute in the stanza represented by this object
    attribTokenArray_.add( new Integer( pgEnum.getInt( "inputPath" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "configFiles" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "odmAddFiles" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "odmClassDef" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "rootControlFiles" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "rootAddFiles" ) ) );

    // Construct an Array that equals the size of the attribTokenArray_
    attribTypeArray_ = new ArrayList( attribTokenArray_.size() );
    attribTokenIterator = attribTokenArray_.listIterator();

    while( attribTokenIterator.hasNext() )
    {
      Integer arrayValue = (Integer)attribTokenIterator.next();

      if( arrayValue.intValue() == pgEnum.getInt( "inputPath" ) )
      {
        attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );
      }
      else if( arrayValue.intValue() == pgEnum.getInt( "configFiles" ) )
      {
        attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfReqType") ) );
      }
      else if( arrayValue.intValue() == pgEnum.getInt( "odmAddFiles" ) )
      {
        attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfFilenameDataType") ) );
      }
      else if( arrayValue.intValue() == pgEnum.getInt( "odmClassDef" ) )
      {
        attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
      }
      else if( arrayValue.intValue() == pgEnum.getInt( "rootControlFiles" ) )
      {
        attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfFilenameDataType") ) );
      }
      else if( arrayValue.intValue() == pgEnum.getInt( "rootAddFiles" ) )
      {
        attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfFilenameDataType") ) );
      }
    }
  }

  /**
   * Called by the parser which passes in a token when an attribute in a stanza
   * is encountered. This method checks whether the attribute is valid within
   * this stanza( which it represents )  and returns back the type of the attribute
   *
   * @param  int :- token value of the attribute
   * @return int :- token value of the type of attribute
   *                -1 if not present
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



