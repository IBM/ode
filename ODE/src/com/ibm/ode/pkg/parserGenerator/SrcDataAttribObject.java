package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;

//LVW new Attribute Object to represent the SourceData Stanza

public class SrcDataAttribObject extends AttributeObject
{
  
  private ArrayList attribTokenArray_;
  private ArrayList attribTypeArray_;

  public SrcDataAttribObject( ParserGeneratorEnumType pgEnum )

  {
    /**
     * An array to hold token values for the attributes of the stanza
     * represented by this Class
     **/
     ListIterator attribTokenIterator;

     attribTokenArray_ = new ArrayList();

     // Add tokens list for each of the attribute in the stanza represented by this object

     attribTokenArray_.add( new Integer( pgEnum.getInt( "sourceFile" ) ) );
     attribTokenArray_.add( new Integer( pgEnum.getInt( "sourceDir" ) ) );
    
     
     // Construct an Array that equals the size of the attribTokenArray_
     // to hold the type of each token in the attribTokenArray_ array.
     attribTypeArray_ = new ArrayList( attribTokenArray_.size() );
     attribTokenIterator = attribTokenArray_.listIterator();
     
     while( attribTokenIterator.hasNext() )
     {
       Integer arrayValue = (Integer)attribTokenIterator.next();
       
       if( arrayValue.intValue() == pgEnum.getInt( "sourceFile" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
       else if( arrayValue.intValue() == pgEnum.getInt( "sourceDir" ) )
       {
           attribTypeArray_.add( new Integer( pgEnum.getInt("String") ) );
       }
     }
  } // SourceDataAttribObject()
    
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
} //end SrcDataAttribObject class




