package de.choffmeister.auth.spray

import java.util.Date
import java.util.concurrent.TimeUnit

import de.choffmeister.auth.common._
import org.specs2.mutable._
import spray.http._

import scala.concurrent._

class OAuth2BearerTokenHttpAuthenticatorSpec extends Specification {
  def time(delta: Long) = new Date((System.currentTimeMillis + delta) / 1000L * 1000L)

  "OAuth2BearerTokenHttpAuthenticator" should {
    "work" in {
      val sec1 = "secret".getBytes("ASCII")
      val sec2 = "123".getBytes("ASCII")
      val auth = new OAuth2BearerTokenHttpAuthenticator[String]("test", sec1, sub ⇒ Future(Some(sub)))

      val token1 = JsonWebToken(subject = "tom", expiresAt = time(+10000))
      authenticate(auth, token1, sec1) === Some("tom")
      authenticate(auth, token1, sec2) === None

      val token2 = JsonWebToken(subject = "tom2", expiresAt = time(-10000))
      authenticate(auth, token2, sec1) === None
      authenticate(auth, token2, sec2) === None
    }
  }

  def authenticate[U](authenticator: OAuth2BearerTokenHttpAuthenticator[U], token: JsonWebToken, secret: Array[Byte]): Option[U] = {
    val creds = Some(OAuth2BearerToken(JsonWebToken.write(token, secret)))
    Await.result(authenticator.authenticate(creds, null), duration.Duration(1, TimeUnit.SECONDS))
  }
}
