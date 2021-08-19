package edu.sdsu.its.video_inv.API;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.Transaction;
import edu.sdsu.its.video_inv.Models.User;
import edu.sdsu.its.video_inv.Report;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Random;

/**
 * Manage Transaction Endpoints (Get and Create).
 * Once created, a transaction is non-updatable to ensure the chain-of-custody is uninterrupted.
 *
 * @author Tom Paulus
 *         Created on 7/29/16.
 */
@Path("transaction")
public class Transactions {
    private static final int TRANSACTION_ID_LENGTH = 6;
    private static final Logger LOGGER = Logger.getLogger(Transactions.class);

    private static String generateTransactionID() {
        String s = new RandomString(TRANSACTION_ID_LENGTH).nextString().toUpperCase();
        LOGGER.debug("Generated Transaction ID: " + s);
        return s;
    }

    /**
     * List all or a specific transaction.
     * If no transactionID is supplied, all transactions will be returned.
     *
     * @param sessionToken  {@link String} User Session Token
     * @param transactionID {@link String} Transaction ID to fetch, All transactions will be returned if Null
     * @return {@link Response} JSON Transaction Array {@see Models.Transaction}
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTransaction(@HeaderParam("session") final String sessionToken,
                                   @QueryParam("id") String transactionID,
                                   @QueryParam("limit") int limit) {

        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (transactionID == null) transactionID = "";
        LOGGER.info("Recieved Request for Transaction Where ID=" + transactionID);

        String restriction = "";
        if (transactionID.length() > 0) {
            restriction = "t.id = " + transactionID;
        }

        Transaction[] transactions = DB.getTransaction(restriction, limit);
        if (transactionID.length() > 0 && transactions.length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(new SimpleMessage("Error", "No transaction with that ID was found"))).build();

        return Response.status(Response.Status.OK).entity(gson.toJson(transactions)).build();
    }

    /**
     * Create a New Transaction.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} JSON Transaction Object
     * @return {@link Response} Status Message
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTransaction(@HeaderParam("session") final String sessionToken,
                                      final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Create Transaction");
        LOGGER.debug("Transaction Payload: " + payload);

        Transaction createTransaction = gson.fromJson(payload, Transaction.class);
        createTransaction.id = generateTransactionID();

        if (DB.getUser("id = " + createTransaction.owner.dbID).length == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "Owner is not a valid user"))).build();

        User[] supervisor = DB.getUser("id = " + createTransaction.supervisor.dbID);
        if (supervisor.length == 0 || !supervisor[0].supervisor)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "Supervisor is not a valid user, or does not have the authority to authorize new transactions"))).build();

        for (Transaction.Component component : createTransaction.components) {
            if (DB.getItem("i.id = " + component.id).length == 0)
                return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", String.format("Item with ID (%d/%d) is not a valid Item", component.id, component.pubID)))).build();
        }

        DB.createTransaction(createTransaction);

        return Response.status(Response.Status.CREATED).entity(gson.toJson(createTransaction)).build();
    }

    @GET
    @Path("receipt/{id}")
    @Consumes(MediaType.WILDCARD)
    @Produces("application/pdf")
    public Response getTransactionReceipt(@PathParam("id") final String transactionID) {
        Gson gson = new Gson();
        if (transactionID == null || transactionID.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "No Transaction ID Specified"))).build();


        Transaction[] transactions = DB.getTransaction("t.id = '" + transactionID + "'");
        if (transactionID.length() > 0 && transactions.length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(new SimpleMessage("Error", "No transaction with that ID was found"))).build();

        File report = Report.transactionReport(transactions[0]);

        Response.ResponseBuilder response = Response.ok(report);
        response.header("Content-Disposition", String.format("inline; filename=transaction-%s.pdf", transactionID));
        return response.build();
    }

    public static class RandomString {

        private static final char[] symbols;

        static {
            StringBuilder tmp = new StringBuilder();
            for (char ch = '0'; ch <= '9'; ++ch)
                tmp.append(ch);
            for (char ch = 'a'; ch <= 'z'; ++ch)
                tmp.append(ch);
            symbols = tmp.toString().toCharArray();
        }

        private final Random random = new Random();
        private final char[] buf;

        public RandomString(int length) {
            if (length < 1)
                throw new IllegalArgumentException("length < 1: " + length);
            buf = new char[length];
        }

        public String nextString() {
            for (int idx = 0; idx < buf.length; ++idx)
                buf[idx] = symbols[random.nextInt(symbols.length)];
            return new String(buf);
        }
    }
}
