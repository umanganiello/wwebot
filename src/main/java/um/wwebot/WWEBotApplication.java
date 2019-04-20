package um.wwebot;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import um.wwebot.rest.api.HealthCheckResource;
import um.wwebot.rest.api.WWEBotResource;

@SpringBootApplication
public class WWEBotApplication {

	@Bean
	public ResourceConfig resourceConfig() {
	      return new ResourceConfig()
	    		  .register(WWEBotResource.class)
	    		  .register(HealthCheckResource.class);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(WWEBotApplication.class, args);
	}

}
