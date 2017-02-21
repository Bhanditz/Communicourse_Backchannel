package Quiz

import scalaz.State
import Types._


case class Quiz(
                val quizName: String = "",
                val question: String = "",
                val answers: List[Answer] = Nil
               ){
  override def toString() = {
    val sep = System.lineSeparator
    val bar = "="*15
    s"$sep $bar $sep Quiz-name ${this.quizName} $sep Question: ${this.question} $sep $bar $sep ${this.answers.map(a=>{
      val (choice, answer, bool) = a
      s"${choice.fold(_.toString, _.toString)}: $answer"
    }).mkString(sep) + sep}"
  }
}

object Quiz {
  // call with QuizState()
  // "Absorb" the A's and then build a new function that only depends on state S
  val quizInit: State[QuizState, Quiz] = State {s: QuizState => (s, Quiz())}

  def withName(name: String)(q:Quiz): State[QuizState, Quiz] = State[QuizState, Quiz]{
    s: QuizState => (s.copy(nameSet = true), q.copy(quizName = name))
  }

  def withQuestion(question: String)(q: Quiz): State[QuizState, Quiz] =  State[QuizState, Quiz]{
       s: QuizState => (s.copy(questionSet = true), q.copy(question = question))
  }

  def withAnswers(quizAnswers: List[Answer])(q: Quiz): State[QuizState, Quiz] = State[QuizState, Quiz]{
      s: QuizState => (s.copy(answersSet = true), q.copy(answers = q.answers ::: quizAnswers ))
  }

}