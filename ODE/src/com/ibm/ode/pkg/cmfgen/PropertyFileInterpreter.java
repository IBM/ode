/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:36:03
 * Date and Time File was extracted/checked out: 06/04/13 16:45:24
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

import java.util.*;
import java.io.*;

/**
 * This class is responsible for getting all the property names that apply
 * to a shippable file. It also responsible for returning the names of the
 * shippable files. This is a singleton. This class is not multi thread
 * safe.
 *
 * @author Anil Ambati
 * @version 1.1
 */
public final class PropertyFileInterpreter implements Constants
{
  /**
   * Reference to the singleton object
   */
  private static PropertyFileInterpreter instance = null;

  /**
   * This represents the properties read from the CMF_PROP_FILE. These
   * properties are used to generate the fileStanza elements.
   */
  private Properties props = null;

  /**
   * Returns the instance of this class. This method could return a null
   * if an instance of PropertyFileInterpreter could not be instantiated.
   * This method prints the stack trace if an exception is thrown while
   * creating an instance of PropertyFileInterpreter.
   *
   * @return instance of PropertyFileInterpreter.
   */
  public static PropertyFileInterpreter getInstance()
  {
    if (instance == null)
    {
      try
      {
        instance = new PropertyFileInterpreter();
        instance.prepPkgDataValues();
      }
      catch (IOException ioe)
      {
        ioe.printStackTrace();
      }
    }
    return instance;
  }

  /**
   * Protected constructor to make it a singleton
   *
   * @exception IOException if loading of property file fails
   */
  protected PropertyFileInterpreter()
    throws IOException
  {
    this.props = new Properties();
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    try
    {
      fis = new FileInputStream(CMF_PROP_FILE);
      bis = new BufferedInputStream(fis);
      this.props.load(bis);
    }
    finally
    {
      if (bis != null)
        bis.close();
      if (fis != null)
        fis.close();
    }
  }

  /**
   * Returns the Properties object that represents the properties read from the
   * properties file CMF_PROP_FILE.
   */
  public Properties getProperties()
  {
    return props;
  }

  /**
   * Returns a list of shippable file names. This method uses the values of ILIST,
   * EXTRA_ILIST and EXCLUDE_ILIST properties to come up with the list of shippable
   * file names.
   *
   * @return Vector that contains names of the shippable files
   */
  public Vector getShippableFiles()
  {
    String ilist = props.getProperty("ILIST");
    String extraIlist = props.getProperty("EXTRA_ILIST");
    String exilist = props.getProperty("EXCLUDE_ILIST");

    Vector listOfFiles = new Vector();
    StringTokenizer tokenizerObj = null;
    if (ilist != null)
    {
      tokenizerObj = new StringTokenizer(ilist);
      while (tokenizerObj.hasMoreTokens())
      {
        listOfFiles.addElement(tokenizerObj.nextToken());
      }
    }

    if (extraIlist != null)
    {
      tokenizerObj = new StringTokenizer(extraIlist);
      while (tokenizerObj.hasMoreTokens())
      {
        String extIlistEntry = (String)tokenizerObj.nextToken();
        handleDirectoriesAndLinks(extIlistEntry);
        listOfFiles.addElement(extIlistEntry);
      }
    }

    if (exilist != null)
    {
      tokenizerObj = new StringTokenizer(exilist);
      while (tokenizerObj.hasMoreTokens())
      {
        String curToken = tokenizerObj.nextToken();
        listOfFiles.removeElement(curToken);
      }
    }

    int length = listOfFiles.size();
    for (int idx = 0; idx < length; idx++)
    {
      Object curSrcFile = listOfFiles.elementAt(idx);
      String sourceFileKey =
       getFileSpecificPropName(curSrcFile.toString(),
                               getCorrespondingPropName(SOURCEFILE_XML_TAG_NAME));
      if (props.get(sourceFileKey) == null)
        props.put(sourceFileKey, curSrcFile);
    }
    return listOfFiles;
  }

  /**
   * Returns names of the properties that apply to all files and also the
   * properties that apply only to the file with specified name.
   *
   * @param fileName name of a valid shippable file
   */
  public Vector getFileProperties( String fileName )
  {
    Vector fileSpecProps = getFileSpecificPropertyNames(fileName);
    Vector commProps = getCommonPropertyNames();
    Enumeration enumer = commProps.elements();
    while (enumer.hasMoreElements())
    {
      fileSpecProps.addElement(enumer.nextElement());
    }
    return fileSpecProps;
  }

  /**
   * Returns the properties that only apply to the specified file name.
   * The names of the properties that apply only to a file start with the
   * its file name.
   *
   * @param fileName name of a valid shippable file
   * @return Vector that contains property names
   */
  private Vector getFileSpecificPropertyNames( String fileName )
  {
    Vector fileSpecificPropNames = new Vector();
    Enumeration enumer = props.propertyNames();
    while (enumer.hasMoreElements())
    {
      String curProp = (String)enumer.nextElement();
      if (curProp.indexOf(fileName) >= 0)
      {
        fileSpecificPropNames.addElement(curProp);
      }
    }
    return fileSpecificPropNames;
  }

  /**
   * Returns all the properties whose names start with PREFIX. These are the
   * properties that apply to all the files
   *
   * @return Vector that contains property names
   */
  private Vector getCommonPropertyNames()
  {
    Vector commonPropNames = new Vector();
    Enumeration enumer = props.propertyNames();
    while (enumer.hasMoreElements())
    {
      String curProp = (String)enumer.nextElement();
      if (curProp.startsWith(PROPERTY_PREFIX))
      {
        commonPropNames.addElement(curProp);
      }
    }
    return commonPropNames;
  }

  /**
   * Handles the symlinks and directories. This method is responsible for
   * putting right values for sourceDir and sourceFile when the shippable
   * file is a directory or link
   *
   * @param extIlistEntry an entry in the EXTRA_ILIST property
   */
  private void handleDirectoriesAndLinks( String extIlistEntry )
  {
    Element fileType =
      Element.newElement(FILETYPE_XML_TAG_NAME, extIlistEntry);
    String fileTypeVal = fileType.getValue();
    String sourceFileProp =
      getFileSpecificPropName(extIlistEntry,
                              getCorrespondingPropName(SOURCEFILE_XML_TAG_NAME));
    String sourceDirProp =
      getFileSpecificPropName(extIlistEntry,
                              getCorrespondingPropName(SOURCEDIR_XML_TAG_NAME));
    if (Element.isLink(fileTypeVal))
    {
      int idx = extIlistEntry.lastIndexOf(FILE_SEPARATOR);
      String sourceDir = extIlistEntry.substring(0, idx + 1);
      String sourceFile = extIlistEntry.substring(idx + 1);
      props.put(sourceFileProp, sourceFile);
      props.put(sourceDirProp, sourceDir);
    }
    else if (Element.isDirectory(fileTypeVal))
    {
      props.put(sourceFileProp, "");
      props.put(sourceDirProp, extIlistEntry);
    }
  }

  /**
   * Returns the value of the element/attribute with specified name. Return could
   * be a null value.
   *
   * @param name name of an element/attribute
   * @param parent name of a shippable file
   */
  public String getValue( String name, String parent )
  {
    String propName = PropertyFileInterpreter.getCorrespondingPropName(name);
    String fileSpecVal =
      props.getProperty(getFileSpecificPropName(parent, propName));
    String defVal = props.getProperty(propName);
    String retVal = null;
    if (fileSpecVal != null)
      retVal = fileSpecVal;
    else
      retVal = defVal;
    return retVal;
  }

  /**
   * This method checks the value of each property; if it finds two or more
   * package data separators then it inserts a space between two contigous
   * separators.
   */
  private void prepPkgDataValues()
  {
    Enumeration enumer = props.elements();
    Enumeration keys = props.keys();
    final String searchToken =
      PACKAGEDATA_SEPARATOR + PACKAGEDATA_SEPARATOR;

    while (enumer.hasMoreElements() &&
           keys.hasMoreElements())
    {
      String value = (String)enumer.nextElement();
      String propName = (String)keys.nextElement();
      if (value == null) continue;

      // Prepare the string for parsing with tokenizer. Put space if there is
      // no space between two continuos tokens.
      int idx = -1;
      boolean valChanged = false;
      while ((idx = value.indexOf(searchToken)) >= 0)
      {
        StringBuffer sb = new StringBuffer(value);
        sb = sb.insert(idx + 1, " ");
        value = sb.toString();
        valChanged = true;
      }
      if (valChanged)
        props.put(propName, value);
    }
  }

  /**
   * Returns the property name that corresponds to the specified
   * element/attribute name.
   *
   * @param name element/attribute name
   */
  public static String getCorrespondingPropName( String name )
  {
    char[] chars = name.toCharArray();
    StringBuffer propName =
      new StringBuffer(PROPERTY_PREFIX);
    int idx = 0;
    while (idx < chars.length)
    {
      if (Character.isUpperCase(chars[idx]) &&
          !Character.isUpperCase(chars[idx - 1]))
      {
        propName.append(WORD_SEPARATOR);
      }
      propName.append(chars[idx]);
      idx++;
    }
    return propName.toString().toUpperCase();
  }

  /**
   * Returns the file specific property for the specified file name and property.
   *
   * @param fileName name of a valid shippable file
   * @param propName name of a valid property
   */
  public static final String getFileSpecificPropName( String fileName, String propName )
  {
    StringBuffer sb = new StringBuffer(fileName);
    sb.append(WORD_SEPARATOR);
    sb.append(propName);
    return sb.toString();
  }
}
