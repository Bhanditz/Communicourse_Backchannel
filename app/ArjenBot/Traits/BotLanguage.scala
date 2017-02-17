package Traits

/**
  * Created by robertMueller on 14.02.17.
  */
import scala.util.parsing.combinator._
trait BotLanguage extends RegexParsers{
  def language: Parser[Any]
  def word: Parser[String]    = """[a-zA-Z1-9_:-]+""".r ^^ { _.toString }
  def quotedString = """"([^"]*)"""".r ^^ {s=> s.toString}
  def anyString: Parser[String] = """.*""".r ^^ {_.toString}
  def user = ("me"|"username"|"user")
  def withUser: Parser[String] = user ~> opt("=") ~> word
  def character: Parser[Char] = """[a-zA-Z]""".r ^^ {_.charAt(0)}
  def integer: Parser[Int] =  """\d+""".r ^^ { _.toInt}
  def boolean: Parser[Boolean] = bTrue1|bTrue2|bTrue3|bFalse1|bFalse2|bFalse3
  def bTrue1: Parser[Boolean] = "true" ^^ {_ => true}
  def bTrue2: Parser[Boolean] = "True" ^^ {_ => true}
  def bTrue3: Parser[Boolean] = "TRUE" ^^ {_ => true}
  def bFalse1: Parser[Boolean] = "false" ^^ {_ => false}
  def bFalse2: Parser[Boolean] = "False" ^^ {_ => false}
  def bFalse3: Parser[Boolean] = "FALSE" ^^ {_ => false}
}
