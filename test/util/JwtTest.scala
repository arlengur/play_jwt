package util

import auth.AuthServiceOffline
import model.User
import org.scalatestplus.play.PlaySpec
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import play.api.inject.guice.GuiceApplicationBuilder

class JwtTest extends PlaySpec {
  val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.e30.YAdN9DvsRyz2iG5Wzzc3qnQVj8TU0CsQoASNl2IEpPY"

  "Jwt" should {
    "be able to create token" in {
      val accessToken = Jwt.encode(JwtClaim(), "key", JwtAlgorithm.HS256)
      token mustEqual accessToken
    }

    "create valid access and refresh tokens" in {
      val application = new GuiceApplicationBuilder().build()
      val auth = new AuthServiceOffline(application.configuration)

      val user = User(1, "device-id", Option("Bob"), Option("pass1"))
      val (aJwt, rJwt, _) = auth.createJwt(user)

      auth.validateJwt(aJwt).isSuccess mustEqual true
      auth.validateJwt(rJwt).isSuccess mustEqual true
    }
  }
}
