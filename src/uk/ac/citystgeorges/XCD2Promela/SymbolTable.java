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
                       || tp == XCD_type.configt
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
    LstStr inlineFunctions = new LstStr();
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
        return (type==XCD_type.connectort)
            ? Names.paramNameConnector(compilationUnitID, param)
            : Names.paramNameComponent(compilationUnitID, param); // composite
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
        return (type==XCD_type.rolet)
            ? Names.paramNameRole(parent.compilationUnitID, compilationUnitID, param)
            : Names.paramNameComponent(compilationUnitID, param); // component
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

final class Name {final private String value;
    Name(String name) {value=name;}
    @Override public String toString(){return value;}
    @Override public boolean equals(Object o)
    {return o!=null
            && (o instanceof Name)
            && value.equals( ((Name) o).value);}
    @Override public int hashCode() {return value.hashCode();}
}
final class Type {final private String value;
    Type(String type) {value=type;}
    @Override public String toString(){return value;}
    @Override public boolean equals(Object o)
    {return o!=null
            && (o instanceof Type)
            && value.equals(((Type) o).value);}
    @Override public int hashCode() {return value.hashCode();}
}

class Sig extends ArrayList<Type> {Sig(){super();}}
class TypeNamePair {
    TypeNamePair(Type t, Name n) {name=n; type=t;}
    public Type type;
    public Name name;
    @Override public String toString()
    {return type.toString() + "," + name.toString();}
    @Override public boolean equals(Object o)
    {return o!=null
            && (o instanceof TypeNamePair)
            && type.equals(o)
            && name.equals(o);}
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
    public LstStr exceptions = new LstStr();
    public LstStr x_constraintsAccepts = null;
    public LstStr x_constraintsWaits = null;
    public LstStr x_constraintsAllows = null;
    public LstStr x_constraintsEnsures = null;
    public LstStr f_constraintsWhen = null;
    public LstStr f_constraintsWEnsures = null;
    public LstStr f_constraintsRequires = null;
    public LstStr f_constraintsREnsures = null;
    EventStructure(Name n, Sig s, SeqOfTypeNamePairs fs, LstStr excs) {
        name = n; param_types = s; full_sig = fs;
        exceptions = excs;
    }
    @Override
    public String toString() {
        return "\n\tName: " + name
            + "\n\tSig: " + param_types
            + "\n\tFull Sig: " + full_sig
            + "\n\tExceptions: " + exceptions;
    }
}
class MethodStructure extends EventStructure {
    public Type resultType = null;
    MethodStructure(Name n, Sig s, SeqOfTypeNamePairs fs, LstStr excs
                    , Type res) {
        super(n, s, fs, excs);
        resultType = res;
    }
    @Override
    public String toString() {
        return super.toString()
            + "\n\tResult: " + resultType;
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
    // Name 2 Map of <Sig 2 EventStructure>
    Map<Name, Map<Sig, EventStructure>> eventOverloads
        = new HashMap<Name, Map<Sig, EventStructure>>();
    // Name 2 Map of <Sig 2 MethodStructure>
    Map<Name, Map<Sig, MethodStructure>> methodOverloads
        = new HashMap<Name, Map<Sig, MethodStructure>>();
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


class EventConstructs {
    LstStr params = new LstStr();
    LstStr exceptions = new LstStr();
}

class MethodConstructs extends EventConstructs {
    String result = new String();
    MethodConstructs() {super();}
}
class SymbolTableMethod extends SymbolTable { // METHOD or EVENT
    MethodConstructs methodConstructs = new MethodConstructs();
    EventStructure methodStructure = null;
    String comp;                // or role
    String port;                // or portvar
    // String action; == compilationUnitID
    @Override
    SymbolTableMethod you() {// System.err.println("I'm a SymbolTableMethod!");
        return this;}
    SymbolTableMethod(String compUnitID
                      , ParserRuleContext me
                      , XCD_type tp
                      , SymbolTable myparent) {
        super(compUnitID, me, tp, false, myparent);
        port = parent.compilationUnitID;
        comp = parent.parent.compilationUnitID;
        EnvironmentCreationVisitor
            .myAssert(compUnitID!=null, "compUnitID is null");
    }

    String getParamName(String param) {
        // The same parameter name can be used by different actions
        // with different types and at a different position in the
        // parameters list, so we need to be able to distinguish the
        // parameter of one action from that of another.
        return Names.paramNameAction(comp, port, compilationUnitID, param);
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
        return Names.paramName(compilationUnitID, param);
    }
}
