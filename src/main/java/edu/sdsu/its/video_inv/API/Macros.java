package edu.sdsu.its.video_inv.API;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Label;
import edu.sdsu.its.video_inv.Models.Macro;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Random;

/**
 * Manage Macros. (Create, List, Update, and Delete).
 * Macros allow several items, like a Kit, to be represented by a single ID/Barcode Sticker.
 * <p>
 * Session Tokens are needed for all endpoints, and all endpoints that make modifications to the User need to be made
 * by a Supervisor.
 *
 * @author Tom Paulus
 *         Created on 7/29/16.
 */
@Path("macro")
public class Macros {
    private static final Logger LOGGER = Logger.getLogger(Macros.class);

    /**
     * List either all, or a specific macro. If no ID is provided, all Macros will be returned.
     *
     * @param sessionToken {@link String} User Session Token
     * @param macroID      {@link int} MacroID, if null, all Macros will be returned
     * @return {@link Response} JSON Macro Array {@see Models.Macro}
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMacro(@HeaderParam("session") final String sessionToken,
                             @QueryParam("id") final int macroID) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info(String.format("Recieved Request to Get Macro(s) WHERE ID = %d", macroID));

        String restriction = "";
        if (macroID != 0) restriction = "id = " + formatID(macroID);

        Macro[] macros = DB.getMacro(restriction);

        if (macroID != 0 && macros.length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(new SimpleMessage("Error", "Macro not found"))).build();
        return Response.status(Response.Status.OK).entity(gson.toJson(macros)).build();
    }

    /**
     * Create a new Macro.
     * An unique ID will be created, and included in the Return Object.
     * The Macro must have a name defined.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} JSON Macro Object {@see Models.Macro}
     * @return {@link Response} Created Macro Object as JSON {@see Models.Macro}
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMacro(@HeaderParam("session") final String sessionToken,
                                final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Create Macro");
        LOGGER.debug("Item Payload: " + payload);

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Macro createMacro = gson.fromJson(payload, Macro.class);
        if (createMacro.name == null || createMacro.name.length() == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Macro does not have a name defined"))).build();

        int id = 0;
        boolean exists = false;

        while (id == 0 || exists) {
            // Generate 6 Digit ID, and check that it doesn't already exist
            Random rnd = new Random();
            id = 100000 + rnd.nextInt(900000);

            if (DB.getMacro("id = " + id).length > 0 || DB.getItem("i.pub_id = " + id).length > 0) exists = true;
        }
        LOGGER.debug("Creating Macro with ID: " + id);
        createMacro.id = id;
        DB.createMacro(createMacro);

        return Response.status(Response.Status.CREATED).entity(gson.toJson(createMacro)).build();
    }

    /**
     * Update a Macro.
     * The Macro's ID must be supplied, and cannot be changed.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} JSON Macro Object {@see Models.Macro}
     * @return {@link Response} Status Message
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateMacro(@HeaderParam("session") final String sessionToken,
                                final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Update Macro");
        LOGGER.debug("Item Payload: " + payload);

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Macro updateMacro = gson.fromJson(payload, Macro.class);
        if (updateMacro.id == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "No Identifier Supplied"))).build();

        if (DB.getMacro("id = " + updateMacro.id).length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(new SimpleMessage("Error", "Macro Not Found. You may need to create it first"))).build();

        if (updateMacro.name == null || updateMacro.name.length() == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Macro does not have a name defined"))).build();

        DB.updateMacro(updateMacro);

        return Response.status(Response.Status.OK).entity(gson.toJson(new SimpleMessage("Macro Updated"))).build();
    }

    /**
     * Delete a Macro.
     * This action is permanent, and not-revertible. The Macro's ID must be supplied in the payload.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} JSON Macro Object {@see Models.Macro}
     * @return {@link Response} Status Message
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMacro(@HeaderParam("session") final String sessionToken,
                                final String payload) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        LOGGER.info("Recieved Request to Update Macro");
        LOGGER.debug("Item Payload: " + payload);

        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }

        Macro deleteMacro = gson.fromJson(payload, Macro.class);
        if (deleteMacro.id == 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "No Identifier Supplied"))).build();

        if (DB.getMacro("id = " + deleteMacro.id).length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(new SimpleMessage("Error", "Macro Not Found"))).build();

        DB.deleteMacro(deleteMacro);

        return Response.status(Response.Status.OK).entity(gson.toJson(new SimpleMessage("Macro Deleted"))).build();
    }

    /**
     * Generate DYMO Label XML for a Macro
     *
     * @param sessionToken {@link String} User Session Token
     * @param macroID      {@link int} Macro ID
     * @return {@link Response} Label XML
     */
    @Path("label")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_XML)
    public Response getMacroLabel(@HeaderParam("session") final String sessionToken,
                                  @QueryParam("id") int macroID) {
        User user = Session.validate(sessionToken);
        Gson gson = new Gson();
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (macroID == 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "No ID Supplied"))).build();
        }

        LOGGER.info(String.format("Recieved Request for Item Label (PublicID=%d)", macroID));
        macroID = formatID(macroID);
        if (intLength(macroID) != 6) {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Public ID Length"))).build();
        }

        return Response.status(Response.Status.OK).entity(Label.generateMacroLabel(macroID)).build();
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
