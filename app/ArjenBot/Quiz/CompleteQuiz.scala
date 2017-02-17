package Quiz

/**
  * Created by robertMueller on 09.02.17.
  */
object CompleteQuiz {
  def unapply(state: QuizState) = {
    state.nameSet && state.questionSet && state.answersSet
  }
}
