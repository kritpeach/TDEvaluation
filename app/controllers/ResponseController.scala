package controllers

import dao.{CommentDAO, QuestionDAO, ResponseDAO}
import javax.inject._
import models.{Comment, Question, Response}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.mvc._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

@Singleton
class ResponseController @Inject()(
                                    responseDAO: ResponseDAO,
                                    questionDAO: QuestionDAO,
                                    commentDAO: CommentDAO,
                                    authenticatedManagerAction: AuthenticatedManagerAction,
                                    authenticatedAssessorAction: AuthenticatedAssessorAction,
                                    cc: ControllerComponents)
                                  (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  val responseForm = Form(
    tuple(
      "responseId" -> optional(longNumber),
      "questionId" -> longNumber,
      "answer" -> nonEmptyText,
      "reason" -> nonEmptyText,
      "commentId" -> optional(longNumber)
    )
  )

  def upsertResponse(): Action[AnyContent] = authenticatedAssessorAction.async { implicit request =>
    // TODO: Improve performance
    val (responseId: Option[Long], questionId: Long, answer: String, reason: String, commentId: Option[Long]) = responseForm.bindFromRequest.get
    val creatorId: Long = request.session.get("uid").get.toLong
    val futures: Future[(Response, Comment, Option[Question])] = for {
      upsertedResponse <- responseDAO.upsert(Response(id = responseId, answer = answer, creatorId = creatorId, questionId = questionId))
      upsertedComment <- commentDAO.upsert(Comment(commentId, reason, creatorId, responseId.get))
      question <- questionDAO.getById(questionId)
    } yield (upsertedResponse, upsertedComment, question)
    val (_, _, question) = Await.result(futures, Duration.Inf)
    questionDAO.getNextQuestion(question.get).map({
      case Some(q) => Redirect(routes.QuestionController.askQuestion(q.id.get))
      case None => Redirect(routes.ResponseController.complete())
    })
  }

  def complete() = authenticatedAssessorAction { implicit request => Ok(views.html.complete()) }

  def createTable(): Action[AnyContent] = authenticatedManagerAction.async {
    responseDAO.createTable.map(_ => Ok("Created Table"))
  }
}
