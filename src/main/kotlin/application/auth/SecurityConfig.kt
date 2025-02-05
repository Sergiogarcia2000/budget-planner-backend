package application.auth

import EnvironmentHandler
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

object SecurityConfig {
    const val AUTH_JWT = "auth-jwt"

    private val algorithm = Algorithm.HMAC256(EnvironmentHandler.JWT_SECRET)

    fun generateToken(userId: Int, email: String): String {
        return JWT.create()
            .withIssuer(EnvironmentHandler.JWT_ISSUER)
            .withAudience(EnvironmentHandler.JWT_AUDIENCE)
            .withClaim("id", userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000)) // 1H
            .sign(algorithm)
    }

    fun Application.configureSecurity() {
        install(Authentication) {
            jwt(AUTH_JWT) {
                realm = EnvironmentHandler.JWT_REALM
                verifier(JwtConfig.verifier)

                validate { credential ->
                    if (!credential.payload.getClaim("email").asString().isNullOrEmpty()) {
                        JWTPrincipal(credential.payload)
                    } else null
                }
            }
        }
    }

}