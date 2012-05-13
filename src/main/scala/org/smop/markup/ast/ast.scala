package org.smop.markup.ast

import util.parsing.input.Positional

/**
 * Used for String values that can be interpolated.
 */
sealed trait MString

object MString {
  implicit def string2mString(s: String): MString = MWrappedString(s)
}

object MPlaceholderString extends MString
case class MWrappedString(s: String) extends MString

sealed trait MNode
case class MElement(name: String, attributes: Map[String, MString], placeholderAttributes: Boolean, children: List[MNode]) extends MNode
case class MText(value: String) extends MNode
case object MPlaceholder extends MNode

