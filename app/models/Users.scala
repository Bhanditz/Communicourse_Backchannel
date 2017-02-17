package models

import play.api.data._
import play.api.data.Forms._
import play.api.Play
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by robertMueller on 17.02.17.
  */



case class User(id: Long, role: String, userName: String, password: String, email: String)

case class UserFormData(role: String, userName: String, password: String, email: String)

case class LoginUserFormData(userName: String, password: String)

object UserFormData {
  val form = Form(mapping(
    "role"->nonEmptyText,
    "userName"->nonEmptyText,
    "password"->nonEmptyText,
    "email"-> email
  )(UserFormData.apply)(UserFormData.unapply)
  )
}

object LoginUserFormData {
  val form = Form(mapping(
    "userName" -> nonEmptyText,
    "password" -> nonEmptyText
  )(LoginUserFormData.apply)(LoginUserFormData.unapply)
  )
}


class UserTableDef(tag: Tag) extends Table[User](tag, "user") {

  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def role = column[String]("role")
  def userName = column[String]("userName")
  def password = column[String]("password")
  def email = column[String]("email")


  override def * =
    (id, role, userName, password, email) <> (User.tupled, User.unapply)
}

object Users {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val users = TableQuery[UserTableDef]

  def add(user: User): Future[String] = {
    dbConfig.db.run(users += user).map(res => "User successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(users.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Seq[User]] = {
    dbConfig.db.run(users.filter(_.id === id.toLong).result)
  }

  def check(userName: String, password: String): Future[Seq[User]] = {
    dbConfig.db.run(users.filter(_.userName === userName).filter( _.password === password).result)
  }

  def checkpassword(id: Long, password: String): Future[Seq[User]] = {
    dbConfig.db.run(users.filter(_.id === id).filter( _.password === password).result)
  }

  def update_password(id: String, password: String): Future[Int] = {
    dbConfig.db.run(users.filter(_.id === id.toLong).map(p => (p.password))
      .update(password))
  }

  def update_personalinfor(id: String, name: String, email: String): Future[Int] = {
    dbConfig.db.run(users.filter(_.id === id.toLong).map(p => (p.userName,p.email))
      .update(name,email))
  }

  def listAll: Future[Seq[User]] = {
    dbConfig.db.run(users.result)
  }



}
