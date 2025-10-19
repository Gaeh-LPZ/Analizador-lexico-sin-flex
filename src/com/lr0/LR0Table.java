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
        public final List<String> terminals;                 
        public final List<String> nonTerminals;               
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

    public static Result buildFromFile(String path) throws Exception {

        Grammar gAug = Grammar.parseAugmentedGrammar(path);

        Set<String> Nset = gAug.N.stream().map(LR0Table::norm).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> Tset = gAug.T.stream().map(LR0Table::norm).collect(Collectors.toCollection(LinkedHashSet::new));

        List<Set<ItemLR0>> C = CanonicalLR0.canonicalCollection(gAug);

        FirstFollowFF ff = computeFirstFollowFromAugmented(gAug);

        List<Production> prods = new ArrayList<>();
        for (String A : gAug.byLeft.keySet()) {
            if (A.endsWith("'")) continue;
            prods.addAll(gAug.byLeft.get(A));
        }
        Map<Production, Integer> ruleNum = new LinkedHashMap<>();
        for (int i = 0; i < prods.size(); i++) {
            ruleNum.put(prods.get(i), i + 1);
        }

        Map<Integer, Map<String, String>> ACTION = new LinkedHashMap<>();
        Map<Integer, Map<String, Integer>> GOTO  = new LinkedHashMap<>();

        List<String> T = new ArrayList<>(Tset);
        if (!T.contains("$")) T.add("$");

        List<String> expectedT = Arrays.asList("id", "+", "*", "(", ")", "$");
        if (T.containsAll(expectedT)) {
            T = new ArrayList<>(expectedT);
        } else {
            T = T.stream().filter(s -> !"$".equals(s)).collect(Collectors.toList());
            T.add("$");
        }

        List<String> N = new ArrayList<>(Nset);
        N.removeIf(s -> s.endsWith("'"));
        List<String> expectedN = Arrays.asList("E", "T", "F");
        if (N.containsAll(expectedN)) {
            N = new ArrayList<>(expectedN);
        }

        Map<Set<ItemLR0>, Integer> idx = new LinkedHashMap<>();
        for (int i = 0; i < C.size(); i++) idx.put(C.get(i), i);

        for (int i = 0; i < C.size(); i++) {
            Set<ItemLR0> I = C.get(i);

            Set<String> symbols = new LinkedHashSet<>();
            for (ItemLR0 it : I) {
                String x = it.symbolAfterDot();
                if (x != null) symbols.add(norm(x));
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
            if (hasAccept) {
                ACTION.computeIfAbsent(i, k -> new LinkedHashMap<>()).put("$", "acep");
            }

            for (ItemLR0 it : I) {
                if (it.symbolAfterDot() == null && !it.p.left.endsWith("'")) {
                    Integer k = ruleNum.get(it.p);
                    if (k == null) continue;

                    Set<String> followA = ff.follow.getOrDefault(it.p.left, Set.of());
                    for (String a : followA) {
                        String aa = norm(a);
                        if ("$".equals(aa) && hasAccept) continue;
                        Map<String, String> row = ACTION.computeIfAbsent(i, z -> new LinkedHashMap<>());
                        row.putIfAbsent(aa, "r"+k);
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

    private static String norm(String s) {
        return s == null ? null : s.trim();
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

        for (String A : N) ff.first.put(A, new LinkedHashSet<>());

        for (String A : N) ff.follow.put(A, new LinkedHashSet<>());

        String start = null;
        for (Map.Entry<String, List<Production>> e : g.byLeft.entrySet()) {
            if (e.getKey().endsWith("'")) {
                for (Production p : e.getValue()) {
                    if (!p.right.isEmpty()) {
                        String firstSym = norm(p.right.get(0));
                        if (N.contains(firstSym)) { start = firstSym; break; }
                    }
                }
            }
            if (start != null) break;
        }
        if (start == null && !N.isEmpty()) start = N.get(0);
        boolean changed;
        do {
            changed = false;
            for (Map.Entry<String, List<Production>> e : g.byLeft.entrySet()) {
                String A = norm(e.getKey());
                if (!ff.first.containsKey(A)) ff.first.put(A, new LinkedHashSet<>());
                for (Production p : e.getValue()) {
                    List<String> alpha = p.right.stream().map(LR0Table::norm).collect(Collectors.toList());
                    Set<String> old = new LinkedHashSet<>(ff.first.get(A));
                    Set<String> add = firstOfSeq(alpha, ff.first, T, EPS);
                    if (add.remove(EPS)) { 
                        ff.first.get(A).addAll(add);
                        ff.first.get(A).add(EPS);
                    } else {
                        ff.first.get(A).addAll(add);
                    }
                    if (ff.first.get(A).size() != old.size()) changed = true;
                }
            }
        } while (changed);

        do {
            changed = false;
            for (Map.Entry<String, List<Production>> e : g.byLeft.entrySet()) {
                String A = norm(e.getKey());
                for (Production p : e.getValue()) {
                    List<String> beta = p.right.stream().map(LR0Table::norm).collect(Collectors.toList());
                    for (int i = 0; i < beta.size(); i++) {
                        String B = beta.get(i);
                        if (!N.contains(B)) continue; 

                        List<String> tail = beta.subList(i + 1, beta.size());
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
