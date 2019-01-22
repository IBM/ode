package com.ibm.sdwb.ode.core;

import org.eclipse.core.resources.IProject;


/**
 * @author kiranl
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class ODEMakefileProperties extends ODEProperties
{
	// Which targets to generate
	boolean generateJavaClasses; // Generate JAVA_CLASSES targets
	boolean generateJavaDocs; // Generate JAVADOC targets
	boolean generateJars; // Generate JARS Targets
	boolean generateObjects; // Generate OBJECTS Targets
	boolean generateIncludes; // Generate EXPORT Targets
	boolean runJavadocAfterJavac; // Run JAVADOC pass after JAVAC pass

	/**
	 * Constructor for MakefileProperties.
	 */
	public ODEMakefileProperties( IProject project )
	{
		super( project );
		this.generateJavaClasses = super.b7;
		this.generateJavaDocs = super.b6;
		this.generateJars = super.b8;
		this.generateObjects = super.b12;
		this.generateIncludes = super.b13;
		this.runJavadocAfterJavac = super.b14;
	}

	public void readProperties()
	{
		super.readProperties();
		this.generateJavaClasses = super.b7;
		this.generateJavaDocs = super.b6;
		this.generateJars = super.b8;
		this.generateObjects = super.b12;
		this.generateIncludes = super.b13;
		this.runJavadocAfterJavac = super.b14;
	}
}
