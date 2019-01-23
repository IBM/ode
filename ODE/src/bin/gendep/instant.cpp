using namespace std;
// Generate explicit instantiations for gendep.

#include "lib/string/smartstr.hpp"
#include "lib/portable/hashtabl.cxx"
#include "lib/portable/nilist.cxx"

#ifdef USE_PRAGMA_FOR_TEMPINST
#pragma define (Hashtable< SmartCaseString, char >)
#pragma define (HashEnumBase< SmartCaseString, char >)
#else
template class Hashtable< SmartCaseString, char >;
template class HashEnumBase< SmartCaseString, char >;
#endif
