/*
 * Copyright (c) 2017.
 *    * Unauthorized copying of this file  (SimulatedAnnealing.java), via any medium is strictly prohibited
 *    * Proprietary and confidential
 *    * Written by :
 * 		Amir Ahooye Atashin - FUM <amir.atashin@mail.um.ac.ir>
 * 		Majeed Mohammadi - TBM <M.Mohammadi@tudelft.nl>
 *
 * 																						Last modified: 2017 - 9 - 6
 *
 */

package am;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("Duplicates")
public class SimulatedAnnealing {
    private double[][] similarity;
    private double[][] similaritySup;
    private List<Integer> sol;
    private int row, col, cntGoods = 0;
    private double thresholdGoods = 0.73;

    private Random random;

    public SimulatedAnnealing(double[][] sim, double[][] simSup){

        similarity = sim;
        similaritySup = simSup;
        row = sim.length;
        col = sim[0].length;
        //random = new Random(System.currentTimeMillis());
        random = new Random(0);
    }
    public void solve(int duration) {
        double deltaE, temperature = 1.0, alpha = 0.997;
        sol = generateInitSol();
        List<Integer> next, curr, best;
        curr = best = sol;
        double fitNext, fitCurr = getFitness(curr), fitBest = fitCurr;
        for (int t = 0; t < duration; ++t){
            curr = best;
            fitCurr = fitBest;
            for (int i = 0; i < 50; ++i) {
                next = successor(curr);
                fitNext = getFitness(next);
                deltaE = fitNext - fitCurr;
                if (deltaE > 0) {
                    curr = next;
                    fitCurr = fitNext;
                } else if (random.nextDouble() >= Math.exp(deltaE / temperature)) {
                    curr = next;
                    fitCurr = fitNext;
                }
                if(fitBest < fitCurr)
                {
                    fitBest = fitCurr;
                    best = curr;
                }
            }
            temperature = (temperature > 0.0001) ? (temperature * alpha) : 0.0001;
            System.out.print("\n" + (t + 1) + "\t: " + fitBest);
        }
        System.out.println("\n" + "Final temperature : " + temperature);
        sol = best;
    }
    public List<Pair<Integer, Integer>> getSolution() {
        return extractSolutionFinal(sol);
    }
    private List<Integer> successor(final List<Integer> curr) {
        int batchSz = Math.min(4, (row / 2) * 2);
        int[] randInx = random.ints(cntGoods, row).distinct().limit(batchSz).toArray();
        List<Integer> next = new ArrayList<>(curr);
        for (int i = 0; i < batchSz; i += 2)
            java.util.Collections.swap(next, randInx[i], randInx[i + 1]);
        return next;
    }
    private double getFitness(final List<Integer> S) {
        double sum1 = 0, sum2 = 0, simValSup;
        List<Pair<Integer, Integer>> SS = extractSolution(S);
        for (Pair<Integer, Integer> item: SS) {
            double simVal = similarity[item.getL()][item.getR()];
            if(simVal >= thresholdGoods) {
                sum1 += simVal;
                continue;
            }
            if(similaritySup != null) {
                simValSup = similaritySup[item.getL()][item.getR()];
                if (simVal > 0.55 && simValSup >= 1.0) {
                    sum2 += (simValSup + simVal);
                    continue;
                }
                if (simVal > 0.65 && simValSup >= 0.8)
                    sum2 += (simValSup + simVal);
            }
        }
        return sum1 * 200 + sum2 * 10;
    }
    private List<Integer> generateInitSol(){
        List<Integer> randOrder = random.ints(0, row).distinct().limit(row).boxed().collect(Collectors.toCollection(ArrayList::new));
        List<Integer> ans = new ArrayList<>(row);
        boolean[] selected = new boolean[col];
        boolean[] selected2 = new boolean[row];
        int maxInd;
        double maxVal;
        for (double thr = 1.0; thr >= 0.80; thr -= 0.05) {
            for (int i : randOrder) {
                if(selected2[i]) continue;
                maxInd = -1;
                maxVal = -1;
                for (int j = 0; j < col; ++j) {
                    if (similarity[i][j] > maxVal && !selected[j]) {
                        maxInd = j;
                        maxVal = similarity[i][j];
                    }
                }
                if (maxVal >= thr) {
                    selected[maxInd] = true;
                    selected2[i] = true;
                    cntGoods++;
                    ans.add(i);
                }
            }
        }
        for (int i : randOrder) if (!selected2[i]) ans.add(i);
        return ans;
    }
    private List<Pair<Integer, Integer>> extractSolutionFinal(final List<Integer> visitOrder){
        List<Pair<Integer, Integer>> SS = extractSolution(visitOrder);
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        double simValSup, simVal;
        for (Pair<Integer, Integer> item: SS) {
            simVal = similarity[item.getL()][item.getR()];
            if(simVal >= thresholdGoods) {
                res.add(item);
                continue;
            }
            if(similaritySup != null) {
                simValSup = similaritySup[item.getL()][item.getR()];
                if (simVal >= 0.55 && simValSup >= 1.0) {
                    res.add(item);
                    continue;
                }
                if (simVal > 0.65 && simValSup >= 0.8)
                    res.add(item);
            }
        }
        return res;
    }
    private List<Pair<Integer, Integer>> extractSolution(final List<Integer> visitOrder){

        int maxInd;
        double maxVal;
        boolean[] selected = new boolean[col];
        List<Pair<Integer, Integer>> res = new ArrayList<>();
        for (int i : visitOrder) {
            maxInd = -1;
            maxVal = -1;
            for (int j = 0; j < col; ++j) {
                if(similarity[i][j] > maxVal && !selected[j])
                {
                    maxInd = j;
                    maxVal = similarity[i][j];
                }
            }
            if(maxVal >= 0.5) {
                selected[maxInd] = true;
                res.add(new Pair<>(i, maxInd));
            }
        }
        return res;
    }
}
