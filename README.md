# Refrain

Collection of small, but useful [Clojure][], [ClojureScript][] and
[ClojureCLR][] functions and macros I've created that I frequently find myself
replicating across programs.

Essentially this library is my own personal version of [Medley](), but you can
use both at the same time as they mostly contain different things.

[Clojure]: https://clojure.org
[ClojureScript]: https://clojurescript.org
[ClojureCLR]: https://clojure.org/about/clojureclr
[Medley]: https://github.com/weavejester/medley


## Installation

### tools.deps

Add the following to your `deps.edn` file:

```clojure
{:deps {uk.axvr/refrain
        {:git/tag "v0.1" :git/sha "008716e"
         :git/url "https://github.com/axvr/refrain.git"}}}
```


### Leiningen

To install Refrain with Leiningen, you will need to use
[lein-git-down](https://github.com/reifyhealth/lein-git-down) as
Refrain is not distributed as a JAR. This is an example `project.clj` file:

```clojure
(defproject my-project "0.1.0"
  :plugins      [[reifyhealth/lein-git-down "0.4.1"]]
  :middleware   [lein-git-down.plugin/inject-properties]
  :repositories [["public-github" {:url "git://github.com"}]]
  :git-down     {uk.axvr/refrain {:coordinates axvr/refrain}}
  :dependencies [[uk.axvr/refrain "008716e9b4be9eb2f96a834096672254c084f6d2"]])
```


## Legal

- Copyright © 2022 Alex Vear.
- Copyright © 2009, 2016 Rich Hickey.

The use and distribution terms for this software are covered by the
[Eclipse Public License 1.0](https://www.eclipse.org/legal/epl-v10.html)
which can be found in the accompanying `LICENCE` file.  By using this software
in any fashion, you are agreeing to be bound by the terms of this license.  You
must not remove this notice, or any other, from this software.

The author is unaware of any patent claims which may affect the use,
modification or distribution of this software.
