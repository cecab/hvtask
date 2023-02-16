package hvtask;

import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

@Singleton
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

}