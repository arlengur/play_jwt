package model

import play.api.libs.json.Json

// Create our User type as a standard case class
case class User(id: Long,
                deviceId: String,
                name: Option[String] = None,
                password: Option[String] = None,
                refreshToken: Option[String] = None
               )

object User {
  // We're going to be serving this type as JSON, so specify a
  // default Json formatter for our User type here
  implicit val format = Json.format[User]
}