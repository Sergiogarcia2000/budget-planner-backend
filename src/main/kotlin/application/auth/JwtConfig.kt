package application.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*

object JwtConfig {

    private val algorithm = Algorithm.HMAC256(EnvironmentHandler.JWT_SECRET)

    val verifier = JWT.require(algorithm)
        .withIssuer(EnvironmentHandler.JWT_ISSUER)
        .build()

    fun validateToken(token: String?): JWTPrincipal? {
        if (token.isNullOrEmpty()) return null

        return try {
            val decodedJWT = verifier.verify(token)
            if (decodedJWT.getClaim("email").asString().isNotEmpty()) {
                JWTPrincipal(decodedJWT)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}