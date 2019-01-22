/**
 * Usage: tcndeval [response_file]
 *
 * response_file should contain Make conditional statements,
 * with optional text between them (which will be echoed
 * for true blocks).  E.g., in the following, only the
 * message "TRUE" would be printed (assuming the environment
 * variable PATH is set):
 *
 * .ifdef PATH
 * TRUE
 * .else
 * FALSE
 * .endif
 *
 * If response_file is not given, lines are read from stdin
 * until EOF (Ctrl-Z on NT/OS2 and Ctrl-D on Unix) is read
 * (therefore yet another way to run this program is
 * "tcndeval <response_file").
 *
**/

#include <iostream.h>

#include <base/binbase.hpp>
#include "lib/util/condeval.hpp"
#include "lib/exceptn/parseexc.hpp"
#include "lib/io/path.hpp"
#include "lib/string/variable.hpp"

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );

  istream *stream = &cin;
  ifstream *filestrm = 0;
  String buf;
  Variable var;
  CondEvaluator eval( 0, 0, &var, Cond::DEFINED_FUNC | Cond::EMPTY_FUNC );
  int rc = 0;
  boolean showtext = true;

  if (argc > 1)
  {
    filestrm = Path::openFileReader( argv[1] );
    stream = filestrm;
  }
  else
    cout << "Enter conditional statements, EOF char (^D or ^Z) when finished."
        << endl;

  while (Path::readLine( *stream, &buf ))
  {
    try
    {
      if (buf.startsWith( ".ifdef" ))
        showtext = eval.parseIfdef( buf.substring( STRING_FIRST_INDEX + 6 ) );
      else if (buf.startsWith( ".ifndef" ))
        showtext = eval.parseIfndef( buf.substring( STRING_FIRST_INDEX + 7 ) );
      else if (buf.startsWith( ".if" ))
        showtext = eval.parseIf( buf.substring( STRING_FIRST_INDEX + 3 ) );
      else if (buf.startsWith( ".else" ))
        showtext = eval.parseElse();
      else if (buf.startsWith( ".elifdef" ))
        showtext = eval.parseElifdef( buf.substring( STRING_FIRST_INDEX + 8 ) );
      else if (buf.startsWith( ".elifndef" ))
        showtext = eval.parseElifndef( buf.substring(
            STRING_FIRST_INDEX + 9 ) );
      else if (buf.startsWith( ".elif" ))
        showtext = eval.parseElif( buf.substring( STRING_FIRST_INDEX + 5 ) );
      else if (buf.startsWith( ".endif" ))
        showtext = eval.parseEndif();
      else if (showtext)
        cout << buf << endl;
    }
    catch (ParseException &e)
    {
      cerr << "EXCEPTION: " << e.toString() << endl;
      rc = 1;
      break;
    }
  }

  if (!eval.allBlocksClosed())
    cerr << "ERROR: Missing one or more .endif statements!" << endl;

  if (filestrm != 0)
    Path::closeFileReader( filestrm );

  return rc;
}
