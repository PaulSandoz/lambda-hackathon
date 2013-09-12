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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReduceAndCollect {

    static void main(String[] args) {

        // Sum of squares pure reduction to a list

        {
            List<Integer> squares = IntStream.rangeClosed(1, 8)
                    .map(i -> i * i).boxed()
                    .map(Collections::singletonList)
                    .reduce(Collections.emptyList(),
                            (l, r) -> {
                                List<Integer> lr = new ArrayList<>();
                                lr.addAll(l);
                                lr.addAll(r);
                                return lr;
                            });
        }

        // Sum of squares pure fold to a list

        {
            List<Integer> squares = IntStream.rangeClosed(1, 8)
                    .map(i -> i * i).boxed()
                    .reduce(Collections.emptyList(),
                            (l, e) -> {
                                List<Integer> nl = new ArrayList<>(l);
                                nl.add(e);
                                return nl;
                            },
                            (l, r) -> {
                                List<Integer> lr = new ArrayList<>();
                                lr.addAll(l);
                                lr.addAll(r);
                                return lr;
                            });
        }

        // Mutable reduce aka collect toList()

        {
            List<Integer> squares = IntStream.rangeClosed(1, 8)
                    .map(i -> i * i).boxed()
                    .collect(Collectors.toList());
        }


        // Explicit mutable reduction with Stream.collect

        {
            List<Integer> squares = IntStream.rangeClosed(1, 8)
                    .map(i -> i * i).boxed()
                    .collect(ArrayList::new,
                             ArrayList::add,
                             ArrayList::addAll);
        }

        // Explicit mutable reduction with Collector.of

        {
            List<Integer> squares = IntStream.rangeClosed(1, 8)
                    .map(i -> i * i).boxed()
                    .collect(Collector.of(ArrayList::new,
                                          ArrayList::add,
                                          (l, r) -> { l.addAll(r); return l; }
                    ));
        }
    }
}
