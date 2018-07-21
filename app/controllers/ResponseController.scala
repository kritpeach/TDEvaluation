package controllers

import dao.{QuestionDAO, ResponseDAO}
import javax.inject._
import models.Response
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration

@Singleton
class ResponseController @Inject()(
                                    responseDAO: ResponseDAO,
                                    questionDAO: QuestionDAO,
                                    authenticatedManagerAction: AuthenticatedManagerAction,
                                    authenticatedAssessorAction: AuthenticatedAssessorAction,
                                    cc: ControllerComponents)
                                  (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  val responseForm = Form(
    tuple(
      "responseId" -> optional(longNumber),
      "questionId" -> longNumber,
      "answer" -> nonEmptyText
    )
  )

  def upsertResponse(): Action[AnyContent] = authenticatedAssessorAction.async { implicit request =>
    val (responseId: Option[Long], questionId: Long, answer: String) = responseForm.bindFromRequest.get
    val creatorId: Long = request.session.get("uid").get.toLong
    val response: Response = Response(id = responseId, answer = answer, creatorId = creatorId, questionId = questionId)
    val upsertedResponse = Await.result(responseDAO.upsert(response), Duration.Inf)
    val question = Await.result(questionDAO.getById(questionId), Duration.Inf)
    questionDAO.getNextQuestion(upsertedResponse.questionId, question.get.evaluationId).map({
      case Some(q) => Redirect(routes.QuestionController.askQuestion(q.id.get))
      case None => Redirect(routes.ResponseController.complete())
    })
  }

  def complete() = authenticatedAssessorAction { implicit request => Ok(views.html.complete()) }

  def createTable(): Action[AnyContent] = authenticatedManagerAction.async {
    responseDAO.createTable.map(_ => Ok("Created Table"))
  }
}
