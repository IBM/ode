#include <base/binbase.hpp>
#include "lib/util/arch.hpp"
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"

int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  StringArray paths, parsed;
  Archive *arc;
  CachedArchMember *mem;

  if (argc < 3)
	 cerr << "Usage: tarch <archive> <member> [path:path...] [cwd]" << endl;
  else
  {
	 if (argc > 3)
	 {
		String pathlist = String( argv[3] );
		pathlist.split( ":", 0, &paths );
	 }
	 if (argc > 4)
      arc = ArchiveCache::get( argv[1], argv[4], paths, true );
    else
      arc = ArchiveCache::get( argv[1], "", paths, true );
    if (arc == 0)
		cerr << "ArchiveCache.get returned null" << endl;
    else if ((mem = arc->getMemb( argv[2] )) == 0)
		cerr << "Archive.getMemb returned null" << endl;
    else
		cerr << *mem << "'s modtime: " << mem->getModTime() << endl;
  }
  return 0;
}
