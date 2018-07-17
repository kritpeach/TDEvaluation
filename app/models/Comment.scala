package models

import play.api.libs.json.{Json, OFormat, Reads}

case class Comment(id: Option[Long] = None, comment: String, userId: Long, responseId: Long)

object Comment {
  implicit val commentFormat: OFormat[Comment] = Json.format[Comment]
  implicit val commentReads: Reads[Comment] = Json.reads[Comment]
}