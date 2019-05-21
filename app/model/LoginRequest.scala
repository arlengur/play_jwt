package model

import play.api.libs.json.Json

final case class LoginRequest(username: String, password: String)

object LoginRequest {
  // We're going to be serving this type as JSON, so specify a default Json formatter for our LoginRequest type here
  implicit val format = Json.format[LoginRequest]
}
