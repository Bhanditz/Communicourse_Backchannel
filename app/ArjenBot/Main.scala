import Quiz.QuizActor._
import Quiz.QuizManager._
import Quiz._
import UserGeneratedInfo.UserGeneratedInfoLanguage
import akka.actor.{ActorSystem, Props}
import akka.pattern._
import akka.util.Timeout

import scala.util.Success
//import Quiz.QuizActor._
import Quiz._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by robertMueller on 30.01.17.
  */
object Main extends App{
  implicit val timeout = Timeout(2.seconds)
  /*val quizSystem = ActorSystem("SimpleSystem")
  val quizActor = quizSystem.actorOf(Props[QuizActor], "Quiz-Actor")
  quizActor ! NewQuiz("test-Quiz")
  quizActor ! AddQuizQuestion("test-Quiz", "How big is china?")
  val answers = List(
    (Left(1), "12.000.000 km^2", false), (Left(2), "9.560.000 km^2", true)
  )
  val answers_ = List(
    (Left(3), "Keine Antwort stimmt", false), (Left(4), "10.500.000 km^2", true)
  )
  quizActor ! AddQuizAnswers("test-Quiz", answers_)
  val published = quizActor ? PublishQuiz("test-Quiz")
  published.foreach(p => println(p))
  val addedAnswer = quizActor ? AnswerQuiz("robert", "test-Quiz", Left(2))
  val addedAnswer_ = quizActor ? AnswerQuiz("jindong", "test-Quiz", Left(3))
  val publishedQuizzes = quizActor ? GetPublishedQuizzes
  publishedQuizzes.foreach(q => print(q))

  val eval = quizActor ? EvaluateQuiz("test-Quiz")
  val scoreboard = quizActor ? GetScoreboard
  scoreboard.foreach(println)*/

  /*val manager = QuizManager()
  val qname = "tonald_drump"
  val qb = quizInit flatMap(withName(qname))
  val qm_ = quizInPendingQuizzesLens(qname).set(manager, Some(qb))
  val qm__ = quizInPendingQuizzesLens(qname).mod(qm_)(_.map(_ flatMap(withQuestion("how old is he?"))))
  println(qm__.pendingQuizzes.get(qname).get.run(QuizState())._2)
*/

  /*val myQuiz = quizInit flatMap(withName("plotics")) flatMap(withQuestion("How tall is Angela Merkel?"))
  val quiz = myQuiz.run(QuizState())
  println(quiz._1)*/

  val quizLangParser = new QuizBotLanguage
  val quizLang = quizLangParser.language

  val newQuizCommand = "new quiz testquiz user=robert"
  val makeQuizCommand = "make quiz testquiz-2"
  val addQuestionCommand = "testquiz question How tall is Angela Merkel?"
  val addAnswerCommand = "testquiz add answer B me=robert"
  val publishCommand = "testquiz publish"
  val evaluateCommand = "testquiz eval"
  val quizzesCommand = "get quizzes"
  val scoreboardCommand = "get scoreboard"
  val addQuizAnswers = "testquiz add answers a : \"120cm\" true b : \"130cm\""

  val result = quizLangParser.parse(quizLang, newQuizCommand)
  val result2 = quizLangParser.parse(quizLang, makeQuizCommand)
  val result3 = quizLangParser.parse(quizLang, addQuestionCommand)
  val result4 = quizLangParser.parse(quizLang, addAnswerCommand)
  val result5 = quizLangParser.parse(quizLang, publishCommand)
  val result6 = quizLangParser.parse(quizLang, evaluateCommand)
  val result7 = quizLangParser.parse(quizLang, quizzesCommand)
  val result8 = quizLangParser.parse(quizLang, scoreboardCommand)
  val result8_ = quizLangParser.parse(quizLang, addQuizAnswers)

  println(result)
  println(result2)
  println(result3)
  println(result4)
  println(result5)
  println(result6)
  println(result7)
  println(result8)
  println(result8_)


  val userGeneratedInfoLangParser = new UserGeneratedInfoLanguage
  val userGeneratedInfoLang = userGeneratedInfoLangParser.language

  val addProposalCommand = "create info name=pruefung_scala info=23.02.2017"
  val upvoteCommand = "upvote pruefung_scala user = robert"
  val downvoteCommand = "- pruefung_scala user = robert"
  val deleteCommand = "delete pruefung_scala"

  val result9 = userGeneratedInfoLangParser.parse(userGeneratedInfoLang, addProposalCommand)
  val result10 = userGeneratedInfoLangParser.parse(userGeneratedInfoLang, upvoteCommand)
  val result11 = userGeneratedInfoLangParser.parse(userGeneratedInfoLang, downvoteCommand)
  val result12 = userGeneratedInfoLangParser.parse(userGeneratedInfoLang, deleteCommand)

  println(result9)
  println(result10)
  println(result11)
  println(result12)



}
