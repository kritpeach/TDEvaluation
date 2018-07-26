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

  def getQuestions = TableQuery[QuestionsTable]

  def list(): Future[Seq[Question]] = db.run(Questions.sortBy(_.seq).result)

  def list(evaluationId: Long): Future[Seq[Question]] = db.run(Questions.filter(_.evaluationId === evaluationId).sortBy(_.seq).result)

  def getById(id: Long): Future[Option[Question]] = db.run(Questions.filter(_.id === id).result.headOption)

  def getFirst(evaluationId: Long): Future[Option[Question]] = db.run(Questions.filter(_.evaluationId === evaluationId).sortBy(_.seq).take(1).result.headOption)

  def getNextQuestion(question: Question): Future[Option[Question]] = db.run(Questions
    .filter(_.evaluationId === question.evaluationId)
    .filter(_.seq > question.seq)
    .sortBy(_.seq)
    .take(1)
    .result
    .headOption
  )

  def delete(id: Long): Future[Int] = db.run(Questions.filter(_.id === id).delete)

  def upsert(question: Question): Future[Question] = db.run((Questions returning Questions).insertOrUpdate(question)).map {
    case None => question
    case Some(u) => question.copy(id = u.id)
  }

  def updateQuestionSeq(questionSeq: List[Long]): Future[List[Int]] = {
    val actions = DBIO.sequence(questionSeq.zipWithIndex.map(question => {
      val questionId = question._1
      val index = question._2
      Questions.filter(_.id === questionId).map(_.seq).update(index)
    }))
    db.run(actions)
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

    def seq = column[Int]("seq")

    def * : ProvenShape[Question] = (id.?, content, questionType, evaluationId, seq) <> ((Question.apply _).tupled, Question.unapply)

    def evaluation = foreignKey("EVALUATION_FK", evaluationId, evaluationDAO.Evaluations)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

}
