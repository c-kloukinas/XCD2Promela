package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorLeftHandSideContext
    extends TranslatorPrimaryContext
    implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (LeftHandSideContext)ctx); }
    public T translate(BaseVisitor<T> bv, LeftHandSideContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        // mywarning("TESTING: Here're the result and exception names:"
        //           + "\n\t" + bv.etThisMethodResultName()
        //           + "\n\t" + bv.getThisMethodExceptionName());
        if (ctx.name!=null) {
            String name = ctx.name.getText();
            // for (var v : bv.getAssignableName(name, true))
            //     res.add( Names.varPostName(v) );
            bv.globalAssignableName=true;
            res.addAll( bv.getAssignableName(name) );
            bv.globalAssignableName=false;
        } else if (ctx.res!=null) {
            res.add( bv.getThisMethodResultName() );
        } else if (ctx.exc!=null) {
            res.add( bv.getThisMethodExceptionName() );
        } else {                // arrayAcc
            bv.globalAssignableName=true;
            T arr = bv.visit(ctx.arrayAcc);
            bv.globalAssignableName=false;
            bv.myassert(arr.size()==1
                        , "ArrayAccess didn't return exactly one element");
            res = arr;
        }
        return res;
    }
}
