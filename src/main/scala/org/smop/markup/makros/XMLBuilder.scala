package org.smop.markup.makros

import scala.reflect.macros.Context
import org.smop.markup.ast.MNode

trait XMLBuilder[C <: Context] {
  val c: C
  def apply(mnodes: List[MNode], params: Seq[c.Expr[Any]]): c.Expr[Any]
}
