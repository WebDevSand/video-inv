package edu.sdsu.its.video_inv.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.Item;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * User Endpoints (List, Create, Update, and Label)
 * Session Tokens are needed for all endpoints, and all endpoints that make modifications to the User need to be made
 * by a Supervisor.
 *
 * @author Tom Paulus
 *         Created on 7/29/16.
 */
@Path("user")
public class Users {
    private static final Logger LOGGER = Logger.getLogger(Users.class);
    private final Gson mGson;

    public Users() {
        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        this.mGson = builder.create();
    }

    /**
     * Get all users, or a specific user based on their public or their internal identifier.
     *
     * @param sessionToken {@link String} User Session Token
     * @param username     {@link String} User's Username
     * @param dbID         {@link int} User's Internal Identifier
     * @return {@link Response} JSON Array of Users
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@HeaderParam("session") final String sessionToken,
                            @QueryParam("username") final String username,
                            @QueryParam("db-id") final int dbID) {
        User user = Session.validate(sessionToken);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info(String.format("Recieved Request for User in DB Where Username=%s OR dbID=%d", username, dbID));

        User[] users;

        if (dbID != 0) {
            final String restriction = "id = " + dbID;

            LOGGER.debug(String.format("Retrieving Users with Internal ID Restriction, \"%s\"", restriction));
            users = DB.getUser(restriction);
        } else if (username != null && !username.isEmpty()) {
            final String restriction = "username = '" + username + "'";

            LOGGER.debug(String.format("Retrieving Users with Username Restriction, \"%s\"", restriction));
            users = DB.getUser(restriction);
        } else {
            LOGGER.debug("Retrieving all users in DB");
            users = DB.getUser(null);
        }

        if (users.length > 0) {
            return Response.status(Response.Status.OK).entity(mGson.toJson(users)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(mGson.toJson(new SimpleMessage("Error", "No user exists with the specified ID"))).build();
        }
    }

    /**
     * Create a new User
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} User JSON {@see Models.User}
     * @return {@link Response} Status Message
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@HeaderParam("session") final String sessionToken,
                               final String payload) {
        User user = Session.validate(sessionToken);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(mGson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(mGson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();

        LOGGER.info("Recieved Request to create new Item");
        LOGGER.debug("POST Payload: " + payload);

        User createUser = mGson.fromJson(payload, User.class);

        // Decode Password from Base 64
        byte[] decodedPassword = Base64.decodeBase64(createUser.getPassword());
        createUser.setPassword(new String(decodedPassword));

        if (DB.createUser(createUser))
            return Response.status(Response.Status.CREATED).entity(mGson.toJson(new SimpleMessage("User Created Successfully"))).build();
        else
            return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("error", "A user with that username already exists"))).build();
    }

    /**
     * Update a User. The Internal Identifier (DB ID) cannot be changed since it is used to identify the user to update.
     * Either the user's username, or their internal identifier needs to be supplied to update the user.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} User JSON {@see Models.User}
     * @return {@link Response} Status Message
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("session") final String sessionToken,
                               final String payload) {
        User user = Session.validate(sessionToken);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(mGson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();

        User updateUser = mGson.fromJson(payload, User.class);

        if (!user.supervisor && !user.username.equals(updateUser.username)) {
            return Response.status(Response.Status.FORBIDDEN).entity(mGson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }
        if (user.username.equals(updateUser.username)) {
            LOGGER.debug("User updating own parameters");
            if (!user.supervisor) {
                // Remove the ability to change supervisor status from non-supervisory users
                updateUser.supervisor = null;
            }
        }

        if (updateUser.dbID == 0 && updateUser.username == null)
            return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "No Identifier supplied"))).build();
        if (DB.getUser("username = '" + user.username + "' OR id = " + user.dbID).length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(mGson.toJson(new SimpleMessage("Error", "User does not exist"))).build();
        if (updateUser.dbID == 0) {
            if (updateUser.username == null || updateUser.username.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "Invalid username"))).build();

            updateUser.dbID = DB.getUser("username = '" + updateUser.username + "'")[0].dbID;
        }
        if (updateUser.getPassword() != null && !updateUser.getPassword().isEmpty()) {
            // Decode Password from Base 64
            byte[] decodedPassword = Base64.decodeBase64(updateUser.getPassword());
            updateUser.setPassword(new String(decodedPassword));
        }

        DB.updateUser(updateUser);

        return Response.status(Response.Status.OK).entity(mGson.toJson(new SimpleMessage("User Updated"))).build();
    }

    /**
     * Delete a User. The Internal Identifier (DB ID) cannot be changed since it is used to identify the user to update.
     * Either the user's username, or their internal identifier needs to be supplied to delete the user.
     * The User may not have any transaction history, either owning or approving, since a Primary Key/Foreign key
     * relationship exists, preventing deletion.
     *
     * @param sessionToken {@link String} User Session Token
     * @param payload      {@link String} User JSON {@see Models.User}
     * @return {@link Response} Status Message
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("session") final String sessionToken,
                               final String payload) {
        User user = Session.validate(sessionToken);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        if (!user.supervisor) {
            return Response.status(Response.Status.FORBIDDEN).entity(mGson.toJson(new SimpleMessage("Error", "You are not allowed to do that."))).build();
        }
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(mGson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();

        User deleteUser = mGson.fromJson(payload, User.class);
        if (deleteUser.dbID == 0 && deleteUser.username == null)
            return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "No Identifier supplied"))).build();
        if (DB.getUser("username = '" + user.username + "' OR id = " + user.dbID).length == 0)
            return Response.status(Response.Status.NOT_FOUND).entity(mGson.toJson(new SimpleMessage("Error", "User does not exist"))).build();
        if (deleteUser.dbID == 0) {
            if (deleteUser.username == null || deleteUser.username.isEmpty())
                return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "Invalid username"))).build();

            deleteUser.dbID = DB.getUser("username = '" + deleteUser.username + "'")[0].dbID;
        }
        if (DB.getTransaction("u.id = " + deleteUser.dbID + " OR s.id = " + deleteUser.dbID).length > 0)
            return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "User has transaction history. Cannot be deleted."))).build();

        DB.deleteUser(deleteUser);

        return Response.status(Response.Status.OK).entity(mGson.toJson(new SimpleMessage("User Deleted"))).build();
    }


    /**
     * List all checked-out items for the current user (determined by the sessionToken)
     *
     * @param sessionToken {@link String} User Session Token
     * @return {@link Response} Item JSON
     */
    @Path("checkedOut")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckedOut(@HeaderParam("session") final String sessionToken) {
        User user = Session.validate(sessionToken);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(mGson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        Item[] items = DB.getItem("checked_out = 1 AND t1.owner = " + user.dbID);

        Gson gson = new Gson(); // We need a different GSON since mGSON is setup to deal with Users, not Items
        return Response.status(Response.Status.OK).entity(gson.toJson(items)).build();
    }
}
