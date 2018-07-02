package controllers

import dao.UserDAO
import javax.inject._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(
                                userDAO: UserDAO,
                                cc: ControllerComponents)
                              (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def managementUserList() = Action { implicit request =>
    Ok(views.html.managementUserList())
  }

  def userList() = Action { implicit request =>
    // userDAO.all()
    Ok("TEST")
  }
}
