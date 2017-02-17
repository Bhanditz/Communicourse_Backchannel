package UserGeneratedInfo

import UserGeneratedInfo.UserGeneratedInfoActor._
import akka.actor.{Actor, Props}
import akka.actor.Actor.Receive
import ValueProposal._

/**
  * Created by robertMueller on 14.02.17.
  */
object UserGeneratedInfoActor {
  val props = Props[UserGeneratedInfoActor]
  /*=====MESSAGES=====*/
  case object GetAllProposals
  case class AllProposals(proposals: List[ValueProposal])
  case class AddProposal(proposal: ValueProposal)
  case class AddedProposal(key: String, value: String)
  case class Upvote(infoName: String, userName: String)
  case class Downvote(infoName: String, userName: String)
  case class DeleteProposal(key: String)
  case object GeneratedInfoSuccess
  /*=====ERRORS=====*/
  case object DeletionRequirementsNotGiven
  case object NoSuchProposal

}

class UserGeneratedInfoActor extends Actor{

  def getIfExists(infoName: String, proposals: List[ValueProposal]) ={
    val p = proposals.filter(p => p.infoName == infoName).headOption
    p.map(prop => {
      (prop, proposals diff List(prop))
    }). orElse({
      sender ! NoSuchProposal
      None
    })
  }

  override def receive: Receive = update(Nil)

  def update(proposals: List[ValueProposal]): Receive = {

    case GetAllProposals => sender ! AllProposals(proposals)
    case AddProposal(proposal) => {
      val possiblyRenamed = renameWhileTaken(proposal, proposals)
      sender ! AddedProposal(possiblyRenamed.infoName, possiblyRenamed.infoValue)
      context become update(proposals.::(possiblyRenamed))
    }
    case Upvote(infoName: String,userName: String) => getIfExists(infoName, proposals) map(p => {
      val (head, tail ) = p
      tail.::(upvote(userName)(head))
    }) foreach(proposals => {
        sender ! GeneratedInfoSuccess
        context become update(proposals)
    })
    case Downvote(infoName: String, userName: String) => getIfExists(infoName, proposals) map(p => {
      val (head, tail ) = p
      tail.::(downvote(userName)(head))
    }) foreach(proposals => {
      sender ! GeneratedInfoSuccess
      context become update(proposals)
    })
    case DeleteProposal(infoName: String) => getIfExists(infoName, proposals) foreach (p => {
      val (head, tail) = p
      mayBeDeleted(head) match {
        case false => sender ! DeletionRequirementsNotGiven
        case true => sender ! GeneratedInfoSuccess; context become update(tail)
      }
    })
  }

}
