define(`CAT', `$1$2')

define(`_context',ROOT)
define(`_varname',`bar')

define(`conn',dnl
  pushdef(`_context',CAT(defn(`_context'),_conn))dnl
  pushdef(`_varname',`'$1)dnl
dnl
  a `'_varname`'dnl
  b $1`'dnl
  c _context`_'$1`'dnl
  `define('_context`_'$1`,zanzibar)'dnl
  d _context`_'$1`'dnl
dnl
  popdef(`_varname')dnl
  popdef(`_context')dnl
)dnl

conn(foo)

ROOT_conn_foo

_context
_varname
