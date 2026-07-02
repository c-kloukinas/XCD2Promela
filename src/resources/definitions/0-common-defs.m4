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

## _forloop(index,startValue,endValue,body) - body expanded while index<endValue
#
# _forloop(i, 3, 5,i*2 ) -> space needed after i*2, to separate results
#
# _forloop(i, 0, 5,`eval(i*2) ') -> eval (quoted!!!) needed to compute with i
define(`_forloop',dnl
`ifelse(eval($2 < $3), 1,dnl
`pushdef(`$1', `$2')$4`'popdef(`$1')`'_forloop(`$1', incr($2), $3, `$4')',)')dnl

divert(0)dnl

