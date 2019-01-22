#include <iostream.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>


#include "lib/io/path.hpp"
#include "lib/io/file.hpp"
#include "lib/exceptn/ioexcept.hpp"
#include <base/binbase.hpp>

/* ****************************************************************
*
*  Global data members.
*
*  debugLevel: specifies the debugging level.
*              (0: default: only the final report will be printed)
*              (1: The failing tests will be printed)
*              (2: everything will be printed)
*
*  failed:    specifies the tests that failed.
*  passed:    specifies the tests that passed.
*  total :    specifies failed + passed.
* *****************************************************************
*/
unsigned int debugLevel;
unsigned long failed;
unsigned long passed;
unsigned long total;
char buffer[512];



/* ****************************************************************
*
* Help functions.
*
* init, assert and debug functions.
*
* *****************************************************************
*/
void init(unsigned int val)
{

debugLevel=val; 
failed=0UL;
passed=0UL;
total=0UL;
}

void printLevel2(const char* val)
{   
  if(debugLevel==2 ) 
    cout << val << endl;
}

void printLevel1(const char* val)
{  
   if(debugLevel==1 || debugLevel==2) 
      cout << buffer << "\t " << val << endl;
}


/* ****************************************************************
*
*  ODELIBAssert()
*
*  It compares the expected value to the actual value.
*
*  If they are different, it increments the failed counter.
*  If they are the same, it increments the passed counter.
*
*  There are different flavors of this ODELIBAssert:
*  ODELIBAssert( int, int, char*)
*  ODELIBAssert( unsigned int,  unsigned int, char*)
*  ODELIBAssert( unsigned long, unsigned  long, char*)
*  ODELIBAssert( long, long, char*)
*  ODELIBAssert( char, char, char*)
*  ODELIBAssertNeg( void*, void*, char*)
* ****************************************************************
*/
void ODELIBAssertNeg(void* notexpected, void* actual, const char* what)
{
 // This method makes sure an actual is different from non expected.
 // i.e: when allocating pointer, we want to make sure the allocation is NOT
 // NULL (0), however, there is no way to know which adress it will be 
 // allocated at. So we cannot really assert for something. The actual value
 // is unknown. However, we know what it should not be.
 printLevel2("Calling ODELIBAssertNeg( with void*).");
 char val[255];

 total++;
 if(notexpected == actual ) {
   sprintf(val,"Testing method %s FAILED. \n I was NOT expecting %ld \t Actual value is %ld",what, notexpected, actual);
   printLevel1(val); 
   failed++; 
 }
 else {
   sprintf(val,"Testing method %s OK.",what);
   printLevel2(val); 
   passed++;
 }
}
void ODELIBAssert(char expected, char actual, const char* what)
{
 printLevel2("Calling ODELIBAssert( with chars).");
 char val[255];

 total++;
 if(expected == actual ) {
   sprintf(val,"Testing method %s was OK.",what);
   printLevel2(val); 
   passed++; 
 }
 else {
   sprintf(val,"Testing method %s FAILED. \n I was expecting %c \t Actual value is %c",what, expected, actual);
   printLevel1(val); 
   failed++;
 }
}

void ODELIBAssert(int expected, int actual, const char* what)
{
 printLevel2("Calling ODELIBAssert( with ints).");
 char val[255];

 total++;
 if(expected == actual ) {
   sprintf(val,"Testing method %s was OK.",what);
   printLevel2(val); 
   passed++; 
 }
 else {
   sprintf(val,"Testing method %s FAILED. \n I was expecting %d \t Actual value is %d",what, expected, actual);
   printLevel1(val); 
   failed++;
 }
}


void ODELIBAssert(unsigned int expected, unsigned  int actual, 
                                 const char* what)
{
 printLevel2("Calling ODELIBAssert( with unsigned ints).");
 char val[255];

  total++;
  if(expected == actual ) {
   sprintf(val,"Testing method %s was OK.",what);
   printLevel2(val); 
   passed++; 
  }
  else {
   sprintf(val,"Testing method %s FAILED. \n I was expecting %d \t Actual value is %d",what, expected, actual);
   printLevel1(val); 
   failed++;
  }
}

void ODELIBAssert(long expected, long actual, const char* what)
{
 printLevel2("Calling ODELIBAssert( with long).");
 char val[255];
  
 total++;
 if(expected == actual ) {
   sprintf(val,"Testing method %s was OK.",what);
   printLevel2(val); 
   passed++; 
  }
  else {
   sprintf(val,"Testing method %s FAILED. \n I was expecting %ld \t Actual value is %ld",what, expected, actual);
   printLevel1(val); 
   failed++;
  }
}

void ODELIBAssertNeg(long expected, long actual, const char* what)
{
 printLevel2("Calling ODELIBAssert( with long).");
 char val[255];
  
 total++;
 if(expected != actual ) {
   sprintf(val,"Testing method %s was OK.",what);
   printLevel2(val); 
   passed++; 
  }
  else {
   sprintf(val,"Testing method %s FAILED. \n I was not expecting %ld \t Actual value is %ld",what, expected, actual);
   printLevel1(val); 
   failed++;
  }
}

void ODELIBAssert(unsigned long expected, unsigned long actual, const char* what)
{
 printLevel2("Calling ODELIBAssert( with unsigned long).");
 char val[255];

 total++;
 if(expected == actual ) {
   sprintf(val,"Testing method %s was OK.",what);
   printLevel2(val); 
   passed++; 
  }
  else {
   sprintf(val,"Testing method %s FAILED. \n I was expecting %lu \t Actual value is %lu",what, expected, actual);
   printLevel1(val); 
   failed++;
  }
}

void generateReport(const char* exe)
{
  cout << endl << " *********************************** " << endl;
  cout << "\t     " << total  << " tests executed      " << endl;
  cout << "\t     " << failed << " tests failed         " << endl;
  cout << "\t     " << passed << " tests passed         " << endl;
  cout << endl << " *********************************** " << endl;

  if((failed>0) && (debugLevel==0))
    cout << endl << " You can see the failing tests by running '"
         << exe <<" 1' " << endl;
}

/* *****************************************
*
* Path::isSamePath(const File&, const File&)
*
* *****************************************
*/
void testIsSamePath()
{
  { 
  //Get 4 File objects.
  File t1("hello1.txt",true);
  File t2("hello2.txt",true);
  File t3("hello1.txt",true);
  File t4("dir1/hello1.txt",true);

  //Make sure they are different, except t1 and t3.
  ODELIBAssert(false,Path::isSamePath(t1, t2),"Path::isSamePath()");
  ODELIBAssert(true ,Path::isSamePath(t1, t3),"Path::isSamePath()");
  ODELIBAssert(false,Path::isSamePath(t3, t2),"Path::isSamePath()");
  ODELIBAssert(false,Path::isSamePath(t4, t1),"Path::isSamePath()");
  }
}


/* *******************************************************
*
* Path::pathInList(const File&, const Array<File>&) method
*
* ********************************************************
*/
void testPathInList()
{
  { 
  //Create 4 File objects.
  
  File file1("hello1.txt",true);
  File file2("hello2.txt",true);
  File file3("hello3.txt",true);
  File file4("hello4.txt",true);

  //Insert 3 of them in the List.
  Array<File> list;
  list.append(file1);
  list.append(file2);
  list.append(file3);

  //Make sure file1, file2, file3 are in the List.
  //file4 is NOT in the list.
  ODELIBAssert((int)1,Path::pathInList(file1, list),"Path::pathInList()");
  ODELIBAssert((int)2,Path::pathInList(file2, list),"Path::pathInList()");
  ODELIBAssert((int)3,Path::pathInList(file3, list),"Path::pathInList()");

  ODELIBAssert((int)ELEMENT_NOTFOUND,Path::pathInList(file4, list),"Path::pathInList()");
  }
}

/* *************************************
*
* Path::testAbsolute(const File&) method
*
* **************************************
*/
void testAbsolute()
{
 { 
  // Have 2 File objects.
  File t1("",true); // Empty cannot be absolute
  File t2("hello2.txt",true);

  //t1 is absolute, t2 is not.
  //Maybe I need a better test than this one.
  ODELIBAssert(false ,Path::absolute(t1),"Path::absolute()");
  ODELIBAssert(true,Path::absolute(t2),"Path::absolute()");
  }
}

/* ********************************************
*
* Path::rename(const File&, const File&) method
*
* *********************************************
*/
void testRename()
{
  { 
  File file1("hello1.txt",true); // This file does not exist.
  File file2("hello3.txt",true); // This file does not exist

 
  //if hello3 already exists, delete it
  if(file2.doesExist())
      ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");

  //Recreate the file 'hello3.txt'
  {
   fstream* filep=Path::openFileWriter(file2);
   filep->put('a');
   if(filep)
     Path::closeFileWriter(filep);
  }
  File dir1("dir1",true); // This directory does not exist.
  File dir2("dir2",true); // This directory does exist, but is empty.
  File dir3("dir3",true); // This directory exists and has files in it.
  dir1.setDir();
  dir2.setDir();
  dir3.setDir();

  ODELIBAssert(false,Path::rename(file1, file2),"Path::rename()");
  ODELIBAssert(true,Path::rename(file2, file1),"Path::rename()");
  ODELIBAssert(false,Path::rename(file2, file1),"Path::rename()");
  //deleting hello1.txt after renaming
  ODELIBAssert(true,Path::deletePath(file1), " Path::deletePath()");
  }
}
 

/* ********************************************
*
* Path::touch(const File&, long time) method
*
* *********************************************
*/
void testTouch()
{
  {
    File file1("testfile.txt",true);
    File file2("file2.txt",true);
    if(file1.doesExist())
      ODELIBAssert(true,Path::deletePath(file1), " Path::deletePath()");
    if(file2.doesExist())
      ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");

    //Recreate the file 'testfile.txt'
    {
    fstream* filep=Path::openFileWriter(file1);
    filep->put('a');
    if(filep)
        Path::closeFileWriter(filep);
    }

    //Recreate the file 'file2.txt'
    {
    fstream* filep=Path::openFileWriter(file2);
    filep->put('a');
    if(filep)
        Path::closeFileWriter(filep);
    }
    //touching testfile.txt 
    Path::touch("testfile.txt");
    //updating file statistics
    file1.stat();
    file2.stat();
    //comparing times of testfile.txt and file2.txt
    ODELIBAssertNeg(0.0,(Path::timeCompare(file1, file2)),"Path::touch()");
    ODELIBAssert(true,Path::deletePath(file1), " Path::deletePath()");
    ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");

  }
}


/* **************************************************
*
* Path::timeCompare(const File&, const File& ) method
*
* ***************************************************
*/
void testTimeCompare()
{
   //Before running this test, create a file file1.txt.
  { //Compares the times between two files.
    File file1("file1.txt",true);
    File file2("file2.txt",true);
    File file3("file3.txt",true);
            

    if(file2.doesExist())
      ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");
    if(file3.doesExist())
      ODELIBAssert(true,Path::deletePath(file3), " Path::deletePath()");


    //Recreate the file 'file2.txt'
    {
    fstream* filep=Path::openFileWriter(file2);
    filep->put('a');
    if(filep)
        Path::closeFileWriter(filep);
    }

    //Recreate the file 'file3.txt'
    {
    fstream* filep=Path::openFileWriter(file3);
    filep->put('a');
    if(filep)
       Path::closeFileWriter(filep);
    }

    file2.stat();
    file3.stat();
    //Run Path::timeCompare() on the different files.
    //file1 already exists. Modifying times for file1, file2 differ
    ODELIBAssertNeg(0.0,(Path::timeCompare(file2, file1)),
                       "Path::timeCompare(file2, file1)");
    //file2, file3 are created at same time. 
    ODELIBAssert(0.0,(Path::timeCompare(file2, file3)),
                       "Path::timeCompare(file2, file3)");
    //file1 already exists. Modifying times for file1, file3 differ
    ODELIBAssertNeg(0.0,(Path::timeCompare(file3, file1)),
                       "Path::timeCompare(file3, file1)");
    ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");
    ODELIBAssert(true,Path::deletePath(file3), " Path::deletePath()");
 }
}

/* ************************************
*
* Path::deletePath(const File& ) method
*
* *************************************
*/
void testDeletePath()
{
  {
    //Create a File object.
    File t2("hello.txt",true);

    //If it exists, try to delete it.
    if(t2.doesExist())
      ODELIBAssert(true,Path::deletePath(t2), " Path::deletePath()");
    else { 
      //File object does not exist, create it.
      {
      fstream* filep=Path::openFileWriter(t2);
      filep->put('a');
      if(filep)
        Path::closeFileWriter(filep);
      }
      ODELIBAssert(true,Path::deletePath(t2), " Path::deletePath()");
    }
  }
}

/* ************************************
*
* Path::normalize(const String& ) method
*
* *************************************
*/

void testNormalize()
{
  String str1("\\dir1/dir2/dir3");
  String str2 = Path::DIR_SEPARATOR + "dir1" + Path::DIR_SEPARATOR + "dir2" +
                Path::DIR_SEPARATOR + "dir3"; 
  ODELIBAssert(0, strcmp( Path::normalize( str1 ), str2 ), "Path::normalize()"); 
  ODELIBAssertNeg(0, strcmp( str1, str2 ), "Path::normalize()"); 
  ODELIBAssert(0, strcmp( Path::normalizeThis( str1 ), str2 ), 
               "Path::normalizeThis()");
  ODELIBAssert(0, strcmp( str1, str2 ), "Path::normalizeThis()"); 
}


/* ************************************
*
* Path::unixize(const String& ) method
*
* *************************************
*/
void testUserize()
{
  String str1("\\dir1\\dir2/dir3");
  String str2 = Path::DIR_SEPARATOR + "dir1" + Path::DIR_SEPARATOR + "dir2"
                + Path::DIR_SEPARATOR + "dir3";
  ODELIBAssert(0, strcmp( Path::userize( str1 ), str2 ), "Path::userize()"); 
  ODELIBAssertNeg(0, strcmp( str1, str2 ), "Path::userize()"); 
  ODELIBAssert(0, strcmp( Path::userizeThis( str1 ), str2 ), 
               "Path::userizeThis()");
  ODELIBAssert(0, strcmp( str1, str2 ), "Path::userizeThis()"); 
}


void testUnixize()
{
  String str1("\\dir1\\dir2/dir3");
  String str2 = "/dir1/dir2/dir3"; 
  ODELIBAssert(0, strcmp( Path::unixize( str1 ), str2 ), "Path::unixize()"); 
  ODELIBAssertNeg(0, strcmp( str1, str2 ), "Path::unixize()"); 
  ODELIBAssert(0, strcmp( Path::unixizeThis( str1 ), str2 ), 
               "Path::unixizeThis()");
  ODELIBAssert(0, strcmp( str1, str2 ), "Path::unixizeThis()"); 
}

/* ***********************************************************************
*
* Path::canonicalize(const String&, boolean, boolean const String& ) method
*
* ************************************************************************
*/
void testCanonicalize()
{
#ifdef UNIX
  String drive_prepend = "";
#else
  String drive_prepend = Path::getcwd().substring( STRING_FIRST_INDEX,
      STRING_FIRST_INDEX + 2 ); // drive letter plus colon
#endif
  String str1("\\dir/dir1/../dir2/./dir3\\dir4/");
  String str2 = drive_prepend + Path::DIR_SEPARATOR + "dir" +
      Path::DIR_SEPARATOR + "dir2" + Path::DIR_SEPARATOR + "dir3" +
      Path::DIR_SEPARATOR + "dir4";
  String str3("./\\./");
  String str4("..\\/.\\");
  String str5("/");
  String str6("\\");
#ifndef UNIX
  String str7(drive_prepend);
  String str8(drive_prepend + str4);
#endif

  ODELIBAssert(0, strcmp( Path::canonicalize( str1, false ), str2 ),
      "Path::canonicalize()"); 
  //str1 should not have changed after previous assertion
  ODELIBAssertNeg(0, strcmp( str1, str2 ), "Path::canonicalize()"); 
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str1, false ), str2 ), 
      "Path::canonicalizeThis()"); 
  //str1 should have changed after previous assertion
  ODELIBAssert(0, strcmp( str1 , str2 ), "Path::canonicalizeThis()");

  ODELIBAssert( 0, strcmp( Path::canonicalizeThis( str4, false ), 
      Path::filePath( Path::getcwd() ) ), "Path::canonicalizeThis()" ); 
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str5, false ), 
      drive_prepend + Path::DIR_SEPARATOR ), "Path::canonicalizeThis()"); 
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str6, false ), 
      drive_prepend + Path::DIR_SEPARATOR ), "Path::canonicalizeThis()"); 
#ifndef UNIX
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str7, false ), 
      Path::getcwd() ), "Path::canonicalizeThis()");
  ODELIBAssert( 0, strcmp( Path::canonicalizeThis( str8, false ), 
      Path::filePath( Path::getcwd() ) ), "Path::canonicalizeThis()" ); 
#ifdef TPATH_TWODISK_TEST1
  // This section of code assumes that your current directory is
  // d:\test\path  and there exists a c:\junk\testpath directory
  // which would be your current directory if you did c: to change disks.
  // c:\junk\testpath\path2 does not exist.
  {
  String str9("c:");
  String str10("c:path2");
  String str11("c:");
  String str12("c:path2");
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str9, false ), 
      "C:\\junk\\testpath" ), "Path::canonicalizeThis() TWO DISK TEST 1"); 
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str10, false ), 
      "C:\\junk\\testpath\\path2" ), "Path::canonicalizeThis() TWO DISK TEST 1"); 
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str11, true ), 
      "C:\\junk\\testpath" ), "Path::canonicalizeThis() TWO DISK TEST 1"); 
  ODELIBAssertNeg(0, strcmp( Path::canonicalizeThis( str12, true ), 
      "C:\\junk\\testpath\\path2" ), "Path::canonicalizeThis() TWO DISK TEST 1"); 
  }
#endif
#ifdef TPATH_TWODISK_TEST2
  // This section of code assumes that your current directory is
  // d:\test\path  and you would be in c:\ if you did c: to change disks.
  // c:\junk\testpath exists but c:\junk\testpath\path2 does not exist.
  {
  String str9a("c:");
  String str10a("c:junk");
  String str11a("c:junk\\testpath\\path2");
  String str12a("c:junk\\testpath\\path2");
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str9a, false ), 
      "C:\\" ), "Path::canonicalizeThis() TWO DISK TEST 2"); 
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str10a, false ), 
      "C:\\junk" ), "Path::canonicalizeThis() TWO DISK TEST 2"); 
  ODELIBAssert(0, strcmp( Path::canonicalizeThis( str11a, false ), 
      "C:\\junk\\testpath\\path2" ), "Path::canonicalizeThis() TWO DISK TEST 2"); 
  ODELIBAssertNeg(0, strcmp( Path::canonicalizeThis( str12a, true ), 
      "C:\\junk\\testpath\\path2" ), "Path::canonicalizeThis() TWO DISK TEST 2"); 
  }
#endif
#endif // not UNIX
}

/* ***********************************************************
*
* Path::getDirContentAsFiles(const File&, const File& ) method
*
* ************************************************************
*/
void testGetDirContentAsFile()
{
  { // Create a directory and add two files in it.
    File dir1("hellodir",true);
    File file1("hellodir/file1.txt",true);
    File file2("hellodir/file2.exe",true);

    ODELIBAssert(true, Path::createPath(dir1.toString()),"Path::createPath()");

    // Create files file1.txt and file2.exe
    {
    fstream* filep=Path::openFileWriter(file1);
    filep->put('a');
    if(filep) 
        Path::closeFileWriter(filep);
    }

    {
    fstream* filep=Path::openFileWriter(file2);
    filep->put('b');
    if(filep)
       Path::closeFileWriter(filep);
    }

    {
     //First select all '*.exe' files from that directory.
     String pattern("*.exe");
     Array<File>* files=Path::getDirContentsAsFiles(dir1, Path::CONTENTS_FILES, 
                                                     0, 0, pattern);

      //Ensure, it does not return zero (0)
      ODELIBAssertNeg((void*)0,(void*)files,"Path::getDirContentAsFiles()");

      if(files!=0) {
        ODELIBAssert(1UL, files->size(),"Path::getDirContentAsFiles()");
        delete files;
      }
    }
    {
      //Select all files.
      String pattern("*");
      Array<File>* files=Path::getDirContentsAsFiles(dir1, Path::CONTENTS_FILES, 
                                              0, 0, pattern);
      //Ensure, it does not return zero (0)
      ODELIBAssertNeg((void*)0,(void*)files,"Path::getDirContentAsFiles()");

      //If it mistakenly returns 0, make sure it does not crash.
      if(files!=0) {
        ODELIBAssert(2UL, files->size(),"Path::getDirContentAsFiles()");
        delete files;
      }
    }

    {
     //Select files with extension that does not exist.
     String pattern("*.u");
     Array<File>* files=Path::getDirContentsAsFiles(dir1, Path::CONTENTS_FILES, 
                                              0, 0, pattern);

     //This should be zero (0).
     ODELIBAssert(0UL, (unsigned long)files,"Path::getDirContentAsFiles()");

     if(files!=0)
       ODELIBAssert(0UL, files->size(),"Path::getDirContentAsFiles()");
    }

     //deleting the created files and directories
     ODELIBAssert(true,Path::deletePath(file1), " Path::deletePath()");
     ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");
     ODELIBAssert(true,Path::deletePath(dir1), " Path::deletePath()");

  }
  
}

/* ***************************************
*
* Path::openFileReader(const File&) method
*
* ****************************************
*/
void testOpenFileReader()
{
  //Before running this test, do the following:
  //create a text file called 'openfile.txt', put some data in it.
  { //Create File object.
    File t2("hello2.txt",true);

    //
    if(!t2.doesExist()) {
      cout << " testOpenFileReader() did not run properly... " << endl;
      cout << " You need to create a file called 'openfile.txt' in current"
              " directory " << endl;

    } else {
      ifstream* filep=Path::openFileReader(t2);
      ODELIBAssertNeg((void*)0,(void*)filep,"Path::openFileReader()");
      if(filep) {
        char ch;
        filep->get(ch);
        ODELIBAssert(ch,'a',"Path::openFileReader()");
        Path::closeFileReader(filep);
    }
  }
 }
}


/* ****************************************
*
* Path::openFileWriter(const File& ) method
*
* *****************************************
*/
void testOpenFileWriter()
{
  { 
    File t2("hello2.txt",true);

    if(t2.doesExist())
      Path::deletePath(t2); 
    fstream* filep=Path::openFileWriter(t2);
    ODELIBAssertNeg((void*)0,(void*)filep,"Path::openFileWriter()");
    filep->put('a');
    if(filep) {
      Path::closeFileWriter(filep);
    }
  }
}

/* ********************************************
*
* Path::openFileReadWriter(const File& ) method
*
* *********************************************
*/
void testOpenFileReadWriter()
{
  {  
    File t2("hello2.txt",true);

    fstream* filep=Path::openFileReadWriter(t2);
    ODELIBAssertNeg((void*)0,(void*)filep,"Path::openFileReadWriter()");
    if(filep) {
      Path::closeFileReadWriter(filep);
    }
    if(t2.doesExist())
      ODELIBAssert(true,Path::deletePath(t2), " Path::deletePath()");
   
  }
}

/* ***********************************************************************
*
* Calls testOpenFileWriter(), testOpenFileReader(), testOpenFileReadWriter() in that order to properly test the manipulaton of "hello2.txt"
*
* ************************************************************************
*/
void testFileManip()
{
  testOpenFileWriter();
  testOpenFileReader();
  testOpenFileReadWriter();
}

/* **************************************************
*
* Path:copyfile(const File&, const File& ) method
*
* ***************************************************
*/
void testcopyFile()
{
 {
  //test the Path::copyfile() method
  File file1("copyfile1.txt",true);
  File file2("copyfile2.txt",true);
  File file3("copyfile3.txt",true);
  File dir1("copyfiledir",true);

  ODELIBAssert(true, Path::createPath(dir1.toString()),"Path::createPath()");
  dir1.setDir();
  if(file1.doesExist())
      ODELIBAssert(true,Path::deletePath(file1), " Path::deletePath()");
  if(file2.doesExist())
      ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");
  if(file3.doesExist())
      ODELIBAssert(true,Path::deletePath(file3), " Path::deletePath()");

  {
    fstream* filep=Path::openFileWriter(file1);
    filep->put('a');                          //creating file1
    if(filep) 
        Path::closeFileWriter(filep);
  }
  {
    fstream* filep=Path::openFileWriter(file2);
    filep->put('b');                          //creating file2
    if(filep) 
        Path::closeFileWriter(filep);
  }
  {
    fstream* filep=Path::openFileWriter(file3);
    filep->put('c');                          //creating file3
    if(filep) 
        Path::closeFileWriter(filep);
  }

  //Update filestat
  file1.stat();
  file2.stat();
  file3.stat();                  

  //Ensure if file2 is copied into file1 when overwrite is true
  ODELIBAssert(true, Path::copyFile(file2, file1, 
                     true, 0),"Path::copyFile():");
  //Check if file2 is copied into file3 when overwrite is false
  ODELIBAssert(true, Path::copyFile(file2, file3, 
                     false, 0),"Path::copyFile():");
  //Ensure if file2 is copied to dir1
  ODELIBAssert(true, Path::copyFile(file2, dir1, 
                     true, 0),"Path::copyFile()");
  File file4("copyfiledir/copyfile2.txt",true);

  //Asserting if files exist after copying
  ODELIBAssert(true, file1.doesExist(),"FILE EXISTS");  
  ODELIBAssert(true, file3.doesExist(),"FILE EXISTS");
  ODELIBAssert(true, file4.doesExist(),"FILE EXISTS");

  ODELIBAssert(true,Path::deletePath(file1), " Path::deletePath()");
  ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");
  ODELIBAssert(true,Path::deletePath(file3), " Path::deletePath()");
  ODELIBAssert(true,Path::deletePath(file4), " Path::deletePath()");
  ODELIBAssert(true,Path::deletePath(dir1), " Path::deletePath()");
 } 
  
}

/* **************************************************
*
* Path:copy(const File&, const File& ) method
*
* ***************************************************
*/
void testcopy()
{
  File file4("test*.txt",true);
  File file1("test1.txt",true);
  File file2("test2.txt",true);
  File dir1("testdir1",true);
  File dir2("testdir2",true);
  File file3("testdir2/test3.txt",true);
  ODELIBAssert(true, Path::createPath(dir1.toString()),"Path::createPath()");
  dir1.setDir();
  ODELIBAssert(true, Path::createPath(dir2.toString()),"Path::createPath()");
  dir2.setDir();
  if(file1.doesExist())
      ODELIBAssert(true,Path::deletePath(file1), " Path::deletePath()");
  if(file2.doesExist())
      ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");
  if(file3.doesExist())
      ODELIBAssert(true,Path::deletePath(file3), " Path::deletePath()");

  {
    fstream* filep=Path::openFileWriter(file1);
    filep->put('1');                          //creating file1
    if(filep) 
        Path::closeFileWriter(filep);
  }
  {
    fstream* filep=Path::openFileWriter(file2);
    filep->put('2');                          //creating file2
    if(filep) 
        Path::closeFileWriter(filep);
  }
  {
    fstream* filep=Path::openFileWriter(file3);
    filep->put('3');                          //creating file3
    if(filep) 
        Path::closeFileWriter(filep);
  }
  //Update filestat
  file1.stat();
  file3.stat();
  //Ensure if file2 is copied into file1
  ODELIBAssert(true, Path::copy(file2, file1, 
                     true),"Path::copy():");
  //Ensure if file2 is copied to dir1
  ODELIBAssert(true, Path::copy(file2, dir1, 
                     true),"Path::copy()");
  //Ensure if file3 from directory dir2 is copied into dir1 
  ODELIBAssert(true, Path::copy(file3, dir1, 
                     true),"Path::copy()");
  //Ensure if a pattern of files is copied into dir1
  ODELIBAssert(true, Path::copy(file4, dir1, 
                     false),"Path::copy()");
  File file5("testdir1/test1.txt",true);
  File file6("testdir1/test2.txt",true);
  File file7("testdir1/test3.txt",true);
  //Asserting if files exist after copying
  ODELIBAssert(true, file5.doesExist(),"FILE EXISTS");  
  ODELIBAssert(true, file6.doesExist(),"FILE EXISTS");
  ODELIBAssert(true, file7.doesExist(),"FILE EXISTS");

  if(file1.doesExist())
     ODELIBAssert(true,Path::deletePath(file1), " Path::deletePath()");
  if(file2.doesExist())
     ODELIBAssert(true,Path::deletePath(file2), " Path::deletePath()");
  if(file3.doesExist())
     ODELIBAssert(true,Path::deletePath(file3), " Path::deletePath()");
  if(file4.doesExist())
     ODELIBAssert(true,Path::deletePath(file4), " Path::deletePath()");
  if(file5.doesExist())
     ODELIBAssert(true,Path::deletePath(file5), " Path::deletePath()");
  if(file6.doesExist())
     ODELIBAssert(true,Path::deletePath(file6), " Path::deletePath()");
  if(file7.doesExist())
     ODELIBAssert(true,Path::deletePath(file7), " Path::deletePath()");
  if(dir1.doesExist())
     ODELIBAssert(true,Path::deletePath(dir1), " Path::deletePath()");
  dir2.stat();
  if(dir2.doesExist())
     ODELIBAssert(true,Path::deletePath(dir2), " Path::deletePath()");
}

/* ***********************************
*
* Path::createPath(const File&) method
*
* ************************************
*/
void testCreatePath()
{
  { // test the Path::createPath() method.
    File t2("createhellodir",true);

    ODELIBAssert(true, Path::createPath(t2.toString()),"Path::createPath()");
    ODELIBAssert(true,Path::deletePath(t2), " Path::deletePath()");
  }
  
}


/* **************************************************
*
 
* 
*
* ***************************************************
*/
void testBasic()
{ 
  {
    File file("hello2.txt",true);
    File dir("hellodir",true);
    dir.setDir();

    if(file.doesExist())
      Path::deletePath(file); 
    fstream* filep=Path::openFileWriter(file);
    ODELIBAssertNeg((void*)0,(void*)filep,"Path::openFileWriter()");
    filep->put('a');
    if(filep) 
      Path::closeFileWriter(filep);

    Path::createPath(dir.toString());



    File file1("hello2.txt",true);
    File dir1("hellodir",true);

    ODELIBAssert(true,Path::isFile(file1.toString()),"Path::isFile()");
    ODELIBAssert(false,Path::isLink(file1.toString()),"Path::isLink()");
    ODELIBAssert(false, Path::isDirectory(file1.toString()) ,"Path::isDirectory()");
    ODELIBAssert(1UL,Path::size(file1.toString()),"Path::size()");


    ODELIBAssert(false,Path::isFile(dir1.toString()),"Path::isFile()");
    ODELIBAssert(false,Path::isLink(dir1.toString()),"Path::isLink()");
    ODELIBAssert(true, Path::isDirectory(dir1.toString()) ,"Path::isDirectory()");
//    ODELIBAssert(320UL,Path::size(dir1.toString()),"Path::size()");

  if(file.doesExist())
     ODELIBAssert(true,Path::deletePath(file), " Path::deletePath()");
  if(file1.doesExist())
     ODELIBAssert(true,Path::deletePath(file1), " Path::deletePath()");
  if(dir.doesExist())
     ODELIBAssert(true,Path::deletePath(dir), " Path::deletePath()");
  if(dir1.doesExist())
     ODELIBAssert(true,Path::deletePath(dir1), " Path::deletePath()");
    }

}


/* **************************************************
*
* Path::setcwd(const File&) method
*
* ***************************************************
*/
void testSetCwd()
{ 
  {
    //Create a Directory.
    File dir("hellodir1",true);
    dir.setDir();
    Path::createPath(dir.toString());
    //getting the path of current working directory
    String temp = Path::getcwd();
    //saving current working directory to dir2
    File dir2(temp,true);
    dir2.setDir();

    //Create a directory object, that does not exist.
    File dir1("doesnotexistdir",true);
    dir1.setDir();
    
    //Make sure i can set it to an existing directory.
    ODELIBAssert(true,Path::setcwd(dir),"Path::setcwd()");
    ODELIBAssert(false,Path::setcwd(dir1),"Path::setcwd()");

    //Changing current working directory to actual path (dir2)
    ODELIBAssert(true,Path::setcwd(dir2),"Path::setcwd()");
    dir.stat();
    if(dir.doesExist())
       ODELIBAssert(true,Path::deletePath(dir), " Path::deletePath()");
    if(dir1.doesExist())
       ODELIBAssert(true,Path::deletePath(dir1), " Path::deletePath()");
  }
}

/* ****************************
*
*  Main()
*
*
* *****************************
*/
int main( int argc, const char **argv, const char **envp )
{
  Tool::init( envp );
  try
  {
  if(argc==1)
    init(0);
  else
    init(atoi(argv[1]));

//Run the tests.
  testIsSamePath();
  testPathInList();
  testAbsolute();
  testCreatePath();
  testDeletePath();
  testNormalize();
  testUnixize();
  testUserize();
  testCanonicalize();
  testRename();
  testGetDirContentAsFile();
  testTimeCompare();
  testTouch();
  testFileManip();
  testBasic();
  testSetCwd();
  testcopyFile();
  testcopy();
  }
  catch(IOException &ex)
  {
    cout << ex.getMessage() <<endl;
  }
  catch(FileNotFoundException &mesg)
  { 
    cout << mesg.getMessage() <<endl; 
  }
  catch(...)
  {
   cout<<"caught unknown error"<<endl;
  }

//Generate Report.
  generateReport(argv[0]);
  return 0;
}

