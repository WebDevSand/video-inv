package edu.sdsu.its.video_inv.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Create initial sessions for users and verify pins from supervisors.
 *
 * @author Tom Paulus
 *         Created on 8/4/16.
 */
@Path("/")
public class Login {
    private static final Logger LOGGER = Logger.getLogger(Login.class);
    private final Gson mGson;

    public Login() {
        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        this.mGson = builder.create();
    }

    /**
     * Login a User via their public identifier
     *
     * @param payload {@link String} Login JSON Object - A User object with the Username(username) and Password {@see Models.User}
     * @return {@link Response} User JSON Object {@see Models.User} and Session Token (Header)
     */
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(final String payload) {
        if (payload == null || payload.length() == 0)
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(mGson.toJson(new SimpleMessage("Error", "Empty Request Payload"))).build();
        User user = mGson.fromJson(payload, User.class);
        if (user.username == null)
            return Response.status(Response.Status.BAD_REQUEST).entity(mGson.toJson(new SimpleMessage("Error", "No valid identifier supplied"))).build();

        // Decode Password from Base 64
        byte[] decodedPassword = Base64.decodeBase64(user.getPassword());
        user.setPassword(new String(decodedPassword));

        User loginUser = user.login();
        if (loginUser == null)
            return Response.status(Response.Status.NOT_FOUND).entity(mGson.toJson(new SimpleMessage("Error", "That user does not exist or the password is incorrect."))).build();

        Session session = new Session(loginUser);
        return Response.status(Response.Status.OK).entity(mGson.toJson(loginUser)).header("session", session.getToken()).build();

    }
}

