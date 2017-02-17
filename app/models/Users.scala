package models

import play.api.data._
import play.api.data.Forms._

/**
  * Created by robertMueller on 17.02.17.
  */

trait Role
case object Student extends Role
case object Teacher extends Role

case class User(role: Role, userName: String, password: String, email: String)

case class UserFormData(role: String, userName: String, password: String, email: String)

object UserFormData {
  val form = Form(mapping(
    "role"->nonEmptyText,
    "userName"->nonEmptyText,
    "password"->nonEmptyText,
    "email"-> email
  )(UserFormData.apply)(UserFormData.unapply)
  )
}