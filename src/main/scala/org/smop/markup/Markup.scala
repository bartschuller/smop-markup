import language.experimental.macros
package org.smop {

import markup.engines.MarkupEngine
import markup.makros.{ToScalaXML, XMLBuilder, XmlMacro}

package object markup {
  //implicit val defaultBuilder = ToScalaXML
  implicit class Markup(sc: StringContext) {
    // def x(params: Any*)(implicit implicitBuilder: XMLBuilder): Any = macro XmlMacro.xImpl
    def x(params: Any*): Any = macro XmlMacro.xImpl
  }
}
}
