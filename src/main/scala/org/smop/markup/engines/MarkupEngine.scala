package org.smop.markup.engines

trait MarkupEngine {
  def expandEntities: Boolean
}

case class ScalaXmlEngine(expandEntities: Boolean=true) extends MarkupEngine
