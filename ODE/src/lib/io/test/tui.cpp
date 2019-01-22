#include <iostream.h>
#include "lib/io/ui.hpp"
#include <base/binbase.hpp>

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  String resp;
  Interface::getResponse( resp, false );
  cout << resp << endl;
  return 0;
}
