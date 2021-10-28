import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;

public class QueenProp extends Propagator<IntVar> {
    private final boolean VERBOSE = false;
    private final int n;

    /*
    Variable used to track whose queen's domain size becomes 1, 2 or 3 after removing some values.
    E.g. domainBecame123 will keep the index of the "lowest" queen whose domain size became 1, 2 or 3
    because some values were removed by this constraint. The "lowest" refers to the row index of a queen.
    */
    private int domainBecame123;

    public QueenProp(IntVar[] rQueens, int n) {
        super(rQueens, PropagatorPriority.LINEAR, false);
        this.n = n;
        this.domainBecame123 = n; // This variable is supposed to take values in [0,n-1].
                                  // This out-of-bounds assignment is just to make propagate work properly.
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (VERBOSE) System.out.println("Propagate method called");
        // print domain values for each queen
        printQueenDomains();

        for (int i = 0; i < n; i++) {
            try {
                switch (vars[i].getDomainSize()) {
                    case 1 -> removeOne(i);
                    case 2 -> removeTwo(i);
                    case 3 -> removeThree(i);
                }
            } catch (ContradictionException e) {
                if (VERBOSE) System.out.println("No solution possible! Backtracking...\n");
                throw e;
            }

            if (exists(domainBecame123)) {
                i = domainBecame123-1;
                domainBecame123 = n;  // reset it to out-of-bounds value
            }
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }

    private void removeOne(int row) throws ContradictionException {
        if (VERBOSE) System.out.println("removeOne() called for queen in row " + row);
        int value = vars[row].getValue();
        boolean somethingRemoved = false;
        for (int affectedQueen = 0; affectedQueen < n; affectedQueen++) {
            int rowDiff = row - affectedQueen;
            if (affectedQueen != row) {
                somethingRemoved = vars[affectedQueen].removeValue(value, this);  // same column

                if (exists(value - rowDiff)) {
                    if (vars[affectedQueen].removeValue(value - rowDiff, this)) {  // major diagonal
                        somethingRemoved = true;
                    }
                }
                if (exists(value + rowDiff)) {
                    if(vars[affectedQueen].removeValue(value + rowDiff, this)) {  // minor diagonal
                        somethingRemoved = true;
                    }
                }
                // if this queen is in a row before the selected one and her domain becomes 1,2 or 3
                // we have to warn the propagate method
                if(somethingRemoved && affectedQueen < row) {
                    if (vars[affectedQueen].getDomainSize() <= 3) {
                        if (affectedQueen < domainBecame123) domainBecame123 = affectedQueen;
                    }
                }
            }
        }
        printQueenDomains();
    }

    private void removeTwo(int row) throws ContradictionException {
        if (VERBOSE) System.out.println("removeTwo() called for queen in row " + row);
        DisposableValueIterator vit = vars[row].getValueIterator(true);
        int v1 = vit.next();
        int v2 = vit.next();
        vit.dispose();
        int d = v2 - v1;
        int affectedQueen = row - d; 
        if (exists(affectedQueen)) {
            boolean b1 = vars[affectedQueen].removeValue(v1, this);
            boolean b2 = vars[affectedQueen].removeValue(v2, this);
            if (b1 || b2) { // if some values where removed from the domain
                if (vars[affectedQueen].getDomainSize() <= 3) {
                    if (affectedQueen < domainBecame123) domainBecame123 = affectedQueen;
                }
            }
        }
        affectedQueen = row + d;
        if (exists(affectedQueen)) {
            vars[affectedQueen].removeValue(v1, this);
            vars[affectedQueen].removeValue(v2, this);
        }
        printQueenDomains();
    }

    private void removeThree(int row) throws ContradictionException {
        if (VERBOSE) System.out.println("removeThree() called for queen in row " + row);
        DisposableValueIterator vit = vars[row].getValueIterator(true);
        int v1 = vit.next();
        int v2 = vit.next();
        int v3 = vit.next();
        vit.dispose();
        int d1 = v2 - v1;
        int d2 = v3 - v2;
        if (d1 == d2) {
            int affectedQueen = row - d1;
            if (exists(affectedQueen)) {
                if (vars[affectedQueen].removeValue(v2, this)) { // if some values where removed from the domain
                    if (vars[affectedQueen].getDomainSize() <= 3) {
                        if (affectedQueen<domainBecame123) domainBecame123 = affectedQueen;
                    }
                }
            }
            affectedQueen = row + d1;
            if (exists(affectedQueen)) {
                vars[affectedQueen].removeValue(v2, this);
            }
        }
        printQueenDomains();
    }

    private boolean exists(int index) {
        return index >= 0 && index <n;
    }

    private void printQueenDomains() {
        if (!VERBOSE) return;

        for (IntVar queen : vars) {
            boolean wait = false;
            int v = -1;
            DisposableValueIterator vit = queen.getValueIterator(true);
            for (int i = 0; i<n; i++) {
                if (!wait) {
                    if (vit.hasNext()){
                        v = vit.next();
                    } else {
                        v = -1;
                    }
                }

                if (v == i) {
                    if (i<10) System.out.print("0" + i + " ");
                    else System.out.print(i + " ");
                    wait = false;
                } else {
                    System.out.print("-- ");
                    wait = true;
                }
            }
            vit.dispose();
            System.out.println();
        }
        System.out.println();
    }
}
