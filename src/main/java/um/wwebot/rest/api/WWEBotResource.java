package um.wwebot.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Path("/wwebot")
public class WWEBotResource {

	public WWEBotResource(){
     log.info("WWEBot resource started");     
  }

  @GET
  @Path("/{name}")
  public Response getMessage(@PathParam("name") String name) {
      return Response.ok().entity("Ciao "+name).build();
  }
}