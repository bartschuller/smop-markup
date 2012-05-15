package org.smop.markup.makros

import org.smop.markup.ast.MNode
import reflect.makro.Context
import xml._

object ToScalaXML extends XMLBuilder {
  def apply(c: Context)(mNodes: List[MNode], params: Seq[c.Expr[Any]]): c.Expr[Any] = {
    import c.mirror._
    if (mNodes.tail.isEmpty) {
      single(c)(mNodes.head, params)
    } else {
      val children = mNodes.toSeq.map(mNode=>single(c)(mNode, params).tree)
      val childExpr = c.Expr[List[Node]](Apply(Select(Select(This(newTypeName("immutable")), newTermName("List")), newTermName("apply")), children))
      c.reify({
        new Group(childExpr.eval)
      })
    }
  }
  
  def single(c: Context)(mNode: MNode, params: Seq[c.Expr[Any]]): c.Expr[Node] = {
    c.reify({
      new Elem(null, "foo", Null, TopScope, true)
    })
  }
}
