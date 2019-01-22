#include <iostream.h>
#include "lib/io/file.hpp"
#include <base/binbase.hpp>

int main(int argc, char *argv[], const char **envp)
{
  Tool::init( envp );
  File *tf;
  for (int i=0; i<argc; i++)
  {
    cout << "Evaluating file: " << argv[i] << endl;
    tf = new File( argv[i], true );
    cout << " conanical file: " << tf->toString() << endl;
    cout << " file size     : " << tf->getSize() << endl;
    cout << " mod  time      : " << tf->getModTime() << endl;
    cout << " chan time      : " << tf->getChangeTime() << endl;
    cout << " acce time      : " << tf->getAccessTime() << endl;
    cout << " lmod time      : " << tf->getLinkModTime() << endl;

    if (tf->doesExist())
      cout << " file exists" << endl;
    else
      cout << " file does NOT exist" << endl;

    if (tf->isDir())
      cout << " file is directory" << endl;
    else
      cout << " file is NOT directory" << endl;

    if (tf->isFile())
      cout << " file is file" << endl;
    else
      cout << " file is NOT file" << endl;

    if (tf->isLink())
      cout << " file is link" << endl;
    else
      cout << " file is NOT link" << endl;

    delete tf;
  }
  return ( 0 );
}
