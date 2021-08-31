# minimalist-fulcro-template-backendless

A template for starting a new, frontend-only Fulcro application with in-browser Pathom. Intended for playing with and learning Fulcro, not for production apps, and therefore simpler than the official [fulcro-template](https://github.com/fulcrologic/fulcro-template). It is a good starting point for your learning projects that is hopefully simple enough for you to understand.

For a template that has an actual backend, see [minimalist-fulcro-template](https://github.com/holyjak/minimalist-fulcro-template).

## Creating a new application from the template

[Download](FIXME) or clone this repository to your computer and start hacking away.

## Explanation

You will run shadow-cljs, which will watch, compile, and update the sources and also run a HTTP server to serve the application.

## Usage

Prerequisites: Java and [Clojure CLI](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools) installed.

First, install frontend dependencies via npm, yarn, or similar:

    npm install # or yarn install # reportedly yarn < v3

then start the application either via

    clojure -M:serve # or `-M:serve:cider` if you use Emacs
    

or, if you have [Babashka](https://babashka.org/) installed, via

    bb serve # or `serve-emacs`

Finally, navigate to http://localhost:8000 and, _after that_, connect to the shadow-cljs nREPL at port 9001\* and switch to the browser REPL by evaluating `(shadow/repl :main)` (Calva does the latter for you).

\*) In Calva: run _Calva: Connect to a Running REPL Server in tour Project_, select _shadow-cljs_, and confirm the port.

### Create a standalone build

You can also compile the sources into a directory via

    clj -M:build

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