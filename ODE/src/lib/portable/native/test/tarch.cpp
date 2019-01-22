#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <iostream.h>
#include <base/binbase.hpp>
#include "lib/portable/native/arch.h"


void bailOut( char *msg )
{
  cerr << msg << endl;
  exit( 0 );
}


int main( int argc, char **argv, const char **envp )
{
  Tool::init( envp );
  ODEArchInfo *ainfo;
  long offset = -1;

  if (argc < 2 || argc > 3)
    bailOut( "Usage: tarch <archive_library> [member_to_touch]" );
  if ((ainfo = ODEopenArch( argv[1] )) == 0)
    bailOut( "Couldn't open archive library (or it was corrupt)!" );
  while (ODEreadArchNext( ainfo ) == 0)
  {
    cout << argv[1] << "(" << ainfo->name << ") : " << ainfo->date << endl;
    if (argc > 2 && strcmp( ainfo->name, argv[2] ) == 0)
      offset = ainfo->current_member;
  }
  if (offset >= 0)
  {
    cerr << "updating member " << argv[2] << endl;
    ainfo->current_member = offset;
    ainfo->date = (long)time(0);
    cerr << "update return code = " << ODEsetMemberDate( ainfo ) <<endl;
  }
  ODEcloseArch( ainfo );
  cout << "done." << endl;
}
