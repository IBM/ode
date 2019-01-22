//*****************************************************************************
//*                    Licensed Materials - Property of IBM
//*
//* XXXX-XXX (C) Copyright by IBM Corp. 1997.  All Rights Reserved.
//*****************************************************************************

package com.ibm.ode.pkg.parserGenerator;

import java.util.*;

/** 
 * EntitySubsetInfo class is represented as an object in the InstallEntity class. 
 * Since this stanza occurs multiple times we represent it as a seperate class.
 * This class is also a part of the repository in which the parsed information from
 * the CMF file is stored and latter used by the generator to generate control files.
 * 
 * @version     1.4 97/05/05
 * @author      Krisv
**/

 public class EntitySubsetInfo
 {
 
	 //following attributes will be found under the EntitySubsetInfo stanza
	 //of InstallEntity

	 private String subsetName_;
	 private String fullSubsetName_;
	 private PgSpecialType subsetDescription_;
	 private ArrayList subsetContent_;

	 //constructor
	 public EntitySubsetInfo()
	 {
		 //do all initialisation here
	 }

	 //set and get methods for the attribute subsetName_
	 public void setSubsetName(String subsetName)
	 {
		 subsetName_ = subsetName;
	 }

	 public String getSubsetName()
	 {
		 return subsetName_;
	 }

	 //set and get methods for the attribute fullSubsetName_
	 public void setFullSubsetName(String fullSubsetName)
	 {
		 fullSubsetName_ = fullSubsetName;
	 }

	 public String getFullSubsetName()
	 {
		 return fullSubsetName_;
	 }

	 //set and get methods for the attribute subsetDescription_
	 public void setSubsetDescription(PgSpecialType subsetDescription)
	 {
		 subsetDescription_ = subsetDescription;
	 }

	 public PgSpecialType getSubsetDescription()
	 {
		 return subsetDescription_;
	 }

	 //set and get methods for the attribute subsetContent_
	 public void setSubsetContent(ArrayList subsetContent)
	 {
		 subsetContent_ = new ArrayList(subsetContent);
	 }

	 public ArrayList getSubsetContent()
	 {
		 return subsetContent_;
	 }
 } //end of EntitySubsetInfo class
