package com.lr0;

import com.coleccionCanonica.CanonicalLR0;
import com.coleccionCanonica.Grammar;
import com.coleccionCanonica.ItemLR0;
import com.coleccionCanonica.Production;
import java.util.*;
import java.util.stream.Collectors;

public final class LR0Table {

    private LR0Table() {}

    public static final class Result {
        public final int states;
        public final List<String> terminals;                  // columnas ACCION
        public final List<String> nonTerminals;               // columnas Ir_a
        public final Map<Integer, Map<String, String>> action;
        public final Map<Integer, Map<String, Integer>> gotoTable;

        public Result(int states,
                      List<String> terminals,
                      List<String> nonTerminals,
                      Map<Integer, Map<String, String>> action,
                      Map<Integer, Map<String, Integer>> gotoTable) {
            this.states = states;
            this.terminals = terminals;
            this.nonTerminals = nonTerminals;
            this.action = action;
            this.gotoTable = gotoTable;
        }
    }

    private static String norm(String s) { return s == null ? null : s.trim(); }
    private static boolean isEpsilonSymbol(String s) {
        if (s == null) return false;
        String t = s.trim();
        return t.equals("ε") || t.equalsIgnoreCase("epsilon");
    }
    private static boolean isEpsilonProduction(Production p) {
        return p.right != null && p.right.size() == 1 && isEpsilonSymbol(p.right.get(0));
    }

    /** Construye la tabla desde un archivo de gramática aumentada (con S'→S $). */
    public static Result buildFromFile(String path) throws Exception {

        // 1) Gramática aumentada
        Grammar gAug = Grammar.parseAugmentedGrammar(path);

        // Normalización de conjuntos
        Set<String> Nset = gAug.N.stream().map(LR0Table::norm).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> Tset = gAug.T.stream().map(LR0Table::norm).collect(Collectors.toCollection(LinkedHashSet::new));

        // 2) Colección canónica
        List<Set<ItemLR0>> C = CanonicalLR0.canonicalCollection(gAug);

        // 3) FIRST/FOLLOW desde la gramática aumentada
        FirstFollowFF ff = computeFirstFollowFromAugmented(gAug);

        // 4) Numeración de reglas (sin S')
        List<Production> prods = new ArrayList<>();
        for (String A : gAug.byLeft.keySet()) {
            if (A.endsWith("'")) continue;
            prods.addAll(gAug.byLeft.get(A));
        }
        Map<Production, Integer> ruleNum = new LinkedHashMap<>();
        for (int i = 0; i < prods.size(); i++) ruleNum.put(prods.get(i), i + 1);

        // 5) Tablas
        Map<Integer, Map<String, String>> ACTION = new LinkedHashMap<>();
        Map<Integer, Map<String, Integer>> GOTO  = new LinkedHashMap<>();

        // ===== Columnas: conservar TODOS los terminales; reordenar sin perder =====
        List<String> T = new ArrayList<>(new LinkedHashSet<>(Tset));
        T.replaceAll(s -> s.trim());
        T.remove("$"); T.add("$");

        List<String> expT = Arrays.asList("id", "+", "*", "(", ")", "$");
        if (new HashSet<>(T).containsAll(expT)) {
            LinkedHashSet<String> ordered = new LinkedHashSet<>();
            for (String s : Arrays.asList("id", "+", "*", "(", ")"))
                if (T.contains(s)) ordered.add(s);
            for (String s : T) if (!"$".equals(s) && !ordered.contains(s)) ordered.add(s);
            ordered.add("$");
            T = new ArrayList<>(ordered);
        }

        List<String> N = new ArrayList<>(new LinkedHashSet<>(Nset));
        N.replaceAll(s -> s.trim());
        N.removeIf(s -> s.endsWith("'"));
        List<String> expN = Arrays.asList("E", "T", "F");
        if (N.containsAll(expN)) {
            LinkedHashSet<String> orderedN = new LinkedHashSet<>();
            for (String s : expN) if (N.contains(s)) orderedN.add(s);
            for (String s : N) if (!orderedN.contains(s)) orderedN.add(s);
            N = new ArrayList<>(orderedN);
        }

        Map<Set<ItemLR0>, Integer> idx = new LinkedHashMap<>();
        for (int i = 0; i < C.size(); i++) idx.put(C.get(i), i);

        for (int i = 0; i < C.size(); i++) {
            Set<ItemLR0> I = C.get(i);

            Set<String> symbols = new LinkedHashSet<>();
            for (ItemLR0 it : I) {
                String x = norm(it.symbolAfterDot());
                if (x != null && !isEpsilonSymbol(x)) symbols.add(x);
            }

            for (String a : symbols) {
                if (T.contains(a)) {
                    Set<ItemLR0> J = CanonicalLR0.goTo(gAug, I, a);
                    Integer j = idx.get(J);
                    if (j == null) for (int k = 0; k < C.size(); k++) if (C.get(k).equals(J)) { j = k; break; }
                    if (j != null) ACTION.computeIfAbsent(i, k -> new LinkedHashMap<>()).put(a, "d"+j);
                }
            }

            boolean hasAccept = I.stream().anyMatch(it ->
                    "$".equals(norm(it.symbolAfterDot())) &&
                    (it.dot + 1) == it.p.right.size() &&
                    it.p.left.endsWith("'"));
            if (hasAccept) ACTION.computeIfAbsent(i, k -> new LinkedHashMap<>()).put("$", "acep");

            for (ItemLR0 it : I) {
                boolean endOfRule = (it.symbolAfterDot() == null);
                boolean epsReady  = isEpsilonProduction(it.p) && it.dot == 0;
                if ((endOfRule || epsReady) && !it.p.left.endsWith("'")) {
                    Integer k = ruleNum.get(it.p);
                    if (k == null) continue;

                    Set<String> followA = ff.follow.getOrDefault(it.p.left, Set.of());
                    boolean acceptHere = hasAccept;
                    for (String a : followA) {
                        String aa = norm(a);
                        if (!T.contains(aa)) continue;
                        if ("$".equals(aa) && acceptHere) continue;
                        ACTION.computeIfAbsent(i, z -> new LinkedHashMap<>()).putIfAbsent(aa, "r"+k);
                    }
                }
            }

            for (String A : symbols) {
                if (N.contains(A)) {
                    Set<ItemLR0> J = CanonicalLR0.goTo(gAug, I, A);
                    Integer j = idx.get(J);
                    if (j == null) for (int k = 0; k < C.size(); k++) if (C.get(k).equals(J)) { j = k; break; }
                    if (j != null) GOTO.computeIfAbsent(i, k -> new LinkedHashMap<>()).put(A, j);
                }
            }
        }

        return new Result(C.size(), T, N, ACTION, GOTO);
    }

    private static final class FirstFollowFF {
        final Map<String, Set<String>> first = new LinkedHashMap<>();
        final Map<String, Set<String>> follow = new LinkedHashMap<>();
    }

    private static FirstFollowFF computeFirstFollowFromAugmented(Grammar g) {
        FirstFollowFF ff = new FirstFollowFF();

        List<String> N = g.N.stream().map(LR0Table::norm).collect(Collectors.toList());
        List<String> T = g.T.stream().map(LR0Table::norm).collect(Collectors.toList());
        final String EPS = "ε";

        for (String A : N) { ff.first.put(A, new LinkedHashSet<>()); ff.follow.put(A, new LinkedHashSet<>()); }

        boolean changed;
        do {
            changed = false;
            for (Map.Entry<String, List<Production>> e : g.byLeft.entrySet()) {
                String A = norm(e.getKey());
                for (Production p : e.getValue()) {
                    List<String> alpha = p.right.stream().map(LR0Table::norm).collect(Collectors.toList());
                    Set<String> before = new LinkedHashSet<>(ff.first.get(A));
                    Set<String> add = firstOfSeq(alpha, ff.first, T, EPS);
                    if (add.remove(EPS)) { ff.first.get(A).addAll(add); ff.first.get(A).add(EPS); }
                    else ff.first.get(A).addAll(add);
                    if (ff.first.get(A).size() != before.size()) changed = true;
                }
            }
        } while (changed);

        do {
            changed = false;
            for (Map.Entry<String, List<Production>> e : g.byLeft.entrySet()) {
                String A = norm(e.getKey());
                for (Production p : e.getValue()) {
                    List<String> beta = p.right.stream().map(LR0Table::norm).collect(Collectors.toList());
                    int m = beta.size();
                    for (int i = 0; i < m; i++) {
                        String B = beta.get(i);
                        if (!N.contains(B)) continue;

                        if (i + 1 < m) {
                            String next = beta.get(i + 1);
                            if (!N.contains(next) && !isEpsilonSymbol(next)) {
                                if (ff.follow.get(B).add(next)) changed = true;
                            }
                        }

                        List<String> tail = beta.subList(i + 1, m);
                        Set<String> firstTail = firstOfSeq(tail, ff.first, T, EPS);
                        Set<String> add1 = new LinkedHashSet<>(firstTail);
                        add1.remove(EPS);
                        if (!add1.isEmpty()) {
                            if (ff.follow.get(B).addAll(add1)) changed = true;
                        }
                        if (tail.isEmpty() || firstTail.contains(EPS)) {
                            if (ff.follow.get(B).addAll(ff.follow.get(A))) changed = true;
                        }
                    }
                }
            }
        } while (changed);

        return ff;
    }
    private static Set<String> firstOfSeq(List<String> seq,
                                          Map<String, Set<String>> firstNT,
                                          List<String> terminals,
                                          String EPS) {
        Set<String> out = new LinkedHashSet<>();
        if (seq.isEmpty()) { out.add(EPS); return out; }

        for (String s : seq) {
            if (s == null || s.isEmpty()) { out.add(EPS); break; }
            if (isEpsilonSymbol(s)) { out.add(EPS); continue; }
            if (firstNT.containsKey(s)) {
                Set<String> fs = firstNT.getOrDefault(s, Set.of());
                out.addAll(fs.stream().filter(x -> !x.equals(EPS)).collect(Collectors.toList()));
                if (!fs.contains(EPS)) return out;
            } else {
                out.add(s);
                return out;
            }
        }
        out.add(EPS);
        return out;
    }
}
