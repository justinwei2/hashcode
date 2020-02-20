import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

public class Hashcode {

    // parses the input file as a 2d int array
    static Integer[][] parseFile(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String[] lines = reader.lines().toArray(String[]::new);
            reader.close();

            Integer[][] nums = new Integer[lines.length][];
            for (int i = 0; i < lines.length; i++) {
                nums[i] = Arrays.stream(lines[i].split(" ")).map(Integer::parseInt).toArray(Integer[]::new);
            }
            return nums;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    // swap two elements at a time until the gain becomes 0
    static int twoSwap(boolean[] state, Integer[] slices, int sum, int max) {
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
        if (newSum != sum) {
            return twoSwap(state, slices, newSum, max);
        }
        return newSum;
    }

    // greedy sum maximizer at filename
    static int greedySolver(String filename) {
        Integer[][] nums = parseFile(filename);

        // actual problem (greedy approach)
        int maxSlices = nums[0][0];
        int maxPizzas = nums[0][1];
        Integer[] slices = nums[1];

        // rightSum is the sum from rightIndex to the end
        int rightIndex = maxPizzas - 1;
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

        // create state representation and gradient descent
        boolean[] state = new boolean[slices.length];
        for (int i = 0; i <= prevLeftIndex; i++) {
            state[i] = true;
        }
        for (int i = prevRightIndex; i < slices.length; i++) {
            state[i] = true;
        }
        twoSwap(state, slices, sum(state, slices), maxSlices);

        // write solution to file
        int sum = validateWrite(state, slices, filename.replace(".in", "_greedy.out"));
        return maxSlices - sum;
    }

    // simulated annealing maximizer at filename
    static int saSolver(String filename) {
        Integer[][] nums = parseFile(filename);

        // actual problem (simulated annealing approach)
        int maxSlices = nums[0][0];
        int maxPizzas = nums[0][1];
        Integer[] slices = nums[1];

        SimulatedAnnealing sa = new SimulatedAnnealing(slices, maxPizzas, maxSlices);
        sa.simulate();

        // write solution to file
        int sum = validateWrite(sa.minState, slices, filename.replace(".in", "_sa.out"));
        return maxSlices - sum;
    }

    public static void main(String[] args) {
        String[] files = {"a_example.in", "b_small.in", "c_medium.in", "d_quite_big.in", "e_also_big.in"};
        String dir = System.getProperty("user.dir").replaceAll("hashcode.*", "hashcode/");

        for (String file : files) {
            String filename = dir + file;
            int gDiff = greedySolver(filename);
            int saDiff = saSolver(filename);
            System.out.println(file + " -> greedy: " + gDiff + ", sa: " + saDiff + "\n");
        }
    }
}