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
import play.api.mvc.WebSocket.FrameFormatter
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class Application @Inject() (system: ActorSystem) extends Controller {

  def login = Action {implicit req =>
    Ok(views.html.login())
  }

  def chat(username: String) = Action { implicit req =>
    Ok(views.html.chat(req, username)) //real user?
  }
  def register = Action {implicit req=>
    Ok(views.html.register(models.UserFormData.form))
  }
  /*def login = Action {implicit req=>
    Ok(views.html.login(models.UserFormData.form))
  }*/

  def loginSubmission = Action.async {implicit req =>
    models.LoginUserFormData.form.bindFromRequest.fold(
      formWithErrors => Future(BadRequest),
      userLoginData =>  {
        UserService.checkUser(userLoginData.userName, userLoginData.password).map(_.isEmpty).map(b => {
          b match {
            case true => Unauthorized(views.html.login())
            case false => Ok(views.html.chat(req, username = userLoginData.userName))
          }
        })
      }
    )
  }



  def registerSubmission = Action.async {implicit req =>
    models.UserFormData.form.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.register(models.UserFormData.form))) //or redirect // clientside error
        //((Future.successful(Redirect(routes.Application.chat("guest")).withSession({"username"-> "guest"}))
      },
      userData => {
        //could be improved qith case object
        val role_ = if(userData.role.toLowerCase == "teacher") "teacher" else "student"
        val newUser = models.User(0,role_,userData.userName, userData.password, userData.email)
        UserService.addUser(newUser).map(res => { //TODO verbessern
          Redirect(routes.Application.chat(newUser.userName)).withSession({"username"->newUser.userName})
        })

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