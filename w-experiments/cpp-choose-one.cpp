// cpp -P

// http://jhnet.co.uk/articles/cpp_magic
#define IF_ELSE(condition) _IF__( condition )
#define _IF__(c) _IF_ ## c
#define _IF_1(...) __VA_ARGS__ _IF_1_ELSE
#define _IF_0(...)             _IF_0_ELSE

#define _IF_1_ELSE(...)
#define _IF_0_ELSE(...) __VA_ARGS__

// https://mailund.dk/posts/macro-metaprogramming/
#define SECOND(a, b, ...) b
#define TEST(p) SECOND(p, 0)
#define TRUE() -, 1

TEST(TRUE())
TEST(anything)

IF_ELSE( TEST(TRUE()) )(foo)(bar)
IF_ELSE( TEST(x) )(foo)(bar)

#define A TEST(TRUE())
#define B TEST(anything)

IF_ELSE( A )(foo)(bar)
IF_ELSE( B )(foo)(bar)


IF_ELSE( 1 )(foo)(bar)
IF_ELSE( 0 )(foo)(bar)
