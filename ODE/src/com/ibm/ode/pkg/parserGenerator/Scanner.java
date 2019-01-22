//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//* File, Component, Release: COM/ibm/sdwb/bps/subsystem/build/packaging/parserGenerator/Scanner.java, parserGenerator, sdwb2.2, sdwb2.2_b37
//*
//* Version: 1.8
//*
//* Date and Time File was last checked in:       98/01/22 18:14:45
//* Date and Time File was extracted/checked out: 99/04/25 09:13:26
//*
//*
//* Author   Defect (D) or Feature (F) and Number
//* ------   ------------------------------------
//* KS       F 784	Initial Creation of file
//* KS       F 784	modified reserved word - to take ';' as a
//*				valid delimiter for a reserved word
//* KS       D 1510     Modify InputStream to InputStreamReader to ensure
//*                      proper character read using default char. encoding
//* KS       D 1682     Modify scan method to get newline char as a single
//*                      '\n' char. irrespective of platform
//*
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;

import java.io.* ; //InputStream InputStreamReader FileInputStream File;
import java.lang.String;
import java.util.NoSuchElementException;


/**
 * @version     1.8 98/01/22
 * @author 	Kurt Shah
 Purpose :
    Provides a mechanism to tokenize input based on strings
    of interest and tokens defined in the ParserGeneratorEnumBase
    class.  Uses statics defined in the ParserGeneratorEnumBase
    class when strings or tokens are recognized, and tokenizes them
    by creating instances (one for each token) of class Symbol
    (or its derived class Extended Symbol in specific cases where
    value associated with the symbol is to be stored and passed back).

    It recognizes valid tokens, valid value delimiters, control
    characters of interest (like EOF/NULL), reserved words etc.
    All these values are returned as Symbols back to call - except
    when a comment (slash-slash and slash-star are supported) is
    encountered. in case of a comment, it is to be ignored, and
    hence not returned back to call. Instead, the next token is
    returned back.
**/

public class Scanner
{


// D 1510 related changes start
/******************************************************************
 * handle to Input Stream Reader, using which source CMF is read.
 * valid values include valid InputStream reference only
 * @see java.io.InputStream
 * @see java.io.InputStreamReader
 * @see java.io.BufferedReader
 * @see java.io.LineNumberReader
**/
    private LineNumberReader inFile_;//Reads i/p in default char encoding


// D 1510 related changes end here


/******************************************************************
 * private variable which stores (last character scanned) next character
 *    to be interpreted by the scanner.
**/
    private int nextChar_; //next character to be scanned

/******************************************************************
 * private variable which holds handle to instance of
 * ParserGeneratorEnumType, using which words valid for CMF language
 * can be recognized, and corresponding tokens associated with it.
 *
**/
    private ParserGeneratorEnumType pgEnum_ ;


/******************************************************************
 * private scanner variable, keeps track of line no. of current line
 *   being scanned - primarily used for error reporting and tracking.
**/
    private int curLineNo_; //current line no. in input


/******************************************************************
 * Creates an instance of the Scanner, which expects CMF input from
 *   <I>stdin<I>, and creates and associates a new instance of
 *   ParserGeneratorEnumType with corresponding local variable.
 *   Initializes other scanner variables.
 *
 * @exception ScannerException if error occurs in reading from <I>stdin</I>.
**/
    public Scanner() throws ScannerException
    {
      //constructor for input from stdin - no filename specified

// D 1682/1510 related changes start
	InputStream inStream = System.in;
        InputStreamReader inStreamReader = new InputStreamReader(inStream);
        this.inFile_ = new LineNumberReader(inStreamReader);
// D 1682/1510 related changes end here

        this.pgEnum_ = new ParserGeneratorEnumType();
        getNextChar();
        curLineNo_=1;
    }//end blank constructor


/******************************************************************
 * Creates an instance of the Scanner, which expects CMF input from
 *   specified file. Checks for validity of file.
 *   Initializes other scanner variables.
 *
 * @param fileName Relative Path + Filename, from where CMF input is to be read.
 * @param pgEnumBase Instance of ParserGeneratorEnumType, which holds information about valid CMF tokens, symbols and reserved words.
 *
 *
 * @see ParserGeneratorEnumType
 * @exception java.io.FileNotFoundException if specified file is not found
 * @exception ScannerException if file does not exist as a normal readable text file.
**/
    public Scanner(String fileName, ParserGeneratorEnumType PGEnumBase)
	throws  java.io.FileNotFoundException,
		ScannerException
    {
      //constructor for input from file named filename

	//do a check if this file exists and is readable - etc.

	try
	{
	    if (fileName == null)
	    {
		throw new ScannerException("ScannerException: CMF " +
		  "filename cannot be null.");
	    }
	    File tempFile = new File(fileName);

	    if (tempFile.exists()  == false )
	    {
		throw new ScannerException("\n\nScanner: Cannot locate "
		  + "file " + fileName + "\n\n" + "Absolute path of file "
		  + "specified is \n\n" + tempFile.getAbsolutePath()
		  + "\n\n\n");
	    }

	    if (tempFile.canRead()  == false )
	    {
		throw new ScannerException("\n\nScanner: Cannot read "
		  + "file " + fileName + "\n\n" + "Absolute path of file "
                  + "specified is \n\n" + tempFile.getAbsolutePath()
		  + "\n\n\n");
	    }

	    if (tempFile.isFile()  == false )
	    {
		throw new ScannerException("\n\nScanner: File " + fileName
		 + " not a normal file (i.e. this may be the name of a"
		 + " directory).\n\n");
	    }
	}//end try block
	catch(SecurityException e)
	{
		throw new ScannerException("Scanner: " + e.toString() );
	}

	//file is okay

// D 1682/1510 related changes start
	InputStream inStream = new FileInputStream(fileName);
        InputStreamReader inStreamReader = new InputStreamReader(inStream);
        this.inFile_ = new LineNumberReader(inStreamReader);
// D 1682/1510 related changes end here

        this.pgEnum_ = PGEnumBase;
        curLineNo_=1;
    }//end parametrized constructor


/*****************************************************************
 * @return returns the line no. of current line being scanned in the CMF
**/
    public int getCurLineNo()
    {
	return this.curLineNo_ ;
    } //end method getCurLineNo()



/*****************************************************************
 * Private Scanner Method.
 *   Used to recognize C/C++ (slash-star and slash-slash) type comments.
 *   skips input characters that occur as part of comment.
 *
 *
 * @exception ScannerException if an invalid character is encountered after the first '/' signifying possible start of comment
**/
    private void comment()
	throws ScannerException
    {

    //this method is used to ignore (and skip) characters that
    //appear as either as C-type or C++ type comments.

    //enters here after the first slash has been recognized.

        if ( (char)nextChar_ == '/' ){ // slashslash comments
            getNextChar();
            while ( (char)nextChar_ != '\n' ) { //end of line
                getNextChar();
            }//end while
        }
        else
        if ( (char)nextChar_ == '*' ){ // slashstar comments
            getNextChar();
            while ( true ) {
                while ( (char)nextChar_ != '*' ) { //possible end of comment
                    getNextChar();
                if (nextChar_ == -1)
                  throw new ScannerException("Scanner: Encountered an "
                                              + "unclosed comment");
                }
                getNextChar();
                if ( (char)nextChar_ == '/' )
		{
			//comment over
			getNextChar();
			return;
		}
            }
        }
        else
          //only / or * can follow the first / when
          //appearing as begin of token
        throw new ScannerException("Scanner: Invalid Character at line "
             + curLineNo_ + " after character '/' - comment expected");
    }//end method comment



/*****************************************************************
 * Private Scanner Method.
 *   called when the starting delimiter for an extended symbol is
 *   recognized. Reads input, creates new instance of extended symbol
 *   with delimiter dependent type and input value bound by ending
 *   delimiter.
 *
 *
 * @param lastChar Ending delimiter character as an integer, used by method to recognize end of value of ExtendedSymbol
 *
 *
 * @exception ScannerException if a valid delimiter for known data types is not passed as parameter, or EOF is encountered before getting the expected ending delimiter.
 *
 *
 * @return a new instance of ExtendedSymbol, with type and value set according to the input.
 *
 * @see ExtendedSymbol
 * @see ScannerException
 * @see ParserGeneratorEnumType
**/
    private ExtendedSymbol getExtSymbol(int lastChar)
	throws ScannerException
    {

    //this method-called by the parser-gets one Extendedsymbol per invocation.
    //the scanner calls it when one of the VALUE delimiters (", ', < - for now)
    //are encountered.
    //scanner calls it passing the character it expects as the last char
    //the delimiting character for this value type to be recognized.

        String value;
        int type = ParserGeneratorEnumType.ERROR; //default type.
        StringBuffer buffer = new StringBuffer();

        if ( (char)lastChar == '>' )
	{
	   type = ParserGeneratorEnumType.FILENAMEDATATYPE;//for data type
	}
        else if ( (char)lastChar == '"' )
	{
	   type = ParserGeneratorEnumType.STRING;
	}
        else if ( lastChar == '\'' )
	{
	   type = ParserGeneratorEnumType.CONSTANT;
	}
        else
	{
		throw new ScannerException("Scanner :" +
			"Unknown type in call to getExtSymbol"
			+ "at line" + curLineNo_ + " in the CMF");
	}

          //compensatory call - not called as always before this method.
          //gets the char. after the extsymbol delimiter
	  //- first char in value string.
        getNextChar();

          //until the ending delimiter is found, buffer one
	  //character at a time to StringBuffer
        while ( nextChar_ != lastChar ) {

	    //check if end of file is reached - possible cause - missing
	    //brace - FileInputStream.read() returns -1 if eof is reached.

	    if (nextChar_ == -1)
	    {
		//EOF encountered
		throw new ScannerException("Value Delimiter at line " +
		  curLineNo_ + " in CMF not found. ");
	    }


	    //check for occurence of delimiter within value - preceded
	    //by a '\' - if '\' is encountered, if the next char is
	    //the ending delimiter, then the ending delimiter is taken
	    //as part of the value, and '\' is ignored - if it is foll
	    //by any other character, '\' is considered to be part of the
	    //value itself.

	    if ( (char)nextChar_ == '\\' )
	    {
		getNextChar();
		if (nextChar_ == lastChar)
		{
			//append just last char and ignore '\'
            		buffer = buffer.append( (char)nextChar_ );

		}
		else
		{
			//append both '\' and nextChar
            		buffer = buffer.append( ((char)('\\') ) );
            		buffer = buffer.append( (char)nextChar_ );

		};
	    }//end if
	    else
	    {
		buffer = buffer.append( (char)nextChar_ );
	    }


	    //check if this character read, is a valid character
	    //for this data type

	    if ( checkIfInvalidChar(type, nextChar_ ) == true )
	    {
            	getNextChar();
	    }
	    else
	    {
		throw new ScannerException("Invalid character in "
		   + "attribute value for specified attribute type"
		   + " at line " + curLineNo_ + " in CMF");
	    }

        }

          //convert string buffer to string to maintain type compatibility
        value = buffer.toString();

        getNextChar(); //maintain definition of nextChar_.

        return new ExtendedSymbol(type, value);

    }//end method getExtSymbol


/*****************************************************************
 * Private Scanner Method.
 *   called from within method getExtSymbol - depending on type of extendedsymbol, certain characters can be recognized as invalid characters.
 *
 *
 * @param type The data type of the Extended Symbol
 * @param curChar The character which is to be checked if valid or not for the said data type.
 *
 *
 * @return Boolean value - false if character is invalid for specified data type.
 *
 * @see <a href="http://w3dce.raleigh.ibm.com/~balap/pkging/cmf/cmf.attr.html">CMF document specifying attribute value limitations for acceptable data types.</a>
**/
    private boolean checkIfInvalidChar(int type, int curChar)
    {

      //checks if curChar, is a valid character for a value of type
      //datatype type - returns boolean true or false.

	//do not include delimiting or special characters which can
	//be recognized as valid with any escape character sequence

	switch(type){
	case ParserGeneratorEnumType.CONSTANT :

		//constant cannot have any white space
		if (    (nextChar_ == '\n')
		     || (nextChar_ == '\t')
		     || (nextChar_ == ' ') )
		{
			return false;
		}
		break;

	case ParserGeneratorEnumType.FILENAMEDATATYPE:

		//filenames cannot have tab or newline character
		if (    (nextChar_ == '\n')
		     || (nextChar_ == '\t') )
		{
			return false;
		}
		break;

	default :  //case for datatypes with no restrictions eg. String
		return true;

	}//end switch

	return true;
    }//end method checkIfInvalidChar


/******************************************************************
 * Private Scanner Method.
 * Used to recognize a CMF reserved word.
 *
 * buffers one character at a time and appends it, until an ending delimiter for a reserved word (=,:,;,{,} or white space) is encountered. If valid CMF word is recognized, creates an instance of type Symbol with integer corresponding to this word and returns it. Else, if the word is not recognized, or maximum length specified is exceeded, an ExtendedSymbol of type ERROR with appropriate message is created and returned.
 *
 *
 * @param maxLength maximum length allowed for matching input to possible CMF reserved words, before Error is presumed.
 * @return instance of type Symbol representing the reserved word
 *
 * @exception ScannerException if error occurs in reading character from input stream.
 * @see ParserGeneratorEnumType#getInt
 * @see Symbol
 * @see ExtendedSymbol
**/

    private Symbol reservedWord( int maxLength )
	throws ScannerException
    {
        //code to scan and zero down on reserved word

        String value;
        int length = 0;
        int token;
        StringBuffer buffer = new StringBuffer();

          //until the ending delimiter is found, buffer
	  //one character at a time to StringBuffer

        while (    ( nextChar_ != '=' )
                && ( nextChar_ != ':' ) //: follows Labels
                && ( nextChar_ != ';' ) //; may follow NULL value
                && ( nextChar_ != '{' )
                && ( nextChar_ != '}' )
                && ( nextChar_ != ' ' )
                && ( nextChar_ != '\n' )
                && ( nextChar_ != '\t' )
                && ( length < maxLength )
              )
        {
            buffer = buffer.append( (char)nextChar_ );
            length++ ;
            getNextChar();
        }

          //convert string buffer to string to maintain type compatibility
        value = buffer.toString();

        if (length < maxLength )
        { //no error because of exceeded max length for reserved word
            try
            {
                token = pgEnum_.getInt(value);
                return new Symbol(token); //valid symbol recognized.
            }
            catch (NoSuchElementException e)
            {
		  //value is not defined as a reserved word
		  //but length does not exceed max length
                return new ExtendedSymbol(ParserGeneratorEnumType.ERROR,
						"Unknown symbol " + value );
            }
        }
        else
        { //length exceeds max length for token
                return new ExtendedSymbol(ParserGeneratorEnumType.ERROR,
						"Symbol " + value+" too long");
        }

    }//end method reservedword


/*****************************************************************
 * Private Scanner Method used to read one character from InputStream
 *
 * <B>Modifies </B>Scanner variable nextChar_
 *
 * @exception ScannerException if there is an error in reading from InputStream
 * @see java.io.FileInputStream#read
**/

    private void getNextChar() throws ScannerException
    {
	try
	{
        	nextChar_ = inFile_.read();
          	//returns ascii code -as integer - of char read -
          	//returns a value of -1 if end of file is encountered.
	}
	catch (java.io.IOException e)
	{
	  throw new ScannerException("Scanner: Error in reading character from InputStream");
	}
    }//end method getNextChar


/**********************************************************************
 * Used to get the next valid symbol from the input. Takes one character at a time from InputStream, recognizes word and associates it with corresponding token, creates a new instance of type Symbol and returns it. When EOF character signifying end of input file is encountered, closes handle to file.
 *
 * <B>Modifies</B> scanner variables curLineNo_, nextChar_, inFile_
 *
 * @return An instance of type Symbol corresponding to next recognized input word.
 *
 * @exception ScannerException if unrecognized character encountered in input.
**/

    public Symbol getNextSymbol()
	throws ScannerException
    {

          //skip blanks and line feeds - which are not part of strings
        while (     ((char)nextChar_ == ' ')
                ||  ((char)nextChar_ == '\n')
                ||  ((char)nextChar_ == '\t')   )
        {
            if ( (char)nextChar_ == '\n')
			curLineNo_ ++; //increment line count

            getNextChar(); //read the next char from the input stream
        }


        if ( nextChar_ == -1 )
	{
		//end of file encountered - syntax for read()
		//close the infile being read and return last symbol
	    try
	    {
	    	inFile_.close();
            	return new Symbol(ParserGeneratorEnumType.EOF);
	    }
	    catch (java.io.IOException e)
	    {
		throw new ScannerException("Scanner: Cannot Close" +
		   " Input File Stream - Error \n");
	    }
	}
        else
        {
          //various cases for the input character

            switch( (char)nextChar_ ){

            // NULL recognized
                case '\0'    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.NULL);

            //respective braces recognized
                case '{'    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.LEFTCURLYBRACE);
                case '}'    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.RIGHTCURLYBRACE);
                case '['    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.LEFTSQUAREBRACE);
                case ']'    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.RIGHTSQUAREBRACE);
                case '('    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.LEFTROUNDBRACE);
                case ')'    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.RIGHTROUNDBRACE);


                case '='    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.EQUAL); //'='
                case ';'    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.SCOLON); //';'
                case ':'    : getNextChar(); //maintain definition of NextChar
                  return new Symbol(ParserGeneratorEnumType.COLON); //':'


                case '/'    : getNextChar(); //maintain definition of NextChar_
				//comment out - and return next symbol
                              comment();
				//comment is never returned to call -
                                //another call made to self and the next
				//non-comment symbol is returned.
                              return getNextSymbol();

                case '<'    : //getNextChar() called in func
                              //to maintain definition of NextChar_

                                //expected - filename
				//ending delimiter for filename is >
                              return getExtSymbol('>');

                case '\''    :   //ASCII code for ' is 39
                                //expected - Constant
				//ending delimiter for constant is '
                              return getExtSymbol('\'');

                case '"'    :   //ASCII code for "
                                //expected - String value
				//ending delimiter for string is "
                              return getExtSymbol('"');


                default     : //either reserved word or unidentified symbol
                              return reservedWord(256);
				//max length of reserved word assumed 256
            }//end of switch
        }//end else - nextchar is not EOF
    }//end of method getNextSymbol.

}//end class Scanner

