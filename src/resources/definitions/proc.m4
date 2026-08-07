dnl -*- mode: m4 -*-
divert(-1)dnl
# include definitions of subconnectors
# NO SUBCONNECTORS
#
define(`_proc',`dnl
dnl### Start of connector proc definition
pushdef(`_context',`$1')dnl
pushdef(`_varname',`$2')dnl
pushdef(`_params',`')dnl
dnl
dnl Same context & varname cannot have different params, so params not in name
dnl (first comma in _NAME is to get an initial _).
pushdef(`__prefixX',_NAME(,_context,Xproc,V`'_varname))dnl
dnl
dnl##### Start of role caller definitions
dnl
dnl #
dnl # Name of role#1
dnl #
define(_NAME(__prefixX,RlNm1),`caller')dnl
dnl
dnl #
dnl # Prefix of role#1
dnl #
pushdef(`__prefixR',_NAME(__prefixX,Rl1))dnl
dnl DEBUG: Role _NAME(__prefixX,RlNm1) _EVALNAME(__prefixX,RlNm1) is __prefixR
dnl #
dnl # Size expr, and size of the role
dnl #
define(_NAME(__prefixR,sizeExpr),`1')dnl
define(_NAME(__prefixR,size),`1')dnl
dnl #
dnl # Size checker of role#1
dnl #
define(_NAME(__prefixR,checkSize),`dnl
dnl Delay evaluation of $1 below, so it is the $1 of checkSize, and _NOT_
dnl that of the connector macro (within which checkSize is defined).
pushdef(`__mySizeShouldBeExpr',$'`1)dnl
pushdef(`__mySizeShouldBe',eval(__mySizeShouldBeExpr))dnl
ifelse(__mySizeShouldBe,'_EVALNAME(__prefixR,size)`,,dnl
dnl Below we use two quotes because we are inside the quoted _error,
dnl which is inside the quoted macro definition body
`_error(Role array size ("''_NAME(__prefixR,sizeExpr)" = _EVALNAME(__prefixR,size)``) is different to size passed ("__mySizeShouldBeExpr" = __mySizeShouldBe).
)')dnl
popdef(`__mySizeShouldBe')dnl
popdef(`__mySizeShouldBeExpr')dnl
')dnl
dnl #
dnl # Typedefs of role#1
dnl #
dnl # Name of the type of the role variables
pushdef(`__prefixRType',_NAME(__prefixR,Type))dnl
dnl #
dnl # Type definition for the structure containing the role variables.
dnl #
dnl # role variables should also contain the variables of any          TODO XXX
dnl # sub-connector roles that this role has been associated with
define(_NAME(__prefixR,typedefs),)dnl
dnl #
dnl # Role variable (structure) declaration
dnl #
define(_NAME(__prefixR,vardecls),)dnl
dnl #
dnl # Role variable initialisations
dnl #
define(_NAME(__prefixR,inits),)dnl
dnl##### Start of role caller, port client, action _ definitions
dnl # Connector,Role,Port
dnl #
dnl # Name of port#1
dnl #
define(_NAME(__prefixR,PrtNm1),`client')dnl
dnl #
dnl # Prefix of port#1
dnl #
pushdef(`__prefixRP',_NAME(__prefixR,Prt1))dnl
dnl DEBUG: Port _NAME(__prefixR,PrtNm1) _EVALNAME(__prefixR,PrtNm1) is __prefixRP
dnl #
dnl # Size expr, size, and kind of the port
dnl #
define(_NAME(__prefixRP,sizeExpr),`1')dnl
define(_NAME(__prefixRP,size),`1')dnl
define(_NAME(__prefixRP,kind),`required')dnl
dnl #
dnl # Size checker of port#1
dnl #
define(_NAME(__prefixRP,checkSize),`dnl
dnl Delay evaluation of $1 below, so it is the $1 of checkSize, and _NOT_
dnl that of the connector macro (within which checkSize is defined).
pushdef(`__mySizeShouldBeExpr',$'`1)dnl
pushdef(`__mySizeShouldBe',eval(__mySizeShouldBeExpr))dnl
ifelse(__mySizeShouldBe,'_EVALNAME(__prefixRP,size)`,,dnl
dnl Below we use two quotes because we are inside the quoted _error,
dnl which is inside the quoted macro definition body
`_error(Port array size ("''_NAME(__prefixRP,sizeExpr)" = _EVALNAME(__prefixRP,size)``) is different to size passed ("__mySizeShouldBeExpr" = __mySizeShouldBe).
)')dnl
popdef(`__mySizeShouldBe')dnl
popdef(`__mySizeShouldBeExpr')dnl
')dnl
dnl #
dnl # Check that the port if of the right kind
dnl #
define(_NAME(__prefixRP,checkKind),`dnl
pushdef(`__myKindShouldBe',$'`1)dnl
ifelse(__myKindShouldBe,'_NAME(__prefixRP,kind)`,,dnl
`_error(Port kind (''_NAME(__prefixRP,kind)``) is different to kind passed (__myKindShouldBe).
)')dnl
popdef(`__myKindShouldBe')dnl
')dnl
dnl #
dnl # Check both the port size and kind
dnl #
define(_NAME(__prefixRP,check),`dnl
_CAT(_NAME('__prefixRP`,checkSize)('$`1))dnl
_CAT(_NAME('__prefixRP`,checkKind)('$`2))dnl
')dnl
dnl # Connector,Role,Port,Action guards
dnl #
dnl pushdef(`__prefixRPA',dnl name of the role action
dnl _NAME(__prefixRP,))dnl
dnl define(_NAME(__prefixRPA,guards),)dnl
dnl dnl # Conditional updates for actions
dnl dnl
dnl define(_NAME(__prefixRPA,ensures),)dnl
dnl popdef(`__prefixRPA')dnl
dnl popdef(`__prefixRP')dnl
dnl##### End of role caller, port client, action _ definitions
`'dnl
popdef(`__prefixRType')dnl
popdef(`__prefixR')dnl
dnl##### End of role caller definitions
dnl
dnl##### Start of role callee definitions
dnl
dnl #
dnl # Name of role#2
dnl #
define(_NAME(__prefixX,RlNm2),`callee')dnl
dnl
dnl #
dnl # Prefix of role#2
dnl #
pushdef(`__prefixR',_NAME(__prefixX,Rl2))dnl
dnl #
dnl # Size expr, and size of the role
dnl #
define(_NAME(__prefixR,sizeExpr),`1')dnl
define(_NAME(__prefixR,size),`1')dnl
dnl #
dnl # Size checker of role#2
dnl #
define(_NAME(__prefixR,checkSize),`dnl
dnl Delay evaluation of $1 below, so it is the $1 of checkSize, and _NOT_
dnl that of the connector macro (within which checkSize is defined).
pushdef(`__mySizeShouldBeExpr',$'`1)dnl
pushdef(`__mySizeShouldBe',eval(__mySizeShouldBeExpr))dnl
ifelse(__mySizeShouldBe,'_EVALNAME(__prefixR,size)`,,dnl
dnl Below we use two quotes because we are inside the quoted _error,
dnl which is inside the quoted macro definition body
`_error(Role array size ("''_NAME(__prefixR,sizeExpr)" = _EVALNAME(__prefixR,size)``) is different to size passed ("__mySizeShouldBeExpr" = __mySizeShouldBe).
)')dnl
popdef(`__mySizeShouldBe')dnl
popdef(`__mySizeShouldBeExpr')dnl
')dnl
dnl #
dnl # Typedefs of role#2
dnl #
dnl # Name of the type of the role variables
pushdef(`__prefixRType',_NAME(__prefixR,Type))dnl
dnl #
dnl # Type definition for the structure containing the role variables.
dnl #
dnl # role variables should also contain the variables of any          TODO XXX
dnl # sub-connector roles that this role has been associated with
define(_NAME(__prefixR,typedefs),)dnl
dnl #
dnl # Role variable (structure) declaration
dnl #
define(_NAME(__prefixR,vardecls),)dnl
dnl #
dnl # Role variable initialisations
dnl #
define(_NAME(__prefixR,inits),)dnl
dnl##### Start of role callee, port server, action _ definitions
dnl # Connector,Role,Port
dnl #
dnl # Name of port#1
dnl #
define(_NAME(__prefixR,PrtNm1),`server')dnl
dnl #
dnl # Prefix of port#1
dnl #
pushdef(`__prefixRP',_NAME(__prefixR,Prt1))dnl
dnl DEBUG: Port _NAME(__prefixR,PrtNm1) _EVALNAME(__prefixR,PrtNm1) is __prefixRP
dnl #
dnl # Size expr, size, and kind of the port
dnl #
define(_NAME(__prefixRP,sizeExpr),`1')dnl
define(_NAME(__prefixRP,size),`1')dnl
define(_NAME(__prefixRP,kind),`provided')dnl
dnl #
dnl # Size checker of port#1
dnl #
define(_NAME(__prefixRP,checkSize),`dnl
dnl Delay evaluation of $1 below, so it is the $1 of checkSize, and _NOT_
dnl that of the connector macro (within which checkSize is defined).
pushdef(`__mySizeShouldBeExpr',$'`1)dnl
pushdef(`__mySizeShouldBe',eval(__mySizeShouldBeExpr))dnl
ifelse(__mySizeShouldBe,'_EVALNAME(__prefixRP,size)`,,dnl
dnl Below we use two quotes because we are inside the quoted _error,
dnl which is inside the quoted macro definition body
`_error(Port array size ("''_NAME(__prefixRP,sizeExpr)" = _EVALNAME(__prefixRP,size)``) is different to size passed ("__mySizeShouldBeExpr" = __mySizeShouldBe).
)')dnl
popdef(`__mySizeShouldBe')dnl
popdef(`__mySizeShouldBeExpr')dnl
')dnl
dnl #
dnl # Check that the port if of the right kind
dnl #
define(_NAME(__prefixRP,checkKind),`dnl
pushdef(`__myKindShouldBe',$'`1)dnl
ifelse(__myKindShouldBe,'_NAME(__prefixRP,kind)`,,dnl
`_error(Port kind (''_NAME(__prefixRP,kind)``) is different to kind passed (__myKindShouldBe).
)')dnl
popdef(`__myKindShouldBe')dnl
')dnl
dnl #
dnl # Check both the port size and kind
dnl #
define(_NAME(__prefixRP,check),`dnl
_CAT(_NAME('__prefixRP`,checkSize)('$`1))dnl
_CAT(_NAME('__prefixRP`,checkKind)('$`2))dnl
')dnl
dnl # Connector,Role,Port,Action guards
dnl #
dnl pushdef(`__prefixRPA',dnl name of the role action
dnl _NAME(__prefixRP,))dnl
dnl define(_NAME(__prefixRPA,guards),)dnl
dnl dnl # Conditional updates for actions
dnl dnl
dnl define(_NAME(__prefixRPA,ensures),)dnl
dnl popdef(`__prefixRPA')dnl
dnl popdef(`__prefixRP')dnl
dnl##### End of role callee, port server, action _ definitions
`'dnl
popdef(`__prefixRType')dnl
popdef(`__prefixR')dnl
dnl##### End of role callee definitions
`'dnl
popdef(`__prefixX')dnl
popdef(`_params')dnl
popdef(`_varname')dnl
popdef(`_context')dnl
dnl### End of connector proc definition
')dnl
divert(0)dnl
dnl Debugging
dnl _proc(a,b)
dnl _a_Xproc_Vb_RlNm1 _a_Xproc_Vb_Rl1_PrtNm1 _a_Xproc_Vb_Rl1_Prt1_kind
dnl _a_Xproc_Vb_RlNm2 _a_Xproc_Vb_Rl2_PrtNm1 _a_Xproc_Vb_Rl2_Prt1_kind

