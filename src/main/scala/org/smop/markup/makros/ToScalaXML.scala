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
    c.Expr[xml.NodeSeq](tree)
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
      case MPlaceholder => (params.head.tree, params.tail)
    }
  }

  def attribute(name: String, value: MString, params: List[c.Expr[Any]]): (c.Tree, List[c.Expr[Any]]) = {
    import c.universe._
    val (valTree, paramsLeft) = value match {
      case MWrappedString(s) => (Literal(Constant(s)), params)
      case MPlaceholderString => {
        // c.info(c.enclosingPosition, params.head.actualType.typeSymbol.toString, true)
        (params.head.tree, params.tail)
      }
    }

    val valExp = c.Expr[String](
      if (value == MPlaceholderString && params.head.actualType.typeSymbol.toString != "class String")
        Apply(Select(valTree, newTermName("toString")), List())
      else valTree
    )
    val mdRef = c.Expr[xml.MetaData](Ident(newTermName("$md")))
    val Some(List(prefix, base)) = "(?:([^:]+):)?(.*)".r.unapplySeq(name)
    val prefixExpr = c.Expr[String](Literal(Constant(prefix)))
    val baseExpr = c.Expr[String](Literal(Constant(base)))
    ((if (prefix == null)
      reify(new xml.UnprefixedAttribute(baseExpr.splice, valExp.splice, mdRef.splice))
    else
      reify(new xml.PrefixedAttribute(prefixExpr.splice, baseExpr.splice, valExp.splice, mdRef.splice))).tree,
    paramsLeft)
  }

  def element(el: MElement, params: List[c.Expr[Any]]): (c.Tree, List[c.Expr[Any]]) = {
    import c.universe._

    val label = Literal(Constant(el.name))
    val minimizeEmpty = Literal(Constant(el.isEmpty))
    val Block(List(varMd), _) = reify{ var $md: xml.MetaData = xml.Null }.tree
    val (mdAdd, childParams) = el.attributes.foldLeft((Seq.empty[c.Tree], params)) { case ((result, p), (attrName, attrVal)) =>
      val (expr, newParams) = attribute(attrName, attrVal, p)
      (result :+ Assign(Ident(newTermName("$md")), expr), newParams)
    }

    val attributes =
      if (el.attributes.isEmpty)
        reify(xml.Null).tree
      else
        Block(
          Seq(varMd) ++
          mdAdd :+
          Ident(newTermName("$md"))
          :_*
        )
    val (substituted, paramsLeft) = el.children.foldLeft((Seq.empty[c.Tree], childParams)) { case ((result, p), mChild) =>
      val (expr, newParams) = single(mChild, p)
      (result :+ Apply(Select(Ident(newTermName("$c")), newTermName("$amp$plus")), List(expr)), newParams)
    }
    val Block(List(valC), _) = reify{ val $c = new xml.NodeBuffer }.tree
    val children =
      Block(
        Seq(valC) ++
        substituted :+
        Ident(newTermName("$c"))
        :_*
      )

    val tree =
      Apply(
        Select(
          New(
            Select(Select(Ident("scala"), newTermName("xml")), newTypeName("Elem"))
          ), nme.CONSTRUCTOR),
        List(
          Literal(Constant(null)),
          label,
          attributes,
          reify(xml.TopScope).tree,
          minimizeEmpty,
          Typed(children, Ident(tpnme.WILDCARD_STAR))
        )
      )
    (tree, paramsLeft)
  }

  def text(txt: MText): c.Tree = {
    import c.universe._
    val value = c.Expr[String](Literal(Constant(txt.value)))
    reify(new xml.Text(value.splice)).tree
  }
}
