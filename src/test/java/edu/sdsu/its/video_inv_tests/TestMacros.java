package edu.sdsu.its.video_inv_tests;

import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.Macro;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * Test Macros and their functionality.
 * Macros represent multiple items with a single ID, which is useful for pre-packaged kits.
 *
 * @author Tom Paulus
 *         Created on 8/5/16.
 */
public class TestMacros {
    private static final Logger LOGGER = Logger.getLogger(TestMacros.class);

    private static final String TEST_MACRO_NAME = "Test Macro";
    private static final String UPDATE_MACRO_NAME = "Same Macro, New Name";
    private static final Integer[] TEST_MACRO_ITEMS = {1};
    private static final Integer[] UPDATE_MACRO_ITEMS = {2};
    private static final int TEST_MACRO_ID = 999901;
    private static Macro TEST_MACRO;

    @BeforeClass
    public static void setUp() throws Exception {
        LOGGER.info(String.format("Creating new Macro (ID: %d; Name: %s; Items: %s)", TEST_MACRO_ID, TEST_MACRO_NAME, Arrays.toString(TEST_MACRO_ITEMS)));
        TEST_MACRO = new Macro(TEST_MACRO_ID, TEST_MACRO_NAME, TEST_MACRO_ITEMS);
        DB.createMacro(TEST_MACRO);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LOGGER.info("Deleting Test Macro - ID: " + TEST_MACRO.id);
        DB.deleteMacro(TEST_MACRO);
    }

    @Test
    public void getMacros() throws Exception {
        Macro[] macros = DB.getMacro(null);
        assertTrue("No Macros Found", macros.length > 0);
        LOGGER.debug(String.format("%d macros found in DB", macros.length));
        boolean foundTestMacro = false;
        for (Macro m : macros) {
            assertTrue("Macro not defined", m != null);
            assertTrue("Macro not Complete - Name Missing", m.name != null && m.name.length() > 0);
            assertTrue("Macro not Complete - Items Missing", m.items != null && m.items.length > 0);
            if (m.equals(TEST_MACRO)) foundTestMacro = true;
        }
        assertTrue("Test Macro could not be found in list", foundTestMacro);
    }

    @Test
    public void updateMacro() throws Exception {
        LOGGER.debug("Current Macro = " + TEST_MACRO.toString());
        TEST_MACRO.name = UPDATE_MACRO_NAME;
        TEST_MACRO.items = UPDATE_MACRO_ITEMS;
        LOGGER.debug("Updating Macro to... " + TEST_MACRO.toString());
        LOGGER.info("Updating Macro");
        DB.updateMacro(TEST_MACRO);
        assertTrue("Update Not Successful", TEST_MACRO.equals(DB.getMacro("id = " + TEST_MACRO.id)[0]));

    }
}
