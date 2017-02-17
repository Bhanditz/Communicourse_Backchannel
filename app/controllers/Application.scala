package controllers

import javax.inject.Inject
import javax.inject.Singleton

import play.api.libs.json._
import CommunicationProtocol.Message
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

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import scala.concurrent.duration._
import CommunicationProtocol._
import models.{Student, Teacher}
import play.api.mvc.WebSocket.FrameFormatter


@Singleton
class Application @Inject() (system: ActorSystem) extends Controller {



  def index = Action { implicit req =>
    Ok(views.html.index(req))
  }
  def register = Action {implicit req=>
    Ok(views.html.register(models.UserFormData.form))
  }
  def login = Action {implicit req=>
    Ok(views.html.login(models.UserFormData.form))
  }

  def registerSubmission = Action {implicit req =>
    models.UserFormData.form.bindFromRequest.fold(
      formWithErrors => {
        BadRequest //or redirect // clientside error
        Redirect(routes.Application.index()).withSession({"username"-> "guest"})
      },
      userData => {
        val role_ = if(userData.role.toLowerCase == "teacher") Teacher else Student
        val newUser = models.User(role_,userData.userName, userData.password, userData.email)
        //databse! //session
        Redirect(routes.Application.index()).withSession({"username"->newUser.userName})
      }
    )
  }

  /*=====BOT SETUP=====*/
  import play.api.Play.current
  implicit val timeout = Timeout(2.seconds)
  //val actorSystem = ActorSystem("arjen-bot-system")
  //val quizActor = actorSystem.actorOf(Props[QuizActor], "quiz-actor")
  val chatroomSystem = ActorSystem("chatroom-actor-system")
  val chatroom = chatroomSystem.actorOf(Chatroom.props("room_1"), "chatroom-actor")

  implicit val messageFormat = Json.format[ClientMessage]
  implicit val messageFrameFormatter = FrameFormatter.jsonFrame[ClientMessage]
  def chatUserSocket = WebSocket.acceptWithActor[ClientMessage, ClientMessage] { request => out =>
    ChatUser.props(out, chatroom)
  }

}