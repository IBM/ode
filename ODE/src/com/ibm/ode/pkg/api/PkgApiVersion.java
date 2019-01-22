package com.ibm.ode.pkg.api;

/**
 * Class to define the version and supported versions of the 
 * Packaging API.
 */

public class PkgApiVersion 
{
  
  private static String pkgApiVersion_ = "2.0";
  private static String[] pkgApiSupportedVersions_;

  static
	{
		// Initialize array with supported versions
    pkgApiSupportedVersions_ = new String[1];
    pkgApiSupportedVersions_[0] = "2.0";
  }

  /**
   * Returns the Pkg API version
   * @return The version number of this Packaging Api
   **/
  public static String getPkgApiVersion()
	{
    return pkgApiVersion_;
	}

  /**
   * Return the versions of the API supported by this versiob
   * @return The version numbers of all Packaging API versions
   # supported by this version of the API.
   **/
  public static String[] getPkgApiSupportedVersions()
	{
    return pkgApiSupportedVersions_;
	}

  /**********************************************************************
   * Print ODE packaging API version number.
   */
  public static void main( String[] args )
  {
    System.out.println(getPkgApiVersion()); // Print out Ode version.
  }

}

