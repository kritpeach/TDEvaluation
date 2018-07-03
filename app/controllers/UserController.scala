package controllers

import dao.UserDAO
import javax.inject._
import models.User
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
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
  val userForm: Form[User] = Form(
    mapping(
      "id" -> optional(longNumber),
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "is_manager" -> boolean
    )(User.apply)(User.unapply)
  )

  def managementUserList(): Action[AnyContent] = Action.async { implicit request =>
    implicit val userFormat = Json.format[User]
    userDAO.list.map(users => {
      val userListJSON = Json.toJson(users).toString()
      Ok(views.html.managementUserList(userListJSON))
    })
  }

  def deleteUser(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userDAO.delete(id).map(x => Ok(Json.toJson(x)))
  }

  def createUser(): Action[AnyContent] = Action.async { implicit request =>
    val user: User = userForm.bindFromRequest.get
    println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + user)
    userDAO.insert(user).map(x => Ok(Json.toJson(x)))
  }

  def createTable() = Action {
    Await.result(userDAO.createTable, Duration.Inf)
    Ok("Create Table")
  }

  def userList(): Action[AnyContent] = Action.async { implicit request =>
    implicit val userFormat = Json.format[User]
    userDAO.list.map(user => Ok(Json.toJson(user)))
  }
}
