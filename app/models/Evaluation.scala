package models

import java.sql.Timestamp

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
  implicit val userFormat: OFormat[Evaluation] = Json.using[Json.WithDefaultValues].format[Evaluation]
  implicit val userReads: Reads[Evaluation] = Json.reads[Evaluation]
  implicit val reads: Reads[Timestamp] = Reads.of[Long] map (new Timestamp(_))
  implicit val writes: Writes[Timestamp] = Writes { ts: Timestamp => JsString(ts.toString) }
  implicit val formats: Format[Timestamp] = Format(reads, writes)
}