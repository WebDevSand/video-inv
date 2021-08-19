package edu.sdsu.its.video_inv_tests;

import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.Item;
import edu.sdsu.its.video_inv.Models.Transaction;
import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test Transaction sna their related functionality.
 *
 * @author Tom Paulus
 *         Created on 8/6/16.
 */
public class TestTransactions {
    private static final Logger LOGGER = Logger.getLogger(TestTransactions.class);

    private static final String TEST_USERNAME = "tester";
    private static final String TEST_PASSWORD = "test";
    private static final String TEST_USER_FNAME = "Test";
    private static final String TEST_USER_LNAME = "User";
    private static final boolean TEST_USER_ACCESS = true;

    private static final int TEST_ITEM_PUBID = 999953;
    private static final String TEST_ITEM_NAME = "Test Item for Transactions";
    private static final String TEST_ITEM_CONDITION = "Falling apart at the seams";

    private static final String TEST_TRANSACTION_ID = "ABC123";
    private static final boolean TEST_TRANSACTION_DIRECTION = false;

    private static User USER;
    private static Item ITEM;
    private static Transaction TRANSACTION;

    @BeforeClass
    public static void setUp() throws Exception {
        LOGGER.info("Creating New Test User");
        USER = new User(TEST_USERNAME, TEST_USER_FNAME, TEST_USER_LNAME, TEST_USER_ACCESS);
        USER.setPassword(TEST_PASSWORD);
        assertTrue(DB.createUser(USER));

        USER.completeUser();
        LOGGER.debug("Created New User: " + USER.toString());

        LOGGER.info("Creating Test Item");
        ITEM = new Item(TEST_ITEM_PUBID, TEST_ITEM_NAME, "");
        DB.createItem(ITEM);

        ITEM.completeItem();
        LOGGER.debug("Created Test Item: " + ITEM.toString());

        LOGGER.info("Creating Test Transaction");
        List<Transaction.Component> componentList = new ArrayList<>();
        componentList.add(new Transaction.Component(ITEM.id, ITEM.pubID, ITEM.category, ITEM.name, ITEM.assetID, TEST_ITEM_CONDITION));
        TRANSACTION = new Transaction(TEST_TRANSACTION_ID, USER, USER, TEST_TRANSACTION_DIRECTION, componentList);
        DB.createTransaction(TRANSACTION);

        LOGGER.debug("Created new Test Transaction: " + TRANSACTION.toString());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LOGGER.warn(String.format("Deleting Test Transaction (ID: %s)", TRANSACTION.id));
        DB.deleteTransaction(TRANSACTION);

        LOGGER.warn(String.format("Deleting Test Item (ID: %d/%d)", ITEM.id, ITEM.pubID));
        DB.deleteItem(ITEM);

        LOGGER.warn(String.format("Deleting Test User (ID: %d/%s)", USER.dbID, USER.username));
        DB.deleteUser(USER);
    }

    @Test
    public void getTransaction() throws Exception {
        LOGGER.info("Getting All Transactions and checking for completion");
        Transaction[] transactions = DB.getTransaction(null);
        assertTrue("No Transactions found", transactions.length > 0);
        LOGGER.debug(String.format("Found %d transactions", transactions.length));

        boolean test_transaction_found = false;
        for (Transaction t : transactions) {
            assertNotNull(t);
            assertTrue("ID Not defined", t.id != null && t.id.length() > 0);
            assertTrue("Owner Not Defined", t.owner != null);
            assertNotNull("Time not defined", t.time);
            assertTrue("Supervisor Not Defined", t.supervisor != null);
            assertNotNull("Direction not defined", t.direction);

            for (Transaction.Component c : t.components) {
                assertNotNull(c);
                assertNotNull(c.id);
                assertTrue(c.name != null && c.name.length() > 0);
            }

            if (t.equals(TRANSACTION)) test_transaction_found = true;
        }
        assertTrue("Test Transaction not found", test_transaction_found);
    }

    @Test
    public void checkItem() throws Exception {
        LOGGER.info("Checking that Item was updated after Transaction");
        Item[] item = DB.getItem("i.id = " + ITEM.id);
        assertTrue("Test Item Not Found", item.length > 0);
        LOGGER.debug("Item: " + item[0].toString());

        assertEquals(TRANSACTION.id, item[0].lastTransactionID);
        assertEquals(!TEST_TRANSACTION_DIRECTION, item[0].checked_out);
        assertEquals(TEST_ITEM_CONDITION, item[0].comments);
    }
}
