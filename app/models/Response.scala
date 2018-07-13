package models

import java.sql.Timestamp
import java.text.SimpleDateFormat

import play.api.libs.json._

case class Response(id: Option[Long] = None, answer: String, createAt: Timestamp, creatorId: Long, questionId: Long)

object Response {
  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    def reads(json: JsValue): JsSuccess[Timestamp] = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }
  implicit val responseFormat: OFormat[Response] = Json.using[Json.WithDefaultValues].format[Response]
  implicit val responseReads: Reads[Response] = Json.reads[Response]
}