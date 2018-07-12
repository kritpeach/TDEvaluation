package dao

import java.sql.Timestamp

import javax.inject.Inject
import models.Evaluation
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class EvaluationDAO @Inject()(val userDAO: UserDAO, protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val Evaluations = TableQuery[EvaluationsTable]
  def getById(id: Long): Future[Option[Evaluation]] = db.run(Evaluations.filter(_.id === id).result.headOption)
  def list(): Future[Seq[Evaluation]] = db.run(Evaluations.sortBy(_.id).result)
  def list(enabled: Boolean): Future[Seq[Evaluation]] = db.run(Evaluations.filter(_.enabled === enabled).sortBy(_.id).result)

  def insert(evaluation: Evaluation): Future[Evaluation] = db
    .run(Evaluations returning Evaluations.map(_.id) += evaluation)
    .map(id => evaluation.copy(id = Some(id)))

  def delete(id: Long): Future[Int] = db.run(Evaluations.filter(_.id === id).delete)

  def upsert(evaluation: Evaluation): Future[Evaluation] = db.run((Evaluations returning Evaluations).insertOrUpdate(evaluation)).map {
    case None => evaluation
    case Some(u) => evaluation.copy(id = u.id)
  }
  def createTable: Future[Unit] = db.run(Evaluations.schema.create)

  class EvaluationsTable(tag: Tag) extends Table[Evaluation](tag, "Evaluation") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def titleIndex = index("index_title", title, unique = true)
    def enabled = column[Boolean]("enabled")
    def createAt = column[Timestamp]("create_at")
    def creatorId = column[Long]("creator")
    def * = (id.?, title, enabled, createAt, creatorId) <> ((Evaluation.apply _).tupled, Evaluation.unapply)

    def creator = foreignKey("CREATOR_FK", creatorId, userDAO.Users)(_.id , onDelete=ForeignKeyAction.Cascade)
  }

}
