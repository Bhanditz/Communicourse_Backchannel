package controllers

import javax.inject.{Inject, Singleton}

import CommunicationProtocol.{ChatUser, Chatroom, ClientMessage}
import akka.actor.ActorSystem
import akka.util.Timeout
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller, WebSocket}
import play.api.mvc.WebSocket.FrameFormatter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
/**
  * Created by robertMueller on 18.02.17.
  */

class ChatroomController @Inject() (system: ActorSystem, chatroomSystem: ActorSystem, name: String) extends Controller {

  /*=====BOT SETUP=====*/
  import play.api.Play.current
  implicit val timeout = Timeout(2.seconds)
  //val actorSystem = ActorSystem("arjen-bot-system")
  //val quizActor = actorSystem.actorOf(Props[QuizActor], "quiz-actor")
  val chatroom = chatroomSystem.actorOf(Chatroom.props(name), ("chatroom-actor"+name).replace(" ", "-"))

  implicit val messageFormat = Json.format[ClientMessage]
  implicit val messageFrameFormatter = FrameFormatter.jsonFrame[ClientMessage]
  def chatUserSocket = WebSocket.acceptWithActor[ClientMessage, ClientMessage] { request => out =>
    val role = request.session.get("role").getOrElse("student")
    ChatUser.props(out, chatroom, role)
  }
}
