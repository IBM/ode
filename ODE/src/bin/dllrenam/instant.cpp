// Generate explicit instantiations for dllrenam.

#include "lib/portable/array.cxx"

#ifdef USE_PRAGMA_FOR_TEMPINST
#pragma define (Array< int >)
#else
template class Array< int >;
#endif
