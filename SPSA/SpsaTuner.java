package src.SPSA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class SpsaTuner {

    private final List<Param> baseParams;
    private final SpsaParams sched;
    private final Random rng;
    private final List<Integer> delta;
    // match config (time control only)
    private final int pairsPerIter;             // one pair = two games (swap colors)
    private final int boardSize;
    private final int boardOffset;
    private final long baseTimeMs;
    private final long incMs;
    private final int randomOpeningPlies;
    private final int threads;
    private final ParamApplier applier;
    private int k = 0; // iteration index

    public SpsaTuner(SpsaParams sched,
                     List<Param> params,
                     long seed,
                     int pairsPerIter,
                     int boardSize,
                     int boardOffset,
                     double baseTimeSeconds,
                     double incSeconds,
                     int randomOpeningPlies,
                     int threads,
                     ParamApplier applier) {

        this.sched = sched;
        this.baseParams = params;
        this.rng = new Random(seed);
        this.delta = new ArrayList<>(Collections.nCopies(params.size(), 0));

        this.pairsPerIter = Math.max(1, pairsPerIter);
        this.boardSize = boardSize;
        this.boardOffset = boardOffset;
        this.baseTimeMs = Math.round(baseTimeSeconds * 1000.0);
        this.incMs = Math.round(incSeconds * 1000.0);
        this.randomOpeningPlies = randomOpeningPlies;
        this.threads = Math.max(1, threads);
        this.applier = applier;
    }

    private static List<Param> copyParams(List<Param> src) {
        List<Param> dst = new ArrayList<>(src.size());
        for (Param p : src) dst.add(new Param(p.name, p.value, p.minValue, p.maxValue, p.step));
        return dst;
    }

    public void stepOnce() {
        k++;
        double a_t = sched.a / Math.pow(k + sched.A, sched.alpha);
        double c_t = sched.c / Math.pow(k, sched.gamma);

        delta.replaceAll(ignored -> rng.nextBoolean() ? +1 : -1);

        // θ+ and θ− around base
        List<Param> plus = copyParams(baseParams);
        List<Param> minus = copyParams(baseParams);
        for (int i = 0; i < baseParams.size(); i++) {
            Param p = baseParams.get(i);
            int s = delta.get(i);
            double d = s * p.step * c_t;
            plus.get(i).update(+d);
            minus.get(i).update(-d);
        }

        // Build seeds for each pair and evaluate in parallel
        List<Long> seeds = new ArrayList<>(pairsPerIter);
        for (int i = 0; i < pairsPerIter; i++) seeds.add(rng.nextLong());

        double diff = parallelPairedDiff(plus, minus, seeds);

        // Gradient & update
        for (int i = 0; i < baseParams.size(); i++) {
            Param p = baseParams.get(i);
            int s = delta.get(i);
            double g_i = diff / (2.0 * c_t * p.step * s);
            p.update(-a_t * g_i);
        }
    }

    public List<Param> getParams() {
        return baseParams;
    }

    private double parallelPairedDiff(List<Param> plus, List<Param> minus, List<Long> seeds) {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Double>> tasks = new ArrayList<>(seeds.size());
            for (long seed : seeds) {
                tasks.add(() -> SelfPlay.playPairTimeControl(
                        boardSize, boardOffset, baseTimeMs, incMs, randomOpeningPlies,
                        plus, minus, applier, seed));
            }

            double sum = 0.0;
            for (Future<Double> f : pool.invokeAll(tasks)) {
                sum += f.get();
            }
            return sum / seeds.size();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during evaluation", ie);
        } catch (ExecutionException ee) {
            throw new RuntimeException("Game task failed: " + ee.getCause(), ee);
        } finally {
            pool.shutdownNow();
        }
    }
}