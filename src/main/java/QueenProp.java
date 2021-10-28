import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;

public class QueenProp extends Propagator<IntVar> {
    private boolean VERBOSE = false;
    private final int n;
    private static int count = 0;

    /*
    Variables used to track whose queen's domain size becomes 1, 2 or 3 after removing some values.
    E.g. domainBecame1 will keep the index of the "lowest" queen whose domain size became 1 because
    some values were removed by this constraint. The "lowest" refers to the row index of a queen.
    */
    private int domainBecame1;
    private int domainBecame2;
    private int domainBecame3;
    private int domainBecame123;

    public QueenProp(IntVar[] rQueens, int n) {
        super(rQueens, PropagatorPriority.LINEAR, false);
        this.n = n;

        this.domainBecame1 = n; // These variables should have a range [0,n-1].
        this.domainBecame2 = n; // They are assigned to a higher value (in this case n) so that the propagate algorithm behaves correctly
        this.domainBecame3 = n;
        this.domainBecame123 = n;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (VERBOSE) System.out.println("Propagate method called "+count);
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

            if (exists(domainBecame1)) {
                i = domainBecame1-1; // go back to check the first queen whose domain size became 1
                domainBecame1 = n; // restore default value
            }
            if (exists(domainBecame2) && domainBecame2-1<i) {
                i = domainBecame2-1; // go back to check the first queen whose domain size became 2
                domainBecame2 = n; // restore default value
            }            
            if (exists(domainBecame3) && domainBecame3-1<i) {
                i = domainBecame3-1; // go back to check the first queen whose domain size became 3
                domainBecame3 = n; // restore default value
            }
//            if (exists(domainBecame123)) {
//                i = domainBecame123-1;
//                domainBecame123 = n;
//            }
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
        for (int i = 0; i < n; i++) {
            int rowDiff = row - i;
            if (i != row) {
                somethingRemoved = vars[i].removeValue(value, this);  // same column

                if (exists(value - rowDiff)) {
                    if (vars[i].removeValue(value - rowDiff, this)) {  // major diagonal
                        somethingRemoved = true;
                    }
                }
                if (exists(value + rowDiff)) {
                    if(vars[i].removeValue(value + rowDiff, this)) {  // minor diagonal
                        somethingRemoved = true;
                    }
                }
                // if this queen is in a row before the selected one and her domain becomes 1,2 or 3
                // I have to warn the propagate method
                if(somethingRemoved && i<row) {
                    switch (vars[i].getDomainSize()) {
                        case 1:
                            if (i<domainBecame1) domainBecame1 = i;
                            break;
                        case 2:
                            if (i<domainBecame2) domainBecame2 = i;
                            break;
                        case 3:
                            if (i<domainBecame3) domainBecame3 = i;
                            break;
                    }
//                    if (vars[i].getDomainSize() <= 3) {
//                        if (i<domainBecame123) domainBecame123 = i;
//                    }
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
        if (exists(row - d)) {
            boolean b1 = vars[row - d].removeValue(v1, this);
            boolean b2 = vars[row - d].removeValue(v2, this);
            if (b1 || b2) { // if some values where removed from the domain
                switch (vars[row - d].getDomainSize()) {
                    case 1:
                        if (row < domainBecame1) domainBecame1 = row;
                        break;
                    case 2:
                        if (row < domainBecame2) domainBecame2 = row;
                        break;
                    case 3:
                        if (row < domainBecame3) domainBecame3 = row;
                        break;
                }
//                if (vars[row].getDomainSize() <= 3) {
//                    if (row<domainBecame123) domainBecame123 = row;
//                }
            }
        }
        if (exists(row + d)) {
            vars[row + d].removeValue(v1, this);
            vars[row + d].removeValue(v2, this);
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
        if (d1 == d2 && d1 >1) {
            if (exists(row - d1)) {
                boolean somethingChanged = vars[row - d1].removeValue(v2, this);
                if (somethingChanged) { // if some values where removed from the domain
                    switch (vars[row - d1].getDomainSize()) {
                        case 1:
                            if (row < domainBecame1) domainBecame1 = row;
                            break;
                        case 2:
                            if (row < domainBecame2) domainBecame2 = row;
                            break;
                        case 3:
                            if (row < domainBecame3) domainBecame3 = row;
                            break;
                    }
//                if (vars[row].getDomainSize() <= 3) {
//                    if (row<domainBecame123) domainBecame123 = row;
//                }
                }
            }
            if (exists(row + d1)) {
                vars[row + d1].removeValue(v2, this);
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
                    System.out.print(i + "" + i + " ");
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
