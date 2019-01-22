package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;

/*****************************************************************************
 * A class for representing the attributes and their of the file stanza
 * in the CMF. Any new stanza added to the CMF must be represented in
 * similar fashion as this class
 * @version     1.3 97/09/30
 * @author      Prem Bala
 ****************************************************************************/
public class MvsInfoAttribObject extends AttributeObject
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

  public MvsInfoAttribObject( ParserGeneratorEnumType pgEnum )
  {                     
    ListIterator attribTokenIterator;

    attribTokenArray_ = new ArrayList();

    // Add tokens list for each of the attribute in the stanza represented by this object

    attribTokenArray_.add( new Integer( pgEnum.getInt( "applid" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "distlibs" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "srel" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "type" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "fesn" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "sep" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "delete" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "future" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "previous" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "ctldefinFile" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "extraSmpeFile" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "versionReq" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "rework" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "jclinLib" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "dsnHlq" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "lkedUnit" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "lkedUnit" ) ) );
    attribTokenArray_.add( new Integer( pgEnum.getInt( "featureFmids" ) ) );

    // Construct an Array that equals the size of the attribTokenArray_
    attribTypeArray_ = new ArrayList( attribTokenArray_.size() );
    attribTokenIterator = attribTokenArray_.listIterator();

    while( attribTokenIterator.hasNext() )
      {
        Integer arrayValue = (Integer)attribTokenIterator.next();
        
        if( arrayValue.intValue() == ( pgEnum.getInt( "applid" ) ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "distlibs" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );   
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "srel" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "type" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfConstant") ) );   
          }     
        else if( arrayValue.intValue()  == pgEnum.getInt( "fesn" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("Constant") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "sep" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("ListOfString") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "delete" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "future" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "previous" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "ctldefinFile" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("FilenameDataType") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "extraSmpeFile" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("FilenameDataType") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "versionReq" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "rework" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );   
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "jclinLib" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) ); 
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "dsnHlq" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) ); 
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "lkedUnit" ) )
          {
            attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) ); 
          }
        else if( arrayValue.intValue()  == pgEnum.getInt( "featureFmids" ) )
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



