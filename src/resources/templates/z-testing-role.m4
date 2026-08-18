divert(-1)
define(`_context',ROOT)
define(`_varname',bar)
dnl
dnl hello, _CAT(zello,bar), _CAT(bar,zello) and hi
dnl
a0 _context
b0 _varname
dnl __$<connector_name>(_context,_varname)
pushdef(`_context',defn(`_context')_foo)dnl
pushdef(`_varname',bar)dnl
a1 _context
b1 _varname
c1 _GLOBAL_TYPEDEFS
dnl _$<connector_name>(_context,_varname)$< connector_role_tests>

dnl _$<connector_name>($<params_fictional>)dnl $ <connector_role_tests>
# params_fictional: ($<params_fictional>) _CAT(_CAT(eval(_NAME($<params_fictional>))))
_$<connector_name>_client($<params_fictional>)dnl $ <connector_role_tests>

_NAME(_context,$<connector_name>,_varname,client_service_open_guards)
_EVALNAME(_context,$<connector_name>,_varname,client_service_open_guards)

_NAME(_context,$<connector_name>,_varname,client_service_open_ensures)
_EVALNAME(_context,$<connector_name>,_varname,client_service_open_ensures)

_NAME(_context,$<connector_name>,_varname,client_service_close_guards)
_EVALNAME(_context,$<connector_name>,_varname,client_service_close_guards)

_NAME(_context,$<connector_name>,_varname,client_service_close_ensures)
_EVALNAME(_context,$<connector_name>,_varname,client_service_close_ensures)

_NAME(_context,$<connector_name>,_varname,client_service_request_guards)
_EVALNAME(_context,$<connector_name>,_varname,client_service_request_guards)

_NAME(_context,$<connector_name>,_varname,client_service_request_ensures)
_EVALNAME(_context,$<connector_name>,_varname,client_service_request_ensures)

_$<connector_name>_server($<params_fictional>)dnl $ <connector_role_tests>

_NAME(_context,$<connector_name>,_varname,server_service_open_guards)
_EVALNAME(_context,$<connector_name>,_varname,server_service_open_guards)

_NAME(_context,$<connector_name>,_varname,server_service_open_ensures)
_EVALNAME(_context,$<connector_name>,_varname,server_service_open_ensures)

_NAME(_context,$<connector_name>,_varname,server_service_close_guards)
_EVALNAME(_context,$<connector_name>,_varname,server_service_close_guards)

_NAME(_context,$<connector_name>,_varname,server_service_close_ensures)
_EVALNAME(_context,$<connector_name>,_varname,server_service_close_ensures)

_NAME(_context,$<connector_name>,_varname,server_service_request_guards)
_EVALNAME(_context,$<connector_name>,_varname,server_service_request_guards)

_NAME(_context,$<connector_name>,_varname,server_service_request_ensures)
_EVALNAME(_context,$<connector_name>,_varname,server_service_request_ensures)

popdef(`_context')dnl
popdef(`_varname')dnl
a2 _context
b2 _varname`'dnl $ <connector_role_tests>
dnl # c-start
dnl # dumpdef(`__$<connector_name>')
dnl # c-end dnl

pushdef(`_context',defn(`_context')_foo)dnl
pushdef(`_varname',bar)dnl $ <connector_role_tests>
popdef(`_context')dnl
popdef(`_varname')dnl
dnl
dnl hello, _CAT(zello,bar), _CAT(bar,zello) and hi

ct _GLOBAL_TYPEDEFS

cv _GLOBAL_VARDECLS

ci _GLOBAL_INITS

divert(0)dnl start of v2 macros checking.
XCXC
define(`_context',`_NONE')
define(`_varname',`foobar')
define(`my_connector_name',client2server_deadlock)
AA _CAT(_,my_connector_name)(_context,_varname,3,1,2)dnl _$<connector_name>(_context,_varname,3,1,2)dnl
dnl (first comma in _NAME below is to get an initial _).
dnl define(`theXInstanceName',_NAME(_context,X`'my_connector_name,_CAT(V,_varname)))dnl
define(`theXInstanceName',__connectorId(_context,my_connector_name,_varname))dnl
BB _NAME(theXInstanceName,Rl1,Name) "_EVALNAME(theXInstanceName,Rl1,Name)"
CC Correct role size?
_NAME(theXInstanceName,Rl1_checkSize)(1*1) "_CAT(_NAME(theXInstanceName,Rl1_checkSize)(1*1))"

DD _NAME(theXInstanceName,Rl2,Name) "_EVALNAME(theXInstanceName,Rl2,Name)"
EE Correct role size?
_NAME(theXInstanceName,Rl2_checkSize)(2-1) "_CAT(_NAME(theXInstanceName,Rl2_checkSize)(2-1))"

FF Port _NAME(theXInstanceName,Rl1,PrtNm1) "_EVALNAME(theXInstanceName,Rl1,PrtNm1)":
GG Correct port size & type?
_NAME(theXInstanceName,Rl1,Prt1,checkSize)(1+0) "_CAT(_NAME(theXInstanceName,Rl1,Prt1,checkSize)(1+0))"
_NAME(theXInstanceName,Rl1,Prt1,checkKind)(required) "_CAT(_NAME(theXInstanceName,Rl1,Prt1,checkKind)(required))"
_NAME(theXInstanceName,Rl1,Prt1,check)(2-1,required) "_CAT(_NAME(theXInstanceName,Rl1,Prt1,check)(2-1,required))"

connectorId: __connectorId(_context,`'my_connector_name,_varname) _EVALNAME(         __connectorId(_context,`'my_connector_name,_varname),sizeExpr)
     roleId: __roleId(     _context,`'my_connector_name,_varname,1) _EVALNAME(       __roleId(     _context,`'my_connector_name,_varname,1),sizeExpr)
     portId: __portId(     _context,`'my_connector_name,_varname,1,1) _EVALNAME(     __portId(     _context,`'my_connector_name,_varname,1,1),sizeExpr)
   actionId: __actionId(   _context,`'my_connector_name,_varname,1,1,open)
"_EVALNAME(__actionId(   _context,`'my_connector_name,_varname,1,1,open),guards)"

connectorId: __connectorId(_context,`'my_connector_name,_varname) _EVALNAME(         __connectorId(_context,`'my_connector_name,_varname),size)
     roleId: __roleId(     _context,`'my_connector_name,_varname,1) _EVALNAME(       __roleId(     _context,`'my_connector_name,_varname,1),size)
     portId: __portId(     _context,`'my_connector_name,_varname,1,1) _EVALNAME(     __portId(     _context,`'my_connector_name,_varname,1,1),size)
   actionId: __actionId(   _context,`'my_connector_name,_varname,1,1,open)
"_EVALNAME(__actionId(   _context,`'my_connector_name,_varname,1,1,open),ensures)"

define(_NAME(__roleId(_context,my_connector_name,_varname,1),ActualName),SIVLE)
define(_NAME(__roleId(_context,my_connector_name,_varname,1),ActualNameLHS),ELVIS)
define(_NAME(__roleId(_context,my_connector_name,_varname,2),ActualName),SIVLE)
define(_NAME(__roleId(_context,my_connector_name,_varname,2),ActualNameLHS),ELVIS)

ctBEGIN
__GLOBAL_TYPEDEFS
ctEND

cvBEGIN
__GLOBAL_VARDECLS
cvEND

ciBEGIN
__GLOBAL_INITS
ciEND

dnl Debugging
dnl _async(a,b)
dnl _a_Xasync_Vb_Rl1_Name _a_Xasync_Vb_Rl1_PrtNm1 _a_Xasync_Vb_Rl1_Prt1_kind
dnl _a_Xasync_Vb_Rl2_Name _a_Xasync_Vb_Rl2_PrtNm1 _a_Xasync_Vb_Rl2_Prt1_kind
dnl
dnl _proc(a,b)
dnl _a_Xproc_Vb_Rl1_Name _a_Xproc_Vb_Rl1_PrtNm1 _a_Xproc_Vb_Rl1_Prt1_kind
dnl _a_Xproc_Vb_Rl2_Name _a_Xproc_Vb_Rl2_PrtNm1 _a_Xproc_Vb_Rl2_Prt1_kind

