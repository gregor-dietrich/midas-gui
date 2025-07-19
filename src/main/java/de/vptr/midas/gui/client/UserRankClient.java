package de.vptr.midas.gui.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.vptr.midas.gui.dto.UserRankDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/user-ranks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserRankClient {

    @GET
    List<UserRankDto> getAllRanks(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/{id}")
    Response getRank(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/name/{name}")
    Response getRankByName(@PathParam("name") String name, @HeaderParam("Authorization") String authorization);

    @POST
    Response createRank(UserRankDto rank, @HeaderParam("Authorization") String authorization);

    @PUT
    @Path("/{id}")
    Response updateRank(@PathParam("id") Long id, UserRankDto rank, @HeaderParam("Authorization") String authorization);

    @PATCH
    @Path("/{id}")
    Response patchRank(@PathParam("id") Long id, UserRankDto rank, @HeaderParam("Authorization") String authorization);

    @DELETE
    @Path("/{id}")
    Response deleteRank(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);
}
