package edu.sdsu.its.video_inv_tests;

import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.Category;
import edu.sdsu.its.video_inv.Models.Item;
import edu.sdsu.its.video_inv.Models.Macro;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Test Items and their functionality.
 *
 * @author Tom Paulus
 *         Created on 8/5/16.
 */
public class TestItems {
    private static final Logger LOGGER = Logger.getLogger(TestItems.class);

    private static final int TEST_ITEM_1_PUBID = 999951;
    private static final String TEST_ITEM_1_NAME = "Test Item 1";
    private static final String TEST_ITEM_1_SHORT_NAME = "IT 1";
    private static final String UPDATE_ITEM_1_NAME = "Updated Item 1";
    private static final String UPDATE_ITEM_1_SHORT_NAME = "IT.U 1";

    private static final int TEST_ITEM_2_PUBID = 999952;
    private static final String TEST_ITEM_2_NAME = "Test Item 2";

    private static final String TEST_CATEGORY_NAME = "Test Category";

    private static final int TEST_MACRO_ID = 999900;
    private static final String TEST_MACRO_NAME = "Test Macro";

    private static Item item1;
    private static Item item2;

    private static Category category;
    private static Macro macro;

    @BeforeClass
    public static void setUp() throws Exception {
        LOGGER.info("Creating Test Item #1");
        item1 = new Item(TEST_ITEM_1_PUBID, TEST_ITEM_1_NAME, TEST_ITEM_1_SHORT_NAME);
        item1.assetID = "ti-" + UUID.randomUUID().toString();
        DB.createItem(item1);

        item1.completeItem();
        LOGGER.debug("Created Test Item #1: " + item1.toString());

        LOGGER.info("Creating Test Item #2");
        item2 = new Item(TEST_ITEM_2_PUBID, TEST_ITEM_2_NAME, "");
        DB.createItem(item2);

        item2.completeItem();
        LOGGER.debug("Created Test Item #2: " + item2.toString());

        LOGGER.info("Creating Test Category");
        category = new Category(TEST_CATEGORY_NAME);
        category = DB.createCategory(category);
        LOGGER.debug("Created Test Category: " + category.toString());

        LOGGER.info("Creating Test Macro");
        macro = new Macro(TEST_MACRO_ID, TEST_MACRO_NAME, new Integer[]{item1.id, item2.id});
        DB.createMacro(macro);
        LOGGER.debug("Created Test Macro: " + macro.toString());
    }


    @AfterClass
    public static void tearDown() throws Exception {
        LOGGER.warn(String.format("Deleting Test Items (ID: %d/%d, %d/%d)", item1.id, item1.pubID, item2.id, item2.pubID));
        DB.deleteItem(item1);
        DB.deleteItem(item2);

        LOGGER.warn(String.format("Deleting Test Category (ID: %d, Name: %s)", category.id, category.name));
        LOGGER.debug(String.format("Removing Categories from Test Items. Previously: %s, %s", item1.category.name != null ? item1.category.name : "", item2.category.name != null ? item2.category.name : ""));
        item1.category.id = item2.category.id = null;
        DB.updateItem(item1);
        DB.updateItem(item2);


        DB.deleteCategory(category);

        LOGGER.warn("Deleting Test Macro");
        DB.deleteMacro(macro);
    }

    @Test
    public void getItems() throws Exception {
        LOGGER.info("Getting Items and checking for completion");
        Item[] items = DB.getItem(null);
        LOGGER.debug(String.format("Retrieved %d items from DB", items.length));
        boolean test_item1_found = false;
        boolean test_item2_found = false;
        for (Item i : items) {
            assertNotNull("Item not defined", i);
            assertTrue("Item not Complete - Name Missing", i.name != null && i.name.length() > 0);
            assertTrue("Item not Complete - DB ID Missing", i.id != 0);
            assertTrue("Item not Complete - Public ID Missing", i.pubID != 0);
            if (i.pubID == item1.pubID) test_item1_found = true;
            if (i.pubID == item2.pubID) test_item2_found = true;
        }
        assertTrue("Test Item 1 not found in DB", test_item1_found);
        assertTrue("Test Item 2 not found in DB", test_item2_found);
    }

    @Test
    public void getItemsViaMacro() throws Exception {
        LOGGER.info(String.format("Getting a group of items via a Macro (M.ID: %d)", TEST_MACRO_ID));
        Item[] macroItems = DB.getItem("m.id = " + TEST_MACRO_ID);
        assertTrue("Macro has no items", macroItems.length > 0);
        LOGGER.debug(String.format("Macro retrieved %d items", macroItems.length));
        boolean test_item1_found = false;
        boolean test_item2_found = false;
        for (Item i : macroItems) {
            assertNotNull("Item not defined", i);
            assertTrue("Item not Complete - Name Missing", i.name != null && i.name.length() > 0);
            assertTrue("Item not Complete - DB ID Missing", i.id != 0);
            assertTrue("Item not Complete - Public ID Missing", i.pubID != 0);
            if (i.pubID == item1.pubID) test_item1_found = true;
            if (i.pubID == item2.pubID) test_item2_found = true;
        }
        assertTrue("Test Item 1 not part of Macro", test_item1_found);
        assertTrue("Test Item 2 not part of Macro", test_item2_found);
    }

    @Test
    public void updateItem() throws Exception {
        LOGGER.info("Updating Item Name & Short Name");
        LOGGER.debug("Current Item: " + item1.toString());
        item1.name = UPDATE_ITEM_1_NAME;
        item1.shortName = UPDATE_ITEM_1_SHORT_NAME;
        item1.assetID = "ti-" + UUID.randomUUID().toString();
        DB.updateItem(item1);
        LOGGER.debug("Updated Item: " + item1.toString());

        assertEquals(item1, DB.getItem("i.id = " + item1.id)[0]);

    }

    @Test
    public void itemCategory() throws Exception {
        LOGGER.info("Adding Item1 to Test Category");
        item1.category.id = category.id;
        LOGGER.debug("Updated Item: " + item1.toString());
        DB.updateItem(item1);

        assertEquals(item1, DB.getItem("i.id = " + item1.id)[0]);

        LOGGER.info("Removing Item1 to Test Category");
        item1.category.id = null;
        item1.category.name = null;
        LOGGER.debug("Updated Item: " + item1.toString());
        DB.updateItem(item1);

        assertEquals(item1, DB.getItem("i.id = " + item1.id)[0]);

    }

    @Test
    public void itemAssetID() throws Exception {
        assertNotNull("Test Item #1 has not Asset ID defined", item1.assetID);
        assertTrue("AssetID was not included in Create",
                item1.assetID.equals(DB.getItem("i.pub_id = " + item1.pubID)[0].assetID));
    }
}
