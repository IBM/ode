using namespace std;
/**
 * Main Entry for make
**/
using namespace std;
#include <base/binbase.hpp>
#include "bin/make/makec.hpp"

#ifdef ODE_USE_GLOBAL_ENVPTR
int main( int argc, char **argv_orig )
{
  const char **argv = (const char **)argv_orig, **envp = 0;
#else
int main( int argc, const char **argv, const char **envp )
{
#endif
  return (Make::classMain( argv, envp ));
}

