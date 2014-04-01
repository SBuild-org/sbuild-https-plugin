package org.sbuild.plugins.https

import java.io.File

/**
 * Configuration for the SBuild Https Plugin.
 *
 * Based on it's configuration, this plugin will register a `[[de.tototec.sbuild.SchemeHandler SchemeHandler]]` supporting the `HTTPS` protocol.
 *
 * @param schemeName The Name of the registered scheme.
 *   Most typical is `https`.
 * @param downloadDir The directory, where downloaded resources will be stored.
 * @param disableTrustManager If `true`, no certifate checking will be done.
 *   This option is unsafe as it's enables undetected Man-in-the-middle attacks, but might be helpful for testing, e.g. with sites with self signed certificates.
 * @param basicAuthCredentials If defined, will be used for basic auth againts the https service.
 */
case class Https(
  schemeName: String,
  downloadDir: File,
  disableTrustManager: Boolean = false,
  authProvider: AuthProvider = AuthProvider.None)
