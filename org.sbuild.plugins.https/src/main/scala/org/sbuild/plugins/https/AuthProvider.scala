package org.sbuild.plugins.https

sealed trait AuthProvider

object AuthProvider {
  case object None extends AuthProvider
  case class BasicAuth(username: String, password: String) extends AuthProvider {
    def this(username: String, password: Array[Char]) = this(username, password.mkString)
  }
}

