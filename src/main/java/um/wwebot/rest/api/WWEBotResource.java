package um.wwebot.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Path("/wwebot")
public class WWEBotResource {

	public WWEBotResource(){
		log.info("WWEBot resource started");     
	}

//  @GET
//  @Path("/{name}")
//  public Response getMessage(@PathParam("name") String name) {
//      return Response.ok().entity("Hello "+name).build();
//  }
	
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
			String name = jsonContext.read(firstNamePath) + " " + jsonContext.read(lastNamePath);
			
			log.info("Received message \"{}\" from {} ({})", text, chatId, name);
			return Response.ok().build();
		}
		catch(RuntimeException e) {
			log.error("Error when parsing input message", e);
			return Response.serverError().entity(e.getMessage()).build();
		}
	}
}