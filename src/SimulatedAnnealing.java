import java.util.Arrays;

public class SimulatedAnnealing {
    boolean[] state;
    Integer[] slices;
    int sum;
    int max;
    int minDiff;
    boolean[] minState;

    double T = 1;
    static final double Tmin = 1e-6;
    static final double alpha = .99;
    static final double iterations = 1e6;

    SimulatedAnnealing(boolean[] state, Integer[] slices, int sum, int max) {
        this.state = state;
        this.slices = slices;
        this.sum = sum;
        this.max = max;
        minDiff = Integer.MAX_VALUE;
        minState = Arrays.copyOf(state, state.length);
    }

    void simulate() {
        // Continues annealing until reaching minimum temp
        while (T > Tmin) {
            for (int i = 0; i < iterations; i++) {
                int index = permute();
                // determine if wanna switch
                if (state[index]) {
                    sum -= slices[index];
                } else {
                    sum += slices[index];
                }
                state[index] = !state[index];

//                sum = HashcodePractice.twoSwap(state, slices, sum, max, false);
                int newDiff = max - sum;
                if (newDiff < minDiff && newDiff >= 0) {
                    minDiff = newDiff;
                    for (int j = 0; j < minState.length; j++) {
                        minState[j] = state[j];
                    }
                    if (minDiff == 0) {
                        return;
                    }
                }
            }
            T *= alpha;
        }
    }

    // move the state space to a neighbor
    int permute() {
        int n = state.length;
        return (int) (Math.random() * n);
    }
}