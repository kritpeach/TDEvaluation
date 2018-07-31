package controllers

import dao._
import javax.inject._
import models.User
import play.api.mvc._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class HomeController @Inject()(questionDAO: QuestionDAO,
                               evaluationDAO: EvaluationDAO,
                               responseDAO: ResponseDAO,
                               commentDAO: CommentDAO,
                               userDAO: UserDAO,
                               cc: ControllerComponents)(implicit executionContext: ExecutionContext) extends AbstractController(cc) {
  def index() = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.EvaluationController.assessorEvaluation())
  }

  def error404(path: String) = Action { implicit request: Request[AnyContent] =>
    NotFound(views.html.error("404", "Not Found", "The page you’re looking for was not found."))
  }

  def createTable() = Action { implicit request =>
    val results = for {
      userTable <- userDAO.createTable
      evaluationTable <- evaluationDAO.createTable
      questionTable <- questionDAO.createTable
      responseTable <- responseDAO.createTable
      commentTable <- commentDAO.createTable
      manager <- userDAO.upsert(User(None, "manager", "123456", isManager = true))
    } yield (userTable, evaluationTable, questionTable, responseTable, commentTable, manager)
    Await.result(results, Duration.Inf)
    Ok("CREATE TABLE SUCCESSFULLY")
  }
}
