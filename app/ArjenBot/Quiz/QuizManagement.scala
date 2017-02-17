package Quiz
import Types._
import scalaz.{Lens, State}

/**
  * Created by robertMueller on 09.02.17.
  */

object QuizManagement {
  def apply(): QuizManagement ={
    new QuizManagement(
      Map(), Map(), Map()
    )
  }
}

class QuizManagement(var publishedQuizzes: Map[String, (Quiz, UserQuizAnswers)],
                     var pendingQuizzes: Map[String,  State[QuizState, Quiz]],
                     var scoreboard: Map[String, Int]) {

  def getPendingQuiz(key: String): Option[State[QuizState, Quiz]] = {
    pendingQuizzes.get(key)
  }

  def getPublishedQuiz(key: String): Option[(Quiz, UserQuizAnswers)] = {
    publishedQuizzes.get(key)
  }

  def addQuiz(key: String, quiz: State[QuizState, Quiz]) = {
    pendingQuizzes += (key -> quiz)
  }

  def publishQuiz(key: String) = {
    getPendingQuiz(key).foreach(quizState => {
      val (state, quiz) = quizState.run(QuizState())
      state match {
        case completeQuiz @ CompleteQuiz() => {
          /* no answers set yet */
          publishedQuizzes += (quiz.quizName -> (quiz, Map()))
          pendingQuizzes -= quiz.quizName
        }
      }
    })
  }

  def addUserAnswer(userName :String, quizName: String, usersAnswer: Either[Int, Char]): Unit = {
    getPublishedQuiz(quizName)
      .foreach({// wht if user enters an answer that doesnt exist
        case qa @ (quiz, answers) =>  {
          val newQa = (quiz, answers + (userName -> usersAnswer))
          publishedQuizzes += (quizName -> newQa)
        }
    })

  }

  /* helper function */
  def eval(quizWithAnswers: (Quiz, UserQuizAnswers)): List[String] = {
    val (quiz, userAnswers) = quizWithAnswers
    val rightAnswer: List[Either[Int, Char]] = quiz.answers.filterNot({
      /*Partial function*/
      case (_, _, rightOrWrong) => rightOrWrong
    }).map(a => a._1)
    // list of users that were right
    userAnswers.toList
      .filterNot({
        case (username, answer) => rightAnswer.contains(answer)
      }).map(u => u._1)
  }

  def evaluateQuiz(key: String): Option[List[String]] = {
    val quiz = getPublishedQuiz(key)
    val winners = quiz.map(this.eval)
    winners.foreach(winnerList => {
        for(winner <- winnerList){
          scoreboard.get(winner) match {
            case Some(score) => scoreboard += (winner -> (score+1))
            case None => scoreboard += (winner -> 1)
          }
        }
      })
    publishedQuizzes -= key // None if non existent
    winners
  }

}
