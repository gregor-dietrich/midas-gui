package de.vptr.midas.gui.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.vptr.midas.gui.dto.PageDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/pages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PageClient {

    @GET
    List<PageDto> getAllPages(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/{id}")
    Response getPage(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/search/title")
    List<PageDto> searchByTitle(@QueryParam("q") String title, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/search/content")
    List<PageDto> searchContent(@QueryParam("q") String searchTerm, @HeaderParam("Authorization") String authorization);

    @POST
    Response createPage(PageDto page, @HeaderParam("Authorization") String authorization);

    @PUT
    @Path("/{id}")
    Response updatePage(@PathParam("id") Long id, PageDto page, @HeaderParam("Authorization") String authorization);

    @PATCH
    @Path("/{id}")
    Response patchPage(@PathParam("id") Long id, PageDto page, @HeaderParam("Authorization") String authorization);

    @DELETE
    @Path("/{id}")
    Response deletePage(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);
}
