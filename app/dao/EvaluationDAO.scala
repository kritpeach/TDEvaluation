package dao

import java.sql.Timestamp

import javax.inject.Inject
import models.Evaluation
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class EvaluationDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val Evaluations = TableQuery[EvaluationsTable]

  def list(): Future[Seq[Evaluation]] = db.run(Evaluations.sortBy(_.id).result)

  def insert(evaluation: Evaluation): Future[Evaluation] = db
    .run(Evaluations returning Evaluations.map(_.id) += evaluation)
    .map(id => evaluation.copy(id = Some(id)))

  def delete(id: Long): Future[Int] = db.run(Evaluations.filter(_.id === id).delete)

  def upsert(evaluation: Evaluation): Future[Evaluation] = db.run((Evaluations returning Evaluations).insertOrUpdate(evaluation)).map {
    case None => evaluation
    case Some(u) => evaluation.copy(id = u.id)
  }
  def createTable: Future[Unit] = db.run(Evaluations.schema.create)

  private class EvaluationsTable(tag: Tag) extends Table[Evaluation](tag, "USER") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def title = column[String]("title")

    def titleIndex = index("index_title", title, unique = true)

    def enabled = column[Boolean]("enabled")

    def createAt = column[Timestamp]("create_at")

    def creator = column[Long]("creator")

    def * = (id.?, title, enabled, createAt, creator) <> ((Evaluation.apply _).tupled, Evaluation.unapply)
  }

}
