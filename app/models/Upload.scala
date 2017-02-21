package models


import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.dbio.DBIOAction

import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

/**
  * Created by robertMueller on 19.02.17.
  */
case class Upload(id: Long=0, userId: Long, path: String, timestamp: Long)
// System.currentTimeMillis / 1000
class UploadTableDef(tag: Tag) extends Table[Upload](tag, "upload") {

  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def userId = column[Long]("userId")
  def path = column[String]("path")
  def timestamp = column[Long]("timestamp")
  def user = foreignKey("UserTableDef",userId, Users.users)(_.id)
  override def * =
    (id, userId, path, timestamp) <> ((Upload.apply _).tupled, Upload.unapply)
}


object Upload {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val uploads = TableQuery[UploadTableDef]

  def userByName(userName: String)= {Users.users.filter(_.userName === userName)}



  def getUserByName(username: String) = for {
    user <- Users.users.filter(_.userName === username).result

  } yield user.headOption



  def userUpload(userName: String, path: String) =
    dbConfig.db.run(
        getUserByName(userName).flatMap(u =>
          u match {
            case Some(u) =>  uploads += Upload(0, u.id, path, System.currentTimeMillis()/1000)
            case None => DBIOAction.failed(new Exception("No such user"))
          }

        )
      )

  def allUploads: Future[Seq[Upload]] =
    dbConfig.db.run(
      uploads.sortBy(_.timestamp).result
    )

}
