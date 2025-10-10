package com.coleccionCanonica;

import java.util.*;

/** Lógica de cerradura, ir_a y colección canónica (sin numerar el estado de aceptación). */
public class CanonicalLR0 {

    // ---------- cerradura ----------
    public static Set<ItemLR0> closure(Grammar g, Set<ItemLR0> I) {
        Set<ItemLR0> C = new LinkedHashSet<>(I);
        boolean changed = true;
        while (changed) {
            changed = false;
            List<ItemLR0> snapshot = new ArrayList<>(C);
            for (ItemLR0 it : snapshot) {
                String X = it.symbolAfterDot();
                if (X != null && g.N.contains(X)) {
                    for (Production p : g.byLeft.getOrDefault(X, Collections.emptyList())) {
                        ItemLR0 cand = new ItemLR0(p, 0);
                        if (!C.contains(cand)) { C.add(cand); changed = true; }
                    }
                }
            }
        }
        return C;
    }

    // ---------- ir_a ----------
    public static Set<ItemLR0> goTo(Grammar g, Set<ItemLR0> I, String X) {
        Set<ItemLR0> J = new LinkedHashSet<>();
        for (ItemLR0 it : I) {
            if (X.equals(it.symbolAfterDot())) J.add(it.advance());
        }
        return closure(g, J);
    }

    // ---------- colección canónica (NO añade estado por '$') ----------
    public static List<Set<ItemLR0>> canonicalCollection(Grammar g) {
        // I0 = cerradura({ [S'→•α] })  (α es la producción de inicio aumentada)
        Production startProd = g.byLeft.get(g.startPrime).get(0);
        Set<ItemLR0> I0 = closure(g, new LinkedHashSet<>(Collections.singleton(new ItemLR0(startProd, 0))));

        List<Set<ItemLR0>> C = new ArrayList<>();
        C.add(I0);

        boolean changed = true;
        while (changed) {
            changed = false;
            List<Set<ItemLR0>> snapshot = new ArrayList<>(C);
            for (Set<ItemLR0> I : snapshot) {
                // símbolos candidatos tras punto
                Set<String> symbols = new LinkedHashSet<>();
                for (ItemLR0 it : I) {
                    String s = it.symbolAfterDot();
                    if (s != null) symbols.add(s);
                }
                for (String X : symbols) {
                    if ("$".equals(X)) continue; // << no crear estado para aceptación
                    Set<ItemLR0> J = goTo(g, I, X);
                    if (J.isEmpty()) continue;
                    boolean exists = false;
                    for (Set<ItemLR0> K : C) if (K.equals(J)) { exists = true; break; }
                    if (!exists) { C.add(J); changed = true; }
                }
            }
        }
        return C;
    }

    // ---------- helpers de formateo (estilo del profe) ----------
    private static List<String> orderedSymbols(Grammar g, Set<ItemLR0> I) {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        for (ItemLR0 it : I) {
            String x = it.symbolAfterDot();
            if (x != null) s.add(x);
        }
        List<String> out = new ArrayList<>();
        for (String A : g.N) if (s.contains(A)) out.add(A);
        for (String a : g.T) if (s.contains(a)) out.add(a);
        for (String x : s) if (!out.contains(x)) out.add(x);
        return out;
    }

    private static String itemsAsSet(Set<ItemLR0> S) {
        List<String> lines = new ArrayList<>();
        for (ItemLR0 it : S) lines.add(it.toString());
        return "{" + String.join("|", lines) + "}";
    }

    /** Reporte textual como en el ejemplo del profesor. */
    public static String formatReport(Grammar g, List<Set<ItemLR0>> C) {
        StringBuilder out = new StringBuilder();

        // Encabezado y I0
        out.append("cerradura({[").append(g.startPrime).append("→•").append("...]})\n");
        out.append("I0=").append(itemsAsSet(C.get(0))).append("\n\n");

        // Mapa de índices
        Map<Set<ItemLR0>, Integer> idx = new LinkedHashMap<>();
        for (int i = 0; i < C.size(); i++) idx.put(C.get(i), i);

        // Por cada estado, listar Ir_a ordenado (N luego T)
        for (int i = 0; i < C.size(); i++) {
            Set<ItemLR0> I = C.get(i);
            List<String> symbols = orderedSymbols(g, I);

            for (String X : symbols) {
                Set<ItemLR0> J = goTo(g, I, X);

                // ¿Aceptación?
                boolean isAccept = "$".equals(X);
                out.append("Ir_a(I").append(i).append(", ").append(X).append(")=");

                if (isAccept) {
                    out.append("Aceptación");
                } else {
                    out.append("cerradura(").append(itemsAsSet(J)).append(")");
                    // número de estado si existe
                    Integer j = idx.get(J);
                    if (j == null) {
                        for (int k = 0; k < C.size(); k++) if (C.get(k).equals(J)) { j = k; break; }
                    }
                    if (j != null) out.append(" =I").append(j);
                }
                out.append("\n");
            }
            out.append("\n");
        }
        return out.toString();
    }
}
