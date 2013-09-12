/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package lambda.hackathon;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

// Anagram inspired form Joe Bowbeer's example
//   https://bitbucket.org/joebowbeer/anagrams/
public class AnagramAndHistogram {

    static final String INTO_WORDS = "[- @/.,:;_'\"?!()]+"; // regexp for splitting

    public static void main(String[] args) throws IOException {
        Path filename = Paths.get("target/JaneAusten-PrideAndPrejudice.txt");

        Imperative.main(filename);
//        Lambda.main(filename);
    }

    static class Imperative {

        static void main(Path filename) throws IOException {
            List<Set<String>> allAnagrams = anagrams(filename);
            for (Set<String> anagrams : allAnagrams) {
                System.out.println(anagrams);
            }

            Map<Integer, List<String>> h = histogram(filename);
            for (Map.Entry<Integer, List<String>> e : h.entrySet()) {
                System.out.println(e.getKey() + " -> " + e.getValue());
            }
        }

        /**
         * Find all the anagrams in a text file
         */
        static List<Set<String>> anagrams(Path filename) throws IOException {
            try (BufferedReader br =
                         Files.newBufferedReader(filename, StandardCharsets.UTF_8)) {
                return anagrams(br);
            }
        }

        static List<Set<String>> anagrams(BufferedReader br) throws IOException {
            Map<String, Set<String>> m = new HashMap<>();
            String line;

            while ((line = br.readLine()) != null) {
                line = line.toLowerCase();
                for (String w : line.split(INTO_WORDS)) {
                    String anagramKey = key(w);

                    Set<String> anagramsForKey = m.get(anagramKey);
                    if (anagramsForKey == null) {
                        anagramsForKey = new HashSet<>();
                        m.put(anagramKey, anagramsForKey);
                    }
                    anagramsForKey.add(w);

                    // New method in JDK 8
//                    m.computeIfAbsent(anagramKey, k -> new HashSet<>())
//                            .add(w);
                }
            }

            List<Set<String>> allAnagrams = new ArrayList<>();
            for (Set<String> anagrams : m.values()) {
                if (anagrams.size() > 1) {
                    allAnagrams.add(anagrams);
                }
            }
            return allAnagrams;
        }

        /**
         * Create a histogram of words > 4 in length
         */
        static Map<Integer, List<String>> histogram(Path filename) throws IOException {
            try (BufferedReader br =
                         Files.newBufferedReader(filename, StandardCharsets.UTF_8)) {
                return histogram(br);
            }
        }

        static Map<Integer, List<String>> histogram(BufferedReader br) throws IOException {
            Map<String, Integer> m = new HashMap<>();
            String line;

            while ((line = br.readLine()) != null) {
                for (String w : line.split(INTO_WORDS)) {

                    Integer count = m.get(w);
                    if (count == null) {
                        count = 0;
                    }
                    count++;
                    m.put(w, count);

                    // New method in JDK 8
//                    m.compute(w, (k, v) -> v == null ? 0 : v + 1);
                }
            }

            // Reverse, and filter out words < 5 characters
            Map<Integer, List<String>> h = new TreeMap<>(Collections.reverseOrder());
            for (Map.Entry<String, Integer> e : m.entrySet()) {
                String word = e.getKey();
                int count = e.getValue();

                if (word.length() > 4) {
                    List<String> words = h.get(count);
                    if (words == null) {
                        words = new ArrayList<>();
                        h.put(count, words);
                    }
                    words.add(word);
                }

                // New method in JDK 8
//                h.computeIfAbsent(count, k -> new ArrayList<>())
//                        .add(word);
            }
            return h;
        }
    }

    static String key(String s) {
        char[] chars = s.toLowerCase().toCharArray();
        Arrays.sort(chars);
        return new String(chars);
    }

    static class Lambda {

        static void main(Path filename) throws IOException {
            Stream<Set<String>> words = anagrams(filename);
            words.forEach(System.out::println);

            Map<Integer, List<String>> h = histogram(filename);
            h.forEach((k, v) -> System.out.println(k + " -> " + v));
        }

        static Stream<Set<String>> anagrams(Path filename) throws IOException {
            try (Stream<String> s = Files.lines(filename, StandardCharsets.UTF_8)) {
                return anagrams(s);
            }
        }

        static Stream<Set<String>> anagrams(Stream<String> lines) throws IOException {
            Stream<String> words = lines
                    .map(String::toLowerCase)
                    .flatMap(line -> Stream.of(line.split(INTO_WORDS)));

            Map<String, Set<String>> groupedAnagrams = words
                    .collect(groupingBy(AnagramAndHistogram::key, toSet()));

            return groupedAnagrams.values().stream().filter(v -> v.size() > 1);
        }

        static Map<Integer, List<String>> histogram(Path filename) throws IOException {
            try (Stream<String> s = Files.lines(filename, StandardCharsets.UTF_8)) {
                return histogram(s);
            }
        }

        static Map<Integer, List<String>> histogram(Stream<String> lines) throws IOException {
            Stream<String> words = lines
                    .flatMap(line -> Stream.of(line.split(INTO_WORDS)));

            Map<String, Long> m = words.collect(groupingBy(Function.identity(),
                                                           counting()));

//            Map<String, Long> m = words.collect(groupingBy(Function.identity(),
//                                                           reducing(0, w -> 1L, Long::sum)));
//
//            Map<String, Long> m = words.collect(toMap(Function.identity(),
//                                                      w -> 1L, Long::sum));

            // Reverse
            Map<Integer, List<String>> h = m.entrySet().stream()
                    .filter(e -> e.getKey().length() > 4)
                    .collect(groupingBy(e -> e.getValue().intValue(),
                                        () -> new TreeMap<>(Comparator.<Integer>reverseOrder()),
                                        mapping(e -> e.getKey(), toList())));

            return h;
        }
    }
}
