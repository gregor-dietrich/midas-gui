package de.vptr.midas.gui.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.vptr.midas.gui.dto.UserDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserClient {

    @GET
    List<UserDto> getAllUsers(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/me")
    Response getCurrentUser(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/{id}")
    Response getUser(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/username/{username}")
    Response getUserByUsername(@PathParam("username") String username,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/email/{email}")
    Response getUserByEmail(@PathParam("email") String email, @HeaderParam("Authorization") String authorization);

    @POST
    Response createUser(UserDto user, @HeaderParam("Authorization") String authorization);

    @PUT
    @Path("/{id}")
    Response updateUser(@PathParam("id") Long id, UserDto user, @HeaderParam("Authorization") String authorization);

    @PATCH
    @Path("/{id}")
    Response patchUser(@PathParam("id") Long id, UserDto user, @HeaderParam("Authorization") String authorization);

    @DELETE
    @Path("/{id}")
    Response deleteUser(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);
}
