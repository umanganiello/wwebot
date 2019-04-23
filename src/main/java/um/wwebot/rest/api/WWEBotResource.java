package um.wwebot.rest.api;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import um.wwebot.model.Champion;
import um.wwebot.model.IncomingMessage;
import um.wwebot.model.OutgoingMessage;
import um.wwebot.model.WWEBotCommand;
import um.wwebot.parser.WWEBotParser;
import um.wwebot.rest.client.TelegramClient;
import um.wwebot.rest.client.WikipediaClient;

@Slf4j
@Path("/wwebot")
public class WWEBotResource {
	
	@Autowired
	private TelegramClient telegramClient;

	@Autowired
	private WikipediaClient wikipediaClient;
	
	@Autowired
	private WWEBotParser parser;
	
	private final List<Pair<String, String>> sections; //Wikipedia page section ids for each roster table

	private final String championsWikipediaPage = "List_of_current_champions_in_WWE";

	public WWEBotResource(){
		sections = new LinkedList<>();
		sections.add(Pair.of("RAW", "2"));
		sections.add(Pair.of("SmackDown", "3"));
		sections.add(Pair.of("205 Live", "4"));
		sections.add(Pair.of("NXT", "5"));
		sections.add(Pair.of("NXT UK", "6"));
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
			DocumentContext jsonContext = JsonPath.parse(update);
			String text = jsonContext.read(textPath);
			String chatId = jsonContext.read(chatIdPath)+"";
			String firstName = jsonContext.read(firstNamePath);
			String lastName = jsonContext.read(lastNamePath);
			IncomingMessage incomingMessage = new IncomingMessage(text, chatId, firstName, lastName);
			log.info("Received message: {}", incomingMessage);
			
			OutgoingMessage responseToSend = processRequest(incomingMessage);
			
			telegramClient.sendMessage(responseToSend);
			
			return Response.ok().build();
		}
		catch(RuntimeException e) {
			log.error("Error processing message", e);
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	private OutgoingMessage processRequest(IncomingMessage incomingMessage) {
		OutgoingMessage response = new OutgoingMessage();
		response.setChat_id(incomingMessage.getChatId());
		
		WWEBotCommand cmd = WWEBotCommand.lookup(incomingMessage.getText());
		
		if(cmd == null) {
			response.setText("WHAT? Command not recognized.");
			return response;
		}
		
		switch(cmd) {
		case CHAMPIONS:
			log.info("Recognized command {}", WWEBotCommand.CHAMPIONS.getCmdPath());
			StringBuilder sb = new StringBuilder();
			
			//TODO: Optimization: single API call
			sections.forEach(s -> {
				sb.append("\n");
				sb.append(s.getLeft());
				sb.append("\n");
				String sectionHTML = wikipediaClient.getSection(championsWikipediaPage, s.getRight());
				List<Champion> champions = parser.getChampionsFromSection(sectionHTML, s.getLeft());
				sb.append(champions.stream().map(Champion::toString).collect(Collectors.joining("\n")));
				sb.append("\n");
			});
			
			response.setText(sb.toString());
			break;
		case NEXT_PPV:
			response.setText("Command not supported yet."); //TODO
			break;
		case TITLE_HOLDERS:
			response.setText("Command not supported yet."); //TODO
			break;
		}
		return response;
	}
}