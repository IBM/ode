//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/ExtendedSymbol.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.5
//* 
//* Date and Time File was last checked in:       98/01/22 18:14:24
//* Date and Time File was extracted/checked out: 99/04/25 09:13:25
//* 
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* KS       F 784
//*
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;


/**
 * @version     1.5 98/01/22
 * @author 	Kurt Shah
 Purpose :
    Provides a mechanism to recognise and store type and 
    value of 'VALUE' symbols, or Errors.
    
    A new instance is created by the Scanner when any char sequence 
    is recognized and sent back to the parser which in turn uses 
    this to recognize the particular char. sequence interprete it 
    as type ExtendSymbol, and retreive its value and type.
**/

public class ExtendedSymbol extends Symbol
{

/**********************************************************************
 * type of the extended symbol - must correspond to a valid integer token
 *  defined to specify datatypes in ParserGeneratorEnumType.
**/
    private int type_;


/**********************************************************************
 * value of the extended symbol - holds the actual value of this instance
 * as a string.
 *
 * e.g., if type of this instance corresponds to 'Constant', the value of
 *  this constant is represented as a string in this field.
**/
    private String value_;

/**************************************************************************
**
 * Invalid constructor call - always throws ScannerException
 * @exception ScannerException - thrown when blank constructor for ExtendedSymbol is called.
**/

    //blank constructor would be an error.
    public ExtendedSymbol() 
	throws ScannerException 
    {
        throw new ScannerException("ExtendedSymbol: Blank constructor called.");
    }
        

/**************************************************************************
**
 * Creates a new instance with specified type and value
 * @param 	type  :- type of ExtendedSymbol as integer(String/FileName/...)
 * @param 	value :- value of ExtendedSymbol as String
**/

    //parametrized constructor.
    public ExtendedSymbol(int type, String value){

		//standard token for extendedsymbol
        this.token_ = ParserGeneratorEnumType.EXTENDSYMBOL;
        this.type_ = type;
        this.value_ = value;
    }


//set functions not made available - constructor to be the only way to set
//parameters


/**************************************************************************
     returns the type of this Extended Symbol.
     @return type  :- valid integer corresponding to type of ExtendedSymbol
     <p>
     <p>
     <p>
     <B>See Also:</B>Valid values are type tokens defined in class ParserGeneratorEnumType 
     @see ParserGeneratorEnumType 
**/
    public int getType()
    {
	return this.type_ ;
    }

/**************************************************************************
     returns the value of this Extended Symbol.
     @return value :- valid String corresponding to value of ExtendedSymbol
     <B>See Also:</B>Valid values are type tokens defined in class ParserGeneratorEnumType 
     @see ParserGeneratorEnumType 
**/

    public String getValue()
    {
	return this.value_;
    }

}
