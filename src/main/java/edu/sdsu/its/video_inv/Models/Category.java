package edu.sdsu.its.video_inv.Models;

/**
 * Models an Item Category.
 * Items can have at most one category.
 *
 * @author Tom Paulus
 *         Created on 7/27/16.
 */
public class Category {
    public Integer id;
    public String name;

    public Category() {
        id = null;
        name = null;
    }

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return id.equals(category.id) && name.equals(category.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
