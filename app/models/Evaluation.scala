package models

import java.sql.Timestamp
import java.text.SimpleDateFormat

import org.joda.time.DateTime
import play.api.libs.json._


case class Evaluation(
                       id: Option[Long] = None,
                       title: String,
                       enabled: Boolean = true,
                       createAt: Timestamp = new Timestamp(DateTime.now.getMillis),
                       creator: Long
                     )

object Evaluation {
  implicit val timestampFormat: Format[Timestamp] = new Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    def reads(json: JsValue): JsSuccess[Timestamp] = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }
  implicit val userFormat: OFormat[Evaluation] = Json.using[Json.WithDefaultValues].format[Evaluation]
  implicit val userReads: Reads[Evaluation] = Json.reads[Evaluation]
}