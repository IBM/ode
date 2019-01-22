/**
 * Usage: tcond <if|ifdef|ifmake> <expression>
**/

#include <base/binbase.hpp>
#include "lib/util/cond.hpp"
#include "lib/exceptn/parseexc.hpp"
#include "lib/io/path.hpp"
#include "lib/string/variable.hpp"

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );

  Variable var;

  if (argc != 3)
  {
    cout << "Usage: tcond <if|ifdef|ifmake> <expression>" << endl;
    exit( -1 );
  }
  try
  {
    if (strcmp( argv[1], "if" ) == 0)
      cout << Cond::evalIfExpr( argv[2], 0, 0, &var ) << endl;
    else if (strcmp( argv[1], "ifdef" ) == 0)
      cout << Cond::evalIfDefExpr( argv[2], 0, 0, &var ) << endl;
    else if (strcmp( argv[1], "ifmake" ) == 0)
      cout << Cond::evalIfMakeExpr( argv[2], 0, 0, &var ) << endl;
    else
      cout << "Invalid conditional type" << endl;
  }
  catch (ParseException &e)
  {
    cout << "EXCEPTION: " << e.toString() << endl;
  }
}
