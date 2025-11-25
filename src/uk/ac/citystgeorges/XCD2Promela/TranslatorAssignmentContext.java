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
        if (ctx.lhsIs!=null)
            s = bv.visit(ctx.lhsIs).get(0)
                + "="
                + bv.visit(ctx.assgnExpr);
        else if (ctx.lhsIn!=null) { // inRange
            String lhs = bv.visit(ctx.lhsIn).get(0);
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
                + "  " + lhs + "=" + theLoopIndex + ";\n"
                /* Reset theLoopIndex to save states */
                + "  " + theLoopIndex + "=0 ;\n"
                + "}\n";
        } else {                    // inSet
            String lhs = bv.visit(ctx.lhsIn).get(0);
            T theSet = bv.visit(ctx.theSet);
            bv.myassert(theSet.size()!=0
                        , "Assignment: Cannot choose a value from an empty set");
            s += "atomic {\n  "
                + "  if\n";
            for (var val : theSet)
                s += "  :: true -> " + lhs + "=" + val + "; break;\n";
            s += "  fi; skip\n"
                + "}\n";
        }
        res.add(s);
        return res;
    }
}
