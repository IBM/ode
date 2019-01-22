//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/PgSpecialType.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.3
//* 
//* Date and Time File was last checked in:       97/09/30 10:18:17
//* Date and Time File was extracted/checked out: 99/04/25 09:13:26
//* 
//* 
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* BP       F 784       Initial Creation of the File
//* BP       F 1004      Add documentation
//*
//*****************************************************************************
   
package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.lang.*;

/**
 * A class for representing an attribute of type SpecialType in the CMF.
 * Any specialType attribute defined in the CMF could be of type
 * String, FileName or a Constant. This type information is required by 
 * some Generators. Attributes in CMF whcih could be of the above types
 * must be represented using this class
 * @version	1.3 97/09/30
 * @author 	Prem Bala
**/
public class PgSpecialType
{
  /**
   * type_ :- holds the type of the attribute represented by this object
   **/
  private int         type_;
  /**
   * value_ :- value of the attribute represented by this object
   **/
  private String      value_;

  /******************************************************************************
   * Default constructor
   * */
  public PgSpecialType()
  {
    type_  = 0;
    value_ = "";
  }
  
  /******************************************************************************
   * Constructor with type and value  being passed
   * @param int     type  :- type ( from PGEnumType )of the attribute represented by this object
   * @param String  value :- value of the attribute
   **/
  public PgSpecialType(int type, String value)
  {
    type_  = type;
    value_ = value;
  }

  /******************************************************************************
   * Copy Constructor
   * @param PgSpecialType orig :- the PgSpecialType to be copied over
   **/
  public PgSpecialType( PgSpecialType orig )
  {
    this();
    copy( orig );
  }

  private void copy( PgSpecialType right )
  {
    type_ = right.type_;
    value_ = right.value_;
  }
  
  /******************************************************************************
   * getter for type_
   * @return int :- returns the type
   **/  
  public int getType()
  {
    return type_;
  }
  
  /******************************************************************************
   * getter for value
   * @return String :- returns the value.
   **/  
  public String getValue()
  {
    return value_;
  }
 
  /******************************************************************************
   * setter for type_
   * @param int :- the type to be set
   * usage :
   * <pre>
   *    setType( ParserGeneratorEnumType.STRING )
   * </pre>
   **/  
  public void setType( int type )
  {
    type_ = type;
  }
  
  /******************************************************************************
   * setter for value
   * @param String :- sets the value
   **/  
  public void setValue( String value )
  {
    value_ = value ;
  }
      
}

