bigbrother
==========

Example

import com.bigbrother.core;

public class Application {
  
  // a class that implements an interface and defines how to do logging.
  public static class MyLogger implements Logger {
    @Override
    public boolean enabled() {
      return true;
    }
    
    @Override
    public void log(Object value) {
      // define behaviour
      System.out.println(value);
    }
  }
  
  @Timer(value=MyLogger.class)
  public Event findEventInDb(long eid) {
    return db.findEventById(eid);    
  }
  
  public static void main(String[] args) {
    Application app = ...;
    Event event = app.findEventById(100000);
  }
}
