Taskmin offers a simple way to schedule tasks that run at some later time.
Currently it only supports one-off tasks but may support recurring tasks in
the future.

#### Usage
Here is a simple program demonstrating basic usage.
```java
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Example {
    public static void main(String[] args) throws InterruptedException {
        // Create the TaskManager. You probably just need one of these.
        TaskManager taskManager = TaskManager.createBasicTaskManager();
        
        // Add a simple task that prints out "Hello world!" after 1 second.
        taskManager.addTask(Task.builder(() -> System.out.println("Hello world!"))
                .executeAt(LocalDateTime.now().plus(1, ChronoUnit.SECONDS)));
        
        //  Wait 2 seconds before exiting (just to make sure you see the message!)
        Thread.sleep(2000);
        System.out.println("Done!");
    }
}
```

#### Building
Simply run `gradlew build` to build the library.

#### Maven Dependency Information
Coming soon.