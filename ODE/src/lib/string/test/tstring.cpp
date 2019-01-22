#include <iostream.h>
#include <base/binbase.hpp>
#include "lib/string/string.hpp"

int main( int argc, const char **argv, const char **envp)
{
  Tool::init( envp );
  String linesep = "##############################";

  String s1a;
  String s1b(s1a);
  String s1c( (int)1 );
#if 0
  String s1d( (double)1 );
#endif
  String s1e( 'a' );
  String s1f( "test" );

  cout << "   String " << s1f << endl;
  
  cout << endl << linesep << " Test #1 " << endl;

  String s2 = String("test1") + s1e;
  String s2b;
  String s2c;
  String s2d( s2b + s2c );

  cout << endl << "testing +/+=/= " << endl;
  cout << " s2d len= " << s2d.length() << " s2d = `" << s2d << "'" << endl;
  s2c = "some stuff";
  cout << " s2c = " << s2c << ", s2 = " << s2 << endl;
  s2c += s2;
  cout << " s2c += s2, s2c = " << s2c << endl;

  cout << endl << "testing concat" << endl;
  s2c = "some stuff";
  cout << " s2c = " << s2c << ", s2 = " << s2 << endl;
  s2c = s2c.concat( s2 );
  cout << " s2c = s2c.concat( s2 ), s2c = " << s2c << endl;
  
  cout << endl << "testing startsWith/endsWith" << endl;
  String s3a( "-usage" );
  if (s3a.startsWith( "-" ))
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  String s3b("xxx/yyy/.");
  if (!s3b.startsWith( "-" ))
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;
  if (s3b.endsWith( "y/." ))
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;
  if (s3b.endsWith( "xy/." ))
    cout << "Test failed" << endl;
  else
    cout << "Test passed" << endl;

  cout << endl << "testing trim" << endl;
  String s4a( "abc def      ge" );
  String s4b( "        abc");
  String s4c( "           def       f              ");
  cout << "\"" << s4a << "\"" << " trim = " << "\"" << s4a.trim() << "\"" << endl;
  cout << "\"" << s4b << "\"" << " trim = " << "\"" << s4b.trim() << "\"" << endl;
  cout << "\"" << s4c << "\"" << " trim = " << "\"" << s4c.trim() << "\"" << endl;

  String s5a = "abc defghijklmnop sfff";
  String s5b = "de";
  String s5c = "ssff";
  
  cout << endl << "testing substring of '" << s5a << "'" << endl;
  cout << " substring(3, 8)   is : " << s5a.substring(3, 8) << endl;
  cout << " substring(1, 11)  is : " << s5a.substring(1, 11) << endl;
  cout << " substring(1, 23)  is : " << s5a.substring(1, 23) << endl;
  cout << " substring(6)   is : " << s5a.substring(6) << endl;

  cout << endl << "testing indexOf" << endl;
  cout << "indexOf " << s5b << " in " << s5a << " is :" << s5a.indexOf( s5b ) << endl;
  cout << "indexOf `' in " << s5a << " is :" << s5a.indexOf( "" ) << endl;
  cout << "indexOf `g' in " << s5a << " is :" << s5a.indexOf( 'g' ) << endl;
  cout << "indexOf " << s5c << " in " << s5a << " is :" << s5a.indexOf( s5c ) << endl;
  cout << endl << "testing lastIndexOf" << endl;
  cout << "lastIndexOf " << s5b << " in " << s5a << " is :" << s5a.lastIndexOf( s5b ) << endl;
  cout << "lastIndexOf '' in " << s5a << " is :" << s5a.lastIndexOf( "" ) << endl;
  cout << "lastIndexOf `g' in " << s5a << " is :" << s5a.lastIndexOf( 'g' ) << endl;
  cout << "lastIndexOf " << s5c << " in " << s5a << " is :" << s5a.lastIndexOf( s5c ) << endl;
  cout << "lastIndexOf 'f' in " << s5a << " is :" << s5a.lastIndexOf( 'f' ) << endl;

  cout << endl << "testing equals" << endl;
  String s9a( "val1" ), s9b( "val1" ), s9c( "val1 ");
  String s9d( "ODEi2.1" );
  if (s9a.equals( s9b ))
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  if (s9a.equals( s9c ))
    cout << "Test failed" << endl;
  else
    cout << "Test passed" << endl;

  if (s9a != s9c)
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  if (s9d == "ODEi2.1")
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  cout << endl << "testing charAt" << endl;
  String s10a( "123456789" );
  cout << "charAt(" << 6 << ") in " << s10a << " is " << s10a.charAt( 6 ) << endl;
  cout << "charAt(" << 7 << ") in " << s10a << " is " << s10a.charAt( 7 ) << endl;
  cout << "charAt(" << 1 << ") in " << s10a << " is " << s10a.charAt( 1 ) << endl;

  cout << endl << "testing replace" << endl;
  String s11a( "abc dbc abd" );
  String s11b( "a" );
  String s11c( "zzz" );
  String s11d( "ab" );
  String s11e;
  String s11f( " " );
  String s11g( "abc" );
  String s11h( "c" );
  cout << "replace " << s11h << " with " << s11g << " in " << s11a << " is " << s11a.replace( s11h, s11g ) << endl;
  cout << "replace " << s11b << " with " << s11c << " in " << s11a << " is " << s11a.replace( s11b, s11c ) << endl;
  cout << "replace " << s11d << " with " << s11c << " in " << s11a << " is " << s11a.replace( s11d, s11c ) << endl;
  cout << "replace '" << s11e << "' with " << s11c << " in " << s11a << " is " << s11a.replace( s11e, s11c ) << endl;
  cout << "replace '" << s11f << "' with " << s11b << " in " << s11a << " is " << s11a.replace( s11f, s11b ) << endl;
  cout << "replace 'ab' with 'z' in " << s11a << " is " << s11a.replace( "ab", "z" ) << endl;
  cout << "replace 'ab' once with 'z' in " << s11a << " is " << s11a.replace( "ab", "z", 1 ) << endl;
  cout << "replace 'ab' once with '' starting at index 2 in " << s11a << " is " << s11a.replace( "ab", "", 1, 2 ) << endl;
  cout << "replace abc with '' in " << s11g << " is " << s11g.replace( "abc", "" ) << endl;

  cout << endl << "testing isDigits/asInt" << endl;
  String s12a = "2";
  if (s12a.isDigits())
    cout << "Yes, " << s12a << " is digits" << endl;
  else
    cout << "No, " << s12a << "is not digits" << endl;

  String s12b = "2453453";
  if (s12b.isDigits())
    cout << "Yes, " << s12b << " is digits" << endl;
  else
    cout << "No, " << s12b << "is not digits" << endl;

  int i12 = s12b.asInt();
  cout << "i12 = " << i12 << endl;

  String s12c = "yyy";
  if (s12c.isDigits())
    cout << "Yes, " << s12c << " is digits" << endl;
  else
    cout << "No, " << s12c << "is not digits" << endl;

  cout << endl << "testing lowercase/UPPERCASE" << endl;
  String s14a( "aBc Def 123 GXZz" );
  String s14b( "abc def 123 gxzz" );
  String s14c( "ABC DEF 123 GXZZ" );
  String s14d;
  s14d = s14d.toUpperCase(); // test boundaries
  if (s14b.equals(s14a.toLowerCase()))
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  if (s14c.equals(s14a.toUpperCase()))
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  cout << endl << "testing equalsIgnoreCase" << endl;
  if (s14c.equalsIgnoreCase(s14a))
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  cout << endl << "testing remove" << endl;
  String s15a( "1234567890123456789" );
  String s15b( s15a );
  cout << "remove from idx 3, 6 chars in " << s15a;
  cout << " is " << s15a.remove( 3, 6 ) << endl;

  cout << endl << "testing comparison ops >,<,..." << endl;
  String s16a( "fooa.c" );
  String s16b( "foob.c" );
  String s16c( "foob.c" );
  if (s16a < s16b)
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  if (s16a <= s16b)
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  if (s16b > s16a)
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  if (s16b >= s16c)
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;
  
  cout << endl << "testing null strings" << endl;
  String s17a;
  String s17c( "" );

  if (s17a.isEmpty() && s17c.isEmpty())
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  if (s17c == "")
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  if (s17a == "")
    cout << "Test passed" << endl;
  else
    cout << "Test failed" << endl;

  cout << endl << "testing dequote" << endl;
  {
    String s( "\"quoted 'string'\"" );

    cout << "Test 1 " << ((s.dequote() == "quoted 'string'") ?
        "passed." : "failed.") << endl;
    s.dequoteThis();
    cout << "Test 2 " << ((s == "quoted 'string'") ?
        "passed." : "failed.") << endl;
  }

  return 0;
}
