# Noir

A framework for writing clojure websites. Noir is currently being used in production at https://www.readyforzero.com

Learn more at [Web Noir](http://www.webnoir.org) and see [Pinot](https://github.com/ibdknox/pinot) for its ClojureScript counterpart.

## Usage

The best way to get started with noir is by downloading the lein noir plugin for [leiningen](https://github.com/technomancy/leiningen):

```bash
lein plugin install lein-noir 1.2.1
lein noir new my-website
cd my-website
lein run
```
If you want to include Noir in an already created leiningen project, simply add this to your dependencies:

```clojure
[noir "1.2.1"]
```

## Docs
* [Web Noir](http://www.webnoir.org)

## Roadmap

* Annotated examples of all public functions
* More tutorials

## License

Copyright (C) 2011 Chris Granger

Distributed under the Eclipse Public License, the same as Clojure.
