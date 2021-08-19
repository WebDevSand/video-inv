package edu.sdsu.its.video_inv_tests;

import edu.sdsu.its.video_inv.DB;
import edu.sdsu.its.video_inv.Models.Category;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test Categories and their functionality.
 *
 * @author Tom Paulus
 *         Created on 8/5/16.
 */
public class TestCategories {
    private static final Logger LOGGER = Logger.getLogger(TestCategories.class);

    private static final String TEST_CATEGORY_NAME = "Test Category";
    private static final String UPDATE_CATEGORY_NAME = "Same Category, Different Name";
    private static Category category;

    @BeforeClass
    public static void setUp() throws Exception {
        LOGGER.info("Creating new Category");
        category = new Category(TEST_CATEGORY_NAME);
        LOGGER.debug("Created new Category: " + category.toString());
        category = DB.createCategory(category);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LOGGER.warn(String.format("Deleting Category (id: %d; Name: %s)", category.id, category.name));
        DB.deleteCategory(category);
        LOGGER.debug("Category Deleted");
    }

    @Test
    public void getCategories() throws Exception {
        Category[] fetchCategories = DB.getCategory(null);
        assertTrue("No Categories in DB", fetchCategories.length > 0);
        LOGGER.debug(String.format("Retrieved %d categories from DB", fetchCategories.length));
        boolean test_category_found = false;
        for (Category c : fetchCategories) {
            assertTrue("Category has no ID", c.id != 0);
            assertTrue("Category has no name", c.name != null && c.name.length() > 0);
            if (c.equals(category)) test_category_found = true;
        }
        assertTrue("Test Category could not be found in list", test_category_found);
    }

    @Test
    public void updateCategory() throws Exception {
        LOGGER.info("Updating Category Name");
        LOGGER.debug("Current Category: " + category.toString());
        category.name = UPDATE_CATEGORY_NAME;
        DB.updateCategory(category);
        LOGGER.debug("Updated Category: " + category.toString());
        assertEquals(category, DB.getCategory(category.id)[0]);
    }

}
