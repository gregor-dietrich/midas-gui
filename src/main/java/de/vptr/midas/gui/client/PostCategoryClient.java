package de.vptr.midas.gui.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.vptr.midas.gui.dto.PostCategoryDto;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PostCategoryClient {

    @GET
    List<PostCategoryDto> getAllCategories(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/root")
    List<PostCategoryDto> getRootCategories(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/{id}")
    Response getCategory(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/parent/{parentId}")
    List<PostCategoryDto> getCategoriesByParent(@PathParam("parentId") Long parentId,
            @HeaderParam("Authorization") String authorization);

    @POST
    Response createCategory(PostCategoryDto category, @HeaderParam("Authorization") String authorization);

    @PUT
    @Path("/{id}")
    Response updateCategory(@PathParam("id") Long id, PostCategoryDto category,
            @HeaderParam("Authorization") String authorization);

    @PATCH
    @Path("/{id}")
    Response patchCategory(@PathParam("id") Long id, PostCategoryDto category,
            @HeaderParam("Authorization") String authorization);

    @DELETE
    @Path("/{id}")
    Response deleteCategory(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);
}
