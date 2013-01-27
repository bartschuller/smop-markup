import org.specs2.mutable._
import org.smop.markup.Markup

class MacroSpec extends Specification {
  "The macros" should {
    val one = 1
    "do basic stuff" in {
      val xobjs = x"""<a href="http://example.org/">Hi there</a>"""
      xobjs === <a href="http://example.org/">Hi there</a>
    }
    "interpolate to string" in {
      val xobjs = x"""<a>foo ${1+one}</a>"""
      // the number of child nodes differs, same thing happens with Scala xml
      xobjs.toString() === <a>foo 2</a>.toString()
    }
    "interpolate to element" in {
      val xobjs = x"""<a>bar ${x"<baz/>"}</a>"""
      xobjs === <a>bar <baz/></a>
    }
    "interpolate attribute values" in {
      val xobjs = x"""<a id=${one} s=${"2"}/>"""
      xobjs === <a id="1" s="2"/>
    }
  }
}
