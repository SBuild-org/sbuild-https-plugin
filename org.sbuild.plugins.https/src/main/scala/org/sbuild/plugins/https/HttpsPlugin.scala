package org.sbuild.plugins.https

import de.tototec.sbuild._

class HttpsPlugin(implicit project: Project) extends Plugin[Https] {

  override def create(name: String): Https = {
    val schemeName = if (name == "") "https" else name
    Https(
      schemeName = schemeName,
      downloadDir = Path(".sbuild") / schemeName
    )
  }

  override def applyToProject(instances: Seq[(String, Https)]): Unit = instances.foreach {
    case (name, https) =>
      SchemeHandler(https.schemeName, new HttpsSchemeHandler(https.downloadDir, https.disableTrustManager))
  }
}