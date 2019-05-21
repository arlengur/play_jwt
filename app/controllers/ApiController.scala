package controllers

import auth.{AuthAction, AuthServiceOffline}
import javax.inject.{Inject, Singleton}
import model.{LoginRequest, User}
import play.api.libs.json.{JsError, Reads}
import play.api.mvc._
import play.api.{Configuration, Logger}
import repositories.InMemoryUserRepository
import repositories.UserRepository.{UserExists, UserNotFound}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

@Singleton
class ApiController @Inject()(cc: ControllerComponents,
                              authAction: AuthAction,
                              authService: AuthServiceOffline,
                              userRepo: InMemoryUserRepository,
                              config: Configuration)
  extends AbstractController(cc) {

  val logger: Logger = Logger(this.getClass)

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok("Home link with free access.")
  }

  def login(): Action[LoginRequest] = Action.async(validateJson[LoginRequest]) { request =>
    val user = request.body
    userRepo.find(user.username, user.password).flatMap { user =>
      createJwt(user, "Now you are logged in.")
    }.recover(requestErrorHandling)
  }

  def register(): Action[LoginRequest] = Action.async(validateJson[LoginRequest]) { request =>
    val user = request.body
    val deviceId = request.headers.get("device-id").getOrElse("deviceId")
    userRepo.add(deviceId, user.username, user.password).flatMap { user =>
      createJwt(user, "Now you are registered.")
    }.recover(requestErrorHandling)
  }

  def media() = authAction {
    Ok("Media resources allowed only with jwt token.")
  }

  private def createJwt(user: User, msg: String): Future[Result] = {
    val (accessToken, refreshToken, aTokenExp) = authService.createJwt(user)
    userRepo.update(user.id, refreshToken).map { _ =>
      Ok(msg).withHeaders(
        "Access-Token" -> accessToken,
        "Refresh-Token" -> refreshToken,
        "Expires-In" -> aTokenExp.toString
      )
    }.recover(requestErrorHandling)
  }

  private def requestErrorHandling: PartialFunction[Throwable, Result] = {
    case ex: UserNotFound =>
      logger.warn("UserNotFound")
      NotFound(ex.getMessage)
    case ex: UserExists =>
      logger.warn("UserExists")
      Conflict(ex.getMessage)
    case NonFatal(e) =>
      logger.error("InternalServerError", e)
      InternalServerError(e.getMessage)
  }

  // This helper parses and validates JSON using the implicit `placeReads`
  // above, returning errors if the parsed json fails validation.
  private def validateJson[A: Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )
}