package de.vptr.midas.gui.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.vptr.midas.gui.dto.UserDto;
import de.vptr.midas.gui.dto.UserGroupDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/user-groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserGroupClient {

    @GET
    List<UserGroupDto> getAllGroups(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/{id}")
    Response getGroup(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/name/{name}")
    Response getGroupByName(@PathParam("name") String name, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/{id}/users")
    List<UserDto> getUsersInGroup(@PathParam("id") Long groupId, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/user/{userId}")
    List<UserGroupDto> getGroupsForUser(@PathParam("userId") Long userId,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/user/{userId}/group/{groupId}/check")
    Response checkUserInGroup(@PathParam("userId") Long userId, @PathParam("groupId") Long groupId,
            @HeaderParam("Authorization") String authorization);

    @POST
    Response createGroup(UserGroupDto group, @HeaderParam("Authorization") String authorization);

    @PUT
    @Path("/{id}")
    Response updateGroup(@PathParam("id") Long id, UserGroupDto group,
            @HeaderParam("Authorization") String authorization);

    @PATCH
    @Path("/{id}")
    Response patchGroup(@PathParam("id") Long id, UserGroupDto group,
            @HeaderParam("Authorization") String authorization);

    @DELETE
    @Path("/{id}")
    Response deleteGroup(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

    @POST
    @Path("/{groupId}/users/{userId}")
    Response addUserToGroup(@PathParam("groupId") Long groupId, @PathParam("userId") Long userId,
            @HeaderParam("Authorization") String authorization);

    @DELETE
    @Path("/{groupId}/users/{userId}")
    Response removeUserFromGroup(@PathParam("groupId") Long groupId, @PathParam("userId") Long userId,
            @HeaderParam("Authorization") String authorization);
}
