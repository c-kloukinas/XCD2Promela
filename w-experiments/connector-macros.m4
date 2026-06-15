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
