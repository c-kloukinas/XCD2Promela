package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorALiteralContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (ALiteralContext)ctx); }
    public T translate(BaseVisitor<T> bv, ALiteralContext ctx) {
        bv.updateln(ctx);
        T res = new T(1);
        String val = "";
        if (ctx.trueToken!=null)
          val=Names.True;
        else if (ctx.falseToken!=null)
          val=Names.False;
        else
          val=ctx.number.getText();
        res.add(val);
        return res;
    }
}
