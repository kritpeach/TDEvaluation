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
                                   cc: ControllerComponents)
                                 (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {
  /*
    def managementCommentList(): Action[AnyContent] = Action.async { implicit request =>
      commentDAO.list().map(comments => {
        val commentListJSON = Json.toJson(comments).toString()
        Ok(views.html.managementCommentList(commentListJSON))
      })
    }
  */
  def deleteComment(id: Long): Action[AnyContent] = Action.async { implicit request =>
    commentDAO.delete(id).map(x => Ok(Json.toJson(x)))
  }

  def upsertComment(): Action[AnyContent] = Action { implicit request =>
    val uid: Long = request.session.get("uid").get.toLong
    val comment = request.body.asJson.get.as[JsObject] + ("userId" -> JsNumber(uid))
    comment.validate[Comment] match {
      case JsSuccess(comment: Comment, _) =>
        Try(Await.result(commentDAO.upsert(comment), Duration.Inf)) match {
          case Success(u) => Ok(Json.obj("success" -> true, "comment" -> u))
          case Failure(e: PSQLException) => e.getSQLState match {
            case "23505" => Ok(Json.obj("success" -> false, "uniqueViolation" -> true))
            case _ => Ok(Json.obj("success" -> false))
          }
        }
      case e: JsError => Ok(JsError.toJson(e))
    }
  }

  def createTable() = Action {
    Await.result(commentDAO.createTable, Duration.Inf)
    Ok("Create Table")
  }

  def commentList(): Action[AnyContent] = Action.async { implicit request =>
    commentDAO.list().map(comment => Ok(Json.toJson(comment)))
  }
}
