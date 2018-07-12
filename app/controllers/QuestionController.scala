package controllers

import dao.{EvaluationDAO, QuestionDAO}
import javax.inject._
import models.Question
import org.postgresql.util.PSQLException
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

@Singleton
class QuestionController @Inject()(
                                    questionDAO: QuestionDAO,
                                    evaluationDAO: EvaluationDAO,
                                    cc: ControllerComponents)
                                  (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def managementQuestionList(evaluationId: Long): Action[AnyContent] = Action.async { implicit request =>
    questionDAO.list(evaluationId).map(questions => {
      val questionListJSON = Json.toJson(questions).toString()
      Await.result(evaluationDAO.getById(evaluationId), Duration.Inf) match {
        case Some(evaluation) => Ok(views.html.managementQuestionList(questionListJSON, evaluation))
        case None => ???
      }
    })
  }

  def deleteQuestion(id: Long): Action[AnyContent] = Action.async { implicit request =>
    questionDAO.delete(id).map(x => Ok(Json.toJson(x)))
  }

  def upsertQuestion(): Action[AnyContent] = Action { implicit request =>
    val question = request.body.asJson.get.as[JsObject]
    question.validate[Question] match {
      case JsSuccess(question: Question, _) => Try(Await.result(questionDAO.upsert(question), Duration.Inf)) match {
        case Success(s) => Ok(Json.obj("success" -> true, "question" -> s))
        case Failure(e: PSQLException) => e.getSQLState match {
          case "23505" => Ok(Json.obj("success" -> false, "uniqueViolation" -> true))
          case _ => Ok(Json.obj("success" -> false))
        }
      }
      case JsError(e) => Ok(JsError.toJson(e))
    }
  }

  def createTable() = Action {
    Await.result(questionDAO.createTable, Duration.Inf)
    Ok("Create Table")
  }

  def questionList(): Action[AnyContent] = Action.async { implicit request =>
    questionDAO.list().map(question => Ok(Json.toJson(question)))
  }
}
