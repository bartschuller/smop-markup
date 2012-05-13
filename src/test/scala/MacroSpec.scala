import org.smop.markup.Markup
import org.specs2.mutable._

class MacroSpec extends Specification {
  "The macros" should {
    import org.smop.markup.Markup
    "do basic stuff" in {
      println(x"""<a href="http://example.org/">Hi there</a>""")
      success
    }
  }
}
