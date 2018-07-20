package dao

import java.sql.Timestamp

import javax.inject.Inject
import models.{Evaluation, UserResponseCount}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

class EvaluationDAO @Inject()(val userDAO: UserDAO, protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val Evaluations = TableQuery[EvaluationsTable]

  def getById(id: Long): Future[Option[Evaluation]] = db.run(Evaluations.filter(_.id === id).result.headOption)

  def list(): Future[Seq[Evaluation]] = db.run(Evaluations.sortBy(_.id).result)

  def list(enabled: Boolean): Future[Seq[Evaluation]] = db.run(Evaluations.filter(_.enabled === enabled).sortBy(_.id).result)

  def listOnlyDefinedQuestion(enabled: Boolean): Future[Seq[Evaluation]] = {
    /*
    val implicitInnerJoin = for {
      e <- Evaluations
      q <- questionDAO.getQuestions if e.id === q.evaluationId
    } yield e
    db.run(implicitInnerJoin.distinctOn(_.id).filter(_.enabled === enabled).result)
    */
    ???
  }

  def delete(id: Long): Future[Int] = db.run(Evaluations.filter(_.id === id).delete)

  def upsert(evaluation: Evaluation): Future[Evaluation] = db.run((Evaluations returning Evaluations).insertOrUpdate(evaluation)).map {
    case None => evaluation
    case Some(u) => evaluation.copy(id = u.id)
  }

  def createTable: Future[Unit] = db.run(Evaluations.schema.create)

  def userResponseCount(evaluationId: Long): Future[Vector[UserResponseCount]] = {
    implicit val getUserResponseCount: AnyRef with GetResult[UserResponseCount] = GetResult[UserResponseCount](r => UserResponseCount(r.nextLong, r.nextString, r.nextInt, r.nextInt))
    val sql = sql"""select u."ID", u."USERNAME", coalesce(response_count,0) as response_count,(select count(*) from "Question" where "evaluationId" = $evaluationId) as answer_count from ( select creator, count(q) as response_count from "Response" join "Question" q on "Response".question = q.id where q."evaluationId" = $evaluationId group by creator) a right join "USER" u on u."ID" = a.creator where u."IS_MANAGER" is false;""".as[UserResponseCount]
    db.run(sql)
  }

  def questionResponseJSON(evaluationId: Long, userId: Long): Future[Option[String]] = {
    val sql =
      sql"""
           |select json_agg(t)
           |from (select
           |        "Question".id      as question_id,
           |        "Question".content as question,
           |        R.id               as response_id,
           |        R.answer,
           |        comments
           |      from "Question"
           |        join (
           |               select
           |                 "Response".*,
           |                 array_remove(array_agg(C), NULL) as comments
           |               from "Response"
           |                 left join (
           |                             select
           |                               U."ID"         as user_id,
           |                               U."USERNAME"   as username,
           |                               "Comment"."ID" as comment_Id,
           |                               "Comment".comment,
           |                               "Comment".response_id
           |                             from "Comment"
           |                               join "USER" U on "Comment".user_id = U."ID") C on "Response".id = C.response_id
           |               where creator = $userId
           |               group by "Response".id) R on "Question".id = R.question
           |      where "evaluationId" = $evaluationId
           |      order by question_id) t
         """.stripMargin.as[String].headOption
    db.run(sql)
  }


  class EvaluationsTable(tag: Tag) extends Table[Evaluation](tag, "Evaluation") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def title = column[String]("title")

    def titleIndex = index("index_title", title, unique = true)

    def enabled = column[Boolean]("enabled")

    def createAt = column[Timestamp]("create_at")

    def creatorId = column[Long]("creator")

    def * = (id.?, title, enabled, createAt, creatorId) <> ((Evaluation.apply _).tupled, Evaluation.unapply)

    def creator = foreignKey("CREATOR_FK", creatorId, userDAO.Users)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

}
