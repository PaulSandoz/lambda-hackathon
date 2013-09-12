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
import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparing;

public class DefaultMethods {

    // A way to evolve interfaces

    // Adding a new default method to an existing interface is both
    // source and binary compatible

    // Multiple inheritance of *behaviour* not state
    //   Java always had multiple inheritance of types

    // How to resolve conflicts? three simple rules

    interface Shape {
        int sides();

        default String name() {
            return "Shape";
        }

        // Can only be referenced from Shape and not sub-types
        static void assertEulersFormula(int v, int e, int f) {
            assert v - e + f == 2;
        }
    }

    // Rule 1 class wins
    // Defaults only considered if no method declared in superclass chain
    // Ensure compatibility with pre-Java8 inheritance
    //   Thou shalt link!
    static class Rule1 {

        static class Octagon implements Shape {
            @Override
            public int sides() {
                return 8;
            }

            @Override
            public String name() {
                return "Octogon";
            }
        }

        static class ColouredOctagon extends Octagon implements Shape {
            // Inherits name() from Octagon
        }

    }

    // Rule 2 subtypes win
    // Select default method from most specific subtype
    // Shape of inheritance tree does not matter
    static class Rule2 {

        interface Box extends Shape {

            default int sides() {
                return 4;
            }

            default String name() {
                return "Box";
            }

        }

        static class EmptyBox implements Box, Shape {
            // Inherits name() from Box
        }

    }

    // Rule 3 there is no rule 3
    // If rule 1 does not apply and rule 2 produces no specific subtype
    //  Conflict!
    static class ThereIsNoRule3 {

        interface RogueShape {
            default String name() {
                return "RougeShape";
            }
        }

        static class ConfusedShape implements Shape, RogueShape {
            @Override
            public int sides() {
                return -1;
            }

            // Override or re-abstract
            @Override
            public String name() {
                // Delegate to new inherited implementation
                return Shape.super.name();
            }
        }

    }


    // Default methods are instance methods
    static class InstanceMethods {
        interface Box extends Shape {
            int sides();

            default String name() {
                return "Shape " + sides();
                // + this.sides();
            }
        }

    }

    // Diamonds do not pose a problem for behavioural inheritance
    static class Diamonds {
        interface Box extends Shape {}

        interface Rectangle extends Shape {}

        // "Redundant" inheritance does not affect resolution
        static class BoxedRectangle implements Box, Rectangle {
            @Override
            public int sides() {
                return 4;
            }
        }

    }


    // Example: Comparators and sorting
    static class Sorting {
        interface Person {
            String getFirstName();
            String getLastName();
        }

        List<Person> people = new ArrayList<>();

        {
            Comparator<Person> byLastName = new Comparator<Person>() {
                @Override
                public int compare(Person x, Person y) {
                    return x.getLastName().compareTo(y.getLastName());
                }
            };
            Collections.sort(people, byLastName);

            Collections.sort(people, Comparator.comparing(
                    p -> p.getLastName()));

            people.sort(comparing(p -> p.getLastName()));

            people.sort(comparing(Person::getLastName));

            people.sort(comparing(Person::getLastName).reversed());

            people.sort(comparing(Person::getLastName).
                    thenComparing(Person::getFirstName));
        }
    }

}
