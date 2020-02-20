import os
import pulp
import re
import time


def pizzaProblem(filename):
    # Create a LP Minimization problem
    prob = pulp.LpProblem('Hashcode', pulp.LpMaximize)

    # Parse the problem and create variables
    with open(filename) as f:
        lines = f.readlines()
        f.close()
    ints = [[int(e) for e in line.strip().split(" ")] for line in lines]

    maxSlices = ints[0][0]
    maxPizzas = ints[0][1]
    slices = ints[1]

    vars = []
    for i in range(len(slices)):
        vars.append(pulp.LpVariable('x{}'.format(i), cat=pulp.constants.LpBinary))

    # Objective Function ac
    prob += sum([slices[int(var.name[1:])] * var for var in vars])

    # Constraints:
    prob += sum(vars) <= maxPizzas  # trival constraint
    prob += prob.objective <= maxSlices

    # Display the problem
    # print(prob)

    status = prob.solve()  # Solver
    print(pulp.LpStatus[status])  # The solution status

    # Printing the final solution
    # for var in vars:
    #     print(pulp.value(var))

    print(pulp.value(prob.objective))
    print("Difference: {}".format(maxSlices - pulp.value(prob.objective)))


if __name__ == '__main__':
    files = ["a_example.in", "b_small.in", "c_medium.in", "d_quite_big.in", "e_also_big.in"]

    dir = re.sub("hashcode.*", "hashcode/{}", os.path.dirname(os.path.realpath(__file__)))
    filenames = [dir.format(file) for file in files]

    for filename in filenames:
        tic = time.perf_counter()
        pizzaProblem(filename)
        toc = time.perf_counter()
        print("{} seconds".format(toc - tic))
