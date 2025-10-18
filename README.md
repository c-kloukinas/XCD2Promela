# XCD2Promela
A basic compiler from the XCD language to SPIN's Promela language.

# What's XCD?

The XCD language is an ADL (Architectural Description Language), which tries to represent the significant structure of a SW system's architecture.
It maintains that the most important aspect of an architecture are the connectors (i.e., protocols) used in it, not the components so much.

## Components aren't important? Why so?

Think of resistors connected together; what matters is how you've connected them, not the resistors themselves - these are just numbers to plug into an equation that depends on how they were connected.
Or think of complexity analysis of algorithms (=protocols =connectors) - again, the basic result depends little on the actual behaviour of the functional primitives (=operators =components).

## So? Other ADLs have connectors too.

Unlike other ADLs that support connectors (e.g., Wright), XCD designs are always *realisable*. Wright connectors are essentially choreographies - these are a way to describe *requirements* (what one would wish to see played out in the system) aka a *property*, instead of *designs* that should explain how one can meet the requirement/property (without requiring distributed consensus among all parties so that they can agree on the global state - that'd be overkill).

### How's XCD's realisability guaranteed?

XCD is realisable because there is no way to express *shared* state among the components in it. Nor any primitives that depend on shared state (like *synchronous send*). So, if your choreography/property can be met by whatever XCD solution you've come up with you know you have a design - otherwise, you know you don't have one (and don't fool yourself that you do when you don't - a bit like giving someone a drawing of a [Penrose stairway](https://en.wikipedia.org/wiki/Penrose_stairs) and telling them that that's the architecture for an infinite stairway, the builders can get on with it now [^chop-chop]).

## Where can I find more info on XCD?

A web page collecting some info on XCD (original papers, etc.) is [https://www.staff.city.ac.uk/c.kloukinas/Xcd/]

# OK, so what's this project all about?

The purpose of this project is to create a *new* compiler for the translation to the model-checker [Spin's](https://spinroot.com/) ProMeLa language, that is more modular [^easier] than the original compiler and [^easier] to understand. Hopefully once that is achieved it'll be easier to make changes to the language/translation/etc.

[^chop-chop]: chop-chop!

[^easier]: "more modular"/"easier to understand" does *not* mean "modular"/"easy to understand" - I'm sure that the code could be improved.
