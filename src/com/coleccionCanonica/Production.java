package com.coleccionCanonica;

import java.util.List;
import java.util.Objects;

public class Production {
    public final String left;
    public final List<String> right;

    public Production(String left, List<String> right) {
        this.left = left;
        this.right = right;
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Production)) return false;
        Production p = (Production) o;
        return Objects.equals(left, p.left) && Objects.equals(right, p.right);
    }

    @Override public int hashCode() { return Objects.hash(left, right); }

    @Override public String toString() {
        return left + "->" + String.join(" ", right);
    }
}
