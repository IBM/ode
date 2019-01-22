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
 * A class for representing the stanza names which contain attrib-value pairs
 * in the CMF. Any new stanza added to the CMF must be represented in
 * similar fashion as this class. A corresponding AttributeObject needs to exist
 * along with this addition.
 * @version	1.5 97/05/06
 * @author 	Prem Bala
 ****************************************************************************/

public class AttributeCollection
{
  /**
   * variable to hold tokens of the stanza names which contain attrib-value pairs in CMF
   **/
  static ArrayList stanzaTokenArray_;

  /**
   * variable to hold references of the objects which hold information of each 
   * attribute and its type in the CMF. NOTE that this has a ONE-TO-ONE correspondence
   * to the above array of stanza token's.
   **/
  static ArrayList attributeObjectArray_;

  /******************************************************************************
   * Constructor for AttributeCollection. It is responsible for creating the
   * the releationship between a stanza name and the attributes it contains.
   * @param  ParserGeneratorEnumType :- reference to the P/G Enum Class
   **/
  public AttributeCollection( ParserGeneratorEnumType pgEnum)
  {
    ListIterator stanzaTokenIterator;

    stanzaTokenArray_ = new ArrayList();    
    
    // Add tokens list for each of the stanza names which contain attribute-value pairs in the CMF

    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "SourceData" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "PackageData" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "EntityInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "LinkInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "VendorInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "ArchitectureInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "SupportInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "InstallStatesInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "PathInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "RequisitesInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "EntitySubsetInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "PackageInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "MvsInfo" ) ) );
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "VplInfo" ) ) );

    // KS-F1584 Start Service Packaging modification
    stanzaTokenArray_.add( new Integer( pgEnum.getInt( "ServiceInfo" ) ) );
    //KS-F1584:Service Packaging : End

    // Construct an Array that equals the size of the stanzaTokenArray_
    attributeObjectArray_ = new ArrayList( stanzaTokenArray_.size() );
    
    stanzaTokenIterator = stanzaTokenArray_.listIterator();

    // Create the corresponding attribObjects for each token in the stanaTokenArray_
    while( stanzaTokenIterator.hasNext() )
      {
        Integer arrayValue = (Integer)stanzaTokenIterator.next();
        
        if( arrayValue.intValue() == pgEnum.getInt("SourceData") )
          {
            attributeObjectArray_.add( new SrcDataAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "PackageData" ) )
          {
            attributeObjectArray_.add( new PkgDataAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "EntityInfo" ) )
          {
            attributeObjectArray_.add( new EntityInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "LinkInfo" ) )
          {
            attributeObjectArray_.add( new LinkInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "VendorInfo" ) )
          {
            attributeObjectArray_.add( new VendorInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "ArchitectureInfo" ) )
          {
            attributeObjectArray_.add( new ArchitectureInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "SupportInfo" ) )
          {
            attributeObjectArray_.add( new SupportInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "InstallStatesInfo" ) )
          {
            attributeObjectArray_.add( new InstallStatesInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "PathInfo" ) )
          {
            attributeObjectArray_.add( new PathInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "RequisitesInfo" ) )
          {
            attributeObjectArray_.add( new RequisitesInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "EntitySubsetInfo" ) )
          {
            attributeObjectArray_.add( new EntitySubsetInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "PackageInfo" ) )
          {
            attributeObjectArray_.add( new PackageInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "MvsInfo" ) )
          {
            attributeObjectArray_.add( new MvsInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "VplInfo" ) )
          {
            attributeObjectArray_.add( new VplInfoAttribObject( pgEnum ) );
          }
        else if( arrayValue.intValue() == pgEnum.getInt( "ServiceInfo" ) )
          {
            attributeObjectArray_.add( new ServiceInfoAttribObject( pgEnum ) );
          }
      }    
  }

  /*****************************************************************************
   * Called by the parser which passes in a token when a stanza name 
   * is encountered. This method checks whether the stanza name is valid 
   *  and returns back the index in the array where this token was found
   * @param  int :- token value of the stanza
   * @return int :- index in the array where the token was found
   *                -1 if not present
   **/
  public int validateAndGetIndex( int token )
  {
    ListIterator stanzaTokenIterator = stanzaTokenArray_.listIterator();
    Integer tokenValue;

    while( stanzaTokenIterator.hasNext() )
    {
      tokenValue = (Integer)stanzaTokenIterator.next();
      if( tokenValue.intValue() == token )
        return (stanzaTokenIterator.nextIndex() - 1);
    }
    
    return -1;
  }

  /*****************************************************************************
   * Called by the parser. Given the index ( obtained by the previous call )
   * this method will return a reference to the AttribObject which holds
   * info about the attributes ( of a particular stanza ) and it's type .
   * @param  int :- index where the AttribObject could be found
   * @return AttributeObject :- reference to the (stanzaName)AttribObject
   **/
  public AttributeObject getRefToAttributeObject( int index )
  {
    return ( (AttributeObject)attributeObjectArray_.get( index ) );
  }
}
