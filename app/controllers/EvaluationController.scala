package controllers

import dao.EvaluationDAO
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

  def toEvaluation: Action[AnyContent] = Action { implicit request =>
    Ok("toEvaluation")
  }
}
