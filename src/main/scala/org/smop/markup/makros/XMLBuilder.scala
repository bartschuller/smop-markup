package org.smop.markup.makros

import reflect.makro.Context
import org.smop.markup.ast.MNode

trait XMLBuilder {
  def apply(c: Context)(mnodes: List[MNode], params: Seq[c.Expr[Any]]): c.Expr[Any]
}
