package controllers

import javax.inject.Inject
import javax.inject.Singleton
import akka.actor.ActorSystem
import play.api._
import play.api.libs.EventSource
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.JsValue
import play.api.mvc._

@Singleton
class Application @Inject() (system: ActorSystem) extends Controller {

  val (chatOut, chatChannel) = Concurrent.broadcast[JsValue]

  def index = Action { implicit req =>

    Ok(views.html.index(routes.Application.chatFeed(),
      routes.Application.postMessage()
    ))
  }

  def chatFeed = Action { req =>
    println("User connected:" + req.remoteAddress)
    Ok.chunked(chatOut &> EventSource()).as("text/event-stream")
  }

  def postMessage = Action(parse.json) {req =>
    chatChannel.push(req.body)
    Ok
  }
}