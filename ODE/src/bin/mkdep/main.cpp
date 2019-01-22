#include "lib/intcmds/mkdep.hpp"
#include "lib/exceptn/mkdepexc.hpp"

#ifdef ODE_USE_GLOBAL_ENVPTR
int main( int argc, char **argv_orig )
{
  const char **argv = (const char **)argv_orig, **envp = 0;
#else
int main( int argc, const char **argv, const char **envp )
{
#endif
  // MkDep object must be created here in the executable
  // so that platforms which use environ (rather than envp)
  // use the right environment table.  Otherwise the library
  // will use *its* version of the environment (which might
  // be empty, as it is on MVS).
  MkDep md( argv, envp );

  try
  {
    return (md.run());
  }
  catch (Exception &se)
  {
    Interface::printError( String( "mkdep: " ) + se.getMessage() );
    Interface::quit( 1 );
  }
  return (0);
}
