/*******************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 ******************************************************************************/
package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;

/**
 * This class is responsible for parsing the CMF according to its structure
 * provided in the StanzaCollection, AttributeCollection and related classes.
 * Primary method which drives the parse is <a href="#parseCMF">parseCMF</a>.
 *
 * It validates the input as per this structure, and makes calls to the
 * Package and its sub objects, to populate parsed data.
 *
 * <pre>
 *   Parser P = new Parser(pgEnumType);
 *   P.parseCMF(packageRef, strPathPlusFileName);
 * </pre>
 */
public class Parser
{
  /**
   * Holds reference to an instance of Scanner, which is used by parser
   * to scan input. Private Parser Variable.
   * @see Scanner
   */
  private Scanner scanner_;

  /**
   * Stores the next Symbol to be Parsed. Private Parser Variable.
   * @see Symbol
   */
  private Symbol nextSym_;

  /**
   * Holds reference to an instance of StanzaCollection, which is used
   * by parser. Private Parser Variable.
   * @see StanzaCollection
   */
  private StanzaCollection stanzaCollection_;

  /**
   * Holds reference to an instance of ParserGeneratorEnumType, which is used by
   * parser. Private Parser Variable.
   * @see ParserGeneratorEnumType
   */
  private ParserGeneratorEnumType pgEnum_;

  /**
   * Holds reference to an instance of AttributeCollection, which is used by
   * parser. Private Parser Variable.
   * @see AttributeCollection
   */
  private AttributeCollection attributeCollection_;

  /**
   * Holds reference to an instance of Package, which is used by parser to
   * populate various entities as they are parsed. Private Parser Variable.
   * @see Package
   */
  private Package packageRef_;

  /********************************************************************
   * Used by parser to keep track of the nested stanza structure during parse.
   * Private Parser Variable.
  **/
  private int index_;

  /********************************************************************
   * Used to represent token corresponding to Parent Stanza to manage nested
   * stanza structure during start. The value always corresponds to a statically
   * defined integer corresponding to the stanza name, in
   * ParserGeneratorEnumType. This is initialized to START
   * @see ParserGeneratorEnumType
  **/
  private int curParentStanza_;

  /********************************************************************
   * Used to represent token corresponding to Current Stanza to manage nested
   * stanza structure during start. The value always corresponds to a statically
   * defined integer corresponding to the stanza name, in
   * ParserGeneratorEnumType.
   * This is initialized to NULL
   * @see ParserGeneratorEnumType
  **/
  private int curStanza_;

  /********************************************************************
   * Creates a new instance of Parser and initializes it. It creates new
   * instances of supporting classes StanzaCollection, AttributeCollection
   * required for this action.
   *
   * @param pGEnumBase Instance of ParserGeneratorEnumType.
   *
   * @exception ParserException if an error occurs in Creating or initializing
   * the parser.
   *
   * @see AttributeCollection
   * @see StanzaCollection
   * @see ParserGeneratorEnumType
  **/
  public Parser( ParserGeneratorEnumType pGEnumBase )
    throws ParserException
  {
    try
    {
      this.pgEnum_ = pGEnumBase;
      stanzaCollection_ = new StanzaCollection( pGEnumBase );
      attributeCollection_ = new AttributeCollection( pGEnumBase );


    } //end try block
    catch( StanzaCollectionException e )
    {
      throw new ParserException(e.toString() );
    }
    catch(Exception e)
    {
      // Any other exception. Ensures only exception of type ParserException
      //originates from within the Parser
      throw new ParserException( "Parser: \n" +e.toString() );
    }
  }//end constructor



  /********************************************************************
   * This method checks whether a particular string value has any characters
   * if doen't have any it returns true
   * used while trying to populate the package objects
   * if the value is empty populate is not called : BP-784
   *
   * Private Parser Method
  **/
  static private boolean checkIfValueIsEmpty( String value )
  {
    if( value.trim().length() == 0 )
      return true;
    else
      return false;
  }

  /********************************************************************
   * Parses input for a ReqType object, creates a new instance and populates it.
   * This method is called when the parser expects to get input for object of
   * type ReqType
   * Private Parser Method.
   *
   * @exception ParserException if input does not confirm to syntax of ReqType
   * object, or an error occurs in creating or initializing the object.
  **/
  private ReqType getReqType()
    throws ParserException
  {
    ReqType myReqType_;
    int i = 1; //temp vbl. - reqtype expects 1-2-3 - so init to 1

    //check if nextSym_ corresponds to '('
    if (nextSym_.getToken() != ParserGeneratorEnumType.LEFTROUNDBRACE)
    {
      throw new ParserException("Parser: Invalid input - '(' expected "
                 + "at line " + scanner_.getCurLineNo() );
    }

    //instantiate the ReqType object
    myReqType_ = new ReqType();
    getNextCheckedSymbol();

    while ( i <= 3 )
    {

      if (nextSym_ instanceof Symbol)
      {
        if ( ((ExtendedSymbol)nextSym_).getType()
                                                   != pgEnum_.getInt("String") )
        {
           throw new ParserException("Parser: Invalid data type at "
                              + scanner_.getCurLineNo() + " : String Expected");
        }
        else
        {
          //setOneValue sets value and returns boolean if successful
          if ( myReqType_.setOneValue( i,
                                ((ExtendedSymbol)nextSym_).getValue() ) != true)
          {
             throw new ParserException("Parser: Could not set value no "
                     + i + " in ReqType value at " + scanner_.getCurLineNo() );
          }
        }

        getNextCheckedSymbol();

        //check if closing delimiter ')' is reached.
        if (nextSym_.getToken() == ParserGeneratorEnumType.RIGHTROUNDBRACE)
        {
          break; //from outer while loop
        }

      }
      else
      {
        throw new ParserException("Parser: Value expected at"
                + scanner_.getCurLineNo() );
      }

      i++;

    }//end while

    //check ending delimiter to be ')' - redundant if < 3 vals specified
    //necessary if 3 (or more) vals specified in reqtype object
    if (nextSym_.getToken() != ParserGeneratorEnumType.RIGHTROUNDBRACE)
    {
        throw new ParserException("Parser: Invalid input for ReqType.  "+
                    "only 3 items can be specified for a ReqType"
                + scanner_.getCurLineNo() );
    }

    //the next call to getNextCheckedSymbol may need to be deleted.
    getNextCheckedSymbol();

    return myReqType_;

  }//ends method getReqTyppe


  /**
   * Main method which drives the parse routine after Parser is instantiated.
   * Initializes some parser variables as required.
   *
   * @param argPackage an Instance of Package which will be populated during the
   * parse.
   * @param strFilename The Path + Filename, as a string, from which the CMF
   * input is to be read for parsing.
   *
   * return Boolean, true if CMF is parsed successfully, false otherwise.
   *
   *
   * @exception ParserException if an unrecognized character, word or token is
   * encountered in input, or incorrect file name given as input, or there is an
   * error in reading from the specified file.
   * @exception PackageException if an error occurs in populating one or more
   * attributes in the Package (or its sub) objects.
   *
   * <p>
   * <p>
   * <p>
   * <B>See Also:</B> Sequentially calls <a href = "#evaluateEntities">evaluateEntities</a>, once for each base level entity, until all entities are parsed, or an error is encountered.
   */
   public boolean parseCMF(Package argPackage, String strFilename)
  throws ParserException , PackageException
   {
    try
    {
        this.packageRef_ = argPackage;
        if (strFilename.equals("") )
            scanner_ = new Scanner();
        else
            scanner_ = new Scanner(strFilename , this.pgEnum_ );

        curParentStanza_ = ParserGeneratorEnumType.START; //needed
        index_ = 0;
        curStanza_ = ParserGeneratorEnumType.NULL; //needed
            //this is NULL for now, and will be assigned on the fly
            //as the need be - imperative to be null for logic
            //to match in the package object.

        getNextCheckedSymbol(); //initializes nextSym_

  //Begin of File char may be treated as a null in some cases
  //this is the first symbol in CMF - if null, we may ignore it
  if (nextSym_.getToken() == ParserGeneratorEnumType.NULL)
    getNextCheckedSymbol();

  //at this point, nextSym definitely holds the first non-null
  //  symbol of CMF


        //at least one entity required - thus do-while
        //blank file will result in error - else use while do
        do
        {
             //evalEntities evaluates one entity at a time
            if ( evaluateEntities() != true)
            {
                throw new ParserException("Parser: Error at line "
                    + scanner_.getCurLineNo() + " in CMF");
            }
        } while( nextSym_.getToken() != ParserGeneratorEnumType.EOF );

        //successful parse
        return true;
    }
    catch(java.io.FileNotFoundException e)
    {
        throw new ParserException("Parser: "+ e.toString() );
    }
    catch(java.io.IOException e)
    {
        throw new ParserException("Parser: "+ e.toString() );
    }
    catch(ScannerException e)
    {
        throw new ParserException(e.toString() );
    }

   }//end of method parseCMF



/********************************************************************
 * Evaluates one <I>Base Level</I> entity - i.e., one that occurs as a child of Virtual Stanza <I>START</I> in StanzaCollection - at a time. This entity can be a single stanza entity or a multi-stanza entity.
 *
 * Private Parser Method
 *
 *
 * @exception ParserException if stanza name encountered is an invalid stanza name at the current place in the CMF
 * @exception PackageException if there is an error in populating one or more attributes of the Package, or its sub-objects, as this entity is parsed.
 *
 * @see StanzaCollection
 * @see AttributeCollection
 * @see #evaluateMultiStanza
 * @see #evaluateSingleStanzas
**/
  private boolean evaluateEntities()
    throws ParserException , PackageException
  {
    int tempindex = 0;
    int temptoken = nextSym_.getToken();
    if ( stanzaCollection_.validateChildStanzaName(temptoken,index_)==false)

    {
      // LVW If stanza name is invalid, check to see if token is an
      // attribute of sourceData or
      // packageData stanzas.  If so, evaluate as single stanza
      // using specific class.

      if ( curParentStanza_ != ParserGeneratorEnumType.FILE)
      {
        throw new ParserException("Parser: Invalid stanza name at line "
                         + scanner_.getCurLineNo() + " in CMF");
      }

      else if ( ( temptoken != ParserGeneratorEnumType.SOURCEFILE ) &&
              ( temptoken != ParserGeneratorEnumType.SOURCEDIR ) &&
              ( temptoken != ParserGeneratorEnumType.PARTNUM ) &&
              ( temptoken != ParserGeneratorEnumType.FILETYPE ) &&
              ( temptoken != ParserGeneratorEnumType.FILEDIRECTIVES ) &&
              ( temptoken != ParserGeneratorEnumType.TARGETFILE ) &&
              ( temptoken != ParserGeneratorEnumType.TARGETDIR ) &&
              ( temptoken != ParserGeneratorEnumType.DISTLIB ) &&
              ( temptoken != ParserGeneratorEnumType.PERMISSIONS ) &&
              ( temptoken != ParserGeneratorEnumType.USERID ) &&
              ( temptoken != ParserGeneratorEnumType.GROUPID ) &&
              ( temptoken != ParserGeneratorEnumType.MAJORDEVNUM ) &&
              ( temptoken != ParserGeneratorEnumType.MINORDEVNUM ) &&
              ( temptoken != ParserGeneratorEnumType.FLAGS ) &&
              ( temptoken != ParserGeneratorEnumType.COMP ) &&
              ( temptoken != ParserGeneratorEnumType.SHIPTYPE ) &&
              ( temptoken != ParserGeneratorEnumType.PARTINFO ) &&
              ( temptoken != ParserGeneratorEnumType.VPLSECURITY ) &&
              ( temptoken != ParserGeneratorEnumType.VPLPARTQUAL ) &&
              ( temptoken != ParserGeneratorEnumType.HFSCOPYTYPE) &&
              ( temptoken != ParserGeneratorEnumType.EXTATTR) &&
              ( temptoken != ParserGeneratorEnumType.HFSALIAS) &&
              ( temptoken != ParserGeneratorEnumType.PDSALIAS) &&
              ( temptoken != ParserGeneratorEnumType.SETCODE) &&
              ( temptoken != ParserGeneratorEnumType.ENTRY) &&
              ( temptoken != ParserGeneratorEnumType.JCLINMODE) &&
              ( temptoken != ParserGeneratorEnumType.INCLUDE) &&
              ( temptoken != ParserGeneratorEnumType.LKEDTO) &&
              ( temptoken != ParserGeneratorEnumType.LKEDRC) &&
              ( temptoken != ParserGeneratorEnumType.HFSLKEDNAME) &&
              ( temptoken != ParserGeneratorEnumType.PDSLKEDNAME) &&
              ( temptoken != ParserGeneratorEnumType.JCLINLKEDPARMS) &&
              ( temptoken != ParserGeneratorEnumType.SYSLIBS) &&
              ( temptoken != ParserGeneratorEnumType.ORDER) &&
              ( temptoken != ParserGeneratorEnumType.LKEDPARMS) &&
              ( temptoken != ParserGeneratorEnumType.LIBRARYDD) &&
              ( temptoken != ParserGeneratorEnumType.SYSLIBS_LIBRARYDD) &&
              ( temptoken != ParserGeneratorEnumType.LKEDCOND) &&
              ( temptoken != ParserGeneratorEnumType.SIDEDECKAPPENDDD))

          throw new ParserException("Parser: Invalid stanza name at line "+
                                     scanner_.getCurLineNo() + " in CMF");
      else
      {
        evaluatePkgDataStanza(temptoken);
        return true;
      }
    }


    tempindex = stanzaCollection_.validateAndGetIndex( temptoken );

    if ( tempindex >= 0 )
    {
        //implies a MultiStanza has been found
        evaluateMultiStanza(tempindex);
        return true;
    }
    else
    {
        tempindex = attributeCollection_.validateAndGetIndex( temptoken );
        if ( tempindex >= 0 )
        {
            evaluateSingleStanzas(tempindex);
            return true;
        }
        else
        {
            throw new ParserException("Parser: Unknown Stanza name at line "
                + scanner_.getCurLineNo() + " in the CMF");
        }
    }
  }//end method evaluateEntities



/********************************************************************
 * This method is called when any stanza in the CMF, which contains one or more sub-stanzas, is encountered in the Parse. It evaluates one such stanza.
 * Private Parser Method
 *
 * @param argindex index of current Multi-Stanza in CMF structure
 *
 * @exception ParserException if stanza name encountered is an invalid stanza name at the current place in the CMF
 * @exception PackageException if there is an error in populating one or more attributes of the Package, or its sub-objects, as this entity is parsed.
 *
 * @see StanzaCollection
 * @see AttributeCollection
 * @see Package#openBrace
 * @see Package#closeBrace
**/
  private void evaluateMultiStanza(int argindex)
  throws ParserException , PackageException
  {
    int temptoken = nextSym_.getToken();

    if (stanzaCollection_.checkParent(argindex, curParentStanza_)==false)
    {
        //incorrectly specified stanzaCollection
        throw new ParserException("StanzaCollection: Child-Parent " +
            "relationship incorrectly specified for stanzaname at index "
            + argindex + ":: Error trapped in Parser.evaluateMultiStanza");
    }
    else
    {
        this.index_ = argindex ;
        this.curParentStanza_ = temptoken;
  this.curStanza_ = ParserGeneratorEnumType.NULL ;
    //necessary for pkg logic
    }
    //a valid Multistanza name encountered

    getNextCheckedSymbol();

    if ( nextSym_.getToken() != ParserGeneratorEnumType.LEFTCURLYBRACE )
    {
        throw new ParserException("Parser: '{' expected at line " +
            scanner_.getCurLineNo() + " in CMF");
    }

    //intimate the package about start of a new multistanza
    packageRef_.openBrace(curParentStanza_ , curStanza_ );

    getNextCheckedSymbol();

    do
    {
        evaluateEntities();
    }while( nextSym_.getToken() != ParserGeneratorEnumType.RIGHTCURLYBRACE );

    //intimate package about end of a multistanza
    packageRef_.closeBrace(curParentStanza_ , curStanza_ );

    //reset parser variables
    this.index_ = stanzaCollection_.getParentIndex(this.index_);
    curParentStanza_ = stanzaCollection_.getToken(this.index_);

    this.curStanza_ = ParserGeneratorEnumType.NULL ;
  //this is not necessary , but is done to preserve consistency
  //may remove this statement without affecting logic, if needed

    getNextCheckedSymbol();

  }//end method evaluateMultiStanza


/********************************************************************
 * This method is called when any stanza in the CMF, which contains only one or more attributes, and no sub-stanzas, is encountered in the Parse. It evaluates one such stanza.
 * Private Parser Method
 *
 * @param argindex index of current Multi-Stanza in CMF structure
 *
 * @exception ParserException if stanza name encountered is an invalid stanza name at the current place in the CMF
 * @exception PackageException if there is an error in populating one or more attributes of the Package, or its sub-objects, as this entity is parsed.
 *
 * @see StanzaCollection
 * @see AttributeCollection
 * @see Package#openBrace
 * @see Package#populate
 * @see Package#closeBrace
**/
  private void evaluateSingleStanzas(int argindex)
  throws ParserException , PackageException
  {
    AttributeObject myAttributeObject;
    ReqType myReqType;
    int temptype;
    int temptoken;

    this.curStanza_ = nextSym_.getToken();

    myAttributeObject=attributeCollection_.getRefToAttributeObject(argindex);

    getNextCheckedSymbol();

    if ( nextSym_.getToken() != ParserGeneratorEnumType.LEFTCURLYBRACE )
    {
      throw new ParserException("Parser: '{' expected at line " +
      scanner_.getCurLineNo() + " in CMF");
    }

    //intimate package of start of new stanza
    packageRef_.openBrace(curParentStanza_ , curStanza_ );

    getNextCheckedSymbol(); //attrib name expected in this call

    // empty stanza would not break the parse - depends on specific gen
    // if a required param is absent.
    while (nextSym_.getToken() != ParserGeneratorEnumType.RIGHTCURLYBRACE)
    {
      temptoken = nextSym_.getToken(); //store this temporarily
      temptype = myAttributeObject.validateAndGetType(temptoken);

      // takes care of invalid tokens like ; or = after a ;
      if (temptype == -1)
      {
        throw new ParserException("Parser: Unexpected token at line " +
                                  scanner_.getCurLineNo() + " in the CMF");
      }

      getNextCheckedSymbol(); // '=' expected
      if (nextSym_.getToken() !=  ParserGeneratorEnumType.EQUAL)
      {
        throw new ParserException("Parser: '=' expected at line " +
                                  scanner_.getCurLineNo() + " in CMF");
      }

      getNextCheckedSymbol(); // value expected-'['or'('orExtSym
      if (nextSym_.getToken() != ParserGeneratorEnumType.SCOLON)
      {
        if (nextSym_.getToken()==ParserGeneratorEnumType.LEFTSQUAREBRACE )
        {
          // list value encountered
          populateList(temptoken, temptype);
        }
        else
          if ( nextSym_.getToken()==ParserGeneratorEnumType.LEFTROUNDBRACE )
          {
            if (temptype != pgEnum_.getInt("ReqType") )
            {
              throw new ParserException("Parser: ReqType not expected " +
                "at line " + scanner_.getCurLineNo() + " in CMF");
            }
            else
            {
              myReqType = getReqType();
              
              // populate this reqtype value
              if( checkIfValueIsEmpty( myReqType.getType() )
              && checkIfValueIsEmpty( myReqType.getValue() )
              && checkIfValueIsEmpty( myReqType.getDescription() ))
              {
                // if all are empty do not populate
              }
              else
              {
                packageRef_.populate(curParentStanza_, curStanza_,
                                     temptoken, temptype, myReqType);
              }
            }
          }
          else if (nextSym_ instanceof ExtendedSymbol)
          {
            if ( temptype == ( (ExtendedSymbol)nextSym_).getType()
                       || temptype == pgEnum_.getInt("PGSpecialType") )
          {
            //if special type encountered
            //no type check at parser end needed
            //Or type must match expected type
            if( !checkIfValueIsEmpty( ((ExtendedSymbol)nextSym_).getValue() ) )
            {
              packageRef_.populate( curParentStanza_, curStanza_, temptoken,
              ( (ExtendedSymbol)nextSym_).getType(),
              ((ExtendedSymbol)nextSym_).getValue() );
            }

            getNextCheckedSymbol(); // ';' expected
            //in other cases, definition of nextSym to be
            //maintained in the respective methods.
          }
          else
          {
            throw new ParserException("Parser: Invalid datatype1 " +
              "or value at line " + scanner_.getCurLineNo() + " in the CMF\n" +
              ((ExtendedSymbol)nextSym_).getValue() + "\n");
          }
        }
        else if (nextSym_.getToken() == ParserGeneratorEnumType.NULL)
        {
          //skip populating this attribute altogether.
          //populate call skipped deliberately
          getNextCheckedSymbol(); //; expected
        }
        else
        {
          throw new ParserException("Parser: Value expected at line  "
          + " " + scanner_.getCurLineNo() + " in the CMF");
        } //current token populated
          //at this point, nextSym has to hold token for ; for valid input

        if (nextSym_.getToken() != ParserGeneratorEnumType.SCOLON)
        {
          throw new ParserException("Parser: ';' expected at line " +
                                    scanner_.getCurLineNo() + " in CMF");
        }
      }
      else
      {
        //do nothing : value is not specified for that attribute
      }
      getNextCheckedSymbol();//'}' or another singlestanza name expected
    };

    //intimate package of end of this stanza
    packageRef_.closeBrace(curParentStanza_, curStanza_);

    this.curStanza_ = ParserGeneratorEnumType.NULL;

    getNextCheckedSymbol(); // '}' got in prev call - end of stanza - get
                            //name of next stanza in same level, or '}'
                            //to signify end of def of parent stanza
  }//end method evaluateSingleStanzas

  /*****************************************************************************
   * LVW New method only called when an attribute is encountered in a file
   * stanza instead of a stanza name.
  **/
  private void evaluatePkgDataStanza( int argindex )
      throws ParserException, PackageException
  {
    ReqType myReqType;
    int temptype;
    int temptoken;
    int tempindex;
    int attribname;
    int pkgtoken = ParserGeneratorEnumType.PACKAGEDATA;
    String srcdir = null;
    String srcfile = null;
    String filetype = null;
    boolean isDirFileType = false;
    boolean isSymlinkFileType = false;

    this.curStanza_ = ParserGeneratorEnumType.PACKAGEDATA;
    int pkgindex = attributeCollection_.validateAndGetIndex(pkgtoken);
    AttributeObject myAttributeObject = 
      attributeCollection_.getRefToAttributeObject(pkgindex);
    packageRef_.openBrace(curParentStanza_, curStanza_);

    while (nextSym_.getToken() != ParserGeneratorEnumType.RIGHTCURLYBRACE)
    {
      temptoken = nextSym_.getToken();
      temptype = myAttributeObject.validateAndGetType(temptoken);
      
      // takes care of invalid tokens like ; or = after a ;
      // conditions for SOURCEDIR and SOURCEFILE are provided
      // because their temptypes are -1
      // as per the logic and we dont want to treat them as invalid tokens
      if ((temptoken != ParserGeneratorEnumType.SOURCEDIR) &&
          (temptoken != ParserGeneratorEnumType.SOURCEFILE) && (temptype == -1))
      {
        throw new ParserException("Parser: Unexpected token at line " +
            scanner_.getCurLineNo() + " in your CMF");
      }

      getNextCheckedSymbol(); // '=' expected
      if (nextSym_.getToken() !=  ParserGeneratorEnumType.EQUAL)
      {
        throw new ParserException("Parser: '=' expected at line " +
                                  scanner_.getCurLineNo() + " in CMF");
      }
      
      getNextCheckedSymbol(); // value expected-'['or'('orExtSym
      if (nextSym_.getToken() != ParserGeneratorEnumType.SCOLON)
      {
        if (nextSym_.getToken() == ParserGeneratorEnumType.LEFTSQUAREBRACE)
        {
          // list value encountered
          populateList(temptoken, temptype);
        }
        else
        {
          if (nextSym_.getToken() == ParserGeneratorEnumType.LEFTROUNDBRACE)
          {
            if (temptype != pgEnum_.getInt("ReqType"))
            {
              throw new ParserException("Parser: ReqType not expected "
                        + "at line " + scanner_.getCurLineNo() + " in CMF");
            }
            else
            {
              myReqType = getReqType();

              // populate this reqtype value
              if (checkIfValueIsEmpty(myReqType.getType()) &&
                  checkIfValueIsEmpty(myReqType.getValue()) &&
                  checkIfValueIsEmpty(myReqType.getDescription()))
              {
                // if all are empty do not populate
              }
              else
              {
                packageRef_.populate(curParentStanza_,
                                    curStanza_,
                                    temptoken,
                                    temptype,
                                    myReqType);

              }
            }
          }
          else if (nextSym_ instanceof ExtendedSymbol)
          {
            // If nextSym_ is SourceData attribute, store value to be
            // populated later.
            if (temptoken == ParserGeneratorEnumType.FILETYPE)
            {
                filetype = ((ExtendedSymbol)nextSym_).getValue();
                if (filetype.equalsIgnoreCase("d") ||
                    filetype.equalsIgnoreCase("dir"))
                  isDirFileType = true;
                else if (filetype.equalsIgnoreCase("s") ||
                    filetype.equalsIgnoreCase("symlink"))
                  isSymlinkFileType = true;
            }
            
            // If nextSym_ is SourceData attribute, store value to be
            // populated later.
            if ( temptype == ( (ExtendedSymbol)nextSym_).getType()
              || temptype == pgEnum_.getInt("PGSpecialType")
              || temptoken == ParserGeneratorEnumType.SOURCEFILE
              || temptoken == ParserGeneratorEnumType.SOURCEDIR )
            {
              if ( !checkIfValueIsEmpty(((ExtendedSymbol)nextSym_).getValue()) )
              {
                if ( temptype < 0 )
                {
                  if ( temptoken == ParserGeneratorEnumType.SOURCEFILE )
                  {
                    srcfile = ((ExtendedSymbol)nextSym_).getValue();
                  }
                  else if ( temptoken == ParserGeneratorEnumType.SOURCEDIR )
                  {
                    srcdir = ((ExtendedSymbol)nextSym_).getValue();
                  }
                }
                else
                {
                  packageRef_.populate( curParentStanza_,
                                      curStanza_,
                                      temptoken ,
                                      ((ExtendedSymbol)nextSym_).getType(),
                                      ((ExtendedSymbol)nextSym_).getValue() );
                }
              }
              getNextCheckedSymbol();
            }
            // ';' expected
            else
            {
              throw new ParserException("Parser: Invalid datatype "
                                + "or value at line " + scanner_.getCurLineNo()
                                + "in the CMF");
            }
          }
          else if (nextSym_.getToken() == ParserGeneratorEnumType.NULL)
          {
            //skip populating this attribute altogether.
            //populate call skipped deliberately
            getNextCheckedSymbol(); //; expected
          }
          else
          {
            throw new ParserException("Parser: Value expected at line  "
                  + " " + scanner_.getCurLineNo() + " in the CMF");
          } //current token populated
        }
        
        // at this point, nextSym has to hold token for ; for valid input
        if ( nextSym_.getToken() !=  ParserGeneratorEnumType.SCOLON )
        {
          throw new ParserException("Parser: ';' expected at line " +
                         scanner_.getCurLineNo() + " in CMF");
        }
        else
        {
          //do nothing : value is not specified for that attribute
        }
        
        // Right Curly Brace or another single stanza name expected
        getNextCheckedSymbol();
      }  
    } //end while
    
    if (!isSymlinkFileType && !isDirFileType && 
        (srcdir == null || srcfile == null))
    {  
      // Each packageData stanza must have a sourceData stanza specified.
      throw new ParserException("Parser: No sourceDir or sourceFile " +
              "defined at line " + scanner_.getCurLineNo() + " in CMF.\n" +
              "A PackageData Stanza is not valid without a " +
              "SourceData Stanza specified.");
    }
    
    if ((isSymlinkFileType || isDirFileType) && srcdir == null)
    {
      //  if its a symlink, only sourceDir required here
      throw new ParserException("Parser: No sourceDir defined for the file " +
           "stanza ending at the line " + scanner_.getCurLineNo() + " in CMF.");
    }
    
    // Close packageData stanza
    packageRef_.closeBrace(curParentStanza_ , curStanza_);

    // Set values explicitly to populate sourceFile and sourceDir
    temptype = ParserGeneratorEnumType.STRING;
    this.curParentStanza_ = ParserGeneratorEnumType.FILE;
    this.curStanza_ = ParserGeneratorEnumType.SOURCEDATA;
    attribname = ParserGeneratorEnumType.SOURCEFILE;

    // Open sourceData stanza.
    packageRef_.openBrace(curParentStanza_, curStanza_);
    packageRef_.populate(curParentStanza_, curStanza_,
                         attribname, temptype, srcfile);
    attribname = ParserGeneratorEnumType.SOURCEDIR;
    packageRef_.populate(curParentStanza_, curStanza_,
                         attribname, temptype, srcdir);
    
    // Close sourceData stanza.
    packageRef_.closeBrace(curParentStanza_, curStanza_);

    this.curStanza_ = ParserGeneratorEnumType.NULL;

  } // end method EvaluatePkgDataStanza



  /********************************************************************
   * This method is called to request the Scanner for the next Symbol
   * to be parsed in the input and validates it.
   * Private Parser Method
   *
   * @exception ParserException if an invalid word is encountered in the
   * CMF, i.e., if a Scanner error occurs.
   *
   * @see Scanner#getNextSymbol
  **/
  private void getNextCheckedSymbol()
   throws ParserException

  {
  String unrecogToken;
        try
        {
            nextSym_ = scanner_.getNextSymbol();

            //check if it is a label.
            //check if ERROR SYMBOL is returned

            if ( (nextSym_ instanceof ExtendedSymbol) &&
                 ( ( (ExtendedSymbol)nextSym_).getType()
        == ParserGeneratorEnumType.ERROR) )
            {

    unrecogToken = ((ExtendedSymbol)nextSym_).getValue();
                //if colon follows, it is a comment, else error

                nextSym_ = scanner_.getNextSymbol();
                if ( nextSym_.getToken() == ParserGeneratorEnumType.COLON)
                {
                    //yes, it is a label - get the next symbol
                    getNextCheckedSymbol();
                }
                else
                {
                    //it is an error
                   throw new ParserException("Parser: " + unrecogToken +
      " at line " + scanner_.getCurLineNo() +
      " in the CMF" );
                }
            }
        }
        catch (ScannerException e)
        {
            throw new ParserException(e.toString() );
        }
    }//ends method getNextCheckedSymbol


  /********************************************************************
 * Called when the Left-List-Delimiter ([) is encountered in the input. It creates an instance of a list (array), populates values into it as received from the input, and returns when the Right-List-Delimiter is encountered or an error is detected.
   *
   * This method is used to populate one list attribute.
   * Private Parser Method.
   *
   * @param curtoken Token corresponding to the current attribute being parsed.
   * @param expectedType Token corresponding to the data type expected for the current attribute, as per CMF definition specified.
   *
   * @ParserException if an incorrect input is received from the CMF
   * @PackageException if an error occurs in populating a list object in Package or its sub objects
   *
   * @see Package#populate
   * @see Various @AttribObject Classes, one for each stanza containing attributes.
  **/
  private void populateList(int curtoken, int expectedType)
  throws ParserException, PackageException
  {
    ArrayList listArray = new ArrayList();
    ReqType tempReqType;
    int temptype = 0;
    String tempvalue;

    try
    {
      //check if nextSym holds token for '['

      if ( nextSym_.getToken() != ParserGeneratorEnumType.LEFTSQUAREBRACE )
      {
        throw new ParserException("Parser: Invalid Input - '[' expected"
                          + " at line " + scanner_.getCurLineNo() + " in CMF");
      }
      getNextCheckedSymbol();

      //switch based on the expected type of list elements
      switch(expectedType)
      {
        case ParserGeneratorEnumType.LISTOFSTRING:
          //empty list is not an error
          while(nextSym_.getToken()!=ParserGeneratorEnumType.RIGHTSQUAREBRACE)
          {
            if ( nextSym_ instanceof ExtendedSymbol)
            {
              temptype = ( (ExtendedSymbol)nextSym_).getType();
              if ( temptype != pgEnum_.getInt("String") )
              {
                throw new ParserException("Parser: Invalid type " +
                        "specified at line " + scanner_.getCurLineNo() +
                        " in CMF");
              }

              if( !checkIfValueIsEmpty( ((ExtendedSymbol)nextSym_).getValue()) )
              {
              listArray.add(new String( ((ExtendedSymbol)nextSym_).getValue()));
              }
              getNextCheckedSymbol();
            }//end then if instanceof
            else
            {
              throw new ParserException("Parser: A value Symbol expected"
                          + "within List at line " + scanner_.getCurLineNo() );
            }//end else if instanceof

          }//end while
          break;

        case ParserGeneratorEnumType.LISTOFFILENAMEDATATYPE:
          //empty list is not an error
          while(nextSym_.getToken()!=ParserGeneratorEnumType.RIGHTSQUAREBRACE)
          {
            if ( nextSym_ instanceof ExtendedSymbol)
            {
              temptype = ( (ExtendedSymbol)nextSym_).getType();
              if ( temptype != pgEnum_.getInt("FilenameDataType") )
              {
                throw new ParserException("Parser: Invalid type specified"
                             + "at line" + scanner_.getCurLineNo() + " in CMF");
              }
              if( !checkIfValueIsEmpty(((ExtendedSymbol)nextSym_).getValue()) )
               listArray.add(new String(((ExtendedSymbol)nextSym_).getValue()));
              getNextCheckedSymbol();
            }//end then if instanceof
            else
            {
              throw new ParserException("Parser: A value Symbol expected"
                          + "within List at line " + scanner_.getCurLineNo() );
            }//end else if instanceof
          }//end while
          break;

        case ParserGeneratorEnumType.LISTOFCONSTANT:
          //empty list is not an error
          while(nextSym_.getToken()!=ParserGeneratorEnumType.RIGHTSQUAREBRACE)
          {
            if ( nextSym_ instanceof ExtendedSymbol)
            {
              temptype = ( (ExtendedSymbol)nextSym_).getType();
              if ( temptype != pgEnum_.getInt("Constant") )
              {
                throw new ParserException("Parser: Invalid type specified"
                        + "at line" + scanner_.getCurLineNo() + " in CMF");
              }
              if( !checkIfValueIsEmpty(((ExtendedSymbol)nextSym_).getValue()) )
               listArray.add(new String(((ExtendedSymbol)nextSym_).getValue()));
              getNextCheckedSymbol();
            }//end then if instanceof
            else
            {
              throw new ParserException("Parser: A value Symbol expected"
                        + "within List at line " + scanner_.getCurLineNo() );
            }//end else if instanceof
          }//end while
          break;

        case ParserGeneratorEnumType.LISTOFREQTYPE:

          //empty list is not an error
          while(nextSym_.getToken()!=ParserGeneratorEnumType.RIGHTSQUAREBRACE)
          {
            //getReqType takes care of potential errors
            //in each ReqType occurence
            //the [ preceding the list is accounted for and getNextCheckedSym
            //called before the switch - so '(' expected to be nextSym_
            tempReqType = getReqType();

            //comes here only if no error, and tempReqType holds valid ReqType
            //object - else exception is thrown from within getReqType
            if( checkIfValueIsEmpty( tempReqType.getType() )
               && checkIfValueIsEmpty( tempReqType.getValue() )
               && checkIfValueIsEmpty( tempReqType.getDescription() ) )
            {
              // if all are empty in the ReqType do not add it to the array
            }
            else
            {
              listArray.add(new ReqType(tempReqType) );
            }
            //copy constructor of ReqType called
            // getNextCheckedSymbol() not required here coz it is taken
            // care of in getReqType

          }//end while
          break;

        case ParserGeneratorEnumType.LISTOFPGSPECIALTYPE:
          //this shall not have any type checks - the value will
          while ( nextSym_ instanceof ExtendedSymbol )
          {
            temptype = ( (ExtendedSymbol)nextSym_).getType();
            tempvalue = ( (ExtendedSymbol)nextSym_).getValue();
            if( !checkIfValueIsEmpty( tempvalue ) )             //BP-784
            {
              listArray.add(new PgSpecialType(temptype, tempvalue) );
            }
            getNextCheckedSymbol();
          };

          if ( nextSym_.getToken() != ParserGeneratorEnumType.RIGHTSQUAREBRACE)
          {
            throw new ParserException("Parser: ']' expected at line " +
                          scanner_.getCurLineNo() + " in CMF, at end of List");
          }
          break;
        default:
          throw new ParserException("Parser: Type mismatch for value"
                    + " specified at line "+ scanner_.getCurLineNo() +
                      " in the CMF.\n" );
      }//end switch based on expectedtype

      //populate the whole list-Array into the package.
      //type of Array passed as ListOfString
      //expected type holds correct type for this call
      if (listArray.size() != 0)
      {
        packageRef_.populate(curParentStanza_,
                             curStanza_,
                             curtoken,
                             expectedType,
                             listArray);
      }
      getNextCheckedSymbol();

    }//end try block
    catch (ClassCastException ex)
    {
      throw new ParserException("Parser: Invalid input at line " +
      scanner_.getCurLineNo() + " in CMF, List not populated in Package.");
    }
    catch (Exception ex)
    {
      throw new ParserException(ex.toString() + "Parser: List at line " +
      scanner_.getCurLineNo() + " in CMF, not populated in Package.");
    }//end catch block
  }//end method populateList
}//end class Parser
