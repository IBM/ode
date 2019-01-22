/*******************************************************************************
 *
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:41
 * Date and Time File was extracted/checked out: 06/04/13 16:45:21
 ******************************************************************************/

package com.ibm.ode.pkg.cmfgen;

import java.io.IOException;
import java.io.File;
import java.util.Vector;
import java.util.Hashtable;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * This represents the XML parser used to parse all the metadata files
 *
 * @author Kiran Lingutla
 * @version 1.1
 */

public class MetaDataParser {

  public XMLReader parser;
  public MetaDataContentHandler contentHandler;
  public static String pathToParser = "org.apache.xerces.parsers.SAXParser";
  //holds the mapping of each entityName to all its children
  public static Hashtable ParentToChildMapping = new Hashtable();
  //holds the information about all the file stanzas in the CMF format
  public StringBuffer fileStanzasBuffer;

  /**
   * Constructor
   */
  public MetaDataParser()
  {
    try {
      //this is the SAX parser used to parse all the metadata files
      parser = XMLReaderFactory.createXMLReader( pathToParser );
      parser.setFeature("http://xml.org/sax/features/validation", true );
      parser.setFeature("http://apache.org/xml/features/validation/schema", true );
      //create a content handler and attach it to the parser for content
      //and error handling
      contentHandler = new MetaDataContentHandler();
      parser.setContentHandler(contentHandler);
      parser.setErrorHandler(contentHandler);
      fileStanzasBuffer = new StringBuffer();
    }
    catch (Exception e) 
    {
      VariablesAndMessageHandler.printException( e, "MetaDataParser",
                                           "Error while creating the Parser" );
    }
  }

  /**
   * Parses all the metadata files and returns the information in the
   * CMF format in a StringBuffer
   */
  public StringBuffer parseFiles(Vector dirList)
  {
    for (int i=0; i<dirList.size(); i++)
    {
      String uri = dirList.elementAt(i).toString() + File.separator + 
                   VariablesAndMessageHandler.METADATA_FILE;
      File metafile = new File( uri );
      if (metafile.exists())
      {
        try {
          System.out.println("Parsing metadata file " + uri );
          parser.parse( uri );
          System.out.println("Finished parsing metadata file " + uri );
          //append the information from each metadata file
          fileStanzasBuffer.append(contentHandler.metaDataBuffer.toString());

        }
        catch (IOException e) {
          VariablesAndMessageHandler.printException( e, "MetaDataParser", 
                             "An error occured while trying " +
                             "to read the file: " + uri );
        }
        catch (SAXException e) {
          VariablesAndMessageHandler.printException( e, "MetaDataParser", 
                                        "An error occured during parsing");
        }
        catch (Exception e) {
          VariablesAndMessageHandler.printException(e, "MetaDataParser",
                                         "MetaDataParser: Error occured");
        }
      }
      else
       continue;
    }
    System.out.println("Finished parsing all the metadata files");
    return fileStanzasBuffer;
  }

}
