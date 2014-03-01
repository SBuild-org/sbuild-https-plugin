package org.sbuild.plugins.https

import java.io.File
import de.tototec.sbuild._
import java.net.URL
import org.apache.http.client.CredentialsProvider
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.HttpGet
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

case class BasicAuthCredentials(username: String, password: String)

class HttpsSchemeHandler(downloadDir: File, disableTrustManager: Boolean, basicAuthCredentials: Option[BasicAuthCredentials] = None)(implicit project: Project) extends SchemeResolver {

  private val userAgent = s"SBuild/${SBuildVersion.osgiVersion} (HttpsSchemeHandler)"

  override def localPath(schemeContext: SchemeHandler.SchemeContext): String = s"file:${localFile(schemeContext.path).getPath}"

  override def resolve(schemeContext: SchemeHandler.SchemeContext, targetContext: TargetContext): Unit = {

    val source = url(schemeContext.path)
    val target = localFile(schemeContext.path)

    if (target.exists) {
      // TODO: check file length

    } else {
      val clientBuilder = HttpClientBuilder.create()
      clientBuilder.setUserAgent(userAgent)
      if (disableTrustManager) {
        val trustManager = new X509TrustManager {
          override def checkClientTrusted(xcs: Array[X509Certificate], string: String): Unit = {}
          override def checkServerTrusted(xcs: Array[X509Certificate], string: String): Unit = {}
          override def getAcceptedIssuers(): Array[X509Certificate] = null
        }

        val sslCtx = SSLContext.getInstance("TLS")
        sslCtx.init(null, Array(trustManager), null)
        clientBuilder.setSslcontext(sslCtx)
      }
      if (basicAuthCredentials.isDefined) {
        val credentialsProvider = new BasicCredentialsProvider()
        val authScope = new AuthScope(source.getHost(), source.getPort())
        val creds = basicAuthCredentials.get
        val credentials = new UsernamePasswordCredentials(creds.username, creds.password)
        credentialsProvider.setCredentials(authScope, credentials)
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider)
      }
      val httpClient = clientBuilder.build()
      try {
        val request = new HttpGet(source.toExternalForm())
        val response = httpClient.execute(request)

        val status = response.getStatusLine()
        println(s"Status for URL: ${source}: ${status}")
        status.getStatusCode() match {
          case 404 => throw new RuntimeException(s"No content found (404) at url: ${source}")
          case _ =>
        }

        response.getEntity() match {
          case null => throw new RuntimeException(s"No content found at url: ${source}")
          case entity =>
            target.getParentFile().mkdirs()

            val length = Some(entity.getContentLength()).filter(_ >= 0)
            // TODO: evaluate content length
            // TODO: detect in-the-middle closed streams and re-continue download
            // TODO: retry failed download attempts
            // TODO: log progress
            val inputStream = new BufferedInputStream(entity.getContent())
            val outputStream = new BufferedOutputStream(new FileOutputStream(target))

            try {
              val bufferSize = 1024
              var break = false
              var buffer = new Array[Byte](bufferSize)

              while (!break) {
                inputStream.read(buffer, 0, bufferSize) match {
                  case x if x < 0 => break = true
                  case count => {
                    outputStream.write(buffer, 0, count)
                  }
                }
              }

            } finally {
              inputStream.close()
              outputStream.close()
            }
        }

      } finally {
        httpClient.close()
      }

    }

  }

  def url(path: String): URL = new URL("https:" + path)

  def localFile(path: String): File = {
    url(path)
    // ok, path is a valid URL
    downloadDir / path
  }

}