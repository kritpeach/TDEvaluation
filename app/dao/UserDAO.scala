package dao

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import models.User

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  private val Users = TableQuery[UsersTable]
  def all(): Future[Seq[User]] = db.run(Users.result)

  private class UsersTable(tag: Tag) extends Table[User](tag, "USER") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def username = column[String]("USERNAME")
    def password = column[String]("PASSWORD")
    def isManager = column[Boolean]("IS_MANAGER")

    def * = (id.?, username, password, isManager) <> (User.tupled, User.unapply)
  }

}
