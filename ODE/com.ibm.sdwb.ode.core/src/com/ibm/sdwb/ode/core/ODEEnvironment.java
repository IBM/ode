package com.ibm.sdwb.ode.core;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;


// import org.eclipse.jdt.core.*;
/**
 * @author ODE Team This class implements static methods aimed at helping to get
 *         environment variable, flags, ...
 */
public class ODEEnvironment
{
	/**
	 * Gets the outputFolder of this project. This is currently not used.
	 * 
	 * @param project is the current Project
	 */
	public static String getOutputFolder( IProject project )
	{
		// ODECommonUtilities.PrintDebugInformation("Envrionment::GetOutputFolder","No
		// action");
		IPath outputFolder = project.getLocation();
		IWorkspace workspace = project.getProject().getWorkspace();
		IResource outLoc = workspace.getRoot().findMember( outputFolder );
		return outLoc.getLocation().toOSString();
	}
	/*
	 * No longer used, but may be able to scrounge something useful private
	 * static String getClasspath(IJavaProject project, boolean exportedOnly)
	 * throws JavaModelException {
	 * //ODECommonUtilities.PrintDebugInformation("ODEEnvironment::GetClassPath(params)","No
	 * action"); String cp = getOutputFolder(project.getProject());
	 * IClasspathEntry[] classpath = project.getRawClasspath();
	 * project.getResolvedClasspath(true); for (int i = 0; i < classpath.length;
	 * i++) { IClasspathEntry entry = classpath[i]; switch
	 * (entry.getEntryKind()) { case IClasspathEntry.CPE_SOURCE: break; case
	 * IClasspathEntry.CPE_PROJECT: break; // will add them later case
	 * IClasspathEntry.CPE_VARIABLE: // if
	 * (entry.getPath().toString().equals(JavaRuntime.JRELIB_VARIABLE)) { //
	 * continue; // } else { entry = JavaCore.getResolvedClasspathEntry(entry); // } //
	 * fall through default: cp += resolveClasspathEntry(project, entry); break; } }
	 * String reqProjects[] = project.getRequiredProjectNames(); for (int i = 0;
	 * i < reqProjects.length; i++) { IJavaProject reqPrj =
	 * project.getJavaModel().getJavaProject(reqProjects[i]); if
	 * (reqPrj.exists() && reqPrj.isOpen()) { cp +=
	 * System.getProperty("path.separator") + getClasspath(reqPrj, true); } }
	 * return cp; }
	 * 
	 * 
	 * private static String resolveClasspathEntry(IJavaProject project,
	 * IClasspathEntry entry) throws JavaModelException {
	 * //ODECommonUtilities.PrintDebugInformation("ODEEnvironment::resolveClasspathentry","No
	 * action"); String separator = System.getProperty("path.separator");
	 * IPackageFragmentRoot[] roots = project.findPackageFragmentRoots(entry);
	 * String cp = ""; for (int i = 0; i < roots.length; i++) { cp += separator;
	 * IPackageFragmentRoot root = roots[i]; if (root.isExternal()) { cp +=
	 * root.getPath().toOSString(); } else { IResource res =
	 * root.getCorrespondingResource(); cp += res.getLocation().toOSString(); } }
	 * return cp; }
	 */
}
