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
import um.wwebot.model.Event;
import um.wwebot.model.IncomingMessage;
import um.wwebot.model.Match;
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
	
	private final List<Pair<String, String>> showsSections; //Wikipedia page section ids for each roster table

	private final String WIKIPEDIA_PAGE_CHAMPIONS = "List_of_current_champions_in_WWE";
	private final String WIKIPEDIA_PAGE_EVENTS = "List_of_WWE_pay-per-view_and_WWE_Network_events";
	private final String WIKIPEDIA_SECTION_NR_UPCOMING_EVENTS = "41";
	private final String WIKIPEDIA_SECTION_NR_NEXT_EVENT_MATCHES = "4";
	private final String WIKIPEDIA_SECTION_NR_NEXT_EVENT_MATCHES_ALT = "2";
	
	public WWEBotResource(){
		showsSections = new LinkedList<>();
		showsSections.add(Pair.of("RAW", "2"));
		showsSections.add(Pair.of("SmackDown", "3"));
		showsSections.add(Pair.of("205 Live", "4"));
		showsSections.add(Pair.of("NXT", "5"));
		showsSections.add(Pair.of("NXT UK", "6"));
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
			log.error("Error processing update: {} \n Error:\n", update, e);
			return Response.ok().build();
//			return Response.serverError().entity(e.getMessage()).build();
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
		
		StringBuilder sb;
		String upcomingEventsHTML;
		
		switch(cmd) {
		case CHAMPIONS:
			log.info("Recognized command {}", WWEBotCommand.CHAMPIONS.getCmdPath());
			sb = new StringBuilder();
			
			//TODO: Optimization: single API call
			showsSections.forEach(s -> {
				String sectionHTML = wikipediaClient.getSection(WIKIPEDIA_PAGE_CHAMPIONS, s.getRight());
				List<Champion> champions = parser.getChampionsFromSection(sectionHTML, s.getLeft());
				
				sb.append("\n*")
				.append(s.getLeft())
				.append("*\n")
				.append(champions.stream().map(Champion::toString).collect(Collectors.joining("\n")))
				.append("\n");
			});
			
			response.setText(sb.toString());
			break;
		case NEXT_PPV:
			log.info("Recognized command {}", WWEBotCommand.NEXT_PPV.getCmdPath());
			sb = new StringBuilder();
			
			upcomingEventsHTML = wikipediaClient.getSection(WIKIPEDIA_PAGE_EVENTS, WIKIPEDIA_SECTION_NR_UPCOMING_EVENTS);
			List<Event> nextEvents = parser.getNextEventsFromSection(upcomingEventsHTML, 1);
			
			nextEvents.forEach(e -> {
				sb.append("\n")
				.append(e.toBotString())
				.append(" ")
				.append(WWEBotCommand.NEXT_PPV_CARD.getCmdPath());
			});
			
			response.setText(sb.toString());
			break;
		case NEXT_PPV_CARD:
			log.info("Recognized command {}", WWEBotCommand.NEXT_PPV_CARD.getCmdPath());
			upcomingEventsHTML = wikipediaClient.getSection(WIKIPEDIA_PAGE_EVENTS, WIKIPEDIA_SECTION_NR_UPCOMING_EVENTS);
			Event nextEvent = parser.getNextEventsFromSection(upcomingEventsHTML, 1).get(0);
			
			String nextEventMatchesHTML;
			try {
				nextEventMatchesHTML = wikipediaClient.getSection(nextEvent.getPageUrl(), WIKIPEDIA_SECTION_NR_NEXT_EVENT_MATCHES);
			}
			catch(Exception e) {
				log.info("NEXT_PPV_CARD section not found with id={}, trying with alternative id={}", WIKIPEDIA_SECTION_NR_NEXT_EVENT_MATCHES, WIKIPEDIA_SECTION_NR_NEXT_EVENT_MATCHES_ALT);
				nextEventMatchesHTML = wikipediaClient.getSection(nextEvent.getPageUrl(), WIKIPEDIA_SECTION_NR_NEXT_EVENT_MATCHES_ALT);
			}
			
			List<Match> nextEventMatches = parser.getMatchesFromEventSection(nextEventMatchesHTML);
			
			sb = new StringBuilder();
			
			nextEventMatches.forEach(m -> {
				sb.append("\n")
				.append(m.toBotString());
			});
			
			response.setText(sb.toString());
			break;
		default:
			response.setText("Command not supported yet."); //TODO
			break;
		}
		return response;
	}
}