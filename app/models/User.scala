package models

import play.api.libs.json.{Json, OFormat, Reads}

case class User(id: Option[Long] = None, username: String, password: String, isManager: Boolean)

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit val userReads: Reads[User] = Json.reads[User]
}