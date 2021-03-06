package CommunicationProtocol

import CommunicationProtocol.Chatroom.{Broadcast, Unicast}
import akka.actor.ActorRef


object BotInstructions {
  val BOTREFERENCE = "hey_"
  val BOTNAME = "arjen"
  val BOTHANDLE = BOTREFERENCE+BOTNAME

  def extractMessageForBotPos(s: String): Int ={
    val i = s.indexOf(BOTHANDLE)
    if (s.contains(BOTHANDLE)) i + BOTHANDLE.length else -1
  }

  def botUnicast(message: String, receiver: ActorRef)= Unicast(PrivateMessage(message, BOTNAME, true), receiver)
  def botBroadcast(message: String) = Broadcast(PublicMessage(message, BOTNAME, true))
}
