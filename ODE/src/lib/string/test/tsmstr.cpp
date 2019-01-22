#include <stdio.h>
#include <string.h>
#include "lib/string/smartstr.hpp"
#include "lib/portable/hashtabl.hpp"
#include <base/binbase.hpp>

/**
* Global Variables:
* flag: denotes the integer passed as a commandline parameter. It can be 0,1,2
* flag = 0: generates only final report
* flag = 1: generates final report along with failed tests
* flag = 2: generates final report along with all tests done
* passed: denotes the number of tests passed
* failed: denotes the number of tests failed
* total: denotes the total number of tests done. total = passed + failed
**/

unsigned int flag;
unsigned long passed = 0UL;
unsigned long failed = 0UL;
unsigned long total = 0UL;

void printpass( const char* mesg )
{
 if (flag == 2)
     cout << mesg << endl;
}

void printfail( const char* mesg )
{
 if ((flag == 1) || (flag == 2))
     cout << mesg << endl;
}
 
void generateReport( char *prog )
{
 total = passed + failed;
 cout<<"\n******************************************\n"<<endl;
 cout<<"Total Number of Tests performed: " << total << endl;
 cout<<"Number of Tests passed         : " << passed << endl;
 cout<<"Number of Tests Failed         : " << failed << endl; 
 cout<<"\n******************************************\n"<<endl;
 if ((failed != 0) && (flag == 0))
  {
   cout<< "Enter \"" << prog << " 1\" to see failed tests only" << endl;
   cout<< "Enter \"" << prog << " 2\" to see all the tests\n" << endl;
  }
}

/**
* Assertions are used to verify if a method is properly working
* expected: It is the value the method is expected to return
* actual: It is the acual value the method is returning
**/
 
void ODELIBAssert( char expected, char actual, const char* method )
{
  char mesg[255];
  if (expected == actual)
   {
     sprintf(mesg,"Testing method %s was O.K", method);
     printpass( mesg );
     passed++;
   }
  else
   {
     sprintf(mesg,"Testing method %s failed\n   I was expecting %c but actual value is %c",method, expected, actual);
     printfail( mesg );
     failed++;
   }
}


void ODELIBAssert( int expected, int actual, const char* method )
{
  char mesg[255];
  if (expected == actual)
   {
     sprintf(mesg,"Testing method %s was O.K", method);
     printpass( mesg );
     passed++;
   }
  else
   {
     sprintf(mesg,"Testing method %s failed\n   I was expecting %d but actual value is %d", method, expected, actual);
     printfail( mesg );
     failed++;
   }
}

void ODELIBAssert( long expected, long actual, const char* method )
{
  char mesg[255];
  if (expected == actual)
   {
     sprintf(mesg,"Testing method %s was O.K", method);
     printpass( mesg );
     passed++;
   }
  else
   {
     sprintf(mesg,"Testing method %s failed\n   I was expecting %ld but actual value is %ld", method, expected, actual);
     printfail( mesg );
     failed++;
   }
}

void ODELIBAssert( unsigned long expected, unsigned long actual, const char* method )
{
  char mesg[255];
  if (expected == actual)
   {
     sprintf(mesg,"Testing method %s was O.K", method);
     printpass( mesg );
     passed++;
   }
  else
   {
     sprintf(mesg,"Testing method %s failed\n   I was expecting %lu but actual value is %lu", method, expected, actual);
     printfail( mesg );
     failed++;
   }
}
 

/**
* Tests SmartCaseString::ODEHashFunction()
**/

void testODEHashFunction()
 {
 int i;
 SmartCaseString s1("FILE");
 SmartCaseString s2("File");
 Hashtable<SmartCaseString, String> h;
 h.put(s1, "crap");
 h.put(s2, "stuff");
 if (strcmp( h.get(s1)->toCharPtr(), h.get(s2)->toCharPtr() ) == 0)
   i=0;
 else 
   i=1;
 ODELIBAssert( PlatformConstants::onCaseSensitiveMachine(), i,
             "SmartCaseString::ODEHashFunction()" );  
 
}


/**
* Tests SmartCaseString::equals()
**/

void testEquals()
{
 SmartCaseString scstr1("HELLO");
 SmartCaseString scstr2("HELLO");
 SmartCaseString scstr3("hello");
 SmartCaseString scstr4("HellO");
 SmartCaseString scstr5("hello");
 String str1("HELLO");
 String str2("hello");
 String str3("HellO");

 ODELIBAssert( !PlatformConstants::onCaseSensitiveMachine(),
               scstr1.equals(scstr5), 
              "SmartCaseString::equals(const SmartCaseString &)" );

 ODELIBAssert( !PlatformConstants::onCaseSensitiveMachine(), 
               scstr1.equals(str2), 
               "[2]SmartCaseString::equals(const String &)" );

  scstr1.replaceThis( "h", "j" );

  ODELIBAssert( !PlatformConstants::onCaseSensitiveMachine(),
      scstr1.equals("Jello"), "[1]SmartCaseString::equals(const char *)" );
}
 

/**
* Tests SmartCaseString::operator==()
**/
void testOperatorEquals()
{
 SmartCaseString scstr1("HELLO");
 SmartCaseString scstr2("HELLO");
 SmartCaseString scstr3("hello");
 SmartCaseString scstr4("HellO");
 SmartCaseString scstr5("hello");
 String str1("HELLO");
 String str2("hello");
 String str3("HellO");
 const char* ptr1 = "HELLO";
 const char* ptr2 = "hello";
 const char* ptr3 = "heLLo";

 ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(), scstr1==scstr5, 
             "SmartCaseString::operator==(const SmartCaseString &)");

 ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(),
              scstr1==str2, "SmartCaseString::operator==(const String &)");
 ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(), 
              scstr1==str3, "SmartCaseString::operator==(const String &)");
 
 ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(), 
             scstr1.SmartCaseString::operator==(ptr2), 
             "SmartCaseString::operator==(const char *)");
 ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(), 
              scstr1.SmartCaseString::operator==(ptr3), 
              "SmartCaseString::operator==(const char *)");
}

 
/**
* Tests SmartCaseString::operator!=()
**/
void testOperatorNotEqual()
{
 SmartCaseString scstr1("HELLO");
 SmartCaseString scstr2("HELLO");
 SmartCaseString scstr3("hello");
 SmartCaseString scstr4("HellO");
 SmartCaseString scstr5("hello");
 String str1("HELLO");
 String str2("hello");
 String str3("HellO");
 const char* ptr1 = "HELLO";
 const char* ptr2 = "hello";
 const char* ptr3 = "HellO";

 ODELIBAssert(PlatformConstants::onCaseSensitiveMachine(), scstr1!=scstr5, 
              "SmartCaseString::operator!=(const SmartCaseString &)");

 ODELIBAssert(PlatformConstants::onCaseSensitiveMachine(), 
              scstr1!=str2, "SmartCaseString::operator!=(const String &)");
 ODELIBAssert(PlatformConstants::onCaseSensitiveMachine(), 
              scstr1!=str3, "SmartCaseString::operator!=(const String &)");

 ODELIBAssert(PlatformConstants::onCaseSensitiveMachine(), 
             scstr1.SmartCaseString::operator!=(ptr2), 
             "SmartCaseString::operator!=(const char *)");
 ODELIBAssert(PlatformConstants::onCaseSensitiveMachine(), 
             scstr1.SmartCaseString::operator!=(ptr3), 
            "SmartCaseString::operator!=(const char *)");
}
 

/**
* Tests SmartCaseString::operator<()
**/
void testOperatorLessThan()
{
 SmartCaseString scstr1("HELLO");
 SmartCaseString scstr2("HELLO");
 SmartCaseString scstr3("hello");
 SmartCaseString scstr4("HellO");
 SmartCaseString scstr5("HellO");
 String str1("HELLO");
 String str2("hello");
 String str3("HellO");
 
 ODELIBAssert(PlatformConstants::onCaseSensitiveMachine(), scstr1<scstr5, 
               "SmartCaseString::operator<(const SmartCaseString &)");

}


/**
* Tests SmartCaseString::operator<=()
**/
void testOperatorLessThanOrEqual()
{
 SmartCaseString scstr1("HELLO");
 SmartCaseString scstr2("HELLO");
 SmartCaseString scstr3("hello");
 SmartCaseString scstr4("HellO");
 SmartCaseString scstr5("HellO");
 String str1("HELLO");
 String str2("hello");
 String str3("HellO");
 
 ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(), scstr5<=scstr1, 
             "SmartCaseString::operator<=(const SmartCaseString &)");
}

/**
* Tests SmartCaseString::operator>()
**/
void testOperatorGreaterThan()
{
 SmartCaseString scstr1("HELLO");
 SmartCaseString scstr2("HELLO");
 SmartCaseString scstr3("hello");
 SmartCaseString scstr4("HellO");
 SmartCaseString scstr5("HellO");
 String str1("HELLO");
 String str2("hello");
 String str3("HellO");
 
 ODELIBAssert(PlatformConstants::onCaseSensitiveMachine(), scstr5>scstr1, 
             "SmartCaseString::operator>(const SmartCaseString &)");
}

/**
* Tests SmartCaseString::operator>=()
**/
void testOperatorGreaterThanOrEqual()
{
 SmartCaseString scstr1("HELLO");
 SmartCaseString scstr2("HELLO");
 SmartCaseString scstr3("hello");
 SmartCaseString scstr4("HellO");
 SmartCaseString scstr5("HellO");
 String str1("HELLO");
 String str2("hello");
 String str3("HellO");
 
 ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(), scstr1>=scstr5, 
             "SmartCaseString::operator>=(const SmartCaseString &)");
}

/**
* Tests SmartCaseString::startsWith()
**/
void testStartsWith()
{
 String str1("H");
 String str2("HEL");
 String str3("he");
 SmartCaseString scstr1("HELLO");
 SmartCaseString scstr2("hello");
 
 ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(), 
              scstr1.startsWith(str3), 
              "SmartCaseString::startsWith(const String &)");
}
 
 
/**
* Tests SmartCaseString::endsWith()
**/
void testEndsWith()
{
 String str1("O");
 String str2("LLO");
 String str3("lo");
 SmartCaseString scstr1("HELLO");
 SmartCaseString scstr2("hello");
 
 ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(), 
              scstr1.endsWith(str3), 
             "SmartCaseString::endsWith(const String &)");
}


/**
* Tests SmartCaseString::indexOf()
**/
void testIndexOf()
{
 SmartCaseString scstr1("hello");
 String str1("HelL");
 String str2("LlO");

 ODELIBAssert((PlatformConstants::onCaseSensitiveOS()) ? 0UL : 1UL,
     scstr1.indexOf('H'), "SmartCaseString::indexOf(char, unsigned int)");
 ODELIBAssert((PlatformConstants::onCaseSensitiveOS()) ? 0UL : 3UL,
		 scstr1.indexOf('L',3), "SmartCaseString::indexOf(char, unsigned int)");
 
 ODELIBAssert((PlatformConstants::onCaseSensitiveOS()) ? 0UL : 1UL,
		 scstr1.indexOf(str1),
     "SmartCaseString::indexOf(const String, unsigned int)");
 ODELIBAssert((PlatformConstants::onCaseSensitiveOS()) ? 0UL : 3UL,
     scstr1.indexOf(str2, 2), 
     "SmartCaseString::indexOf(const String, unsigned int)");
}
 
/**
* Tests SmartCaseString::lastIndexOf()
**/
void testLastIndexOf()
{
 SmartCaseString scstr1("hello");
 String str1("HelL");
 String str2("LlO");

 ODELIBAssert((PlatformConstants::onCaseSensitiveOS()) ? 0UL : 1UL,
     scstr1.lastIndexOf('H'),
		 "SmartCaseString::lastIndexOf(char, unsigned int)");
 ODELIBAssert((PlatformConstants::onCaseSensitiveOS()) ? 0UL : 4UL,
		 scstr1.lastIndexOf('L'),
		 "SmartCaseString::lastIndexOf(char, unsigned int)");
 ODELIBAssert((PlatformConstants::onCaseSensitiveOS()) ? 0UL : 3UL,
		 scstr1.lastIndexOf('L',3),
		 "SmartCaseString::lastIndexOf(char, unsigned int)");
 
 ODELIBAssert((PlatformConstants::onCaseSensitiveOS()) ? 0UL : 1UL,
		 scstr1.lastIndexOf(str1),
     "SmartCaseString::lastIndexOf(const String, unsigned int)");
 ODELIBAssert((PlatformConstants::onCaseSensitiveOS()) ? 0UL : 3UL,
     scstr1.lastIndexOf(str2), 
     "SmartCaseString::lastIndexOf(const String, unsigned int)");
}


/**
* Tests SmartCaseString::replace()
**/
void testReplace()
{
 SmartCaseString scstr1("ABBACADF");
 String str1("BB");
 String str2("YY");
 String str3("AD");
 String str4("AA");
 String str5("ad");
 String str6("aCa");

 ODELIBAssert(!PlatformConstants::onCaseSensitiveOS(),
             scstr1.replace('a', 'x', 2, 2)=="ABBxCxDF", 
            "SmartCaseString::replace(char, char, unsigned int, unsigned int)");
 ODELIBAssert(!PlatformConstants::onCaseSensitiveOS(),
             scstr1.replace('c', 'x', 2) == "ABBAxADF",
            "SmartCaseString::replace(char, char, unsigned int, unsigned int)");
 ODELIBAssert(!PlatformConstants::onCaseSensitiveOS(),
             scstr1.replace(str5, str2, 2) == "ABBACYYF",
            "SmartCaseString::replace(const String, const String, unsigned int, unsigned int)");
 ODELIBAssert(true, 
             scstr1.replace(str1, str5, 2, 1) == "AadACADF",
            "SmartCaseString::replace(const String, const String, unsigned int, unsigned int)");
 ODELIBAssert(true, 
             scstr1.replace(str4, str2, 2, 2) == "ABBACADF",
             "SmartCaseString::replace(const String, const String, unsigned int, unsigned int)");
}

/**
* Tests SmartCaseString::compareTo()
**/
void testCompareTo()
{
  SmartCaseString scstr1("HELLO");
  SmartCaseString scstr2("HELLO");
  SmartCaseString scstr3("HAI");
  SmartCaseString scstr4("hello");
  String str1("HAI");
  String str2("HELLOworld");
  String str3("HELLO");

  ODELIBAssert(true, scstr1.compareTo(str1) > 0, "SmartCaseString::compareTo(string)");
  ODELIBAssert(true, scstr1.compareTo(str2) < 0, "SmartCaseString::compareTo(string)");
  ODELIBAssert(!PlatformConstants::onCaseSensitiveMachine(), 
               scstr4.compareTo(str3) == 0, "SmartCaseString::compareTo(string)");

  ODELIBAssert(true, scstr1.compareTo(scstr2) == 0, "SmartCaseString::compareTo(string)");
  ODELIBAssert(true, scstr1.compareTo(scstr3) > 0, "SmartCaseString::compareTo(string)");
  ODELIBAssert(true, scstr3.compareTo(scstr1) < 0, "SmartCaseString::compareTo(string)");
 
}
  
int main(int argc, char *argv[], const char **envp)
{
  Tool::init( envp );
  if (argc == 1)
     flag = 0;
  else
     flag = atoi(argv[1]);
  testEquals();
  testOperatorEquals();
  testOperatorNotEqual();
  testOperatorLessThan();
  testOperatorLessThanOrEqual();
  testOperatorGreaterThan();
  testOperatorGreaterThanOrEqual();
  testStartsWith();
  testEndsWith();
  testIndexOf();
  testLastIndexOf();
  testReplace();
  testCompareTo();
  testODEHashFunction();
  generateReport(argv[0]);
  return 0;
}
