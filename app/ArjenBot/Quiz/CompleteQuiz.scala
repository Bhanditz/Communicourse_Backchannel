package Quiz


object CompleteQuiz {
  def unapply(state: QuizState) = {
    state.nameSet && state.questionSet && state.answersSet
  }
}
