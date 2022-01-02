# minimalist-fulcro-template-backendless

A template for starting a new, frontend-only Fulcro application with in-browser Pathom. Intended for playing with and learning Fulcro, not for production apps, and therefore simpler than the official [fulcro-template](https://github.com/fulcrologic/fulcro-template). It is a good starting point for your learning projects that is hopefully simple enough for you to understand.

~For a template that has an actual backend, see [minimalist-fulcro-template](https://github.com/holyjak/minimalist-fulcro-template).~

## Note on Pathom 3

This template uses Pathom 2 but there is essentially complete rewrite in Pathom 3 at the [pathom3 branch](https://github.com/holyjak/minimalist-fulcro-template-backendless/tree/pathom3).

## Creating a new application from the template

[Download](https://github.com/holyjak/minimalist-fulcro-template-backendless/archive/refs/heads/main.zip) or clone this repository to your computer and start hacking away.

## Explanation

You will run shadow-cljs, which will watch, compile, and update the sources and also run a HTTP server to serve the application.

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

### Create a standalone build

You can also compile the sources into a directory via

    npx shadow-cljs release main
    # or: bb build

and then serve the whole website using a HTTP server, e.g.:

    cd resources/public/
    python3 -m http.server 8000

## Why is this not suitable for production?

No thought was given to security, performance, monitoring, error tracking and other important production concerns. It also bakes in fulcro-troubleshooting, which you do not want unnecessarily increasing your bundle size in production settings. So if you want to use the template as a starting point for a production application, you will need to add those yourself.

## TODO

* Can we display an error in the UI when we remove the `i-fail` resolver from Pathom? Currently it returns `::p/errors ::p/not-found`, which Fulcro ignores

## License

Copyleft © 2021 Jakub Holý

Distributed under the [Unlicense](https://unlicense.org/).
