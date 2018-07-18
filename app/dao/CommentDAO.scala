package dao

import javax.inject.Inject
import models.Comment
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class CommentDAO @Inject()(val userDAO: UserDAO, val responseDAO: ResponseDAO, protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val Comments = TableQuery[CommentsTable]

  def list(): Future[Seq[Comment]] = db.run(Comments.sortBy(_.id).result)

  def insert(comment: Comment): Future[Comment] = db
    .run(Comments returning Comments.map(_.id) += comment)
    .map(id => comment.copy(id = Some(id)))

  def delete(id: Long): Future[Int] = db.run(Comments.filter(_.id === id).delete)

  def upsert(comment: Comment): Future[Comment] = db.run((Comments returning Comments).insertOrUpdate(comment)).map {
    case None => comment
    case Some(u) => comment.copy(id = u.id)
  }

  def createTable: Future[Unit] = db.run(Comments.schema.create)

  class CommentsTable(tag: Tag) extends Table[Comment](tag, "Comment") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def comment = column[String]("comment")

    def userId = column[Long]("user_id")

    def responseId = column[Long]("response_id")

    def * = (id.?, comment, userId, responseId) <> ((Comment.apply _).tupled, Comment.unapply)

    def idx = index("commentUserResponseIndex", (userId, responseId), unique = true)

    def userFK = foreignKey("USER_FK", userId, userDAO.Users)(_.id, onDelete = ForeignKeyAction.Cascade)

    def responseFK = foreignKey("RESPONSE_FK", responseId, responseDAO.Responses)(_.id, onDelete = ForeignKeyAction.Cascade)
  }

}
