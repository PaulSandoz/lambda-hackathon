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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;

public class Words {

    static final int LEN = 14;

    static final String INTO_WORDS = "[- @/.,:;_'\"?!()]+"; // regexp for splitting

    public static void main(String[] args) throws IOException {
        Path filename = Paths.get("target/JaneAusten-PrideAndPrejudice.txt");

        Imperative.main(filename);
    }

    // Find all distinct words in a text file greater than 14 characters in
    // length, then sort those words with a primary key of string length and
    // the secondary key of the string

    static class Imperative {

        static void main(Path filename) throws IOException {
            List<String> words = processWords(filename);
            for (String w : words) {
                System.out.println(w);
            }
            System.out.println(words.size());
        }

        static List<String> processWords(Path filename) throws IOException {
            try (BufferedReader br =
                         Files.newBufferedReader(filename, StandardCharsets.UTF_8)) {
                return processWords(br);
            }
        }

        static List<String> processWords(BufferedReader br) throws IOException {
            Set<String> seen = new HashSet<>();
            String line;

            while ((line = br.readLine()) != null) {
                line = line.toLowerCase();
                for (String w : line.split(INTO_WORDS)) {
                    if (w.length() >= LEN) {
                        seen.add(w);
                    }
                }
            }

            List<String> words = new ArrayList<>(seen);
            Collections.sort(words, new Comparator<String>() {
                public int compare(String s1, String s2) {
                    int diff = s1.length() - s2.length();
                    if (diff != 0) {
                        return diff;
                    }
                    else {
                        return s1.compareTo(s2);
                    }
                }
            });

            return words;
        }
    }


    static class Lambda {

        static void main(Path filename) throws IOException {
            List<String> words = processWords(filename, false);
            words.forEach(System.out::println);
            System.out.println(words.size());
        }

        static List<String> processWords(Path filename, boolean parallel) throws IOException {
            try (Stream<String> s = Files.lines(filename, StandardCharsets.UTF_8)) {
                return processWords(parallel ? s.parallel() : s.sequential());
            }
        }

        static List<String> processWords(Stream<String> lines) throws IOException {
            return lines
                    .map(String::toLowerCase)
                    .flatMap(line -> Stream.of(line.split(INTO_WORDS)))
                    .filter(word -> word.length() >= LEN)
                    .distinct()
                    .sorted(comparingInt(String::length).
                            thenComparing(naturalOrder()))
                    .collect(toList());
        }
    }
}
