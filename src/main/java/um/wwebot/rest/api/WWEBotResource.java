package um.wwebot.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import um.wwebot.model.IncomingMessage;
import um.wwebot.model.OutgoingMessage;
import um.wwebot.rest.client.TelegramClient;

@Slf4j
@Path("/wwebot")
public class WWEBotResource {
	
	@Autowired
	private TelegramClient client;

	public WWEBotResource(){
		log.info("WWEBot resource started");     
	}

	@POST
	@Path("/update/434ef3c8-fc00-4b70-945c-c2c756d9101a")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUpdate(String update) {
		log.debug("Received update:\n {}", update) ;
		
		String textPath = "$.message.text";
		String chatIdPath = "$.message.chat.id";
		String firstNamePath = "$.message.chat.first_name";
		String lastNamePath = "$.message.chat.last_name";
		
		try {
			/*Jackson?*/
			DocumentContext jsonContext = JsonPath.parse(update);
			String text = jsonContext.read(textPath);
			String chatId = jsonContext.read(chatIdPath)+"";
			String firstName = jsonContext.read(firstNamePath);
			String lastName = jsonContext.read(lastNamePath);
			IncomingMessage incomingMessage = new IncomingMessage(text, chatId, firstName, lastName);
			log.info("Received message: {}", incomingMessage);
			/**/
			
			OutgoingMessage responseToSend = processRequest(incomingMessage);
			
			client.sendMessage(responseToSend);
			
			return Response.ok().build();
		}
		catch(RuntimeException e) {
			log.error("Error processing message", e);
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	private OutgoingMessage processRequest(IncomingMessage incomingMessage) {
		// TODO: Manage user commands
		OutgoingMessage om = new OutgoingMessage();
		om.setChat_id(incomingMessage.getChatId());
		om.setText("You asked: "+incomingMessage.getText());
		return om;
	}
}