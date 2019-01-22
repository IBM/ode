/**
 * CondEvaluator
 *
**/

#ifndef _ODE_LIB_UTIL_CONDEVAL_HPP_
#define _ODE_LIB_UTIL_CONDEVAL_HPP_

#include "lib/util/finforep.hpp"
#include "lib/util/minforep.hpp"
#include "lib/util/bitset.hpp"
#include "lib/util/cond.hpp"
#include "lib/string/string.hpp"
#include "lib/string/variable.hpp"
#include "lib/exceptn/parseexc.hpp"

class CondEvaluator
{
  public:

    CondEvaluator( const FileInfoReportable *file_info = 0,
        const MakeInfoReportable *make_info = 0,
        const Variable *var_eval = 0,
        int valid_functions = Cond::ALL_FUNC,
        boolean true_on_exception = false ) :
        file_info( file_info ), make_info( make_info ),
        var_eval( var_eval ), valid_functions( valid_functions ),
        true_on_exception( true_on_exception ), depth( 0 ),
        cond_considered( false )
    {
    };

    virtual ~CondEvaluator()
    {
    };

    // to change constructor parameters after construction...
    inline void changeFileInfo( const FileInfoReportable *file_info );
    inline void changeMakeInfo( const MakeInfoReportable *make_info );
    inline void changeVariable( const Variable *var_eval );
    inline void changeValidFunctions( int valid_functions );
    inline void changeExceptionTruth( boolean true_on_exception );

    boolean parseIf( const String &line, boolean wrap_vars = false );
    inline boolean parseIfdef( const String &line );
    inline boolean parseIfndef( const String &line );
    inline boolean parseIfmake( const String &line );
    inline boolean parseIfnmake( const String &line );
    boolean parseElse();
    boolean parseElif( const String &line, boolean wrap_vars = false );
    inline boolean parseElifdef( const String &line );
    inline boolean parseElifndef( const String &line );
    inline boolean parseElifmake( const String &line );
    inline boolean parseElifnmake( const String &line );
    boolean parseEndif();
    inline boolean allBlocksClosed() const;
    inline boolean prevCondEvaluated() const;


  private:

    // variables to pass to Cond
    const FileInfoReportable *file_info;
    const MakeInfoReportable *make_info;
    const Variable *var_eval;
    int valid_functions;
    boolean true_on_exception;
    boolean cond_considered;   // Does this cond need to be evaluated?

    // variables used to save state information for nesting
    int depth;        // how deep in the nesting are we?
    BitSet truth;     // what is the current truth value at each level?
    BitSet seen_true; // have we already processed a true block?
    BitSet seen_else; // have we encountered an "else" at each level?

    // types for the private parse[El]If function(s)
    enum
    {
      IF_TYPE, IFDEF_TYPE, IFNDEF_TYPE, IFMAKE_TYPE, IFNMAKE_TYPE
    };

    void throwParseException();
    void throwParseException( ParseException &e );
    boolean parseIf( int if_type, const String &line );
    boolean parseElif( int if_type, const String &line );
    boolean parseCond( int if_type, const String &line );
};


inline void CondEvaluator::changeFileInfo( const FileInfoReportable *file_info )
{
  this->file_info = file_info;
}

inline void CondEvaluator::changeMakeInfo( const MakeInfoReportable *make_info )
{
  this->make_info = make_info;
}

inline void CondEvaluator::changeVariable( const Variable *var_eval )
{
  this->var_eval = var_eval;
}

inline void CondEvaluator::changeValidFunctions( int valid_functions )
{
  this->valid_functions = valid_functions;
}

inline void CondEvaluator::changeExceptionTruth( boolean true_on_exception )
{
  this->true_on_exception = true_on_exception;
}

inline boolean CondEvaluator::parseIfdef( const String &line )
{
  return (parseIf( IFDEF_TYPE, line ));
}

inline boolean CondEvaluator::parseIfndef( const String &line )
{
  return (parseIf( IFNDEF_TYPE, line ));
}

inline boolean CondEvaluator::parseIfmake( const String &line )
{
  return (parseIf( IFMAKE_TYPE, line ));
}

inline boolean CondEvaluator::parseIfnmake( const String &line )
{
  return (parseIf( IFNMAKE_TYPE, line ));
}

inline boolean CondEvaluator::parseElifdef( const String &line )
{
  return (parseElif( IFDEF_TYPE, line ));
}

inline boolean CondEvaluator::parseElifndef( const String &line )
{
  return (parseElif( IFNDEF_TYPE, line ));
}

inline boolean CondEvaluator::parseElifmake( const String &line )
{
  return (parseElif( IFMAKE_TYPE, line ));
}

inline boolean CondEvaluator::parseElifnmake( const String &line )
{
  return (parseElif( IFNMAKE_TYPE, line ));
}

inline boolean CondEvaluator::allBlocksClosed() const
{
  return (depth <= 0);
}

inline boolean CondEvaluator::prevCondEvaluated() const
{
  return (cond_considered);
}

#endif /* _ODE_LIB_UTIL_CONDEVAL_HPP_ */
