import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

public class QueenModel {
    private static final boolean print = false;
    private static final int n = 13;

    public static void main(String[] args) {
        Model model = new Model(n + "-queens problem primal");
        IntVar[] rQueens = model.intVarArray("RQ", n, 0, n-1, false);

        /* Constraints */
        Constraint myConstraint = new Constraint("QueenConstraint",new QueenProp(rQueens, n));
        myConstraint.post();

        /* Solving and enumerating */
        Solver solver = model.getSolver();
        if (print) {
            for (int i = 1; solver.solve(); i++) {
                System.out.println("****** Solution n° " + i + " ******");
                printSolution(rQueens, n);
            }
        } else {
            while(solver.solve());
        }

        solver.printStatistics();
    }

    public static void printSolution(IntVar[] rQueens, int n) {
        // check if solution is valid
        boolean problem = false;
        for (int i = 0; i < n-1; i++) {
            for (int j = i+1; j < n; j++) {
                if (rQueens[i].getValue() == rQueens[j].getValue()) problem = true;
                if (i-rQueens[i].getValue() == j-rQueens[j].getValue()) problem = true;
                if (i+rQueens[i].getValue() == j+rQueens[j].getValue()) problem = true;
                if (problem) break;
            }
        }

        if (problem) {
            System.out.println("!!!!!!!!!!!!!!! THIS SOLUTION SEEMS TO BE NOT VALID !!!!!!!!!!!!!!!!!!!");
        }

        int[][] solved_matrix = new int[n][n];

        // print graphical solution
        for (int i = 0; i < n; i++) {
            solved_matrix[i][rQueens[i].getValue()] = 1;
        }

        printMatrix(solved_matrix, n);

    }

    public static void printMatrix(int matrix[][], int n) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if(matrix[i][j] == 1) {
                    System.out.print("xx ");
                } else {
                    System.out.print("-- ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
