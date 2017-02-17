package Quiz
import Traits.BotLanguage

import scala.util
import scala.util.parsing.combinator._
import scala.util.parsing.combinator.JavaTokenParsers
import Types._

/**
  * Created by robertMueller on 13.02.17.
  */

class QuizBotLanguage extends BotLanguage{

  /*=====ACTION MAPPINGS=====*/
  import QuizActor.{NewQuiz, AnswerQuiz, AddQuizQuestion,
  AddQuizAnswers, PublishQuiz, EvaluateQuiz,
  GetPublishedQuizzes, GetScoreboard, RemoveQuiz,GetPendingQuizzes}

  /*=====PRODUCTION RULES=====*/
  def language = newQuiz|addQuizQuestion|addQuizAnswers|answerQuiz|publishQuiz|evaluateQuiz|getPublishedQuizzes|getScoreboard|removeQuiz|getPendingQuizzes

  def newQuiz = ("new quiz"|"make quiz"|"create quiz") ~> word ^^ {quizName => NewQuiz(quizName)}

  def addQuizQuestion = word ~ opt("with" | "add") ~ "question" ~ opt("=") ~ quotedString <~ opt(withUser) ^^ {
    case (quizName ~ _ ~ _ ~_ ~ question) => AddQuizQuestion(quizName, question) }

  def addQuizAnswers =  word ~ opt("with" | "add") ~ "answers" ~ opt("=") ~  quizAnswers <~ opt(withUser) ^^ {
    case (quizName ~ _~_~_ ~ answers) => AddQuizAnswers(quizName, answers)
  }

  def quizAnswers: Parser[List[Answer]] = rep((charOrInt ~ opt(":") ~ quotedString  ~  opt(boolean))) <~ opt(withUser) ^^ { list =>
    list.map({
      case (ci ~ _ ~ answer ~  Some(b)) => (ci, answer, b)
      case (ci ~ _  ~ answer ~  None) => (ci, answer, false)
    })
  }

  def answerQuiz = word ~ opt("with" | "add") ~ "answer" ~ charOrInt ~ withUser ^^ {
    case (quizName~_~_~usersAnswer ~ userName) => AnswerQuiz(userName, quizName, usersAnswer)
  }

  def publishQuiz = word <~ ("publish"|"pub") ^^ {quizName => PublishQuiz(quizName)}

  def evaluateQuiz =  word <~ ("eval"|"evaluate"|"remove"|"kill") ^^ {quizName => EvaluateQuiz(quizName)}

  def removeQuiz = word <~  ("kill"|"remove"|"delete")  ^^ {quizName => RemoveQuiz(quizName)}

  def getPublishedQuizzes = opt("get") ~> ("quizzes"|"published quizzes") ~> opt(withUser) ^^  {_=> GetPublishedQuizzes}
  def getPendingQuizzes = opt("get") ~> ("pending quizzes"|"pending") ~> opt(withUser) ^^ {_ => GetPendingQuizzes}
  def getScoreboard = opt("get") ~> ("scoreboard"|"scoreboard"|"scores"|"score") <~ opt(withUser) ^^ {_=> GetScoreboard}

  def charOrInt: Parser[Either[Int, Char]] = integer ^^ {i => Left(i)}|character ^^ {c => Right(c)}
}


