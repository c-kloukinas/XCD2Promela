#define foo(a) bar(a-1)

#define bar(b) goo(b+1)

#define goo(b) hoo(b*1)

#define hoo(b) hoo(b/1)

foo(3)

bar(4)

// Variadic Macros
#define headless(lst, ...) (lst __VA_OPT__(,) __VA_OPT__( headless2(__VA_ARGS__) ) )

#define headless2(lst, ...) lst __VA_OPT__(,) __VA_OPT__( headless(__VA_ARGS__) )

headless(1, 2, 3, 4)

#undef headless
#undef headless2
#define headless(lst, ...) (lst __VA_OPT__(,) __VA_OPT__( headless2(__VA_ARGS__) ) )

#define headless2(lst, ...) lst __VA_OPT__(,) __VA_OPT__( headless3(__VA_ARGS__) )

#define headless3(lst, ...) lst __VA_OPT__(,) __VA_OPT__( headless(__VA_ARGS__) )

headless(1, 2, 3, 4)

#undef headless
#define headless(...) (__VA_OPT__(strip_head(__VA_ARGS__)) __VA_OPT__(,) __VA_OPT__( strip_rest(__VA_ARGS__) ) )

#define strip_head(head, ...) head

#define strip_rest1(head, ...) __VA_OPT__( headless( __VA_ARGS__ ) )

// #define strip_rest1(head, ...) __VA_OPT__( \
// #define x COUNTER \
// #define headless##x (...) (__VA##_OPT__(strip_head(__VA##_ARGS__)) __VA##_OPT__(,) __VA##_OPT__( strip_rest(__VA##_ARGS__) ) ) \
// headless##x(__VA_ARGS__) )

#define strip_rest(...) __VA_OPT__(strip_rest1(__VA_ARGS__))

headless(1, 2, 3, 4)

// #define less(a, b) \
//   #if a < b \
//     a \
//   #else \
//   b \
//   #endif
#define less(a, b) (a < b) ? a : b

less(2, 7)

#define x y ## __COUNTER__

#define z y ## __COUNTER__

x __COUNTER__
y __COU##NTER__
y ##__COUNTER__
y __COUNTER__##
k##__COUNTER__
k##__COUNTER__
k##__COUNTER__
m__COUNTER__
m__COUNTER__
m__COUNTER__
z __COUNTER__

#define makeAnX(x, c) x##c
#define newX(x) makeAnX(x, __COUNTER__)

newX(x)
newX(x)
newX(y)

/* The m4 preprocessor allows recursive definitions:

   define(`reverse', `ifelse(`$#', `0', ,
                             `$#', `1', ``$1'',
                             `reverse(shift($@)), `$1'')')

   include(`curry.m4')
   include(`stack.m4')
   pushdef(`a', `1')pushdef(`a', `2')pushdef(`a', `3')
   stack_foreach(`a', `:curry(`reverse', `4')')
   curry(`curry', `reverse', `1')(`2')(`3')
     ⇒:1, 4:2, 4:3, 4
     ⇒3, 2, 1

   include(`join.m4')
   join,join(`-'),join(`-', `'),join(`-', `', `')
   joinall,joinall(`-'),joinall(`-', `'),joinall(`-', `', `')
     ⇒,,,
     ⇒,,,-

   ;
*/
