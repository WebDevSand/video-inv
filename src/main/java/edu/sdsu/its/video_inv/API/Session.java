package edu.sdsu.its.video_inv.API;

import com.google.gson.Gson;
import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.User;
import edu.sdsu.its.video_inv.Vault;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.UUID;

/**
 * Creates and Verifies Sessions.
 * Session tokens are Base64 Encoded and Encrypted and contain the user's username.
 *
 * @author Tom Paulus
 *         Created on 7/6/16.
 */
@Path("session")
public class Session {
    final private static String PROJECT_TOKEN = Vault.getParam("project_token");
    final private static String TOKEN_CYPHER = Vault.getParam("token_cypher");
    final private static long TTL = Long.parseLong(Vault.getParam("token_ttl"));

    final private static Logger LOGGER = Logger.getLogger(Session.class);
    final private static StandardPBEStringEncryptor ENCRYPTOR = new StandardPBEStringEncryptor();

    private String token; // this is the authentication token user will send in order to use the web service
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private long expires;

    @SuppressWarnings({"unused", "WeakerAccess"})
    public Session() {
        // Leave Blank, Used by Jersey for API Endpoint.
    }

    public Session(User user) {
        if (!ENCRYPTOR.isInitialized()) initializeEncryptor();
        final long millis = System.currentTimeMillis();
        String key = UUID.randomUUID().toString().toUpperCase() +
                "|" + PROJECT_TOKEN +
                "|" + user.username +
                "|" + millis;

        this.token = new String(Base64.encodeBase64(ENCRYPTOR.encrypt(key).getBytes()));
        this.expires = millis + TTL;
    }

    @Path("verify")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static Response checkToken(@HeaderParam("session") final String sessionToken) {
        final Response.ResponseBuilder response;
        Gson gson = new Gson();

        if (sessionToken == null || sessionToken.length() == 0 || "undefined".equals(sessionToken) || "null".equals(sessionToken)) {
            response = Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new SimpleMessage("Error", "Malformed Requests - Session header not set for request")));
        } else if (validate(sessionToken) == null) {
            response = Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Token - The token is invalid, which is usually because the token has expired.")));
        } else {
            response = Response.status(Response.Status.OK).entity(gson.toJson(new SimpleMessage("Token valid")));
        }

        return response.build();
    }

    /**
     * Validate a Session Token. Checks for Proper Format, User existence, and TTL
     *
     * @param token {@link String} Base64 Encoded Token
     * @return {@link User} User associated with Token. Null if invalid.
     */
    public static User validate(final String token) {
        if (!ENCRYPTOR.isInitialized()) initializeEncryptor();
        if (token == null || token.isEmpty()) return null;

        LOGGER.debug(String.format("Validating Token: \"%s\"", token));
        final String decodedToken = new String(Base64.decodeBase64(token.getBytes()));
        LOGGER.debug(String.format("Decoded Token: \"%s\"", decodedToken));
        final String decryptedToken;
        try {
            decryptedToken = ENCRYPTOR.decrypt(decodedToken);
        } catch (EncryptionOperationNotPossibleException e) {
            LOGGER.warn("Problem decrypting session token", e);
            return null;
        }
        LOGGER.debug(String.format("Decrypted Token: \"%s\"", decryptedToken));
        final String[] key = decryptedToken.split("\\|");  // Split on Vertical Bars. | is a special character in RegEx.
        LOGGER.debug("Token Components: " + Arrays.toString(key));

        final String tokenProject = key[1];
        LOGGER.debug("Token Project Token: " + tokenProject);
        if (!PROJECT_TOKEN.equals(tokenProject)) {
            LOGGER.info("Token Validation Failed - Project Token Invalid");
            return null;
        }

        final long tokenTTL = System.currentTimeMillis() - Long.parseLong(key[3]);
        LOGGER.debug("Token TTL: " + tokenTTL);
        if (tokenTTL > TTL) {
            LOGGER.info("Token Validation Failed - TTL Invalid");
            return null;
        }
        final String tokenUsername = key[2];
        LOGGER.debug("Token Username: " + tokenUsername);

        final User user = DB.getUser("username = '" + tokenUsername + "'")[0];
        if (user != null) {
            LOGGER.info(String.format("Token for User (\"%s\") is valid!", tokenUsername));
            return user;
        } else {
            LOGGER.info(String.format("Token Validation Failed - User (\"%s\") does not exist", tokenUsername));
            return null;
        }
    }

    private static void initializeEncryptor() {
        LOGGER.info("Initializing Encryptor - Setting Token Cypher");
        ENCRYPTOR.setPassword(TOKEN_CYPHER);
    }

    public String getToken() {
        return this.token;
    }

    public long getExpires() {
        return expires;
    }
}