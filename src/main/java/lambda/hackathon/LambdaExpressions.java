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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

public class LambdaExpressions {
    public static void main(String[] args) {

        // A lambda expression is an anonymous method
        //   Code as data

        // Has an argument list, a body and a return type

        // a -> a.toUpperCase()

        // (a, b) -> a + " " + b;

        // Evaluates to an instance of a functional interface
        //   Interface with one abstract method

        // Compiler recognizes functional interfaces structurally

        {
            // A function to convert a string to upper case
            Function<String, String> function = a  ->   a.toUpperCase();
                                            // (a) -> { a.toUpperCase(); }

            // Simple expressions are very concise

            // Optional types of arguments
            Function<String, String> typeArgs = (String a) -> a.toUpperCase();

            // Not just variables
            callMe(a -> a.toUpperCase());

            BiFunction<String, String, String> biF = (a, b) -> a + b;
            BinaryOperator<String> biO = (a, b) -> biF.apply(a, b);

            Runnable runnable = () -> System.out.println("RUN!");
            Runnable block = () -> {
                System.out.println("RUN!");
                System.out.println("RUN!");
            };
            Runnable anonClass = new Runnable() {
                @Override
                public void run() {
                    System.out.println("RUN!");
                    System.out.println("RUN!");
                }
            };

            FilenameFilter ff = (File f, String name) -> true;
            // Like a BiPredicate<File, String>

            // Same expression targeted to different functional interfaces

            Supplier<String> consumer = () -> Thread.currentThread().getName();

            Callable<String> callable = () -> Thread.currentThread().getName();
        }


        // Can refer to values in enclosing lexical scope
        //  Just like inner classes

        {
            // Effectively final
            int captureMe = 42;

            Function<Integer, Integer> f = i -> i + captureMe;
            // Specialized functional interfaces primitives
            IntUnaryOperator o = i -> i + captureMe;

            new LambdaExpressions().captureThis();
        }


        // References

        {
            // Method reference
            Function<Object, String> f = Object::toString;
                                    // = (s) -> s.toString()

            // Constructor references
            Supplier<List<String>> ls = ArrayList::new;
            Function<Integer, List<String>> lss = ArrayList::new;

            // Method reference on instance
            List<String> l = ls.get();
            IntFunction<String> lg = l::get;

            // Array constructor reference
            Function<Integer, String[]> sac = String[]::new;
            IntFunction<String[]> psac = String[]::new;
        }


        //

        {
            Function<Integer, Function<Integer, Integer>> adder
                    = a -> b -> a + b;
            Function<Integer, Integer> add1 = adder.apply(1);
            int two = add1.apply(1);


            Function<ExecutorService, Function<Runnable, CompletableFuture<Void>>>
                    runServiceAsyncer = es -> r -> CompletableFuture.runAsync(r, es);

            ExecutorService es = new ForkJoinPool();
            Function<Runnable, CompletableFuture<Void>> runAsync = runServiceAsyncer.apply(es);
            CompletableFuture<Void> cf = runAsync.apply(() -> System.out.println("RUN ME"));


            // See Y-combinators (which may make some of the Lamba
            //   https://gist.github.com/aruld/3965968
            //
            //   http://mail.openjdk.java.net/pipermail/lambda-dev/2013-September/010954.html
            //   http://mail.openjdk.java.net/pipermail/lambda-dev/2013-September/010955.html
            //
            //   Great test case
            //   No syntax required for recursive lambdas

        }

    }

    static void callMe(Function<String, String> f) {

    }

    void captureThis() {
        Runnable r = () -> {
            // "this" refers to the enclosing class, if in scope
            this.doSomething();
        };
    }

    void doSomething() { }
}
