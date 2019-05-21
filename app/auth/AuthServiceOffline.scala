package auth

import javax.inject.Inject
import model.User
import pdi.jwt._
import play.api.Configuration

import scala.util.Try

class AuthServiceOffline @Inject()(config: Configuration) {
  // Secret key
  private def key = config.get[String]("auth.key")
  // Secret key
  private def aTokenExp = config.get[Long]("auth.accessTokenExpSec")
  // Secret key
  private def rTokenExp = config.get[Long]("auth.refreshTokenExpSec")
  // Validates a JWT and  returns the claims
  def validateJwt(token: String): Try[JwtClaim] =
    JwtJson.decode(token, key, Seq(JwtAlgorithm.HS256)) // Decode the token using the secret key

  def createJwt(user: User, now: Long = JwtTime.nowSeconds): (String, String, Long) = {
    val accessClaim = JwtClaim(issuedAt = Option(now), expiration = Option(now + aTokenExp))
    val accessToken = Jwt.encode(accessClaim, key, JwtAlgorithm.HS256)

    val refreshClaim = JwtClaim(issuer = Option(user.id.toString), issuedAt = Option(now), expiration = Option(now + rTokenExp))
    val refreshToken = Jwt.encode(refreshClaim, key, JwtAlgorithm.HS256)

    (accessToken, refreshToken, now + aTokenExp)
  }
}