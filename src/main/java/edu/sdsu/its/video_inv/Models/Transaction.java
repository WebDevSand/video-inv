package edu.sdsu.its.video_inv.Models;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Models a Checkout Transaction
 *
 * @author Tom Paulus
 *         Created on 2/23/16.
 */
public class Transaction {
    public String id;
    public User owner;
    public User supervisor;
    public Timestamp time;
    public boolean direction; // 0 for out; 1 for in
    public List<Component> components;

    public Transaction(String id, User owner, User supervisor, Timestamp time, boolean direction) {
        this.id = id;
        this.owner = owner;
        this.supervisor = supervisor;
        this.time = time;
        this.direction = direction;
        this.components = new ArrayList<>();
    }

    public Transaction(String id, User owner, User supervisor, boolean direction, List<Component> components) {
        this.id = id;
        this.owner = owner;
        this.supervisor = supervisor;
        this.direction = direction;
        this.components = components;
    }

    public static class Component {
       public int id;
        public int pubID;
        public Category category;

        public String name;
        public String assetID;
        public String comments;

        public Component(int id, int pubID, Category category, String name, String assetID, String comments) {
            this.id = id;
            this.pubID = pubID;
            this.category = category;
            this.name = name;
            this.assetID = assetID;
            this.comments = comments;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Component component = (Component) o;

            if (id != component.id) return false;
            if (pubID != component.pubID) return false;
            if (!name.equals(component.name)) return false;
            return comments != null ? comments.equals(component.comments) : component.comments == null;

        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", owner=" + owner +
                ", supervisor=" + supervisor +
                ", time=" + time +
                ", direction=" + direction +
                ", components=" + components +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        if (direction != that.direction) return false;
        if (!id.equals(that.id)) return false;
        if (!owner.equals(that.owner)) return false;
        if (!supervisor.equals(that.supervisor)) return false;
        return components.equals(that.components);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + owner.hashCode();
        return result;
    }
}
