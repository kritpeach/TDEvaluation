package controllers

import dao.{EvaluationDAO, QuestionDAO, UserDAO}
import javax.inject._
import models.Evaluation
import org.postgresql.util.PSQLException
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext

@Singleton
class EvaluationController @Inject()(
                                      evaluationDAO: EvaluationDAO,
                                      questionDAO: QuestionDAO,
                                      userDAO: UserDAO,
                                      cc: ControllerComponents)
                                    (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def managementEvaluationList(): Action[AnyContent] = Action.async { implicit request =>
    evaluationDAO.list().map(evaluations => {
      val evaluationListJSON = Json.toJson(evaluations).toString()
      Ok(views.html.managementEvaluationList(evaluationListJSON))
    })
  }

  def deleteEvaluation(id: Long): Action[AnyContent] = Action.async { implicit request =>
    evaluationDAO.delete(id).map(x => Ok(Json.toJson(x)))
  }

  def upsertEvaluation(): Action[AnyContent] = Action { implicit request =>
    val uid: Long = request.session.get("uid").get.toLong
    val evaluation = request.body.asJson.get.as[JsObject] + ("creator" -> JsNumber(uid))
    evaluation.validate[Evaluation] match {
      case JsSuccess(evaluation: Evaluation, _) => Try(Await.result(evaluationDAO.upsert(evaluation.copy(creator = uid)), Duration.Inf)) match {
        case Success(s) => Ok(Json.obj("success" -> true, "evaluation" -> s))
        case Failure(e: PSQLException) => e.getSQLState match {
          case "23505" => Ok(Json.obj("success" -> false, "uniqueViolation" -> true))
          case _ => Ok(Json.obj("success" -> false))
        }
      }
      case JsError(e) => Ok(JsError.toJson(e))
    }
  }

  def createTable() = Action {
    Await.result(evaluationDAO.createTable, Duration.Inf)
    Ok("Create Table")
  }

  def evaluationList(): Action[AnyContent] = Action.async { implicit request =>
    evaluationDAO.list().map(evaluation => Ok(Json.toJson(evaluation)))
  }

  def assessorEvaluation(): Action[AnyContent] = Action.async { implicit request =>
    evaluationDAO.list(true).map(evaluationList => Ok(views.html.evaluation(evaluationList)))
  }

  def evaluation(id: Long): Action[AnyContent] = Action.async { implicit request =>
    questionDAO.getFirst(id).map(question => Redirect(routes.QuestionController.askQuestion(question.get.id.get)))
  }

  def managementEvaluationView(id: Long): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val evaluation = Await.result(evaluationDAO.getById(id), Duration.Inf).get
    evaluationDAO.userResponseCount(id).map(responseCount => Ok(views.html.managementEvaluationView(evaluation, responseCount)))
  }

  def report(): Action[AnyContent] = Action.async {
    evaluationDAO.questionResponseJSON(1, 56).map(s => Ok(s.get))
  }

  def managementEvaluationResponse(evaluationId: Long, userId: Long): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val evaluation = Await.result(evaluationDAO.getById(evaluationId), Duration.Inf).get
    val user = Await.result(userDAO.getById(userId), Duration.Inf).get
    evaluationDAO.questionResponseJSON(evaluationId, userId).map(questionResponseJSON => Ok(views.html.managementEvaluationResponse(user,evaluation,questionResponseJSON.get)))
  }
}
