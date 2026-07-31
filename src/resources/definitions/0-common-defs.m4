dnl -*- mode: m4 -*-
divert(-1)dnl discard output - hash'ed comments don't copy over
dnl collect all typedefs that need to be done globally
define(__GLOBAL_TYPEDEFS,dnl UNDEFINED

)
define(__GLOBAL_VARDECLS,dnl UNDEFINED
)
define(__GLOBAL_INITS,dnl UNDEFINED
)

define(`_warningM',dnl
`errprint(ifdef(`__program__', `__program__', ``m4'')'dnl
`:ifelse(__line__, `0', `',dnl
`__file__:__line__:')` $1: 'shift($@)`
')')dnl
define(`_warning',dnl
`_warningM(warning,$*)')dnl
define(`_error',dnl
`_warningM(fatal error,$*)m4exit(`1')')dnl
dnl
define(`_CAT', `$1$2')dnl
define(`_NAME', `ifelse(`$#',`0',`',`ifelse(`$#',`1',`$1',`$1_`'_NAME(shift($@))')')')dnl
define(`_EVALNAME', `_CAT(_NAME($@))')dnl
define(`_ite', `ifelse(eval($1),0,`$3',`$2')')dnl
define(`_ilog2r',`ifelse($1,1,$2,`_log2r(eval($1/2),incr($2))')')dnl
define(`_ilog2',`ifelse(eval($1>0),0,`errprint(log2 is only defined on positive integers
)',`_log2r($1,0)')')dnl

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

