package edu.sdsu.its.video_inv_tests;

import edu.sdsu.its.video_inv.API.Session;
import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Session and Pin Validation.
 *
 * @author Tom Paulus
 *         Created on 8/5/16.
 */
public class TestSession {
    private static final Logger LOGGER = Logger.getLogger(TestSession.class);

    private static final String TEST_USERNAME = "tester";
    private static final String TEST_USER_FNAME = "Session";
    private static final String TEST_USER_LNAME = "Tester";
    private static final String TEST_USER_PASSWORD = "wxyz";
    private static User USER;
    private static Session SESSION;

    @BeforeClass
    public static void setUp() throws Exception {
        LOGGER.info("Creating new Test User");
        USER = new User(TEST_USERNAME, TEST_USER_FNAME, TEST_USER_LNAME, true);
        USER.setPassword(TEST_USER_PASSWORD);
        assertTrue(DB.createUser(USER));
        LOGGER.debug("Created new Test User: " + USER.toString());

        SESSION = new Session(USER);
        LOGGER.info("Created Session Token: " + SESSION.getToken());
        LOGGER.debug("Token Expires: " + SESSION.getExpires());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LOGGER.warn(String.format("Deleting Test User (ID: %d/%s)", USER.dbID, USER.username));
        DB.deleteUser(USER);
    }

    @Test
    public void validateSession() throws Exception {
        assertEquals(USER, Session.validate(SESSION.getToken()));
    }

    @Test
    public void login() throws Exception {
        assertEquals(USER, DB.login(TEST_USERNAME, TEST_USER_PASSWORD));
    }
}
