package com.ibm.ode.pkg.pkgCommon;

import com.ibm.ode.lib.util.EnumBase;
import java.util.*;

/**
 * This Enum class defines all possible
 * values for PKG_TYPE environment variable
 * used in packaging subsystem
 *
 * @version 98/08/20 19:14:51
 * @author Chary Lingachary
**/
public class PackageType extends EnumBase
{
  /**
   * class name for this enum class
   **/
  public static String className_ = "PackageType";

  /**
   * possible values for PKG_TYPE
   **/
  public static final int USER = 1;
  public static final int OFFICIAL = 2;

  static
  {
    addValuePair( USER, "USER", className_ );
    addValuePair( OFFICIAL,"OFFICIAL", className_ );
  }

  public PackageType( int _value )
  {
     setInt( _value );
  }

  public PackageType( String _str )
  {
     setString( _str );
  }

  public PackageType( PackageType _orig )
  {
     copy( _orig );
  }

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

  public void setString( String _str )
  {
     try
     {
        setString( _str, className_ );
     }
     catch( NoSuchElementException X )
     {
        throw new NoSuchElementException( className_ + ":" + _str );
     }
  }

  public int getInt( String _str )
  {
     return getInt( _str, className_ );
  }

  public String getString( int _value )
  {
     return getString( _value, className_ );
  }

  public Enumeration getAllInts()
  {
          return getAllInts( className_ );
  }

  public Enumeration getAllStrings() 
  {
     return getAllStrings( className_ );
  }

  public String toString() 
  {
     return toString( className_ );
  }

  public synchronized void copy( PackageType _orig )
  {
     // set actual int value
     setInt ( _orig.getInt(), className_ );
  }

  public Object clone()
  {
     return new PackageType( this );
  }

  /**
   * Compares two PackageType objects
   *
   * @param _object PackageType object to compare
   */
  public boolean equals( PackageType _object )
  {
     if( _object.getInt() == getInt() )
     {
        return true;
     }

     return false;
  }
}

