package CommunicationProtocol

import CommunicationProtocol.Chatroom.{Broadcast, Join, Leave, Unicast}
import Quiz.{QuizActor, QuizBotLanguage}
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import akka.event.LoggingReceive
import akka.util.Timeout
import play.api.libs.json.Json
import BotInstructions._
import scala.concurrent.duration._

/**
  * Created by robertMueller on 15.02.17.
  */

object Chatroom {
  def props(name: String) = Props(new Chatroom(name))
  case class Broadcast(clientMessage: ClientMessage)
  case class Unicast(message: Message, sender:ActorRef)
  case class Join(ref: ActorRef)
  case class Leave(ref: ActorRef)
}
class Chatroom(val name: String) extends Actor {
  implicit val timeout = Timeout(2 seconds)
  val quizBot = context.actorOf(QuizActor.props, "quiz-actor")
  var users: Set[ActorRef] = Set.empty

  override def receive: Receive = {
    case cm@ClientMessage(m,u,_) => {
      //magic to come
      val botCommandPosition = extractMessageForBotPos(m)
      if(botCommandPosition >= 0){
        val quizLangParser = new QuizBotLanguage
        val quizLang = quizLangParser.language
        val command =  m.drop(botCommandPosition).take(128)
        command.isEmpty match {
          case true => {
            sender ! botMessage("You did not give me any instructions :-(" )
          }
          case _ => {
            val result = quizLangParser.parse(quizLang, command)
            result.successful match {
              case true => sender ! botMessage(result.toString)
              case _ =>  sender ! botMessage("you gave me an instruction i could not understand understand :-(")
            }
          }
        }
      }else {
        self ! Broadcast(cm)
      }
    }
    case Broadcast(ClientMessage(m,u,b)) => users.foreach(_ ! PublicMessage(m,u,b))
    case Unicast(message, sender) => sender ! message
    case Join(actorRef: ActorRef) => users += actorRef
    case Leave(actorRef: ActorRef) => users -= actorRef
  }
}
