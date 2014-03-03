package org.sbuild.plugins.https

sealed trait AuthProvider

object AuthProvider {
  case object None extends AuthProvider

  object BasicAuth {
    def apply(username: String, password: Array[Char]): BasicAuth = BasicAuth(username, password.mkString)
  }
  case class BasicAuth(username: String, password: String) extends AuthProvider
}

