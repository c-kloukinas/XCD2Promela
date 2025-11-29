grammar XCD;

compilationUnits: ( elements+=compilationUnit )+ ;
compilationUnit:
  globEnum=enumDeclaration
  | globTypedef=typeDefDeclaration
  | globInlineFunction=inlineFunctionDeclaration
  | basicComponent=componentOrRoleDeclaration
  | composite=compositeOrConnectorDeclaration
  | config=elementVariableDeclaration
;

compositeOrConnectorDeclaration:
  tp=( TK_COMPOSITE | TK_COMPONENT | TK_CONNECTOR )
    id=ID ( cparams=formalParameters | xparams=connectorParameterList )?
  TK_LBRACE
    ( elements+=compositeElement )+
  TK_RBRACE
;
// CONNECTOR or COMPOSITE component (= mini configuration)
compositeElement:
  cenum=enumDeclaration
  | cTypedef=typeDefDeclaration
  | inlineFunction=inlineFunctionDeclaration
  | role=componentOrRoleDeclaration // Used in CONNECTORs & COMPOSITEs
  | variable=elementVariableDeclaration
  // | assert=assertDeclaration
;

componentOrRoleDeclaration:
  struct=(TK_COMPONENT | TK_ROLE) id=ID (param=formalParameters)?
  TK_LBRACE
    ( elements+=componentElement )+
  TK_RBRACE
;
componentElement:
  cenum=enumDeclaration
  | cTypedef=typeDefDeclaration
  | inlineFunction=inlineFunctionDeclaration
  | port=portDeclaration
  | variable=variableDeclaration TK_SEMICOLON
  // | assert=assertDeclaration
;

portDeclaration:
  type=(TK_EMITTER | TK_CONSUMER | TK_REQUIRED | TK_PROVIDED)
      valOrVar=( TK_PORT | TK_PORTVAR ) id=ID (size=arraySize)?
  TK_LBRACE
    // ( (events+=eventContract)+
    // | (methods+=methodContract)+ )
    (methods+=methodContract)+
  TK_RBRACE
;

methodContract:
  (icontract= generalInteractionContract)?
  (fcontract= generalFunctionalContract)?
  port_method=methodSignature TK_SEMICOLON
;
generalInteractionContract:
  TK_INTERACTION
  TK_LBRACE
  // Used by COMPONENTS
      (  compIC=componentInteractionConstraint
  // Used by ROLES
         | roleIC=roleInteractionConstraint    )
   TK_RBRACE
;
componentInteractionConstraint:
  ( TK_ACCEPTS TK_COLON accept=conditionalExpression TK_SEMICOLON )?
  ( TK_WAITS TK_COLON wait=conditionalExpression TK_SEMICOLON     )?
;
roleInteractionConstraint:
  ( TK_ALLOWS TK_COLON allows+=conditionalExpression TK_SEMICOLON
      TK_ENSURES TK_COLON alEnsures+=statements         )+
;
generalFunctionalContract:
  TK_FUNCTIONAL
  TK_LBRACE
  //// WHEN used to constrain values of arguments to send (aka, PROMISE)
  // Used by: emitter, required ports
    ( TK_WHEN TK_COLON wGuards+=conditionalExpression TK_SEMICOLON
        TK_ENSURES TK_COLON wEnsures+=statements           )*
  //// REQUIRES used to express cases for arguments/results received
  // Used by: consumer, required, provided ports
    ( TK_REQUIRES TK_COLON rGuards+=conditionalExpression TK_SEMICOLON
        TK_ENSURES TK_COLON rEnsures+=statements )*
  TK_RBRACE
;
methodSignature:                // rettype is obligatory for methods
  // rettype=dataType es=eventSignature
  (rettype=dataType)?
// ;
// eventSignature:
  id=ID params=formalParameters
  /* WRONG: (An event cannot return a value, so it cannot throw
   * an exception either)
   *
   * *ACTUALLY*, a method call is two events - one EMITTing the
   * information for the call, and one CONSUMing the response.
   *
   * So, the CONSUMing event is the one giving us the result
   * and/or exception.
   */
  ( TK_THROWS exc_pre=ID ( TK_COMMA excs+=ID )* )?
;
//
enumDeclaration:
  TK_ENUM
  id=ID
  TK_ASSIGN
  TK_LBRACE
    constant_pre=ID ( TK_COMMA constants+=ID )*
  TK_RBRACE
  TK_SEMICOLON
;
typeDefDeclaration:
  TK_TYPEDEF existingtype=dataType newtype=ID TK_SEMICOLON
;
inlineFunctionDeclaration:
  id=ID params=formalParameters
  TK_LBRACE
    TK_RETURN inline=expression TK_SEMICOLON
  TK_RBRACE
;

variableDeclaration:
  type=dataType  id=ID (size=arraySize)?
  (op=TK_ASSIGN initval=variableDefaultValue)?
;

// The following rule should be replaced by arrayIndex
arraySize:
  TK_LBRACKET
    (constant = NATURAL |config_par = ID)
  TK_RBRACKET
;
// The following rule should be replaced by conditionalExpression
variableDefaultValue:
  icons=ID
  | inum=NATURAL
  | itrue=TK_TRUE
  | ifalse=TK_FALSE
;

elementVariableDeclaration:
( elType=(TK_COMPONENT | TK_COMPOSITE)
     userdefined=ID
     id=ID (size=arraySize)? TK_LPAR (params=argumentList)? TK_RPAR )
     TK_SEMICOLON
  | ( elType=TK_CONNECTOR
       ( userdefined=ID | basicConnProc=TK_PROC | basicConnAsync=TK_ASYNC )
       id=ID (connsize=arraySize)? conn_params=connectorArgumentList )
    TK_SEMICOLON
;
//
connectorParameterList:
  TK_LPAR
    par_pre= connectorParameter ( TK_COMMA pars+= connectorParameter )*
  TK_RPAR
;
connectorParameter:
  ( role= ID (size= arraySize)?
    TK_LBRACE
      pv_pre= ID ( TK_COMMA pvs+= ID )*
    TK_RBRACE )
  | prim_param= variableDeclaration
;
connectorArgumentList:
  TK_LPAR
    par_pre= connectorArgument ( TK_COMMA pars+= connectorArgument )*
  TK_RPAR
;
connectorArgument:
  ( role= ID ( index= arrayIndex )?
    TK_LBRACE
      pv_pre= connectorArgument_pv ( TK_COMMA pvs+= connectorArgument_pv )*
    TK_RBRACE )
  | prim_val= expression // paramArgument
;
connectorArgument_pv:
  pv= ID ( index= arrayIndex )?
;

formalParameters:
  TK_LPAR
    ( par_pre= variableDeclaration ( TK_COMMA pars+= variableDeclaration )* )?
  TK_RPAR
;

dataType:
  basic=TK_INTEGER
  | basic=TK_SHORT
  | basic=TK_BYTE
  | basic=TK_BOOL
  | basic=TK_VOID
  | id=ID
;

statements: (stmts+=statement)+
;
statement:
  anAssgn=assignment TK_SEMICOLON
  | skip=TK_SKIP TK_SEMICOLON
  | anAssert=assertDeclaration
;

assertDeclaration :
  TK_ASSERT TK_LPAR cond=conditionalExpression TK_RPAR TK_SEMICOLON
;
//
// range and set not in Java expression grammar
range:
  TK_LBRACKET
    minVal=assignmentExpression TK_COMMA maxVal=assignmentExpression
  TK_RBRACKET
;
set:
  TK_LBRACE
    val1=assignmentExpression ( TK_COMMA vals+=assignmentExpression )*
  TK_RBRACE
;
/*
 * Expression grammar below follows the Java BNF definition at
 * https://cs.au.dk/~amoeller/RegAut/JavaBNF.html
 * (2023 Sep 04: https://web.archive.org/web/20230904055037/https://cs.au.dk/~amoeller/RegAut/JavaBNF.html)
 *
 * See also C's BNF grammar here:
 * https://cs.wmich.edu/~gupta/teaching/cs4850/sumII06/The%20syntax%20of%20C%20in%20Backus-Naur%20form.htm (2025 Apr 01: https://web.archive.org/web/20250401022151/https://cs.wmich.edu/~gupta/teaching/cs4850/sumII06/The%20syntax%20of%20C%20in%20Backus-Naur%20form.htm)
 */

/*
  // WAS:
 <assignment> ::= <leftHandSide> <assignmentOperator> <assignmentExpression>
        | <leftHandSide> \in <range>    // XXX: Extension
        | <leftHandSide> \in <set>      // XXX: Extension
*/
assignment:
  lhsIs=leftHandSide is=TK_ASSIGN assgnExpr=assignmentExpression
  | lhsIn=leftHandSide inRange=TK_IN theRange=range // XXX: Extension
  | lhsMember=leftHandSide inSet=TK_IN theSet=set   // XXX: Extension
;
/*
  <leftHandSide> ::= ID         // XXX: Extension
        | <arrayAccess>
        // | <fieldAccess> // XXX: Not present
*/
leftHandSide:
  name=ID
  | res=TK_RESULT               // XXX: Extension \result = ID
  | exc=TK_EXCEPTION            // XXX: Extension \exception = ID
  | arrayAcc=arrayAccess
;
/*
  <expression> ::= <assignmentExpression>
*/
expression: assgnExpr=assignmentExpression ;
/*
  <assignmentExpression> ::= <conditionalExpression>
        | <assignment>          // XXX: to allow chaining: a = b = 3
*/
assignmentExpression:
  condExpr=conditionalExpression
  | assgnmnt=assignment
;
/*
  <conditionalExpression> ::= <conditionalOrExpression>
        | <conditionalOrExpression> ? <expression> : <conditionalExpression>
*/
conditionalExpression:
  orExpr=conditionalOrExpression
  | orExprGuard=conditionalOrExpression
      TK_QUESTIONMARK exprThen=expression
      TK_COLON condExprElse=conditionalExpression
;
/*
  <conditionalOrExpression> ::= <conditionalAndExpression>
        | <conditionalOrExpression> || <conditionalAndExpression>
*/
conditionalOrExpression:
  andExpr=conditionalAndExpression
  | orExpr1=conditionalOrExpression TK_OR andExpr2=conditionalAndExpression
;
/*
  <conditionalAndExpression> ::= <inclusiveOrExpression>
        | <conditionalAndExpression> && <inclusiveOrExpression>

*/
conditionalAndExpression:
  bitorExpr=inclusiveOrExpression
  | andExpr1=conditionalAndExpression TK_AND bitorExpr2=inclusiveOrExpression
;
/*
  <inclusiveOrExpression> ::= <exclusiveOrExpression>
        | <inclusiveOrExpression> '|' <exclusiveOrExpression>
*/
inclusiveOrExpression:
  bitxorExpr=exclusiveOrExpression
  | bitorExpr1=inclusiveOrExpression TK_BITOR bitxorExpr2=exclusiveOrExpression
;
/*
  <exclusiveOrExpression> ::= <andExpression>
        | <exclusiveOrExpression> ^ <andExpression>
*/
exclusiveOrExpression:
  bitandExpr=andExpression
  | bitxorExpr1=exclusiveOrExpression TK_BITXOR bitandExpr2=andExpression
;
/*
  <andExpression> ::= <equalityExpression>
        | <andExpression> & <equalityExpression>
*/
andExpression:
  eqExpr=equalityExpression
  | bitandExpr1=andExpression TK_BITAND eqExpr2=equalityExpression
;
/*
  <equalityExpression> ::= <relationalExpression>
        | <equalityExpression> == <relationalExpression>
        | <equalityExpression> != <relationalExpression>
        | <equalityExpression> '\in' <range> // XXX: Extension
        | <equalityExpression> '\in' <set> // XXX: Extension
*/
equalityExpression:
  relExpr=relationalExpression
  | eqExpr1=equalityExpression
      op=( TK_EQUALS | TK_DIFFERS )
      relExpr2=relationalExpression
  | eqExpr1=equalityExpression inRange=TK_IN theRange=range
  | eqExpr1=equalityExpression inSet=TK_IN theSet=set
;
/*
  <relationalExpression> ::= <shiftExpression>
        | <relationalExpression> < <shiftExpression>
        | <relationalExpression> > <shiftExpression>
        | <relationalExpression> <= <shiftExpression>
        | <relationalExpression> >= <shiftExpression>
        // | <relationalExpression> instanceof <reference type>
*/
relationalExpression:
  shiftExpr=shiftExpression
  | relExpr1=relationalExpression
      op=( TK_LESS | TK_GREATER | TK_LESSEQ | TK_GREATEREQ)
      shiftExpr2=shiftExpression
;
/*
  <shiftExpression> ::= <additiveExpression>
        | <shiftExpression> << <additiveExpression>
        | <shiftExpression> >> <additiveExpression>
        // | <shiftExpression> >>> <additiveExpression>
        // XXX: We only consider shifts of UNSIGNED numbers!
*/
shiftExpression:
  addExpr=additiveExpression
  | shiftExpr1=shiftExpression
      op=( TK_SHIFTL | TK_SHIFTR )
      addExpr2=additiveExpression
;
/*
  <additiveExpression> ::= <multiplicativeExpression>
        | <additiveExpression> + <multiplicativeExpression>
        | <additiveExpression> - <multiplicativeExpression>
*/
additiveExpression:
  multExpr=multiplicativeExpression
  | addExpr1=additiveExpression
      op=( TK_PLUS | TK_MINUS )
      multExpr2=multiplicativeExpression
;
/*
  <multiplicativeExpression> ::= <unaryExpression>
        | <multiplicativeExpression> * <unaryExpression>
        | <multiplicativeExpression> / <unaryExpression>
        | <multiplicativeExpression> % <unaryExpression>
*/
multiplicativeExpression:
  unaryExpr=unaryExpression
  | multExpr1=multiplicativeExpression
      op=( TK_MULT | TK_DIV | TK_MOD )
      unaryExpr2=unaryExpression
;
/*
  <unaryExpression> ::= + <unaryExpression>
        | - <unaryExpression>
        | <unaryExpressionNotPlusMinus>
*/
unaryExpression:
  op=( TK_PLUS | TK_MINUS ) unaryExpr=unaryExpression
  | unaryExprNPM=unaryExpressionNotPlusMinus
;
/*
  <unaryExpressionNotPlusMinus> ::= <primary>
     //   | ID                  // XXX: can get this from <primary>
        | ~ <unaryExpression>
        | ! <unaryExpression>
*/
unaryExpressionNotPlusMinus:
 prim=primary
 | op=( TK_BITNOT | TK_NOT ) unaryExpr=unaryExpression
;
/*
  <primary> ::= <aLiteral>
        | ( <expression> )
        | ID                    // XXX: Extention
        | <functionInvocation>
        | <arrayAccess>
*/
primary:
  lit=aLiteral
  | TK_LPAR parExpr=expression TK_RPAR
  | name=ID
  | at=TK_AT                          // XXX: Extension - another form of ID
  | theResult=TK_RESULT               // XXX: Extension - another form of ID
  | theException=TK_EXCEPTION         // XXX: Extension - another form of ID
  | funcCall=functionInvocation
  | arrayAcc=arrayAccess
;
/*
  <functionInvocation> ::= ID ( <argumentList>? )
*/
functionInvocation: funcName=ID TK_LPAR (args=argumentList)? TK_RPAR ;
/*
  <argumentList> ::= <expression>
        | <argumentList> , <expression>
*/
argumentList: arg1=expression ( TK_COMMA restArgs+=expression )* ;
/*
  <arrayAccess> ::= ID [ <expression> ]
*/
arrayAccess: arrayName=ID // TK_LBRACKET arrayOffset=expression TK_RBRACKET
      arrayIndex
;
arrayIndex:
  TK_LBRACKET
    arrayOffset=expression
  TK_RBRACKET
;
/*
  <aLiteral> ::= <integerLiteral>
        | <booleanLiteral>
*/
aLiteral:
  number=NATURAL
  | trueToken=TK_TRUE
  | falseToken=TK_FALSE
;
//Lexer:
TK_TYPEDEF: 'typedef';
TK_CONSTANT: 'const';

TK_COMPOSITE: 'composite';
        TK_CONNECTOR: 'connector';
TK_COMPONENT: 'component';
        TK_ROLE: 'role';
TK_PORT: 'port';
        TK_PORTVAR: 'port_variable';

TK_EMITTER: 'emitter';
TK_CONSUMER: 'consumer';
        TK_REQUIRED: 'required';
        TK_PROVIDED: 'provided';

TK_INTERACTION: '@interaction';
TK_FUNCTIONAL: '@functional';

TK_ACCEPTS: 'accepts';
TK_WAITS: 'waits';
        TK_ALLOWS: 'allows';
TK_WHEN: 'when';
TK_REQUIRES: 'requires';
TK_ENSURES: 'ensures';

TK_THROWS: 'throws';
TK_PRE: 'pre';                  /* Do I *REALLY* need it? Anything in
                                 * the LHS ':=' is a POST variable,
                                 * anything in the RHS is a PRE
                                 * variable */


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

TK_EQUALS: '==';
TK_DIFFERS: '!=';
TK_GREATER: '>';
TK_LESS: '<';
TK_GREATEREQ: '>=';
TK_LESSEQ: '<=';
TK_BITNOT:'~';
TK_NOT:'!';

TK_SHIFTL: '<<';
TK_SHIFTR: '>>';
TK_BITAND: '&';
TK_BITXOR: '^';
TK_BITOR: '|';
TK_AND: '&&';
TK_OR: '||';
TK_DIV: '/';
TK_MULT:'*';
TK_MOD: '%';
TK_ASSIGN: ':=';
TK_INCREMENT:'++';
TK_DECREMENT:'--';
TK_PLUS:'+';
TK_MINUS:'-';

TK_COMMA: ',';
TK_SEMICOLON: ';';
TK_COLON: ':';
TK_QUESTIONMARK: '?';
TK_LBRACE: '{';
TK_RBRACE: '}';
TK_LBRACKET: '[';
TK_RBRACKET: ']';
TK_LPAR: '(';
TK_RPAR: ')';
TK_AT: '@';

NATURAL:  '0' | [1-9] [0-9]*;

ID:   [a-zA-Z_] [a-zA-Z_0-9]* ;

WS: [ \t\n\r]+ -> skip ;

COMMENT     :   '/*' .*? '*/' {skip();} ;
LINE_COMMENT:   '//' .*? '\r'? '\n' {skip();} ;
