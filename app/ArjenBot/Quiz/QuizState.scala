package Quiz

/**
  * Created by robertMueller on 09.02.17.
  */
case class QuizState(
                      val nameSet: Boolean = false,
                      val questionSet: Boolean = false,
                      val answersSet: Boolean = false
                    ) {
  override def toString: String = {
    val sep = System.lineSeparator
    s"name set: ${this.nameSet} $sep question-set: ${this.questionSet} $sep answers set: ${this.answersSet}"
  }
}
