divert(-1)
define(`my_connector_name',client2server_deadlock)dnl
include(0-common-defs.m4)dnl
define(`srcFile',_NAME(CONNECTOR_TYPE,client2server_deadlock.pml.m4))dnl
include(srcFile)dnl
dnl
dnl old content moved after m4exit
dnl
dnl traceon(`_client2server_deadlock')dnl
dnl traceon(`_client2server_internal')dnl
dnl traceon(`_proc')dnl
divert(0)dnl start of v2 macros checking.
XCXC
define(`_ctxRoot',`_NONE')dnl
define(`_varnameRoot',`foobar')dnl
define(`_varnameRootSize',`3')dnl
dnl (first comma in _NAME below is to get an initial _).
AA
`_ctxRoot' _ctxRoot
`_varnameRoot' _varnameRoot
`_varnameRootSize' _varnameRootSize
dnl DO NOT enclose the next line in a CAT()!!!
_CAT(_,my_connector_name)(_ctxRoot,_varnameRoot,_varnameRootSize, 1,2)`'dnl
/* Defined connector my_connector_name */
define(`theXInstanceName',__connectorId(_ctxRoot,my_connector_name,_varnameRoot))dnl
dnl now call role macros, to complete their definitions
dnl
define(rRl1,__roleId(_ctxRoot,my_connector_name,_varnameRoot,1))dnl
define(rRl2,__roleId(_ctxRoot,my_connector_name,_varnameRoot,2))dnl
dnl
dnl Rl1s XvarIterator is _EVALNAME(rRl1,XvarIterator)
define(_NAME(rRl1,InstancePrefixNoOffset),FOO)dnl
define(_EVALNAME(rRl1,XvarIterator),xIter)dnl
define(_NAME(rRl1,InstancePrefixWithOffset),dnl
_NAME(rRl1,InstancePrefixNoOffset)[_EVALNAME(rRl1,XvarIterator)]`'dnl
)dnl
dnl Rl2s XvarIterator is _EVALNAME(rRl2,XvarIterator)
define(_NAME(rRl2,InstancePrefixNoOffset),SNA)dnl
dnl NOTE: rl2 XvarIterator is the same as rl1 XvarIterator!!!
dnl define(_EVALNAME(rRl2,XvarIterator),xIter)dnl
define(_NAME(rRl2,InstancePrefixWithOffset),dnl
_NAME(rRl2,InstancePrefixNoOffset)[_EVALNAME(rRl2,XvarIterator)]`'dnl
)dnl
dnl _EVALNAME(rRl1,Define)(_ctxRoot,_varnameRoot,_NAME(theXInstanceName,Iterator))
dnl _EVALNAME(rRl2,Define)(_ctxRoot,_varnameRoot,_NAME(theXInstanceName,Iterator))
// Defined roles 1 & 2 of connector my_connector_name
dnl
BB _NAME(rRl1,Name) "_EVALNAME(rRl1,Name)" "_CAT(_EVALNAME(rRl1,Name))"
CC Correct role size?
_NAME(rRl1,checkSize)(1*1) "_CAT(_NAME(rRl1,checkSize)(1*1))"

DD _NAME(rRl2,Name) "_EVALNAME(rRl2,Name)"
EE Correct role size?
_NAME(rRl2,checkSize)(2-1) "_CAT(_NAME(rRl2,checkSize)(2-1))"

FF Port _NAME(rRl1,PrtNm1) "_EVALNAME(rRl1,PrtNm1)":
GG Correct port size & type?
_NAME(rRl1,Prt1,checkSize)(1+0) "_CAT(_NAME(rRl1,Prt1,checkSize)(1+0))"
_NAME(rRl1,Prt1,checkKind)(required) "_CAT(_NAME(rRl1,Prt1,checkKind)(required))"
_NAME(rRl1,Prt1,check)(2-1,required) "_CAT(_NAME(rRl1,Prt1,check)(2-1,required))"


connectorId: __connectorId(_ctxRoot,`'my_connector_name,_varnameRoot) _EVALNAME(         __connectorId(_ctxRoot,`'my_connector_name,_varnameRoot),sizeExpr)
     roleId: __roleId(     _ctxRoot,`'my_connector_name,_varnameRoot,1) _EVALNAME(       __roleId(     _ctxRoot,`'my_connector_name,_varnameRoot,1),sizeExpr)
     portId: __portId(     _ctxRoot,`'my_connector_name,_varnameRoot,1,1) _EVALNAME(     __portId(     _ctxRoot,`'my_connector_name,_varnameRoot,1,1),sizeExpr)
   actionId: __actionId(   _ctxRoot,`'my_connector_name,_varnameRoot,1,1,open)
"_EVALNAME(__actionId(   _ctxRoot,`'my_connector_name,_varnameRoot,1,1,open),guards)"

connectorId: __connectorId(_ctxRoot,`'my_connector_name,_varnameRoot) _EVALNAME(         __connectorId(_ctxRoot,`'my_connector_name,_varnameRoot),size)
     roleId: __roleId(     _ctxRoot,`'my_connector_name,_varnameRoot,1) _EVALNAME(       __roleId(     _ctxRoot,`'my_connector_name,_varnameRoot,1),size)
     portId: __portId(     _ctxRoot,`'my_connector_name,_varnameRoot,1,1) _EVALNAME(     __portId(     _ctxRoot,`'my_connector_name,_varnameRoot,1,1),size)
   actionId: __actionId(   _ctxRoot,`'my_connector_name,_varnameRoot,1,1,open)
"_EVALNAME(__actionId(   _ctxRoot,`'my_connector_name,_varnameRoot,1,1,open),ensures)"

ctBEGIN
__GLOBAL_TYPEDEFS
ctEND

cvBEGIN
dnl __GLOBAL_VARDECLS
_varnameRoot [@_EVALNAME(__connectorId(_ctxRoot,`'my_connector_name,_varnameRoot),Iterator):_varnameRootSize] {
_EVALNAME(__roleId(     _ctxRoot,`'my_connector_name,_varnameRoot,1),vardecls)

_EVALNAME(__roleId(     _ctxRoot,`'my_connector_name,_varnameRoot,2),vardecls)
}
cvEND

ciBEGIN
dnl __GLOBAL_INITS
_EVALNAME(__roleId(     _ctxRoot,`'my_connector_name,_varnameRoot,1),inits)

_EVALNAME(__roleId(     _ctxRoot,`'my_connector_name,_varnameRoot,2),inits)
ciEND

dnl Debugging
dnl _async(a,b)
dnl _a_Xasync_Vb_Rl1_Name _a_Xasync_Vb_Rl1_PrtNm1 _a_Xasync_Vb_Rl1_Prt1_kind
dnl _a_Xasync_Vb_Rl2_Name _a_Xasync_Vb_Rl2_PrtNm1 _a_Xasync_Vb_Rl2_Prt1_kind
dnl
dnl _proc(a,b)
dnl _a_Xproc_Vb_Rl1_Name _a_Xproc_Vb_Rl1_PrtNm1 _a_Xproc_Vb_Rl1_Prt1_kind
dnl _a_Xproc_Vb_Rl2_Name _a_Xproc_Vb_Rl2_PrtNm1 _a_Xproc_Vb_Rl2_Prt1_kind

dnl

m4exit(0)
_warning(XXX: Print a list of all macros along with their definitions
)
dumpdef

PROC
dumpdef(`_proc')

INTERNAL
dumpdef(`_client2server_internal')

DEADLOCK
dumpdef(`_client2server_deadlock')

define(`_ctxRoot',ROOT)
define(`_varnameRoot',bar)
dnl
dnl hello, _CAT(zello,bar), _CAT(bar,zello) and hi
dnl
a0 _ctxRoot
b0 _varnameRoot
dnl __$<connector_name>(_ctxRoot,_varnameRoot)
pushdef(`_ctxRoot',defn(`_ctxRoot')_foo)dnl
pushdef(`_varnameRoot',bar)dnl
a1 _ctxRoot
b1 _varnameRoot
c1 _GLOBAL_TYPEDEFS
dnl _$<connector_name>(_ctxRoot,_varnameRoot)$< connector_role_tests>

dnl _$<connector_name>($<params_fictional>)dnl $ <connector_role_tests>
dnl # params_fictional: ($<params_fictional>) _CAT(_CAT(_EVALNAME($<params_fictional>)))
dnl _$<connector_name>_client($<params_fictional>)dnl $ <connector_role_tests>

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_open_guards)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_open_guards)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_open_ensures)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_open_ensures)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_close_guards)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_close_guards)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_close_ensures)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_close_ensures)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_request_guards)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_request_guards)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_request_ensures)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,client_service_request_ensures)

dnl _$<connector_name>_server($<params_fictional>)dnl $ <connector_role_tests>

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_open_guards)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_open_guards)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_open_ensures)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_open_ensures)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_close_guards)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_close_guards)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_close_ensures)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_close_ensures)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_request_guards)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_request_guards)

_NAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_request_ensures)
_EVALNAME(_ctxRoot,$<connector_name>,_varnameRoot,server_service_request_ensures)

popdef(`_ctxRoot')dnl
popdef(`_varnameRoot')dnl
a2 _ctxRoot
b2 _varnameRoot`'dnl $ <connector_role_tests>
dnl # c-start
dnl # dumpdef(`__$<connector_name>')
dnl # c-end dnl

pushdef(`_ctxRoot',defn(`_ctxRoot')_foo)dnl
pushdef(`_varnameRoot',bar)dnl $ <connector_role_tests>
popdef(`_ctxRoot')dnl
popdef(`_varnameRoot')dnl
dnl
dnl hello, _CAT(zello,bar), _CAT(bar,zello) and hi

ct _GLOBAL_TYPEDEFS

cv _GLOBAL_VARDECLS

ci _GLOBAL_INITS

