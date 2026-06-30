dnl -*- mode: m4 -*-
divert(-1)dnl discard output - hash'ed comments don't copy over
dnl collect all typedefs that need to be done globally
define(_GLOBAL_TYPEDEFS,dnl UNDEFINED

)
define(_GLOBAL_VARDECLS,dnl UNDEFINED
)
define(_GLOBAL_INITS,dnl UNDEFINED
)

define(`_CAT', `$1$2')dnl
define(`_NAME', `ifelse(`$#',`0',`',`ifelse(`$#',`1',`$1',`$1_`'_NAME(shift($@))')')')dnl
define(`_EVALNAME', `_CAT(_NAME($@))')dnl

# case 1: `includeall' (no parens) $#==0
# case 2: `includeall()' $#==1, $1==`'
# case 3: `includeall(foo)' $#==1, $1!=`'
define(`_includeall', `ifelse(`$#',`0',`',`ifelse(`$1',`',`$1',`include($1) includeall(shift($@))')')')dnl

dnl define(`drefine',`define($1,$2)')

divert(0)dnl

