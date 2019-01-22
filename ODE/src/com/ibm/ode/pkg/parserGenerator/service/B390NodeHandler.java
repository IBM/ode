/*******************************************************************************
 *
 *
 ******************************************************************************/
package com.ibm.ode.pkg.parserGenerator.service;

import com.ibm.ode.pkg.parserGenerator.EntityTreeObject;
import com.ibm.ode.pkg.parserGenerator.FileEntity;
import com.ibm.ode.pkg.parserGenerator.GeneratorException;
import com.ibm.ode.pkg.parserGenerator.InstallEntity;
import com.ibm.ode.pkg.pkgMvs.MvsProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
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
 * @see TreeHandler
 * @see TreeCustomizer
 * @see NodeHandler
 * @see Formatter
 *
 * @version 0.1 9/19/2000
 * @author Bruce Gibbons
 */
public class B390NodeHandler extends NodeHandler
{
   /**
    * Used to maintain a handle to the formatters.
    */
   private Formatter updFormatter;
   private Formatter lstFormatter;

   /**
    * Used to maintain a handle to the output streams.
    */
   private FileOutputStream updFile_;
   private FileOutputStream lstFile_;
   private boolean wroteToLstFile = false;
   private Vector newFiles;
   private Vector deletedFiles;

   private Hashtable openFiles = new Hashtable();
  
   /**
    * Constructor 
    *
    * @param int filter The type of file list filtering to be used when 
    * generating output. 
    * @see B390LSTFormatter  
    * @see B390UPDFormatter  
    */
   public B390NodeHandler( int filter )
      throws GeneratorException
   {
      super();
      updFormatter = new B390UPDFormatter(this);
      lstFormatter = new B390LSTFormatter(this, filter);
   }

   /**
    * This method handles one product at a time.
    *
    * @param productIE InstallEntity The product install entity
    * @param productETO a valid EntityTreeObject instance which is expected to 
    *        hold a reference to the product InstallEntity.
    * @see GeneratorException
    */
   boolean handleProduct( EntityTreeObject productETO )
      throws GeneratorException
   {
      wroteToLstFile = false;  // Reset LST file flag
      boolean result = super.handleProduct(productETO);
      if (result) 
      {
         String updTextSectionOne = "";

         openFile();
         if ( MvsProperties.deletedFiles != null )
         {
            updTextSectionOne = "OP=SET,@PLANID=1 ";
            this.treeHandler.writeString(updFile_, updTextSectionOne);
         }

         setNewFiles();
         setDeletedFiles();

         if ( MvsProperties.deletedFiles != null )
         {
            String  nextLine = "\n";
            updTextSectionOne = ((B390UPDFormatter)updFormatter).formatDeletedFileSectionOne();            
            this.treeHandler.writeString(updFile_, updTextSectionOne);
            this.treeHandler.writeString(updFile_, nextLine);
         }

         String updText = updFormatter.formatProductInfo();
         this.treeHandler.writeString(updFile_, updText);

         updText = ((B390UPDFormatter)updFormatter).formatDeletedFiles();
         this.treeHandler.writeString(updFile_, updText);
         this.addJCLINFileEntries();
      }
      return result;
   } // end method handleProduct

   /**
    * Adds the entries for the JCLIN files in the UPD and LST files.
    *
    * @throws GeneratorException
    */
   private void addJCLINFileEntries()
      throws GeneratorException
   {
      B390UPDFormatter b390UPDFormatter = (B390UPDFormatter)updFormatter;
      B390LSTFormatter b390LSTFormatter = (B390LSTFormatter)lstFormatter;
      String fmid = productIE.getEntityName().trim().toUpperCase();
      File jclinFile = new File(MvsProperties.pkgControlDir +
                                MvsProperties.fileSeparator +
                                fmid + ".jclin");
      if (jclinFile.exists())
      {   
         this.treeHandler.
            writeString(updFile_, b390UPDFormatter.formatJCLINFile(fmid));
         String filePath = jclinFile.getPath();
         this.treeHandler.writeString(
            lstFile_, "JCLIN." + fmid + " " + filePath + " SRC-F-80-T\n");
      }
   }
  
  /**
   * This method handles one product at a time.
   *
   * @param productIE InstallEntity The product install entity
   * @param productETO a valid EntityTreeObject instance which is
   * expected to hold a reference to the product InstallEntity.
   * @exception GeneratorException
   */
  void openFile()
     throws GeneratorException
  {
     String directoryName = getOutputDirectoryName();
     if (!openFiles.containsKey(directoryName))
     {
        File directory = new File(directoryName);
        if (!(directory.exists() && directory.isDirectory())) 
        { 
           if (!directory.mkdir())
           {
              throw new GeneratorException(
                 "B390NodeHandler: Unable to create directory " + directoryName);
           }
        } 
        this.updFile_ = this.treeHandler.openFile(directoryName,
                                                  getUPDFileName());
        this.lstFile_ = this.treeHandler.openFile(directoryName,
                                                  getLSTFileName());
        openFiles.put(directoryName, "OPEN");
     } 
     else 
     {
        throw new GeneratorException("Product already handled: " + directoryName);
     }
  } //end method openFile

  /**
   * This method generates information about one file stanza.
   *
   * @param productIE InstallEntity The product install entity
   * @param curFE FileEntity the current file entity. 
   * @exception GeneratorException
   */
  void handleFile( FileEntity fileEntity )
     throws GeneratorException
  {
     super.handleFile(fileEntity);
     String updText = updFormatter.formatFileInfo();
     if (updText.length() != 0)
     {
        this.treeHandler.writeString(updFile_, updText);
     }
     
     String lstText = lstFormatter.formatFileInfo();
     lstText += ((B390LSTFormatter)lstFormatter).addFiles(
               ((B390UPDFormatter)updFormatter).getMcsFiles());
     if (lstText.length() != 0)
     {
        this.treeHandler.writeString(lstFile_, lstText);
        wroteToLstFile = true;
     }
  } //end method handleFile

  /**
   * This method returns the name of the directory containing the 
   * file list and upd information. The file will be generated at 
   * ${PKG_CONTROL_DIR}/<fmid>/. 
   *
   * @return String The name of the directory containing the file list 
   * and upd information. 
   */
  public String getOutputDirectoryName()
  {
     // The file will be generated at ${PKG_CONTROL_DIR}/<fmid>/. 
     String separator = "/";
     String pkgControlDirectory=getPkgControlDir();
     
     // product and fmid are always the same.
     String product = productIE.getEntityName().trim().toUpperCase();
     return pkgControlDirectory + separator + product + separator; 
  }

  /**
   * This method returns the name of the file containing the file list information. 
   * The name is always B390.LST
   *
   * @return String The name of the file containing the file list information. 
   */
  private String getLSTFileName()
  {
     String staticName = "B390.LST";
     return staticName;
  }  
  
  /**
   * This method returns the name of the file containing the upd information. 
   * The name is always B390.UPD
   *
   * @return String The name of the file containing the upd information. 
   */
  private String getUPDFileName()
  {
     String fileName = "B390.UPD";
     return fileName;
  }

  /**
   * This method sets the list of new files in this build
   *
   * @return Vector The list of new files
   */
  private void setNewFiles()
  {
     String files = MvsProperties.newFiles;
     newFiles = new Vector();
     if (files != null)
     {
        StringTokenizer st = new StringTokenizer( files, " ," );
        while (st.hasMoreTokens())
        {
           newFiles.addElement( st.nextToken());
        }
     }
  }

  /**
   * This method returns the list of new files in this build
   *
   * @return Vector The list of new files
   */
  public Vector getNewFiles()
  {
     return newFiles;
  }

  /**
   * This method sets the list of files to be deleted in this build
   *
   * @return Vector The list of files to be deleted
   **/
  private void setDeletedFiles()
  {
     String files = MvsProperties.deletedFiles;    
     String fmid = productIE.getEntityName().trim().toUpperCase();
     deletedFiles = new Vector();
     if (files != null)
     {
        StringTokenizer st = new StringTokenizer( files, " ," );
        while (st.hasMoreTokens())
        {
           String file = st.nextToken();
           if (file.startsWith( fmid ))
              deletedFiles.addElement( file );
        }
     }
  }
  
  /**
   * This method returns the list of files to be deleted in this build
   *
   * @return Vector The list of files to be deleted
   */
  public Vector getDeletedFiles()
  {
     return deletedFiles;
  }

  /**
   * This method returns the tree customizer that will be used with
   * this NodeHandler.
   *
   * @return TreeCustomizer The tree customizer that will be used with
   * this NodeHandler
   * @see TreeHandler
   * @see TreeCustomizer
   **/
  TreeCustomizer getTreeCustomizer()
  {
     return new MVSTreeCustomizer();
  }
  
  /**
   * This method generates information about one file stanza
   *
   * @see GeneratorException
   */
  void init()
     throws GeneratorException
  {
  }

  /**
   * This method performs misc. clean up tasks
   *
   * @param productIE InstallEntity The product install entity
   * @param curFE FileEntity the current file entity. 
   * @exception GeneratorException
   */
  void finish()
     throws GeneratorException
  {
     this.treeHandler.closeFile(this.updFile_);
     this.treeHandler.closeFile(this.lstFile_);
 
     // If LST hasn't been updated, delete it
     if (!wroteToLstFile)
     {
        File lstfile = new File( getOutputDirectoryName(), getLSTFileName() );
        lstfile.delete();
     }
  }
}  //end class definition
