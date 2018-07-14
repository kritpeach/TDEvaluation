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
                                    cc: ControllerComponents)
                                  (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  val responseForm = Form(
    tuple(
      "responseId" -> optional(longNumber),
      "questionId" -> longNumber,
      "answer" -> nonEmptyText
    )
  )
  //  def managementResponseList(): Action[AnyContent] = Action.async { implicit request =>
  //    responseDAO.list().map(responses => {
  //      val responseListJSON = Json.toJson(responses).toString()
  //      Ok(views.html.managementResponseList(responseListJSON))
  //    })
  //  }

  def deleteResponse(id: Long): Action[AnyContent] = Action.async { implicit request =>
    responseDAO.delete(id).map(x => Ok(Json.toJson(x)))
  }

  def upsertResponse(): Action[AnyContent] = Action.async { implicit request =>
    val (responseId: Option[Long], questionId: Long, answer: String) = responseForm.bindFromRequest.get
    val creatorId: Long = request.session.get("uid").get.toLong
    val response: Response = Response(id = responseId, answer = answer, creatorId = creatorId, questionId = questionId)
    val upsertedResponse = Await.result(responseDAO.upsert(response), Duration.Inf)
    val question = Await.result(questionDAO.getById(questionId), Duration.Inf)
    questionDAO.getNextQuestion(upsertedResponse.questionId, question.get.evaluationId).map({
      case Some(q) => Redirect(routes.QuestionController.askQuestion(q.id.get))
      case None => Ok("No more question")
    })
  }

  def createTable() = Action {
    Await.result(responseDAO.createTable, Duration.Inf)
    Ok("Create Table")
  }

  def responseList(): Action[AnyContent] = Action.async { implicit request =>
    responseDAO.list().map(response => Ok(Json.toJson(response)))
  }
}
