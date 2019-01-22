/* ************************************************
*
* Array<T> has more functionality than ODETDList<T>
*
* *************************************************
*/


#include <iostream.h>
#include <stdlib.h>
#include <stdio.h>


#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/portable/array.hpp"
#include "lib/portable/array.cxx"

typedef Array<int> MYARRAY;


/* ***************************************************************
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
char gBuffer[512];


/* **************************************************
*
* Help functions.
*
* Init, Assert and Debug functions.
*
* **************************************************
*/
void init(unsigned int val)
{

debugLevel=val; 
failed=0UL;
passed=0UL;
total=0UL;
}

void printLevel2(const char* buffer)
{   
  if(debugLevel==2 ) 
    cout << " " << buffer << endl;
}

void printLevel1(const char* buffer)
{  
   if(debugLevel==1 || debugLevel==2) 
     cout << gBuffer << "\t " << buffer << endl;
}


/* *********************************************************
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
* **********************************************************
*/
void ODELIBAssert(char expected, char actual, const char* what)
{
 printLevel2("Calling ODELIBAssert( with chars).");
 char buffer[255];

 total++;
 if(expected == actual ) {
   sprintf(buffer,"Testing method %s was OK.",what);
   printLevel2(buffer); 
   passed++; 
 }
 else {
   sprintf(buffer,"Testing method %s FAILED. \n I was expecting %c \t Actual value is %c",what, expected, actual);
   printLevel1(buffer); 
   failed++;
 }
}

void ODELIBAssert(int expected, int actual, const char* what)
{
 printLevel2("Calling ODELIBAssert( with ints).");
 char buffer[255];

 total++;
 if(expected == actual ) {
   sprintf(buffer,"Testing method %s was OK.",what);
   printLevel2(buffer); 
   passed++; 
 }
 else {
   sprintf(buffer,"Testing method %s FAILED. \n I was expecting %d \t Actual value is %d",what, expected, actual);
   printLevel1(buffer); 
   failed++;
 }
}


void ODELIBAssert(unsigned int expected, unsigned  int actual, 
                                 const char* what)
{
 printLevel2("Calling ODELIBAssert( with unsigned ints).");
 char buffer[255];

  total++;
  if(expected == actual ) {
   sprintf(buffer,"Testing method %s was OK.",what);
   printLevel2(buffer); 
   passed++; 
  }
  else {
   sprintf(buffer,"Testing method %s FAILED. \n I was expecting %d \t Actual value is %d",what, expected, actual);
   printLevel1(buffer); 
   failed++;
  }
}

void ODELIBAssert(long expected, long actual, const char* what)
{
 printLevel2("Calling ODELIBAssert( with long).");
 char buffer[255];
  
 total++;
 if(expected == actual ) {
   sprintf(buffer,"Testing method %s was OK.",what);
   printLevel1(buffer); 
   passed++; 
  }
  else {
   sprintf(buffer,"Testing method %s FAILED. \n I was expecting %ld \t Actual value is %ld",what, expected, actual);
   printLevel1(buffer); 
   failed++;
  }
}


void ODELIBAssert(unsigned long expected, unsigned long actual, const char* what)
{
 printLevel2("Calling ODELIBAssert( with unsigned long).");
 char buffer[255];

 total++;
 if(expected == actual ) {
   sprintf(buffer,"Testing method %s was OK.",what);
   printLevel2(buffer); 
   passed++; 
  }
  else {
   sprintf(buffer,"Testing method %s FAILED. \n I was expecting %lu \t Actual value is %lu",what, expected, actual);
   printLevel1(buffer); 
   failed++;
  }
}

/* *******************************************************************
*
* These are the tests.
*
*  They are all independent and can 
*  be run seperately.
*
* ********************************************************************
*/
void testConstructors()
{
  {  //Testing the different constructors for Lists:
     // Remember: Assert(expected, actual, "comment");
     // The default constructor creates a list that has nothing in it.
     sprintf(gBuffer,"testConstructors: testing Default Constructors");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     ODELIBAssert((boolean)1,(boolean)list.isEmpty(),"Array::isEmpty()"); 
  }

  {  //Testing the other constructors for Lists:
     // Remember: Assert(expected, actual, "comment");
     // Constructors with given size is similar to default
     // constructors. It is recommended to use the extendAs() method to 
     // increase the size of the List.
     sprintf(gBuffer,"testConstructors: testing Default Constructors with size");
     MYARRAY list(5);
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     ODELIBAssert((boolean)1,(boolean)list.isEmpty(),"Array::isEmpty()"); 
  }
  
}


void testInsertion()
{
  {  //Testing the Insertion of data into the list
     // using the add() method.
     // Remember: Assert(expected, actual, "comment");
     sprintf(gBuffer,"testInsertion: testing Insertion using ::add() ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 

     //Inserting 5 Elements into the List.
     list.add(1);
     list.add(2);
     list.add(3);
     list.add(4);
     list.add(5);
     ODELIBAssert(5UL,list.size(),"Array::size()"); 
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"Array::isEmpty()"); 
     ODELIBAssert(1UL,list.firstIndex(),"Array::firstIndex()"); 
     ODELIBAssert(5UL,list.lastIndex(),"Array::lastIndex()"); 
    
     //Assuming that Array<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual values.
     
     for(unsigned long i=list.firstIndex(); i<=list.lastIndex(); i++)
       ODELIBAssert(i,(unsigned long)list[i],"Array::add");
  }
  {  //Testing the Insertion of data into the list
     // using the append() method.
     // Remember: Assert(expected, actual, "comment");
     sprintf(gBuffer,"testInsertion: testing Insertion using ::append() ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 

     //Inserting 5 Elements into the List.
     list.append(1);
     list.append(2);
     list.append(3);
     list.append(4);
     list.append(5);
     ODELIBAssert(5UL,list.size(),"Array::size()"); 
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"Array::isEmpty()"); 
     ODELIBAssert(1UL,list.firstIndex(),"Array::firstIndex()"); 
     ODELIBAssert(5UL,list.lastIndex(),"Array::lastIndex()"); 
    
     //Assuming that Array<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual values.
     
     for(unsigned long i=list.firstIndex(); i<=list.lastIndex(); i++)
      ODELIBAssert(i,(unsigned long)list[i],"Array::insert");
  }

  {  //Testing the Insertion of data into the list using append().
     // append() behaves exactly as append().
     // Remember: Assert(expected, actual, "comment");
     sprintf(gBuffer,"testInsertion: testing Insertion using ::append() ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 

     //Inserting 5 Elements into the List.
     list.append(1);
     list.append(2);
     list.append(3);
     list.append(4);
     list.append(5);
     ODELIBAssert(5UL,list.size(),"Array::size()"); 
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"Array::isEmpty()"); 
     ODELIBAssert(1UL,list.firstIndex(),"Array::firstIndex()"); 
     ODELIBAssert(5UL,list.lastIndex(),"Array::lastIndex()"); 
     //Assuming that Array<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
     for(unsigned long i=list.firstIndex(); i<=list.lastIndex(); i++)
      ODELIBAssert(i,(unsigned long)list[i],"Array::append");
  }
  {  //Testing the Insertion of data into the list using prepend().
     // prepend() adds at the beginning of the List.
     // Remember: Assert(expected, actual, "comment");
     sprintf(gBuffer,"testInsertion: testing Insertion using ::prepend() ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 

     //Inserting 5 Elements into the List.
     list.append(1);
     list.prepend(2);
     list.prepend(3);
     list.prepend(4);
     list.prepend(5);
     ODELIBAssert(5UL,list.size(),"Array::size()"); 
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"Array::isEmpty()"); 
     ODELIBAssert(1UL,list.firstIndex(),"Array::firstIndex()"); 
     ODELIBAssert(5UL,list.lastIndex(),"Array::lastIndex()"); 
     //Assuming that Array<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
     for(unsigned long i=list.lastIndex(); i>=list.firstIndex(); i--)
       ODELIBAssert(list.lastIndex()-i+1UL,(unsigned long)list[i],
                    "Array::prepend");
  }

  {  //Testing the Insertion of data into the list using prepend()
     // AddAsFirst() behaves exactly as prepend().
     // Remember: Assert(expected, actual, "comment");
     sprintf(gBuffer,"testInsertion: testing Insertion using ::prepend() ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 

     //Inserting 5 Elements into the List.
     list.prepend(1);
     list.prepend(2);
     list.prepend(3);
     list.prepend(4);
     list.prepend(5);
     ODELIBAssert(5UL,list.size(),"Array::size()"); 
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"Array::isEmpty()"); 
     ODELIBAssert(1UL,list.firstIndex(),"Array::firstIndex()"); 
     ODELIBAssert(5UL,list.lastIndex(),"Array::lastIndex()"); 
     //Assuming that Array<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
     for(unsigned long i=list.lastIndex(); i>=list.firstIndex(); i--)
      ODELIBAssert(list.lastIndex()-i+1UL,(unsigned long)list[i],
         "Array::prepend");
  }
}

/* *********************************************************************
*
*  testSearching()
*
*  Test the searching of different elements in the List.
*
*
*
* **********************************************************************
*/
void testSearching()
{
  {  //Testing the Finding of data from the list
     //  In this test, we use elementAtPosition(), elementAtPosition().
     //  operator[]() will be tested later.
     //  In this test, all indexes are inbound.
     //  Behaviour for out-of bound indexes is undefined in this context.
     //  Using out-of-bound indexes in the responsability of the user.
     //  Remember: Assert(expected, actual, "comment");
     sprintf(gBuffer,"testSearching: testing Searching() ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1111);
     list.append(-22);
     list.append(3);
     list.append(444);
     list.append(500);
     int result1=list.elementAtPosition(3) ;     
     int result2=list.elementAtPosition(5) ;     
     int result3=list.elementAtPosition(4) ;     
     int result4=list.elementAtPosition(ARRAY_FIRST_INDEX) ;     
     int result5=list.elementAtPosition(list.lastIndex());
     ODELIBAssert(3,result1,"Array::findPosition()"); 
     ODELIBAssert(500,result2,"Array::elementAtPosition()"); 
     ODELIBAssert(444,result3,"Array::ElementAtPosition()"); 
     ODELIBAssert(1111,result4,"Array::elementAtPosition()"); 
     ODELIBAssert(500,result5,"Array::elementAtPosition()"); 
  }

 
#ifdef TEST_OUT_OF_BOUND_INDEXES 
//Do not test out-of-bound indexes.
  {  // Testing the Finding of data from the list using elementAtPosition()
     // Remember: Assert(expected, actual, "comment");
     // Testing the use of Out of bound indexes. 
     // In case of out of bound searches, the List will dump core.
     // By accessing the array, one should only go from firstIndex() to
     // lastIndex(). This is the only combination that garanty beeing
     // inbounds.
     sprintf(gBuffer,"testSearching: testing indexing out of bounds.");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1);
     list.append(2);
     list.append(3);
     int result1=list.elementAtPosition(5) ;     
     ODELIBAssert(0,result1,"Array::findPosition()"); 

  }
#endif
}


void testRemoval()
{
  {  //Testing the Removal of data from the list
     // Here we test the methods:
     // removeAtPosition():  removes the element at position firstIndex()
     // removeLast(): removes the element at position lastIndex()
     // removeAtPosition(i): removes the element at position (i) (1 based)
     // clear(): removes all the elements from the list.
     // Remember: Assert(expected, actual, "comment");
     sprintf(gBuffer,"testRemoval: testing removing vals from the list ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1111);
     list.append(-22);
     list.append(3);
     list.append(444);
     list.append(500);
     ODELIBAssert(5UL,list.size(),"Array::size()"); 
     list.removeAtPosition( ARRAY_FIRST_INDEX );
     ODELIBAssert(4UL,list.size(),"Array::size()"); 
     int result1=list[1];
     ODELIBAssert(-22,result1,"Array:removeAtPosition()"); 
     list.removeAtPosition(3);
     result1=list[3];
     ODELIBAssert(500,result1,"Array:removeAtPosition()"); 
     list.clear();
     ODELIBAssert(0UL,list.size(),"Array:clear()"); 
  }

  {  //Testing the Removal of data from the list using clear() 
     // clear(): removes all the elements from the list.
     // Remember: Assert(expected, actual, "comment");
     sprintf(gBuffer,"test clearing the list: using ::clear() ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1111);
     list.append(-22);
     list.append(3);

     ODELIBAssert(3UL,list.size(),"Array::size()"); 
     list.clear();
     ODELIBAssert(0UL,list.size(),"Array::size()"); 

  }
}


void testOperators()
{
printLevel2("testOperators():tests the different operators in the list.");
     //Testing the use of operators in conjunction with the list
     // Here also we have a specific out of bounds error test following.
     // We will test:
     // operator[]
     // operator+=(const MYARRAY&)
     // operator=
     // operator==
     // append(const MYARRAY&)
     // prepend(const MYARRAY&)
     // Remember: Assert(expected, actual, "comment");
  {
     // operator[]: All indexes are in-bound.
     sprintf(gBuffer,"test operator[](unsigned long): all indexes are inbounds");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1111);
     list.append(-22);
     list.append(3);
     list.append(444);
     list.append(500);
     int result1=list[3] ;     
     int result2=list[5] ;     
     int result3=list[1] ;     
     int result4=list[list.firstIndex()];
     int result5=list[list.lastIndex()];
     ODELIBAssert(3,result1,"Array::operator[]"); 
     ODELIBAssert(500,result2,"Array::operator[]"); 
     ODELIBAssert(1111,result3,"Array::operator[]"); 
     ODELIBAssert(1111,result4,"Array::operator[list.firstIndex()]"); 
     ODELIBAssert(500,result5,"Array::operator[list.lastIndex()]"); 
  }

  {
     // operator+=
  
    sprintf(gBuffer,"test operator+=(const MYARRAY&) ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1);
     list.append(2);
     list.append(3);
     list.append(4);
     list.append(5);

     MYARRAY list2;
     list.append(6);
     list.append(7);
     list.append(8);

     list+=list2;
     
     ODELIBAssert(8UL,list.size(),"Array::operator+="); 
     //Assuming that Array<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
     for(unsigned long i=list.firstIndex(); i<=list.lastIndex(); i++)
      ODELIBAssert(i,(unsigned long)list[i],"Array::operator+=");

  }

  {
     // operator=
    sprintf(gBuffer,"test operator=(const MYARRAY&) ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1);
     list.append(2);
     list.append(3);
     list.append(4);
     list.append(5);

     MYARRAY list2;
     list2.append(6);
     list2.append(7);
     list2.append(8);

     list2=list;
     
     ODELIBAssert(5UL,list2.size(),"Array::operator="); 

     for(unsigned long i=list2.firstIndex(); i<=list2.lastIndex(); i++)
      ODELIBAssert(i,(unsigned long)list2[i],"Array::operator=");

  }
  {
     // operator==
     // operator=
     sprintf(gBuffer,"test operator==(const MYARRAY&) ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1);

     MYARRAY list2;
     list2.append(6);
     list2.append(7);

     MYARRAY list3;
     list3.append(1);

     ODELIBAssert(true,list==list ,"Array::operator=="); 
     ODELIBAssert(true,list2==list2 ,"Array::operator=="); 
     ODELIBAssert(true,list3==list3 ,"Array::operator=="); 
     ODELIBAssert(true,list==list3 ,"Array::operator=="); 

     ODELIBAssert(false,list2==list ,"Array::operator=="); 
     ODELIBAssert(false,list==list2 ,"Array::operator=="); 
     ODELIBAssert(false,list2==list3 ,"Array::operator=="); 
  }
  {
     // append(const MYARRAY&)
     sprintf(gBuffer,"test append(const MYARRAY&) ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1);

     MYARRAY list2;
     list.append(2);
     list.append(3);

     MYARRAY list3 = list.append(list2);
     ODELIBAssert(3UL,list3.size(),"Array::append(const MYARRAY&)"); 
     //Assuming that Array<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
    for(unsigned long i=list3.firstIndex(); i<=list3.lastIndex(); i++)
    ODELIBAssert(i,(unsigned long)list3[i],"Array::append(const MYARRAY&)");
  }

  {
     // prepend(const MYARRAY&)
     sprintf(gBuffer,"test ::prepend(const MYARRAY&) ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(3);

     MYARRAY list2;
     list2.append(1);
     list2.append(2);

     MYARRAY list3=list.prepend(list2);
     ODELIBAssert(3UL,list3.size(),"Array::prepend(const MYARRAY&)"); 
     //Assuming that Array<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
    for(unsigned long i=list3.firstIndex(); i<=list3.lastIndex(); i++)
      ODELIBAssert(i,(unsigned long)list3[i],"Array::prepend(const MYARRAY&)");
  }

}

void testCopying()
{
  {
     // Remember: Assert(expected, actual, "comment");
     // Tests copying a List into another list using the copy constructor.
     // Currently, we only do unprotected deep copy. 
     sprintf(gBuffer,"test Copying one array into another.");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1111);
     list.append(-22);
     list.append(3);
     list.append(444);
     list.append(500);

     MYARRAY list2(list);
     ODELIBAssert(5UL,list.size(),"Array::size()"); 
     for(unsigned long i=list.firstIndex(); i<=list.lastIndex(); i++)
       ODELIBAssert(list[i],list2[i],"Array::Array(const Array<int>&"); 
  }

}

void testExtension()
{
  {
     // Remember: Assert(expected, actual, "comment");
     // Test the Extension of the List. 
     sprintf(gBuffer,"test ::extendAs(const MYARRAY&) ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1);
     list.append(2);
     list.append(3);
     list.append(4);
     list.append(5);

     list.extendTo( 20,12);
     for(unsigned long i=list.firstIndex(); i<=5; i++)
       ODELIBAssert(i,(unsigned long)list[i],"Array::extendTo()");
     for(i=6; i<=list.lastIndex(); i++)
       ODELIBAssert(12UL,(unsigned long)list[i],"Array::extendTo()");

   }

  {
     // Remember: Assert(expected, actual, "comment");
     // Test the Extension of the List as Pointer. 
     sprintf(gBuffer,"test ::extendAsPtr(const MYARRAY&) ");
     MYARRAY list;
     ODELIBAssert(0UL,list.size(),"Array::size()"); 
     list.append(1);
     list.append(2);
     list.append(3);
     list.append(4);
     list.append(5);

     MYARRAY* listPtr=list.extendToAsPtr( 20,12);
     for(unsigned long i=listPtr->firstIndex(); i<=5; i++)
       ODELIBAssert(i,(unsigned long)(*listPtr)[i],"Array::extendToAsPtr()");
     for(i=6; i<=list.lastIndex(); i++)
       ODELIBAssert(12UL,(unsigned long)(*listPtr)[i],"Array::extendToAsPtr()");

   }

}


void generateReport(const char* exe)
{
  cout << endl << " *********************************** " << endl;
  cout << "\t     " << total  << " tests executed       " << endl;
  cout << "\t     " << failed << " tests failed         " << endl;
  cout << "\t     " << passed << " tests passed         " << endl;
  cout << endl << " *********************************** " << endl;
  if(failed>0 && debugLevel==0)
    cout << endl << " You can see he failing tests by running '"<< exe <<" 1' " << endl;

}


/* ******************************
* main()
* Invoke the tests individually.
*
* *******************************
*/

int main(int argc, char** argv, const char **envp)
{
  Tool::init( envp );
  if(argc==1)
    init(0);
  else
    init(atoi(argv[1]));

//Run the tests.
  testConstructors();
  testInsertion();
  testSearching();
  testRemoval();
  testOperators();
  testCopying();
  testExtension();

//Print the report.
  generateReport(argv[0]);


return 0;
}

