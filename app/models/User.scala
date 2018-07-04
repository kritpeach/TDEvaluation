package models

import play.api.libs.json.Json

case class User(id: Option[Long] = None, username: String, password: String, isManager: Boolean)

object User {
  implicit val userFormat = Json.format[User]
  implicit val userReads = Json.reads[User]
}