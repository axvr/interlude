# Refrain

Collection of small, but useful Clojure, ClojureScript and ClojureCLR functions
and macros I've created that I frequently find myself replicating across
programs.

Essentially this library is my own personal version of
[Medley](https://github.com/weavejester/medley), but you can use both at the
same time as they mostly contain different things.


## Installation

> **Warning**
> Refrain is still a work-in-progress.  Until it reaches v1.0, expect backwards
> incompatible changes.

Add the following to your `deps.edn` file:

```clojure
{:deps {uk.axvr/refrain
        {:git/sha "d005de46b689c7bacf9bd59b6c68b0d8bcb93e74"
         :git/url "https://github.com/axvr/refrain.git"}}}
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
