package edu.sdsu.its.video_inv_tests;

import edu.sdsu.its.video_inv.API.Quote;
import edu.sdsu.its.video_inv.DB;
import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test Quotes Functionality.
 * At least one quote must be defined.
 *
 * @author Tom Paulus
 *         Created on 8/5/16.
 */
public class TestQuotes {
    private static final Logger LOGGER = Logger.getLogger(TestQuotes.class);
    private static final int QUOTE_NUM_TO_FETCH = 1;

    @Test
    public void numQuotes() throws Exception {
        int numQuotes = DB.getNumQuotes();
        LOGGER.debug(String.format("Found %d quotes in DB", numQuotes));
        assertTrue("No Quotes found in DB", numQuotes > 0);
    }

    @Test
    public void randomQuote() throws Exception {
        LOGGER.info(String.format("Retrieving quote #%d from DB", QUOTE_NUM_TO_FETCH));
        Quote.QuoteModel quote = DB.getQuote(QUOTE_NUM_TO_FETCH);
        assertNotNull(quote);
        LOGGER.debug("Retrieved Quote from DB");
        LOGGER.debug("Quote.author = " + quote.author);
        LOGGER.debug("Quote.text = " + quote.text);

        assertTrue("Quote has no author", quote.author != null && quote.author.length() > 0);
        assertTrue("Quote has no text", quote.text != null && quote.text.length() > 0);
    }
}
