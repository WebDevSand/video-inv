package edu.sdsu.its.video_inv;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;

import java.util.HashMap;

/**
 * Manages Requests that go to the Vault, which stores all sensitive key data, like API Keys, Password, etc.
 * An AppRole ID and Secret ID are required for the connection to be established. All "Application Names" are expected
 * to be stored under the "secret" backend.
 * <p>
 * Uses "VAULT_ADDR", "VAULT_ROLE", "VAULT_SECRET", and "WELCOME_APP" environment variables to set the Vault Configuration.
 *
 * @author Tom Paulus
 *         Created on 11/3/16.
 */
public class Vault {
    final private static String VAULT_ADDR = System.getenv("VAULT_ADDR").endsWith("/") ? System.getenv("VAULT_ADDR") : System.getenv("VAULT_ADDR") + "/";
    final private static String ROLE_ID = System.getenv("VAULT_ROLE");
    final private static String SECRET_ID = System.getenv("VAULT_SECRET");
    final private static String APP_NAME = System.getenv("VIMS_APP");

    final private static Logger LOGGER = Logger.getLogger(Vault.class);

    private static String token = null;
    private static Long tokenExpires = null;

    @SuppressWarnings("FieldCanBeLocal")
    private static Integer renewalEpsilon = 120; // Number of Seconds before the token expires, before we renew it


    private static String getToken() {
        Gson gson = new Gson();

        if (token == null || (tokenExpires != null && tokenExpires < System.currentTimeMillis() / 1000)) {
            // We don't have a token, or it has expired
            AppRoleTokenRequest request = new AppRoleTokenRequest(ROLE_ID, SECRET_ID);
            HttpResponse vaultResponse;

            try {
                LOGGER.debug("Requesting New Token via AppRole");

                vaultResponse = Unirest
                        .post(VAULT_ADDR + "v1/auth/approle/login")
                        .body(gson.toJson(request))
                        .asJson();
            } catch (UnirestException e) {
                LOGGER.error("Problem making request to AppRole Login endpoint at Vault", e);
                return null;
            }

            if (vaultResponse.getStatus() != 200) {
                LOGGER.error(String.format("Problem Getting Login Token from Vault. HTTP Status: %d", vaultResponse.getStatus()));
                return null;
            }

            Response response = gson.fromJson(vaultResponse.getBody().toString(), Response.class);
            LOGGER.info("Retrieved Token from Vault");
            LOGGER.debug(String.format("Token: %s", response.getToken()));

            token = response.getToken();
            if (response.auth.renewable)
                tokenExpires = response.auth.lease_duration + System.currentTimeMillis() / 1000;
            else tokenExpires = null;
        } else if (tokenExpires != null && tokenExpires - renewalEpsilon > System.currentTimeMillis() / 1000) {
            // Token is Valid, and will not expire soon
            return token;
        } else {
            // Renew the Token
            HttpResponse vaultResponse;
            try {
                LOGGER.debug("Reviewing Token via Renew Request to Vault");
                LOGGER.debug(String.format("Current Token: %s", token));

                vaultResponse = Unirest
                        .post(VAULT_ADDR + "v1/auth/token/renew-self")
                        .header("X-Vault-Token", token)
                        .asJson();
            } catch (UnirestException e) {
                LOGGER.error("Problem making request to Renew Vault Token", e);
                return null;
            }

            Response response = gson.fromJson(vaultResponse.getBody().toString(), Response.class);
            if (vaultResponse.getStatus() != 200) {
                LOGGER.warn(String.format("Problem Renewing Token HTTP Status: %d", vaultResponse.getStatus()));
                token = null;
            } else {

                LOGGER.info("Renewed Vault Token");
                if (!token.equals(response.getToken())) {
                    LOGGER.info("Token has changed");
                    LOGGER.debug(String.format("New Token: %s", response.getToken()));
                } else {
                    LOGGER.debug("Token was not changed during renew");
                }

                token = response.getToken();
                if (response.auth.renewable)
                    tokenExpires = response.auth.lease_duration + System.currentTimeMillis() / 1000;
                else tokenExpires = null;
            }
            if (token == null) token = getToken(); // We failed to renew the token, let's get a new one.
        }

        return token;
    }


    public static boolean testConnection() {
        String value = getParam("hello", "value");

        if (value != null && value.isEmpty()) {
            LOGGER.warn("There is no value saved for the key \"secret\\hello\"");
        }

        LOGGER.info(String.format("Value for \"secret\\hello\" is \"%s\"", value));
        return value != null && !value.isEmpty();
    }

    /**
     * Get Parameter form the Default Application (whose name is an environment variable)
     *
     * @param parameterName {@link String} Parameter Name
     * @return {@link String} Parameter Value
     */
    public static String getParam(final String parameterName) {
        return getParam(APP_NAME, parameterName);
    }

    /**
     * Retrieve Param from Key Server
     *
     * @param applicationName {@link String} Application that the parameter is associated with
     * @param parameterName   {@link String} Parameter Name
     * @return {@link String} Parameter Value
     */
    public static String getParam(final String applicationName, final String parameterName) {
        HttpResponse vaultResponse;
        Gson gson = new Gson();

        try {
            LOGGER.debug(String.format("Requesting Secret Node: \"%s\" Value: \"%s\" from Vault", applicationName, parameterName));
            vaultResponse = Unirest
                    .get(VAULT_ADDR + "v1/secret/" + applicationName.toLowerCase())
                    .header("X-Vault-Token", getToken())
                    .asJson();
        } catch (UnirestException e) {
            LOGGER.error("Problem making request to AppRole Login endpoint at Vault", e);
            return null;
        }

        SecretRequest request = gson.fromJson(vaultResponse.getBody().toString(), SecretRequest.class);

        return request.data.get(parameterName);
    }

    private static class AppRoleTokenRequest {
        String role_id;
        String secret_id;

        public AppRoleTokenRequest(String role_id, String secret_id) {
            this.role_id = role_id;
            this.secret_id = secret_id;
        }
    }

    private static class Response {
        Auth auth;

        public String getToken() {
            return auth.client_token;
        }

        private static class Auth {
            String client_token;
            Integer lease_duration;
            Boolean renewable;
        }
    }

    private static class SecretRequest {
        HashMap<String, String> data;
    }
}