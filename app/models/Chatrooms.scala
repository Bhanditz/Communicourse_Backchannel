package models

import play.api.data.Form
import play.api.data.Forms._



case class NewChatroomForm(name: String)

object NewChatroomForm {
  val form = Form(mapping(
    "name"->nonEmptyText
  )(NewChatroomForm.apply)(NewChatroomForm.unapply)
  )
}
