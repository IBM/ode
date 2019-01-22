package com.ibm.ode.pkg.pkgCommon;

import com.ibm.ode.lib.util.*;
import java.util.NoSuchElementException;
import java.util.*;
import com.ibm.ode.lib.util.EnumBase;


/**
 * Class PackageClassification which is a subclass of EnumBase that defines the real int-String value pairs. Each
 *  of such subclasses corresponds to C/C++ enum type. 
 * For example:
 *</pre>
 *      PackageClassification b = new PackageClassification();
 *</pre>
 * @version 1.12 98/08/20
 * @author      Abhaya Singh(abhaya@raleigh.ibm.com)
 **/
public class PackageClassification extends EnumBase {
  
  /**
   * class name for this enum class
   **/
  public static String className_ = "PackageClassification";
  
  
  //
  // The enum values
  //  
  public static final int IPP = 1;
  public static final int SP = 2;
  
  
  static
  {
    addValuePair(IPP, "IPP",className_);
    addValuePair(SP, "SP",className_);
  }
    
  //************************************************************************
  // Assign the default value if this is a concern
  //
  public PackageClassification() {
    
    super();
    setInt(IPP,className_);
  }
  
  //************************************************************************
  // Construct with a specific enum value
  //
  public PackageClassification(int _value) {
    
    setInt(_value);
  }
  
  /**
   * Construct Platform value for a given string value
   */
  
  
  public PackageClassification( String _str )
    {
      setString( _str);
    }
  
  
  public PackageClassification( PackageClassification _orig )
    {
      copy( _orig );
    }
  
  /**
   * This method overrides the base class method and prevents
   * user from passing in the class name
   *
   * @param current PackageClassification value as Int
   * @author Chary Lingachary
   */
  public void setInt( int _value )
    {
      try
	{
	  setInt( _value, className_ );
	}
      catch( NoSuchElementException X )
	{
	  throw new NoSuchElementException(className_ + ":" + _value );
	}
    }
  
  /**
   * This method overrides the base class method and prevents
   * user from passing in the class name
   *
   * @param current PackageClassification value as String
   * @author Chary Lingachary
   */
  
  public void setString( String _str )
    {
      try
	{
	  if (_str != null)
	  setString( _str.toUpperCase(), className_ );
	}
      catch( NoSuchElementException X )
	{
	  throw new NoSuchElementException( className_ + ":" + _str );
	}
    }
  
  public int getInt( String _str )
    {
      return getInt( _str );
    }
  
  
  /**
   * This method overrides the base class method and prevents
   * user from passing in the class name
   *
   * @author Chary Lingachary
   */
  
  public String getString() throws NoSuchElementException
    {
      return super.getString( className_ );
    }
  
  public String getString( int _value )
    {
      return getString( _value );
    }
  
  public Enumeration getAllInts()
    {
      return getAllInts( className_ );
    }
  
  public Enumeration getAllStrings() 
    {
      return getAllStrings( className_ );
    }
  
  //************************************************************************
  // (Optional) Print the value pair it currently has
  //
  // @return All the legal PackageClassification/String pairs, and the current enum value
  //
  
  public String toString() 
    {
      return toString( className_ );
    }
  
  public synchronized void copy( PackageClassification _orig )
    {
      // set actual int value
      setInt ( _orig.getInt(), className_ );
    }
  
  public Object clone()
    {
      return new PackageClassification( this );
    }
  
  
  public boolean equals(PackageClassification inPackageClassification)
    {
      
      if ( getInt() == inPackageClassification.getInt() )
	
	return true;
      
      else return false;
      
    }
  
  
}









