/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:23
 * Date and Time File was extracted/checked out: 06/04/13 16:45:17
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.util.*;
import java.io.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is the top level class that drives the creation of file stanza
 * metadata files. Each metadata file contain a bunch of <fileStanza> elements
 *
 * @author Anil Ambati
 * @version 1.1
 */
public class FileMetadata extends XMLElement
  implements Constants
{
  /**
   * This contains the list of shippable files
   */
  private Vector shippableFiles = null;

  /**
   * Constructor
   *
   * @param attributes attributes of this XML element
   */
  public FileMetadata( Hashtable attributes )
  {
    PropertyFileInterpreter propFileIntObj =
      PropertyFileInterpreter.getInstance();
    setAttributes(attributes);
    shippableFiles = propFileIntObj.getShippableFiles();
  }

  /**
   * Returns XML form of this object. Never returns a null.
   */
  public String toXML()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(getBeginTag()).append(NEW_LINE);
    for (int idx = 0; idx < shippableFiles.size(); idx++)
    {
      FileStanza fileStanzaObj =
        new FileStanza((String)shippableFiles.elementAt(idx));
      fileStanzaObj.setIndentPosition(getIndentPosition() + 1);
      sb.append(fileStanzaObj.toXML()).append(NEW_LINE);
    }
    sb.append(getEndTag());
    return sb.toString();
  }

  /**
   * This method is responsible for generating the metadata file
   * that contains <fileStanza> elements
   *
   * @exception IOException if an error occurrs while creating the file
   */
  public void generateMetadataFile()
    throws IOException
  {
    FileWriter fw = null;
    BufferedWriter bw = null;
    try
    {
      String xmlForm = this.toXML();
      fw = new FileWriter(FILESTANZA_METADATA_URI);
      bw = new BufferedWriter(fw);
      bw.write("<?xml version=\"1.0\"?>");
      bw.write(NEW_LINE);
      bw.write(xmlForm);
    }
    finally
    {
      if (bw != null)
        bw.close();
      if (fw != null)
        fw.close();
    }
  }

  /**
   * Returns true if there are any shippable files to process else prints a
   * message and returns false.
   */
  public boolean isGenerationRequired()
  {
    if (this.shippableFiles.size() == 0)
    {
      StringBuffer msg =
        new StringBuffer("There are no shippable files to process. Please define");
      msg.append(" either the ILIST or the EXTRA_ILIST makefile variable.");
      msg.append(" The '");
      msg.append(FILESTANZA_METADATA_URI);
      msg.append("' file is not generated.");
      System.out.println(msg);
      return false;
    }
    return true;
  }

  /**
   * Parse the schema and extract the subelements and attributes of fileStanza,
   * packageData and partInfo elements. Here are we are making assumptions of the
   * structure of the schema and resultant XML file. After this method is executed,
   * the myElements and myAttributes vectors of FileStanza, PackageData and PartInfo
   * classes are populated.
   */
  public void parseSchema()
  {
    try
    {
      XMLReader parser =
        (XMLReader)Class.forName(PARSER_CLASS_NAME).newInstance();
      parser.setFeature("http://xml.org/sax/features/validation",
                        false);
      parser.setFeature("http://apache.org/xml/features/validation/schema",
                        false);
      FileMetadataHandler saxHandler = new FileMetadataHandler();
      parser.setContentHandler(saxHandler);
      parser.parse(FILESTANZA_SCHEMA_URI);
    }
    catch (Exception ex)
    {
      // catch all exceptions that are risen as part of parsing the schema.
      // we will just put up a warning message and continue.
      ex.printStackTrace(System.err);
    }
  }

  /**
   * Check if required properties are defined or not. If any of the required
   * properties are not defined then return false.
   */
  public static boolean checkRequiredInfo()
  {
    String msg =
      "The following required makefile variables have not been set:";
    StringBuffer notSet = new StringBuffer();
    if (Constants.FILESTANZA_SCHEMA_URI == null)
    {
      notSet.append(FILESTANZA_SCHEMA_URI_STR).append(NEW_LINE);
    }
    if (Constants.CMF_PROP_FILE == null)
    {
      notSet.append(CMF_PROP_FILE_STR).append(NEW_LINE);
    }
    if (Constants.FILESTANZA_METADATA_URI == null)
    {
      notSet.append(FILESTANZA_METADATA_URI_STR).append(NEW_LINE);
    }
    if (notSet.length() == 0)
      return true;
    else
    {
      StringBuffer errMessage = new StringBuffer(msg);
      errMessage.append(NEW_LINE).append(notSet);
      System.out.println(errMessage);
      return false;
    }
  }

  /**
   * main method. Execution starts here.
   *
   * @param args arguments to this program
   */
  public static final void main( String[] args )
  {
    try
    {
      if (!FileMetadata.checkRequiredInfo())
        System.exit(-1);

      // Here it is assumed that getInstance method prints the stack trace
      // while creating an instance of PropertyFileInterpreter. Also,
      // getInstance method is called for the first time here.
      if (PropertyFileInterpreter.getInstance() == null)
      {
        StringBuffer msg = new StringBuffer();
        msg.append("Unable to load properties from the file: ");
        msg.append(CMF_PROP_FILE).append(NEW_LINE);
        msg.append("due to the above exception. ");
        msg.append("Please make sure the properties file exists.");
        System.out.println(msg);
        System.exit(-1);
      }

      Hashtable attributes = new Hashtable();
      attributes.put("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
      attributes.put("xsi:noNamespaceSchemaLocation", FILESTANZA_SCHEMA_URI);
      FileMetadata metaObj = new FileMetadata(attributes);
      if (metaObj.isGenerationRequired())
      {
        metaObj.parseSchema();
        metaObj.generateMetadataFile();
      }
      else
        System.exit(0);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.exit(-1);
    }
  }

  /**
   * This class is responsible for capturing the subelements and attributes
   * of fileStanza, packageData and partInfo as the schema is parsed and
   * and events are generated.
   *
   * @see org.xml.sax.helpers.DefaultHandler
   */
  class FileMetadataHandler extends DefaultHandler
  {
    boolean startFileStanza = false;
    boolean startPartInfo = false;
    boolean startPkgData = false;

    /**
     * Overrides the startElement method of DefaultHandler. This method will
     * get called by the SAX parser when an element is read from the XML
     * document it is parsing.
     */
    public void startElement( String uri, String localName,
                              String name, Attributes attrs )
    {
      if (name.indexOf("complexType") >= 0)
      {
        String nameOfType = attrs.getValue("name");
        if (nameOfType.equalsIgnoreCase("packageDataType"))
          startPkgData = true;
        else if (nameOfType.equalsIgnoreCase("partInfoType"))
          startPartInfo = true;
        else if (nameOfType.equalsIgnoreCase("fileStanzaType"))
          startFileStanza = true;
      }
      if (name.indexOf("element") >= 0)
      {
        if (startPkgData)
          PackageData.mySubElements.addElement(attrs.getValue("name"));
        if (startPartInfo)
          PartInfoElement.mySubElements.addElement(attrs.getValue("name"));
        if (startFileStanza)
          FileStanza.mySubElements.addElement(attrs.getValue("name"));
      }
      if (name.indexOf("attribute") >= 0)
      {
        if (startFileStanza)
          FileStanza.myAttributes.addElement(attrs.getValue("name"));
      }
    }

    /**
     * Overrides the endElement method of DefaultHandler. This method will
     * get called by the SAX parser when the end tag of an element is read
     * from an XML document.
     */
    public void endElement( String uri,
                            String localName, String name )
    {
      if (name.indexOf("complexType") >= 0)
      {
        if (startPkgData)
          startPkgData = false;
        if (startPartInfo)
          startPartInfo = false;
        if (startFileStanza)
          startFileStanza = false;
      }
    }
  }
}
