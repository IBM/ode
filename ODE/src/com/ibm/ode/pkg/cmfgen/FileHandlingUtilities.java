/*******************************************************************************
 *
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:19
 * Date and Time File was extracted/checked out: 06/04/13 %T
 ******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.io.*;
import java.util.Vector;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Enumeration;

/**
 * This class is responsible for handling all the file related operations.
 * It returns a list of sub-directories to be searched for metadata files,
 * merges ProductInfo and file stanza info to create the CMF.
 *
 * @author Kiran Lingutla
 * @version 1.1
 */

public class FileHandlingUtilities
{
  //Holds a list of directories already travelled while recursing
  private Vector travelledDirectories;

  /**
   * Constructor
   *
   */
  public FileHandlingUtilities()
  {
    travelledDirectories = new Vector();
  }
  
  /**
   * Recurses in all the sub directories under FILESTANZA_METADATA_DIR using the
   * Depth First Search (DFS) algorithm
   *
   * @return A vector with full paths to all the subdirectories
   */
  public Vector getAllSubDirs()
  {
    String metaDataDir = VariablesAndMessageHandler.METADATA_DIR;
    if (metaDataDir.endsWith( File.separator ))
      metaDataDir = metaDataDir.substring( 0, metaDataDir.length() );
    File objectDir = new File( metaDataDir );
    Stack dirList = new Stack();
    Vector finalDirList = new Vector();
    dirList.push( objectDir );
    if (dirList.empty())
      return null;
    while (true)
    {
      if (dirList.empty())
        break;
      // get the first element in the stack with out removing it
      File dirContent = (File)dirList.peek();
      if (dirContent.isDirectory())
      {
        boolean containsDirectory = false;
        String[] dirs = dirContent.list();
        if (dirs.length == 0)
        {
          // it is an empty directory, so do not include it and also remove it
          // from the stack so as to go to the next element
          dirList.pop();    
          continue;
        }
        else
        {
          for (int i=0; i<dirs.length; i++)
          {
            String fullPath = dirContent + File.separator + dirs[i];
            // mark that this directory is already checked to prevent from
            // checking this path again. If this path is already checked,
            // skip it and check the next value
            if (isPathMarked( fullPath ))
            {
              continue;
            }
            else
            {
              markPath( fullPath );
            }
            File tempFile = new File( fullPath );
            if (tempFile.isDirectory())
            {
              // it is a sub directory, so add it to the stack 
              dirList.push( tempFile );
              containsDirectory = true;
            }
          }
          if (!containsDirectory)
          {
            // this sub directory contains only files and no directories
            // this means that it is the last level in this particular path
            // hence it can be added to the final list
            dirList.pop();
            finalDirList.addElement(dirContent);
          }
          continue;
        }
      }
      else
      {
        // This entry is a file, so skip it
        dirList.pop();
        continue;
      }
    }
    return finalDirList;
  }

  /**
   * Checks if a path is already travelled
   *
   * @param A string representing the path to be checked
   *
   * @return true if travelled and false otherwise
   */
  private boolean isPathMarked( String path )
  {
    if (travelledDirectories.contains( path ))
      return true;
    else
      return false;
  }

  /**
   * Marks that a path is already travelled
   *
   * @param A string representing the path to be marked
   */
  private void markPath( String path )
  {
    travelledDirectories.addElement( path );
  }

  /**
   * Creates the CMF by merging the info about the product and the file stanzas
   *
   * @param A StringBuffer holding the info about all the file stanzas
   */

  public void createCMF( StringBuffer fileStanzasBuffer )
  {
    try 
    {
      String cmfProductFile = VariablesAndMessageHandler.CMF_PRODUCT_FILE;
      String cmfFile = VariablesAndMessageHandler.CMF_FILE;
      StringBuffer productInfo = new StringBuffer(); 
      String inputLine;
      String childFilesEntry = "";
      BufferedReader productFile = new BufferedReader( 
                                            new FileReader( cmfProductFile ) );
      BufferedWriter outputCMFFile = new BufferedWriter(
                                                   new FileWriter( cmfFile ) );
      System.out.println("Reading the product file " + cmfProductFile);
      while ((inputLine = productFile.readLine()) != null)
        productInfo.append( inputLine + "\n" );
      System.out.println("Done reading the product file");
      //replace the stubs in "immChildFiles" of product info with the 
      //coresponding child files
      String finalProductInfo = replaceImmChildFilesStubs( 
                                                     productInfo.toString() );
     productFile.close();
     System.out.println("Creating the CMF " + cmfFile);
     outputCMFFile.write( finalProductInfo );
     outputCMFFile.newLine();
     outputCMFFile.newLine();
     outputCMFFile.write( fileStanzasBuffer.toString() );
     outputCMFFile.close();
     System.out.println("Done creating the CMF");
    }
    catch( IOException e )
    {
      VariablesAndMessageHandler.printException( e, "FileHandlingUtilities",
               "An IO exception occured while reading the product file or " +
               "generating the CMF" );
    }
    catch( Exception e )
    {
      VariablesAndMessageHandler.printException( e, "FileHandlingUtilities",
               "An exception occured while reading the product file or " +
               "generating the CMF" );
    }
  }

  /**
   * Replaces the stubs in "immChildFiles" of product info with the 
   * corresponding child files
   *
   * @param A string containing all the product information
   *
   * @return A string with the stubs in the product information replaced
   *         with corresponding child files
   */
  private String replaceImmChildFilesStubs( String productInfo )
  {
    Enumeration enumer = MetaDataParser.ParentToChildMapping.keys();
    String key;
    Vector value;
    String formattedChildList;
    String tempString1;
    String tempString2;
    int index;
    while (enumer.hasMoreElements())
    {
      key = (String)enumer.nextElement();
      value = (Vector)MetaDataParser.ParentToChildMapping.get( key );
      formattedChildList = formatChildFiles( value );
      index = productInfo.indexOf( "%" + key + "%" );
      if (index == -1)
        continue;
      //split productInfo into two at the tag, replace the tag and join the
      //three parts
      tempString1 = productInfo.substring( 0, index );
      tempString2 = productInfo.substring( index+key.length()+2 );
      productInfo = tempString1.concat( 
                                   formattedChildList.concat( tempString2 ) );
    }
    return productInfo;
  }

  /**
   * Formats a list of child files
   *
   * @param A vector containing the child files to be formatted
   *
   * @return A string with the formatted values
   */
  private String formatChildFiles( Vector value )
  {
    String file = "\n";
    for (int i=0; i<value.size(); i++)
    {
      for (int j=0; j<20; j++)
        file = file.concat( " " );
      file = file.concat( "<" + value.elementAt(i) + ">\n" );
    }
    return file;
  }

}
    
