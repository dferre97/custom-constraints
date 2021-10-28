import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

public class NeqModel {
    public static void main(String[] args) {
        Model model = new Model( "Custom constraint");
        IntVar x = model.intVar("x",1,3);
        IntVar y = model.intVar("y",1,3);

        new Constraint("NeqConstraint", new NeqProp(x,y)).post();

        /* Solving and enumerating */
        Solver solver = model.getSolver();
        Solution solution = new Solution(model);
        while(solver.solve()) {
            solution.record();
            System.out.println(solution);
        }
        solver.printStatistics();
    }
}
