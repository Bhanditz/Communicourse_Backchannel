package CommunicationProtocol

import java.security.PrivilegedExceptionAction

import play.api.i18n.Messages.Message

/**
  * Created by robertMueller on 14.02.17.
  */
object BotInstructions {
  val BOTREFERENCE = "hey_"
  val BOTNAME = "arjen"
  val BOTHANDLE = BOTREFERENCE+BOTNAME

  def extractMessageForBotPos(s: String): Int ={
    val i = s.indexOf(BOTHANDLE)
    if (s.contains(BOTHANDLE)) i + BOTHANDLE.length else -1
  }

  def botMessage(mesage: String) = PrivateMessage(mesage, BOTNAME, true)
  val generalJsonErrorMessage = botMessage("Your JSON is invalid")

}
