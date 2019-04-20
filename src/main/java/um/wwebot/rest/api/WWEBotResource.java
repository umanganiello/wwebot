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
		log.info("Received update:\n {}", update) ;
		
		String jsonPath = "$.address.street";
		DocumentContext jsonContext = JsonPath.parse(update);
		String street = jsonContext.read(jsonPath);
		log.info("Street {}", street);
		
		return Response.ok().build();
	}
}