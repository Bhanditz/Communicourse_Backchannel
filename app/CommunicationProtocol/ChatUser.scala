package CommunicationProtocol

import CommunicationProtocol.ChatUser
import CommunicationProtocol.Chatroom.{Join, Leave}
import CommunicationProtocol.Message
import akka.actor.{Actor, ActorRef, Props}
import akka.actor.Actor.Receive
import akka.event.LoggingReceive

/**
  * Created by robertMueller on 15.02.17.
  */

object ChatUser{
  def props(out: ActorRef, chatroom: ActorRef) = Props(new ChatUser(out, chatroom))
}

class ChatUser(out: ActorRef, chatroom: ActorRef)extends Actor{


  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = chatroom ! Join(self)
  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = chatroom ! Leave(self)

  override def receive: Receive = LoggingReceive{
    /*=====DOWNWARDS MESSAGES=====*/
    case prm @ PrivateMessage(m,u,b) => out ! ClientMessage(m, "(private) " + u ,b)
    case pm @ PublicMessage(m,u,b) => out ! ClientMessage(m,u,b)
    /*=====UPWARDS MESSAGES=====*/
    case cm @ ClientMessage(m,_,_) => chatroom ! cm // hoch
  }
}
