package org.smop.markup.makros

import reflect.makro.Context
import xml._
import org.smop.markup.ast.{MElement, MNode}

object ToScalaXML extends XMLBuilder {
  def apply(c: Context)(mNodes: List[MNode], paramsSeq: Seq[c.Expr[Any]]): c.Expr[Any] = {
    val params = paramsSeq.toList
    import c.mirror._
    val tree = if (mNodes.tail.isEmpty)
        single(c)(mNodes.head, params)
      else {
        val children = mNodes.map(mNode=>single(c)(mNode, params))
        group(c)(mNodes, params)
      }
  }

  def group(c: Context)(mNodes: List[MNode], params: Seq[c.Expr[Any]]): List[c.Tree] = {
    import c.mirror._
    if (mNodes.tail.isEmpty) {
      single(c)(mNodes.head, params)
    } else {
      val children = mNodes.map(mNode=>single(c)(mNode, params).tree)
      Apply(
        Select(
          New(
            Ident(
              newTypeName("Group")
            )
          ),
          newTermName("<init>")
        ),
        List(
          Apply(
            Select(
              Ident(
                newTermName("Seq")
              ),
              newTermName("apply")
            ),
            children
          )
        )
      )
    }
  }

  def single(c: Context)(mNode: MNode, params: Seq[c.Expr[Any]]): c.Tree = {
    mNode match {
      case el: MElement => element(c)(el, params)
    }
  }

  def element (c: Context)(el: MElement, params: Seq[c.Expr[Any]]): c.Tree = {
    import c.mirror._
    Apply(
      Select(
        New(
          Ident(
            newTypeName("Elem")
          )
        ),
        newTermName("<init>")
      ),
      List(
        Literal(Constant(null)),
        Literal(Constant(el.name)),
        Select(Select(Ident(newTermName("scala")), newTermName("xml")), newTermName("Null")),
        Select(Select(Ident(newTermName("scala")), newTermName("xml")), newTermName("TopScope")),
        Literal(Constant(el.isEmpty)),
        Apply(
          Select(
            Ident(
              newTermName("Seq")
            ),
            newTermName("apply")
          ),
          apply(c)(el.children, params)
        )
      )
    )
  }
}
