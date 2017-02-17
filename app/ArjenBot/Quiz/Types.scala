package Quiz

/**
  * Created by robertMueller on 09.02.17.
  */
object Types {
  type Answer = (Either[Int, Char], String, Boolean)
  type UserQuizAnswers = Map[String, Either[Int, Char]]
}
