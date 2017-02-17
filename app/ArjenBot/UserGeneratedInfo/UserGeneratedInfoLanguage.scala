package UserGeneratedInfo
import Traits.BotLanguage
import scala.util.parsing.combinator.RegexParsers

/**
  * Created by robertMueller on 14.02.17.
  */
class UserGeneratedInfoLanguage extends BotLanguage {

  /*=====ACTION MAPPINGS=====*/
  import UserGeneratedInfoActor.{GetAllProposals, AllProposals,
    AddProposal,
    Upvote, Downvote, DeleteProposal
  }

  override def language = addProposal|upvote|downvote|delete|allProposals

  def addProposal = ("create info"|"new info"|"make info")~>("name"|"key")~>opt("=")~>quotedString~("info"|"value")~opt("=")~quotedString~opt("message"|"msg" ~> opt("=") ~> quotedString) ^^ {
    case (infoName~_~_~value~message) => AddProposal(ValueProposal(infoName, value, message))
  }

  def allProposals = ("get props"|"props"|"proposals"|"all proposals"|"get all proposals"|"get info"|"all info"|"info") ^^ {_ => GetAllProposals}

  def upvote = ("+1"|"+"|"upvote"|"up")~>quotedString~withUser ^^ {
    case(infoName~userName)=> Upvote(infoName, userName)
  }

  def downvote = ("-1"|"-"|"downvote"|"down")~>quotedString~withUser ^^ {
    case(infoName~userName)=> Downvote(infoName, userName)
  }

  def delete = ("delete info"|"remove info"|"kill info")~>quotedString ^^ {
    case(infoName)=> DeleteProposal(infoName)
  }

}
