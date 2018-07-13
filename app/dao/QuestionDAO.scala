package dao

import javax.inject.Inject
import models.{Question, QuestionType}
import models.QuestionType.QuestionType
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class QuestionDAO @Inject()(val evaluationDAO: EvaluationDAO, protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val Questions = TableQuery[QuestionsTable]
  def list(): Future[Seq[Question]] = db.run(Questions.sortBy(_.id).result)
  def list(evaluationId: Long): Future[Seq[Question]] = db.run(Questions.filter(_.evaluationId === evaluationId).result)
  def getById(id: Long): Future[Option[Question]] = db.run(Questions.filter(_.id === id).result.headOption)
  def getNextQuestion(questionId: Long, evaluationId: Long): Future[Option[Question]] = db.run(Questions
    .filter(_.evaluationId === evaluationId)
      .filter(_.id > questionId)
      .sortBy(_.id)
      .take(1)
      .result
      .headOption
  )
  def insert(question: Question): Future[Question] = db
    .run(Questions returning Questions.map(_.id) += question)
    .map(id => question.copy(id = Some(id)))

  def delete(id: Long): Future[Int] = db.run(Questions.filter(_.id === id).delete)

  def upsert(question: Question): Future[Question] = db.run((Questions returning Questions).insertOrUpdate(question)).map {
    case None => question
    case Some(u) => question.copy(id = u.id)
  }
  def createTable: Future[Unit] = db.run(Questions.schema.create)

  class QuestionsTable(tag: Tag) extends Table[Question](tag, "Question") {
    implicit val questionTypeMapper = MappedColumnType.base[QuestionType, String](
      e => e.toString,
      s => QuestionType.withName(s)
    )

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def questionType = column[QuestionType]("question_type")
    def evaluationId = column[Long]("evaluationId")
    def * : ProvenShape[Question] = (id.?, content, questionType, evaluationId) <> ((Question.apply _).tupled, Question.unapply)

    def evaluation = foreignKey("EVALUATION_FK", evaluationId, evaluationDAO.Evaluations)(_.id , onDelete=ForeignKeyAction.Cascade)
  }

}
