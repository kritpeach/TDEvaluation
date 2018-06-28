package dao

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.ExecutionContext
import models.User

class UserDAO  @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  private val Users = TableQuery[]
  private class UsersTable(tag: Tag) extends Table[User](tag,"user") {
    def username = column[String]("username",O.PrimaryKey)
    def password = column[String]("password")
    def isManager = column[Boolean]("is_manager")
    def * = (username,password,isManager) <> (User.tupled,User.unapply)
  }
}
