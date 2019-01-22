package com.ibm.ode.lib.util;

import java.util.*;
import java.io.*;

/** 
 * This class provides an enum-like facility that can store enum as both
 * integer and String values. Mapping between integer and String is
 * provided. Actual integer-String value pairs are specified at subclasses.
 *
 * The general restriction is that the integer and String values should be
 * unique within the same subclass. Exceptions will be thrown if duplicated
 * integer or String values are inserted.
 *
 * <UL>
 * <LI><B>Typical Use</B> Typical use of this class is creating an
 * enum-like class from this, and define all int-String pairs in the
 * a static block and define the toString() function, etc.  The static block
 * is used so the int-String values can be used without creating the object.
 * This is why the subclass name, name_, has to be supplied in some of the
 * methods.
 * <UL> 
 *  <LI>Create a subclass that defines the real int-String value pairs. Each
 *  of such subclasses corresponds to C/C++ enum type. For example, 
 *  <PRE>
 * //
 * // A sample status class derived from EnumBase. This shows typically how one
 * // would create a new enum class. The legal enum const integer/String values
 * // are (OK, "OK") and (FAIL, "FAIL")
 * //
 * public class MyStatus extends EnumBase {
 * //
 * // The enum values
 * //
 * public static final int UNINITIALIZED = 0;
 * public static final int OK = UNINITIALIZED + 1;
 * public static final int FAIL = OK + 1;
 *
 *
 * //************************************************************************
 * // Assign the default value if this is a concern
 * //
 * static 
 * {
 *   addValuePair(UNINITIALIZED, "", name_);
 *   addValuePair(OK, "OK", name_);
 *   addValuePair(FAIL, "FAIL", name_);
 * }
 * public MyStatus() 
 * {
 *   setInt(UNINITIALIZED);
 * }
 *
 * //************************************************************************
 * // Construct with a specify enum value
 * //
 * public MyStatus(int value) 
 * {
 *   setInt(value, name_);
 * }
 *
 * //************************************************************************
 * // (Optional) Print the value pair it currently has
 * //
 * // @return All the legal enum/String pairs, and the current enum value
 * //
 * public String toString() {
 *   return 
 *     "MyStatus enum value pairs:\n" + super.toString(name_) +
 *     "Current enum value: (" + getInt() +", '" + getString(name_) + "')";
 * }
 *}
 *  </PRE>
 *  <LI>Then whenever an object of MyStatus is needed, add code like
 *    <PRE>
 *    MyStatus status = new MyStatus();
 *    </PRE>
 *  <LI>Use the variable "status" as follows:
 *    <PRE>
 *      status.setInt(MyStatus.OK, name_);
 *      int value = status.getInt();
 *      status.setString("FAIL", name_);
 *      if (status.equals(MyStatus.OK)) {...}
 *      MyStatus newStatus = new MyStatus(MyStatus.FAIL);
 *      if (status.equals(newStatus)) {...}        
 *      for (Enumeration e = status.getAllStrings(name_);e.hasMoreElements();) {...}
 *    </PRE>
 * </UL><BR><BR>
 *
 * <LI><B>Error Handling Policy</B> throw IllegalArgumentException if add a
 * duplicate integer or String value via addValuePair(); throw
 * NoSuchElementException if invalid (non-existent) int or String value is
 * given to setInt() or setString().
 * </UL>
 *
 * Note that since we expect the subclass to define specific int-String
 * value pairs, we make ths class abstract even though it doesn't have
 * abstract methods.
 * 
 * We might take advantage of JGL.
 *
 * @version 1.3 97/04/17
 * @author Heng Chu */
abstract public class EnumBase implements Serializable, Cloneable
{
  // The following two static Hashtables can eventually contain all of 
  // the int-String value pairs for all of the subclasses.  These value
  // pairs for subclasses no longer being referenced should be removed from the
  // static Hashtables, but there is no way to find this out.

  /**
   * The container storing the subclassName->intToString_ Hashtable mapping.
   */
  private static Hashtable subclassNameToIntToString_ = new Hashtable();

  /**
   * The container storing the subclassName->stringToInt_ Hashtable mapping.
   */
  private static Hashtable subclassNameToStringToInt_ = new Hashtable();

  /**
   * The current integer enum value. 
   */
  private int value_;

  /**
   * The table of subclass objects
  **/
  private static Hashtable subclasses_ = new Hashtable();


  /************************************************************************
   * Retrieve the current integer value.
   *
   * @return the current enum integer value
   */
  public int getInt() 
  {
    return value_;
  }

  /************************************************************************
   * Retrieve the String value based on the subclassName.  The subclassName
   * is needed to retrieve the appropriate intToString Hashtable.
   *
   * @param subclassName the name of the subclass
   *
   * @return the current String value
   */
  public String getString() 
      throws NoSuchElementException
  {
    return getString( getMyClassName() );
  }

  public String getString(String subclassName) 
      throws NoSuchElementException
  {
    Hashtable intToString = (Hashtable)subclassNameToIntToString_.get(subclassName);

    // get the String object
    String string = (String)(intToString.get( new Integer(value_) ));

    // return proper value or throw an exception
    if (string == null)
      throw new NoSuchElementException();
    else
      return string;
  }

  /************************************************************************
   * Set the value with an integer
   *
   * @param value the new enum integer value
   * @param subclassName the name of the subclass
   */
  public void setInt(int value) 
  {
    setInt( value, getMyClassName() );
  }

  public void setInt(int value, String subclassName) 
  {
    // First does a look up in the table, if not there, we will throw an
    // exception
    getString(value, subclassName);
    value_ = value;
  }

  /************************************************************************
   * Set the string value
   *
   * @param string the new String value
   * @param subclassName the name of the subclass
   */
  public void setString(String string) 
  {
    setString( string, getMyClassName() );
  }

  public void setString(String string, String subclassName) 
  {
    // Does a lookup first, and assign the mapped integer value
    value_ = getInt(string, subclassName);
  }

  /************************************************************************
   * Add a new integer-String pair. Better be executed in the subclass
   * constructor. So we make this method protected.
   *
   * @param value the enum integer value to be added.
   * @param string the enum String value to be added.
   * @param subclassName the subclass name.
   *
   */
  protected static void addValuePair( int    value,
                                      String string,
                                      String subclassName )
  {
    Hashtable intToString = (Hashtable)subclassNameToIntToString_.get(subclassName);
    Hashtable stringToInt = (Hashtable)subclassNameToStringToInt_.get(subclassName);

    // If the reference to either of the two Hashtables is null, this must be a new
    // subclass so add it to both subclass Hastables.
    if ( intToString == null )
    {
      intToString = new Hashtable();
      stringToInt = new Hashtable();

      subclassNameToIntToString_.put(subclassName, intToString);
      subclassNameToStringToInt_.put(subclassName, stringToInt);
    }

    Integer val = new Integer(value);

    // If a value for the specified key already exits then it is replaced with
    // the specified value.
    intToString.put( val, string );
    stringToInt.put( string, val );
  }

  /************************************************************************
   * Given an integer value, return the mapped string. 
   * 
   * We explicitly throw a NoSuchElementException (doesn't have to be
   * explicitly caught) even though Hashtable.get() method throws
   * NullPointerException. We think our exception makes more sense and since
   * it's in our control, it allows us to use a different implementation
   * (such as JGL) for the mapping tables.
   *
   * @param value the enum integer value
   *
   * @return the mapped String value
   *
   * @exception NoSuchElementException if given an illegal enum integer value
   */
  public static String getString(int value, String subclassName) 
    throws NoSuchElementException 
  {
    Hashtable intToString = (Hashtable)subclassNameToIntToString_.get(subclassName);

    // get the String object
    String string = (String)(intToString.get(new Integer(value)));

    // return proper value or throw an exception
    if (string == null)
      throw new NoSuchElementException();
    else
      return string;
  }

  /************************************************************************
   * Given a string, return the mapped integer value. See exception comment
   * above.
   *
   * @param string the enum String value
   * @param subclassName the subclass name.
   *
   * @return the mapped enum integer value
   *
   * @exception NoSuchElementException if given an illegal enum String value
   */
  public static int getInt(String string, String subclassName) 
    throws NoSuchElementException 
  {
    Hashtable stringToInt = (Hashtable)subclassNameToStringToInt_.get(subclassName);

    // get the integer value
    Integer value = ((Integer)(stringToInt.get(string)));

    // return proper value or throw an exception
    if (value == null)
      throw(new NoSuchElementException());
    else
      return value.intValue();
  }

  /************************************************************************
   * Get all the integer values as an Enumeration of *Integer*. We might
   * want to use JGL library classes since Enumeration is too limited.
   *
   * @param subclassName the subclass name.
   *
   * @return the enumeration (in *Integer*) of all legal integer values
   */
  public static Enumeration getAllInts(String subclassName)
  {
    Hashtable intToString = (Hashtable)subclassNameToIntToString_.get(subclassName);
    return intToString.keys();
  }

  /************************************************************************
   * Get all the string values as an Enumeration of String. We might want
   * to use JGL library classes. 
   *
   * @param subclassName the subclass name.
   *
   * @return the enumeration (in String) of all legal String values
   */
  public static Enumeration getAllStrings(String subclassName) 
  {
    Hashtable stringToInt = (Hashtable)subclassNameToStringToInt_.get(subclassName);
    return stringToInt.keys();
  }

  /************************************************************************
   * Compare the current enum integer value with another
   *
   * @param value any integer value
   *
   * @return true if equals, false otherwise
   */
  public boolean equals(int value) 
  {
    return value_ == value;
  }

  /************************************************************************
   * Compare with another EnumBase object
   * 
   * @param enumer another EnumBase object
   *
   * @return true if equals, false otherwise
   */
  public boolean equals(EnumBase enumer)
  {
    return value_ == enumer.value_;
  }

  /************************************************************************
   * Print all the value pairs. Overrides Object.toString()
   *
   * @param subclassName the subclass name.
   *
   * @return all the int-String value pairs
   */
  public String toString(String subclassName) 
  {
    Enumeration e = getAllInts(subclassName);
    int value = getInt();
    String result = subclassName + " current enum value: \n"
                                 + "  (" + value +", '" + getString(value, subclassName) + "') \n\n"
                                 + subclassName + " Enum - value pairs: \n";

    while (e.hasMoreElements()) 
    {
      value = ((Integer)(e.nextElement())).intValue();
      result += "  (" + value + ", " + getString(value, subclassName) + ")\n";
    }

    return result;
  }

  // A helper method used when the classname was not passed into a method call
  // that wants it.
  protected String getMyClassName()
  {
    String s;
    
    // instances of classes derived from this one will get their
    // derived class names here!
    s = getClass().getName();
    return s.substring( s.lastIndexOf('.')+1 );
  }
}
