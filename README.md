# Noir

A framework for writing clojure websites. Noir is currently being used in production at http://www.typewire.io

## Usage

The best way to get started with noir is by downloading the lein noir plugin:

```bash
lein plugin install lein-noir 0.1.1
lein noir new my-website
cd my-website
lein run
```
If you want to include Noir in an already created leiningen project, simply add this to your dependencies:

```clojure
[noir "0.4.0"]
```

## Docs
[Autodoc](http://ibdknox.github.com/noir/index.html)
[Noir-blog](https://github.com/ibdknox/Noir-blog) - a complete example blog built in Noir.

## Roadmap

* Anotated examples of all public functions
* A beautiful project website :D

## License

Copyright (C) 2011 Chris Granger

Distributed under the Eclipse Public License, the same as Clojure.
