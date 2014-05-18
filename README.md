bigbrother
==========

public class MyLogger implements com.bigbrother.core.Logger {
  @Override
  public boolean enabled() {
    return true;
  }
  
  @Override
  public void log(Object value) {
    System.out.println(value);
  }
}
