grammar XCD;

compilationUnits: (elements+=compilationUnit)+;

compilationUnit:
          glob_enum=enumDeclaration
        | glob_typedef=typeDefDeclaration
        | component=componentDeclaration
        | connector=connectorDeclaration
        | config=elementVariableDeclaration TK_SEMICOLON
        ;

connectorDeclaration:
        TK_CONNECTOR id=ID params=connectorParameterList
        TK_LBRACE
                body=connectorBody
        TK_RBRACE
    ;

connectorBody:
        (elements+=connectorBody_Element)+
        ;

connectorBody_Element:
         role=roleDeclaration
     | cenum=enumDeclaration
             /*
              * Connectors CANNOT have (normal) variables! That would
              * be distributed shared state, which XCD's goal is to
              * make impossible (so as to guarantee realisability).
              *
              * Instead what these are are `configuration' variables,
              * that define how role port variables are connected
              * together (through which sub-connectors).
              *
              * So, we're talking about the 2nd case of
              * elementVariableDeclaration.
              */
     | variable=variableDeclaration
    ;

roleDeclaration:
        TK_ROLE id=ID (size=arraySize)?
        (params=formalParameters)?
        TK_LBRACE
        body=roleBody
        TK_RBRACE
    ;

roleBody:
        (elements+=roleBody_Element)+
        ;

roleBody_Element:
          assert=assertDeclaration
     |variable=variableDeclaration
     |renum=enumDeclaration
     |inlineFunction=inlineFunctionDeclaration
     |pv=rolePortvar
    ;
rolePortvar:
        emitter=emitterPortvar
   |consumer=consumerPortvar
   |required=requiredPortvar
   |provided=providedPortvar
;

emitterPortvar:
        type=TK_EMITTER
        TK_PORTVAR id=ID (size=arraySize)?
    TK_LBRACE
        (events+=emitterPortvar_event)+
        TK_RBRACE
;
consumerPortvar:
    type=TK_CONSUMER
    TK_PORTVAR id=ID (size=arraySize)?
    TK_LBRACE
        (events+=emitterPortvar_event)+
        TK_RBRACE
;
requiredPortvar:
    type=TK_REQUIRED
    TK_PORTVAR id=ID (size=arraySize)?
    TK_LBRACE
        (methods+=requiredPortvar_method)+
        TK_RBRACE
;
providedPortvar:
    type=TK_PROVIDED
    TK_PORTVAR id=ID (size=arraySize)?
    TK_LBRACE
        ( methods+=providedPortvar_method )+
    TK_RBRACE
;

emitterPortvar_event:
        (icontract= emitterPv_InteractionContract)?
        event_sig=eventSignatureSemicolon
;
requiredPortvar_method:
        (icontract= emitterPv_InteractionContract)?
        method_sig=methodSignatureSemicolon
;
providedPortvar_method:
        (icontract= providedPv_InteractionContract)?
         method_sig=methodSignatureSemicolon
;

emitterPv_InteractionContract:
        TK_INTERACTION
        TK_LBRACE
          (constraints+=emitterPv_InteractionConstraint)+
        TK_RBRACE
;
providedPv_InteractionContract:
        TK_INTERACTION
        TK_LBRACE
          (constraints+=emitterPv_InteractionConstraint)+
        TK_RBRACE
;
emitterPv_InteractionConstraint:
        TK_WAITS TK_COLON promise=conditionalStatement
        TK_ENSURES TK_COLON ensure=postStatement
;
providedPv_InteractionConstraint:
    (TK_WAITS TK_COLON promises+=conditionalStatement
     TK_ENSURES TK_COLON ensures+=postStatement )+
;

componentDeclaration:
        TK_COMPONENT id=ID (param=formalParameters)?
        TK_LBRACE
        body = componentBody
        TK_RBRACE
    ;

componentBody:
        (elements+=componentBody_Element)+
        ;

componentBody_Element:
   variable=variableDeclaration
  |assert=assertDeclaration
  |cenum=enumDeclaration
  |inlineFunction=inlineFunctionDeclaration
  |port=componentPort
  ;

componentPort:
        emitter=emitterPort
        |consumer=consumerPort
        |required=requiredPort
        |provided=providedPort
;

emitterPort:
        type=TK_EMITTER
        TK_PORT id=ID (size=arraySize)?
    TK_LBRACE
        (events+=emitterPort_event)+
        TK_RBRACE
;
consumerPort:
    type=TK_CONSUMER
    TK_PORT id=ID (size=arraySize)?
    TK_LBRACE
        (events+=consumerPort_event)+
        TK_RBRACE
;
requiredPort:
        (TK_LBRACKET concurrency=TK_CONCURRENT TK_RBRACKET )?
    type=TK_REQUIRED
    TK_PORT id=ID (size=arraySize)?
    TK_LBRACE
        (methods+=requiredPort_method)+
        TK_RBRACE
;
providedPort:
    type=TK_PROVIDED
    TK_PORT id=ID (size=arraySize)?
    TK_LBRACE
        ( methods+=providedPort_method )+
        TK_RBRACE
;

emitterPort_event:
        (icontract= emitterRequired_InteractionContract)?
        (fcontract=emitterPort_functionalContract)?
        port_event=eventSignatureSemicolon
;
consumerPort_event:
        (icontract= consumerProvided_InteractionContract)?
        (fcontract=consumerPort_functionalContract)?
        port_event=eventSignatureSemicolon
;
requiredPort_method:
        (icontract= emitterRequired_InteractionContract)?
        (fcontract=requiredPort_functionalContract)?
        port_method=methodSignatureSemicolon
;
providedPort_method:
        (icontract= consumerProvided_InteractionContract)?
    (fcontract=providedPort_functionalContract)?
        port_method=methodSignatureSemicolon
;

complex_providedPort_method:
        (icontract_req= complex_provided_InteractionContract_Req)?
    (fcontract_req= complex_providedPort_functionalContract_Req)?
        (icontract_res= complex_provided_InteractionContract_Res)?
    (fcontract_res= complex_providedPort_functionalContract_Res) ?
        port_method=methodSignatureSemicolon
;

complex_providedPort_functionalContract_Res:
        TK_FUNCTIONAL_RES
        TK_LBRACE
        TK_ENSURES TK_COLON ensure=postStatement
        TK_RBRACE
;

complex_provided_InteractionContract_Res:
        TK_INTERACTION_RES
        TK_LBRACE
        constraint_pre=emitterRequired_InteractionConstraint
            (constraints+=emitterRequired_InteractionConstraint)*
        TK_RBRACE
;

complex_providedPort_functionalContract_Req:
        TK_FUNCTIONAL_REQ
        TK_LBRACE
        constraint_pre=consumerPort_functionalConstraint
            (constraints+=consumerPort_functionalConstraint)*
        TK_RBRACE
;

complex_provided_InteractionContract_Req:
        TK_INTERACTION_REQ
        TK_LBRACE
        constraint_pre=consumerProvided_InteractionConstraint
            (constraints+=consumerProvided_InteractionConstraint)*
        TK_RBRACE
;

emitterRequired_InteractionContract:
        TK_INTERACTION
        TK_LBRACE
        constraint_pre=emitterRequired_InteractionConstraint
            (constraints+=emitterRequired_InteractionConstraint)*
        TK_RBRACE
;
consumerProvided_InteractionContract:
        TK_INTERACTION
        TK_LBRACE
        constraint_pre=consumerProvided_InteractionConstraint
            (constraints+=consumerProvided_InteractionConstraint)*
        TK_RBRACE
;
emitterRequired_InteractionConstraint:
        TK_WAITS TK_COLON promise=conditionalStatement
;
consumerProvided_InteractionConstraint:
        TK_ACCEPTS TK_COLON accept=conditionalStatement
        | TK_WAITS TK_COLON wait_cl=conditionalStatement
  ;

emitterPort_functionalContract:
        TK_FUNCTIONAL
        TK_LBRACE
        constraint_pre=emitterPort_functionalConstraint
            (constraints+=emitterPort_functionalConstraint)*
        TK_RBRACE
            ;
consumerPort_functionalContract:
        TK_FUNCTIONAL
        TK_LBRACE
        constraint_pre=consumerPort_functionalConstraint
            (constraints+=consumerPort_functionalConstraint)*
        TK_RBRACE
            ;
requiredPort_functionalContract:
        TK_FUNCTIONAL
        TK_LBRACE
            (TK_PROMISES TK_COLON promises+=postStatement)*
            (constraints+=consumerPort_functionalConstraint)*
        TK_RBRACE
            ;


providedPort_functionalContract:
        TK_FUNCTIONAL
        TK_LBRACE
        constraint_pre=providedPort_functionalConstraint
            (constraints+=providedPort_functionalConstraint)*
        TK_RBRACE
            ;



emitterPort_functionalConstraint:
        TK_PROMISES TK_COLON promise=postStatement
        TK_ENSURES TK_COLON ensure=postStatement
;
consumerPort_functionalConstraint:
        TK_REQUIRES TK_COLON require=conditionalStatement
    TK_ENSURES TK_COLON ensure=postStatement
;
providedPort_functionalConstraint:
        TK_REQUIRES TK_COLON require=conditionalStatement
    (TK_ENSURES TK_COLON ensure=postStatement |
     TK_THROWS TK_COLON throwExc=ID TK_SEMICOLON
    )
;


methodSignatureSemicolon:
        methodSig=methodSignature
        TK_SEMICOLON
    ;

eventSignatureSemicolon:
    eventSig=eventSignature
     TK_SEMICOLON
        ;

methodSignature:
        rettype=dataType es=eventSignature
            ;

eventSignature:
    id=ID
    params=formalParameters
        (TK_THROWS (exceptions+=ID)+)?
        /* WRONG: (An event cannot return a value, so it cannot throw
         * an exception either)
         *
         * Actually, a method call is two events - one EMITTing the
         * information for the call, and one CONSUMing the response.
         *
         * So, the CONSUMing event is the one giving us the result
         * and/or exception.
         */
        //    (TK_THROWS exc_pre=ID (TK_COMMA excs+=ID)* )?
        ;

enumDeclaration:
    TK_ENUM
    id=ID
    TK_ASSIGN
    TK_LBRACE
    constant_pre=ID
    (TK_COMMA constants+=ID)*
    TK_RBRACE
    TK_SEMICOLON
    ;

assertDeclaration :
    TK_ASSERT
    TK_LPARANT
    cond=conditionalExpression
    TK_RPARANT
    TK_SEMICOLON
    ;

typeDefDeclaration:
        TK_TYPEDEF
        existingtype=dataType
        newtype=ID
        TK_SEMICOLON;

variableDeclaration:
    prim=primitiveVariableDeclaration TK_SEMICOLON
    |
    elem=elementVariableDeclaration TK_SEMICOLON
    ;

primitiveVariableDeclaration:
      type=dataType  id=ID (size=arraySize)?
      (op=TK_ASSIGN initval=variable_initialValue)?
     ;
formalParameter:
      type=dataType  id=ID (size=arraySize)?
      (op=TK_ASSIGN initval=variable_initialValue)?
     ;

arraySize:
        TK_LBRACKET
// The following two cases should be replaced by conditionalExpression
        (constant = NATURAL |config_par = ID)
        TK_RBRACKET
;

arrayIndex:
        TK_LBRACKET
        index = conditionalExpression
        TK_RBRACKET
;


variable_initialValue:
// The following two cases should be replaced by conditionalExpression
        icons=ID
  | inum=integerLiteral
  | itrue=TK_TRUE
  | ifalse=TK_FALSE
;

inlineFunctionDeclaration:
       id=ID
       params=formalParameters
       TK_LBRACE
       TK_RETURN inline=conditionalStatement
       TK_RBRACE

    ;

elementVariableDeclaration:
    ( elType=TK_COMPONENT
       userdefined=ID
       id=ID (size=arraySize)? (params=argumentList)? )
    | ( elType=TK_CONNECTOR
         ( userdefined=ID | basicConnProc=TK_PROC | basicConnAsync=TK_ASYNC )
         id=ID (connsize=arraySize)? conn_params=connectorArgumentList )
    ;

//assignmentStatement:
//        id=conditionalExpression (TK_ASSIGN expr=conditionalExpression TK_SEMICOLON)?
//;

conditionalStatement:
        condExpr=conditionalExpression
        TK_SEMICOLON
    ;
postStatement:
         postExpr=setExpression        TK_SEMICOLON
         (postExprs+=setExpression        TK_SEMICOLON)*
        | (skip=TK_SKIP) TK_SEMICOLON
;

conditionalExpression:
     condexpr1=relationalExpression
        (
        op+=(TK_OR|TK_AND // | TK_SEMICOLON // No idea what this is
             ) condexprs+=relationalExpression
        )*
    ;

setExpression:
    setexpr_var=equalityExpression
        ;

equalityExpression:
        eqexpr_pre=relationalExpression
                (op=TK_ASSIGN eqexpr=ternaryExpression
                |op=TK_IN set=range
                )
        ;

ternaryExpression:
        cond=relationalExpression ( op=TK_QUESTIONMARK var2=relationalExpression TK_COLON var3=ternaryExpression)?
;

relationalExpression:
        relexpr_pre=additiveExpression
        (
         op+=(TK_LESS|TK_GREATER|TK_GREATEROREQUAL
                |TK_LESSTHANOREQUAL|TK_EQUAL| TK_NOTEQUAL)
         relexprs+=additiveExpression )*
            ;
additiveExpression:
        addexpr_pre=multiplicativeExpression
        (op+=(TK_SUM|TK_SUBTRACT)
        addexprs+=multiplicativeExpression
        )*
        ;
multiplicativeExpression:
        multexpr_pre=unaryExpression
        (op+=(TK_MULTIPLY|TK_DIVIDE|TK_MODULO) multexprs+=unaryExpression
         )*
    ;
unaryExpression:
     nullexpr=nullaryExpression postop=(TK_INCREMENT | TK_DECREMENT)?
//    |preop=TK_INCREMENT nullexpr=nullaryExpression
//    |preop=TK_DECREMENT nullexpr=nullaryExpression
    |preop=TK_NEGATION nullexpr=nullaryExpression
//    |preop=TK_WHEN TK_LPARANT condexpr=conditionalExpression TK_RPARANT
    ;

nullaryExpression:
    number=integerLiteral
  | trueToken=TK_TRUE
  | falseToken=TK_FALSE
  | at=TK_AT
  | varid=ID (varindex=arrayIndex)?
  | inline_id=ID inline_args= argumentList
  |  (pre=TK_PRE)? TK_LPARANT var_withpar=conditionalExpression TK_RPARANT
  | result=TK_RESULT
  | xcd_exception = TK_EXCEPTION /* looks like "exception" is a reserved word in ANTLR */
  ;



range:
    TK_LBRACKET
    minvar=conditionalExpression TK_COMMA maxvar=conditionalExpression
    TK_RBRACKET
    ;

argumentList:
     TK_LPARANT
     (arg_pre=paramArgument
      (TK_COMMA args+=paramArgument)*
     )?
     TK_RPARANT
    ;

paramArgument:
// The following two cases should be replaced by conditionalExpression
        id=ID
  | constant = integerLiteral
  | at = TK_AT
;

connectorParameterList:
 TK_LPARANT par_pre= connectorParameter (TK_COMMA pars += connectorParameter)* TK_RPARANT
;
connectorParameter:
        (role = ID (size = arraySize)?
        TK_LBRACE
        pv_pre = ID (TK_COMMA pvs+=ID)*
        TK_RBRACE)
        | prim_param =formalParameter
;

connectorArgumentList:
 TK_LPARANT par_pre= connectorArgument (TK_COMMA pars += connectorArgument)* TK_RPARANT
;
connectorArgument:
        (role = ID (index = connectorIndex)?
        TK_LBRACE
        pv_pre = connectorArgument_pv ( TK_COMMA pvs+=connectorArgument_pv)*
        TK_RBRACE)
        | prim_val= paramArgument
;

connectorIndex:
        TK_LBRACKET
//        (constant = NATURAL
//                |const_zero = '0'
//        |at = TK_AT)
     value=additiveExpression
        TK_RBRACKET
;
connectorArgument_pv:
        pv=ID (index=connectorIndex)?
;

formalParameters:
        TK_LPARANT
        (
        par_pre=formalParameter(TK_COMMA pars+=formalParameter)*
        )?
        TK_RPARANT
    ;

dataType:
     basic=TK_INTEGER
    |basic=TK_SHORT
    |basic=TK_BYTE
    |basic=TK_BOOL
    |basic=TK_VOID
    |id=ID
    ;

integerLiteral:
  //       (valueZero='0')
  // |
  (valueUnsigned=NATURAL)
  | TK_SUBTRACT (valueSigned=NATURAL)
  ;

TK_TYPEDEF: 'typedef';
TK_CONSTANT: 'const';
TK_CONCURRENT: 'concurrent_method';

TK_CONNECTOR: 'connector';
TK_ROLE: 'role';
TK_PORTVAR: 'port_variable';
TK_COMPONENT: 'component';
TK_PORT: 'port';

TK_REQUIRED: 'required';
TK_PROVIDED: 'provided';
TK_COMPLEXPROVIDED: 'complex_provided';
TK_EMITTER: 'emitter';
TK_CONSUMER: 'consumer';

TK_INTERACTION: '@interaction';
TK_FUNCTIONAL: '@functional';
TK_INTERACTION_REQ: '@interaction_req';
TK_FUNCTIONAL_REQ: '@functional_req';
TK_INTERACTION_RES: '@interaction_res';
TK_FUNCTIONAL_RES: '@functional_res';
TK_FUNCTIONAL_EXC: '@functional_EXCEPTION';

TK_WAITS: 'waits';
TK_REQUIRES: 'requires';
TK_PROMISES: 'promises';
TK_ENSURES: 'ensures';
TK_ACCEPTS: 'accepts';
TK_WHEN: 'when';

TK_THROWS: 'throws';
TK_PRE: 'pre';


TK_BOOL: 'bool';
TK_INTEGER: 'int';
TK_BYTE: 'byte';
TK_SHORT: 'short';
TK_VOID: 'void';
TK_ENUM: 'enum';

TK_TRUE: 'true';
TK_FALSE: 'false';

TK_RETURN: 'return';
TK_RESULT : '\\result';
TK_EXCEPTION : '\\exception';
TK_SKIP: 'skip';         // in Dijkstra's Guarded Commands language it's 'do od'
TK_ABORT: 'abort';       // not used - in Dijkstra's GCL it's 'if fi'

TK_ASSERT: 'assert';

TK_IN: '\\in';

TK_PROC: 'proc';
TK_ASYNC: 'async';

TK_EQUAL: '==';
TK_NOTEQUAL: '!=';
TK_GREATER: '>';
TK_LESS: '<';
TK_GREATEROREQUAL: '>=';
TK_LESSTHANOREQUAL: '<=';
TK_NEGATION:'!';

TK_AND: '&&';
TK_OR: '||';
TK_DIVIDE: '/';
TK_MULTIPLY:'*';
TK_MODULO: '%';
TK_ASSIGN: ':=';
TK_INCREMENT:'++';
TK_DECREMENT:'--';
TK_SUM:'+';
TK_SUBTRACT:'-';

TK_DOT: '.';
TK_COMMA: ',';
TK_SEMICOLON: ';';
TK_COLON: ':';
TK_QUESTIONMARK: '?';
TK_LBRACE: '{';
TK_RBRACE: '}';
TK_LBRACKET: '[';
TK_RBRACKET: ']';
TK_LPARANT: '(';
TK_RPARANT: ')';
TK_AT: '@';

NATURAL:  '0' | [1-9] [0-9]*;

ID:   [a-zA-Z_] [a-zA-Z_0-9]* ;

WS: [ \t\n\r]+ -> skip ;

COMMENT     :   '/*' .*? '*/' {skip();} ;
LINE_COMMENT:   '//' .*? '\r'? '\n' {skip();} ;
