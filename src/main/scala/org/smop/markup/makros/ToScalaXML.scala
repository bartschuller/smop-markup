package org.smop.markup.makros

import org.smop.markup.ast.MNode
import reflect.makro.Context
import xml._

object ToScalaXML extends XMLBuilder {
  def apply(c: Context)(mNodes: List[MNode], params: c.Expr[Any]*): c.Expr[Any] = {
    c.reify({
      new Elem(null, "foo", Null, TopScope, true)
    })
  }
}
