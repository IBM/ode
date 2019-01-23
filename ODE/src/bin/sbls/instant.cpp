using namespace std;
// Generate explicit instantiations for sbls.

#include "lib/string/string.hpp"
#include "lib/portable/nilist.cxx"
#include "lib/portable/stack.cxx"

#ifdef USE_PRAGMA_FOR_TEMPINST
#pragma define (Stack< String >)
#else
template class Stack< String >;
#endif
