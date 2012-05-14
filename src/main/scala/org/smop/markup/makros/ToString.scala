package org.smop.markup.makros

import reflect.makro.Context
import org.smop.markup.ast.MNode

object ToString extends XMLBuilder {
  def apply(c: Context)(mNodes: List[MNode], params: c.Expr[Any]*): c.Expr[Any] = {
    import c.mirror._
    val theString = mNodes.toString()
    c.Expr[String](Literal(Constant(theString)))
  }
}
