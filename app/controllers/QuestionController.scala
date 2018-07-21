package controllers

import dao.{EvaluationDAO, QuestionDAO, ResponseDAO}
import javax.inject._
import models.{Question, Response}
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
                                    responseDAO: ResponseDAO,
                                    authenticatedManagerAction: AuthenticatedManagerAction,
                                    authenticatedAssessorAction: AuthenticatedAssessorAction,
                                    cc: ControllerComponents)
                                  (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def managementQuestionList(evaluationId: Long): Action[AnyContent] = authenticatedManagerAction { implicit request =>
    val questionListJSON = Await.result(questionDAO.list(evaluationId).map(questions => Json.toJson(questions).toString),Duration.Inf)
    val evaluation = Await.result(evaluationDAO.getById(evaluationId),Duration.Inf).get
    Ok(views.html.managementQuestionList(questionListJSON, evaluation))
  }

  def deleteQuestion(id: Long): Action[AnyContent] = authenticatedManagerAction.async { implicit request =>
    questionDAO.delete(id).map(x => Ok(Json.toJson(x)))
  }

  def upsertQuestion(): Action[AnyContent] = authenticatedManagerAction { implicit request =>
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

  def createTable(): Action[AnyContent] = authenticatedManagerAction.async {
    questionDAO.createTable.map(_ => Ok("Created Table"))
  }

  def askQuestion(id: Long): Action[AnyContent] = authenticatedAssessorAction.async { implicit request =>
    val uid: Long = request.session.get("uid").get.toLong
    val existingResponse: Option[Response] = Await.result(responseDAO.get(uid, id), Duration.Inf)
    questionDAO.getById(id).map(question => Ok(views.html.askQuestion(question.get, existingResponse)))
  }
}
