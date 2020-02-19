public class SimulatedAnnealing {
    // state representation
    final boolean[] state;
    final Integer[] slices;
    final int maxPizzas;
    final int maxSlices;

    // simulation cached values
    int sum;
    int minDiff;
    final boolean[] minState;

    // simulation parameters
    double T = 1;
    static final double T_MIN = 1e-5;
    static final double alpha = .95;
    static final double iterations = 1e5;

    SimulatedAnnealing(Integer[] slices, int maxPizzas, int maxSlices) {
        state = new boolean[slices.length];
        this.slices = slices;
        this.maxPizzas = maxPizzas;
        this.maxSlices = maxSlices;

        sum = 0;
        minDiff = maxSlices;
        minState = new boolean[slices.length];
    }

    // continues annealing until minimum temp is reached
    void simulate() {
        while (T > T_MIN) {
            for (int i = 0; i < iterations; i++) {
                int flipIndex = (int) (state.length * Math.random());

                // calculate neighbor state attributes
                int diff = maxSlices - sum;
                if (state[flipIndex]) {
                    diff += slices[flipIndex];
                } else {
                    diff -= slices[flipIndex];
                }

                // decide whether to move to neighbor state
                double prob = ((double) diff) / minDiff;
                if (prob < T) {
                    state[flipIndex] = !state[flipIndex];
                    sum = maxSlices - diff;

                    if (tryCacheMinState(diff)) return;
                }
            }
            T *= alpha;
        }
    }

    // caches a new minimum state (returns if global optima is reached)
    boolean tryCacheMinState(int diff) {
        if (diff < minDiff && diff >= 0) {
            minDiff = diff;
            System.arraycopy(state, 0, minState, 0, state.length);
        }
        return minDiff == 0;
    }
}