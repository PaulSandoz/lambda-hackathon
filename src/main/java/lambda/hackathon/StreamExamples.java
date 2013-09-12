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

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.stream.Collectors.*;

public class StreamExamples {

    public static void main(String[] args) {
        // Properties
        // Filter for names starting with "sun"
        // count such properties

        // Collect to list
        // Mess around with list

        // Frequency count of characters of property keys
        // Map<Character, Long>
        // Reverse it, sorting entries, Map<Long, Character>, Map<Long, List<Character>>
    }

    {
        System.getProperties().keySet().stream()
                .map(Object::toString)
                .filter(s -> s.startsWith("sun"));


        Map<Character, Long> m = System.getProperties().keySet().stream()
                .flatMapToInt(o -> o.toString().chars())
                .boxed()
                .collect(groupingBy(ic -> (char) ic.intValue(), TreeMap::new, counting()));
        m.entrySet().forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));


        Map<Long, Character> rm = m.entrySet().stream()
                .collect(toMap(e -> e.getValue(),
                               e -> e.getKey(),
                               (a, b) -> a,
                               () -> new TreeMap<>(Comparator.<Long>reverseOrder())));
        rm.entrySet().forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));

        m.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(System.out::println);

        m.entrySet().stream()
                .sorted(Map.Entry.<Character, Long>comparingByValue().reversed())
                .forEach(System.out::println);

        Map<Long, List<Character>> rmm = m.entrySet().stream()
                .collect(groupingBy(e -> e.getValue(),
                                    () -> new TreeMap<>(Comparator.<Long>reverseOrder()),
                                    mapping(e -> e.getKey(), toList())));
        rmm.entrySet().forEach(e -> System.out.println(e.getKey() + " -> " + e.getValue()));

    }
}
