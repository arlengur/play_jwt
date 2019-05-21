package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.http.HeaderNames
import play.api.http.Status.{BAD_REQUEST, CONFLICT}
import play.api.test.Helpers._
import play.api.test._

class ApiControllerTest extends PlaySpec with GuiceOneAppPerTest {
  val failJson = """{"u2sername": "Bob", "passwhord": "pass1"}"""
  val user = """{"username": "Bob", "password": "pass1"}"""
  val newUser = """{"username": "Bob1", "password": "pass2"}"""

  "Application" should {
    "reject requests without device-id" in {
      val Some(response) = route(app, FakeRequest(GET, "/"))
      status(response) mustEqual UNAUTHORIZED
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("Device-id has not been sent.")
    }

    "render the index page" in {
      val Some(response) = route(app, FakeRequest(GET, "/").withHeaders("device-id" -> "device-id"))
      status(response) mustEqual OK
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("Home link with free access.")
    }

    "reject login without device-id" in {
      val req = FakeRequest(POST, "/login")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withBody(user)
      val Some(response) = route(app, req)
      status(response) mustEqual UNAUTHORIZED
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("Device-id has not been sent.")
    }

    "reject login if user with such name or password not found" in {
      val req = FakeRequest(POST, "/login")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withHeaders("device-id" -> "device-id")
        .withBody(newUser)
      val Some(response) = route(app, req)
      status(response) mustEqual NOT_FOUND
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("User with such name or password not found.")
    }

    "successful login" in {
      val req = FakeRequest(POST, "/login")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withHeaders("device-id" -> "device-id")
        .withBody(user)
      val Some(response) = route(app, req)
      status(response) mustEqual OK
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("Now you are logged in.")
    }

    "fail JSON parse" in {
      val req = FakeRequest(POST, "/login")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withHeaders("device-id" -> "device-id")
        .withBody(failJson)
      val Some(response) = route(app, req)
      status(response) mustEqual BAD_REQUEST
      contentType(response) mustBe Some(JSON)
      contentAsString(response) must (include("username") and include("password"))
    }

    "reject register without device-id" in {
      val req = FakeRequest(POST, "/register")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withBody(user)
      val Some(response) = route(app, req)
      status(response) mustEqual UNAUTHORIZED
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("Device-id has not been sent.")
    }

    "reject register if user already exists" in {
      val req = FakeRequest(POST, "/register")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withHeaders("device-id" -> "device-id")
        .withBody(user)
      val Some(response) = route(app, req)
      status(response) mustEqual CONFLICT
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("User with such name already exists.")
    }

    "successful register" in {
      val req = FakeRequest(POST, "/register")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withHeaders("device-id" -> "device-id")
        .withBody(newUser)
      val Some(response) = route(app, req)
      status(response) mustEqual OK
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("Now you are registered.")
    }


    "reject get media without device-id" in {
      val accessToken = Jwt.encode(JwtClaim(), "super_secret_key", JwtAlgorithm.HS256)

      val req = FakeRequest(GET, "/media")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withHeaders(HeaderNames.AUTHORIZATION -> s"Bearer $accessToken")
        .withBody(user)
      val Some(response) = route(app, req)
      status(response) mustEqual UNAUTHORIZED
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("Device-id has not been sent.")
    }

    "reject get media without token" in {
      val req = FakeRequest(GET, "/media")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withHeaders("device-id" -> "device-id")
        .withBody(user)
      val Some(response) = route(app, req)
      status(response) mustEqual UNAUTHORIZED
    }

    "reject get media with expired token" in {
      val accessToken = Jwt.encode(JwtClaim().expiresIn(-1), "super_secret_key", JwtAlgorithm.HS256)

      val req = FakeRequest(GET, "/media")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withHeaders("device-id" -> "device-id")
        .withHeaders(HeaderNames.AUTHORIZATION -> s"Bearer $accessToken")
        .withBody(user)
      val Some(response) = route(app, req)
      status(response) mustEqual UNAUTHORIZED
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("The token is expired")
    }

    "successful get media" in {
      val accessToken = Jwt.encode(JwtClaim(), "super_secret_key", JwtAlgorithm.HS256)

      val req = FakeRequest(GET, "/media")
        .withHeaders(CONTENT_TYPE -> JSON)
        .withHeaders("device-id" -> "device-id")
        .withHeaders(HeaderNames.AUTHORIZATION -> s"Bearer $accessToken")
        .withBody(user)
      val Some(response) = route(app, req)
      status(response) mustEqual OK
      contentType(response) mustBe Some(TEXT)
      contentAsString(response) must include("Media resources allowed only with jwt token.")
    }
  }
}
