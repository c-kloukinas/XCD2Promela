define(`CAT', `$1$2')

define(`foo', a)

define(`bar', b)

define(`foobar', c)

"`foo' `bar'" CAT(`foo', `bar')
"foo `bar'" CAT(foo, `bar')
"`foo' bar" CAT(`foo', bar)
"foo bar" CAT(foo, bar)

define(`fiveOrLess',
       `ifelse(eval($1 <= 5), 1, $1, `eval(fiveOrLess(`eval(decr($1))'))')' )

eval(3<=5)
"f`'iveOrLess(3)" fiveOrLess(3)

eval(10<=5)
"f`'iveOrLess(10)" fiveOrLess(10)

define(`forloopF',
  `ifelse(eval($2 > $3), 1, ,
    `pushdef(`$1', `$2')$4`'popdef(`$1')`'forloopF(`$1', incr($2), $3, `$4')')')
"    make index be 2nd expr, expand 4th expr, don't concatenate with Popdef of
index that leaves it with whatever expr it used to have,
don't concatenate with new application of the macro..."

"f`'orloopF(i, 3, 5, i )" forloopF(i, 3, 5, `i ')

"f`'orloopF(i, 3, 5, i*2 )" forloopF(i, 3, 5, `i*2 ')

"f`'orloopF(i, 3, 5, e`'val(i*2) )" forloopF(i, 3, 5, `eval(i*2) ')

define(`rem', `')
rem( forloop(i, 3, 5, i) )


rem( https://web.mit.edu/gnu/doc/html/m4_5.html )
rem(
define(`forloop',
       `pushdef(`$1', `$2')_forloop(`$1', `$2', `$3', `$4')popdef(`$1')')
define(`_forloop',
       `$4`'ifelse($1, `$3', ,
		   `define(`$1', incr($1))_forloop(`$1', `$2', `$3', `$4')')')
)
