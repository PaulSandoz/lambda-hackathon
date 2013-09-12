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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * Inspired from Christophe Grand: http://clj-me.cgrand.net/2011/08/19/conways-game-of-life/
 */
public class Life {

    static final class Cell {
        final int x;
        final int y;

        Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        Cell add(Cell that) {
            return new Cell(that.x + x, that.y + y);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cell cell = (Cell) o;

            if (x != cell.x) return false;
            if (y != cell.y) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }

        @Override
        public String toString() {
            return String.format("[%d, %d]", x, y);
        }

        static Cell of(int x, int y) {
            return new Cell(x, y);
        }
    }

    static class Imperative {

        static List<Cell> neighbourDeltas() {
            int[] values = {1, 0, -1};
            List<Cell> cellDiffs = new ArrayList<>();
            for (int x : values) {
                for (int y : values) {
                    if (y != 0 || x != 0) {
                        cellDiffs.add(new Cell(x, y));
                    }
                }
            }
            return Collections.unmodifiableList(cellDiffs);
        }

        //  List of neighbouring cells around the origin
        //
        //  x  x  x
        //  x  o  x
        //  x  x  x
        //
        static final List<Cell> NEIGHBOUR_DELTAS = neighbourDeltas();

        static Set<Cell> step(Set<Cell> world) {
            // Calculate the frequencies of neighbouring cells
            Map<Cell, Integer> fs = new HashMap<>();
            for (Cell c : world) {
                for (Cell d : NEIGHBOUR_DELTAS) {
                    Cell n = c.add(d);

                    Integer count = fs.get(n);
                    if (count == null) {
                        count = 0;
                    }
                    fs.put(n, ++count);
                }
            }

            Set<Cell> newWorld = new HashSet<>();
            for (Map.Entry<Cell, Integer> e : fs.entrySet()) {
                //     Any live cell with fewer than two live neighbours dies, as if caused by under-population.
                // [*] Any live cell with two or three live neighbours lives on to the next generation.
                //     Any live cell with more than three live neighbours dies, as if by overcrowding.
                // [*] Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
                if (e.getValue() == 3 || (e.getValue() == 2 && world.contains(e.getKey()))) {
                    newWorld.add(e.getKey());
                }
            }
            return newWorld;
        }

        public static void main() {
            // Initial state, a blinker!
            //   x
            //   x
            //   x
            Set<Cell> world = new HashSet<>(Arrays.asList(Cell.of(1, 2),
                                                          Cell.of(1, 1),
                                                          Cell.of(1, 0)));

            // Iterate for 5 generations printing out each generation
            System.out.println(world);
            for (int i = 0; i < 4; i++) {
                world = step(world);
                System.out.println(world);
            }

            //   x              x
            //   x  -> x x x -> x -> x x x
            //   x              x
        }
    }

    static class Lambda {

        static List<Cell> neighbourDeltas() {
            List<Integer> values = Arrays.asList(1, 0, -1);
            Function<Integer, Stream<Cell>> f =
                    x -> values.stream().
                            filter(y -> y != 0 || x != 0).map(y -> new Cell(x, y));
            return values.stream().flatMap(f).collect(toList());
        }

        static final List<Cell> NEIGHBOUR_DELTAS = neighbourDeltas();

        static <T> Collector<T, ?, Map<T, Long>> toFrequencies() {
            return groupingBy(Function.<T>identity(), counting());
        }

        static Stream<Cell> neighbours(Stream<Cell> world) {
            return world.flatMap(cell -> NEIGHBOUR_DELTAS.stream().map(cell::add));
        }

        static Set<Cell> step(Set<Cell> world) {
            // Calculate the frequencies of neighbouring cells
            Map<Cell, Long> fs = neighbours(world.stream()).
                    collect(toFrequencies());

            return fs.entrySet().stream().
                    //     Any live cell with fewer than two live neighbours dies, as if caused by under-population.
                    // [*] Any live cell with two or three live neighbours lives on to the next generation.
                    //     Any live cell with more than three live neighbours dies, as if by overcrowding.
                    // [*] Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.
                    filter(e -> e.getValue() == 3 || (e.getValue() == 2 && world.contains(e.getKey()))).
                    // Map from Stream<Entry<Cell, Integer>> to Stream<Cell>
                    map(e -> e.getKey()).
                    // Collect to a set
                    collect(toSet());
        }

        public static void main() {
            // Initial state, a blinker!
            //   x
            //   x
            //   x
            Set<Cell> world = new HashSet<>(Arrays.asList(Cell.of(1, 2),
                                                          Cell.of(1, 1),
                                                          Cell.of(1, 0)));

            // Iterate for 5 generations printing out each generation
            Stream.iterate(world, Lambda::step).limit(5).
                    forEach(System.out::println);

            //   x              x
            //   x  -> x x x -> x -> x x x
            //   x              x
        }
    }

    public static void main(String[] args) {
        Imperative.main();
        System.out.println();
        Lambda.main();
    }
}
