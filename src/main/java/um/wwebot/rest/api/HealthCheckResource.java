package um.wwebot.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Path("/healthcheck")
public class HealthCheckResource {

	public HealthCheckResource(){
     log.info("HealthCheck resource started");     
  }

  @GET
  public Response healthCheck() {
      return Response.ok().build();
  }
}