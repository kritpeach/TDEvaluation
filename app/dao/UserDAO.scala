package dao

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import models.User

import scala.util.Try

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val Users = TableQuery[UsersTable]

  def list(): Future[Seq[User]] = db.run(Users.sortBy(_.username).result)

  def insert(user: User): Future[User] = db
    .run(Users returning Users.map(_.id) += user)
    .map(id => user.copy(id = Some(id)))

  def delete(id: Long): Future[Int] = db.run(Users.filter(_.id === id).delete)

  // .map(u => user.copy(id = u.get.id))
  def upsert(user: User) = db.run((Users returning Users).insertOrUpdate(user)).map {
    case None => user
    case u => user.copy(id = u.get.id)
  }

  def createTable: Future[Unit] = db.run(Users.schema.create)

  private class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def username = column[String]("USERNAME")

    def idx = index("idx_username", username, unique = true)

    def password = column[String]("PASSWORD")

    def isManager = column[Boolean]("IS_MANAGER")

    def * = (id.?, username, password, isManager) <> ((User.apply _).tupled, User.unapply)
  }

}
