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

[![Clojars](https://img.shields.io/clojars/v/uk.axvr/refrain.svg)](https://clojars.org/uk.axvr/refrain)

[tools.deps][]: Add the following to your `deps.edn` file:

```clojure
uk.axvr/refrain {:mvn/version "0.2"}
```

[Leiningen][]: add the following to your `project.clj` file:

```clojure
[uk.axvr/refrain "0.2"]
```

[tools.deps]: https://clojure.org/reference/deps_and_cli
[Leiningen]: https://leiningen.org


## Contributing

Before working on code changes, please note that PRs adding new functions or
macros are unlikely to be accepted.  However, PRs containing fixes to existing
ones, may be merged if they don't cause regressions.

```shell
clojure -X:test       # Run tests on Clojure (JVM)
clojure -M:cljs-test  # Run tests on ClojureScript (JS)
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
