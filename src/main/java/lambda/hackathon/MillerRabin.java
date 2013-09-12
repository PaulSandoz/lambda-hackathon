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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.math.BigInteger.ONE;
import static java.util.stream.Collectors.toList;

/**
 * http://mathworld.wolfram.com/Rabin-MillerStrongPseudoprimeTest.html
 *
 * http://en.wikipedia.org/wiki/Miller%E2%80%93Rabin_primality_test
 */
public class MillerRabin {
    private static final BigInteger TWO = BigInteger.valueOf(2);

    public static void main(String[] args) {
        List<BigInteger> is = oddBigIntegers(100, 64, 128);

        Imperative imperative = new Imperative();
        LambdaAllMatch lambdaSeq = new LambdaAllMatch(false);
        LambdaAllMatch lambdaPar = new LambdaAllMatch(true);

        for (BigInteger i : is) {
            System.out.println(i);
            boolean a = isProbablePrime(i, imperative, 1000);
            boolean b = isProbablePrime(i, lambdaSeq, 1000);
            boolean c = isProbablePrime(i, lambdaPar, 1000);

            assert !(a & c & b);
        }
    }

    static List<BigInteger> oddBigIntegers(int n, int oBits, int bBits) {
        return Stream.generate(() -> nextOddRandomBigInteger(oBits, bBits))
                .limit(n)
                .collect(toList());
    }

    static BigInteger nextOddRandomBigInteger(int oBits, int bBits) {
        BigInteger b;
        do {
            b = new BigInteger(ThreadLocalRandom.current().nextInt(oBits, bBits),
                               ThreadLocalRandom.current());
        }
        while (b.compareTo(TWO) <= 0 || !b.testBit(0));
        return b;
    }

    static boolean isProbablePrime(BigInteger n, MillerRabinTest mrt, int iterations) {
        BigInteger w = n.abs();
        if (w.equals(TWO))
            return true;
        if (!w.testBit(0) || w.equals(ONE))
            return false;

        return mrt.millerRabinTest(n, iterations);
    }

    @FunctionalInterface
    interface MillerRabinTest {
        boolean millerRabinTest(BigInteger n, int iterations);
    }

    static class Imperative implements MillerRabinTest {

        public boolean millerRabinTest(BigInteger n, int iterations) {
            // Find a and m such that m is odd and n == 1 + 2^a * m
            BigInteger thisMinusOne = n.subtract(ONE);
            int a = thisMinusOne.getLowestSetBit();
            BigInteger m = thisMinusOne.shiftRight(a);

            // Do the tests
            for (int i = 0; i < iterations; i++) {
                // Generate a uniform random on (1, this)
                BigInteger b;
                do {
                    b = new BigInteger(n.bitLength(), ThreadLocalRandom.current());
                }
                while (b.compareTo(ONE) <= 0 || b.compareTo(n) >= 0);

                int j = 0;
                // z = b^m mod n
                BigInteger z = b.modPow(m, n);
                while (!((j == 0 && z.equals(ONE)) || z.equals(thisMinusOne))) {
                    if (j > 0 && z.equals(ONE) || ++j == a)
                        return false;
                    // z = z^2 mod n
                    z = z.modPow(TWO, n);
                }
            }
            return true;
        }
    }

    static class LambdaAllMatch implements MillerRabinTest {

        final boolean parallel;

        LambdaAllMatch(boolean parallel) {
            this.parallel = parallel;
        }

        public boolean millerRabinTest(BigInteger n, int iterations) {
            // Find a and m such that m is odd and this == 1 + 2**a * m
            BigInteger thisMinusOne = n.subtract(ONE);
            int a = thisMinusOne.getLowestSetBit();
            BigInteger m = thisMinusOne.shiftRight(a);

            IntStream range = IntStream.range(0, iterations);
            range = parallel ? range.parallel() : range.sequential();
            return range
                    .mapToObj(i -> {
                        BigInteger b;
                        do {
                            b = new BigInteger(n.bitLength(), ThreadLocalRandom.current());
                        }
                        while (b.compareTo(ONE) <= 0 || b.compareTo(n) >= 0);
                        return b;
                    })
                    .allMatch(b -> {
                        int j = 0;
                        BigInteger z = b.modPow(m, n);
                        while (!((j == 0 && z.equals(ONE)) || z.equals(thisMinusOne))) {
                            if (j > 0 && z.equals(ONE) || ++j == a)
                                return false;
                            z = z.modPow(TWO, n);
                        }
                        return true;
                    });
        }

    }

    static class LambdaSum implements MillerRabinTest {

        final boolean parallel;

        LambdaSum(boolean parallel) {
            this.parallel = parallel;
        }

        public boolean millerRabinTest(BigInteger n, int iterations) {
            // Find a and m such that m is odd and this == 1 + 2**a * m
            BigInteger thisMinusOne = n.subtract(ONE);
            int a = thisMinusOne.getLowestSetBit();
            BigInteger m = thisMinusOne.shiftRight(a);

            IntStream range = IntStream.range(0, iterations);
            range = parallel ? range.parallel() : range.sequential();
            return range
                           .mapToObj(i -> {
                               BigInteger b;
                               do {
                                   b = new BigInteger(n.bitLength(), ThreadLocalRandom.current());
                               }
                               while (b.compareTo(ONE) <= 0 || b.compareTo(n) >= 0);
                               return b;
                           })
                           .mapToInt(b -> {
                               int j = 0;
                               BigInteger z = b.modPow(m, n);
                               while (!((j == 0 && z.equals(ONE)) || z.equals(thisMinusOne))) {
                                   if (j > 0 && z.equals(ONE) || ++j == a)
                                       return 1;
                                   z = z.modPow(TWO, n);
                               }
                               return 0;
                           })
                           .sum() == 0;
        }
    }

}
