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
        String theLHS = lhs.get(0);
        bv.myassert(lhs.size()==1
                    , "LeftHandSide: didn't return exactly one element"
                    + lhs.size());
        if (ctx.is!=null) {
            T assgnExpr = bv.visit(ctx.assgnExpr);
            if (assgnExpr.size()==1) {
                s = theLHS
                    + "="
                    + assgnExpr.get(0);
            } else {            // assignment chaining: x := y := z \in [2, 4];
                s = assgnExpr.get(0) // = assignment expression so far
                    + "; ";
                s += theLHS
                    + "="
                    + assgnExpr.get(1); // = last variable assigned
                // bv.mywarning("*** final assignment is:\n" + s);
            }
        } else if (ctx.inRange!=null) { // inRange
            T theRange = bv.visit(ctx.theRange);
            String valMin = theRange.get(0);
            String valMax = theRange.get(1);
            // ensure the range isn't empty
            s = "assert( (" + valMin + ")<=(" + valMax + ") );"
                + "select( "
                + theLHS
                + " : ( " + valMin + " ) .. ( "
                + valMax
                + " ) )";
        } else {                    // inSet
            T theSet = bv.visit(ctx.theSet);
            bv.myassert(theSet.size()!=0
                        , "Assignment: Cannot choose a value from an empty set");
            s += "  if";
            for (var val : theSet)
                s += "  :: true -> " + theLHS + "=" + val + "; break;";
            s += "  fi";
        }
        res.add(s);
        res.add(theLHS);       // add this so that chained assignments
                               // know what to assign themselves to
        return res;
    }
}
