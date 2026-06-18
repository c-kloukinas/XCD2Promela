dnl 1. Define the initial _context macro
define(`_context', `start')dnl
dnl
dnl 2. Define the bar macro
define(`inner', dnl
`pushdef(`_context', defn(`_context')`_ininner')dnl
Current `_context' inside `inner': _context
popdef(`_context')dnl')dnl
dnl
dnl --- Test the behavior ---
dnl
Before `inner': _context

inner

After `inner': _context

dnl 1. Define macro foo, which contains the definition for bar
define(`foo', `define(`bar', `This is the content of `bar'')')

dnl 2. Call foo so that bar gets defined
foo()

dnl 3. Use bar outside
bar

define(a,az)
dnl 1. Define macro foo, which contains the definition for bar
define(`foo', `dnl
 define(`bar', `This is the content of `bar2'')dnl
 define(b`'a, `This is the content of `baz'')dnl
')

dnl 2. Call foo so that bar gets defined
foo()

dnl 3. Use bar outside
bar
baz
