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

import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.logic.results.Result;
import org.openjdk.jmh.logic.results.RunResult;
import org.openjdk.jmh.output.OutputFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.parameters.TimeValue;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@State(Scope.Benchmark)
public class MillerRabinPerfTest {

    private static int N = 10000;

    private static int ORIGIN_BIT_SIZE = 128;

    private static int BOUND_BIT_SIZE = 129;

    private static int ITERATIONS = 50;

    private static List<BigInteger> bigIntegers;

    static {
        // 40 digit prime
        bigIntegers = Collections.singletonList(new BigInteger("2425967623052370772757633156976982469681"));

        // PI
//        bigIntegers = Collections.singletonList(new BigInteger("3141592653589793238462643383279502884197"));

        // Random values
//        bigIntegers = MillerRabin.oddBigIntegers(N, ORIGIN_BIT_SIZE, BOUND_BIT_SIZE);

    }

    @GenerateMicroBenchmark
    public void testImperative() throws IOException {
        test(new MillerRabin.Imperative());
    }

    @GenerateMicroBenchmark
    public void testBulkParallelImperative() throws IOException {
        test(new MillerRabin.Imperative(), true);
    }

    @GenerateMicroBenchmark
    public void testBulkParallelLambdaAllMatchSeq() throws IOException {
        test(new MillerRabin.LambdaAllMatch(false), true);
    }

    @GenerateMicroBenchmark
    public void testLambdaAllMatchSeq() throws IOException {
        test(new MillerRabin.LambdaAllMatch(false));
    }

    @GenerateMicroBenchmark
    public void testLambdaAllMatchPar() throws IOException {
        test(new MillerRabin.LambdaAllMatch(true));
    }

    @GenerateMicroBenchmark
    public void testBulkParallelLambdaAllMatchPar() throws IOException {
        test(new MillerRabin.LambdaAllMatch(true), true);
    }
    void test(MillerRabin.MillerRabinTest mt) {
        bigIntegers.forEach(b -> mt.millerRabinTest(b, ITERATIONS));
    }

    void test(MillerRabin.MillerRabinTest mt, boolean parallel) {
        Stream<BigInteger> s = parallel ? bigIntegers.parallelStream() : bigIntegers.stream();
        s.forEach(b -> mt.millerRabinTest(b, ITERATIONS));
    }

    public static void main(String[] args) throws RunnerException {
        PrintWriter pw = new PrintWriter(System.out, true);

        pw.printf("N = %d\nBigInteger max bit length = [%d, %d)\nMiller-Rabin iterations = %d\n", bigIntegers.size(), ORIGIN_BIT_SIZE, BOUND_BIT_SIZE, ITERATIONS);

        pw.printf("       imperative = %.2f ns\n", run("testImperative"));
        pw.printf("sequential stream = %.2f ns\n", run("testLambdaAllMatchSeq"));
        pw.printf("  parallel stream = %.2f ns\n", run("testLambdaAllMatchPar"));

        if (bigIntegers.size() > 1) {
            pw.printf("Bulk parallel:\n");
            pw.printf("       imperative = %.2f ns\n", run("testBulkParallelImperative"));
            pw.printf("sequential stream = %.2f ns\n", run("testBulkParallelLambdaAllMatchSeq"));
            pw.printf("  parallel stream = %.2f ns\n", run("testBulkParallelLambdaAllMatchPar"));
        }
    }

    public static double run(String test) throws RunnerException {
        Options opts = new OptionsBuilder()
                .include(".*MillerRabinPerfTest.*" + test)
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.NANOSECONDS)
                .warmupIterations(5)
                .warmupTime(TimeValue.milliseconds(100))
                .measurementIterations(5)
                .measurementTime(TimeValue.milliseconds(100))
                .outputFormat(OutputFormatType.Silent)
                .forks(5)
                .build();

        RunResult runResult = new Runner(opts).runSingle();
        Result result = runResult.getPrimaryResult();
        return result.getScore();
    }
}
