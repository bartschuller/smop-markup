package org.smop.markup.makros

import scala.reflect.macros.Context
import org.smop.markup.ast._

class ToScalaXML[C <: Context](val c: C) extends XMLBuilder[C] {
  def apply(mNodes: List[MNode], paramsSeq: Seq[c.Expr[Any]]): c.Expr[Any] = {
    val params = paramsSeq.toList
    import c.universe._
    val (tree, _) = //if (mNodes.tail.isEmpty)
        single(mNodes.head, params)
      //else {
        //val children = mNodes.map(mNode=>single(mNode, params))
        //group(mNodes, params)
      //}
    c.Expr(tree)
  }

  def group(mNodes: List[MNode], params: Seq[c.Expr[Any]]): List[c.Tree] = {
//    import c.universe._
//    if (mNodes.tail.isEmpty) {
//      single(mNodes.head, params)
//    } else {
//      val children = mNodes.map(mNode=>single(mNode, params).tree)
//      Apply(
//        Select(
//          New(
//            Ident(
//              newTypeName("Group")
//            )
//          ),
//          newTermName("<init>")
//        ),
//        List(
//          Apply(
//            Select(
//              Ident(
//                newTermName("Seq")
//              ),
//              newTermName("apply")
//            ),
//            children
//          )
//        )
//      )
//    }
    ???
  }

  def single(mNode: MNode, params: List[c.Expr[Any]]): (c.Tree, List[c.Expr[Any]]) = {
    mNode match {
      case el: MElement => element(el, params)
      case txt: MText => (text(txt), params)
      case MPlaceholder => placeholder(params)
    }
  }

  def element(el: MElement, params: List[c.Expr[Any]]): (c.Tree, List[c.Expr[Any]]) = {
    import c.universe._

    val label = Literal(Constant(el.name))
    val minimizeEmpty = Literal(Constant(el.isEmpty))
    val children = el.children.map(c=>single(c, params)._1)
    val newParams = params // FIXME the above needs to be a fold or something that gives us the params left
    val tree =
      Apply(
        Select(
          New(
            Select(Select(Ident("scala"), newTermName("xml")), newTypeName("Elem"))
          ), nme.CONSTRUCTOR),
        List(
          Literal(Constant(null)),
          label,
          Select(Select(Ident("scala"), newTermName("xml")), newTermName("Null")),
          Select(Select(Ident("scala"), newTermName("xml")), newTermName("TopScope")),
          minimizeEmpty
        ) ++ children
      )
    (tree, newParams)
  }

  def text(txt: MText): c.Tree = {
    import c.universe._
    Apply(
          Select(
            New(
              Select(Select(Ident("scala"), newTermName("xml")), newTypeName("Text"))
            ), nme.CONSTRUCTOR),
          List(
            Literal(Constant(txt.value))
          )
        )
  }

  def placeholder(params: List[c.Expr[Any]]): (c.Tree, List[c.Expr[Any]]) = {
    import c.universe._
    val tree = Apply(
              Select(
                New(
                  Select(Select(Ident("scala"), newTermName("xml")), newTypeName("Text"))
                ), nme.CONSTRUCTOR),
              List(
                Apply(Select(params.head.tree, newTermName("toString")), List())
                  ))
    c.info(c.enclosingPosition, showRaw(tree, true), force=false)
    (tree, params.tail)
  }
}
