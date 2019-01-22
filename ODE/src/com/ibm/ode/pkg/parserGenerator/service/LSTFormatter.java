package com.ibm.ode.pkg.parserGenerator.service;

import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import java.io.File;
/**********************************************************************
 * This class is responsible handling the MVS plaform specific differences 
 * in the generation of the list of file to be packaged. 
 * 
 * File stanzas may represent files, links, or directories. 
 * System    file   link   directory
 * Solaris    f       s       d
 * hp         F       S       D 
 * nt         -       -       -
 * AIX        F       S       D 
 * MVS        mod/hfs mod/hfs -
 * 
 * The output file format is 
 * CLASS.MOD    HFSFILENAME
 * 
 * Where: 
 *    CLASS = Build/390 UNIMODC part class - which for us is the CMF 
 *            fileType, 
 *    MOD = the short name of the part in the UNIMODC - which for us 
 *            is the CMF targetFile, 
 *    HFSFILENAME = the full path and filename of the built part so 
 *            that Build/390 can copy it to the VSAM bulk log
 *
 * @see FileListGenerator
 * @see FileListCustomizer
 *
 * @version 0.1 9/19/2000
 * @author Bruce Gibbons
**/
public abstract class LSTFormatter extends Formatter
{
  int filter=toStageFilter;        // Set the default to no filter. 
  /**********************************************************************
   * Private filter variable, used to indicate the filtering to be used 
   * when generating the output. 
   **/
  public static final int noFilter=0;      // Do not filter. List all files in the cmf.
  public static final int toStageFilter=1; // Only list the files listed in the cmf that
                                           // exist in the toStage directory.
  /**********************************************************************
   * This method sets the filter to the type passed as a parameter. 
   * @param int filter The type of filtering to be used when generating output. 
   **/
  public LSTFormatter (NodeHandler nodeHandler, int filter)
    throws GeneratorException
  {
     super(nodeHandler);
     setFilter(filter);
  }

  /**********************************************************************
   * This method sets the filter. 
   * @param int filter The filter.
   * @throws GeneratorException
   **/
  private void setFilter(int filter)
    throws GeneratorException
  {
    if (filter<noFilter||noFilter>toStageFilter) {
      throw new GeneratorException("B390LSTFormatter: Unknown filter type. "+filter);
    }
    this.filter=filter;
  }

   /**********************************************************************
   * This method determines if the file will be included in the output 
   * based on the PackageData and FileEntity data. It selects the
   * type of filtering set when the FileListCustomizer was created. 
   * @param curFE FileEntity the current file entity. 
   * @param curPD PackageData the current package data. 
   * @returns boolean true if the file entity should be filtered false otherwise. 
   * @throws GeneratorException
   **/
  public boolean filtered() throws GeneratorException 
  {
    switch (this.filter) 
    {
      case LSTFormatter.noFilter:
      {
        return false;
      }
      case LSTFormatter.toStageFilter:
      {
        return toStageFilter();
      }
      default: {
  	     throw new GeneratorException("LSTFormatter: Unknown filter type " +
                                       this.filter);
      }
    } 
  }

  /**********************************************************************
   * This method performs "toStage" filtering. It filters the file stanza if
   * the file does not exist in the toStage directory. 
   * @param curFE FileEntity the current file entity. 
   * @returns boolean true if the file entity should be filtered false otherwise. 
   * @throws GeneratorException
   **/
  private boolean toStageFilter() 
         throws GeneratorException 
  {
    String sourceFile=nodeHandler.getSourceFile(); 
    boolean filtered=sourceFile==null;//sourceFile=null is a valid condition. 
                                      //It is not an exception.
                                      //The "file" may be a directory. We 
                                      //can not test to see if it is a directory
                                      //at this point because the file type information
                                      //is stored on the package data. There may 
                                      //be multiple package data elements.
    if (filtered) return filtered;
    else 
    {
      File toStageFile = new File(nodeHandler.getToStageDir()+nodeHandler.getSourceDir(),
                                sourceFile);
      return !toStageFile.exists();
    }
  }

}  //end class definition
