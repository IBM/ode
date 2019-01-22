//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/Symbol.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.5
//* 
//* Date and Time File was last checked in:       98/01/22 18:14:53
//* Date and Time File was extracted/checked out: 99/04/25 09:13:27
//* 
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* KS       F 784	Initial Creation of file
//*
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;


/**
 * @version     1.5 98/01/22
 * @author 	Kurt Shah
 Purpose :
    Provides a mechanism to recognise various known character 
    sequences as symbols.
    
    A new instance is created by the Scanner when any char sequence
    is recognized and sent back to the parser which in turn uses 
    this to recognize the particular char. sequence and interprete it.
**/

public class Symbol {
    
/******************************************************************
 * value of token representing this symbol as an integer.
 * valid values include tokens defined in ParserGeneratorEnumType
 * @see ParserGeneratorEnumType
**/

    protected int token_;
    
    //Blank Constructor - Creates a dummy symbol 
    //by default if no token is specified.

/******************************************************************
 * Creates a new Dummy symbol.
**/
    public Symbol() {
        this.token_ = ParserGeneratorEnumType.DUMMY;
    }
    
/******************************************************************
 * Creates a new symbol of type specified.
 * @param token		token representing this valid CMF language 'word' as an integer defined in ParserGeneratorEnumType	
 * @see ParserGeneratorEnumType
**/
    public Symbol(int token_) {
        this.token_ = token_;
    }
    
/******************************************************************
 * Returns value of token associated with this symbol
 * @return the token as an int
**/
    public int getToken()
    {
	return this.token_ ;
    }

}// end definition of class symbol
