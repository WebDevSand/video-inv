package edu.sdsu.its.video_inv.Models;

import com.google.gson.annotations.Expose;
import edu.sdsu.its.video_inv.DB;

/**
 * Models a User of the Inventory System
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class User {
    @Expose
    public int dbID;

    @Expose
    public String username;

    @Expose
    public String firstName;
    @Expose
    public String lastName;

    @Expose
    public Boolean supervisor;

    // Should be Base64 encoded in transit!
    @Expose(serialize = false)
    private String password;

    public User(int dbID, String username, String firstName, String lastName, boolean supervisor) {
        this.dbID = dbID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.supervisor = supervisor;
    }

    public User(String username, String firstName, String lastName, boolean supervisor) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.supervisor = supervisor;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void completeUser() {
        User user = null;
        if (this.dbID == 0 && this.username.length() != 0) {
            user = DB.getUser("username = '" + username + "'")[0];
            this.dbID = user.dbID;
        } else if (this.dbID != 0) {
            user = DB.getUser("id = " + this.dbID)[0];
            this.username = user.username;
        }
        if (this.firstName == null) {
            if (user == null) {
                user = DB.getUser("id = " + this.dbID)[0];
            }
            if (user != null) {
                this.firstName = user.firstName;
            }
        }
        if (this.lastName == null) {
            if (user == null) {
                user = DB.getUser("id = " + this.dbID)[0];
            }
            if (user != null) {
                this.lastName = user.lastName;
            }
        }
        if (this.supervisor == null) {
            if (user == null) {
                user = DB.getUser("id = " + this.dbID)[0];
            }
            if (user != null) {
                this.supervisor = user.supervisor;
            }
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "dbID=" + dbID +
                ", username='" + username + '\''+
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", supervisor=" + supervisor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (dbID != user.dbID && !username.equals(user.username)) return false;
        if (!firstName.equals(user.firstName)) return false;
        if (!lastName.equals(user.lastName)) return false;
        return supervisor != null ? supervisor.equals(user.supervisor) : user.supervisor == null;
    }

    @Override
    public int hashCode() {
        return dbID;
    }

    public User login() {
        return DB.login(this.username, this.password);
    }
}
