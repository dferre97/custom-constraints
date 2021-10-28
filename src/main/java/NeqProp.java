import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

public class NeqProp extends Propagator<IntVar> {

    public NeqProp(IntVar x, IntVar y) {
        super(ArrayUtils.toArray(x, y), PropagatorPriority.BINARY, false);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vars[0].getDomainSize() == 1) {
            vars[1].removeValue(vars[0].getValue(),this);
        }
        if (vars[1].getDomainSize() == 1) {
            vars[0].removeValue(vars[1].getValue(),this);
        }
    }

    @Override
    public ESat isEntailed() {
        if (vars[0].getUB() < vars[1].getLB() ||
                vars[1].getUB() < vars[0].getLB()) {
            return ESat.TRUE;
        } else if (vars[0].getDomainSize() == 1 && vars[0].getDomainSize() == 1) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }
}
