package org.smop.markup.makros

import reflect.makro.Context
import org.smop.markup.parsing.XmlMarkup
import org.smop.markup.ast.MNode

object XmlMacro {
  def xImpl(c: Context)(params: c.Expr[Any]*)(implicitBuilder: c.Expr[XMLBuilder]): c.Expr[Any] = {
    import c.{mirror=>mi}
    import mi._
    val mNodes: List[MNode] = c.prefix.tree match {
      case Apply(_, List(Apply(_, parts))) =>
        val stringList = for(Literal(mi.Constant(stringConstant: String)) <- parts) yield stringConstant
        val markup = stringList.mkString(XmlMarkup.placeholderString)
        XmlMarkup.parse(markup) match {
          case Right(ast) => ast
          case Left(msg) => sys.error(msg)
        }
      case x =>
        sys.error("Unexpected tree: " + showRaw(x))
    }
    params foreach { arg =>
      println("argument: "+showRaw(arg.tree) + "type: "+arg.tree.tpe)
    }

    // How to get our preferred builder?

    println(show(implicitBuilder.tree))
    // Prints "smop.this.markup.`package`.defaultBuilder" when no other implicits in scope, or
    // "myBuilder" when I put
    // implicit val myBuilder: XMLBuilder = ToString
    // in front of my x"" literal.

    // So this is not the way.
    // val builder: XMLBuilder = c.Expr[XMLBuilder](c.resetAllAttrs(implicitBuilder.tree)).eval

    // hard code something for now
    val builder: XMLBuilder = ToScalaXML

    builder(c)(mNodes, params:_*)
  }
}
