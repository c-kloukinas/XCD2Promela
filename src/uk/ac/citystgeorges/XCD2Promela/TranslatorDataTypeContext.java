package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorDataTypeContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (DataTypeContext)ctx); }

    public T translate(BaseVisitor<T> bv, DataTypeContext ctx) {
        bv.updateln(ctx);
        T res = new T();
        String s = "";

        if (ctx.basic!=null) {
            switch (ctx.basic.getType()) {
            case XCDParser.TK_INTEGER:
                s = Names.Int;
                break;
            case XCDParser.TK_SHORT:
                s = Names.Short;
                break;
            case XCDParser.TK_BYTE:
                s = Names.Byte;
                break;
            case XCDParser.TK_BOOL:
                s = Names.Bit;
                break;
            case XCDParser.TK_VOID:
                s = Names.Void;
                break;
            }
        } else {
            s = bv.visitChildren(ctx).get(0);
        }
        res.add(s);
        return res;
    }

}
