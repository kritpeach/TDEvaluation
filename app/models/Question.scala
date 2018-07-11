package models

import models.QuestionType.QuestionType
import play.api.libs.json.{Json, OFormat, Reads, Writes}

case class Question(id: Option[Long] = None, content: String, questionType: QuestionType, evaluationId: Long)

object QuestionType extends Enumeration {
  type QuestionType = Value
  val Score: models.QuestionType.Value = Value("SCORE")
  val Text: models.QuestionType.Value = Value("TEXT")
}

object Question {
  implicit val questionTypeReads: Reads[models.QuestionType.Value] = Reads.enumNameReads(QuestionType)
  implicit val questionTypeWrites = Writes.enumNameWrites
  implicit val questionFormat: OFormat[Question] = Json.using[Json.WithDefaultValues].format[Question]
  implicit val questionReads: Reads[Question] = Json.reads[Question]
}
