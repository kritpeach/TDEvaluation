package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // TODO: Split controller
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def signIn() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.signin())
  }

  def managementUserList() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.managementUserList())
  }

  def managementEvaluationList() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.managementEvaluationList())
  }

  def managementEvaluationEdit(id: Long) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.managementEvaluationEdit())
  }

  def managementEvaluationView(id: Long) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.managementEvaluationView())
  }

  def managementEvaluationResponse(evaluationFormId: Long, userId: Long) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.managementEvaluationResponse())
  }

}
