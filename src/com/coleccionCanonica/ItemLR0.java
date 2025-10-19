package com.coleccionCanonica;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemLR0 {
    public final Production p;
    public final int dot;

    public ItemLR0(Production p, int dot) { this.p = p; this.dot = dot; }

    public String symbolAfterDot() {
        return dot < p.right.size() ? p.right.get(dot) : null;
    }

    public ItemLR0 advance() { return new ItemLR0(p, dot + 1); }

    @Override public boolean equals(Object o) {
        if (!(o instanceof ItemLR0)) return false;
        ItemLR0 x = (ItemLR0) o;
        return dot == x.dot && p.equals(x.p);
    }

    @Override public int hashCode() { return Objects.hash(p, dot); }

    @Override public String toString() {
        List<String> out = new ArrayList<>(p.right);
        out.add(dot, "•");
        return "[" + p.left + "→" + String.join("", out) + "]";
    }
}
