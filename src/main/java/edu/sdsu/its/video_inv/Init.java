package edu.sdsu.its.video_inv;

import edu.sdsu.its.video_inv.Models.User;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Initialize and Teardown the WebApp and DB
 *
 * @author Tom Paulus
 *         Created on 10/21/2016.
 */
@WebListener
public class Init implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(Init.class);
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private static final String DEFAULT_FIRST_NAME = "Administrator";
    private static final String DEFAULT_LAST_NAME = "User";


    /**
     * Initialize the Webapp with the Default User if no users exist.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        User[] users = DB.getUser("");
        LOGGER.info(String.format("Starting Webapp. Found %d staff in DB", users.length));
        if (users.length == 0) {
            LOGGER.info("No users were found in the DB. Creating default User.");
            User user = new User(DEFAULT_USERNAME, DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME, true);
            user.setPassword(DEFAULT_PASSWORD);
            DB.createUser(user);

            LOGGER.info(String.format("Initial Staff Created.\n " +
                    "Username: \"%s\"\n" +
                    "Password: \"%s\"", DEFAULT_USERNAME, DEFAULT_PASSWORD));
        }
    }

    /**
     * Deregister DB Driver to prevent memory leaks.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // Loop through all drivers
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                // This driver was registered by the webapp's ClassLoader, so deregister it:
                try {
                    LOGGER.info(String.format("Deregistering JDBC driver: %s", driver));
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ex) {
                    LOGGER.fatal(String.format("Error deregistering JDBC driver: %s", driver), ex);
                }
            } else {
                // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                LOGGER.info(String.format("Not deregistering JDBC driver %s as it does not belong to this webapp's ClassLoader", driver));
            }
        }
    }
}