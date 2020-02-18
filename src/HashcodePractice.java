import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class HashcodePractice {

    // returns an ArrayList of lines in a file
    static ArrayList<String> readLines(String filename) {
        ArrayList<String> lines = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                lines.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    // returns a ArrayList of space delimited numbers in a line
    static ArrayList<Integer> parseInts(String line) {
        ArrayList<Integer> nums = new ArrayList<>();

        for (String e : line.split(" ")) {
            nums.add(Integer.parseInt(e));
        }
        return nums;
    }

    // creates a boolean array representing the solution of the greedy
    static boolean[] solFromGreedy(int left, int right, Integer[] slices) {
        boolean[] state = new boolean[slices.length];

        for (int i = 0; i <= left; i++) {
            state[i] = true;
        }
        for (int i = right; i < slices.length; i++) {
            state[i] = true;
        }
        return state;
    }

    // sum of the current state
    static int sum(boolean[] state, Integer[] slices) {
        int sum = 0;

        for (int i = 0; i < state.length; i++) {
            if (state[i]) {
                sum += slices[i];
            }
        }
        return sum;
    }

    // validates a solution and writes it to disk in a file and returns the sum
    static int validateWrite(boolean[] state, Integer[] slices, String filename) {
        StringBuilder sb = new StringBuilder();

        int sum = 0;
        int total = 0;
        for (int i = 0; i < state.length; i++) {
            if (state[i]) {
                sb.append(i);
                sb.append(' ');
                ++total;
                sum += slices[i];
            }
        }
        sb.insert(0, total + "\n");
        sb.deleteCharAt(sb.length() - 1);

        // write the solution
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sum;
    }

    // greedy sum maximizer at filename
    static int greedySolver(String filename) {
        ArrayList<String> lines = readLines(filename);
        Integer[][] intLines = new Integer[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            intLines[i] = parseInts(lines.get(i)).toArray(new Integer[0]);
        }

        // actual problem (greedy approach)
        int maxSlices = intLines[0][0];
        int pizzaTypes = intLines[0][1];
        Integer[] slices = intLines[1];

        // rightSum is the sum from rightIndex to the end
        int rightIndex = pizzaTypes - 1;
        int rightSum = 0;
        for (; rightIndex >= 0; rightIndex--) {
            rightSum += slices[rightIndex];
            if (rightSum > maxSlices) {
                break;
            }
        }
        rightSum -= slices[rightIndex++];

        // leftSum is the sum from the start to leftIndex
        int leftUpper = maxSlices - rightSum;
        int leftIndex = 0;
        int leftSum = 0;
        for (; leftIndex < rightIndex; leftIndex++) {
            leftSum += slices[leftIndex];
            if (leftSum > leftUpper) {
                break;
            }
        }
        leftSum -= slices[leftIndex--];

        int remaining = maxSlices - leftSum - rightSum;
        int newRemaining = remaining;

        // decrement rightIndex and advance leftIndex forward
        int prevLeftIndex = leftIndex;
        int prevRightIndex = rightIndex;
        while (newRemaining <= remaining) {
            prevLeftIndex = leftIndex;
            prevRightIndex = rightIndex;

            rightSum -= slices[rightIndex++];
            leftUpper = maxSlices - rightSum;

            for (; leftIndex < rightIndex; leftIndex++) {
                leftSum += slices[leftIndex];
                if (leftSum > leftUpper) {
                    break;
                }
            }
            leftSum -= slices[leftIndex--];

            remaining = newRemaining;
            newRemaining = maxSlices - leftSum - rightSum;
        }

        boolean[] state = solFromGreedy(prevLeftIndex, prevRightIndex, slices);
        twoSwap(state, slices, sum(state, slices), maxSlices, true);
        int sum = validateWrite(state, slices, filename.replace(".in", ".out"));
        return maxSlices - sum;
    }

    // swap two elements at a time until the gain becomes 0
    static int twoSwap(boolean[] state, Integer[] slices, int sum, int max, boolean recurse) {
        int swapOut = 0;
        int swapIn = 0;

        int remaining = max - sum;
        int bestDiff = 0;
        for (int out = 0; out < slices.length; out++) {
            for (int in = out; in < slices.length; in++) {
                if (state[out] && !state[in]) {
                    int diff = slices[in] - slices[out];
                    if (diff <= remaining) {
                        if (diff > bestDiff) {
                            bestDiff = diff;
                            swapIn = in;
                            swapOut = out;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        state[swapIn] = !state[swapIn];
        state[swapOut] = !state[swapOut];

        int newSum = sum - slices[swapOut] + slices[swapIn];
        if (newSum != sum && recurse) {
            return twoSwap(state, slices, newSum, max, recurse);
        }
        return sum;
    }

    // simulated annealing maximizer at filename
    static int npSolver(String filename) {
        ArrayList<String> lines = readLines(filename);
        Integer[][] intLines = new Integer[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            intLines[i] = parseInts(lines.get(i)).toArray(new Integer[0]);
        }

        // actual problem (greedy approach)
        int maxSlices = intLines[0][0];
        int pizzaTypes = intLines[0][1];
        Integer[] slices = intLines[1];

        // rightSum is the sum from rightIndex to the end
        int rightIndex = pizzaTypes - 1;
        int rightSum = 0;
        for (; rightIndex >= 0; rightIndex--) {
            rightSum += slices[rightIndex];
            if (rightSum > maxSlices) {
                break;
            }
        }
        rightSum -= slices[rightIndex++];

        // leftSum is the sum from the start to leftIndex
        int leftUpper = maxSlices - rightSum;
        int leftIndex = 0;
        int leftSum = 0;
        for (; leftIndex < rightIndex; leftIndex++) {
            leftSum += slices[leftIndex];
            if (leftSum > leftUpper) {
                break;
            }
        }
        leftSum -= slices[leftIndex--];

        int remaining = maxSlices - leftSum - rightSum;
        int newRemaining = remaining;

        // decrement rightIndex and advance leftIndex forward
        int prevLeftIndex = leftIndex;
        int prevRightIndex = rightIndex;
        while (newRemaining <= remaining) {
            prevLeftIndex = leftIndex;
            prevRightIndex = rightIndex;

            rightSum -= slices[rightIndex++];
            leftUpper = maxSlices - rightSum;

            for (; leftIndex < rightIndex; leftIndex++) {
                leftSum += slices[leftIndex];
                if (leftSum > leftUpper) {
                    break;
                }
            }
            leftSum -= slices[leftIndex--];

            remaining = newRemaining;
            newRemaining = maxSlices - leftSum - rightSum;
        }

//        boolean[] state = solFromGreedy(prevLeftIndex, prevRightIndex, slices);
//        twoSwap(state, slices, sum(state, slices), maxSlices);
        boolean[] state = new boolean[slices.length];
        SimulatedAnnealing sa = new SimulatedAnnealing(state, slices, sum(state, slices), maxSlices);
        sa.simulate();
        int sum = validateWrite(sa.minState, slices, filename.replace(".in", ".out"));
        return maxSlices - sum;
    }

    public static void main(String[] args) {
        String[] files = {"a_example.in", "b_small.in", "c_medium.in", "d_quite_big.in", "e_also_big.in"};
        String dir = System.getProperty("user.dir") + "/";
        if (!dir.contains("src")) {
            dir += "src/";
        }

        Semaphore sem = new Semaphore(1);
        for (int i = 0; i < files.length; i++) {
            String filename = dir + files[i];
            int diff = greedySolver(filename);
            int diffSA = npSolver(filename);
            System.out.println(filename + " -> greedy: " + diff + ", sa: " + diffSA + "\n");
        }
    }

    static class Solver extends Thread {
        String filename;
        Semaphore sem;

        Solver(String dir, String target, Semaphore sem) {
            super(target);
            filename = dir + target;
            this.sem = sem;
        }

        @Override
        public void run() {
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int diff = greedySolver(filename);
            int diffSA = npSolver(filename);
            System.out.println(this.getName() + " results:\n"
                    + "Greedy: " + diff + ", sa: " + diffSA + "\n");
            sem.release();
        }
    }
}