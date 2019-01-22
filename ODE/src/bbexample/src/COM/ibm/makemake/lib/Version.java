package COM.ibm.makemake.lib;

/**
 * This Interface is used to define build-specific information.
 * Some of these constants should be changed for each build.
**/
public interface Version
{
  /**
   * The build number.
  **/
  public static final String BUILD = "0a";

  /**
   * The build date.
  **/
  public static final String BUILD_DATE = "13-APR-1999";

  /**
   * The release number.
  **/
  public static final String RELEASE = "ODEi2.3";

  /**
   * The version is just a combination of the release and build number.
  **/
  public static final String VERSION = RELEASE + " (build " + BUILD + ")";
}
