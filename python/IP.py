# import the library pulp as p 
import pulp as p

# Create a LP Minimization problem 
prob = p.LpProblem('Knapsack', p.LpMaximize)

# Create problem
values = [60, 100, 120]
costs = [10, 20, 30]
mw = 50;

vars = []
for i in range(len(values)):
    vars.append(p.LpVariable('x{}'.format(i), cat=p.constants.LpBinary))

# Objective Function
prob += sum([values[int(var.name[1])] * var for var in vars])

# Constraints: 
prob += sum([costs[int(var.name[1])] * var for var in vars]) <= mw

# Display the problem 
print(prob)

status = prob.solve()  # Solver
print(p.LpStatus[status])  # The solution status

# Printing the final solution
for var in vars:
    print(p.value(var))
print(p.value(prob.objective))
