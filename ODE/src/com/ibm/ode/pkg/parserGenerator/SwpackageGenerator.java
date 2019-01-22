/*******************************************************************************
 *                    Licensed Materials - Property of IBM
 *
 * XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
 ******************************************************************************/
package com.ibm.ode.pkg.parserGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;
import com.ibm.ode.lib.io.Interface ;
import com.ibm.ode.lib.io.Path ;
import com.ibm.ode.lib.string.PlatformConstants ;

/**
 * This class is responsible for extracting appropriate information from
 * the installEntities and FileEntities which are present in the
 * EntityTree at appropriate levels . It writes out the extracted
 * information to certain files which will be the control files that
 * act as input to the packaging tool. The packaging tool considered
 * here is <I>swpackage</I>
 *
 * One file 'pcd.psf' is generated.
 *
 * This generator can be invoked as follows
 * For example:
 * <pre>
 *  SwpackageGenerator swpackageGenerator = new SwpackageGenerator();
 *  swpackageGenerator.generateTargetMetadataFiles(
 *  curEntityTreeRoot,
 *  curPackageReference )
 * </pre>
 */
public class SwpackageGenerator
   extends Generator
   implements GeneratorInterface
{
   // FileOutputStream, which is used to write the Control file on disk
   private FileOutputStream outFile_;

   // A reference to EntityTreeRoot used for retreiving the logical tree
   // structure binding entities
   private EntityTreeRoot etr_;

   // Private variable, maintains a reference to Package instance
   // used for accessing entities and retreiving data stored within.
   private Package packageRef_;

   // A reference to InstallEntity instance used for accessing InstallEntities
   // and retreiving data stored within.
   private InstallEntity installEntity_;

   // A reference to FileEntity instance used for accessing FileEntities
   // and retreiving data stored within.
   private FileEntity fileEntity_;

   // Used to ensure that all filesets are used and to define sub-products
   // within a certain product that are actually defined within that product.
   private ArrayList minFileset_;

   // Current level in the nested data structure. Used to write as many no.
   // of tabs while generating data to ensure a properly indented control file.
   private int noOfTabs;

   // Info about the shipRootDir used from any SwpackageGenerator method
   private String shipRootDir_;


   /**
    * Creates a new instance of type SwpackageGenerator.
    **/
   public SwpackageGenerator( String shipRootDir, String context,
                              String pkgControlDir, String pkgType,
                              String pkgClass, String pkgFixStrategy )
   {
      // Invoke the constructor for the parent class with appropriate parameters
      super(shipRootDir, context, pkgControlDir,
            pkgType, pkgClass, pkgFixStrategy);

      minFileset_ = new ArrayList();
      noOfTabs = 0;
      shipRootDir_ = shipRootDir ;
   }  //end blank constructor

   /**
    * Primary method used to invoke logic to generate Target Metadata files
    * for swpackage.
    *
    * @param etr Reference to EntityTreeRoot object which holds reference to
    * logical structure of Entities as a tree.
    * @param pkgRef Reference to Package object which holds reference to
    * Parsed Entities and their data.
    * @exception GeneratorException if an error occurs in generating
    * control files, or information about structure of entities (mutual
    * links binding them) are incorrectly specified with respect to the
    * acceptable structure for swpackage.
    */
   public void generateTargetMetadataFiles( EntityTreeRoot etr, Package pkgRef )
      throws GeneratorException
   {
      // The keyword, 'this' refers to the current class and can be used as
      // a method to call another constructor of the class.
      this.etr_ = etr;             // This instance of the EntityTreeRoot class
      this.packageRef_ = pkgRef;   // The package data.
      this.outFile_ = openFile(pkgControlDir_ , "pcd.psf");

      try
      {
         if (genDepot() == false)
           throw new GeneratorException("SwpackageGenerator :\nAn error "
            + "occurred while generating the control file.");   
      }
      finally
      {
        if (outFile_ != null)
          closeFile(outFile_);
      }
   } //end method

   /**
    * Generates one Depot. Private SwpackageGenerator Method.
    * Called from within generateTargetMetadataFiles only.
    *
    * @exception GeneratorException if an error occurs
    * @see #generateTargetMetadataFiles
    */
   private boolean genDepot() throws GeneratorException
   {
      String tempStr;
      ArrayList productETOArray;
      PgSpecialType tempSpc;

      // write depot initializing information
      writeString(outFile_, getTabStr() + "depot\n");

      // getData
      tempStr = ParserGeneratorInitiator.getPackagingTool();
      if (tempStr != null)
      {
         writeString(outFile_, "\n#   Packaging Tool to be used : " +
                               tempStr + "\n\n");
      }

      noOfTabs ++;
      if (pkgClass_.equalsIgnoreCase("SP"))
        writeString(outFile_, getTabStr() + "layout_version\t\t1.0\n");
      tempStr = etr_.getPackageName();
      if (tempStr != null)
      {
         writeString(outFile_, getTabStr() + "tag\t\t" + tempStr + "\n");
      }

      tempStr = etr_.getFullPackageName();
      if (tempStr != null)
      {
         writeString(outFile_, getTabStr() + "title\t\t" + tempStr + "\n");
      }

      tempSpc = etr_.getPackageDescription();
      if (tempSpc != null)
      {
         tempStr = tempSpc.getValue();
         if (tempSpc.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
         {
            tempStr = insertFileSeparator(shipRootDir_, tempStr);
            writeString(outFile_, getTabStr() + "description\t" + "<" + 
                                  tempStr + "\n");
         }
         else if (tempSpc.getType() == ParserGeneratorEnumType.STRING)
         {
            writeString(outFile_, getTabStr() + "description\t" + "\"" + 
                                  tempStr + "\"\n");
         }
         else
            throw new GeneratorException("SwpackageGenerator :\n" +
              "Invalid type for depot description value.");
      }

      tempSpc = etr_.getPackageCopyright();
      if (tempSpc != null)
      {
         tempStr = tempSpc.getValue();
         if (tempSpc.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
         {
            tempStr = insertFileSeparator(shipRootDir_, tempStr);
            writeString(outFile_, getTabStr() + "copyright\t" + "<" + 
                                  tempStr + "\n");
         }
         else if (tempSpc.getType() == ParserGeneratorEnumType.STRING)
         {
            writeString(outFile_, getTabStr() + "copyright\t" + "\"" + 
                                   tempStr + "\"\n");
         }
         else
            throw new GeneratorException("SwpackageGenerator :\n" +
              "Invalid type for depot copyright value.");
      }

      tempStr = etr_.getPackageSerialNumber();
      if (tempStr != null)
      {
         writeString(outFile_, getTabStr() + "number\t\t" + tempStr + "\n");
      }

      // vendor stanza
      tempStr = etr_.getPackageVendorName();
      if (tempStr != null)
      {
         // implies vendor specification has been given
         // write vendor stanza start information
         writeString(outFile_, getTabStr() + "vendor\n");
         noOfTabs ++;

         // vendorname
         writeString(outFile_, getTabStr() + "tag\t\t" + tempStr + "\n");  

         tempStr = etr_.getPackageVendorTitle();
         if (tempStr != null)
         {
           // title
           writeString(outFile_, getTabStr() + "title\t\t" + tempStr + "\n");  
         }

         tempSpc = etr_.getPackageVendorDesc();
         if (tempSpc != null)
         {
           tempStr = tempSpc.getValue();
           if (tempSpc.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
           {
              tempStr = insertFileSeparator(shipRootDir_, tempStr);
              writeString(outFile_, getTabStr() + "description\t" + "<" + 
                                    tempStr + "\n");
           }
           else if (tempSpc.getType() == ParserGeneratorEnumType.STRING)
           {
              writeString(outFile_, getTabStr() + "description\t" + "\"" + 
                                    tempStr + "\"\n");
           }
           else
              throw new GeneratorException("SwpackageGenerator :\n" +
                "Invalid type for product description value");
         }

         // vendor stanza ended - write end information
         noOfTabs --;
         writeString(outFile_, getTabStr() + "end\n");
      } //end if tempStr for vendorName

      ArrayList levelArray = etr_.getLevelArray();
      try
      {
         // product array is at level 0
         productETOArray = (ArrayList)levelArray.get( 0 );    
      }
      catch (ClassCastException ex)
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
           "EntityTreeRoot.levelArray_ can contain objects of type ArrayList only.");
      }
      catch (Exception ex)
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
           "Expecting a non-empty array of Entity Tree Object instances at " +
           "level 0 of EntityTreeRoot.levelArray_");
      }

      ListIterator productIterator;
      EntityTreeObject curETO;

      // check At least one product is necessary
      if (productETOArray == null)
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
           "At least one product is required for swpackage Install tool.");
      }

      if (productETOArray.isEmpty())
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
           "At least one product is required for swpackage Install tool.");
      }

      productIterator = productETOArray.listIterator();
      while ( productIterator.hasNext() )
      {
         //get product ETO
         try
         {
            curETO = (EntityTreeObject)productIterator.next();
         }
         catch (ClassCastException ex)
         {
            throw new GeneratorException("SwpackageGenerator :\n" +
              "EntityTreeRoot's levelArray can only contain arrays of ETO's.");
         }
         catch (Exception ex)
         {
            throw new GeneratorException("SwpackageGenerator :\n" +
              "Error in reading product ETO from array.");
         }
         genProduct(curETO);

      } //end level iterator for loop

      // write depot ending information
      noOfTabs --;
      writeString(outFile_ , getTabStr() + "end\n");
      return true;
   } //end method genDepot

   /**
    * Generates one Product. Called from within genDepot only
    *
    * @param curETO instance of EntityTreeObject holding reference to the
    * InstallEntity corresponding to the product to be generated.
    *
    * @exception GeneratorException if an error occurs.
    * @see #genDepot
    */
   private void genProduct( EntityTreeObject curETO )
      throws GeneratorException
   {
      InstallEntity IERef = curETO.getInstallEntityReference();
      EntityTreeObject childETO;
      String tempStr, version, release, maintLevel, fixLevel, revision = null;
      PgSpecialType tempSpc ;
      ArrayList tempArray ;
      // used in service packaging
      Hashtable ancestorTable = null, supersedesTable = null;

      // validate curETO
      if ((curETO.getType() != ParserGeneratorEnumType.INSTALLENTITY) ||
          (curETO.getFileEntityReference() != null) || (IERef == null) )
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
            "Invalid Product ETO encountered.");
      }

      // write Product begin information
      writeString(outFile_, getTabStr() + "product\n");

      // increase tab count
      noOfTabs ++;

      // write Product specific information
      if (pkgClass_.equalsIgnoreCase("SP"))
      {
        tempStr = System.getProperty("PATCH_NAME");
        if (tempStr == null || tempStr.length() == 0)
          throw new GeneratorException("SwpackageGenerator :\nPATCH_NAME " +
              "not found : required value for service packaging with swpackage.");
      }
      else
      {
        tempStr = IERef.getEntityName();
        if (tempStr == null || tempStr.length() == 0)
          throw new GeneratorException("SwpackageGenerator :\n" +
             "EntityName not found : required attribute for swpackage Product");
      }
      writeString(outFile_, getTabStr() + "tag\t\t" + tempStr + "\n");

      version    = IERef.getVersion();
      release    = IERef.getRelease();
      maintLevel = IERef.getMaintLevel();
      fixLevel   = IERef.getFixLevel();
      if (version == null || release == null)
        revision = null ;
      else if (fixLevel == null && maintLevel != null)
        revision = version + "." + release + "." + maintLevel;
      else if (maintLevel != null && fixLevel != null)
        revision = version + "." + release + "." + maintLevel + "." + fixLevel;
      else
        revision = version + "." + release;

      if (revision != null)
         writeString(outFile_, getTabStr() + "revision\t" + revision + "\n");

      tempArray = IERef.getFullEntityName();
      if (tempArray != null)
      {
         if (tempArray.isEmpty() == false)
         {
            tempStr = (String)tempArray.get(0);
            writeString(outFile_, getTabStr() + "title\t\t" + tempStr + "\n");
         }
      }
      
      tempSpc = IERef.getDescription();
      if (tempSpc != null)
      {
         tempStr = tempSpc.getValue();
         if (tempSpc.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
         {
            tempStr = insertFileSeparator(shipRootDir_, tempStr);
            writeString(outFile_, getTabStr() + "description\t" + "<" + 
                                  tempStr + "\n");
         }
         else if (tempSpc.getType() == ParserGeneratorEnumType.STRING)
         {
            writeString(outFile_, getTabStr() + "description\t" + "\"" + 
                                  tempStr + "\"\n");
         }
         else
            throw new GeneratorException("SwpackageGenerator :\n" +
              "Invalid type for product description value.");
      }

      tempSpc = IERef.getCopyright();
      if (tempSpc != null)
      {
         tempStr = tempSpc.getValue();
         if (tempSpc.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
         {
            tempStr=insertFileSeparator(shipRootDir_, tempStr);
            writeString(outFile_, getTabStr() + "copyright\t" + "<" + tempStr + 
                                  "\n");
         }
         else if (tempSpc.getType() == ParserGeneratorEnumType.STRING)
         {
            writeString(outFile_, getTabStr() + "copyright\t" + "\"" + 
                                  tempStr + "\"\n");
         }
         else
            throw new GeneratorException("SwpackageGenerator :\n" +
               "Invalid type for product copyright value.");
      }

      tempSpc = IERef.getReadme();
      if (tempSpc != null)
      {
         tempStr = tempSpc.getValue();
         if (tempSpc.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
         {
            tempStr=insertFileSeparator(shipRootDir_,tempStr);
            writeString(outFile_, getTabStr() + "readme\t\t" + "<" + 
                                   tempStr + "\n");
         }
         else if (tempSpc.getType() == ParserGeneratorEnumType.STRING)
         {
            writeString(outFile_, getTabStr() + "readme\t\t" + "\"" + tempStr + 
                                  "\"\n");
         }
         else
            throw new GeneratorException("SwpackageGenerator :\n"+
               "Invalid type for product readme value.");
      }

      tempStr = IERef.getEntityId();
      if (tempStr != null)
      {
         writeString(outFile_, getTabStr() + "number\t\t" + tempStr + "\n");
      }

      tempStr = IERef.getCategory();
      if (tempStr != null)
      {
         writeString(outFile_, getTabStr() + "category\t" + tempStr + "\n");
      }

      tempStr = IERef.getCategoryTitle();
      if (tempStr != null)
      {
         writeString(outFile_, getTabStr() + "category_title\t" + tempStr + "\n");
      }

      tempStr = IERef.getIsLocatable();
      if (tempStr != null)
      {
         if (tempStr.equals("false") || tempStr.equals("true"))
         {
            writeString(outFile_, getTabStr() + "is_locatable\t" + tempStr + 
                                  "\n");
         }
         else
         {
            throw new GeneratorException("SwpackageGenerator :\n" +
               "Incorrect value specified for isLocatable.");
         }
      } 

      tempSpc = IERef.getInstallDir();
      if (tempSpc != null)
      {
        tempStr = tempSpc.getValue();
        writeString(outFile_, getTabStr() + "directory\t" + tempStr + "\n");
      }

      tempStr = IERef.getMachineSeries();
      if (tempStr != null)
      {
        writeString(outFile_, getTabStr() + "architecture\t" + tempStr + "\n");
      }

     tempStr = IERef.getMachineType();
     if (tempStr != null)
     {
        writeString(outFile_, getTabStr() + "machine_type\t" + tempStr + "\n");
     }

     tempArray = IERef.getOsName();
     if (tempArray != null)
     {
        if (tempArray.isEmpty() == false)
        {
           if (tempArray.size() > 1)
           {
              Interface.printWarning("Multiple entries for osName not used " +
                "with swpackage - using first entry");
           }

           tempStr = (String)(tempArray.get(0));
           writeString(outFile_ , getTabStr() + "os_name\t" + tempStr + "\n");
        }
     }

     tempArray = IERef.getOsRelease();
     if (tempArray != null)
     {
        if (tempArray.isEmpty() == false)
        {
           if (tempArray.size() > 1)
           {
              Interface.printWarning("Multiple entries for osRelease not used" +
                 " with swpackage - using first entry");
           }

           tempStr = (String)tempArray.get(0);
           writeString(outFile_, getTabStr() + "os_release\t" + tempStr + "\n");
        }
     }

     tempArray = IERef.getOsVersion();
     if (tempArray != null)
     {
        if (tempArray.isEmpty() == false)
        {
           if (tempArray.size() > 1)
           {
              Interface.printWarning("Multiple entries for osVersion not used" +
                 " with swpackage - using first entry");
           }

           tempStr = (String)(tempArray.get(0));
           writeString(outFile_, getTabStr() + "os_version\t" + tempStr + "\n");
        }
     }

     if (pkgClass_.equalsIgnoreCase("SP"))
       writeString(outFile_, getTabStr() + "is_patch\ttrue\n");

     // vendor Information
     tempStr = IERef.getVendorName();
     if (tempStr != null)
     {
        // Optional vendor paragraph specified if required field vendor name is not
        // specified, vendor paragraph is ignored
        writeString(outFile_, getTabStr() + "vendor\n");
        noOfTabs++;

        writeString(outFile_, getTabStr() + "tag\t\t" + tempStr + "\n");

        tempStr = IERef.getVendorTitle();
        if (tempStr != null)
        {
           writeString(outFile_, getTabStr() + "title\t\t" + tempStr + "\n");
        }

        tempSpc = IERef.getVendorDesc();
        if (tempSpc != null)
        {
           tempStr = tempSpc.getValue();
           if (tempSpc.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
           {
              tempStr=insertFileSeparator(shipRootDir_,tempStr);
              writeString(outFile_, getTabStr() +
                 "description\t" + "<" + tempStr + "\n");
           }
           else if (tempSpc.getType() == ParserGeneratorEnumType.STRING)
           {
              writeString(outFile_, getTabStr()+
                 "description\t" + "\"" + tempStr + "\"\n");
           }
           else
              throw new GeneratorException("SwpackageGenerator :\nInvalid " +
                "type for product vendor description value.");
        }

        noOfTabs--;
        writeString(outFile_, getTabStr() + "end\n");
     } //vendor information done


     // SubProduct information
     tempArray = IERef.getEntitySubsetInfo();
     ListIterator i;

     if (tempArray != null)
     {
        if (tempArray.isEmpty() == false)
        {
           //at least one sub product is defined
           i = tempArray.listIterator();
           while (i.hasNext())
           {
              //write subproduct begin information
              writeString(outFile_, getTabStr()+ "subproduct\n");
              noOfTabs++;

              //onesubproduct
              genOneSubProduct(i.next());

              //object of type EntitySubsetInfo to
              //be passed, else error

              noOfTabs--;
              writeString(outFile_, getTabStr() + "end\n");
           } //end subproduct iterator for loop
        } //end subproduct isEmpty
     }

     // ControlScripts Information
     // exists in Pathinfo and requisiteinfo stanza
     tempArray = IERef.getConfigFiles();
     writeString(outFile_, "\n" + getTabStr() + "#Control Scripts\n\n");
     genCtrlScripts( tempArray, ParserGeneratorEnumType.CONFIGFILES );

     tempArray = IERef.getRequisites();
     writeString(outFile_, "\n" + getTabStr() + "#Dependencies\n\n");
     genCtrlScripts( tempArray, ParserGeneratorEnumType.REQUISITES );

     if (pkgClass_.equalsIgnoreCase("SP"))
     {
       ancestorTable = populateHashtable("PATCH_ANCESTOR_LIST");
       supersedesTable = populateHashtable("PATCH_SUPERSEDES_LIST");
     }

     // filesets expected now.
     tempArray = curETO.getChildReferenceArray();

     // would contain ETO for fileset IEs
     if (tempArray == null || tempArray.isEmpty())
     {
        // minimum one fileset expected
        throw new GeneratorException("SwpackageGenerator :\n" +
          "Minimum one fileset must be defined for every Product.");
     }
     else
     {
        i = tempArray.listIterator();
        while (i.hasNext())
        {
           try
           {
              childETO = (EntityTreeObject)i.next();
           }
           catch (Exception ex)
           {
              throw new GeneratorException("SwpackageGenerator :\n" +
                 "Obj. of type ETO expected in ETO.ChildETOArray\n\n" + 
                 ex.toString());
           }

           genFileset(childETO, IERef.getEntityName(), ancestorTable, 
                      supersedesTable);
        } //end fileset iterator
     } //finished generating filesets

     // check if all filesets described in subproducts
     // of this product exist as filesets here.
     if (minFileset_.isEmpty() == false )
     {
        try
        {
           tempStr = "";
           while (minFileset_.size() > 0)
           {
              tempStr = tempStr + " " + (String)minFileset_.remove( 0 );
           }
           throw new GeneratorException("SwpackageGenerator :\n" +
             "Fileset name(s) defined in one or more" +
             " subproducts of a product in CMF not found" +
             " in this product.\n\nMissing fileset(s) are :" + tempStr );
        } //end try
        catch (ClassCastException ex)
        {
           throw new GeneratorException("SwpackageGenerator :\n" +
             "Illegal object type instance detected in ArrayList minFileset_ - must" +
             "be of type String only.");
        }
        catch (Exception ex)
        {
           throw new GeneratorException("SwpackageGenerator :\n" +
             ex.toString());
        }
     }

     //Product specific information done!

     //reset tabcount
     noOfTabs --;

     //write Product Ending information
     writeString(outFile_, getTabStr() + "end\n");

   }//end method genProduct

   /**
    * Generates one Fileset
    * Private Method
    * Called from within genProduct only
    *
    * @param curETO instance of EntityTreeObject holding reference to
    * the InstallEntity corresponding to the fileset to be generated.
    * @param baseProductName name of the product this fileset belongs to
    * @param ancestorTable Hashtable containing the mappings of fileset
    * to its ancestors as listed in PATCH_ANCESTOR_LIST
    * @param supersedesTable Hashtable containing the mappings of fileset
    * to its supersedes as listed in PATCH_SUPERSEDES_LIST
    *
    * @exception GeneratorException if an error occurs.
    * @see #genProduct
    */
   private void genFileset( EntityTreeObject curETO, String baseProductName,
                            Hashtable ancestorTable, Hashtable supersedesTable )
      throws GeneratorException
   {
      ArrayList childArray = getFilesInFileSet(curETO);
      if (childArray == null || childArray.isEmpty())
        return;

      //write fileset beginning information
      writeString(outFile_, getTabStr() + "fileset\n");
      noOfTabs++;

      InstallEntity IERef = curETO.getInstallEntityReference();
      
      // fileset
      String tempStr, version, release, maintLevel, fixLevel, revision = null;
      ArrayList tempArray;
      ListIterator i;
      PgSpecialType tempSpc;
      EntityTreeObject childETO;

      //get and write attributes
      tempStr = IERef.getEntityName();
      writeString(outFile_, getTabStr()+ "tag\t\t" + tempStr+ "\n");

      // If fileset exists in minFilesetList, remove it from there as found
      if (minFileset_.contains(tempStr))
      {
        // removes all occurrences of tempStr in minFileset
        minFileset_.remove(tempStr); 
      }

      version    = IERef.getVersion();
      release    = IERef.getRelease();
      maintLevel = IERef.getMaintLevel();
      fixLevel   = IERef.getFixLevel();
      if (version == null || release == null)
         revision = null ;
      else if (fixLevel == null && maintLevel != null)
         revision = version + "." + release + "." + maintLevel;
      else if (maintLevel != null && fixLevel != null)
         revision = version + "." + release + "." + maintLevel + "." + fixLevel;
      else
         revision = version + "." + release;

      if (revision != null)
         writeString(outFile_, getTabStr()+ "revision\t" + revision + "\n");

      tempArray = IERef.getFullEntityName();
      if (tempArray != null && !tempArray.isEmpty())
      {
        // Only use the first line for swpackage
        tempStr = (String)tempArray.get(0);
        writeString(outFile_, getTabStr() + "title\t\t" + tempStr + "\n");
      }

      tempSpc = IERef.getDescription();
      if (tempSpc != null)
      {
         tempStr = tempSpc.getValue();
         if (tempSpc.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
         {
            tempStr = insertFileSeparator(shipRootDir_,tempStr);
            writeString(outFile_, getTabStr() + "description\t" + "<" + 
                                  tempStr + "\n");
         }
         else if (tempSpc.getType() == ParserGeneratorEnumType.STRING)
         {
           writeString(outFile_, getTabStr() + "description\t\"" + 
                                 tempStr + "\"\n");
         }
         else
            throw new GeneratorException("SwpackageGenerator :\n" +
              "Invalid type for product subproduct description value.");
      }

      tempStr = IERef.getIsKernel();
      if (tempStr != null)
         if (tempStr.equals("false") || tempStr.equals("true"))
            writeString(outFile_, getTabStr() + "is_kernel\t" + tempStr + "\n");
         else
            throw new GeneratorException("SwpackageGenerator :\nIncorrect value"
               + " specified for isKernel.");

      tempStr = IERef.getBootReqmt();
      if (tempStr != null)
      {
         tempStr = tempStr.trim();
         if((tempStr.equalsIgnoreCase("false")) ||
            (tempStr.equalsIgnoreCase("no")) || 
            (tempStr.equalsIgnoreCase("n")))
            writeString(outFile_, getTabStr()+ "is_reboot\t" + "false" + "\n");
         else
            if((tempStr.equalsIgnoreCase("true")) ||
               (tempStr.equalsIgnoreCase("yes")) ||
               (tempStr.equalsIgnoreCase("y")))
               writeString(outFile_, getTabStr() + "is_reboot\t" + "true" + "\n");
            else
               throw new GeneratorException("SwpackageGenerator :\nIncorrect value"
                 + " '"+ tempStr +"' specified for bootReqmt");
      }
      
      if (pkgClass_.equalsIgnoreCase("SP"))
      {
        if (!isNewFileSetOrLinkOrDir(IERef.getEntityName(), "PATCH_NEW_FILESETS"))
          writeString(outFile_, getTabStr() + "is_patch\ttrue\n");
      }
      
      // ControlScripts Information exists in Pathinfo and requisiteinfo stanza
      tempArray = IERef.getConfigFiles();
      writeString(outFile_, "\n" + getTabStr() + "#Control Scripts\n\n");
      genCtrlScripts(tempArray, ParserGeneratorEnumType.CONFIGFILES);

      tempArray = IERef.getRequisites();
      writeString(outFile_, "\n" + getTabStr() + "#Dependencies\n\n");
      genCtrlScripts(tempArray, ParserGeneratorEnumType.REQUISITES);

      if (pkgClass_.equalsIgnoreCase("SP"))
      {
        genAncestorAndSupersedes("ancestor", baseProductName,
                                 IERef.getEntityName(), ancestorTable);
        genAncestorAndSupersedes("supersedes", baseProductName, 
                                 IERef.getEntityName(), supersedesTable);
      }
      
      // Demarcate begin of files section in fileset comment to be written.
      writeString(outFile_, "\n" + getTabStr() + "#Files\n\n");

      i = childArray.listIterator();
      while ( i.hasNext() )
      {
        try
        {
          childETO = (EntityTreeObject)i.next();
        }
        catch (Exception ex)
        {
          throw new GeneratorException("SwpackageGenerator :\nObject of " +
                "type ETO expected in ETO.ChildETOArray\n\n" + ex.toString());
        }
        genFiles(childETO);
      } //end fileset iterator
      //finished generating fileset

      // fileset ends
      noOfTabs--;
      writeString(outFile_, getTabStr() + "end\n");
   } //end method genFileset

   /**
    * Returns an array of files that need to be included in the PSF file for
    * a  Fileset. Called from within genFileset only
    *
    * @param curETO instance of EntityTreeObject holding reference to
    * the InstallEntity corresponding to the fileset to be generated.
    *
    * @exception GeneratorException if an error occurs.
    * @see #genFileSet
    */
   private ArrayList getFilesInFileSet(EntityTreeObject curETO)
      throws GeneratorException
   {
      // Check validity of ETO
      InstallEntity IERef = curETO.getInstallEntityReference();
      String fileSetName = IERef.getEntityName();
      if (fileSetName == null)
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
            "Required field entityName not found for fileset.");
      }
      if (curETO.getType() != ParserGeneratorEnumType.INSTALLENTITY ||
          curETO.getFileEntityReference() != null || IERef == null)
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
           "Invalid Fileset ETO encountered.");
      }

      ArrayList childFileArray = curETO.getChildReferenceArray();
      if (childFileArray == null)
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
           "Minimum one file stanza expected to be defined within every fileset.");
      }
      if (childFileArray.isEmpty())
      {
        // minimum one fileset expected
        throw new GeneratorException("SwpackageGenerator :\n" +
          "Minimum one file stanza expected to be defined within every fileset.");
      }

      // If it's not SP, include all the files in the shiptree
      if (!pkgClass_.equalsIgnoreCase("SP"))
        return childFileArray;     
      else
      {
        // PKG_CLASS is SP
        // Check if it is a new fileset. If it is, it means that it doesn't
        // exist in the base product. So this fileset can be included in the
        // PSF file with all its files irrespective of their existence in the
        // shiptree
        if (isNewFileSetOrLinkOrDir(fileSetName, "PATCH_NEW_FILESETS"))
          return childFileArray;
        
        // This fileset is not new, hence check if any of its files are present
        // in the shiptree and include only those files in the PSF file
        return filesInShipTree(childFileArray);
      }
   } //end method getFilesInFileSet

   /**
    * Returns an array of files, links, directories that exist in the shiptree 
    * or need to be included in the PSF file for a Fileset
    * Called from within getFilesInFileSet
    *
    * @param childFileArray An array of references to child entities
    * @exception GeneratorException if an error occurs.
    * @see #getFilesInFileSet
    */
   private ArrayList filesInShipTree( ArrayList childFileArray )
      throws GeneratorException
   {
      EntityTreeObject childETO;
      FileEntity FERef;
      PackageData curPD;
      ListIterator fileIterator;
      ArrayList includeFiles = new ArrayList();
      ArrayList pDataArray;
      String sourceDir, sourceFile, fullSourceDir, fullSourceFile, fileType;
      File childFile;

      fileIterator = childFileArray.listIterator();
      while ( fileIterator.hasNext() )
      {
        try
        {
          childETO = (EntityTreeObject)fileIterator.next();
          FERef = childETO.getFileEntityReference();
          pDataArray = FERef.getPackageData();
          if (pDataArray == null || pDataArray.isEmpty())
            continue;
          curPD = (PackageData)pDataArray.get(0);
          fileType = curPD.getFileType();
          sourceDir = FERef.getSourceDir();
          if (sourceDir == null)
          {
            throw new GeneratorException("SwpackageGenerator :\n" +
               "Required attribute sourceDir for file Stanza not found.");
          }

          // get absolute path to the source directory
          fullSourceDir = insertFileSeparator(shipRootDir_, sourceDir);
     
          if (fileType.equalsIgnoreCase("F") || fileType.equalsIgnoreCase("S"))
          {
            sourceFile = FERef.getSourceFile();
            if (sourceFile == null)
            {
               throw new GeneratorException("SwpackageGenerator :\n" +
                 "Required field sourceFile not found in file stanza.");
            }
            
            // Check for the existence in the shiptree if it is a file
            if (fileType.equalsIgnoreCase("F"))
            {
              // get the absolute path for the file
              fullSourceFile = insertFileSeparator(fullSourceDir, sourceFile);
              childFile = new File(fullSourceFile);
              if (childFile.isFile())
                includeFiles.add(childETO); 
            }
            else if (fileType.equalsIgnoreCase("S"))
            {
              // It is a Symbolic link
              // append the values of sourceDir and sourceFile obtained from the
              // CMF and check if this entry is listed in PATCH_NEW_LINKS
              // If found, include it in the PSF file or else ignore it
              fullSourceFile = insertFileSeparator(sourceDir, sourceFile);
              if (isNewFileSetOrLinkOrDir(fullSourceFile, "PATCH_NEW_LINKS"))
                includeFiles.add(childETO); 
            }
          }
          else if (fileType.equalsIgnoreCase("D"))
          {
            // It is a directory
            // include this directory in the PSF file if it is listed in
            // PATCH_NEW_DIRS
            if (isNewFileSetOrLinkOrDir(sourceDir, "PATCH_NEW_DIRS"))
              includeFiles.add(childETO); 
          }
        }
        catch (Exception ex)
        {
          throw new GeneratorException("SwpackageGenerator :\nObject of " +
             "type ETO expected in ETO.ChildETOArray\n\n" + ex.toString());
        }
      }
      return includeFiles;
   } // end method filesInShipTree

   /**
    * Generates information relating to one File stanza in the CMF
    * Called from within genFileset only
    *
    * @param curETO instance of EntityTreeObject holding reference
    * to the FileEntity corresponding to the File Stanza to be generated.
    * @exception GeneratorException if an error occurs.
    * @see #genFileset
    */
   private void genFiles( EntityTreeObject curETO )
         throws GeneratorException
   {
      // check validity of ETO
      String tempStr;
      FileEntity FERef = curETO.getFileEntityReference();

      ArrayList pDataArray = FERef.getPackageData();
      
      // See if there is nothing to do...
      if (pDataArray == null || pDataArray.isEmpty())
         return;


      if (pDataArray.size() > 1)
      {
         String srcdir = FERef.getSourceDir();
         String srcfile = FERef.getSourceFile();
         String srcpath;
         if (srcfile != null)
            srcpath = srcdir + srcfile;
         else
            srcpath = srcdir;
         Interface.printWarning("Multiple PackageData entries not supported"
            + " with SwpackageGenerator, using first entry from File stanza \""
            + srcpath + "\"");
      }

      // Use first PackageData array element
      PackageData curPD = (PackageData)pDataArray.get(0);

      tempStr = curPD.getPermissions();
      if (curPD.getUserId() != null)
      {
         // if permissions is not specified in the file stanza, the above line
         // would return null to tempStr, so avoid writing null to control file
         if (tempStr != null)
         {
            tempStr = tempStr.trim();
            
            // check for HP machine
            if (PlatformConstants.isHpMachine(PlatformConstants.CURRENT_MACHINE))
            {
               if (tempStr.startsWith( "-" ))
                  tempStr = tempStr + " -o " + curPD.getUserId();
               else
                  tempStr = " -m " + tempStr + " -o " + curPD.getUserId();
            }
            else
               tempStr = tempStr + " -o " + curPD.getUserId();
         }
         else
            tempStr = " -o " + curPD.getUserId();
      }
      if (curPD.getGroupId() != null)
      {
         // if permissions is not specified in the file stanza, the above line
         // would return null to tempStr, so avoid writing null to control file
         if (tempStr != null)
            tempStr = tempStr + " -g " + curPD.getGroupId();
         else
            tempStr = " -g " + curPD.getGroupId();
      }

      // if any of these permissions are specified, then
      // filepermissions for the current fileset are deemed to
      // have been specified in this file stanza - set the vbl
      if (tempStr != null)
         writeString( outFile_ , getTabStr() + "file_permissions\t" +
            tempStr + "\n" );

      if ((curETO.getType() != ParserGeneratorEnumType.FILE) ||
            (curETO.getInstallEntityReference() != null) ||
            (FERef == null) )
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
            "Invalid Fileset ETO encountered.");
      }

      // Check for fileType: File, Directory, Link and do the appropriate
      // thing.
      String fileType = curPD.getFileType();
      if (fileType.equalsIgnoreCase("f") || fileType.equalsIgnoreCase("s"))
      {
         // sourceDir and sourceFile attributes will not be null for fileType=f
         // but sourceFile could be null for fileType=s
         String sourceDir = FERef.getSourceDir();
         String sourceFile = FERef.getSourceFile();
         
         // If sourceFile is not specified and fileType is symbolic link then
         // it means a symbolic link to a directory is intended. Separate the
         // filename from the sourceDir value.
         if (sourceFile == null && fileType.equalsIgnoreCase("s"))
         {
           if (sourceDir.endsWith("/"))
             sourceDir = sourceDir.substring(0, sourceDir.length() - 1);
           int idx = sourceDir.lastIndexOf('/');
           sourceFile = sourceDir.substring(idx + 1, sourceDir.length());
           sourceDir = sourceDir.substring(0, idx + 1);
         }

         String fullSourceDir = insertFileSeparator(shipRootDir_ , sourceDir );

         // insert the ship root dir path and separator if necessary
         // for gatherer to be able to find this filepath
         writeString(outFile_, getTabStr() + "directory\t\t" + fullSourceDir);

         String targetDir = curPD.getTargetDir();
         if (targetDir == null)
         {
            targetDir = FERef.getSourceDir();
         }

         // targetDir is only needed to create the symlink, the control file
         // actually wants the source directory where to find the link and then
         // the target directory where the link will be (which is the same
         // sourceDir).
         if (fileType.equalsIgnoreCase( "s" ))
         {
            writeString(outFile_, " = " + sourceDir + "\n");
         }
         else
         {
            writeString(outFile_, " = " + targetDir + "\n");
         }

         writeString(outFile_, getTabStr() + "file\t\t\t");

         tempStr = curPD.getFlags();
         if (tempStr != null)
         {
            writeString(outFile_, tempStr);
         }        
         writeString(outFile_, "   " + sourceFile + " " + sourceFile + "\n");
         String targetFile = curPD.getTargetFile();
         
         // In case of a symbolic link to a directory, targetFile usually is
         // not specified. So, we will not substitute targetFile with sourceFile
         // when it is not specified in case of a symbolic link
         if (targetFile == null && !fileType.equalsIgnoreCase("s"))
         {
            targetFile = FERef.getSourceFile();
         }

         // Since swpackage/swinstall doesn't automatically create symbolic
         // links, we'll have to do it.
         if (fileType.equalsIgnoreCase("s"))
         {
            Path.createPath(fullSourceDir);
            String targetPath = targetDir;
            if (targetFile != null)
              targetPath += targetFile;
            if (Path.symLink(targetPath, fullSourceDir + sourceFile, true) == 
                false)
            {
               throw new GeneratorException("SwpackageGenerator :\n" +
                  "Unable to create symlink at `" + fullSourceDir +
                  sourceFile + "'");
            }
         }
      }
      else if (fileType.equalsIgnoreCase("d"))
      {
         tempStr = FERef.getSourceDir();
         if (tempStr == null)
         {
            throw new GeneratorException("SwpackageGenerator :\n" +
               "Required attribute sourceDir for file stanza not found.");
         }

         tempStr = Path.stripTrailingSlashes(tempStr);
         String sourcePath = Path.filePath(tempStr);
         String sourceName = Path.fileName(tempStr);

         // Directory
         tempStr = curPD.getTargetDir();
         if (tempStr == null)
         {
            tempStr = FERef.getSourceDir();
         }
         tempStr = Path.stripTrailingSlashes(tempStr);
         String targetPath = Path.filePath(tempStr);
         String targetName = Path.fileName(tempStr);

         if (targetPath.length() == 0)
         {
            targetPath = System.getProperty("file.separator");
         }

         // insert the ship root dir path and separator if necessary
         // for gatherer to be able to find this filepath
         if (sourcePath.length() > 0)
         {
            sourcePath = insertFileSeparator(shipRootDir_ , sourcePath);
         }
         else
         {
            sourcePath = shipRootDir_;
         }

         // Since 'swpackage' and 'swinstall' don't automatically create
         // directories, do so know.
         tempStr = insertFileSeparator(sourcePath, sourceName);
         if (!Path.exists(tempStr))
         {
            if (Path.createPath(tempStr) == false)
            {
               throw new GeneratorException("SwpackageGenerator :\n" +
                  "Unable to create source directory `" + tempStr + "'");
            }
         }

         // Write out the *directory* entry:
         //      directory <source directory> = <target directory>
         writeString(outFile_ , getTabStr() + "directory\t\t" + sourcePath);
         if (targetPath != null)
         {
            writeString(outFile_ , " = " + targetPath + "\n");
         }
         else
         {
            writeString(outFile_, " = " + sourcePath + "\n");
         }

         // Write out the *file* entry:
         //      file    <permission flags> <source file> = <target file>
         writeString(outFile_, getTabStr() + "file\t\t\t");
         tempStr = curPD.getFlags();
         if (tempStr != null)
         {
            writeString(outFile_, tempStr);
         }

         if (sourceName == null)
         {
            throw new GeneratorException("SwpackageGenerator :\n" +
               "Required field sourceFile not found in file stanza.");
         }
         else
         {
            writeString(outFile_, " " + sourceName);
         }

         if (targetName != null)
         {
            writeString(outFile_, "   " + targetName + "\n");
         }
         else
         {
            writeString(outFile_, " " + sourceName + "\n");
         }
      }
      else // Unknown fileType
      {
         String srcdir = FERef.getSourceDir();
         String srcfile = FERef.getSourceFile();
         String srcpath;
         if (srcfile != null)
         {
            srcpath = srcdir + srcfile;
         }
         else
         {
            srcpath = srcdir;
         }
         Interface.printWarning("fileType `" + fileType + "' for File Stanza "
            + " `" + srcpath + "' is not supported.");
         Interface.printWarning("Valid fileTypes are: 'f'(file), "
            + "'s'(symbolic link), 'd'(directory).");
      } // end if fileType
   } //end method genFiles

   /**
    * Private method used to generate control scripts.
    * Called from within genProduct or genFileset only
    *
    * @param ctrlScripts ArrayList of ReqTypes containing control scripts
    * information
    * @param ctrlScriptType Type of Control Script stanza (e.g.dependency
    * info / requisite info) - used to validate control script keyword
    * for said stanza
    * @exception GeneratorException if object passed to array is not of
    * type Reqtype, or control script keyword is found to be invalid for
    * type of stanza specified.
    * @see #genProduct
    * @see #genFileset
    */
   private void genCtrlScripts( ArrayList ctrlScripts, int ctrlScriptType )
      throws GeneratorException
   {
      ListIterator i;
      ReqType tempReq;
      Object obj;
      String strType = null, strValue = null;

      //check if instantiated
      if (ctrlScripts != null)
      {
         //check if at least one obj.
         //exists in array
         if (ctrlScripts.isEmpty() == false)
         {
            //at least one pre/co requisite exists
            i = ctrlScripts.listIterator();
            while ( i.hasNext() )
            {
               obj = i.next() ;

               //object of only type ReqType can only be passed
               if (obj instanceof ReqType)
               {
                  tempReq = (ReqType)obj;
                  strType = checkValidControlScriptKeyword(ctrlScriptType,
                                                           tempReq.getType());
                  if (strType == null)
                    continue;
                  strValue = tempReq.getValue();
                  if (strValue != null && strValue.trim().length() != 0)
                  {
                     if (ctrlScriptType == ParserGeneratorEnumType.CONFIGFILES)
                     {
                        // strValue is the name of the configFile described.
                        // Check to see if its an absolute path (starts with a
                        // forward slash) or assume its a relative path and 
                        // append tostage
                        if (strValue.trim().
                            startsWith(ParserGeneratorInitiator._fileSeparator_))
                          strValue = strValue ;
                        else
                          strValue = insertFileSeparator(shipRootDir_, strValue);
                     } //end if
                     writeString(outFile_, getTabStr() +
                                           strType + "\t" + strValue + "\n");
                  }
                  else
                  {
                     throw new GeneratorException("SwpackageGenerator :\n"
                     + "The CMF attribute " + ctrlScriptType + " does not "
                     + "have a Valid FileName.");
                  }
               } //end if instanceof
               else
               {
                  throw new GeneratorException("SwpackageGenerator :\n"
                  + "Expecting object of type ReqType for reading Requisites.");
               }
            } //end for iterator
         }//end isEmpty
      } //end if null
   } //end method genCtrlScripts

   /**
    * called from within method genCtrlScripts only
    * checks valid keyword for specified control script type stanza.
    *
    * @param ctrlScriptType integer token corresponding to control script
    * stanza type
    * @param typeStr String passed as control script keyword - this
    * keyword is the generic CMF keyword used. (e.g. "P" for 'prerequisites)
    * @return Actual keyword, confirming to swpackage details, to be written
    * to control file for this control script keyword
    * @exception GeneratorException if an error occurs, i.e., control
    * script keyword passed is not a valid keyword for the type of control
    * script stanza specified.
    */
   private String checkValidControlScriptKeyword( int ctrlScriptType,
                                                  String typeStr )
      throws GeneratorException
   {
      switch (ctrlScriptType)
      {
         case ParserGeneratorEnumType.REQUISITES :
            if (typeStr.equalsIgnoreCase("P") || typeStr.equalsIgnoreCase("Pre"))
            {
               return ("prerequisite");
            }
            if (typeStr.equalsIgnoreCase("C") || typeStr.equalsIgnoreCase("Co"))
            {
               return ("corequisite");
            }
            // generate "ancestor" tag for "S" or "Sup" attributes only for
            // IPP packaging. For service, execute getAncestor
            if (typeStr.equalsIgnoreCase("S") || typeStr.equalsIgnoreCase("Sup"))
            {
              if (pkgClass_.equalsIgnoreCase("SP")) 
                return null;
              else
                return "ancestor";
            }

            // throw an exception if the requisite specified is neither P nor C
            throw new GeneratorException("SwpackageGenerator :\n" +
               "Invalid requisite " + typeStr + " specified in the control " +
               "file.\nValid types are 'P' or 'Pre', 'C' or 'Co', and 'S' or 'Sup'.");
         case ParserGeneratorEnumType.CONFIGFILES :
            return typeStr;
         default:
      }

      // only invalid case would fall thru the
      // switch to execute this statement
      throw new GeneratorException("SwpackageGenerator :\n" +
         "Illegal Control Script type specified " +
         "in call to method checkValidControlScriptKeyword.");
   } //end method checkValidControlScriptKeyword

   /**
    * used to generate information about one subproduct. This call is always to
    * made from within method genProduct only
    *
    * @param obj Object of type EntitySubsetInfo which holds information
    * about this subproduct
    * @exception GeneratorException if object passed in call is not an
    * instance of type EntitySubsetInfo, or an incorrect data type is specified
    * for one of the subproduct attributes, or if any required subproduct
    * attribute is not specified.
    * @see #genProduct
    */
   private void genOneSubProduct( Object obj )
      throws GeneratorException
   {
      EntitySubsetInfo curSubProduct;
      String tempStr;
      PgSpecialType tempSpc;

      if (obj instanceof EntitySubsetInfo)
      {
         curSubProduct = (EntitySubsetInfo)obj;

         tempStr = curSubProduct.getSubsetName();
         if (tempStr != null)
         {
            writeString(outFile_, getTabStr() + "tag\t\t" + tempStr + "\n");
         }
         else
         {
            throw new GeneratorException("SwpackageGenerator :\n" +
              "Required field subsetName not found in entitySubsetInfo.");
         }

         tempStr = curSubProduct.getFullSubsetName();
         if (tempStr != null)
         {
            writeString(outFile_, getTabStr() + "title\t\t" + tempStr + "\n");
         }

         tempSpc = curSubProduct.getSubsetDescription();
         if (tempSpc != null)
         {
            tempStr = tempSpc.getValue();
            if (tempSpc.getType() == ParserGeneratorEnumType.FILENAMEDATATYPE)
            {
               tempStr=insertFileSeparator(shipRootDir_,tempStr);
               writeString(outFile_, getTabStr() + "description\t" + "<" + 
                                     tempStr + "\n");
            }
            else if (tempSpc.getType() == ParserGeneratorEnumType.STRING)
            {
               writeString(outFile_, getTabStr() + "description\t" + "\"" + 
                                     tempStr + "\"\n");
            }
            else
               throw new GeneratorException("SwpackageGenerator :\n"
               + "Invalid type for product subproduct description value\n");
         }

         ArrayList contentArray = curSubProduct.getSubsetContent();
         if (contentArray == null || contentArray.isEmpty())
         {
            throw new GeneratorException("SwpackageGenerator :\n" +
               "EntitySubset definition must have " +
               "at least one fileset as subsetContent");
         }
         else
         {
            //at least one fileset found
            String value;
            ListIterator contentIterator = contentArray.listIterator();

            tempStr = getTabStr() + "contents\t\t";

            while ( contentIterator.hasNext() )
            {
               try
               {
                  value=(String)(contentIterator.next());
                  tempStr = tempStr + value + " ";
               }
               catch (Exception ex)
               {
                  throw new GeneratorException("SwpackageGenerator :\n"
                     + "Subset contents must be an array of string values.");
               }

               if (minFileset_.contains(value) == false)
               {
                  //string not found in minFileset
                  minFileset_.add(value);
               }

               //else found means this fileset has been
               //included in some other subproduct
               //of the same product before - no need
               //to incl. any fileset twice
            } //end for contentIterator
            writeString(outFile_ , tempStr + "\n");
         } //end contentArray isEmpty?
      }
      else
      {
         throw new GeneratorException("SwpackageGenerator :\n" +
            "Object of type EntitySubsetInfo expected " +
            "in Subset ArrayList.");
      } //end obj is instanceof EntitySubsetInfo

   } //end genOneSubProduct

   /**
    * Depending on the level of the current attribute
    * in the nested entity structure, it generates and returns a string
    * with an corresponding no. of tabs, to ensure proper indentation in
    * generated control file.
    */
   private String getTabStr()
   {
      int i = 0;
      String tabStr = "";

      while (i < noOfTabs)
      {
         tabStr = tabStr + "\t" ;
         i++ ;
      }
      return tabStr;
   } //end method getTabStr

   /**
    * Returns nothing, generates ancestor and supersedes tags in the PSF file
    * for a fileset. Used in service packaging
    * Private Method
    * Called from within genFileSet
    *
    * @param tag could be ancestor or supersedes
    * @param baseProductName the base product name for this fileset
    * @param filesetName the name of this fileset
    * @param fileSetTable An hashtable containing the mappings of the filset to 
    * its corresponding list of ancestors or supersedes
    *
    * @exception GeneratorException if an error occurs.
    * @see #getFilesInFileSet
    */
   private void genAncestorAndSupersedes( String tag, String baseProductName, 
                                          String fileSetName, 
                                          Hashtable fileSetTable )
      throws GeneratorException
   {
      String fileSetList, entryInPSF = "", makefileVar = null;
      StringTokenizer semicolonTokenizer;
      int openingDelim;
      boolean foundFileSet = false;

      // don't generate this tag if it is a new fileset
      if (isNewFileSetOrLinkOrDir(fileSetName, "PATCH_NEW_FILESETS"))
        return;

      if (tag.equals("ancestor"))
      {
        makefileVar = "PATCH_ANCESTOR_LIST";
      }
      else if (tag.equals("supersedes"))
      {
        makefileVar = "PATCH_SUPERSEDES_LIST";
      }

      // check if the hashtable is null or empty
      if (fileSetTable != null && !fileSetTable.isEmpty())
      {
        if (fileSetTable.containsKey(fileSetName)) 
        {
          // the hashtable contains this fileset as a key, so get its value
          fileSetList = (String) fileSetTable.get(fileSetName);
          foundFileSet = true;
        
          if ((fileSetList != null) && (fileSetList.length() != 0))
          {
            // parse the value obtained from the hashtable with semi-colon
            // as the delimiter and generate the string to be written in the PSF
            semicolonTokenizer = new StringTokenizer(fileSetList, ";");
            while (semicolonTokenizer.hasMoreTokens())
            {
              entryInPSF += semicolonTokenizer.nextToken() + "  ";
            }
          }
          else
          {
            // the fileset is listed in the makefile variable but the corresponding
            // value is either null or empty
            if (tag.equals("ancestor"))
            {
              // This means that the ancestor list is empty for this fileset
              // Hence generate the default value
              System.err.println("Warning: The list is empty for " +
                                 fileSetName + " in " + makefileVar +
                                 ". Hence using the default value");
              entryInPSF = baseProductName + "." + fileSetName;
            }
            else if (tag.equals("supersedes"))
            {
              // This means that the supersedes list is empty for this fileset
              System.err.println("Warning: The list is empty for " +
                                 fileSetName + " in " + makefileVar + 
                                 ". Hence ignoring it");
            }
          }
        }
      }
      
      // It is not a new fileset and also not listed in the makefile variable
      // If generating the ancestor tag, put in the default value
      // If generating the supersedes tag, do not put any value
      if (tag.equals("ancestor"))
      {
        if (foundFileSet == false)
          entryInPSF = baseProductName + "." + fileSetName;
        writeString(outFile_, getTabStr() + "ancestor\t" + entryInPSF + "\n");
      }
      else if (tag.equals("supersedes") && entryInPSF != "")
        writeString(outFile_, getTabStr() + "supersedes\t" + entryInPSF + "\n");  
   } // end of method genAncestorAndSupersedes

   /**
    * Parses a makefile variable looking for a specific entry and returns true
    * if it is found, else returns false. Called from genFileSet, 
    * getFilesInFileSet, filesInShipTree, genAncestorAndSupersedes
    *
    * @param fileEntry the entry to be searched
    * @param makefileVar the makefile variable used for parsing
    */
   private boolean isNewFileSetOrLinkOrDir( String fileEntry, 
                                            String makefileVar )
   {
      String fileList = System.getProperty(makefileVar);
      String token;

      if (fileList == null || fileList.length() == 0)
        return false;

      StringTokenizer st = new StringTokenizer(fileList, ":");
      while (st.hasMoreTokens())
      {
        token = st.nextToken();
        if (token != null && token.length() != 0 && token.equals(fileEntry))
        {
          return true;
        }
      }
      return false;
   }
        
   /**
    * Returns a hashtable after populating it with the mappings of filesets
    * to their corresponding ancestor or supersedes lists
    * Called from genProduct
    *
    * @param makefileVar the makefile variable used for parsing
    * @see #genProduct
    */
   private Hashtable populateHashtable( String makefileVar )
   {
      String fileSetList = System.getProperty(makefileVar);

      if (fileSetList == null || fileSetList.length() == 0)
        return null;
      
      String fileEntry, fileEntryLHS, fileEntryRHS;
      int openingDelim;
      Hashtable ht = new Hashtable();
      StringTokenizer colonTokenizer = new StringTokenizer(fileSetList, ":");

      while (colonTokenizer.hasMoreTokens())
      {
        fileEntry = colonTokenizer.nextToken();
        
        // @ is the delimiter separating a fileset and its corresponding list
        // in a token
        openingDelim = fileEntry.indexOf("@");
        if (openingDelim != -1)
        {
          fileEntryLHS = fileEntry.substring(0, openingDelim);
          if (fileEntryLHS == null || fileEntryLHS.length() == 0)
          {
            // Skip this entry
            System.err.println("Warning: " + makefileVar +
                               " is formatted incorrectly");
            continue;
          }
          fileEntryRHS = fileEntry.substring(openingDelim + 1, 
                                             fileEntry.length());
          if (fileEntryRHS == null && fileEntryRHS.length() == 0)
            fileEntryRHS = "";
          ht.put(fileEntryLHS, fileEntryRHS);
        } 
        else
        {
          System.err.println("Warning: Invalid format in " + makefileVar);
        }
      }
      return ht;
   }
} //end class definition
