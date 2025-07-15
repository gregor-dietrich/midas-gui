package de.vptr.midas.gui.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import de.vptr.midas.gui.dto.UserPayment;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "midas-api")
@Path("/user-payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface UserPaymentClient {

    @GET
    List<UserPayment> getAllPayments(@HeaderParam("Authorization") String authorization);

    @GET
    @Path("/{id}")
    Response getPayment(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/user/{userId}")
    List<UserPayment> getPaymentsByUser(@PathParam("userId") Long userId,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/source/{sourceId}")
    List<UserPayment> getPaymentsBySourceAccount(@PathParam("sourceId") Long sourceId,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/target/{targetId}")
    List<UserPayment> getPaymentsByTargetAccount(@PathParam("targetId") Long targetId,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/date-range")
    List<UserPayment> getPaymentsByDateRange(
            @QueryParam("startDate") LocalDate startDate,
            @QueryParam("endDate") LocalDate endDate,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/recent")
    List<UserPayment> getRecentPayments(@QueryParam("limit") int limit,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/amount-range")
    List<UserPayment> getPaymentsByAmountRange(
            @QueryParam("minAmount") BigDecimal minAmount,
            @QueryParam("maxAmount") BigDecimal maxAmount,
            @HeaderParam("Authorization") String authorization);

    @GET
    @Path("/user/{userId}/total")
    Response getTotalAmountByUser(@PathParam("userId") Long userId, @HeaderParam("Authorization") String authorization);

    @POST
    Response createPayment(UserPayment payment, @HeaderParam("Authorization") String authorization);

    @PUT
    @Path("/{id}")
    Response updatePayment(@PathParam("id") Long id, UserPayment payment,
            @HeaderParam("Authorization") String authorization);

    @PATCH
    @Path("/{id}")
    Response patchPayment(@PathParam("id") Long id, UserPayment payment,
            @HeaderParam("Authorization") String authorization);

    @DELETE
    @Path("/{id}")
    Response deletePayment(@PathParam("id") Long id, @HeaderParam("Authorization") String authorization);
}
