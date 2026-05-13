/*

A connector/composite/role/component have parameters.

connector some_connector (N, role1{port1}, role2{port2} ) {...

These parameters can be used to define the size of arrays, the initial value of their elements or of other variables, etc.

# `byte ar[N+1] := 1+@;`

and later on

# `waits: ar[@] < @;`
# `ensures: ar[@] := N+@+1;`
*/

pushdef(N, 10)

Spin requires that array sizes are a constant, so, the wholeexpression
needs to be evaluated:

`byte ar1[N+1] := N+1+@;' => byte ar1[eval(N+1)] := N+1+@;
`byte ar2[N+2] := N+2;'   => byte ar2[eval(N+2)] := N+2;

For the other expressions however, it's enough to simply expand the
value of the parameter:

`waits  : ar1[@] < N;'        => waits  : ar[@] < N;
`ensures: ar1[@] := N+@+1;'   => ensures: ar[@] := N+@+1;

popdef(N)
