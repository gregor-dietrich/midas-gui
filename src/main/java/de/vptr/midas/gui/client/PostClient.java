package de.vptr.midas.gui.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.vptr.midas.gui.dto.PostDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PostClient {

    @GET
    List<PostDto> getAllPosts(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/published")
    List<PostDto> getPublishedPosts(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/{id}")
    Response getPost(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/user/{userId}")
    List<PostDto> getPostsByUser(@PathParam("userId") Long userId, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/category/{categoryId}")
    List<PostDto> getPostsByCategory(@PathParam("categoryId") Long categoryId,
            @HeaderParam("Authorization") String authorization);

    @POST
    Response createPost(PostDto post, @HeaderParam("Authorization") String authorization);

    @PUT
    @Path("/{id}")
    Response updatePost(@PathParam("id") Long id, PostDto post, @HeaderParam("Authorization") String authorization);

    @PATCH
    @Path("/{id}")
    Response patchPost(@PathParam("id") Long id, PostDto post, @HeaderParam("Authorization") String authorization);

    @DELETE
    @Path("/{id}")
    Response deletePost(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);
}
