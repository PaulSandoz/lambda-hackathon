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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 */
public class ProbablePrime {

    public static void main(String[] args) {
        Imperative imperative = new Imperative();
        Lambda lambdaSeq = new Lambda(false);
        Lambda lambdaPar = new Lambda(true);

        System.out.println(imperative.probablePrimes(10, 128));
        System.out.println(lambdaSeq.probablePrimesUsingGenerate(10, 128));
        System.out.println(lambdaPar.probablePrimesUsingGenerate(10, 128));
    }

    static class Imperative {

        public List<BigInteger> probablePrimes(int n, int bitLength) {
            List<BigInteger> pps = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                BigInteger pp = BigInteger.probablePrime(bitLength, ThreadLocalRandom.current());
                pps.add(pp);
            }
            return pps;
        }
    }

    static class Lambda {

        final boolean parallel;

        Lambda(boolean parallel) {
            this.parallel = parallel;
        }

        public List<BigInteger> probablePrimesUsingRange(int n, int bitLength) {
            IntStream s = IntStream.range(0, n);
            s = parallel ? s.parallel() : s.sequential();

            return s.mapToObj(i -> BigInteger.probablePrime(bitLength,
                                                            ThreadLocalRandom.current()))
                    .collect(toList());
        }

        public List<BigInteger> probablePrimesUsingGenerate(int n, int bitLength) {
            Stream<BigInteger> s = Stream.generate(() -> BigInteger.probablePrime(bitLength,
                                                                                  ThreadLocalRandom.current()));
            s = parallel ? s.parallel() : s.sequential();
            return s.limit(n)
                    .collect(toList());
        }
    }
}
