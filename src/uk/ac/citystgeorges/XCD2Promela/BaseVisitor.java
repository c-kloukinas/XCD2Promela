package uk.ac.citystgeorges.XCD2Promela;
// import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
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
 abstract class BaseVisitor<T>
 /* Use the following to ensure that you haven't forgotten any methods */
 //    extends AbstractParseTreeVisitor<T> implements XCDVisitor<T>
     extends XCDBaseVisitor<T>
{

    static public String keywordResult = "\\result";
    static public String keywordException = "\\exception";
    static public String keywordNothing = "\\nothing";

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

    private ArrayList<ContextInfo> env = new ArrayList<ContextInfo>();
    ArrayList<ContextInfo> getEnv() { return env; }
    public ContextInfo frameNow() {
        int sz = env.size();
        myassert(sz > 0
                 , "frameNow called with empty env stack - env.size()=" + sz);
        return env.get(env.size()-1);
    }
    protected void popLastContext(ContextInfo framenow) {
        int sz = getEnv().size();
        myassert(sz > 0
                 , "Popped too many contexts - env.size()=" + sz);
        ContextInfo lastctx = getEnv().get(sz-1);
        myassert(framenow == lastctx
                 , "Current context is not the last element!");
        getEnv().remove(sz-1);
    }

    final static protected ContextInfo rootContext = new ContextInfo();

    protected IdInfo addIdInfo(String symbol
                               , XCD_type tp, String varTypeName
                               , boolean is_paramp
                               , ArraySizeContext arraySize
                               , Variable_initialValueContext initVal
                               , String parentId) {
        IdInfo res
            = addIdInfo(symbol, tp, is_paramp, arraySize
                        , initVal, parentId);
        res.variableTypeName = varTypeName;
        return res; }
    protected IdInfo addIdInfo(String symbol
                               , XCD_type tp, boolean is_paramp
                               , ArraySizeContext arraySize
                               , Variable_initialValueContext initVal
                               , String parentId) {
        var newInfo = new IdInfo(tp, is_paramp
                                 , arraySize
                                 , initVal
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
                && (info.parent.equals(newInfo.parent));
            if (!matches) {
                mywarning("Symbol \"" +symbol+"\" already in the map"
                          + "\n" + info.type + " vs " + newInfo.type
                          // + "\n" + info.sType + " vs " + newInfo.sType
                          + "\n" + info.is_param + " vs " + newInfo.is_param
                          + "\n" + info.arraySz + " vs " + newInfo.arraySz
                          + "\n" + info.initVal + " vs " + newInfo.initVal
                          + "\n" + info.parent + " vs " + newInfo.parent);
                myassert(false, "Symbol \""+symbol+"\" already in the map");
            } else
                mywarning("Symbol \"" +symbol+"\" aldeary in the map - input visited twice!");

        } else {
            currentMap.put(symbol, newInfo); }
        return newInfo;
    }

    public IdInfo getIdInfo(String id) { return getIdInfo(frameNow(), id); }
    public IdInfo getIdInfo(ContextInfo currEnv, String id) {
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


    boolean isComposite(ContextInfoComp info)
    { return info.subcomponents.size()!=0; }
    boolean hasProvidedPorts(ContextInfoComp info)
    { return info.providedprts.size()!=0; }
    boolean hasRequiredPorts(ContextInfoComp info)
    { return info.requiredprts.size()!=0; }
    boolean hasEmitterPorts(ContextInfoComp info)
    { return info.emitterprts.size()!=0; }
    boolean hasConsumerPorts(ContextInfoComp info)
    { return info.consumerprts.size()!=0; }
    boolean hasPorts(ContextInfoComp info)
    { return hasProvidedPorts(info) || hasRequiredPorts(info)
            || hasEmitterPorts(info) || hasConsumerPorts(info); }

    String component_typeof_id(String var) { return Names.typeOfVar(var);
        // "TypeOf("+ var + ")";
    }
    String component_variable_id(String var, String index)
    { return var + "[" + index + "]"; }

    String component_variable_id(String var, ArraySizeContext index)
    { return var + "[" + index + "]"; }

    boolean is_enumConstant(String id) {
        var idInfo = getIdInfo(id);
        return (idInfo.type == XCD_type.enumt);
    }
    public String component_variable_result(String actionid){
        var framenow = frameNow();
        var compTypeid = framenow.compilationUnitID;
        var portid = "UNKNOWN_PORTID"; // framenow.portID;
        return Names.portActionNameRes(compTypeid, portid, actionid);
    }
    public String component_variable_exception(String actionid) {
        var framenow = frameNow();
        var compTypeid = framenow.compilationUnitID;
        var portid = "UNKNOWN_PORTID"; // framenow.portID;
        return Names.portActionNameExc(compTypeid, portid, actionid);
    }

    public boolean isVarComponentParam(String id) {
        var idInfo = getIdInfo(id);
        return (idInfo.type == XCD_type.paramt);
    }
    public String nameOfVarComponentParam(String id) {
        var idInfo = getIdInfo(id);
        return Names.varNameComponent(idInfo.parent, id);
    }
    public boolean isVarConnectorParam(String id) {
        var idInfo = getIdInfo(id);
        return (idInfo.type == XCD_type.connectorpart);
    }
    public String nameOfVarConnectorParam(String id) { // supposedly also considers var_prefix
        var framenow = frameNow();
        var idInfo = framenow.map.get(id);
        var compTypeid = framenow.compilationUnitID;
        var portid = "UNKNOWN_PORTID"; // framenow.portID;
        String s = Names.xVarName("UNKNOWN_conn", "UNKNOWN_role", id);

        // var String output = "";
        // var String connIns_i = "";
        // for(element: root.elements){
        //     if(element.connector != null ){
        //      if(element.connector.params.par_pre.prim_param != null &&
        //         element.connector.params.par_pre.prim_param.id == variable
        //         ){
        //          connIns_i =  var_prefix.substring(var_prefix.indexOf(element.connector.id) + element.connector.id.length() + 1 ,var_prefix.indexOf("_ROLE"));
        //          output = "Connector_i_Param_N(CompositeName , CompositeID," + element.connector.id + "," + connIns_i + "," + variable +")";
        //      }
        //      else{
        //          for(par: element.connector.params.pars){
        //                      if(par.prim_param!= null && par.prim_param.id == variable){
        //                  connIns_i =  var_prefix.substring(var_prefix.indexOf(element.connector.id) + element.connector.id.length() + 1 ,var_prefix.indexOf("_ROLE"));
        //                     output = "Connector_i_Param_N(CompositeName , CompositeID," + element.connector.id + "," + connIns_i + "," + variable +")";
        //                      }
        //          }
        //      }
        //     }
        // }

        return s;
    }

}
