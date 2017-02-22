package services

/**
  * Created by robertMueller on 17.02.17.
  */

import models.{Users, User}

import scala.concurrent.Future


object UserService {


  def addUser(user: User): Future[String] = {
    Users.add(user)
  }

  def deleteUser(id: Long): Future[Int] = {
    Users.delete(id)
  }


  def update_password(id: String,password:String): Future[Int] = {
    Users.update_password(id,password)
  }

  def getUser(id: Long): Future[Seq[User]] = {
    Users.get(id)
  }

  def checkUser(userName: String, password: String): Future[Option[User]] = {
    Users.check(userName,password)
  }

  def checkPassword(id: Long, password: String): Future[Seq[User]] = {
    Users.checkpassword(id,password)
  }

  def listAllUsers: Future[Seq[User]] = {
    Users.listAll
  }

  def update_personalinfor(id: String,name:String,email:String): Future[Int] = {
    Users.update_personalinfor(id,name,email)
  }
}
