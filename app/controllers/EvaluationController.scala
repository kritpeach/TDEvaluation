package controllers

import dao.EvaluationDAO
import javax.inject._
import models.Evaluation
import org.postgresql.util.PSQLException
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, JsSuccess, Json}
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
    val evaluationResult = request.body.asJson.get.validate[Evaluation]
    println("xxxxxxxxxxxx" + request.session.get("uid"))
    val uid: Long = request.session.get("uid").get.toLong
    evaluationResult match {
      case JsSuccess(evaluation: Evaluation, _) =>
        evaluation.id match {
          case Some(id) => ???
          case None => Try(Await.result(evaluationDAO.upsert(evaluation.copy(creator = uid)), Duration.Inf)) match {
            case Success(s) => Ok(Json.obj("success" -> true, "evaluation" -> s))
            case Failure(e: PSQLException) => e.getSQLState match {
              case "23505" => Ok(Json.obj("success" -> false, "uniqueViolation" -> true))
              case _ => Ok(Json.obj("success" -> false))
            }
          }
        }
      case e: JsError => Ok(JsError.toJson(e))
    }
  }

  def createTable() = Action {
    Await.result(evaluationDAO.createTable, Duration.Inf)
    Ok("Create Table")
  }

  def evaluationList(): Action[AnyContent] = Action.async { implicit request =>
    evaluationDAO.list.map(evaluation => Ok(Json.toJson(evaluation)))
  }
}
