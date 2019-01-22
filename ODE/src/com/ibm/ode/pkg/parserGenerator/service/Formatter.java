package com.ibm.ode.pkg.parserGenerator.service;

import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import com.ibm.ode.pkg.parserGenerator.PackageData;
import java.util.*;
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
public abstract class Formatter 
{
  NodeHandler nodeHandler;
  /**********************************************************************
   * Constructor. 
   * @param NodeHandler nodeHandler The nodeHandler containing the data 
   * to be formatted. 
   **/
  public Formatter (NodeHandler nodeHandler) throws GeneratorException
  {
     setNodeHandler(nodeHandler);
  }
  public void setNodeHandler(NodeHandler nodeHandler) throws GeneratorException
  {
    if (nodeHandler==null) throw new GeneratorException("Formatter: nodeHandler=null");
    this.nodeHandler=nodeHandler;
  }

  /**********************************************************************
   * This method returns the target directory. The target directory
   * is the installation directory.
   * @return String The target directory.
   * @throws GeneratorException
   * <p>
   * <p>
   * <B>See Also:</B> Documents on <a href = "http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_ship_data.html">Metadata Summary</a> and <a href="http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_cmf_req.html">CMF to MVS response file Translation</a>.
   **/
  public String getTargetDir(PackageData packageData) throws GeneratorException
  {
    String targetDir=packageData.getTargetDir().trim();
    if (targetDir != null)
    {
      //check if last char is a '/' - if so, strip it
      if (targetDir.charAt(targetDir.length() - 1) == '/')
      {
        targetDir=targetDir.substring(0, targetDir.length() - 1) ;
      }
    }
    return targetDir;
  }

  /**********************************************************************
   * This method returns the target file. The target file is an
   * installable file.
   * @return String The target file.
   * @throws GeneratorException
   * <p>
   * <p>
   * <B>See Also:</B> Documents on <a href = "http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_ship_data.html">Metadata Summary</a> and <a href="http://w3dce.raleigh.ibm.com/~mad/mvs/mvs_cmf_req.html">CMF to MVS response file Translation</a>.
   **/
  public String getTargetFile(PackageData packageData) throws GeneratorException
  {
    String targetFile = packageData.getTargetFile();
    if (targetFile == null)
    {
      throw new GeneratorException("Formater: TargetDir + " +
                    "targetFile cannot be null - It forms a required field");
    }
    return targetFile;
  }

  /**********************************************************************
   * This method returns the file type. 
   * @return String The file type.
   * @throws GeneratorException
   **/
  public String getFileType(PackageData packageData) throws GeneratorException
  {
    String fileType = packageData.getFileType();
    if (fileType == null)
    {
      throw new GeneratorException("Formater: FileType + " +
                    "fileType cannot be null - It forms a required field");
    }
    return fileType;
  }

  /**********************************************************************
   * This method returns the distlib information from the cmf 
   * for the current package data. 
   * @return String The distlib information for the current package data.
   * @throws GeneratorException
   **/
  ArrayList getDistLibs(PackageData packageData) throws GeneratorException
  {
    ArrayList distLib=new ArrayList();
    String distLibIndex = getTargetDir(packageData);
    if (distLibIndex==null||distLibIndex.equalsIgnoreCase(""))
    {
      throw new GeneratorException("Formatter: TargetDir/DistLib + " +
                    "cannot be null - It forms a required field");
    }
    ArrayList distLibs=nodeHandler.getDistLibs();
    boolean found=false;
    for (int i=0;!found&&(i<distLibs.size());i++)
    {
      String currentDistLib=(String)distLibs.get(i);
      StringTokenizer distLibTokens = new StringTokenizer(currentDistLib,",");
      if (distLibTokens.nextToken().trim().equalsIgnoreCase(distLibIndex))
      {
        found=true;
        distLib.add(distLibIndex);
        while (distLibTokens.hasMoreTokens()) 
        { 
          distLib.add(distLibTokens.nextToken());
        }
      }
    }
    if (!found) 
    {
      throw new GeneratorException("Formatter: TargetDir/DistLib + " +
                    distLibIndex+" not found in distlibs");
    }
    return distLib;
  }

  /**********************************************************************
   * This method formats the file information . 
   * @return String The formatted file information.
   * @throws GeneratorException
   **/
  public abstract String formatFileInfo() throws GeneratorException;

  /**********************************************************************
   * This method formats the product information . 
   * @return String The formatted product information.
   * @throws GeneratorException
   **/
  public abstract String formatProductInfo() throws GeneratorException;
}  //end class definition
