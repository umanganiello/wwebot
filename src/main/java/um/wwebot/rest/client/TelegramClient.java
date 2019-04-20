package um.wwebot.rest.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import um.wwebot.model.OutgoingMessage;

@Slf4j
@Component
public class TelegramClient {
	
	@Autowired
	private String botToken;
	
	public void sendMessage(OutgoingMessage msgToSend) {
		log.info("Sending message to {}", msgToSend.getChat_id());
		RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForLocation("https://api.telegram.org/bot"+botToken+"/sendMessage", msgToSend);
        log.info("Message sent to {}", msgToSend.getChat_id());
	}
}
