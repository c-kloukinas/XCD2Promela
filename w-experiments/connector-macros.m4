define(`CAT', `$1$2')

define(`N', 13)

define(`connXroleYvars', `int m = N; bool ba[eval(N+1)] = 0;')

define(`connXroleYportZmethodQwaits', `m <= N && ba[m]')

define(`connXroleYportZmethodQrequires', `m > 0 || ! ba[m]')

define(`connXroleYportZmethodQensures', `m = (m+1)%N; ba[m]=1;')

connXroleYvars

connXroleYportZmethodQwaits

connXroleYportZmethodQrequires

connXroleYportZmethodQensures

define(context,foo)dnl
define(`callWithParam', `dnl
  pushdef(`context',CAT(defn(`context'),bar))dnl
  define(`varName', CAT(context,$1))dnl
  define(`varName2', context`'$2`'$1)dnl or CAT(CAT(context,$2),$1))
  define(`M', $1)dnl
  define(`mconnXroleYvars', `int m = M; bool ba[eval(M+1)] = 0;')dnl
  define(`mconnXroleYportZmethodQwaits', `m <= M && ba[m]')dnl
  define(`mconnXroleYportZmethodQrequires', `m > 0 || ! ba[m]')dnl
  define(`mconnXroleYportZmethodQensures', `m = (m+1)%M; ba[m]=1;')dnl
  popdef(`context')dnl
')dnl

context
callWithParam(13,test)
varName varName2 context
mconnXroleYvars

mconnXroleYportZmethodQwaits

mconnXroleYportZmethodQrequires

mconnXroleYportZmethodQensures

divert(-1)dnl discard output - hash'ed comments don't copy over
# https://share.google/aimode/GLgloHV60AFgpEv0l "In m4 can I recursively define a macro, so that it contains its previous definitions? And then pop the definitions?" -> "What if I need to implement conditional checks inside the recursion?"
# Recursive macro that appends text until a limit is reached
define(`countdown', `dnl
ifelse(`$1', `0', `Blastoff!', `dnl
$1... pushdef(`countdown', defn(`countdown'))dnl
countdown(decr(`$1'))dnl
')')
divert(0)dnl
countdown(3)

divert(-1)
define(`mystack', `Base Layer')
pushdef(`mystack', `Middle Layer')
pushdef(`mystack', `Top Layer')

# Safely print and pop until the stack is empty
define(`empty_stack', `dnl
ifdef(`mystack', `dnl
Current: mystack
popdef(`mystack')dnl
empty_stack()dnl
', `Stack is fully cleared.
')')
divert(0)dnl
empty_stack()

divert(-1)dnl
define(`includeall', `ifelse(`$#',`0',`',`ifelse(`$1',`',`$1',`include($1) includeall(shift($@))')')')dnl
divert(0)dnl
e includeall
e2 includeall()
b includeall(foo)
c includeall(foo,bar)
