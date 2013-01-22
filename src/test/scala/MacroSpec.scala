import org.smop.markup.makros.{ToString, XMLBuilder}
import org.specs2.mutable._

class MacroSpec extends Specification {
  "The macros" should {
    import org.smop.markup.Markup
    "do basic stuff" in {
      import xml._
      //implicit val myBuilder: XMLBuilder = ToString
      val xobjs = x"""<a href="http://example.org/">Hi there</a>."""
      println("xml objects: "+xobjs)
      success
    }
    "interpolate strings" in {
      import xml._
      //implicit val myBuilder: XMLBuilder = ToString
      val one = 1
      val xobjs = x"""<a>${1+one}</a>."""
      println("xml objects: "+xobjs)
      success
    }
  }
}
