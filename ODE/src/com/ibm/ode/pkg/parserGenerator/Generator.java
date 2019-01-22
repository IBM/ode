//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;

import java.io.*;
import java.util.*;
import com.ibm.ode.lib.string.PlatformConstants;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.pkg.parserGenerator.service.TreeHandler;
import com.ibm.ode.pkg.parserGenerator.service.LSTFormatter;
import com.ibm.ode.pkg.parserGenerator.service.MkinstallNodeHandler;
import com.ibm.ode.pkg.parserGenerator.service.B390NodeHandler;

/**
 * This class is responsible for instantiating the appropriate generator
 * based on the target platform and the install tool specified by
 * the user. It also provides utility methods to create and write to files.
 * @version     1.25 98/01/22
 * @author      Prem Bala
 **/

public class Generator  extends java.lang.Object
{
  protected String shipRootDir_;
  protected String pkgControlDir_;
  protected String pkgTool_;
  protected String context_;
  protected String pkgType_;
  protected String pkgClass_;
  protected String apar_;
  protected String pkgFixStrategy_;

  /*****************************************************************************
   * Constructor for Generator ( to be called by the specific generators )
   * @param          shipRootDir :- Env Var => root dir. of the ship tree.
   * @param          context     :- Env Var => Context
   * @param          pkgControlDir  :- Env Var :- Output directory
   **/
  public Generator( String shipRootDir,
                    String context,
                    String pkgControlDir,
                    String pkgType,
                    String pkgClass,
                    String pkgFixStrategy,
                    String apar)
{
  shipRootDir_    = shipRootDir.trim();
  context_        = context.trim();
  pkgControlDir_  = pkgControlDir.trim();
  pkgType_        = pkgType.trim();
  pkgClass_       = pkgClass.trim();
  if( apar != null )
  {
    apar_           = apar.trim().toUpperCase();
  }
  if( pkgFixStrategy != null )
  {
    pkgFixStrategy_ = pkgFixStrategy.trim();
  }
}
  /*****************************************************************************
   * Constructor for Generator ( to be called by the specific generators )
   * @param          shipRootDir :- Env Var => root dir. of the ship tree.
   * @param          context     :- Env Var => Context
   * @param          pkgControlDir  :- Env Var :- Output directory
   **/
  public Generator( String shipRootDir,
                    String context,
                    String pkgControlDir,
                    String pkgType,
                    String pkgClass,
                    String pkgFixStrategy)
{
  shipRootDir_    = shipRootDir.trim();
  context_        = context.trim();
  pkgControlDir_  = pkgControlDir.trim();
  pkgType_        = pkgType.trim();
  pkgClass_       = pkgClass.trim();
  apar_           = null;
  if( pkgFixStrategy != null )
  {
    pkgFixStrategy_ = pkgFixStrategy.trim();
  }
}


  public Generator()
{
  // does nothing
}

// A function which specific generators will override.
public void generateTargetMetadataFiles( EntityTreeRoot entityTreeRoot,
    Package packageObject ) throws GeneratorException
{
}

/**
 * Instantiates and Invokes the appropriate generator based on the platform
 * and installTool
 *
 * @param entityTreeRoot refernce to the object which holds the structured IE
 * @param packageObject  refernce to the object which contain the IE's and FE's
 * @exception GeneratorException if any error is encountered
 */
public void invokeTargetGenerator( EntityTreeRoot entityTreeRoot,
                                   Package packageObject ) 
   throws GeneratorException
{
   Generator generator = null;
   Generator serviceGenerator = null;
   String pkgtool = ParserGeneratorInitiator.getPackagingTool();
   String pkgtool_ver = ParserGeneratorInitiator.getPackagingToolVersion();
   String warning_msg;

   if (pkgtool == null) // no package tool?  shouldn't ever happen!
      pkgtool = "none";

   warning_msg = "Unsupported machine/pkgtool combination:\n" + "   CONTEXT=" + 
                  context_ + "\n" + "   pkgtool=" + pkgtool;

   if (pkgtool.equalsIgnoreCase(ParserGeneratorInitiator._mkinstallTool_))
   {
      if (!ParserGeneratorInitiator._mkinstallToolMachines_.contains(context_))
      {
         Interface.printWarning( warning_msg ); 
      }
      generator = new MkinstallGenerator(shipRootDir_, context_, pkgControlDir_,
                                         pkgType_, pkgClass_, pkgFixStrategy_);
   }
   else if (pkgtool.equalsIgnoreCase(ParserGeneratorInitiator._swpackageTool_))
   {
      if (!ParserGeneratorInitiator._swpackageToolMachines_.contains(context_))
         Interface.printWarning( warning_msg );
      generator = new SwpackageGenerator(shipRootDir_, context_, pkgControlDir_,
                                         pkgType_, pkgClass_, pkgFixStrategy_);
   }
   else if (pkgtool.equalsIgnoreCase( ParserGeneratorInitiator._pkgmkTool_ ))
   {
      if (!ParserGeneratorInitiator._pkgmkToolMachines_.contains( context_ ))
         Interface.printWarning( warning_msg );
      generator = new PkgmkGenerator(shipRootDir_, context_, pkgControlDir_,
                                     pkgType_, pkgClass_, pkgFixStrategy_);
   }
   else if (pkgtool.equalsIgnoreCase( ParserGeneratorInitiator._mvsTool_ ))
   {
      if (!ParserGeneratorInitiator._mvsToolMachines_.contains( context_ ))
      {
         Interface.printWarning( warning_msg );
      }   
      generator = new MVSGenerator(shipRootDir_, context_, pkgControlDir_,
                                   pkgType_, pkgClass_, pkgFixStrategy_);
      if (pkgClass_.equalsIgnoreCase(ParserGeneratorInitiator._servicePkg_))
      {
         serviceGenerator = new TreeHandler(
            new B390NodeHandler(LSTFormatter.toStageFilter),
            shipRootDir_, context_, pkgControlDir_,
            pkgType_, pkgClass_, pkgFixStrategy_, apar_);
      }
   } 
   else if (pkgtool.equalsIgnoreCase( ParserGeneratorInitiator._rpmTool_ ))
   {
      if (!ParserGeneratorInitiator._rpmToolMachines_.contains( context_ ))
         Interface.printWarning( warning_msg );
      generator = new RPMGenerator(shipRootDir_, context_, pkgControlDir_,
                                   pkgType_, pkgClass_, pkgFixStrategy_);
   }
   else
   {
      throw new GeneratorException( "GeneratorException : "
        + "Invalid parameters passed to the Generator. \n"
        + "CONTEXT = " + context_ + "\n"
        + "pkgtool = " + pkgTool_ + "\n" );
   }
  
   generator.generateTargetMetadataFiles( entityTreeRoot, packageObject );
   if (serviceGenerator != null)
   {
      serviceGenerator.generateTargetMetadataFiles(entityTreeRoot, 
                                                   packageObject);
   }
}

  /******************************************************************************
   * Utility method provided for open a file . All the error checks are done here.
   * @param String :- fileName to be opened
   * @return FileOuptputStream :- file handle to the newly opened file.
   * @exception GeneratorException :- If error is encountered while opening the file
   **/
public FileOutputStream openFile(String fileName)
             throws GeneratorException
{
  try
  {
    File outputFile = new File(fileName);
    FileOutputStream fos = new FileOutputStream(outputFile);

    return fos;
  }
  catch (SecurityException e)
  {
    throw new GeneratorException("Generator: File " + fileName +
                                 " cannot be opened for Writing.\n"+
                                 "\tDirectory Specified for creating file may not exist.\n" );
  }
  catch (IOException e)
  {
    throw new GeneratorException("Generator: File " + fileName +
                                 " cannot be opened for Writing.\n"+
                                 "\tDirectory Specified for creating file may not exist.\n" );
  }

}//open file with filename as arg ends

/******************************************************************************
 * Utility method provided for open a file . All the error checks are done here.
 * @param  String :- full Path of the directory where the file needs to be opened
 * @param String :- fileName to be opened
 * @return FileOuptputStream :- file handle to the newly opened file.
 * @exception GeneratorException :- If error is encountered while opening the file
   **/
public FileOutputStream openFile(String path, String fileName)
                          throws GeneratorException
{
  try
  {
    File outputFile = new File(path, fileName);
    FileOutputStream fos = new FileOutputStream(outputFile);

    return fos;
  }
  catch (SecurityException e)
  {
    throw new GeneratorException("Generator: File " + fileName +
                                 " cannot be opened for Writing.\n"+
                                 "\tDirectory Specified for creating file may not exist.\n" );
     }
  catch (IOException e)
  {
    throw new GeneratorException("Generator: File " + fileName +
                                 " cannot be opened for Writing.\n"+
                                 "\tDirectory Specified for creating file may not exist.\n" );
  }

}//open file with path and filename as arg ends

/******************************************************************************
 * Utility method provided for  closing file . All the error checks are done here.
 * @param FileOuptputStream :- file handle to the file to be closed.
 * @return boolean :- return true if successful
 * @exception GeneratorException :- If error is encountered.
 **/
public boolean closeFile(FileOutputStream outfile)
         throws GeneratorException
{
  try
  {
    if( !PlatformConstants.isMvsMachine(context_) )
    {
      // for MVS don't write it out since the apar data is
      // messed up
      outfile.write(10); //newline
    }
    outfile.close();
    return true;
  }//end try block
  catch (IOException e)
  {
    throw new GeneratorException("Generator: I/O error " +
                                 "occurred while attempting to close output file. "
                                 +"File may be corrupted.\n");
  }//end catch block
}//end method closeFile

/******************************************************************************
 * Utility method provided for  writing a string to a File
 * @param FileOuptputStream :- file handle to the file where the String
 *                             needs to be written out.
 * @param String :- String message to be written to the file.
 * @return void
 * @exception GeneratorException :- If error is encountered.
 **/
public void writeString(FileOutputStream fos, String message)
  throws GeneratorException
{
  try
  {
    fos.write(message.getBytes() );

  }//end try
  catch (IOException e)
  {
    throw new GeneratorException("Generator.writeString(): " +
                                 "An I/O error occured while writing to file. Output file" +
                                 " does not exist or does not have write permissions.\n" );
  }
}//end method writeString


// D-1469 related changes start here
/************************************************************************
******
* Utility method provided for inserting the platform specific file
*  separator. Pass two strings, check if separator exists at end of
*  first or begin of second string, and concatenate them with exactly
*  one instance of the separator between them. Return the resultant
*  string
* @param String :- Left part of filepath/filename
* @param String :- Right part of filepath/filename
* @return String:- Left String+ Separator+ Right String - (separator
*                   at point of join exactly once.
* @exception GeneratorException :- If error is encountered.
**/

public String insertFileSeparator(String leftStr, String rightStr)
  throws GeneratorException
{
  String separator = ParserGeneratorInitiator._fileSeparator_ ;
  String result ;

        result = leftStr;
        if (result.endsWith(separator) == false)
        {
    result = result.concat(separator);
        }

        //here, result has leftString with exactly one occurence of
        //separator at end.

        if (rightStr.startsWith(separator) == true)
        {
    rightStr = rightStr.substring(separator.length());
                //return substring of orig rightStr begin at char #
                //= length of separator (string char #'s 0-(len-1)

                //basicaly clips off initial separator string frm rightStr
        }
        //at this point, right string is devoid of the leading separator
        //sequence of chars.

        result = result.concat(rightStr);

        return result;

}//ends method insertFileSeparator
// D-1469 related changes end here

//KS F1584 related changes start here

/************************************************************************
*****
* public void String formatStringAsText formats a input string based on
* maxLength per line
* @param  inputString string to be formatted
* @param  maxLengthPerLine - a integer value
* <pre>
* usage:
*   Generator.formatStringAsText( inputString, maxLengthPerLine );
* </pre>
**/

//Source of method - COM.ibm.sdwb.bps.persistence.Apar.formatStringAsText()

public String formatStringAsText( String inputString, int maxLengthPerLine )
{
  int     index = 0;
  String  shortLine = null;
  String  bigLine = null;
  String  outputString = "";
  int     strLength; //getting the total length of the string
  int     endIndex = 0;
  int     wordLength = 0;
  int     changeLength = 0;
  int     blankCounter = 0;
  int     totalLength = 0;

  strLength = inputString.length();
  StringTokenizer wordTokens = new StringTokenizer( inputString );
  int numOfWordsInString = wordTokens.countTokens();
  String word = null;

  boolean incrementFlag = true;
  while ( wordTokens.hasMoreTokens() )
  {
    if ( incrementFlag )
    {
      word = (String) wordTokens.nextToken();
      index = inputString.indexOf( word );
      endIndex = word.length() + index;
      wordLength = word.length();
    }
    totalLength = changeLength + wordLength + blankCounter;
    bigLine = inputString.substring( index, endIndex );

    if ( wordLength > maxLengthPerLine )
    {
      incrementFlag = true;
      if ( wordLength >  changeLength + blankCounter )
      {
        outputString += '\n' ;
      }
      while ( wordLength > maxLengthPerLine )
      {
        shortLine = bigLine.substring( 1, maxLengthPerLine );
        outputString += shortLine;
        outputString += '\n';
        bigLine = bigLine.substring(maxLengthPerLine + 1);
        wordLength = bigLine.length();
      }
      outputString += bigLine;
    }

    if ( ( maxLengthPerLine - totalLength ) < 0 )
    {
      changeLength = 0;
      blankCounter = 0;
      incrementFlag = false;        // to take care of the wrapped word
      outputString += '\n';
    }

    if ( ( maxLengthPerLine - totalLength ) >= 0 )
    {
      incrementFlag = true;
      changeLength += wordLength;
      outputString += word;
      outputString += ' ';
      blankCounter++;
    }
  }
  return outputString;
}


//KS F1584 related changes end here


}//end of class Generator
