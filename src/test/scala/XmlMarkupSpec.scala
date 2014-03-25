import org.smop.markup.parsing.XmlMarkup
import org.specs2.matcher.Matcher
import org.specs2.mutable._
import org.smop.markup.ast._

class XmlMarkupSpec extends Specification {
  "The XmlMarkup parser" should {
    def beParsedAs(l: List[MNode]): Matcher[Either[String,List[MNode]]] = beRight(===(l))
    import XmlMarkup.parse
    val ph = XmlMarkup.placeholderString
    "parse a simple String to Text" in {
      parse("foo") should beParsedAs(List(MText("foo")))
      parse("foo and bar, sitting in a tree©") should beParsedAs(List(MText("foo and bar, sitting in a tree©")))
      parse(">") should beParsedAs(List(MText(">")))
      parse("\u0007") should beParsedAs(List(MText("\u0007")))
    }
    "reject markup/invalid characters" in {
      parse("<") should beLeft
      parse("&") should beLeft
    }
    "accept character references" in {
      parse("&#x20ac;") should beParsedAs(List(MText("€")))
      parse("&#38;") should beParsedAs(List(MText("&")))
      parse("-&#1;-") should beParsedAs(List(MText("-\u0001-")))
      parse("&#x0007;") should beParsedAs(List(MText("\u0007")))
    }
    "accept the 5 standard entity references" in {
      parse("&amp;&lt;&gt;&apos;&quot;") should beParsedAs(List(MText("&<>'\"")))
    }
    "parse empty elements" in {
      parse("<foo/>") should beParsedAs(List(MElement("foo", Seq.empty, false, true, Nil)))
      parse("<foo/><bar/>") should beParsedAs(List(MElement("foo", Seq.empty, false, true, Nil), MElement("bar", Seq.empty, false, true, Nil)))
      parse("<foo></foo>") should beParsedAs(List(MElement("foo", Seq.empty, false, false, Nil)))
    }
    "parse nonempty elements" in {
      parse("<a><b/></a>") should beParsedAs(List(MElement("a", Seq.empty, false, false, List(MElement("b", Seq.empty, false, true, Nil)))))
    }
    "parse placeholders in mixed content" in {
      parse(s"<a/>.$ph") should beParsedAs(List(MElement("a", Seq.empty, false, true, Nil), MText("."), MPlaceholder))
    }
    "parse attributes" in {
      parse("""<a a="a a"/>""") should beParsedAs(List(MElement("a", Seq("a"->"a a"), false, true, Nil)))
      parse("""<a a='a"&apos; a' >a</a>""") should beParsedAs(List(MElement("a", Seq("a"->"a\"' a"), false, false, List(MText("a")))))
      parse("""<a a=">" b=''/>""") should beParsedAs(List(MElement("a", Seq("a"->">", "b"->""), false, true, Nil)))
      parse("""<a a="<"/>""") should beLeft
    }
    "parse placeholders for attribute values" in {
      parse(s"""<a a=$ph />""") should beParsedAs(List(MElement("a", Seq("a"->MPlaceholderString), false, true, Nil)))
    }
    "expand custom entity sets" in {
      val myEntities = XmlMarkup.xml5StandardEntities ++ Map("euro"->"€")
      parse("&amp;&euro;", myEntities) should beParsedAs(List(MText("&€")))
    }
  }
}
