package models


import java.io.File

import ch.qos.logback.core.util.FileUtil
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

  def allUploadsJoined =
      for {
        (ul, u) <- uploads join models.Users.users on (_.userId === _.id)
      } yield (u.userName, ul.userId, ul.path, ul.timestamp, ul.id)

  def allUploads_ = dbConfig.db.run(allUploadsJoined.result)

  def deleteUploadById(id: Long) = dbConfig.db.run(uploads.filter(_.id === id).delete)

  import play.api.Play.current
  def quietlyRemove_(id: Long) =
    for {
      ul <- uploads.filter((_.id=== id)).result
    } yield (ul.headOption.map(u=>  {
      new File(Play.application.path + "/public/uploaded" + u.path).delete()
        Play.application.path + "/public/uploaded" + u.path
    }))

  def quietlyRemove(id: Long): Future[Option[String]] = dbConfig.db.run(quietlyRemove_(id))


}
