package CommunicationProtocol
import akka.pattern.ask
import CommunicationProtocol.Chatroom.{Broadcast, Join, Leave, Unicast}
import Quiz.{QuizActor, QuizBotLanguage}
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import akka.event.LoggingReceive
import akka.util.Timeout
import play.api.libs.json.Json
import BotInstructions._

import scala.concurrent.duration._
import Protocol._
import Quiz.QuizActor.GetPendingQuizzes
import UserGeneratedInfo.{UserGeneratedInfoActor, UserGeneratedInfoLanguage}

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by robertMueller on 15.02.17.
  */

object Chatroom {
  def props(name: String) = Props(new Chatroom(name))
  sealed trait ChatroomMessage
  case class Broadcast(clientMessage: Message) extends ChatroomMessage
  case class Unicast(message: Message, sender:ActorRef) extends ChatroomMessage
  case class Join(ref: ActorRef) extends ChatroomMessage
  case class Leave(ref: ActorRef) extends ChatroomMessage
}
class Chatroom(val name: String) extends Actor {
  implicit val timeout = Timeout(2 seconds)
  val quizBot = context.actorOf(QuizActor.props, "quiz-actor")
  val userGeneratedInfoBot = context.actorOf(UserGeneratedInfoActor.props, "user-generated-info-actor")
  var users: Set[ActorRef] = Set.empty

  override def receive: Receive = {
    case cm@ClientMessage(m,u,_) => {

      //FUTURES! - DO THE MESSAGE PASSING INSIDE OF THE FUTURE OR THE ACTOR WILL CLOSE OVER / BLOCK
      Protocol.messageCheck(cm, new QuizBotLanguage, quizBot, sender()).fold(
        _ => Protocol.messageCheck(cm, new UserGeneratedInfoLanguage, userGeneratedInfoBot,sender()).fold(
          message => {self ! message},
          processValueProposalMessages(userGeneratedInfoBot)(sender())(_) foreach(m_ => self ! m_)
        ),
        processParsedQuizMessages(quizBot)(sender())(_) foreach (m_ => self ! m_)
      )}

    case Broadcast(message) => users.foreach(_ ! message)
    case Unicast(message, sender) => sender ! message
    case Join(actorRef: ActorRef) => users += actorRef
    case Leave(actorRef: ActorRef) => users -= actorRef
  }
}
