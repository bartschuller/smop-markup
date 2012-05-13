package org.smop.markup.makros

import reflect.makro.Context
import org.smop.markup.parsing.XmlMarkup

object XmlMacro {
  def xImpl(c: Context)(params: c.Expr[Any]*): c.Expr[Any] = {
    import c.{mirror=>mi}
    import mi._
    val result = c.prefix.tree match {
      case Apply(_, List(Apply(_, parts))) =>
        val stringList = for(Literal(mi.Constant(stringConstant: String)) <- parts) yield stringConstant
        val markup = stringList.mkString(XmlMarkup.placeholderString)
        val ast = XmlMarkup.parse(markup).left.map(sys.error(_)).right.get
        ast
      case x =>
        sys.error("Unexpected tree: " + showRaw(x))
    }
    params foreach { arg =>
      println("argument: "+showRaw(arg.tree) + "type: "+arg.tree.tpe)
    }
    println()
    println(result)
    c.reify("hi")
  }
}
