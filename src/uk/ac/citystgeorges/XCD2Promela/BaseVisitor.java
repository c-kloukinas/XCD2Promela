package uk.ac.citystgeorges.XCD2Promela;
// import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;

// Void is final unfortunately :-(
// class MyReturnType extends Void { public Void() {super();} }

/**
 * A visitor with no visit methods, to define fields and methods used by other
 * classes, and to ensure that whoever extends it does not copy behaviour from
 * some other visitor.
 */
public class BaseVisitor extends XCDBaseVisitor<Void> {

    /**
     * Miscellaneous helper functions
     */
    static protected void updateln(Tree ctx) {Utils.updateln(ctx); }
    static protected String newgensym() { return Utils.newgensym(null); }
    static protected String newgensym(String pref) {return Utils.newgensym(pref);}
    static protected void myAssert(boolean cond, String msg) {
        Utils.myAssert(cond, msg); }
    protected void myassert(boolean cond, String msg) {
        Utils.util.myassert(cond,msg); }
    static protected void myWarning(String msg) { Utils.myWarning(msg); }
    protected void mywarning(String msg) { Utils.util.mywarning(msg); }

    protected ArrayList<ContextInfo> env = new ArrayList<ContextInfo>();
    final static protected ContextInfo rootContext = new ContextInfo();

    protected IdInfo addIdInfo(String symbol
                               , XCD_type tp, String varTypeName
                               , boolean is_paramp
                               , ArraySizeContext arraySize
                               , Variable_initialValueContext initVal
                               , String big_name, String var_prefix
                               , String parentId) {
        IdInfo res
            = addIdInfo(symbol, tp, is_paramp, arraySize
                        , initVal, big_name, var_prefix, parentId);
        res.variableTypeName = varTypeName;
        return res; }
    protected IdInfo addIdInfo(String symbol
                               , XCD_type tp, boolean is_paramp
                               , ArraySizeContext arraySize
                               , Variable_initialValueContext initVal
                               , String big_name, String var_prefix
                               , String parentId) {
        var newInfo = new IdInfo(tp, is_paramp
                                 , arraySize
                                 , initVal
                                 , big_name, var_prefix
                                 , parentId);
        var currentMap = env.get(env.size()-1).map;
        if (currentMap.containsKey(symbol)) {
            IdInfo info = currentMap.get(symbol);
            boolean matches = (info.type == newInfo.type)
                // && (info.sType.equals(newInfo.sType))
                && (info.is_param == newInfo.is_param)
                && ( (info.arraySz==null && newInfo.arraySz==null)
                     || (info.arraySz!=null && newInfo.arraySz!=null
                         && (info.arraySz.equals(newInfo.arraySz))) )
                && ( (info.initVal==null && newInfo.initVal==null)
                     || (info.initVal!=null && newInfo.initVal!=null
                         && (info.initVal.equals(newInfo.initVal))) )
                && (info.big_name.equals(newInfo.big_name))
                && (info.parent.equals(newInfo.parent));
            if (!matches) {
                mywarning("Symbol \"" +symbol+"\" already in the map"
                          + "\n" + info.type + " vs " + newInfo.type
                          // + "\n" + info.sType + " vs " + newInfo.sType
                          + "\n" + info.is_param + " vs " + newInfo.is_param
                          + "\n" + info.arraySz + " vs " + newInfo.arraySz
                          + "\n" + info.initVal + " vs " + newInfo.initVal
                          + "\n" + info.big_name + " vs " + newInfo.big_name
                          + "\n" + info.parent + " vs " + newInfo.parent);
                myassert(false, "Symbol \""+symbol+"\" already in the map");
            } else
                mywarning("Symbol \"" +symbol+"\" aldeary in the map - input visited twice!");

        } else {
            currentMap.put(symbol, newInfo); }
        return newInfo;
    }
    protected IdInfo getIdInfo(ContextInfo currEnv, String id) {
        IdInfo res = null;
        while (res == null && currEnv!=null) {
            Map<String,IdInfo> the_map = currEnv.map;
            if (the_map.containsKey(id))
                res=the_map.get(id);
            currEnv=currEnv.parent;
        }
        myassert(res!=null, "getIdInfo: Symbol \"" + id + "\" not found");
        return res;
    }

}
