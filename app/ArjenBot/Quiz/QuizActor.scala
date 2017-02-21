package Quiz
 import QuizManager._
 import akka.actor.{Actor, Props}
 import Quiz._
 import Types._
 import QuizActor._
 import MyLens._
 import QuizManager._

 import scalaz.State
 import scalaz._


object QuizActor{
  val props = Props[QuizActor]
  /*=====MESSAGES=====*/
  //case class SetTrueAnswer(quizName: String, trueAnswer: Either[Int, Char]) //TODO
  //case class BotInstructionManual
  case class NewQuiz(name: String)
  case class AddQuizQuestion(name: String, question: String)
  case class AddQuizAnswers(name: String, answers: List[Answer])
  case class MissingQuizParameters(missing: List[String])
  case class PublishQuiz(name: String)
  case object PublishQuizSuccess
  case class PublishQuizFailure(reason: PublishErrors)
  case class PublishedQuizzes(publishedQuizzes: Map[String, (Quiz, UserQuizAnswers)])
  case class PendingQuizzes(pendingQuizzes:  Map[String,  State[QuizState, Quiz]])
  case class Scoreboard(scoreboard: Map[String, Int])
  case class EvaluateQuiz(name: String)
  case class QuizEvaluationResults(winners: List[String])
  case class RemoveQuiz(name: String)
  case object GetPendingQuizzes
  case object GetPublishedQuizzes
  case object GetScoreboard
  case object QuizSuccess
  /*=====USERS ANSWER QUIZ=====*/
  case class AnswerQuiz(userName: String, quizName: String, answer: Either[Int, Char])
  /*=====General Errors=====*/
  case object NoSuchQuiz
  case object NoSuchAnswer
}

class QuizActor extends Actor{

  /*=====Queries=====*/
  def getQuizOrErrorMsg[K,V](opt:Option[V]): Option[V] = {
    opt.orElse({
      sender ! QuizActor.NoSuchQuiz
      None
    })
  }
  /*=====THREADSAFE STATE + RECEIVE=====*/
  override def receive = update(QuizManager())

  /*=====STATE UPDATE=====*/
  def update(quizManager: QuizManager): Receive = {

    /*=====CREATING QUIZZES=====*/
    case NewQuiz(name) => {
      val initQuiz = quizInit flatMap(withName(name))
      val updatedManager =  quizInPendingQuizzesLens(name).set(quizManager, Some(initQuiz))
      context become update(updatedManager)
    }

    case AddQuizQuestion(name, question) => {
      val quizWithQuestionLens = quizInPendingQuizzesLens(name)
      getQuizOrErrorMsg (quizWithQuestionLens.get(quizManager))
        .foreach(_ => {
          val updatedManager = quizWithQuestionLens.mod(quizManager)(_.map(_ flatMap(withQuestion(question))))
          sender ! QuizSuccess
          context become update(updatedManager)
      })
    }

    case AddQuizAnswers(name, answers) => {
      val quizWithAnswerLens = quizInPendingQuizzesLens(name)
      getQuizOrErrorMsg (quizWithAnswerLens.get(quizManager))
        .foreach(_ => {
          val updatedManager = quizWithAnswerLens.mod(quizManager)(_.map(_ flatMap(withAnswers(answers))))
          sender ! QuizSuccess
          context become update(updatedManager)
      })
    }
    case PublishQuiz(name: String) => {
      QuizManager.publish(quizManager)(name) match {
          //NoSuchQuiz / UnspecifiedQuizParameter
        case Left(error) => sender ! PublishQuizFailure(error)
        case Right(updatedManager) => {
          sender ! QuizSuccess
          context become update(updatedManager)
        }
      }
    }

    case RemoveQuiz(name:String) => {
      val quizPendingLens = quizInPendingQuizzesLens(name)
      val quizPublishedLens = quizInPublishedQuizzesLens(name)
      if(quizPublishedLens.get(quizManager).nonEmpty) {
        sender ! QuizSuccess
        self ! EvaluateQuiz(name)
      }
      else {
        getQuizOrErrorMsg (quizPendingLens(quizManager))
          .foreach(_=> {
            val updatedManager = quizPendingLens.set(quizManager, None)
            sender ! QuizSuccess
            context become update(updatedManager)
          })
      }

    }

    /*=====GETTING QUIZZES=====*/
    case GetPendingQuizzes => {sender ! PendingQuizzes(quizManager.pendingQuizzes)}
    case GetPublishedQuizzes => {sender ! PublishedQuizzes(quizManager.publishedQuizzes)}
    case GetScoreboard => {sender ! Scoreboard(quizManager.scoreboard)}

    /*=====INTERACT WITH QUIZZES=====*/
      //TODO FEHLER! Warsch. gefixt

    case AnswerQuiz(userName, quizName, usersAnswer) => {
      val publishedQuizzesLens = quizInPublishedQuizzesLens(quizName)
      getQuizOrErrorMsg(publishedQuizzesLens.get(quizManager))
        .foreach(_ => {
          val lens =  userQuizAnswersinPublishedQuizzes(quizName)(userName)
          val updatedManager = lens.set(quizManager, Some(usersAnswer))
          sender ! QuizSuccess
          context become update(updatedManager)
      })
    }

    case EvaluateQuiz(quizName) => {
      getQuizOrErrorMsg(computeWinners(quizManager, quizName)
        .map(wm => {val (winnerList, updatedManager) = wm; sender ! QuizEvaluationResults(winnerList);wm})
        .map((evalWinners _).tupled(_))//pass tuple result to arguments of next fn
      ).foreach(updatedManager => {
        context become  update(updatedManager)
        })
      }
  }
}
