package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;

/*****************************************************************************
 * A class for representing the names of the stanzas which contain stanzas 
 * in the CMF. Any new stanza which contains stanzas added to the CMF must be
 * represented in similar fashion as this class.
 * @see AttributeCollection
 * @see AttributeObject
 * @version     1.9 98/01/22
 * @author 	Prem Bala
 ****************************************************************************/

public class StanzaCollection
{
  /**
   * Array declared to hold tokens of the stanza names which contain stanzas
   **/
  static private ArrayList stanzaCollectionArray_;

  /**
   * Array declared to hold tokens of child stanzas of the above declared stanza
   **/
  static private ArrayList childStanzaArray_;

  /** 
   * Array declared to hold tokens of parent stanza names of the stanza which contain 
   * stanza
   **/
  static private ArrayList parentStanzaArray_;

  static final String ERROR_MSG =   "Error initializing the StanzaCollection Object."
                                  + "\n"
                                  + "Illegal reference found in the Token Array.";

  /**********************************************************************************
   * Constructor for the StanzaCollection
   * @param pgEnum ParserGeneratorEnumType object reference
   * @exception StanzaCollectionException if error is encountered inserting tokens in childStanzaArray_
   **/
  public StanzaCollection( ParserGeneratorEnumType pgEnum) 
         throws StanzaCollectionException
  {
    ListIterator stanzaCollectionArrayIterator ;

    stanzaCollectionArray_ = new ArrayList();
    childStanzaArray_      = new ArrayList();
    parentStanzaArray_     = new ArrayList();

    // add all the names of the stanza which stanzas here
    stanzaCollectionArray_.add( new Integer( ParserGeneratorEnumType.START ) ); // START is a control symbol
    stanzaCollectionArray_.add( new Integer( pgEnum.getInt( "InstallEntity" ) ) );
    stanzaCollectionArray_.add( new Integer( pgEnum.getInt( "file" ) ) );

    
    // add the names of the parent stanza to the above stanzas
    // NOTE : should have one to one correspondence with the above array
    parentStanzaArray_.add( new Integer( ParserGeneratorEnumType.NULL ) ); // NULL is a control symbol
    parentStanzaArray_.add( new Integer( ParserGeneratorEnumType.START ) );
    parentStanzaArray_.add( new Integer( ParserGeneratorEnumType.START ) );

    
   // For each element in the stanzaCollectionArray_ 
    stanzaCollectionArrayIterator = stanzaCollectionArray_.listIterator();
    
    while( stanzaCollectionArrayIterator.hasNext() )
    {
      // create a Integer token array
      Integer arrayValue;
        
      arrayValue = (Integer)stanzaCollectionArrayIterator.next();

      //LVW Include SourceData and PackageData as child stanzas.
      if ( arrayValue.intValue() == ParserGeneratorEnumType.START)
	    {
	      ArrayList childArray = new ArrayList();
        childStanzaArray_.add( childArray);

        // add the children stanza names
        childArray.add (new Integer( pgEnum.getInt("InstallEntity")));
        childArray.add (new Integer( pgEnum.getInt("file")));
      }
      else if ( arrayValue.intValue() == pgEnum.getInt("InstallEntity"))
      {
        ArrayList childArray = new ArrayList();
        childStanzaArray_.add( childArray);

        //add the children stanza names of the Install Entity Stanza in CMF
        childArray.add(  new Integer( pgEnum.getInt("EntityInfo") ) ) ;
        childArray.add(  new Integer( pgEnum.getInt("LinkInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("VendorInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("ArchitectureInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("SupportInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("InstallStatesInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("PathInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("RequisitesInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("EntitySubsetInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("PackageInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("MvsInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("VplInfo") ) );
        childArray.add(  new Integer( pgEnum.getInt("ServiceInfo") ) );
      }
      else if (arrayValue.intValue() == pgEnum.getInt("file"))
      {    
        ArrayList childArray = new ArrayList();
        childStanzaArray_.add( childArray );

        //add the children stanza names of the File Stanza in CMF
        childArray.add( new Integer( pgEnum.getInt("SourceData"))) ;
        childArray.add( new Integer( pgEnum.getInt("PackageData")));
      }
      else                
        throw new StanzaCollectionException( ERROR_MSG 
                                                + "in stanzaCollectionArray_" );
    
    }
  }
   

  /********************************************************************************
   * validates a given token at a given index in the childStanzaArray_
   * @param token the token corresponding to a stanza name in the CMF
   * @param the index of the array in the childStanzaArray_ where the token could be found
   * @return  boolean indicate whether the token wasfound or not. 
   **/
  public boolean validateChildStanzaName( int token, int index )
  {
      ArrayList childArray = (ArrayList)childStanzaArray_.get( index );
      
      ListIterator childArrayIterator ;
      Integer toBeCheckedToken = new Integer( token );
      
      childArrayIterator = childArray.listIterator();
      while( childArrayIterator.hasNext() )
      {
        Integer arrayValue = (Integer)childArrayIterator.next() ;
        if( arrayValue.equals( toBeCheckedToken ) )
          return true;
      } 
      
     return false;
    }
  
  /*****************************************************************************
   * validates the presence of token in the stanzaCollectionArray_ and 
   * return the index
   * @param token token corresponding to stanza name in CMF
   * @return int 
   *           exists - return the index where this token is found <br>
   *           doesn't exist - return -1
  **/
  public int validateAndGetIndex( int token )
  {
    ListIterator stanzaCollectionIterator = stanzaCollectionArray_.listIterator() ;

    while( stanzaCollectionIterator.hasNext() )
      {
        Integer arrayValue = (Integer)stanzaCollectionIterator.next();

        if ( arrayValue.intValue() == token )
          {
            return (stanzaCollectionIterator.nextIndex() - 1);
          }
      }
    return -1;
  }

  /*****************************************************************************
   * checks whether the parent of the current stanza is valid or not
   * @param index index where the parent token could be found in 
   *        parentStanzaArray_ 
   *  @param token token value of the parent stanza name
   *  @return boolean  to indicate success or failure
  **/
  public boolean checkParent( int index, int token )
  {
    Integer tokenValue = (Integer)parentStanzaArray_.get( index );
    
    if( tokenValue.intValue() == token )
      return true;
    else
      return false;
  }

  /*****************************************************************************
   * given a valid index to a stanzaname token return back 
   * the corresponding index of the parent stanza token value 
   * returns index if valid - else throws ParserException
   * Modification - KS - 5/11/97
   * @param token the Stanza name token
   * @return int the token value of the parent stanza name
  **/ 
  public int getParentIndex( int curindex)
    throws ParserException
  {
    Integer tokenValue;
    int newindex = -1;

    if (curindex < 0 )
      throw new ParserException("StanzaCollection: Invalid index passed"
                                + " in call to getParentIndex" );

    tokenValue = (Integer)parentStanzaArray_.get( curindex );
    newindex = validateAndGetIndex(tokenValue.intValue() );

    if (newindex < 0 )
      throw new ParserException("StanzaCollection: Invalid Parent-child"
                                + " relationship specified between stanzas." );

    return newindex;
  }

  /*****************************************************************************
   * given the index , return back the token from stanzaCollectionArray_
   * @param index index where the token could be found in stanzaCollectionArray_
   * @return int the token value from the stanzaCollectionArray_
   **/
  public int getToken( int index )
  {
    Integer tokenValue;
    
    tokenValue = (Integer)stanzaCollectionArray_.get( index );

    return tokenValue.intValue();
  }  
	
} 









