import language.experimental.macros
package org.smop {

import markup.engines.MarkupEngine
import markup.makros.XmlMacro

package object markup {
  implicit class Markup(sc: StringContext) {
    def x(params: Any*): Any = macro XmlMacro.xImpl
  }
}
}
