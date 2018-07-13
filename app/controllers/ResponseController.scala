package controllers

import dao.ResponseDAO
import javax.inject._
import models.Response
import org.postgresql.util.PSQLException
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

@Singleton
class ResponseController @Inject()(
                                responseDAO: ResponseDAO,
                                cc: ControllerComponents)
                                  (implicit executionContext: ExecutionContext) extends AbstractController(cc) with I18nSupport {


//  def managementResponseList(): Action[AnyContent] = Action.async { implicit request =>
//    responseDAO.list().map(responses => {
//      val responseListJSON = Json.toJson(responses).toString()
//      Ok(views.html.managementResponseList(responseListJSON))
//    })
//  }

  def deleteResponse(id: Long): Action[AnyContent] = Action.async { implicit request =>
    responseDAO.delete(id).map(x => Ok(Json.toJson(x)))
  }

  def upsertResponse(): Action[AnyContent] = Action { implicit request =>
    val responseResult = request.body.asJson.get.validate[Response]
    responseResult match {
      case JsSuccess(response: Response, _) =>
        Try(Await.result(responseDAO.upsert(response), Duration.Inf)) match {
          case Success(u) => Ok(Json.obj("success" -> true, "response" -> u))
          case Failure(e: PSQLException) => e.getSQLState match {
            case "23505" => Ok(Json.obj("success" -> false, "uniqueViolation" -> true))
            case _ => Ok(Json.obj("success" -> false))
          }
        }
      case e: JsError => Ok(JsError.toJson(e))
    }
  }

  def createTable() = Action {
    Await.result(responseDAO.createTable, Duration.Inf)
    Ok("Create Table")
  }

  def responseList(): Action[AnyContent] = Action.async { implicit request =>
    responseDAO.list().map(response => Ok(Json.toJson(response)))
  }
}
