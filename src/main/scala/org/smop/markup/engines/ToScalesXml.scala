package org.smop.markup.engines

import org.smop.markup.ast.MNode

object ToScalesXml {
  implicit class ToScalesXmlWrapper(sc: StringContext) {
    def x(params: Any*)(implicit implicitBuilder: XMLBuilder): Any = {}
  }

  def apply(mNodes: List[MNode]) = {

  }
}
