package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Application {

    /*
     * main method for web app
     * currently to test this you must
         * spring-boot:run in maven
         * localhost:8080/startGame
         * localhost:8080/game?player=John (or a name of a player in the game)
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}