package edu.sdsu.its.video_inv.API;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Label;
import edu.sdsu.its.video_inv.Models.Item;
import edu.sdsu.its.video_inv.Models.Transaction;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;

/**
 * Item Endpoints (List, Create, Update, Delete, and Print Label).
 * Session Tokens are needed for all endpoints, and all endpoints that make modifications to the User need to be made
 * by a Supervisor.
 *
 * @author Tom Paulus
 *         Created on 7/29/16.
 */
@Path("item")
public class Items {
    private static final Logger LOGGER = Logger.getLogger(Items.class);

    /**
     * Retrieve All items, a specific item, or macro from the DB.
     * If no restriction is supplied (neither id, nor db-id), then all items will be returned.
     *
     * @param sessionToken {@link String} User Session Token
     * @param publicID     {@link int} Public ID (From either a specific item, or Macro)
     * @param dbID         {@link int} Internal Identifier
     * @return {@link Response} JSON Array of Items {@see Models.Item}
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItem(@HeaderParam("session") final String sessionToken,
                            @QueryParam("id") final int publicID,
                            @QueryParam("db-id") final int dbID) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info(String.format("Recieved Request for Item in DB Where PublicID=%d OR dbID=%d", publicID, dbID));

        Item[] items;
        if (dbID != 0) {
            final String restriction = "i.id = " + dbID;

            LOGGER.debug(String.format("Retrieving Items with Internal ID Restriction, \"%s\"", restriction));
            items = DB.getItem(restriction);
        } else if (publicID != 0) {
            int id = formatID(publicID);
            if (intLength(id) != 6) {
                return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Public ID Length"))).build();
            }
            final String restriction = "i.pub_id = " + id + " OR m.id = " + id;

            LOGGER.debug(String.format("Retrieving Items with Public ID Restriction, \"%s\"", restriction));
            items = DB.getItem(restriction);
        } else {
            LOGGER.debug("Retrieving all items in inventory");
            items = DB.getItem(null);
        }

        if (items.length > 0) {
            return Response.status(Response.Status.OK).entity(gson.toJson(items)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Create new Item.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} Item JSON {@see Models.Item}
     * @return {@link Response} Created Item JSON
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createItem(@HeaderParam("session") final String sessionToken,
                               final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Create Item");
        LOGGER.debug("Item Payload: " + payload);

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Item createItem = gson.fromJson(payload, Item.class);
        int id;
        do {
            Random rnd = new Random();
            id = 100000 + rnd.nextInt(900000);
        }
        while (DB.getItem("i.pub_id = " + id + " OR m.id = " + id).length > 0); // Generate 6 Digit ID, and check that it doesn't already exist

        createItem.pubID = id;
        DB.createItem(createItem);

        return Response.status(Response.Status.CREATED).entity(gson.toJson(createItem)).build();
    }

    /**
     * Update an Item. All defined fields will be updated.
     * The item's public or internal identifier must be supplied.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} Item JSON {@see Models.Item}
     * @return {@link Response} Status Message
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateItem(@HeaderParam("session") final String sessionToken,
                               final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Update Item");
        LOGGER.debug("Item Payload: " + payload);

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Item updateItem = gson.fromJson(payload, Item.class);
        if (updateItem.pubID == 0 && updateItem.id == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "No valid identifier supplied"))).build();
        if (updateItem.id == 0) {
            int id = formatID(updateItem.pubID);
            if (intLength(id) != 6) {
                return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "Invalid ID Length"))).build();
            }
            final String restriction = "i.pub_id = " + id;
            updateItem.id = DB.getItem(restriction)[0].id;
        }

        DB.updateItem(updateItem);

        return Response.status(Response.Status.OK).entity(gson.toJson(new SimpleMessage("Item Updated"))).build();
    }

    /**
     * Delete an Item.
     * The item's public or internal identifier must be defined.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} Item JSON {@see Models.Item}
     * @return {@link Response} Status Message
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteItem(@HeaderParam("session") final String sessionToken,
                               final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Delete Item");
        LOGGER.debug("Item Payload: " + payload);

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Item deleteItem = gson.fromJson(payload, Item.class);
        if (deleteItem.pubID == 0 && deleteItem.id == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "No valid identifier supplied"))).build();

        Item[] item = DB.getItem(String.format("i.pub_id = %d OR i.id = %d", deleteItem.pubID, deleteItem.id));
        if (item.length == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "Unknown Item ID"))).build();
        if (item[0].lastTransactionID != null && !item[0].lastTransactionID.isEmpty())
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "Item has Transaction History. Cannot Delete."))).build();

        DB.deleteItem(item[0]);

        return Response.status(Response.Status.OK).entity(gson.toJson(new SimpleMessage("Deleted Item"))).build();
    }

    /**
     * Generate the DYMO Label XML for the item label.
     * If the item has a short name defined, it will be printed in place of the default Label Text (Header)
     *
     * @param sessionToken {@link String} User Session Token
     * @param itemID       {@link int} Item's Public Identifier
     * @return {@link Response} Label XML
     */
    @Path("label")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_XML)
    public Response getItemLabel(@HeaderParam("session") final String sessionToken,
                                 @QueryParam("id") int itemID) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (itemID == 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "No ID Supplied"))).build();
        }

        LOGGER.info(String.format("Recieved Request for Item Label (PublicID=%d)", itemID));
        itemID = formatID(itemID);
        if (intLength(itemID) != 6) {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Public ID Length"))).build();
        }

        Item item = DB.getItem("i.pub_id = " + itemID)[0];
        return Response.status(Response.Status.OK).entity(Label.generateLabel(item.shortName, itemID)).build();
    }

    /**
     * Get the most popular items.
     * Returns a slightly modified item which includes the frequency of checkouts over its lifetime.
     *
     * @param sessionToken {@link String} User Session Token
     * @param count        {@link int} Number of items to get
     * @return {@link Response} Item.Popular JSON
     */
    @Path("popular")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPopularItems(@HeaderParam("session") final String sessionToken,
                                    @QueryParam("count") int count) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (count == 0) {
            LOGGER.debug("Using Default Item count for Popular Item Request");
        }

        LOGGER.info(String.format("Recieved Request the most popular %d items", count));
        Item.Popular[] populars = DB.getPopularItem(count);
        return Response.status(Response.Status.OK).entity(gson.toJson(populars)).build();
    }

    /**
     * Get all items currently checked out
     *
     * @param sessionToken {@link String} User Session Token
     * @return {@link Response} Checked out Items JSON
     */
    @Path("out")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckedOut(@HeaderParam("session") final String sessionToken) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info("Recieved Request the currently checked out items");
        Item[] items = DB.getItem("checked_out = 1");
        return Response.status(Response.Status.OK).entity(gson.toJson(items)).build();
    }


    /**
     * Access an Items Transaction History and return a Transaction Array as JSON
     *
     * @param sessionToken {@link String} User Session Token
     * @param itemID       {@link int} Item's Public ID
     * @return {@link Response} Transaction History for an Item JSON
     */
    @GET
    @Path("history")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getItemHistory(@HeaderParam("session") final String sessionToken,
                                   @QueryParam("id") final int itemID) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Item item = DB.getItem("i.pub_id = " + itemID)[0];
        if (item == null)
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(new SimpleMessage("Error", "Item not found"))).build();

        Transaction[] history = DB.getTransaction("i.pub_id = " + itemID);
        LOGGER.debug(String.format("Item History for %s(%d) returned %d transactions", item.name, item.id, history.length));
        return Response.status(Response.Status.OK).entity(gson.toJson(history)).build();
    }

    /**
     * All Core IDs are 6 digits, but barcodes have an 8 digit ID, which is scanned by the barcode reader.
     * For BarcodeIDs, the first digit is always 0 and the last digit is the checksum. This last digit is the
     * one that needs to be discarded.
     *
     * @param rawID {@link int} Original
     * @return {@link int} 6-digit ID
     */
    private int formatID(int rawID) {
        if (intLength(rawID) > 6)
            return rawID / 10;
        return rawID;
    }

    /**
     * Calculate the number of digits in an int. Useful for when decoding numeral only barcodes.
     *
     * @param i {@link int} int
     * @return {@link int} Length
     */
    private int intLength(int i) {
        if (i == 0) return 0;
        if (i < 0) i = i * -1;

        return (int) (Math.log10(i) + 1);
    }
}
