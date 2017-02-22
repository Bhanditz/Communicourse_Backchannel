package UserGeneratedInfo

import MyLens.MyLens
import MyLens.containsKey
import Quiz.Quiz
import _root_.Quiz.Types._
import scala.util.matching.Regex

/**
  * Created by robertMueller on 13.02.17.
  */
//case class UserGeneratedInfoGroup(val infoGroupName: String, val info: Map[String, List[ValueProposal]] = Map())
case class ValueProposal(val infoName: String, val infoValue: String, val message: Option[String] = None,
                         val upvotedBy: Set[String] = Set.empty, val downvotedBy: Set[String] = Set.empty)
{
  override def toString():String = {
    val sep = System.lineSeparator
    sep+"="*15 + sep + s"${this.infoName}: ${this.infoValue}"+sep+s"upvotes: ${this.upvotedBy.size} downvotes: ${this.downvotedBy.size}" +sep + "="*15 + sep
  }
}


object ValueProposal {
  /*
  val infoGroupLens = MyLens[UserGeneratedInfoGroup, Map[String, List[ValueProposal]]](
    get = (infoGroup: UserGeneratedInfoGroup) => infoGroup.info,
    set = (infoGroup: UserGeneratedInfoGroup, newInfo: Map[String, List[ValueProposal]]) => infoGroup.copy(info = newInfo)
  )*/

  //def infoLens(infoKey: String) = infoGroupLens andThen containsKey[String, List[ValueProposal]](infoKey)

  /*def proposalLens(infoKey: String) = infoLens(infoKey).andThen(MyLens[Option[List[ValueProposal]], ValueProposal](
  get = (proposals: Option[List[ValueProposal]]) => ???,
  set = (optList: Option[List[ValueProposal]], newProposal: ValueProposal) => Some(optList.getOrElse(Nil) ++: List(newProposal))
))*/

  /*Sets do not allow duplicated*/
  def upvote(userName: String)(proposal: ValueProposal): ValueProposal = proposal.copy(upvotedBy = proposal.upvotedBy + userName , downvotedBy = proposal.downvotedBy - userName)
  def downvote(userName: String)(proposal: ValueProposal): ValueProposal = proposal.copy(upvotedBy = proposal.upvotedBy - userName , downvotedBy = proposal.downvotedBy + userName)
  def withMessage(proposal: ValueProposal, msg : String): ValueProposal = proposal.copy(message = Some(msg))
  def upvoteDownvoteRate(proposal: ValueProposal): Double = proposal.upvotedBy.size/proposal.downvotedBy.size
  def upvoteDownvoteCount(proposal: ValueProposal): Int = proposal.upvotedBy.size+proposal.downvotedBy.size
  val minCountForDeletion = 2
  val maxRateForDeletion = 1.5
  def mayBeDeleted(proposal: ValueProposal) =
    try {
    upvoteDownvoteRate(proposal) <= maxRateForDeletion && upvoteDownvoteCount(proposal) >= minCountForDeletion
  } catch  {
    case e:java.lang.ArithmeticException => false
  }
  def rename(proposal: ValueProposal, version: Int) = {
    val pattern =  """"([^"]*)"""".r
    val newName = proposal.infoName match {
      case pattern(inside) => "\"" + inside + "("+version.toString+")" + "\""
      case _ => proposal.infoName + "("+version.toString+")"
    }
    //val inside = pattern.findAllIn(proposal.infoName).group(0)
    proposal.copy(infoName = newName)
  }
  /*Recursion*/
  def renameWhileTaken(myProposal: ValueProposal, proposals: List[ValueProposal], startWithVersion:Int = 0): ValueProposal = {
    val duplicates = proposals.filter(p=>p.infoName == myProposal.infoName)
    val isDuplicate = duplicates.size > 0
    isDuplicate match {
      case false => myProposal
      case true => renameWhileTaken(rename(myProposal, startWithVersion + 1), proposals)
    }
  }
}