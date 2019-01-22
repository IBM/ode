/********************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 2002.  All Rights Reserved.
 *
 * Version: 1.1
 *
 * Date and Time File was last checked in: 5/10/03 00:35:08
 * Date and Time File was extracted/checked out: 06/04/13 16:45:15
 *******************************************************************************/
package com.ibm.ode.pkg.cmfgen;

/**
 * A class that holds all the constants used during the metadata creation
 *
 * @author Anil Ambati
 * @version 1.1
 */
public interface Constants
{
  public static final String FILESTANZA_SCHEMA_URI_STR =
    "FILESTANZA_SCHEMA_URI";
  public static final String FILESTANZA_METADATA_URI_STR =
    "FILESTANZA_METADATA_URI";
  public static final String CMF_PROP_FILE_STR =
    "CMF_PROP_FILE";

  public static final String FILESTANZA_SCHEMA_URI =
    System.getProperty(FILESTANZA_SCHEMA_URI_STR);
  public static final String CMF_PROP_FILE =
    System.getProperty(CMF_PROP_FILE_STR);
  public static final String FILESTANZA_METADATA_URI =
    System.getProperty(FILESTANZA_METADATA_URI_STR);

  public static final String NEW_LINE =
    System.getProperty("line.separator");
  public static final String FILE_SEPARATOR =
    System.getProperty("file.separator");

  public static final String PACKAGEDATA_SEPARATOR = "|";
  public static final String WORD_SEPARATOR = "_";
  public static final String PROPERTY_PREFIX =
    "CMF" + WORD_SEPARATOR;
  public static final String PARTINFO_PROP_PREFIX =
    "part_info" + WORD_SEPARATOR;
  public static final int INDENT_SPACE = 2;
  public static final String PARSER_CLASS_NAME =
    "org.apache.xerces.parsers.SAXParser";

  public static final String FILETYPE_XML_TAG_NAME = "fileType";
  public static final String SOURCEFILE_XML_TAG_NAME = "sourceFile";
  public static final String SOURCEDIR_XML_TAG_NAME = "sourceDir";
  public static final String TARGETFILE_XML_TAG_NAME = "targetFile";
  public static final String TARGETDIR_XML_TAG_NAME = "targetDir";
}
