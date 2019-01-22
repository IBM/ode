package nls;
import java.util.*;

public class Hello {

  public static void main( String args[] )
  { 
    ResourceBundle rb = ResourceBundle.getBundle( "nls.Hello" );
    System.out.println( rb.getString( "hello" ) );
  }
};
