package controllers

import javax.inject.Inject
import javax.inject.Singleton

import play.api.libs.json._
import CommunicationProtocol.{BotInstructions, Message}
import CommunicationProtocol.BotInstructions._
import CommunicationProtocol.ClientMessage
import Quiz.{QuizActor, QuizBotLanguage}
import akka.actor.ActorSystem
import play.api._
import play.api.libs.{EventSource, json}
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsResult, JsString, JsValue, Json}
import play.api.mvc._
import akka.actor._
import akka.util.Timeout

import scala.util.parsing.combinator._
import scala.util.parsing.combinator.JavaTokenParsers
import scala.concurrent.Future
import scala.concurrent.duration._
import CommunicationProtocol._
import play.api.mvc.WebSocket.FrameFormatter


@Singleton
class Application @Inject() (system: ActorSystem) extends Controller {
  import play.api.Play.current
  implicit val timeout = Timeout(2.seconds)
  //val actorSystem = ActorSystem("arjen-bot-system")
  //val quizActor = actorSystem.actorOf(Props[QuizActor], "quiz-actor")
  val chatroomSystem = ActorSystem("chatroom-actor-system")
  val chatroom = chatroomSystem.actorOf(Chatroom.props("room_1"), "chatroom-actor")


  def index = Action { implicit req =>

    Ok(views.html.index(req))
  }

  implicit val messageFormat = Json.format[ClientMessage]
  implicit val messageFrameFormatter = FrameFormatter.jsonFrame[ClientMessage]
  def chatUserSocket = WebSocket.acceptWithActor[ClientMessage, ClientMessage] { request => out =>
    ChatUser.props(out, chatroom)
  }

}