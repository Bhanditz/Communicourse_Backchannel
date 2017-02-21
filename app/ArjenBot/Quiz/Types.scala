package Quiz


object Types {
  type Answer = (Either[Int, Char], String, Boolean)
  type UserQuizAnswers = Map[String, Either[Int, Char]]
}
