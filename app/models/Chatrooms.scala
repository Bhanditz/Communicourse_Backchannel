package models

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by robertMueller on 18.02.17.
  */

case class NewChatroomForm(name: String)

object NewChatroomForm {
  val form = Form(mapping(
    "name"->nonEmptyText
  )(NewChatroomForm.apply)(NewChatroomForm.unapply)
  )
}
