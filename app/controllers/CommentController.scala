package controllers

import dao.CommentDAO
import javax.inject._
import models.Comment
import org.postgresql.util.PSQLException
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

@Singleton
class CommentController @Inject()(
                                   commentDAO: CommentDAO,
                                   cc: ControllerComponents,
                                   authenticatedManagerAction: AuthenticatedManagerAction)
                                 (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def deleteComment(id: Long): Action[AnyContent] = authenticatedManagerAction.async { implicit request =>
    commentDAO.delete(id).map(x => Ok(Json.toJson(x)))
  }

  def upsertComment(): Action[AnyContent] = authenticatedManagerAction { implicit request =>
    val uid: Long = request.session.get("uid").get.toLong
    val comment = request.body.asJson.get.as[JsObject] + ("userId" -> JsNumber(uid))
    comment.validate[Comment] match {
      case JsSuccess(comment: Comment, _) =>
        Try(Await.result(commentDAO.upsert(comment), Duration.Inf)) match {
          case Success(s) => Ok(Json.obj("success" -> true, "comment" -> s))
          case Failure(e: PSQLException) => e.getSQLState match {
            case "23505" => Ok(Json.obj("success" -> false, "uniqueViolation" -> true))
            case _ => Ok(Json.obj("success" -> false))
          }
        }
      case e: JsError => Ok(JsError.toJson(e))
    }
  }

  def createTable(): Action[AnyContent] = authenticatedManagerAction.async {
    commentDAO.createTable.map(_ => Ok("Create Table"))
  }

  def commentList(): Action[AnyContent] = authenticatedManagerAction.async { implicit request =>
    commentDAO.list().map(comment => Ok(Json.toJson(comment)))
  }
}
