package dao

import java.sql.Timestamp

import javax.inject.Inject
import models.Response
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ResponseDAO @Inject()(val userDAO: UserDAO, val questionDAO: QuestionDAO, protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val Responses = TableQuery[ResponsesTable]

  def list(): Future[Seq[Response]] = db.run(Responses.sortBy(_.id).result)

  def getById(id: Long): Future[Option[Response]] = db.run(Responses.filter(_.id === id).result.headOption)

  def get(userId: Long, questionId: Long): Future[Option[Response]] = db.run(Responses
    .filter(_.creatorId === userId)
    .filter(_.questionId === questionId)
    .result.headOption
  )

  def insert(response: Response): Future[Response] = db
    .run(Responses returning Responses.map(_.id) += response)
    .map(id => response.copy(id = Some(id)))

  def delete(id: Long): Future[Int] = db.run(Responses.filter(_.id === id).delete)

  def upsert(response: Response): Future[Response] = db.run((Responses returning Responses).insertOrUpdate(response)).map {
    case None => response
    case Some(u) => response.copy(id = u.id)
  }

  def createTable: Future[Unit] = db.run(Responses.schema.create)

  class ResponsesTable(tag: Tag) extends Table[Response](tag, "Response") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def answer = column[String]("answer")

    def createAt = column[Timestamp]("createAt")

    def creatorId = column[Long]("creator")

    def questionId = column[Long]("question")

    def * = (id.?, answer, createAt, creatorId, questionId) <> ((Response.apply _).tupled, Response.unapply)

    def idx = index("idx", (creatorId, questionId), unique = true)

    def creatorFK = foreignKey("CREATOR_FK", creatorId, userDAO.Users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def questionFK = foreignKey("QUESTION_FK", questionId, questionDAO.Questions)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

}
