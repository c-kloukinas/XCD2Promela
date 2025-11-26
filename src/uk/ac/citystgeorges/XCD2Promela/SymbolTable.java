package uk.ac.citystgeorges.XCD2Promela;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;

abstract class SymbolTable {
    String compilationUnitID; // enclosing context
    ParserRuleContext myself; // the context that created this
                              // environment's symbol table
    XCD_type type; // which is of type (root,composite*,comp*,port*,method*,function)
    Map<String,IdInfo> map;
    Set<String> allExceptions = new TreeSet<String>();
    SymbolTable parent = null;
    List<SymbolTable> children;
    SymbolTable() {
        this("@root", null, XCD_type.roott, false, null); // call next one
    }
    protected SymbolTable(String compUnitID
                          , ParserRuleContext me
                          , XCD_type tp
                          , boolean is_paramp
                          , SymbolTable myparent) {
        compilationUnitID = compUnitID;
        myself = me;
        type = tp;
        parent = myparent;
        children = new ArrayList<SymbolTable>();
        map = new HashMap<String,IdInfo>();
        map.put(compilationUnitID,
                new IdInfo(tp
                           , is_paramp
                           // not an array, no array size
                           , (ArraySizeContext)null
                           // no initial value
                           , (VariableDefaultValueContext)null
                           , ""     // parent (root)
                           ));
    }
    SymbolTable you() {// System.err.println("I'm a SymbolTable");
        return this;}

    abstract String getParamName(String param);

    synchronized SymbolTableRoot makeSymbolTableRoot()
    {
        synchronized (BaseVisitor.class) {
            SymbolTableRoot res = BaseVisitor.rootContext;
            if (res==null)
                res = new SymbolTableRoot();
            return res;
        }
    }

    SymbolTableComposite makeSymbolTableComposite(String compUnitID
                                                  , ParserRuleContext me
                                                  , XCD_type tp
                                                  , boolean is_paramp)
    { Utils.myAssert(tp == XCD_type.compositet
                       || tp == XCD_type.connectort
                       , "makeSymbolTableComposite called with type "
                       + tp);
        SymbolTableComposite res = new SymbolTableComposite(compUnitID
                                                  , me
                                                  , tp
                                                  , is_paramp
                                                  , this);
        children.add(res);
        return res; }
    SymbolTableComponent makeSymbolTableComponent(String compUnitID
                                                  , ParserRuleContext me
                                                  , XCD_type tp
                                                  , boolean is_paramp)
    {   Utils.myAssert(tp == XCD_type.componentt
                       || tp == XCD_type.rolet
                       , "makeSymbolTableComponent called with type "
                       + tp);
        SymbolTableComponent res = new SymbolTableComponent(compUnitID
                                                            , me
                                                            , tp
                                                            , is_paramp
                                                            , this);
        children.add(res);
        return res; }
    SymbolTablePort makeSymbolTablePort(String portname
                                        , ParserRuleContext me
                                        , XCD_type tp
                                        , boolean is_paramp)
    {   Utils.myAssert(tp == XCD_type.emittert
                       || tp == XCD_type.consumert
                       || tp == XCD_type.requiredt
                       || tp == XCD_type.providedt
                       || tp == XCD_type.emittervart
                       || tp == XCD_type.consumervart
                       || tp == XCD_type.requiredvart
                       || tp == XCD_type.providedvart
                       , "makeSymbolTablePort called with type "
                       + tp);
        SymbolTablePort res = new SymbolTablePort(portname
                                                  , me
                                                  , tp
                                                  , is_paramp
                                                  , this);
        children.add(res);
        return res; }
    SymbolTableMethod makeSymbolTableMethod(String compUnitID
                                            , XCD_type tp
                                            , ParserRuleContext me)
    {   Utils.myAssert(tp == XCD_type.methodt
                       || tp == XCD_type.eventt
                       , "makeSymbolTableComponent called with type "
                       + tp);
        SymbolTableMethod res = new SymbolTableMethod(compUnitID
                                                      , me
                                                      , tp
                                                      , this);
        children.add(res);
        return res; }
    SymbolTableFunction makeSymbolTableFunction(String compUnitID
                                                , ParserRuleContext me)
    {   SymbolTableFunction res = new SymbolTableFunction(compUnitID
                                                          , me
                                                          , XCD_type.functiont
                                                          , this);
        children.add(res);
        return res; }

}

class CommonConstructs {
    LstStr enums = new LstStr();
    LstStr typedefs = new LstStr();
}

class SymbolTableRoot extends SymbolTable {
    CommonConstructs commonConstructs = new CommonConstructs();
    SymbolTableRoot() {
        super();
    }
    SymbolTableRoot(String compUnitID, ParserRuleContext me, XCD_type tp, boolean is_paramp, SymbolTable myparent) {
        super(compUnitID, me, tp, is_paramp, myparent);
    }

    @Override
    String getParamName(String param) {
        Utils.myAssert(false, "SymbolTableRoot's getParamName method called");
        return "";
    }
}

class CompositeConstructs extends CommonConstructs {
    LstStr params = new LstStr();
    LstStr vars = new LstStr();
    LstStr inlineFunctionDecls = new LstStr();
    LstStr translatedAssertions = new LstStr();
    CompositeConstructs() { super(); }
}
class SymbolTableComposite extends SymbolTable { // COMPOSITE or CONNECTOR
    LstStr subcomponents = new LstStr();
    LstStr subconnectors = new LstStr();
    CompositeConstructs compConstructs = new CompositeConstructs();
    Map<String, LstStr> roles2portvarsInParams = new HashMap<String, LstStr>();

    @Override
    SymbolTableComposite you() {// System.err.println("I'm a SymbolTableComposite!");
        return this;}
    SymbolTableComposite(String compUnitID
                         , ParserRuleContext me
                         , XCD_type tp
                         , boolean is_paramp
                         , SymbolTable myparent) {
        super(compUnitID, me, tp, is_paramp, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
}
class ComponentConstructs extends CompositeConstructs {
    LstStr providedprts = new LstStr();
    LstStr consumerprts = new LstStr();
    LstStr requiredprts = new LstStr();
    LstStr emitterprts = new LstStr();
    ComponentConstructs() { super(); }
}
class SymbolTableComponent extends SymbolTable { // COMPONENT or ROLE
    ComponentConstructs compConstructs = new ComponentConstructs();
    @Override
    SymbolTableComponent you() {// System.err.println("I'm a SymbolTableComponent!");
        return this;}
    SymbolTableComponent(String compUnitID
                         , ParserRuleContext me
                         , XCD_type tp
                         , boolean is_paramp
                         , SymbolTable myparent) {
        super(compUnitID, me, tp, is_paramp, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
    LstStr getPortList(int tp) {
        return (tp==XCDParser.TK_EMITTER
                ? compConstructs.emitterprts
                : (tp==XCDParser.TK_CONSUMER
                   ? compConstructs.consumerprts
                   : (tp==XCDParser.TK_PROVIDED
                      ? compConstructs.providedprts
                      : (tp==XCDParser.TK_REQUIRED
                         ? compConstructs.requiredprts
                         : null))));
    }
}

class Name {public String name; Name(String n) {name=n;}@Override public String toString(){return name;}}
class Type {public String type; Type(String t) {type=t;}@Override public String toString(){return type;}}
class Sig extends ArrayList<Type> { Sig(){super();} }
class TypeNamePair {
    TypeNamePair(Type t, Name n) {name=n; type=t;}
    public Type type;
    public Name name;
    @Override public String toString(){return type.toString() + "," + name.toString();}
}
class SeqOfTypeNamePairs extends ArrayList<TypeNamePair> {
    SeqOfTypeNamePairs() {super();}
    SeqOfTypeNamePairs(int sz) {super(sz);}
    void addPair(Type t, Name n) {add(new TypeNamePair(t, n));}
}
class EventStructure {
    public Name name = null;
    public Sig param_types = null;
    // sig is <name, param_types>
    public SeqOfTypeNamePairs full_sig = null;
    public LstStr interaction_constraintsReq = null;
    public LstStr interaction_constraintsRes = null;
    public LstStr functional_constraintsReq = null;
    public LstStr functional_constraintsRes = null;
    public LstStr exceptions = new LstStr();
    EventStructure(Name n, Sig s, SeqOfTypeNamePairs fs, LstStr icQ, LstStr icS, LstStr fcQ, LstStr fcS, LstStr excs) {
        name = n; param_types = s; full_sig = fs;
        interaction_constraintsReq = icQ;
        interaction_constraintsRes = icS;
        functional_constraintsReq = fcQ;
        functional_constraintsRes = fcS;
        exceptions = excs;
    }
}
class MethodStructure extends EventStructure {
    public Type resultType = null;
    MethodStructure(Name n, Sig s, SeqOfTypeNamePairs fs
                    , Type res
                    , LstStr icQ, LstStr icS
                    , LstStr fcQ, LstStr fcS
                    , LstStr excs) {
        super(n, s, fs, icQ, icS, fcQ, fcS, excs);
        resultType = res;
    }
}
class PortConstructs extends CommonConstructs {
    /*
      One may wish to overload events(/methods).

      So we need to use the full event(/method) signature, i.e.,
      <name, param type...>. The parameter names are not part of the
      signature. Neither are the return type (methods) or any
      exceptions the method can throw (events don't return anything by
      definition, so cannot throw exceptions either), since exceptions
      are just a kind of (abnormal) return value.

      To represent these, we need:
      1) Name=String
      2) Type=String
      3) Sig=Pair<Name,Tuple<Type...>> // event/method name, param types...
      4) EventOverloads=Map<Name,Map<Sig, FullSig>> // it's an error if a sig
      // is defined more than once

      Also:
      5) FullSig=Tuple<Name // event/method name
      , Tuple<<Pair<Type, Name>... // param types & names
      , Type // result type
      , Tuple<Type...>> // exception types
    */
    LstStr basicEventNames = new LstStr(); // potentially overloaded
    LstStr basicMethodNames = new LstStr();
    // Name 2 Map of <Sig.toString() 2 EventStructure>
    Map<Name, Map<String, EventStructure>> eventOverloads
        = new HashMap<Name, Map<String, EventStructure>>();
    // Name 2 Map of <Sig.toString() 2 MethodStructure>
    Map<Name, Map<String, MethodStructure>> methodOverloads
        = new HashMap<Name, Map<String, MethodStructure>>();
    // Map<String, EventStructure> allEventInfo = new HashMap<String, EventStructure>();
    // Map<String, String> interaction_constraints
    //     = new HashMap<String, String>(); // event/method *sig* to IC
    // Map<String, String> functional_constraints
    //     = new HashMap<String, String>(); // event/method *sig* to FC
    // Map<String, LstStr> params
    //     = new HashMap<String, LstStr>(); // event/method *sig* to params

    PortConstructs() {super();}
}

class SymbolTablePort extends SymbolTable { // PORT or PORT VARIABLE
    PortConstructs portConstructs = new PortConstructs();
    SymbolTablePort(String portname
                    , ParserRuleContext me
                    , XCD_type portType
                    , boolean is_paramp
                    , SymbolTable parent) {
        super(portname, me, portType, is_paramp, parent);
    }

    String getParamName(String param) {
        return Names.paramNamePort(parent.compilationUnitID // port's component
                                   // port name is ignored?!?
                                   , param);
    }
}

// class SymbolTableConn extends SymbolTable {
//     LstStr params = new LstStr();
//     LstStr vars = new LstStr();
//     CommonConstructs connConstructs = new CommonConstructs();
//     LstStr roles = new LstStr();
//     // LstStr rolesInParameters;
//     LstStr subconnectors = new LstStr();
//     @Override
//     SymbolTableConn you() {// System.err.println("I'm a SymbolTableComp!");
//         return this;}
//     SymbolTableConn(String compUnitID
//                     , ParserRuleContext me
//                     , XCD_type tp
//                     , boolean is_paramp
//                     , SymbolTable myparent) {
//         super(compUnitID, me, tp, is_paramp, myparent);
//         EnvironmentCreationVisitor
//             .myAssert(compUnitID!=null, "compUnitID is null");
//         roles2portvarsInParams = new HashMap<String,LstStr>();
//     }

//     String getParamName(String param) {
//         return Names.paramNameComponent(compilationUnitID, param);
//     }
// }

// class SymbolTableConnRole extends SymbolTableConn {
//     ComponentConstructs compConstructs = new ComponentConstructs();
//     @Override
//     SymbolTableConnRole you() {// System.err.println("I'm a SymbolTableComp!");
//         return this;}
//     SymbolTableConnRole(String compUnitID
//                         , ParserRuleContext me
//                         , XCD_type tp
//                         , boolean is_paramp
//                         , SymbolTable myparent) {
//         super(compUnitID, me, tp, is_paramp, myparent);
//         EnvironmentCreationVisitor
//             .myAssert(compUnitID!=null, "compUnitID is null");
//     }

//     String getParamName(String param) {
//         return Names.paramNameComponent(compilationUnitID, param);
//     }
// }
// class RoleConstructs extends PortConstructs {
//     RoleConstructs() {super();}
// }
// class SymbolTableConnRolePort extends SymbolTableConnRole {
//     RoleConstructs roleConstructs = new RoleConstructs();

//     SymbolTableConnRolePort(String portname
//                             , ParserRuleContext me
//                             , XCD_type portType
//                             , boolean is_paramp
//                             , SymbolTable parent) {
//         super(portname, me, portType, is_paramp, parent);
//     }

//     String getParamName(String param) {
//         return Names.paramNamePort(parent.compilationUnitID // port's component
//                                    // port name is ignored?!?
//                                    , param);
//     }
// }


class EventConstructs {
    LstStr params = new LstStr();
    LstStr exceptions = new LstStr();
}

// class SymbolTableEvent extends SymbolTable {
//     EventConstructs eventConstructs = new EventConstructs();

//     @Override
//     SymbolTableEvent you() {// System.err.println("I'm a SymbolTableComp!");
//         return this;}
//     SymbolTableEvent(String compUnitID
//                      , ParserRuleContext me
//                      , XCD_type tp
//                      , SymbolTable myparent) {
//         super(compUnitID, me, tp, false, myparent);
//         EnvironmentCreationVisitor
//             .myAssert(compUnitID!=null, "compUnitID is null");
//     }

//     String getParamName(String param) {
//         return Names.paramNameComponent(compilationUnitID, param);
//     }
// }

class MethodConstructs extends EventConstructs {
    String result = new String();
    MethodConstructs() {super();}
}
class SymbolTableMethod extends SymbolTable { // METHOD or EVENT
    MethodConstructs methodConstructs = new MethodConstructs();

    @Override
    SymbolTableMethod you() {// System.err.println("I'm a SymbolTableMethod!");
        return this;}
    SymbolTableMethod(String compUnitID
                      , ParserRuleContext me
                      , XCD_type tp
                      , SymbolTable myparent) {
        super(compUnitID, me, tp, false, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
}

class SymbolTableFunction extends SymbolTableMethod {
    @Override
    SymbolTableFunction you() {// System.err.println("I'm a SymbolTableFunction!");
        return this;}
    SymbolTableFunction(String compUnitID
                        , ParserRuleContext me
                        , XCD_type tp
                        , SymbolTable myparent) {
        super(compUnitID, me, tp, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
}
