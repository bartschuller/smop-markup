smop-markup
===========

Macro-enabled markup support using SIP-11 interpolation.

Say What?

XML strings in Scala source code, parsed at compile time (including compile
failures for wellformedness errors), translated to the object model of
your choice: Scala-XML, Anti-XML, ScalesXML, you name it.

Also: interpolation, with the types checked at compile time.

## Status

- XmlMarkup, the XML + Scala interpolation hybrid, seems finished, both as a design (which XML constructs to support, where to allow interpolation) and implementation.
- The macro needs to be rewritten to use quasiquotes
- I'd really like to have a pluggable object model, but I don't know how to have the equivalent of an implicit value at compile time. Tips welcome.
