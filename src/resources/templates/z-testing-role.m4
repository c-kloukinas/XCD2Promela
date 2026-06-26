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
_$<connector_name>$<params_fictional>$<connector_role_tests>
popdef(`_context')dnl
popdef(`_varname')dnl
a2 _context
b2 _varname $<connector_role_tests>
dnl # c-start
dnl # dumpdef(`__$<connector_name>')
dnl # c-end dnl

pushdef(`_context',defn(`_context')_foo)dnl
pushdef(`_varname',bar)$<connector_role_tests>
popdef(`_context')dnl
popdef(`_varname')dnl
dnl
dnl hello, _CAT(zello,bar), _CAT(bar,zello) and hi

cn _GLOBAL_TYPEDEFS
