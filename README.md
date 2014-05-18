bigbrother
==========

A class that implements an interface Logger and define how to do logging


```sh
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
```

A Execution something that can take a long time


```sh
@com.bigbrother.core.annotation.Timer(value=MyLogger.class)
public Event findEventInDb(long id) {
  ....
  return event;
}
```



