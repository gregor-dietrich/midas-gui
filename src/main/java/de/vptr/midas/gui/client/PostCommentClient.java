package de.vptr.midas.gui.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.vptr.midas.gui.dto.PostCommentDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PostCommentClient {

    @GET
    List<PostCommentDto> getAllComments(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/{id}")
    Response getComment(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/post/{postId}")
    List<PostCommentDto> getCommentsByPost(@PathParam("postId") Long postId,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/user/{userId}")
    List<PostCommentDto> getCommentsByUser(@PathParam("userId") Long userId,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/recent")
    List<PostCommentDto> getRecentComments(@QueryParam("limit") int limit,
            @HeaderParam("Authorization") String authorization);

    @POST
    Response createComment(PostCommentDto comment, @HeaderParam("Authorization") String authorization);

    @PUT
    @Path("/{id}")
    Response updateComment(@PathParam("id") Long id, PostCommentDto comment,
            @HeaderParam("Authorization") String authorization);

    @PATCH
    @Path("/{id}")
    Response patchComment(@PathParam("id") Long id, PostCommentDto comment,
            @HeaderParam("Authorization") String authorization);

    @DELETE
    @Path("/{id}")
    Response deleteComment(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);
}
