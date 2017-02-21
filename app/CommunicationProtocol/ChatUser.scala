package CommunicationProtocol

import CommunicationProtocol.Chatroom.{Join, Leave}
import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive



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
    case prm @ PrivateMessage(m,u,b) => out ! ClientMessage(m, u + "(private) " ,b)
    case pm @ PublicMessage(m,u,b) => out ! ClientMessage(m,u,b)
    /*=====UPWARDS MESSAGES=====*/
    case cm @ ClientMessage(m,_,_) => chatroom ! cm // hoch
  }
}
