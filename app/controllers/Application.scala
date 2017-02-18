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

  /*=====Chatroom Management=====*/
  val chatroomSystem = ActorSystem("chatroom-actor-system")
  val defaultRoomName = "CommuniRoom"
  val defaultRoom = new ChatroomController(system, chatroomSystem, defaultRoomName)
  var chatrooms = Map((defaultRoomName -> defaultRoom))
  def newChatroom(name: String) = chatrooms += (name -> new ChatroomController(system, chatroomSystem, name))
  def getChatroom(name: String) = chatrooms.get(name).getOrElse(defaultRoom).chatUserSocket //TODO arjen soll was sagen

  def chatHttp(implicit req: RequestHeader, username: String, chatroomName: String = defaultRoomName) =
    Ok(views.html.chat(req, username, chatroomName, chatrooms.keys))

  def chat(username: String, chatroomName: String) = Action {
    implicit req =>
    chatHttp(req, username, chatroomName)//real user?
  }

  def createChatroom = Action {
    implicit req =>
      models.NewChatroomForm.form.bindFromRequest.fold(
        formWithErrors => BadRequest,
        data => {
          newChatroom(data.name)
          chatHttp(req, req.session.get("username").get, data.name)
        }
      )
  }


  /*=====Authorization=====*/
  def login = Action {implicit req =>
    Ok(views.html.login())
  }

  def register = Action {implicit req=>
    Ok(views.html.register(models.UserFormData.form))
  }

  def loginSubmission = Action.async {implicit req =>
    models.LoginUserFormData.form.bindFromRequest.fold(
      formWithErrors => Future(BadRequest),
      userLoginData =>  {
        UserService.checkUser(userLoginData.userName, userLoginData.password).map(_.isEmpty).map(b => {
          b match {
            case true => Unauthorized(views.html.login())
            case false => chatHttp(req, userLoginData.userName).withSession("username"->userLoginData.userName)
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
          Redirect(routes.Application.chat(newUser.userName, defaultRoomName)).withSession({"username"->newUser.userName})
        })

      }
    )
  }


}