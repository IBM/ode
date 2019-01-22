/**
 * Cond
**/
#define _ODE_LIB_UTIL_COND_CPP_

//#include <iostream.h>
#include <string.h>
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>

#include <base/binbase.hpp>
#include "lib/util/cond.hpp"
#include "lib/portable/env.hpp"
#include "lib/string/strcon.hpp"
#include "lib/exceptn/parseexc.hpp"
#include "lib/exceptn/mfvarexc.hpp"

const int Cond::NO_TOKEN = 1;           // no tokens left
const int Cond::UNARY_OP_TOKEN = 2;     // !
const int Cond::BINARY_OP_TOKEN = 4;    // <, >, <=, >=, ==, !=
const int Cond::BOOLEAN_OP_TOKEN = 8;   // &&, ||
const int Cond::OPEN_PAREN_TOKEN = 16;  // (
const int Cond::CLOSE_PAREN_TOKEN = 32; // )
const int Cond::VALUE_TOKEN = 64;       // value (string or integer)
const int Cond::FUNCTION_TOKEN = 128;   // defined(), empty(), etc.

/**
 * These names are both used when parsing and also for the
 * specialized forms "ifdef/ifnef" and "ifmake/ifnmake".  As
 * a result, a few of them are not currently valid function
 * names for parsing purposes (e.g., ".if nmake(targ)" is not
 * yet legal syntax).  They are commented accordingly.
**/
const String Cond::DEFINED_FUNCNAME = "defined";
const String Cond::UNDEFINED_FUNCNAME = "undefined"; // not a real function
const String Cond::EMPTY_FUNCNAME = "empty";
const String Cond::EXISTS_FUNCNAME = "exists";
const String Cond::MAKE_FUNCNAME = "make";
const String Cond::NMAKE_FUNCNAME = "nmake"; // not a real function
const String Cond::TARGET_FUNCNAME = "target";

const String Cond::TOKENIZE_CHARS = "(!=<>|&)";
const String Cond::NULL_STRING = String( '\0' );
const String Cond::ZERO_STRING = "0";
const String Cond::BOOLEAN_OR = "||";
const String Cond::BOOLEAN_AND = "&&";
const String Cond::EQUAL = "==";
const String Cond::NOT_EQUAL = "!=";
const String Cond::LESS_THAN = "<";
const String Cond::LESS_THAN_OR_EQUAL = "<=";
const String Cond::GREATER_THAN = ">";
const String Cond::GREATER_THAN_OR_EQUAL = ">=";

/**
 * This function will evaluate a conditional expression and
 * return the boolean result of the expression.  Conditional
 * expressions have the form:
 * .if (${CONTEXT}=="i386_win32")
 *
 * @param exp The expression to be evaluated (assumed to be
 * all text AFTER ".if")
 * @param make_info Various information about this pass,
 * such as the Variable evaluator, the target finder, etc.
 * @param file_info The corresponding makefile statement.
 * @return The result of the conditional expression.
 * @exception ParseException If the conditional expression(s)
 * was (were) malformed.
 */
boolean Cond::evalIfExpr( const String &exp,
    const MakeInfoReportable *make_info,
    const FileInfoReportable *file_info,
    const Variable *var_eval, int valid_functions )
//    throws ParseException
{
  boolean rc;
  String leftover;

  try
  {
    rc = evalIf( exp, make_info, leftover, var_eval, valid_functions );
    if (leftover != Cond::NULL_STRING) // shouldn't be anything left
      throw Exception( (String( "extraneous characters: " ) +
          leftover).toCharPtr() );
    return (rc);
  }
  catch (Exception &e)
  {
    if (file_info == 0)
      throw ParseException( String( "Malformed conditional: " ) +
          e.getMessage() );
    else
      throw ParseException( file_info->getPathname(),
          file_info->getLineNumber(), String( "Malformed conditional: " ) +
          e.getMessage() );
  }
}

/**
 * This function will evaluate a conditional expression and
 * return the boolean result of the expression.  Conditional
 * ifdef expressions have the form:
 * .ifdef CONTEXT
 *
 * @param exp The expression to be evaluated (assumed to be
 * all text AFTER ".ifdef")
 * @param make_info Various information about this pass,
 * such as the Variable evaluator, the target finder, etc.
 * @param file_info The corresponding makefile statement.
 * @return The result of the conditional expression.
 * @exception ParseException If the conditional expression(s)
 * was (were) malformed.
 */
boolean Cond::evalIfDef( const String &exp,
    const MakeInfoReportable *make_info,
    const FileInfoReportable *file_info,
    const Variable *var_eval, boolean reverse_truth )
//    throws ParseException
{
  boolean rc;
  String leftover;

  try
  {
    rc = evalIfFunc( ((reverse_truth) ?
        Cond::UNDEFINED_FUNCNAME : Cond::DEFINED_FUNCNAME),
        exp, make_info, leftover, var_eval );
    if (leftover != Cond::NULL_STRING)
      throw Exception( (String( "extraneous characters: " ) +
          leftover).toCharPtr() );
    return (rc);
  }
  catch (Exception &e)
  {
    if (file_info == 0)
      throw ParseException( String( "Malformed conditional: " ) +
          e.getMessage() );
    else
      throw ParseException( file_info->getPathname(),
          file_info->getLineNumber(), String( "Malformed conditional: " ) +
          e.getMessage() );
  }
}

/**
 * This function will evaluate a conditional expression and
 * return the boolean result of the expression.  Conditional
 * ifmake expressions have the form:
 * .ifmake target
 *
 * @param exp The expression to be evaluated (assumed to be
 * all text AFTER ".ifmake")
 * @param make_info Various information about this pass,
 * such as the Variable evaluator, the target finder, etc.
 * @param file_info The corresponding makefile statement.
 * @return The result of the conditional expression.
 * @exception ParseException If the conditional expression(s)
 * was (were) malformed.
 */
boolean Cond::evalIfMake( const String &exp,
    const MakeInfoReportable *make_info,
    const FileInfoReportable *file_info,
    const Variable *var_eval, boolean reverse_truth )
//    throws ParseException
{
  boolean rc;
  String leftover;

  try
  {
    rc = evalIfFunc( ((reverse_truth) ?
        Cond::NMAKE_FUNCNAME : Cond::MAKE_FUNCNAME),
        exp, make_info, leftover, var_eval );
    if (leftover != Cond::NULL_STRING)
      throw Exception( (String( "extraneous characters: " ) +
          leftover).toCharPtr() );
    return (rc);
  }
  catch (Exception &e)
  {
    if (file_info == 0)
      throw ParseException( String( "Malformed conditional: " ) +
          e.getMessage() );
    else
      throw ParseException( file_info->getPathname(),
          file_info->getLineNumber(), String( "Malformed conditional: " ) +
          e.getMessage() );
  }
}

int Cond::tokenize( const String &str, const Variable *var_eval,
    StringArray &result, int valid_functions )
{
  int rc = Cond::NO_TOKEN;
  const String trimstr = str.trim();

  result.extendTo( 2 ); // make sure it's got two elements
  result[result.firstIndex()] =
      result[result.firstIndex() + 1] = Cond::NULL_STRING;
  // first make sure there's something to tokenize!
  if (trimstr == Cond::NULL_STRING || trimstr.length() < 1)
    return (Cond::NO_TOKEN);
  if (var_eval == 0)
  {
    Variable tmp_eval( Env::getSetVars(), false, '\0' );
    tmp_eval.parseUntil( trimstr, Cond::TOKENIZE_CHARS, true, false, &result );
  }
  else
    var_eval->parseUntil( trimstr, Cond::TOKENIZE_CHARS, true, false, &result );

  result[ARRAY_FIRST_INDEX].trimThis();
  result[ARRAY_FIRST_INDEX + 1].trimThis();
  if (result[result.firstIndex()].length() < 1)
    result[result.firstIndex()] = Cond::NULL_STRING;
  if (result[result.firstIndex() + 1].length() < 1)
    result[result.firstIndex() + 1] = Cond::NULL_STRING;
  if (result[result.firstIndex()] == Cond::NULL_STRING &&
      trimstr.startsWith( String( VARIABLE_START_CHAR ) ))
  {
    result[result.firstIndex()] = StringConstants::EMPTY_STRING;
    rc = Cond::VALUE_TOKEN;
  }
  else if (result[result.firstIndex()] == Cond::NULL_STRING &&
      result[result.firstIndex() + 1] != Cond::NULL_STRING)
  { // first token found was a delimiter
    // always put first char of the delimiter(s) in result[0]
    result[result.firstIndex()] = String(
        result[result.firstIndex() + 1].charAt( STRING_FIRST_INDEX ) );
    result[ARRAY_FIRST_INDEX + 1].remove( STRING_FIRST_INDEX, 1 );
    if (result[result.firstIndex() + 1].length() < 1)
      result[result.firstIndex() + 1] = Cond::NULL_STRING;

    rc = Cond::BINARY_OP_TOKEN; // typical default
    switch (result[result.firstIndex()].charAt( STRING_FIRST_INDEX ))
    {
      case '(':
        rc = Cond::OPEN_PAREN_TOKEN;
        break;
      case ')':
        rc = Cond::CLOSE_PAREN_TOKEN;
        break;
      case '!':
        rc = Cond::UNARY_OP_TOKEN;
        // FALL THROUGH!
      case '<':
      case '>':
        if (result[result.firstIndex() + 1].charAt(
            STRING_FIRST_INDEX ) == '=')
        {
          rc = Cond::BINARY_OP_TOKEN; // for !'s sake
          result[result.firstIndex()] += StringConstants::EQUAL_SIGN;
          result[ARRAY_FIRST_INDEX + 1].remove( STRING_FIRST_INDEX, 1 );
        }
        break;
      case '=':
        result[result.firstIndex()] += StringConstants::EQUAL_SIGN;
        // insure "==" was used ("=" is invalid)
        if (result[result.firstIndex() + 1].charAt(
            STRING_FIRST_INDEX ) == '=')
        {
          result[ARRAY_FIRST_INDEX + 1].remove( STRING_FIRST_INDEX, 1 );
        }
        else
          throw Exception( "'=' found, but expected '=='" );
        break;
      case '|':
      case '&':
        rc = Cond::BOOLEAN_OP_TOKEN;
        result[result.firstIndex()] +=
            result[result.firstIndex()].charAt( STRING_FIRST_INDEX );
        if (result[result.firstIndex() + 1].charAt(
            STRING_FIRST_INDEX ) ==
            result[result.firstIndex()].charAt( STRING_FIRST_INDEX ))
          result[ARRAY_FIRST_INDEX + 1].remove( STRING_FIRST_INDEX, 1 );
        else
          throw Exception( (String( "'" ) +
              result[result.firstIndex()].charAt( STRING_FIRST_INDEX ) +
              "' found, but expected '" +
              result[result.firstIndex()].charAt( STRING_FIRST_INDEX ) +
              result[result.firstIndex()].charAt( STRING_FIRST_INDEX ) +
              '\'').toCharPtr() );
        break;
      default: // shouldn't ever happen, right?
        rc = Cond::NO_TOKEN;
        result[result.firstIndex()] =
            result[result.firstIndex() + 1] = Cond::NULL_STRING;
        break;
    }
  }
  else
  {
    if (valid_functions != NO_FUNC &&
        trimstr.startsWith( result[ARRAY_FIRST_INDEX] ))
    {
      if ((valid_functions & Cond::DEFINED_FUNC) &&
          result[ARRAY_FIRST_INDEX].equals( Cond::DEFINED_FUNCNAME ))
        rc = Cond::FUNCTION_TOKEN;
      else if ((valid_functions & Cond::EMPTY_FUNC) &&
          result[ARRAY_FIRST_INDEX].equals( Cond::EMPTY_FUNCNAME ))
        rc = Cond::FUNCTION_TOKEN;
      else if ((valid_functions & Cond::EXISTS_FUNC) &&
          result[ARRAY_FIRST_INDEX].equals( Cond::EXISTS_FUNCNAME ))
        rc = Cond::FUNCTION_TOKEN;
      else if ((valid_functions & Cond::MAKE_FUNC) &&
          result[ARRAY_FIRST_INDEX].equals( Cond::MAKE_FUNCNAME ))
        rc = Cond::FUNCTION_TOKEN;
      else if ((valid_functions & Cond::TARGET_FUNC) &&
          result[ARRAY_FIRST_INDEX].equals( Cond::TARGET_FUNCNAME ))
        rc = Cond::FUNCTION_TOKEN;
      else
        rc = Cond::VALUE_TOKEN;
    }
    else
      rc = Cond::VALUE_TOKEN;
  }

  return (rc);
}

boolean Cond::evalIf( const String &exp, const MakeInfoReportable *make_info,
    String &leftover, const Variable *var_eval, int valid_functions )
{
  String lhs = Cond::NULL_STRING, binary_op, boolean_op;
  boolean determined = false, truth = false, temp_truth, notted = false;
  int token_type;
  int valid_tokens = Cond::UNARY_OP_TOKEN | Cond::FUNCTION_TOKEN |
      Cond::VALUE_TOKEN | Cond::OPEN_PAREN_TOKEN;
  StringArray tokens( 2, 2 );

  leftover = exp;
  do
  {
    token_type = tokenize( leftover, var_eval, tokens, valid_functions );
//    cout << "token_type:" << token_type <<
//        ", tokens[1]:" << tokens[tokens.firstIndex()] <<
//        ", tokens[2]:" << tokens[tokens.firstIndex() + 1] << endl;
    leftover = tokens[tokens.firstIndex() + 1];
    if ((token_type & valid_tokens) == 0)
      throw Exception( (String( "invalid token: " ) +
          tokens[ARRAY_FIRST_INDEX]).toCharPtr() );
    switch (token_type)
    {
      case Cond::OPEN_PAREN_TOKEN:
        temp_truth = evalIf( leftover, make_info, leftover,
            var_eval, valid_functions );
        if (leftover.charAt( STRING_FIRST_INDEX ) != ')')
          throw Exception( "missing ')'" );
        else
          leftover.remove( STRING_FIRST_INDEX, 1 );
        if (notted)
        {
          temp_truth = !temp_truth;
          notted = false;
        }
        truth = changeTruth( truth, temp_truth, determined,
            boolean_op );
        valid_tokens = Cond::BOOLEAN_OP_TOKEN | Cond::NO_TOKEN |
            Cond::CLOSE_PAREN_TOKEN;
        break;
      case Cond::CLOSE_PAREN_TOKEN: // done, but pass back the token to verify
        if (leftover == Cond::NULL_STRING)
          leftover = StringConstants::CLOSE_PAREN;
        else
          leftover = StringConstants::CLOSE_PAREN + leftover;
        // FALL THROUGH!
      case Cond::NO_TOKEN: // done
        if (lhs != Cond::NULL_STRING)
        {
          if (lhs != StringConstants::EMPTY_STRING)
            temp_truth = valueCompare( lhs, Cond::NOT_EQUAL,
                Cond::ZERO_STRING, true );
          else
            temp_truth = false;
          if (notted)
            temp_truth = !temp_truth;
          truth = changeTruth( truth, temp_truth, determined,
              boolean_op );
        }
        return (truth);
      case Cond::UNARY_OP_TOKEN: // for now, we know this is "!"
        notted = !notted;
        valid_tokens = Cond::UNARY_OP_TOKEN | Cond::VALUE_TOKEN |
            Cond::FUNCTION_TOKEN | Cond::OPEN_PAREN_TOKEN;
        break;
      case Cond::BINARY_OP_TOKEN:
        binary_op = tokens[tokens.firstIndex()];
        valid_tokens = Cond::VALUE_TOKEN;
        break;
      case Cond::BOOLEAN_OP_TOKEN:
        if (lhs != Cond::NULL_STRING)
        {
          if (lhs != StringConstants::EMPTY_STRING)
            temp_truth = valueCompare( lhs, Cond::NOT_EQUAL,
                Cond::ZERO_STRING, true );
          else
            temp_truth = false;
          if (notted)
            temp_truth = !temp_truth;
          truth = changeTruth( truth, temp_truth, determined,
              boolean_op );
        }
        if (tokens[tokens.firstIndex()].equals( Cond::BOOLEAN_OR ))
        {
          boolean_op = StringConstants::EMPTY_STRING; // tricky, eh?
          determined |= truth;
        }
        else
          boolean_op = tokens[tokens.firstIndex()];
        valid_tokens = Cond::VALUE_TOKEN | Cond::FUNCTION_TOKEN |
            Cond::UNARY_OP_TOKEN | Cond::OPEN_PAREN_TOKEN;
        lhs = Cond::NULL_STRING;
        notted = false;
        break;
      case Cond::VALUE_TOKEN:
        if (binary_op == StringConstants::EMPTY_STRING)
        {
          lhs = tokens[tokens.firstIndex()];
          valid_tokens = Cond::BINARY_OP_TOKEN | Cond::BOOLEAN_OP_TOKEN |
              Cond::CLOSE_PAREN_TOKEN | Cond::NO_TOKEN;
        }
        else
        {
          temp_truth = valueCompare( lhs, binary_op,
              tokens[tokens.firstIndex()] );
          if (notted)
            temp_truth = !temp_truth;
          binary_op = StringConstants::EMPTY_STRING;
          lhs = Cond::NULL_STRING;
          valid_tokens = Cond::BOOLEAN_OP_TOKEN | Cond::CLOSE_PAREN_TOKEN |
              Cond::NO_TOKEN;
          truth = changeTruth( truth, temp_truth, determined,
              boolean_op );
          boolean_op = StringConstants::EMPTY_STRING;
        }
        break;
      case Cond::FUNCTION_TOKEN:
        temp_truth = evaluateFunction( tokens, make_info, var_eval );
        leftover = tokens[tokens.firstIndex() + 1];
        if (notted)
          temp_truth = !temp_truth;
        truth = changeTruth( truth, temp_truth, determined,
            boolean_op );
        binary_op = StringConstants::EMPTY_STRING;
        lhs = Cond::NULL_STRING;
        boolean_op = StringConstants::EMPTY_STRING;
        notted = false;
        valid_tokens = Cond::BOOLEAN_OP_TOKEN | Cond::CLOSE_PAREN_TOKEN |
            Cond::NO_TOKEN;
        break;
      default:
        throw Exception( (String( "internal error - unknown token(" ) +
            token_type + "): " + tokens[ARRAY_FIRST_INDEX]).toCharPtr() );
    }
  } while (token_type != Cond::NO_TOKEN);
  return (truth);
}

boolean Cond::evalIfFunc( const String &function_name, const String &exp,
    const MakeInfoReportable *make_info, String &leftover,
    const Variable *var_eval )
{
  String boolean_op;
  boolean determined = false, truth = false, temp_truth, notted = false;
  int token_type;
  int valid_tokens = Cond::UNARY_OP_TOKEN | Cond::FUNCTION_TOKEN |
      Cond::VALUE_TOKEN | Cond::OPEN_PAREN_TOKEN;
  StringArray tokens( 2, 2 ), args( 2, 2 );

  leftover = exp;
  do
  {
    token_type = tokenize( leftover, var_eval, tokens, Cond::NO_FUNC );
//    cout << "token_type:" << token_type <<
//        ", tokens[1]:" << tokens[tokens.firstIndex()] <<
//        ", tokens[2]:" << tokens[tokens.firstIndex() + 1] << endl;
    leftover = tokens[tokens.firstIndex() + 1];
    if ((token_type & valid_tokens) == 0)
      throw Exception( (String( "invalid token: " ) +
          tokens[ARRAY_FIRST_INDEX]).toCharPtr() );
    switch (token_type)
    {
      case Cond::OPEN_PAREN_TOKEN:
        temp_truth = evalIfFunc( function_name, leftover,
            make_info, leftover, var_eval );
        if (leftover.charAt( STRING_FIRST_INDEX ) != ')')
          throw Exception( "missing ')'" );
        else
          leftover.remove( STRING_FIRST_INDEX, 1 );
        if (notted)
        {
          temp_truth = !temp_truth;
          notted = false;
        }
        truth = changeTruth( truth, temp_truth, determined,
            boolean_op );
        valid_tokens = Cond::BOOLEAN_OP_TOKEN | Cond::NO_TOKEN |
            Cond::CLOSE_PAREN_TOKEN;
        break;
      case Cond::CLOSE_PAREN_TOKEN: // done, but pass back the token to verify
        if (leftover == Cond::NULL_STRING)
          leftover = StringConstants::CLOSE_PAREN;
        else
          leftover = StringConstants::CLOSE_PAREN + leftover;
        // FALL THROUGH!
      case Cond::NO_TOKEN: // done
        return (truth);
      case Cond::UNARY_OP_TOKEN: // for now, we know this is "!"
        notted = !notted;
        valid_tokens = Cond::UNARY_OP_TOKEN | Cond::VALUE_TOKEN |
            Cond::FUNCTION_TOKEN | Cond::OPEN_PAREN_TOKEN;
        break;
      case Cond::BOOLEAN_OP_TOKEN:
        if (tokens[tokens.firstIndex()].equals( Cond::BOOLEAN_OR ))
        {
          boolean_op = StringConstants::EMPTY_STRING; // tricky, eh?
          determined |= truth;
        }
        else
          boolean_op = tokens[tokens.firstIndex()];
        valid_tokens = Cond::VALUE_TOKEN | Cond::FUNCTION_TOKEN |
            Cond::UNARY_OP_TOKEN | Cond::OPEN_PAREN_TOKEN;
        notted = false;
        break;
      case Cond::VALUE_TOKEN:
      case Cond::FUNCTION_TOKEN:
        args[args.firstIndex()] = function_name;
        args[args.firstIndex() + 1] = StringConstants::OPEN_PAREN +
            tokens[tokens.firstIndex()] + StringConstants::CLOSE_PAREN;
        temp_truth = evaluateFunction( args, make_info, var_eval );
        if (notted)
          temp_truth = !temp_truth;
        truth = changeTruth( truth, temp_truth, determined,
            boolean_op );
        boolean_op = StringConstants::EMPTY_STRING;
        notted = false;
        valid_tokens = Cond::BOOLEAN_OP_TOKEN | Cond::CLOSE_PAREN_TOKEN |
            Cond::NO_TOKEN;
        break;
      default:
        throw Exception( (String( "internal error - unknown token(" ) +
            token_type + "): " + tokens[ARRAY_FIRST_INDEX]).toCharPtr() );
    }
  } while (token_type != Cond::NO_TOKEN);
  return (truth);
}

boolean Cond::evaluateFunction( StringArray &args,
    const MakeInfoReportable *make_info, const Variable *var_eval )
{
  String function_name = args[args.firstIndex()], function_arg;

  if (tokenize( args[args.firstIndex() + 1], var_eval,
      args, Cond::NO_FUNC ) == Cond::OPEN_PAREN_TOKEN)
  {
    // Trick!  Since we need to evaluate the variable inside empty's
    // parens as a variable, we prepend the value with $( so that
    // tokenize will evaluate it for us.  Later we have to make sure
    // NOT to look for empty's closing paren, since the evaluation
    // in tokenize will have removed it.
    if (function_name.equals( Cond::EMPTY_FUNCNAME ))
      args[args.firstIndex() + 1].prepend( "$(" );

    if (tokenize( args[args.firstIndex() + 1],
        var_eval, args, Cond::NO_FUNC ) == Cond::VALUE_TOKEN)
    {
      function_arg = args[args.firstIndex()];

      // the closing paren for "empty" is taken off automatically
      // during the previous tokenize, so don't tokenize again for
      // that function here.
      if (function_name.equals( Cond::EMPTY_FUNCNAME ) ||
          tokenize( args[args.firstIndex() + 1], var_eval,
          args, Cond::NO_FUNC ) == Cond::CLOSE_PAREN_TOKEN)
      {
        if (function_name.equals( Cond::DEFINED_FUNCNAME ))
          return (evaluateIfDefined( function_arg, var_eval ));
        else if (function_name.equals( Cond::UNDEFINED_FUNCNAME ))
          return (!evaluateIfDefined( function_arg, var_eval ));
        else if (function_name.equals( Cond::EMPTY_FUNCNAME ))
          return (evaluateIfEmpty( function_arg, make_info ));
        else if (function_name.equals( Cond::EXISTS_FUNCNAME ))
          return (evaluateIfExists( function_arg, make_info ));
        else if (function_name.equals( Cond::MAKE_FUNCNAME ))
          return (evaluateIfMake( function_arg, make_info ));
        else if (function_name.equals( Cond::NMAKE_FUNCNAME ))
          return (!evaluateIfMake( function_arg, make_info ));
        else if (function_name.equals( Cond::TARGET_FUNCNAME ))
          return (evaluateIfTarget( function_arg, make_info ));
      }
    }
  }
  throw Exception( (String( "function '" ) + function_name +
      "' is invalid").toCharPtr() );
}

boolean Cond::evaluateIfDefined( const String &arg, const Variable *var_eval )
{
  if (var_eval == 0)
  {
    Variable tmp_eval( Env::getSetVars(), false, '\0' );
    return (tmp_eval.getVars()->find( arg ) != 0);
  }
  else
    return (var_eval->getVars()->find( arg ) != 0);
}

boolean Cond::evaluateIfEmpty( const String &arg,
    const MakeInfoReportable *make_info )
{
  return (arg.trim().length() < 1);
}

boolean Cond::evaluateIfExists( const String &arg,
    const MakeInfoReportable *make_info )
{
  if (make_info == 0)
    return (false);
  else
    return (make_info->exists( arg ));
}

boolean Cond::evaluateIfMake( const String &arg,
    const MakeInfoReportable *make_info )
{
  if (make_info == 0)
    return (false);
  else
    return (make_info->isMainTarget( arg ));
}

boolean Cond::evaluateIfTarget( const String &arg,
    const MakeInfoReportable *make_info )
{
  if (make_info == 0)
    return (false);
  else
    return (make_info->isTarget( arg ));
}

boolean Cond::changeTruth( boolean old_truth, boolean new_truth,
    boolean do_not_change, const String &boolean_op )
{
  if (do_not_change)
    return (old_truth);
  else if (boolean_op == StringConstants::EMPTY_STRING)
    return (new_truth);
  else if (boolean_op.equals( Cond::BOOLEAN_AND ))
    return (old_truth && new_truth);
  else // "||"
    return (old_truth || new_truth);
}

long Cond::convertToInt( const String &value, String &leftover )
{
  char *ptr;
  long result = 0;
  leftover = StringConstants::EMPTY_STRING;

  if (value.length() > 0)
  {
    result = strtol( value.toCharPtr(), &ptr, 0 );
    if (*ptr == '\0')
      leftover = Cond::NULL_STRING; // success
    else
      leftover = String( ptr );
  }

  return (result);
}

/**
 * NOTE: ints_only is currently ignored.
**/
boolean Cond::valueCompare( const String &lhs, const String &binary_op,
    const String &rhs, boolean ints_only )
{
  boolean intcmp = true;
  long lhs_int = 0, rhs_int = 0;
  String lhs_leftover, rhs_leftover;

  lhs_int = convertToInt( lhs, lhs_leftover );
  rhs_int = convertToInt( rhs, rhs_leftover );
  if (lhs_leftover != Cond::NULL_STRING ||
      rhs_leftover != Cond::NULL_STRING)
    intcmp = false;

  if (binary_op.equals( Cond::NOT_EQUAL ))
  {
    if (intcmp)
      return (lhs_int != rhs_int);
    else
      return (!lhs.dequote( true ).equals( rhs.dequote( true ) ));
  }
  else if (binary_op.equals( Cond::EQUAL ))
  {
    if (intcmp)
      return (lhs_int == rhs_int);
    else
      return (lhs.dequote( true ).equals( rhs.dequote( true ) ));
  }
  else if (!intcmp) // the rest of these require intcmp to be true
    return (false);
  else if (binary_op.equals( Cond::LESS_THAN ))
    return (lhs_int < rhs_int);
  else if (binary_op.equals( Cond::LESS_THAN_OR_EQUAL ))
    return (lhs_int <= rhs_int);
  else if (binary_op.equals( Cond::GREATER_THAN ))
    return (lhs_int > rhs_int);
  else if (binary_op.equals( Cond::GREATER_THAN_OR_EQUAL ))
    return (lhs_int >= rhs_int);

  throw Exception( (String( "invalid operator: " ) + binary_op).toCharPtr() );
}

String Cond::wrapPreprocVars( const String &line )
{
  Variable var( 0, false, '\0' );
  char *ptr = line.toCharPtr(), quoting = '\0';
  String result;
  StringArray arr;

  while (*ptr != '\0')
  {
    if (quoting == '\0' && (isalpha( *ptr ) || *ptr == '_'))
    {
      String var_name( *(ptr++) );
      while (isalpha( *ptr ) || isdigit( *ptr ) || *ptr == '_')
        var_name += *(ptr++);
      if (var_name == Cond::DEFINED_FUNCNAME) // wrap its argument instead
      {
        result += var_name;
        while (*ptr == ' ' || *ptr == '\t')
         result += *(ptr++);
        result += '(';
        if (*ptr == '(')
          ++ptr;
        while (*ptr == ' ' || *ptr == '\t')
         result += *(ptr++);
        while (isalpha( *ptr ) || isdigit( *ptr ) || *ptr == '_')
          result += *(ptr++);
        while (*ptr == ' ' || *ptr == '\t')
         result += *(ptr++);
        result += ')';
        if (*ptr == ')')
          ++ptr;
      }
      else
      {
        result += StringConstants::DOLLAR_SIGN;
        result += '{';
        result += var_name;
        result += '}';
      }
      continue;
    }
    else if (*ptr == '\"')
    {
      if (quoting == '\"')
        quoting = '\0';
      else if (quoting == '\0')
        quoting = *ptr;
    }
    else if (*ptr == '\'')
    {
      if (quoting == '\'')
        quoting = '\0';
      else if (quoting == '\0')
        quoting = *ptr;
    }
    else if (*ptr == '\\')
    {
      // don't remove the backslash at this point, just escape next char
      if (*(ptr+1) != '\0')
        result += *(ptr++);
    }

    result += *ptr;
    ++ptr;
  }

  return (result);
}
