package controllers

import dao.UserDAO
import javax.inject.Inject
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class AuthenticatedAssessorAction @Inject()(parser: BodyParsers.Default, val userDAO: UserDAO)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    request.session.get("uid") match {
      case None => Future.successful(Unauthorized(views.html.error("401","Forbidden","You’re not signed in.")))
      case Some(uid) =>
        Await.result(userDAO.getById(uid.toLong).map(user => user.get.isManager).map({
          case false => block(request)
          case _ => Future.successful(Forbidden(views.html.error("403","Unauthorized","You're not assessor")))
        }), Duration.Inf)
    }
  }
}
