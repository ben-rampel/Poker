package webapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import poker.TableImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Configuration
@EnableAsync
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/socket")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app")
                .enableSimpleBroker("/poker");
    }

    @Bean
    public TableController getTableController() {
        return new TableController(new TableImpl());
    }

    @Bean public String[] databaseParameters() {
        String[] params = new String[3];
        try {
            Scanner in = new Scanner(new File("C:\\Users\\brampel\\Documents\\pokerdb-conf.txt"));
            for(int i = 0; i < 3; i++){
                params[i] = in.nextLine();
            }
            return params;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Database parameters file not found.");
        }
    }
}