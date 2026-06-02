define(`N', 13)

define(`connXroleYvars', `int m = N; bool ba[eval(N+1)] = 0;')

define(`connXroleYportZmethodQwaits', `m <= N && ba[m]')

define(`connXroleYportZmethodQrequires', `m > 0 || ! ba[m]')

define(`connXroleYportZmethodQensures', `m = (m+1)%N; ba[m]=1;')

connXroleYvars

connXroleYportZmethodQwaits

connXroleYportZmethodQrequires

connXroleYportZmethodQensures

define(`callWithParam', `
  define(`M', $1)
  define(`mconnXroleYvars', `int m = M; bool ba[eval(M+1)] = 0;')
  define(`mconnXroleYportZmethodQwaits', `m <= M && ba[m]')
  define(`mconnXroleYportZmethodQrequires', `m > 0 || ! ba[m]')
  define(`mconnXroleYportZmethodQensures', `m = (m+1)%M; ba[m]=1;')
')

callWithParam(13)

mconnXroleYvars

mconnXroleYportZmethodQwaits

mconnXroleYportZmethodQrequires

mconnXroleYportZmethodQensures
