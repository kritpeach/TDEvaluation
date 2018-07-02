package controllers

import dao.UserDAO
import javax.inject._
import models.User
import play.api.i18n.I18nSupport
import play.api.libs.json.{Json, Writes}
import play.api.mvc._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
// import scala.async.Async.{async, await}

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(
                                userDAO: UserDAO,
                                cc: ControllerComponents)
                              (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def managementUserList() = Action { implicit request =>
    Ok(views.html.managementUserList())
  }

  def createTable() = Action {
    Await.result(userDAO.createTable, Duration.Inf)
    Ok("Create Table")
  }

  def userList() = Action { implicit request =>
    // val user = User(None,"peachkrit","987654321",true)
    implicit val userFormat = Json.format[User]
    val users = Await.result(userDAO.all, Duration.Inf)
    Ok(Json.toJson(users))
  }
}
