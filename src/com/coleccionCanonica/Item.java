package com.coleccionCanonica;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Item {
    private String simboloIzq;
    private List<String> produccion;
    private int posPunto;

    public Item(List<String> produccion, String simboloIzq, int posPunto){
        this.simboloIzq = simboloIzq;
        this.posPunto = posPunto;
        this.produccion = new ArrayList<>(produccion);
    }

    public String getSiguienteSimbolo(){
        if(this.posPunto < produccion.size()) {
            return produccion.get(posPunto);
        }
        return null;
    }

    public Item avanzarPunto(){
        return new Item(produccion, simboloIzq, posPunto + 1);
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Item item = (Item) obj;
        return posPunto == item.posPunto && Objects.equals(simboloIzq, item.simboloIzq) && Objects.equals(produccion, item.produccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simboloIzq, produccion, posPunto);
    }

    @Override
    public String toString() {
        List<String> prod = new ArrayList<>(produccion);
        prod.add(posPunto, "•");
        return simboloIzq + " → " + String.join("", prod);
    }
}
