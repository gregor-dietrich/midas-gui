package de.vptr.midas.gui.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.vptr.midas.gui.dto.UserAccount;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/user-accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserAccountClient {

        @GET
        List<UserAccount> getAllAccounts(@HeaderParam("Authorization") String authorization);

        @GET
        @Path("/{id}")
        Response getAccount(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

        @GET
        @Path("/name/{name}")
        Response getAccountByName(@PathParam("name") String name, @HeaderParam("Authorization") String authorization);

        @GET
        @Path("/user/{userId}")
        List<UserAccount> getAccountsByUser(@PathParam("userId") Long userId,
                        @HeaderParam("Authorization") String authorization);

        @GET
        @Path("/{id}/payments/outgoing")
        Response getOutgoingPayments(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

        @GET
        @Path("/{id}/payments/incoming")
        Response getIncomingPayments(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

        @GET
        @Path("/{id}/users")
        Response getAssociatedUsers(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

        @GET
        @Path("/search")
        List<UserAccount> searchAccounts(@QueryParam("query") String query,
                        @HeaderParam("Authorization") String authorization);

        @POST
        Response createAccount(UserAccount account, @HeaderParam("Authorization") String authorization);

        @PUT
        @Path("/{id}")
        Response updateAccount(@PathParam("id") Long id, UserAccount account,
                        @HeaderParam("Authorization") String authorization);

        @DELETE
        @Path("/{id}")
        Response deleteAccount(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);
}
