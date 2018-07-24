package controllers

import dao.{CommentDAO, EvaluationDAO, QuestionDAO, ResponseDAO}
import javax.inject._
import models.{Comment, Question, Response}
import org.postgresql.util.PSQLException
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

@Singleton
class QuestionController @Inject()(
                                    questionDAO: QuestionDAO,
                                    evaluationDAO: EvaluationDAO,
                                    responseDAO: ResponseDAO,
                                    commentDAO: CommentDAO,
                                    authenticatedManagerAction: AuthenticatedManagerAction,
                                    authenticatedAssessorAction: AuthenticatedAssessorAction,
                                    cc: ControllerComponents)
                                  (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def managementQuestionList(evaluationId: Long): Action[AnyContent] = authenticatedManagerAction { implicit request =>
    val questionListJSON = Await.result(questionDAO.list(evaluationId).map(questions => Json.toJson(questions).toString), Duration.Inf)
    val evaluation = Await.result(evaluationDAO.getById(evaluationId), Duration.Inf).get
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

  def askQuestion(id: Long): Action[AnyContent] = authenticatedAssessorAction { implicit request =>
    // TODO: Improve performance
    val uid: Long = request.session.get("uid").get.toLong
    val futures: Future[(Option[Response], Option[Question])] = for {
      response <- responseDAO.get(uid, id)
      question <- questionDAO.getById(id)
    } yield (response, question)
    val (response: Option[Response], question) = Await.result(futures, Duration.Inf)
    val comment: Option[Comment] = response match {
      case Some(r) => Await.result(commentDAO.get(uid, r.id.get), Duration.Inf)
      case None => None
    }
    Ok(views.html.askQuestion(question.get, response, comment))
  }
}
