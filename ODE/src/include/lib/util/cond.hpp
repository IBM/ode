#ifndef _ODE_LIB_UTIL_COND_HPP_
#define _ODE_LIB_UTIL_COND_HPP_

#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/variable.hpp"
#include "lib/util/minforep.hpp"
#include "lib/util/finforep.hpp"

class Cond
{
  public:

    enum // for the valid_functions arg of the constructor
    {
      NO_FUNC = 0,
      DEFINED_FUNC = 1,
      EMPTY_FUNC = 2,
      EXISTS_FUNC = 4,
      MAKE_FUNC = 8,
      TARGET_FUNC = 16,
      UNDEFINED_FUNC = 32,
      NMAKE_FUNC = 64,
      ALL_FUNC = 127
    };

    static boolean evalIfExpr( const String &exp,
        const MakeInfoReportable *make_info,
        const FileInfoReportable *file_info,
        const Variable *var_eval, int valid_functions = ALL_FUNC );
    inline static boolean evalIfDefExpr( const String &exp,
        const MakeInfoReportable *make_info,
        const FileInfoReportable *file_info,
        const Variable *var_eval );
    inline static boolean evalIfNDefExpr( const String &exp,
        const MakeInfoReportable *make_info,
        const FileInfoReportable *file_info,
        const Variable *var_eval );
    inline static boolean evalIfMakeExpr( const String &exp,
        const MakeInfoReportable *make_info,
        const FileInfoReportable *file_info,
        const Variable *var_eval );
    inline static boolean evalIfNMakeExpr( const String &exp,
        const MakeInfoReportable *make_info,
        const FileInfoReportable *file_info,
        const Variable *var_eval );

    // a function used to wrap C/C++ preprocessor variables
    // in ${} characters so they can be evaluated normally by
    // Variable.  Used by the gendep tool (via CondEvaluator).
    static String wrapPreprocVars( const String &line );


  private:
  
    static const int ODEDLLPORT NO_TOKEN; // no tokens left
    static const int ODEDLLPORT UNARY_OP_TOKEN; // !
    static const int ODEDLLPORT BINARY_OP_TOKEN; // <, >, <=, >=, ==, !=
    static const int ODEDLLPORT BOOLEAN_OP_TOKEN; // &&, ||
    static const int ODEDLLPORT OPEN_PAREN_TOKEN; // (
    static const int ODEDLLPORT CLOSE_PAREN_TOKEN; // )
    static const int ODEDLLPORT VALUE_TOKEN; // value (string or integer)
    static const int ODEDLLPORT FUNCTION_TOKEN; // defined(), empty(), etc.
    static const String ODEDLLPORT DEFINED_FUNCNAME;
    static const String ODEDLLPORT UNDEFINED_FUNCNAME;
    static const String ODEDLLPORT EMPTY_FUNCNAME;
    static const String ODEDLLPORT EXISTS_FUNCNAME;
    static const String ODEDLLPORT MAKE_FUNCNAME;
    static const String ODEDLLPORT NMAKE_FUNCNAME;
    static const String ODEDLLPORT TARGET_FUNCNAME;
    static const String ODEDLLPORT TOKENIZE_CHARS;
    static const String ODEDLLPORT NULL_STRING; // not same as an empty string!
    static const String ODEDLLPORT ZERO_STRING;
    static const String ODEDLLPORT BOOLEAN_OR;
    static const String ODEDLLPORT BOOLEAN_AND;
    static const String ODEDLLPORT EQUAL;
    static const String ODEDLLPORT NOT_EQUAL;
    static const String ODEDLLPORT LESS_THAN;
    static const String ODEDLLPORT LESS_THAN_OR_EQUAL;
    static const String ODEDLLPORT GREATER_THAN;
    static const String ODEDLLPORT GREATER_THAN_OR_EQUAL;

    static int tokenize( const String &str, const Variable *var_eval,
        StringArray &result, int valid_functions );
    static boolean evalIfDef( const String &exp,
        const MakeInfoReportable *make_info,
        const FileInfoReportable *file_info,
        const Variable *var_eval, boolean reverse_truth );
    static boolean evalIfMake( const String &exp,
        const MakeInfoReportable *make_info,
        const FileInfoReportable *file_info,
        const Variable *var_eval, boolean reverse_truth );
    static boolean evalIf( const String &exp,
        const MakeInfoReportable *make_info,
        String &leftover, const Variable *var_eval, int valid_functions );
    static boolean evalIfFunc( const String &function_name, const String &exp,
        const MakeInfoReportable *make_info, String &leftover,
        const Variable *var_eval );
    static boolean evaluateFunction( StringArray &args,
        const MakeInfoReportable *make_info, const Variable *var_eval );
    static boolean evaluateIfDefined( const String &arg,
        const Variable *var_eval );
    static boolean evaluateIfEmpty( const String &arg,
        const MakeInfoReportable *make_info );
    static boolean evaluateIfExists( const String &arg,
        const MakeInfoReportable *make_info );
    static boolean evaluateIfMake( const String &arg,
        const MakeInfoReportable *make_info );
    static boolean evaluateIfTarget( const String &arg,
        const MakeInfoReportable *make_info );
    static boolean changeTruth( boolean old_truth, boolean new_truth,
        boolean do_not_change, const String &boolean_op );
    static long convertToInt( const String &value, String &leftover );
    static boolean valueCompare( const String &lhs, const String &binary_op,
        const String &rhs, boolean ints_only = false );
};

inline boolean Cond::evalIfDefExpr( const String &exp,
    const MakeInfoReportable *make_info,
    const FileInfoReportable *file_info,
    const Variable *var_eval )
{
  return (evalIfDef( exp, make_info, file_info, var_eval, false ));
}

inline boolean Cond::evalIfNDefExpr( const String &exp,
    const MakeInfoReportable *make_info,
    const FileInfoReportable *file_info,
    const Variable *var_eval )
{
  return (evalIfDef( exp, make_info, file_info, var_eval, true ));
}

inline boolean Cond::evalIfMakeExpr( const String &exp,
    const MakeInfoReportable *make_info,
    const FileInfoReportable *file_info,
    const Variable *var_eval )
{
  return (evalIfMake( exp, make_info, file_info, var_eval, false ));
}

inline boolean Cond::evalIfNMakeExpr( const String &exp,
    const MakeInfoReportable *make_info,
    const FileInfoReportable *file_info,
    const Variable *var_eval )
{
  return (evalIfMake( exp, make_info, file_info, var_eval, true ));
}

#endif /* _ODE_LIB_UTIL_COND_HPP_ */
