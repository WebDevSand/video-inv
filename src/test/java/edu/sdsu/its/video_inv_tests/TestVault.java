package edu.sdsu.its.video_inv_tests;

import edu.sdsu.its.video_inv.Vault;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test the Vault Environment Variable configuration and Test Connection to Vault.
 * The Vault must be unsealed for these tests to pass.
 *
 * @author Tom Paulus
 *         Created on 11/3/16.
 */
public class TestVault {
    private final static Logger LOGGER = Logger.getLogger(TestVault.class);

    /**
     * Check that the environment variables that are used by the Key Server are set.
     */
    @Test
    public void checkENV() {
        final String VAULT_ADDR = System.getenv("VAULT_ADDR");
        final String ROLE_ID = System.getenv("VAULT_ROLE");
        final String SECRET_ID = System.getenv("VAULT_SECRET");
        final String APP_NAME = System.getenv("VIMS_APP");

        LOGGER.debug("ENV.VAULT_ADDR =" + VAULT_ADDR);
        LOGGER.debug("ENV.ROLE_ID =" + ROLE_ID);
        LOGGER.debug("ENV.SECRET_ID =" + SECRET_ID);
        LOGGER.debug("ENV.WELCOME_APP =" + APP_NAME);

        assertTrue("Empty Vault Address", VAULT_ADDR != null && VAULT_ADDR.length() > 0);
        assertTrue("Empty Vault Role ID", ROLE_ID != null && ROLE_ID.length() > 0);
        assertTrue("Empty Vault Secret ID", SECRET_ID != null && SECRET_ID.length() > 0);
        assertTrue("Empty App Name", APP_NAME != null && APP_NAME.length() > 0);
    }

    /**
     * Perform a self-test of the connection to the server.
     * Validity of the app-name and api-key are NOT checked.
     */
    @Test
    public void checkConnection() {
        assertTrue(Vault.testConnection());
    }
}
