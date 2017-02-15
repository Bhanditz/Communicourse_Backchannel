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











  /*val (chatOut, chatChannel) = Concurrent.broadcast[JsValue]
  def chatFeed = Action { req =>
    println("User connected:" + req.remoteAddress)
    Ok.chunked(chatOut &> EventSource()).as("text/event-stream")
  }

  def postMessage = Action(parse.json) {req =>
    /*parse message to case class*/
    implicit val messageReads = Json.reads[Message]
    implicit val messageWrites = Json.writes[Message]
    val message: JsResult[Message] = Json.fromJson[Message](req.body)
    /*handle message*/
    message match {
      case JsSuccess(m: Message, path: JsPath) => {
        val botCommandPosition = extractMessageForBotPos(m.message)
        if(botCommandPosition >= 0){
          val quizLangParser = new QuizBotLanguage
          val quizLang = quizLangParser.language
          val command =  m.message.drop(botCommandPosition).take(128)
           command.isEmpty match {
             case true => {
               chatChannel.push(Json.toJson[Message](botMessage("You did not give me any instructions :-(" )))
             }
             case _ => {
               val result = quizLangParser.parse(quizLang, command)
               result.successful match {
                 case true => chatChannel.push(Json.toJson[Message](botMessage(result.toString)))
                 case _ => chatChannel.push(Json.toJson[Message](botMessage("you gave me an instruction i could not understand understand :-(")))
               }

             }

          }

        }
        else {
          chatChannel.push(req.body)
        }
        Ok
      }
      case e: JsError =>{
        val error = BotInstructions.generalJsonErrorMessage
        chatChannel.push(Json.toJson[Message](error))
        BadRequest
      }
    }
  }*/
}