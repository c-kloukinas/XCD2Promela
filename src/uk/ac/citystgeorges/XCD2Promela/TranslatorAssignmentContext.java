package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorAssignmentContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (AssignmentContext)ctx); }
    public T translate(BaseVisitor<T> bv, AssignmentContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";
        T lhs = bv.visit(ctx.lhs);
        bv.myassert(lhs.size()==1
                    , "LeftHandSide: didn't return exactly one element"
                    + lhs.size());
        if (ctx.is!=null) {
            T assgnExpr = bv.visit(ctx.assgnExpr);
            if (assgnExpr.size()==1) {
                s = lhs.get(0)
                    + "="
                    + assgnExpr.get(0);
            } else {            // assignment chaining: x := y := z \in [2, 4];
                s = "";
                while (assgnExpr.size()!=1) {
                    s += assgnExpr.get(0)
                        + "; ";
                    assgnExpr.remove(0);
                }
                s += lhs.get(0)
                    + "="
                    + assgnExpr.get(0);
                // bv.mywarning("*** final assignment is:\n" + s);
            }
        } else if (ctx.inRange!=null) { // inRange
            T theRange = bv.visit(ctx.theRange);
            String valMin = theRange.get(0);
            String valMax = theRange.get(1);
            String theLoopIndex = bv.loopGenSym;
            // ensure the range isn't empty
            s = "assert( (" + valMin + ")<=(" + valMax + ") );";
            s += "atomic {\n  "
                + theLoopIndex + "=" + valMin + ";\n"
                + "  do\n"
                + "  :: " + theLoopIndex + "<" + valMax
                /* Option one - choose this value */
                + " -> break;"
                + "  :: " + theLoopIndex + "<" + valMax
                /* Option two - try next value */
                + " -> " + theLoopIndex + "=" + theLoopIndex + "+1;"
                /* Final option - must choose it */
                + "  :: " + theLoopIndex + "==" + valMax + " -> break;"
                + "  od ; skip ;\n"
                + "  " + lhs.get(0) + "=" + theLoopIndex + ";\n"
                /* Reset theLoopIndex to save states */
                + "  " + theLoopIndex + "=0 ;\n"
                + "}\n";
        } else {                    // inSet
            T theSet = bv.visit(ctx.theSet);
            bv.myassert(theSet.size()!=0
                        , "Assignment: Cannot choose a value from an empty set");
            s += "atomic {\n  "
                + "  if\n";
            for (var val : theSet)
                s += "  :: true -> " + lhs.get(0) + "=" + val + "; break;\n";
            s += "  fi; skip\n"
                + "}\n";
        }
        res.add(s);
        res.add(lhs.get(0)); // add this so chained assignments know
                             // what to assign themselves to
        return res;
    }
}
