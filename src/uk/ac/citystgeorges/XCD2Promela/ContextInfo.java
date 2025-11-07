package uk.ac.citystgeorges.XCD2Promela;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import uk.ac.citystgeorges.XCD2Promela.XCDParser.*;
import org.antlr.v4.runtime.ParserRuleContext;

public class ContextInfo {
    // Context - inside what are we?
    //    String compTypeID; // supposed to have an id, a body, and a param field.
    // int portID;
    // String varPrefix;
    // Above are old, kept so old code compiles - remove them eventually XXX
    String compilationUnitID; // enclosing context
    ParserRuleContext myself; // the context that created the
                              // environment
    XCD_type type; // which is of type (root,comp,conn,conf,tdef,enum)
    LstStr typedefs;
    LstStr enums;
    Map<String,IdInfo> map;
    ContextInfo parent = null;
    List<ContextInfo> children;
    ContextInfo you() {// System.err.println("I'm a ContextInfo");
        return this;}
    ContextInfo() {
        this("@root", null, XCD_type.unknownt, false, null); // call next one
    }
    protected ContextInfo(String compUnitID, ParserRuleContext me, XCD_type tp, boolean is_paramp, ContextInfo myparent) {
        compilationUnitID = compUnitID;
        myself = me;
        type = tp;
        parent = myparent;
        typedefs = new LstStr();
        enums = new LstStr();
        children = new ArrayList<ContextInfo>();
        map = new HashMap<String,IdInfo>();
        map.put(compilationUnitID,
                new IdInfo(tp
                           , is_paramp
                           // not an array, no array size
                           , (ArraySizeContext)null
                           // no initial value
                           , (Variable_initialValueContext)null
                           , compilationUnitID // what's my big name?
                           , ""     // var_prefix
                           , ""     // parent (root)
                           ));
    }

    String getParamName(String param) {
        return null;
    }

    ContextInfoComp makeContextInfoComp(String compUnitID
                                        , ParserRuleContext me
                                        , boolean is_paramp)
    {   ContextInfoComp res = new ContextInfoComp(compUnitID
                                                  , me
                                                  , XCD_type.componentt
                                                  , is_paramp
                                                  , this);
        children.add(res);
        return res; }
    ContextInfoCompPort makeContextInfoCompPort(String portname
                                                , ParserRuleContext me
                                                , XCD_type portType
                                                , boolean is_paramp)
    {   ContextInfoCompPort res = new ContextInfoCompPort(portname
                                                          , me
                                                          , portType
                                                          , is_paramp
                                                          , this);
        children.add(res);
        return res; }
    ContextInfoConn makeContextInfoConn(String compUnitID
                                        , ParserRuleContext me
                                        , boolean is_paramp)
    {   ContextInfoConn res = new ContextInfoConn(compUnitID
                                                  , me
                                                  , XCD_type.connectort
                                                  , is_paramp
                                                  , this);
        children.add(res);
        return res; }
    ContextInfoConnRole makeContextInfoConnRole(String compUnitID
                                                , ParserRuleContext me
                                                , boolean is_paramp)
    {   ContextInfoConnRole res = new ContextInfoConnRole(compUnitID
                                                          , me
                                                          , XCD_type.rolet
                                                          , is_paramp
                                                          , this);
        children.add(res);
        return res; }
    ContextInfoConnRolePort makeContextInfoConnRolePort(String portname
                                                , ParserRuleContext me
                                                , XCD_type portType
                                                , boolean is_paramp)
    {   ContextInfoConnRolePort res = new ContextInfoConnRolePort(portname
                                                                  , me
                                                                  , portType
                                                                  , is_paramp
                                                                  , this);
        children.add(res);
        return res; }

    ContextInfoMethod makeContextInfoMethod(String compUnitID
                                            , ParserRuleContext me)
    {   ContextInfoMethod res = new ContextInfoMethod(compUnitID
                                                      , me
                                                      , XCD_type.methodt
                                                      , this);
        children.add(res);
        return res; }
    ContextInfoEvent makeContextInfoEvent(String compUnitID
                                          , ParserRuleContext me)
    {   ContextInfoEvent res = new ContextInfoEvent(compUnitID
                                                    , me
                                                    , XCD_type.methodt
                                                    , this);
        children.add(res);
        return res; }
    ContextInfoFunction makeContextInfoFunction(String compUnitID
                                                , ParserRuleContext me)
    {   ContextInfoFunction res = new ContextInfoFunction(compUnitID
                                                          , me
                                                          , XCD_type.methodt
                                                          , this);
        children.add(res);
        return res; }
}

class ContextInfoComp extends ContextInfo {
    LstStr paramsORvars;
    LstStr params;
    LstStr vars;
    LstStr subcomponents;
    LstStr subconnectors;
    LstStr providedprts; LstStr requiredprts;
    LstStr consumerprts; LstStr emitterprts;

    @Override
    ContextInfoComp you() {// System.err.println("I'm a ContextInfoComp!");
        return this;}
    ContextInfoComp(String compUnitID
                    , ParserRuleContext me
                    , XCD_type tp
                    , boolean is_paramp
                    , ContextInfo myparent) {
        super(compUnitID, me, tp, is_paramp, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");

        paramsORvars = new LstStr();
        params = new LstStr();
        vars = new LstStr();
        subcomponents = new LstStr();
        subconnectors = new LstStr();
        providedprts = new LstStr();
        requiredprts = new LstStr();
        consumerprts = new LstStr();
        emitterprts = new LstStr();
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
}
class Name {public String name; Name(String n) {name=n;}@Override public String toString(){return name;}}
class Type {public String type; Type(String t) {type=t;}@Override public String toString(){return type;}}
class Sig extends ArrayList<Type> { Sig(){super();} }
class NameTypePair {
    NameTypePair(Name n, Type t) {name=n; type=t;}
    public Name name;
    public Type type;
    @Override public String toString(){return name.toString() + "," + type.toString();}
}
class SeqOfNameTypePairs extends ArrayList<NameTypePair> {
    SeqOfNameTypePairs() {super();}
    SeqOfNameTypePairs(int sz) {super(sz);}
}
class EventStructure {
    public Name name;
    public Sig param_types;
    // sig is <name, param_types>
    public SeqOfNameTypePairs full_sig;
    public LstStr interaction_constraints;
    public LstStr functional_constraints;
}
class MethodStructure extends EventStructure {
    public Type resultType;
    LstStr exceptionTypes;
}
class ContextInfoCompPort extends ContextInfoComp {
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

    ContextInfoCompPort(String portname
                        , ParserRuleContext me
                        , XCD_type portType
                        , boolean is_paramp
                        , ContextInfo parent) {
        super(portname, me, portType, is_paramp, parent);
    }


    String getParamName(String param) {
        return Names.paramNamePort(parent.compilationUnitID // port's component
                                   // port name is ignored?!?
                                   , param);
    }
}

class ContextInfoConn extends ContextInfo {
    LstStr paramsORvars;
    LstStr params;
    LstStr vars;
    LstStr roles;

    @Override
    ContextInfoConn you() {// System.err.println("I'm a ContextInfoComp!");
        return this;}
    ContextInfoConn(String compUnitID
                    , ParserRuleContext me
                    , XCD_type tp
                    , boolean is_paramp
                    , ContextInfo myparent) {
        super(compUnitID, me, tp, is_paramp, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");

        paramsORvars = new LstStr();
        params = new LstStr();
        vars = new LstStr();
        roles = new LstStr();
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
}

class ContextInfoConnRole extends ContextInfo {
    LstStr paramsORvars;
    LstStr params;
    LstStr vars;
    LstStr enums;
    LstStr inlineFunctionDecls;
    LstStr providedprts; LstStr requiredprts;
    LstStr consumerprts; LstStr emitterprts;

    @Override
    ContextInfoConnRole you() {// System.err.println("I'm a ContextInfoComp!");
        return this;}
    ContextInfoConnRole(String compUnitID
                        , ParserRuleContext me
                        , XCD_type tp
                        , boolean is_paramp
                        , ContextInfo myparent) {
        super(compUnitID, me, tp, is_paramp, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");

        paramsORvars = new LstStr();
        params = new LstStr();
        vars = new LstStr();
        enums = new LstStr();
        inlineFunctionDecls = new LstStr();
        providedprts = new LstStr();
        requiredprts = new LstStr();
        consumerprts = new LstStr();
        emitterprts = new LstStr();
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
}
class ContextInfoConnRolePort extends ContextInfoConnRole {
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

    ContextInfoConnRolePort(String portname
                            , ParserRuleContext me
                            , XCD_type portType
                            , boolean is_paramp
                            , ContextInfo parent) {
        super(portname, me, portType, is_paramp, parent);
    }


    String getParamName(String param) {
        return Names.paramNamePort(parent.compilationUnitID // port's component
                                   // port name is ignored?!?
                                   , param);
    }
}




class ContextInfoEvent extends ContextInfo {
    LstStr params;
    LstStr exceptions;

    @Override
    ContextInfoEvent you() {// System.err.println("I'm a ContextInfoComp!");
        return this;}
    ContextInfoEvent(String compUnitID
                     , ParserRuleContext me
                     , XCD_type tp
                     , ContextInfo myparent) {
        super(compUnitID, me, tp, false, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");

        params = new LstStr();
        exceptions = new LstStr();
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
}

class ContextInfoMethod extends ContextInfoEvent {
    String result;

    @Override
    ContextInfoMethod you() {// System.err.println("I'm a ContextInfoComp!");
        return this;}
    ContextInfoMethod(String compUnitID
                      , ParserRuleContext me
                      , XCD_type tp
                      , ContextInfo myparent) {
        super(compUnitID, me, tp, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");

        result = new String();
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
}

class ContextInfoFunction extends ContextInfoMethod {
    @Override
    ContextInfoFunction you() {// System.err.println("I'm a ContextInfoComp!");
        return this;}
    ContextInfoFunction(String compUnitID
                        , ParserRuleContext me
                        , XCD_type tp
                        , ContextInfo myparent) {
        super(compUnitID, me, tp, myparent);
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");
    }

    String getParamName(String param) {
        return Names.paramNameComponent(compilationUnitID, param);
    }
}
