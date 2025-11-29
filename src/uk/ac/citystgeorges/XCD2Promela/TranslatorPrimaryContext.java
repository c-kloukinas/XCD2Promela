package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslatorPrimaryContext implements TranslatorI {
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (PrimaryContext)ctx); }
    public T translate(BaseVisitor<T> bv, PrimaryContext ctx) {
        bv.updateln(ctx);
        T res = new T(1);
        String s = "";
        LstStr argList = new LstStr();
        if (ctx.lit != null) { // NUM
            res = bv.visit(ctx.lit);
            return res;
        } else if (ctx.parExpr!=null) {
            s = bv.visit(ctx.parExpr).get(0);
        } else if (ctx.name != null) {
            String varid = ctx.name.getText();
            String trans = translate_ID(bv, varid);
            s = trans;
        } else if (ctx.at != null) {
            var framenow = bv.symbolTableNow();
            var compTypeid = framenow.compilationUnitID;
            s += "@UNKNOWNAT";
        } else if (ctx.theResult != null) {
            s = bv.component_variable_result("ACTIONNOTKNOWN");
        } else if (ctx.theException != null) {
            s = bv.component_variable_exception("ACTIONNOTKNOWN");
        } else if (ctx.funcCall!= null) {
            var args = bv.visit(ctx.funcCall);
        } else if (ctx.arrayAcc!=null) {
            s = bv.visit(ctx.arrayAcc).get(0);
        } else
            bv.myassert(false, "Unknown case of nullaryExpression");
        // System.out.println("Translation is: " + s);
        res.add(s);
        return res;
    }

    String translate_ID(BaseVisitor<T> bv, String id) {
        bv.myassert(id!=null && !id.equals("")
                    , "empty name for a variable");
        IdInfo idinfo = bv.getIdInfo(id);
        if (idinfo.type==XCD_type.typet
            || idinfo.type==XCD_type.typedeft) {
            if (idinfo.translation.size()!=0)
                return idinfo.translation.get(0);
            else {
                String realType = idinfo.variableTypeName;
                /* res (the real type) may itself be a typedef, so we need
                 * to recurse, until we find its translation */
                DataTypeContext type4recursion = bv.makeDataType(realType);
                var trans = bv.visit(type4recursion).get(0);
                // Store it for next time
                idinfo.translation.add(trans);
                bv.mywarning("TypeDefDeclaration: type "
                             + id
                             + " ("
                             + realType
                             + ") translated to type " + trans);
                return trans;
            }
        } else {
            bv.myassert((idinfo.translation!=null
                         && idinfo.translation.size()>0)
                        , "Missing translation for \""
                        + id
                        + "\" inside environment \""
                        + bv.symbolTableNow().compilationUnitID
                        + "\" of type: "
                        + bv.symbolTableNow().type);
            return idinfo.translation.get(0);
        }
    }
}
