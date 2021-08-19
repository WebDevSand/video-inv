package edu.sdsu.its.video_inv_tests;

import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test Users and their related functionality.
 *
 * @author Tom Paulus
 *         Created on 8/6/16.
 */
public class TestUsers {
    private static final Logger LOGGER = Logger.getLogger(TestUsers.class);

    private static final String TEST_USERNAME = "tester2";
    private static final String TEST_PASSWORD = "abcd";
    private static final String TEST_USER_FNAME = "Test";
    private static final String TEST_USER_LNAME = "User";
    private static final boolean TEST_USER_ACCESS = false;

    private static final String UPDATE_USER_FNAME = "Jane";
    private static final String UPDATE_USER_LNAME = "Doe";
    private static final boolean UPDATE_USER_ACCESS = true;
    private static final String UPDATE_USER_PASSWORD = "fghi";

    private static User USER;

    @BeforeClass
    public static void setUp() throws Exception {
        LOGGER.info("Creating New Test User");
        USER = new User(TEST_USERNAME, TEST_USER_FNAME, TEST_USER_LNAME, TEST_USER_ACCESS);
        USER.setPassword(TEST_PASSWORD);
        assertTrue(DB.createUser(USER));

        USER.completeUser();
        LOGGER.debug("Created New User: " + USER.toString());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LOGGER.warn(String.format("Deleting Test User (ID: %d/%s)", USER.dbID, USER.username));
        DB.deleteUser(USER);
    }

    @Test
    public void createDuplicateUser() {
        assertFalse(DB.createUser(USER));
    }

    @Test
    public void getUser() {
        LOGGER.info("Fetching all users and checking for completeness");
        User[] users = DB.getUser(null);
        assertTrue("No Users found", users.length > 0);
        LOGGER.debug(String.format("Retrieved %d users from DB", users.length));

        boolean test_user_found = false;
        for (User u : users) {
            assertNotNull("User not defined", u);
            assertTrue("User has no ID", u.dbID != 0 && u.username != null);
            assertTrue("User not Complete - Name not defined",
                    u.firstName != null && u.firstName.length() > 0 &&
                            u.lastName != null && u.lastName.length() > 0);
            assertNotNull("User has no status defined", u.supervisor);

            if (u.equals(USER)) test_user_found = true;
        }

        assertTrue("Test User not found in DB", test_user_found);

    }

    @Test
    public void updateUser() throws Exception {
        LOGGER.info("Updating User");
        LOGGER.debug("Current User: " + USER.toString());
        USER.firstName = UPDATE_USER_FNAME;
        USER.lastName = UPDATE_USER_LNAME;
        USER.supervisor = UPDATE_USER_ACCESS;

        USER.setPassword(UPDATE_USER_PASSWORD);
        DB.updateUser(USER);
        LOGGER.debug("Updated User: " + USER.toString());

        User[] fetched = DB.getUser("username = '" + USER.username + "'");
        assertTrue("User not found in DB", fetched.length > 0);
        assertEquals("User not Updated Correctly", USER, fetched[0]);
        assertEquals("Password not updated correctly", USER, DB.login(USER.username, UPDATE_USER_PASSWORD));
    }
}
