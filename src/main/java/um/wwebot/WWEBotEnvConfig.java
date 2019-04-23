package um.wwebot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WWEBotEnvConfig {

	@Bean
	public String getBotToken() {
		String botToken = System.getenv("BOT_TOKEN");
		
		if(botToken == null) {
			log.error("BOT_TOKEN is not set!");
			throw new IllegalArgumentException("BOT_TOKEN is not set!");
		}
		else {
			log.info("BOT_TOKEN found");
			return botToken;
		}
	}	
}
