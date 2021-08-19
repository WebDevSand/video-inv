package edu.sdsu.its.video_inv.Models;

import edu.sdsu.its.video_inv.DB;

/**
 * Models an Item in the DB
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class Item {
    public int id;
    public int pubID;
    public Category category = new Category();

    public String name;
    public String shortName;
    public String comments;
    public String assetID;

    public boolean checked_out;
    public String lastTransactionDate;
    public String lastTransactionID;

    public Item() {
    }

    public Item(int id, int pubID, Category category, String name, String shortName, String comments, String assetID, boolean checked_out) {
        this.id = id;
        this.pubID = pubID;
        this.category = category;
        this.name = name;
        this.shortName = shortName;
        this.comments = comments;
        this.assetID = assetID;
        this.checked_out = checked_out;
    }

    public Item(int pubID, String name, String shortName) {
        this.pubID = pubID;
        this.name = name;
        this.shortName = shortName;
    }

    public void completeItem() {
        Item item = null;
        if (this.id == 0 && this.pubID != 0) {
            item = DB.getItem("i.pub_id = " + pubID)[0];
            this.id = item.id;
        } else if (this.id != 0) {
            item = DB.getItem("i.id = " + this.id)[0];
            this.pubID = item.pubID;
        }
        if (this.name == null) {
            if (item == null) {
                item = DB.getItem("id = " + this.id)[0];
            }
            if (item != null) {
                this.name = item.name;
            }
        }
        if (this.comments == null) {
            if (item == null) {
                item = DB.getItem("id = " + this.id)[0];
            }
            if (item != null) {
                this.comments = item.comments;
            }
        }
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", pubID=" + pubID +
                ", category=" + category +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", comments='" + comments + '\'' +
                ", checked_out=" + checked_out +
                ", lastTransactionDate='" + lastTransactionDate + '\'' +
                ", lastTransactionID='" + lastTransactionID + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (id != item.id) return false;
        if (pubID != item.pubID) return false;
        if (checked_out != item.checked_out) return false;
        if (!name.equals(item.name)) return false;
        if (assetID != null && !assetID.isEmpty() ? !assetID.equals(item.assetID) : item.assetID != null && !item.assetID.isEmpty()) return false;
        if (shortName != null ? !shortName.equals(item.shortName) : item.shortName != null) return false;
        return comments != null ? comments.equals(item.comments) : item.comments == null;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public static class Popular extends Item {
        public int frequency;

        public Popular(int id, int pubID, Category category, String name, String shortName,
                       String comments, String assetID, boolean checked_out, int frequency) {
            super(id, pubID, category, name, shortName, comments, assetID, checked_out);
            this.frequency = frequency;
        }
    }
}
