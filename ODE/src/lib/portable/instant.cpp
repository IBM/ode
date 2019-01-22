/**
 * This file contains all of the template instantiations
 * for the library.
 *
**/
#include "lib/string/setvars.cxx"
#include "lib/portable/array.cxx"
#include "lib/portable/hashtabl.cxx"
#include "lib/portable/nilist.cxx"
#include "lib/portable/vector.cxx"

#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/io/setvarcf.hpp"
#include "lib/io/file.hpp"
#include "lib/portable/hashtabl.hpp"
#include "lib/util/cachfile.hpp"
#include "lib/util/cachdir.hpp"
#include "lib/util/cacharch.hpp"
#include "lib/util/cachamem.hpp"
#include "lib/intcmds/target.hpp"


#ifdef USE_PRAGMA_FOR_TEMPINST
#pragma define(Vector< SmartCaseString >)
#pragma define(Vector< String >)
#pragma define(SetVarsTemplate< String >)
#pragma define(SetVarsTemplate< SetVarConfigFileData >)
#pragma define(Array< String >)
#pragma define(Array< StringArray >)
#pragma define(Array< File >)
#pragma define(Hashtable< SmartCaseString, CachedDir* >)
#pragma define(Hashtable< SmartCaseString, CachedFile* >)
#pragma define(Hashtable< SmartCaseString, Archive* >)
#pragma define(Hashtable< SmartCaseString, SmartCaseString >)
#pragma define(Hashtable< SmartCaseString, String >)
#pragma define(Hashtable< SmartCaseString, SetVarConfigFileData >)
#pragma define(Hashtable< String, CachedArchMember* >)
#pragma define(Hashtable< SmartCaseString, int >)
#pragma define(Hashtable< String, Target* >)
#pragma define(HashEnumBase< SmartCaseString, SetVarConfigFileData >)
#pragma define(HashEnumBase< SmartCaseString, SmartCaseString >)
#pragma define(HashEnumBase< SmartCaseString, String >)
#pragma define(HashEnumBase< SmartCaseString, CachedDir* >)
#pragma define(HashEnumBase< String, Target* >)
#pragma define(ODETDList< String >)
#else
template class Vector< SmartCaseString >;
template class Vector< String >;
template class SetVarsTemplate< String >;
template class SetVarsTemplate< SetVarConfigFileData >;
template class Array< String >;
template class Array< StringArray >;
template class Array< File >;
template class Hashtable< SmartCaseString, CachedDir* >;
template class Hashtable< SmartCaseString, CachedFile* >;
template class Hashtable< SmartCaseString, Archive* >;
template class Hashtable< SmartCaseString, SmartCaseString >;
template class Hashtable< SmartCaseString, String >;
template class Hashtable< SmartCaseString, SetVarConfigFileData >;
template class Hashtable< String, CachedArchMember* >;
template class Hashtable< SmartCaseString, int >;
template class Hashtable< String, Target* >;
template class HashEnumBase< SmartCaseString, SetVarConfigFileData >;
template class HashEnumBase< SmartCaseString, SmartCaseString >;
template class HashEnumBase< SmartCaseString, String >;
template class HashEnumBase< SmartCaseString, CachedDir* >;
template class HashEnumBase< String, Target* >;
template class ODETDList< String >;
#endif
