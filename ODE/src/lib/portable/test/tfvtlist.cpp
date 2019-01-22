#include <iostream.h>
#include <stdlib.h>
#include <stdio.h>


#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/portable/nilist.hpp"
#include "lib/portable/nilist.cxx"

typedef ODETDList<int>    MYINTLIST;

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
     cout << buffer<< "\t " << val << endl;
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
* ****************************************************************
*/
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
   printLevel1(val); 
   passed++; 
  }
  else {
   sprintf(val,"Testing method %s FAILED. \n I was expecting %ld \t Actual value is %ld",what, expected, actual);
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

/* *********************************
*
* These are the tests.
*
*  They are all independent and can 
*  be run seperately.
*
* *********************************
*/


void testConstructors()
{
  {  //Testing the different constructors for Lists:
     // Remember: Assert(expected, actual, "comment");
     // The default constructor creates a list that has nothing in it.
     sprintf(buffer,"testConstructors: testing Default Constructors");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 
     ODELIBAssert((boolean)1,(boolean)list.isEmpty(),"ODETDList::isEmpty()"); 
  }

  {  //Testing the other constructors for Lists:
     // Remember: Assert(expected, actual, "comment");
     // Constructors with given size is similar to default
     // constructors. It is recommended to use the extendAs() method to 
     // increase the size of the List.
     sprintf(buffer,"testConstructors: testing Default Constructors with size");
     MYINTLIST list(5);
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 
     ODELIBAssert((boolean)1,(boolean)list.isEmpty(),"ODETDList::isEmpty()"); 
  }
}


void testInsertion()
{
  {  //Testing the Insertion of data into the list
     // using the addAsLast() method.
     // Remember: Assert(expected, actual, "comment");
     sprintf(buffer,"testInsertion: testing Insertion using ::addAsLast() ");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 

     //Inserting 5 Elements into the List.
     list.addAsLast(1);
     list.addAsLast(2);
     list.addAsLast(3);
     list.addAsLast(4);
     list.addAsLast(5);
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"ODETDList::isEmpty()"); 
     ODELIBAssert(1UL,(unsigned long)ARRAY_FIRST_INDEX,"ARRAY_FIRST_INDEX"); 
     ODELIBAssert(5UL,list.size(),"ODETDList::size()"); 
    
     //Assuming that ODETDList<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual values.
     
     for(unsigned long i=ARRAY_FIRST_INDEX; i<=list.size(); i++)
      ODELIBAssert(i,(unsigned long)list[i],"ODETDList::addAsLast");
  }

  {  //Testing the Insertion of data into the list using addAsLast().
     // addAsLast() behaves exactly as addAsLast().
     // Remember: Assert(expected, actual, "comment");
     sprintf(buffer,"testInsertion: testing Insertion using ::addAsLast() ");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 

     //Inserting 5 Elements into the List.
     list.addAsLast(1);
     list.addAsLast(2);
     list.addAsLast(3);
     list.addAsLast(4);
     list.addAsLast(5);
     ODELIBAssert(5UL,list.size(),"ODETDList::size()"); 
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"ODETDList::isEmpty()"); 
     ODELIBAssert(5UL,list.size(),"ODETDList::size()"); 
     //Assuming that ODETDList<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
     for(unsigned long i=ARRAY_FIRST_INDEX; i<=list.size(); i++)
      ODELIBAssert(i,(unsigned long)list[i],"ODETDList::addAsLast");
  }
  {  //Testing the Insertion of data into the list using addAsFirst()
     // addAsFirst() adds at the beginning of the List.
     // Remember: Assert(expected, actual, "comment");
     sprintf(buffer,"testInsertion: testing Insertion using ::addAsFirst() ");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 

     //Inserting 5 Elements into the List.
     list.addAsFirst(1);
     list.addAsFirst(2);
     list.addAsFirst(3);
     list.addAsFirst(4);
     list.addAsFirst(5);
     ODELIBAssert(5UL,list.size(),"ODETDList::size()"); 
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"ODETDList::isEmpty()"); 
     //Assuming that ODETDList<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
     for(unsigned long i=list.size(); i>=ARRAY_FIRST_INDEX; i--)
       ODELIBAssert(list.size()-i+1UL,(unsigned long)list[i],
         "ODETDList::addAsFirst");
  }

  {  //Testing the Insertion of data into the list using 
     // addAtPosition(unsigned long position).
     // addAtPosition() inserts an element into the list at a given position.
     // If there was an element at that position, that element will 
     // be shifted right.
     // If position is equal (size() +1) , addAtPosition() behaves as addAsLast()
     // Remember: Assert(expected, actual, "comment");
     sprintf(buffer,"testInsertion: testing Insertion using ::addAtPosition() ");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 

     //Inserting 5 Elements into the List.
     list.addAsLast(1);
     list.addAsLast(5);
     list.addAtPosition(2,2);
     list.addAtPosition(3,3);
     list.addAtPosition(4,4);
     ODELIBAssert(5UL,list.size(),"ODETDList::size()"); 
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"ODETDList::isEmpty()"); 
     //Assuming that ODETDList<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
     for(unsigned long i=ARRAY_FIRST_INDEX; i<=list.size(); i++)
       ODELIBAssert(i,(unsigned long)list[i],"ODETDList::addAtPosition");
  }

  {  //Testing the Insertion of data into the list using addAtPosition()
     // addAtPosition() behaves exactly as addAtPosition().
     // Remember: Assert(expected, actual, "comment");
     sprintf(buffer,"testInsertion: testing Insertion using ::addAtPosition()");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 

     //Inserting 5 Elements into the List.
     list.addAsLast(1);
     list.addAsLast(5);
     list.addAtPosition(2,2);
     list.addAtPosition(3,3);
     list.addAtPosition(4,4);
     ODELIBAssert(5UL,list.size(),"ODETDList::size()"); 
     ODELIBAssert((boolean)0,(boolean)list.isEmpty(),"ODETDList::isEmpty()"); 
     //Assuming that ODETDList<T>::elementAt() works, 
     //I'll retrieve the data and assert expected vs actual.
     
     for(unsigned long i=ARRAY_FIRST_INDEX; i<=list.size(); i++)
       ODELIBAssert(i,(unsigned long)list[i],"ODETDList::addAtPosition");
  }
}

void testSearching()
{
  {  //Testing the Finding of data from the list
     //  In this test, we use findPositionAt(), elementAtPosition(), 
     //  firstElement(), lastElement().
     //  operator[]() will be tested later.
     //  In this test, all indexes are inbound.
     //  Behaviour for out-of bound indexes is undefined in this context.
     //  Using out-of-bound indexes in the responsability of the user.
     //  Remember: Assert(expected, actual, "comment");
     sprintf(buffer,"testSearching:testing Searching.");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 
     list.addAsLast(1111);
     list.addAsLast(-22);
     list.addAsLast(3);
     list.addAsLast(444);
     list.addAsLast(500);
     int result1=list.elementAtPosition(3) ;     
     int result2=list.elementAtPosition(5) ;     
     int result3=list.elementAtPosition(4) ;     
     int result4=list.firstElement() ;     
     int result5=list.lastElement() ;     
     ODELIBAssert(3,result1,"ODETDList::findPositionAt()"); 
     ODELIBAssert(500,result2,"ODETDList::findPositionAt()"); 
     ODELIBAssert(444,result3,"ODETDList::ElementAtPosition()"); 
     ODELIBAssert(1111,result4,"ODETDList::firstElement()"); 
     ODELIBAssert(500,result5,"ODETDList::lastElement()"); 
  }
}


void testRemoval()
{
  {  //Testing the Removal of data from the list
     // Here we test the methods:
     // removeFirst():  removes the element at position ARRAY_FIRST_INDEX
     // removeLast(): removes the element at position size()
     // removeAtPosition(i): removes the element at position (i) (1 based)
     // removeAll(): removes all the elements from the list.
     // Remember: Assert(expected, actual, "comment");
     sprintf(buffer,"testRemoval(): testing RemoveFirst(), ....");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 
     list.addAsLast(1111);
     list.addAsLast(-22);
     list.addAsLast(3);
     list.addAsLast(444);
     list.addAsLast(500);
     ODELIBAssert(5UL,list.size(),"ODETDList::size()"); 
     list.removeAtPosition( ARRAY_FIRST_INDEX );
     ODELIBAssert(4UL,list.size(),"ODETDList::size()"); 
     int result1=list[1];
     ODELIBAssert(-22,result1,"ODETDList:removeFirst()"); 
     list.removeAtPosition(3);
     result1=list[3];
     ODELIBAssert(500,result1,"ODETDList:removeAtPosition()"); 
     list.removeAll();
     ODELIBAssert(0UL,list.size(),"ODETDList:removeAll()"); 
  }

  {  //Testing the Removal of data from the list using removeAll() 
     // clear(): removes all the elements from the list.
     // Remember: Assert(expected, actual, "comment");
     sprintf(buffer,"testRemoval(): testing ClearAndDestroy().");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 
     list.addAsLast(1111);
     list.addAsLast(-22);
     list.addAsLast(3);

     ODELIBAssert(3UL,list.size(),"ODETDList::size()"); 
     list.removeAll();
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 

  }
}


void testOperators()
{
     //Testing the use of operators in conjunction with the list
     // Here also we have a specific out of bounds error test following.
     // We will test:
     // operator[]
     // operator+=(const MYINTLIST&)
     // operator=
     // operator==
     // appendElement(const MYINTLIST&)
     // prependElement(const MYINTLIST&)
     // Remember: Assert(expected, actual, "comment");
  {
     // operator[]: All indexes are in-bound.
     sprintf(buffer,"testOperators: testing [](unsigned long) operator.");
     MYINTLIST list;
     ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 
     list.addAsLast(1111);
     list.addAsLast(-22);
     list.addAsLast(3);
     list.addAsLast(444);
     list.addAsLast(500);
     int result1=list[3] ;     
     int result2=list[5] ;     
     int result3=list[1] ;     
     int result4=list[ARRAY_FIRST_INDEX];
     int result5=list[list.lastIndex()];
     ODELIBAssert(3,result1,"ODETDList::operator[]"); 
     ODELIBAssert(500,result2,"ODETDList::operator[]"); 
     ODELIBAssert(1111,result3,"ODETDList::operator[]"); 
     ODELIBAssert(1111,result4,"ODETDList::operator[ARRAY_FIRST_INDEX]"); 
     ODELIBAssert(500,result5,"ODETDList::operator[lastIndex()]"); 
  }
}

void testCopying()
{
  {
   // Remember: Assert(expected, actual, "comment");
   // Tests copying a List into another list using the copy constructor.
   // Currently, we only do unprotected deep copy. 
   sprintf(buffer,"testCopying: testing copying one list into another.");
   MYINTLIST list;
   ODELIBAssert(0UL,list.size(),"ODETDList::size()"); 
   list.addAsLast(1111);
   list.addAsLast(-22);
   list.addAsLast(3);
   list.addAsLast(444);
   list.addAsLast(500);

   MYINTLIST list2(list);
   ODELIBAssert(5UL,list.size(),"ODETDList::size()"); 
   for(unsigned long i=ARRAY_FIRST_INDEX; i<=list.size(); i++)
    ODELIBAssert(list[i],list2[i],"ODETDList::ODETDList(const ODETDList<int>&"); 
  }

}

void testCursor()
{
  {
    // Remember: Assert(expected, actual, "comment");
    // Test the List Enumeration.
    sprintf(buffer,"testCursor: testing Cursor facility of the list.");
    unsigned int i=0;
    MYINTLIST list;
    for(i=1; i<=10; i++)
      list.addAsLast(i);

    i=1;
    ListEnumeration< int > enumer( &list );
    while(enumer.hasMoreElements())
    {
     ODELIBAssert(i++,(unsigned int&)*enumer.nextElement(),"ListEnumeration::getElement()");

    }

  }
}


void testCombination()
{
  {
     // After verifying that each of the list operations can work
     // stand alone, I want to make sure that any random operation on
     // the list is also wrking as expected. i.e: I want to e able to
     // add (at the begining, at the end, at a given position), 
     // remove(from head, from tail, or any randomly chosen element) , 
     // list, extend the list, copy, in any order.
     // Remember: Assert(expected, actual, "comment");
     sprintf(buffer,"testCombination: testing ramdomly choosen operations on the list.");
     MYINTLIST list;

     ODELIBAssert(0UL,list.size(),"ODEDTList::size()");
     list.addAsLast(7);
     list.addAsLast(8);
     ODELIBAssert(2UL,list.size(),"ODEDTList::size()");
     list.addAsFirst(6);
     list.addAsLast(9);
     ODELIBAssert(4UL,list.size(),"ODEDTList::size()");
     list.removeAtPosition(2);
     ODELIBAssert(3UL,list.size(),"ODEDTList::size()");
     list.addAtPosition(2,5);
     list.addAtPosition(2,4);
     ODELIBAssert(5UL,list.size(),"ODEDTList::size()");
     list.removeAtPosition(5);
     list.removeAtPosition(4);
     ODELIBAssert(3UL,list.size(),"ODEDTList::size()");
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
    cout << endl << " You can see the failing tests by running '"<< exe <<" 1' " << endl;

}


/* ******************************
*
*  int main()
*
*  invokes each program seperately.
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

//Running the tests
  testConstructors();
  testInsertion();
  testSearching();
  testRemoval();
  testOperators();
  testCopying();
  testCursor();
  testCombination();

//Generating report
  generateReport(argv[0]);


return 0;
}

