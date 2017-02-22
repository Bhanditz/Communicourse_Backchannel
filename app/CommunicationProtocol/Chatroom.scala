package CommunicationProtocol

import CommunicationProtocol.Chatroom.{Broadcast, Join, Leave, Unicast}
import CommunicationProtocol.Protocol._
import Quiz.{QuizActor, QuizBotLanguage}
import UserGeneratedInfo.{UserGeneratedInfoActor, UserGeneratedInfoLanguage}
import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


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
    case cm@ClientMessageWithRole(m,u,_,role) => {

      //FUTURES! - DO THE MESSAGE PASSING INSIDE OF THE FUTURE OR THE ACTOR WILL CLOSE OVER / BLOCK
      Protocol.messageCheck(cm.asClientMessage, new QuizBotLanguage, quizBot, sender()).fold(
        _ => Protocol.messageCheck(cm.asClientMessage, new UserGeneratedInfoLanguage, userGeneratedInfoBot,sender()).fold(
          message => {self ! message}, //normal message, send message to self
          processValueProposalMessages(userGeneratedInfoBot)(sender)(_) foreach(m_ => self ! m_)
        ),{
          quizCommand: Any =>  authorizationCheck(sender, quizCommand, role) match {
            case UnauthorizedQuizCommand(command) => self ! command
            case AuthorizedQuizCommand(command) => processParsedQuizMessages(quizBot)(sender)(command) foreach (m_ => self ! m_)
          }
        }
      )}

    case Broadcast(message) => users.foreach(_ ! message)
    case Unicast(message, sender) => sender ! message
    case Join(actorRef: ActorRef) => users += actorRef; self ! BotInstructions.botUnicast(s"Welcome to $name.",actorRef)
    case Leave(actorRef: ActorRef) => users -= actorRef
  }
}
