package com.ibm.ode.lib.util;

/**
 * This exception is thrown when an lookup for a BPS property found no
 * value.
 *
 * @version 1.2 97/09/30
 * @author Heng Chu 
 */
public class PropertyLookupException extends RuntimeException
{
  /**********************************************************************
   * Just create a generic message
   */
  public PropertyLookupException()
  {
    super("A BPS Property lookup failed!");
  }

  /**********************************************************************
   * Create a meaningful message
   *
   * @param property the BPS property name
   */
  public PropertyLookupException(String property)
  {
    super("The BPS Property " + property + " lookup failed!");
  }

  /**********************************************************************
   * Create a meaningful message
   *
   * @param property the BPS property name
   * @param message an additional message
   */
  public PropertyLookupException(String property,
                                 String message)
  {
    super("The BPS Property " + property + " lookup failed: " + message);
  }
}
