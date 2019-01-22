/**
 * Exception
 *
**/
#ifndef _ODE_LIB_EXCEPTN_EXCEPTN_HPP_
#define _ODE_LIB_EXCEPTN_EXCEPTN_HPP_


/**
 * Base exception type for all ODE exceptions.
 *
**/
class Exception
{
  public:

    Exception();
    Exception( const Exception &copy );
    Exception( const char *msg, unsigned long err = 0 );
    virtual ~Exception();
        
    inline const char *getMessage() const; // get error text
    inline const char *text() const; // same as getMessage()
    inline unsigned long errorId() const; // get error code
    void setText( const char *msg ); // set the error text
    inline void setError( unsigned long errcode ); // set the error code


  private:

    char *errtext;
    unsigned long errcode;

    void setMessage( const char *msg, unsigned long len );
};

inline const char *Exception::getMessage() const
{
  return (errtext);
}

inline const char *Exception::text() const
{
  return (errtext);
}

inline unsigned long Exception::errorId() const
{
  return (errcode);
}

inline void Exception::setError( unsigned long errcode )
{
  this->errcode = errcode;
}

#endif /* _ODE_LIB_EXCEPTN_EXCEPTN_HPP_ */
