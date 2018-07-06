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
  implicit val evaluationFormat: OFormat[Evaluation] = Json.format[Evaluation]
  implicit val evaluationReads: Reads[Evaluation] = Json.reads[Evaluation]
}