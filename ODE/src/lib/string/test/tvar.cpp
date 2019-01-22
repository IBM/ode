/**
 * Test the Variable class.
 *
 * Usage: tvar <${variable[:mod...]}>
**/
#include <stdlib.h>
#include <iostream.h>

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/strcon.hpp"
#include "lib/string/variable.hpp"
#include "lib/string/sboxcon.hpp"
#include "lib/string/env.hpp"
#include "lib/io/path.hpp"
#include "lib/exceptn/mfvarexc.hpp"

class PathFinder : public StringFindable
{
  public:

    boolean stringFinder( const String &str,
        StringArray &paths, boolean doWildCards ) const;
};

static int num_tests = 0, num_fails = 0;
static PathFinder path_finder;
static Variable var( Env::getSetVars(), &path_finder );
static StringArray results;


boolean PathFinder::stringFinder( const String &str,
    StringArray &paths, boolean doWildCards ) const
{
  paths.setNumElements( 0 );
  if (Path::absolute( str ))
  {
    if (Path::exists( str ))
      paths.add( str );
  }
  else
  {
    StringArray words;
    const String *path = Env::getenv( "PATH" );
    if (path != 0)
      path->split( Path::PATH_SEPARATOR, UINT_MAX, &words );
    for (int i = ARRAY_FIRST_INDEX; i <= words.lastIndex(); ++i)
    {
      words[i] += Path::DIR_SEPARATOR;
      words[i] += str;
      if (Path::exists( words[i] ))
        paths.add( words[i] );
    }
  }

  return (paths.size() > 0);
}


void verify( const String &parse, const String &expect )
{
  ++num_tests;

  try
  {
    var.evaluate( parse, &results );
  }
  catch (MalformedVariable &e)
  {
    results[ARRAY_FIRST_INDEX] = "MalformedVariable EXCEPTION!";
  }
  if (expect != results[ARRAY_FIRST_INDEX])
  {
    cerr << "ERROR: expected \"" << expect <<
        "\" and got \"" << results[ARRAY_FIRST_INDEX] << "\"." << endl;
    cerr << "Above error from parsing: \"" << parse << "\"." << endl;
    ++num_fails;
  }
}

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  String mkpath_test1 = "subtest1/subtest2";
  String mkpath_test1_head = "subtest1"; // to make removal easy
  String mkpath_test2 = "subtest3/subtest4/subtest5";
  String mkpath_test2_head = "subtest3"; // to make removal easy

  Env::setenv( "PATH1", "/ . ./file.c /dir/file.h file", true );
  Env::setenv( "PATH2", String( " " ) + mkpath_test1 + " " +
      Path::PATH_SEPARATOR + " " + Path::PATH_SEPARATOR + " " +
      mkpath_test2 + " ", true );
  Env::setenv( "STR1", "Kiss my ASPHALT!", true );
  Env::setenv( "STR2", "one two three four five", true );
  Env::setenv( "STR3", "one.c two.h three.c four.o five.c", true );
  Env::setenv( "EMPTY", "", true );

  // path-style mods
  verify( "${PATH1:E}", "c h" );
  verify( "${PATH1:H}", ". . /dir ." );
  verify( "${PATH1:R}", "/ ./file /dir/file file" );
  verify( "${PATH1:T}", ". file.c file.h file" );

  // existence
  verify( "${PATH1:XB}", "/ ." );

  // case conversion
  verify( "${STR1:u}", "KISS MY ASPHALT!" );
  verify( "${STR1:l}", "kiss my asphalt!" );

  // blank/nonblank
  verify( "${STR1:bOTHER}", "OTHER" );
  verify( "${NONSTR1:bOTHER}", "" );
  verify( "${EMPTY:bOTHER}", "" );
  verify( "${STR1:BOTHER}", "Kiss my ASPHALT!" );
  verify( "${NONSTR1:BOTHER}", "OTHER" );
  verify( "${EMPTY:BOTHER}", "OTHER" );

  // defined/undefined
  verify( "${STR1:DOTHER}", "OTHER" );
  verify( "${EMPTY:DOTHER}", "OTHER" );
  verify( "${NONSTR1:DOTHER}", "" );
  verify( "${STR1:UOTHER}", "Kiss my ASPHALT!" );
  verify( "${EMPTY:UOTHER}", "" );
  verify( "${NONSTR1:UOTHER}", "OTHER" );

  // literal variable name
  verify( "${NONSTR1:L}", "NONSTR1" );

  // matching/non-matching
  verify( "${STR1:M*!}", "ASPHALT!" );
  verify( "${STR1:Mm[xyz]}", "my" );
  verify( "${PATH1:M*l*}", "./file.c /dir/file.h file" );
  verify( "${STR1:N*!}", "Kiss my" );
  verify( "${STR1:Nm[xyz]}", "Kiss ASPHALT!" );
  verify( "${PATH1:N*l*}", "/ ." );
  verify( "${STR2:Mone five three}", "one three five" );
  verify( "${STR2:None five three}", "two four" );
  verify( "${STR2:M}", "" );
  verify( "${STR2:N}", "one two three four five" );

  // file/path finders
#ifdef UNIX
  verify( "${PATH:Fsh:T}", "sh" );
  verify( "${sh:P:Msh}", "" );
  verify( "${sh:L:p:Msh}", "" );
#else
  verify( "${PATH:Fcommand.com:T}", SmartCaseString( "command.com" ) );
  verify( "${command.com:P:Mcommand.com}", "" );
  verify( "${command.com:L:p:Mcommand.com}", "" );
#endif

  // substitution
  verify( "${STR2:S/f/X/}", "one two three Xour five" );
  verify( "${STR2:S/f/X/g}", "one two three Xour Xive" );
  verify( "${STR2:S/o/X/g}", "Xne twX three fXur five" );
  verify( "${STR2:S/^o/X/g}", "Xne two three four five" );
  verify( "${STR2:S/e$/X/g}", "onX two threX four fivX" );
  verify( "${STR2:S/e$/&s/g}", "ones two threes four fives" );
  verify( "${STR2:S/e$/\\&more/g}", "on&more two thre&more four fiv&more" );
  verify( "${STR2:S/ /X/g}", "one two three four five" );
  verify( "${STR2:s/ /_/g}", "one_two_three_four_five" );
  verify( "${STR2:@x@_$x_@}", "_one_ _two_ _three_ _four_ _five_" );
  verify( "${STR3:.c=.cpp}", "one.cpp two.h three.cpp four.o five.cpp" );

  // run command
  verify( "${dummy:!echo hello!:S/$/ world/}", "hello world" );

  // create path
  verify( "${PATH2:C-}", "" );
  if (!Path::exists( mkpath_test1 ))
  {
    ++num_fails;
    cerr << "ERROR: was unable to create " << mkpath_test1 << endl;
  }
  else
    Path::deletePath( mkpath_test1_head, true, true );
  if (!Path::exists( mkpath_test2 ))
  {
    ++num_fails;
    cerr << "ERROR: was unable to create " << mkpath_test2 << endl;
  }
  else
    Path::deletePath( mkpath_test2_head, true, true );

  cout << "Number of tests   : " << num_tests << endl;
  cout << "Number of failures: " << num_fails << endl;

  return 0;
}
