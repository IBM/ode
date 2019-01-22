/**
 * CondEvaluator
 *
**/

#define _ODE_LIB_UTIL_CONDEVAL_CPP_
#include "lib/util/condeval.hpp"

/**
 * Private version of parseIf which does the work.
 *
**/
boolean CondEvaluator::parseIf( int if_type, const String &line )
{
  // if statements are always legal, so just increase our depth
  depth++;

  // initialize the Bitsets, since they are often reused and may
  // already be set from previous calls.
  truth.clear( depth );
  seen_true.clear( depth );
  seen_else.clear( depth );
  cond_considered = false;

  // if this is the first block, always use the return value of Cond.
  // otherwise, only call Cond if the previous block we were in was
  // a "true" (parsing ON) block.
  if (depth == 1 || truth.get( depth - 1 ))
  {
    try
    {
      if (parseCond( if_type, line ))
      {
        truth.set( depth );
        seen_true.set( depth );
      }
    }
    catch (ParseException &e)
    {
      throwParseException( e ); // rethrow
    }
    cond_considered = true;
  }

  return (truth.get( depth ));
}

/**
 * Private version of parseElif which does the work.
 *
**/
boolean CondEvaluator::parseElif( int if_type, const String &line )
{
  cond_considered = false;

  // if an "else" statement has already been seen, elif is invalid
  if (allBlocksClosed() || seen_else.get( depth ))
    throwParseException();

  // can only have one true block per level, so enforce a false truth
  // value if we've already seen one.
  if (seen_true.get( depth ))
    truth.clear( depth );
  else if (depth == 1 || truth.get( depth - 1 ))
  {
    try
    {
      if (parseCond( if_type, line ))
      {
        truth.set( depth );
        seen_true.set( depth );
      }
    }
    catch (ParseException &e)
    {
      throwParseException( e ); // rethrow
    }
    cond_considered = true;
  }

  return (truth.get( depth ));
}

boolean CondEvaluator::parseCond( int if_type, const String &line )
{
  boolean result;

  switch (if_type)
  {
    case IF_TYPE:
      result = Cond::evalIfExpr( line, make_info, file_info, var_eval,
          valid_functions );
      break;
    case IFDEF_TYPE:
      result = Cond::evalIfDefExpr( line, make_info, file_info, var_eval );
      break;
    case IFNDEF_TYPE:
      result = Cond::evalIfNDefExpr( line, make_info, file_info, var_eval );
      break;
    case IFMAKE_TYPE:
      result = Cond::evalIfMakeExpr( line, make_info, file_info, var_eval );
      break;
    case IFNMAKE_TYPE:
      result = Cond::evalIfNMakeExpr( line, make_info, file_info, var_eval );
      break;
    default:
      result = false;
      break;
  }

  return (result);
}

boolean CondEvaluator::parseIf( const String &line, boolean wrap_vars )
{
  if (wrap_vars)
    return (parseIf( IF_TYPE, Cond::wrapPreprocVars( line ) ));
  else
    return (parseIf( IF_TYPE, line ));
}

boolean CondEvaluator::parseElse()
{
  cond_considered = false;
  // only one else per block is allowed
  if (allBlocksClosed() || seen_else.get( depth ))
    throwParseException();

  // remember that we've seen an else statement
  seen_else.set( depth );

  // can only have one true block per level, so enforce a false truth
  // value if we've already seen one.
  if (seen_true.get( depth ))
    truth.clear( depth );
  else if (depth == 1 || truth.get( depth - 1 ))
  {
    // so this must be the first true block...
    truth.set( depth );
    seen_true.set( depth );
    cond_considered = true;
  }

  return (truth.get( depth ));
}

boolean CondEvaluator::parseElif( const String &line, boolean wrap_vars )
{
  if (wrap_vars)
    return (parseElif( IF_TYPE, Cond::wrapPreprocVars( line ) ));
  else
    return (parseElif( IF_TYPE, line ));
}

boolean CondEvaluator::parseEndif()
{
  // endif is invalid if all blocks are closed (more endif's than if's)
  if (allBlocksClosed())
    throwParseException();

  // close the block
  depth--;


  cond_considered = true;
  // return the correct truth value
  if (depth <= 0)
    return (true); // we've closed all blocks, so always return true
  else
    return (truth.get( depth )); // current truth value of this block
}

void CondEvaluator::throwParseException()
{
  if (file_info == 0)
  {
    ParseException e( "Malformed conditional" );
    throwParseException( e );
  }
  else
  {
    ParseException e( file_info->getPathname(),
        file_info->getLineNumber(), "Malformed conditional" );
    throwParseException( e );
  }
}

void CondEvaluator::throwParseException( ParseException &e )
{
  // trick! if we're supposed to interpret exceptions as "extra"
  // true blocks, then don't say we've seen true yet so that other
  // blocks will continue to be evaluated normally.
  if (true_on_exception)
    truth.set( depth );

  throw e;
}
