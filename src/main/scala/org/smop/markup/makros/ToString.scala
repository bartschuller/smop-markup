package org.smop.markup.makros

import scala.reflect.macros.Context
import org.smop.markup.ast.MNode

class ToString[C <: Context](val c: C) extends XMLBuilder[C] {
  def apply(mNodes: List[MNode], params: Seq[c.Expr[Any]]): c.Expr[Any] = {
    import c.universe._
    val theString = mNodes.toString()
    c.Expr[String](Literal(Constant(theString)))
  }
}
