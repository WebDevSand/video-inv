package edu.sdsu.its.video_inv.Models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Macros represent a group of multiple Items and are returned as a JSON Array of itemIDs.
 *
 * @author Tom Paulus
 *         Created on 5/11/16.
 */
public class Macro {
    public int id;
    public String name;
    public Integer[] items;

    public Macro(int id, String name, String items) {
        this.id = id;
        this.name = name;
        List<Integer> list = new ArrayList<>();
        for (String i : items
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "")
                .split(",")) {
            if (i.length() > 0) list.add(Integer.parseInt(i));
        }
        this.items = list.toArray(new Integer[]{});
    }

    public Macro(int id, String name, Integer[] items) {
        this.id = id;
        this.name = name;
        this.items = items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Macro macro = (Macro) o;

        return id == macro.id && name.equals(macro.name);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Macro{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", items=" + Arrays.toString(items) +
                '}';
    }
}
