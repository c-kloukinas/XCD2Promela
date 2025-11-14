package uk.ac.citystgeorges.XCD2Promela;
import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class TranslatorNullaryExpressionContext implements TranslatorI
{
    @Override
    public T translate(BaseVisitor<T> bv, ParserRuleContext ctx) {
        return translate(bv, (NullaryExpressionContext)ctx); }
    public T translate(BaseVisitor<T> bv, NullaryExpressionContext ctx) {
        bv.updateln(ctx);
        T res = new T(1);
        String s = "";
        LstStr argList = new LstStr();

        if (ctx.number != null) { // NUM
            res = bv.visit(ctx.number);
            return res;
        } else if (ctx.var_withpar != null) {
            if (ctx.pre != null)
                s = Names.varPreName(bv.visit(ctx.var_withpar).get(0));
        } else if (ctx.trueToken != null)
            s = Names.True;
        else if (ctx.falseToken != null)
            s = Names.False;
        else if (ctx.at != null) {
            var framenow = bv.frameNow();
            var compTypeid = framenow.compilationUnitID;
            // var idInfo = getIdInfo(id);
            // var var_prefix = idInfo.varPrefix;
            // // var portid = framenow.portID;
            // var portid = "UNKNOWN";
            // if (var_prefix.contains("COMPONENT"))
            //  s += var_prefix
            //      + "PORT_"
            //      + portid
            //      + "_INDEX";
            // else
            //  s += "COMPONENT_"
            //      + "COMPONENTPREFIX_VAR_PORT_"
            //      + portid
            //      + "_INDEX";
            s += "@UNKNOWNAT";
        } else if (ctx.varid != null) {
            String varid = ctx.varid.getText();
            String trans = translate_ID(bv, varid);
            if (bv.is_enumConstant(varid)) {
                // s = varid;
                s = trans;
            } else {
                // if (bv.isVarComponentParam(varid)){
                //     s = bv.nameOfVarComponentParam(varid);
                // } else if (bv.isVarConnectorParam(varid)) {
                //     s = bv.nameOfVarConnectorParam(varid);
                // } else if (varid.equals(BaseVisitor.keywordResult)) {
                //     s = bv.component_variable_result("ACTIONNOTKNOWN");
                // } else if (varid.toLowerCase().contains(BaseVisitor.keywordException)) {
                //     s = varid;
                // } else {
                //     s = varid;
                // }
                s = trans;
                if (ctx.varindex != null) {
                    s += "["
                        ///                     + visit_conditionalExpression(ctx.varindex.index).get(0)
                        + bv.visit(ctx.varindex).get(0) // why .index??? XXX
                        + "]";
                } else {
                    if(!(varid.equals(BaseVisitor.keywordResult)
                         || varid.toLowerCase().contains(BaseVisitor.keywordException)))
                        s += "[0]";
                }
            }
            // s = "UNKNOWN" + ctx.varid.getText();
            // if (ctx.varindex != null)
            //  s += visit(ctx.varindex).get(0);

        } else if (ctx.inline_id != null)
            s = "UNKNOWN"
                + ctx.inline_id.getText()
                + bv.visit(ctx.inline_args).get(0);
        else if (ctx.result != null)
            s += bv.component_variable_result("ACTIONNOTKNOWN");
        else if (ctx.xcd_exception != null)
            s = bv.component_variable_exception("ACTIONNOTKNOWN");
        else
            bv.myassert(false, "Unknown case of nullaryExpression");

        // System.out.println("Translation is: " + s);
        res.add(s);
        return res;
    }

    String translate_ID(BaseVisitor<T> bv, String id) {
        bv.myassert(id!=null && !id.equals("")
                    , "empty name for a variable");
        IdInfo idinfo = bv.getIdInfo(id);
        bv.myassert(idinfo.translation!=null && idinfo.translation.size()>0,
                    "Missing translation for " + id);
        return idinfo.translation.get(0);
    }
}
