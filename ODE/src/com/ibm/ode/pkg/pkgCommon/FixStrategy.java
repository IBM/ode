package com.ibm.ode.pkg.pkgCommon;

import com.ibm.ode.lib.util.*;
import java.util.NoSuchElementException;
import java.util.*;


/**
 * Class FixStrategy which is a subclass of EnumBase that defines the real int-String value pairs. Each
 *  of such subclasses corresponds to C/C++ enum type. 
 * For example:
 *</pre>
 *      FixStrategy b = new FixStrategy();
 *</pre>
 * @version 1.13 98/08/20
 * @author      Abhaya Singh(abhaya@raleigh.ibm.com)
 **/
public class FixStrategy extends EnumBase {
  
  /**
   * class name for this enum class
   */
  public static String className_ = "FixStrategy";
  
  //
  // The enum values
  //  
  public static final int REFRESH = 1;
  public static final int CUMULATIVE = 2;
  
  static
  {
    addValuePair(REFRESH, "REFRESH",className_);
    addValuePair(CUMULATIVE, "CUMULATIVE",className_);
  }
  
  //************************************************************************
  // Assign the default value if this is a concern
  //
  public FixStrategy() {
    
    super();
    setInt(REFRESH,className_);
  }
  
  
  //************************************************************************
  // Construct with a given integer  value
  //
  public FixStrategy(int _value) {
    
    setInt(_value);
  }
  
  /**
   * Construct FixStrategy value for a given string value
   */
  
  public FixStrategy( String _str )
    {
      setString( _str);
    }
  
  
  public FixStrategy( FixStrategy _orig )
    {
      copy( _orig );
    }
  
  /**
   * This method overrides the base class method and prevents
   * user from passing in the class name
   *
   * @param current FixStrategy value as Int
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
   * @param current FixStrategy value as String
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
  // @return All the legal FixStrategy/String pairs, and the current enum value
  //
  
  public String toString() 
    {
      return toString( className_ );
    }
  
  public synchronized void copy( FixStrategy _orig )
    {
      // set actual int value
      setInt ( _orig.getInt(), className_ );
    }
  
  public Object clone()
    {
      return new FixStrategy( this );
    }
  
  public boolean equals(FixStrategy inFixStrategy)
    {
      
      if ( getInt() == inFixStrategy.getInt() )
	
	return true;
      
      else return false;
      
    }
  
  }










