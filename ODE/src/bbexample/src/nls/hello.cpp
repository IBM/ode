#include <stdio.h>
#include <locale.h>
#include <nl_types.h>
#include <iostream.h>
// Messages from mkcatdefs and helloinc.msg
#ifdef NO_MSGHDR
  // Since we don't have mkcatdefs to convert symbols to numeric
  // identifiers, we'll hardcode it here.
  #define MF_HELLOINC "hello.cat"
  #define HELLOMSG 1
#else
  #ifdef MSG_HDRS_TAIL
    #include <helloinc_msg.h>
  #else
    #include <helloinc.h>
  #endif
#endif

#define MSG_SET_ID 1
#define MSG_ID 1

int main( int argc, char* argv[] )
{
  nl_catd descCat; /* message catalog descriptor */

  (void)setlocale( LC_ALL, "" ); /* set to active locale */

  // Need to set NLSPATH to <directory to catalog file>/%L/<subdirs>/%N
  // Where %L is replaced with the value of LANG
  // and %N is replace with the name of the catalog
  descCat = catopen( MF_HELLOINC, NL_CAT_LOCALE );
  cout << catgets( descCat, MSG_SET_ID, HELLOMSG, "Hello world!(default)" );
  cout << endl;
  (void)catclose( descCat );

  return (0);
}
