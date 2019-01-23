using namespace std;
// Generate explicit instantiations for mk.

using namespace std;
#include "lib/string/string.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/portable/collectn.cxx"
#include "lib/portable/array.cxx"
#include "lib/portable/stack.cxx"
#include "lib/portable/vector.cxx"
#include "lib/portable/hashtabl.cxx"
#include "lib/portable/nilist.cxx"

#include "bin/make/cmdable.hpp"
#include "bin/make/graphnd.hpp"
#include "bin/make/targnode.hpp"
#include "bin/make/commands.hpp"
#include "bin/make/suffix.hpp"
#include "bin/make/suffpair.hpp"
#include "bin/make/passnode.hpp"
#include "bin/make/job.hpp"
#include "bin/make/keyword.hpp"
#include "bin/make/mfstmnt.hpp"
#include "bin/make/makefile.hpp"

#ifdef USE_PRAGMA_FOR_TEMPINST
void dummy_instantiator()
{
  elementsEqual( (GraphNode*)0, (GraphNode*)0 );
  elementsEqual( (TargetNode*)0, (TargetNode*)0 );
  elementsEqual( (PassNode*)0, (PassNode*)0 );
  elementsEqual( (Suffix*)0, (Suffix*)0 );
  elementsEqual( (SuffixPair*)0, (SuffixPair*)0 );
  elementsEqual( (PatternPair*)0, (PatternPair*)0 );
}
#else
template boolean elementsEqual( Suffix* const&, Suffix* const& );
template boolean elementsEqual( SuffixPair* const&, SuffixPair* const& );
template boolean elementsEqual( PatternPair* const&, PatternPair* const& );
template boolean elementsEqual( GraphNode* const&, GraphNode* const& );
template boolean elementsEqual( TargetNode* const&, TargetNode* const& );
template boolean elementsEqual( PassNode* const&, PassNode* const& );
#endif

#ifdef USE_PRAGMA_FOR_TEMPINST
#pragma define (Array< Commandable* >)
#pragma define (Stack< GraphNode* >)
#pragma define (Stack< TargetNode* >)
#pragma define (Vector< GraphNode* >)
#pragma define (Vector< TargetNode* >)
#pragma define (Vector< RunTimeForLoop* >)
#pragma define (Vector< Job* >)
#pragma define (Hashtable< SmartCaseString, Makefile* >)
#pragma define (Hashtable< String, Keyword* >)
#pragma define (Hashtable< SmartCaseString, GraphNode* >)
#pragma define (Hashtable< SmartCaseString, TargetNode* >)
#pragma define (Hashtable< SmartCaseString, RunTimeForLoop* >)
#pragma define (Hashtable< SmartCaseString, Vector< SuffixPair* >* >)
#pragma define (HashEnumBase< SmartCaseString, Makefile* >)
#pragma define (HashEnumBase< SmartCaseString, Vector< SuffixPair* >* >)
#pragma define (HashEnumBase< SmartCaseString, GraphNode* >)
#pragma define (HashEnumBase< SmartCaseString, TargetNode* >)
#pragma define (HashEnumBase< SmartCaseString, RunTimeForLoop* >)
#pragma define (ODETDList< MakefileStatement >)
#pragma define (ODETDList< Command >)
#pragma define (ODETDList< GraphNode* >)
#pragma define (ODETDList< TargetNode* >)
#pragma define (ODETDList< RunTimeForLoop* >)
#pragma define (ODETDList< MakefileStatement const * >)
#pragma define (ODETDList< Job* >)
#ifdef __WEBMAKE__
#ifdef __WEBDAV__
#pragma define (HashEnumBase< String, WebResource* >)
#pragma define (Hashtable< String, WebResource* >)
#endif // __WEBDAV__
#endif // __WEBMAKE__
#else
template class Array< Commandable* >;
template class Stack< GraphNode* >;
template class Stack< TargetNode* >;
template class Vector< GraphNode* >;
template class Vector< TargetNode* >;
template class Vector< RunTimeForLoop* >;
template class Vector< Job* >;
template class Hashtable< SmartCaseString, Makefile* >;
template class Hashtable< String, Keyword* >;
template class Hashtable< SmartCaseString, GraphNode* >;
template class Hashtable< SmartCaseString, TargetNode* >;
template class Hashtable< SmartCaseString, RunTimeForLoop* >;
template class Hashtable< SmartCaseString, Vector< SuffixPair* >* >;
template class HashEnumBase< SmartCaseString, Makefile* >;
template class HashEnumBase< SmartCaseString, Vector< SuffixPair* >* >;
template class HashEnumBase< SmartCaseString, GraphNode* >;
template class HashEnumBase< SmartCaseString, TargetNode* >;
template class HashEnumBase< SmartCaseString, RunTimeForLoop* >;
template class ODETDList< MakefileStatement >;
template class ODETDList< Command >;
template class ODETDList< GraphNode* >;
template class ODETDList< TargetNode* >;
template class ODETDList< RunTimeForLoop* >;
template class ODETDList< MakefileStatement const * >;
template class ODETDList< Job* >;
#ifdef __WEBMAKE__
#ifdef __WEBDAV__
template class HashEnumBase< String, WebResource* >;
template class Hashtable< String, WebResource* >;
#endif // __WEBDAV__
#endif // __WEBMAKE__
#endif
