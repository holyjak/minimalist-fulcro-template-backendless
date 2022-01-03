# Experiment: Using routing params to select a sub-component to show

1. We route to a particular Person (w/ :person/id) and a particular sub-view (<-> "subroute") of the person's details, leveraging the extra routing params to communicate which sub-route to display
2. Inside Person we have a `(case subroute ...)` to display the sub-view of interest

We do not use a nested router inside Person b/c it does not make sense here. All the sub-views show different parts of the person
and we want to load all their data anyway.

In practice we would parse a URL such as `/page/person/123/details1` into the desired change-route call `(dr/change-route! app ["person" "123"] {:subroute "details1"})`
## Usage

Prerequisites: [same as shadow-cljs'](https://github.com/thheller/shadow-cljs#requirements).

First, install frontend dependencies via npm, yarn, or similar:

    npm install # or yarn install # reportedly yarn < v3

then start the application either via

    npx shadow-cljs watch main

or, if you have [Babashka](https://babashka.org/) installed, via

    bb run

NOTE: For Calva, do instead start the REPL from the editor - [run Jack-in](https://calva.io/connect/#jack-in-let-calva-start-the-repl-for-you), selecting _shadow-cljs_ then the `:main` build. Remember to load the page in the browser, see below.

Finally, navigate to http://localhost:8000 and, _after that_, connect to the shadow-cljs nREPL at port 9001\* and switch to the browser REPL by evaluating `(shadow/repl :main)` (Calva does the latter for you).

## License

Copyleft © 2021 https://holyjak.cz/[Jakub Holý & Holy Dev]

Distributed under the [Unlicense](https://unlicense.org/).