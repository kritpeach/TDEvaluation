package controllers

import dao.UserDAO
import javax.inject._
import models.User
import org.postgresql.util.PSQLException
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, JsSuccess, Json, Writes}
import play.api.mvc._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(
                                userDAO: UserDAO,
                                cc: ControllerComponents)
                              (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  val signInForm = Form(
    tuple(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )
  )

  def authenticate: Action[AnyContent] = Action.async { implicit request =>
    val (username, password) = signInForm.bindFromRequest.get
    userDAO.getUser(username, password).map {
      case Some(user) => user.isManager match {
        case true => Redirect(routes.UserController.managementUserList()).withSession("uid" -> user.id.get.toString, "username" -> user.username)
        case _ => Redirect(routes.EvaluationController.assessorEvaluation()).withSession("uid" -> user.id.get.toString, "username" -> user.username)
      }
      case None => Redirect(routes.UserController.signIn()).withNewSession.flashing("Login Failed" -> "Invalid username or password.")
    }
  }

  def signIn() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.signin())
  }

  def signOut = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.UserController.signIn())
      .flashing("info" -> "You are logged out.")
      .withNewSession
  }

  def managementUserList(): Action[AnyContent] = Action.async { implicit request =>
    userDAO.list().map(users => {
      val userListJSON = Json.toJson(users).toString()
      Ok(views.html.managementUserList(userListJSON))
    })
  }

  def deleteUser(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userDAO.delete(id).map(x => Ok(Json.toJson(x)))
  }

  def upsertUser(): Action[AnyContent] = Action { implicit request =>
    val userResult = request.body.asJson.get.validate[User]
    userResult match {
      case JsSuccess(user: User, _) =>
        Try(Await.result(userDAO.upsert(user), Duration.Inf)) match {
          case Success(u) => Ok(Json.obj("success" -> true, "user" -> u))
          case Failure(e: PSQLException) => e.getSQLState match {
            case "23505" => Ok(Json.obj("success" -> false, "uniqueViolation" -> true))
            case _ => Ok(Json.obj("success" -> false))
          }
        }
      case e: JsError => Ok(JsError.toJson(e))
    }
  }

  def createTable() = Action {
    Await.result(userDAO.createTable, Duration.Inf)
    Ok("Create Table")
  }

  def userList(): Action[AnyContent] = Action.async { implicit request =>
    userDAO.list().map(user => Ok(Json.toJson(user)))
  }
}
