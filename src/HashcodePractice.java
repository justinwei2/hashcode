import java.io.*;
import java.util.*;

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
        boolean[] used = new boolean[slices.length];

        for (int i = 0; i <= left; i++) {
            used[i] = true;
        }
        for (int i = right; i < slices.length; i++) {
            used[i] = true;
        }
        return used;
    }

    // validates a solution and writes it to disk in a file and returns the sum
    static int validateWrite(boolean[] used, Integer[] slices, String filename) {
        StringBuilder sb = new StringBuilder();

        int sum = 0;
        int total = 0;
        for (int i = 0; i < used.length; i++) {
            if (used[i]) {
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
    static void greedySolver(String filename) {
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

        boolean[] used = solFromGreedy(prevLeftIndex, prevRightIndex, slices);
        int sum = validateWrite(used, slices, filename.replace(".in", ".out"));
        System.out.println(maxSlices - sum);
        twoSwap(used, slices, sum, maxSlices);
        sum = validateWrite(used, slices, filename.replace(".in", ".out"));
        System.out.println(maxSlices - sum);
        System.out.println();
    }

    // swap two elements at a time until the gain becomes 0
    static int twoSwap(boolean[] used, Integer[] slices, int sum, int max) {
        int swapOut = 0;
        int swapIn = 0;

        int remaining = max - sum;
        int bestDiff = 0;
        for (int out = 0; out < slices.length; out++) {
            for (int in = out; in < slices.length; in++) {
                if (used[out] && !used[in]) {
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
        used[swapIn] = !used[swapIn];
        used[swapOut] = !used[swapOut];

        int newSum = sum - slices[swapOut] + slices[swapIn];
        if (newSum != sum) {
            return twoSwap(used, slices, newSum, max);
        }
        return sum;
    }

    // simulated annealing maximizer at filename
    static void npSolver(String filename) {

    }

    public static void main(String[] args) {
        String[] files = {"a_example.in", "b_small.in", "c_medium.in", "d_quite_big.in", "e_also_big.in"};
        String dir = System.getProperty("user.dir") + "/";
        if (!dir.contains("src")) {
            dir += "src/";
        }
        for (String file : files) {
            greedySolver(dir + file);
        }
    }
}