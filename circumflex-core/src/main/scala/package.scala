package ru.circumflex

import java.security.MessageDigest
import java.util.regex.Pattern
import java.util.{UUID, Random}

/*!# The `core` Package

Package `core` contains different shortcuts, utilities and implicits.
You should import this package if you intend to use Circumflex API:

    import ru.circumflex.core._
*/
package object core {

  val CX_LOG = new Logger("ru.circumflex.core")

  /*! Common Circumflex helpers imported into the namespace are:

  * `cx` for accessing Circumflex configuration singleton;
  * `ctx` for accessing the context -- special key-value storage attached
    to currently executing thread;
  * `msg` for accessing localized messages defined in `Messages.properties` (by default).
  */
  val cx = Circumflex
  def ctx = Context.get()
  lazy val msg = cx.instantiate[MessageResolver]("cx.messages", new PropertyFileResolver)

  /*! Circumflex Core package also includes helpers for various common tasks like
  random generation of UUIDs and alphanumeric strings, converting between camelCase and
  underscore_delimited identifiers, measuring execution time, computing popular digests
  (SHA256, MD5), (un)escaping HTML markup, validating JSON input, etc.
  */

  protected val _rnd = new Random
  protected val _CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

  def randomString(length: Int) = (0 until length)
      .map(i => _CHARS.charAt(_rnd.nextInt(_CHARS.length)))
      .mkString
  def randomUUID = UUID.randomUUID.toString

  // Utils

  def camelCaseToUnderscore(arg: String) = arg.replaceAll("(?<!^)([A-Z])","_$1").toLowerCase

  def time[T](block: => T): (Long, T) = {
    val startTime = System.currentTimeMillis
    val result = block
    (System.currentTimeMillis - startTime, result)
  }

  def any2option[T](obj: T): Option[T] = if (obj == null) None else Some(obj)

  def digest(algorithm: String, text: String) = {
    val md = MessageDigest.getInstance(algorithm)
    md.update(text.getBytes)
    md.digest()
        .map(b => Integer.toHexString(0xFF & b))
        .map(b => if (b.length == 1) "0" + b else b)
        .mkString
  }
  def md5(text: String) = digest("md5", text)
  def sha256(text: String) = digest("sha-256", text)

  // Common escaping

  val ampEscape = Pattern.compile("&(?!(?:[a-zA-Z]+|(?:#[0-9]+|#[xX][0-9a-fA-F]+));)")

  def escapeHtml(s: String) = ampEscape.matcher(s)
      .replaceAll("&amp;")
      .replaceAll(">", "&gt;")
      .replaceAll("<", "&lt;")
      .replaceAll("\"", "&quot;")
      .replaceAll("\\r\\n|\\r", "\n")

  def unescapeHtml(s: String) = s
      .replaceAll("&gt;", ">")
      .replaceAll("&lt;", "<")
      .replaceAll("&quot;", "\"")
      .replaceAll("&amp;", "&")
      .replaceAll("\\r\\n|\\r", "\n")

  // JSON validator

  def validateJSON(s: String): Boolean =
    !Pattern.compile("[^,:{}\\[\\]0-9.\\-+Eaeflnr-u \\n\\r\\t]")
        .matcher(s.replaceAll("\"(\\\\.|[^\"\\\\])*\"", ""))
        .find

  // Pimping Symbols

  @inline implicit def symbol2contextVarHelper(sym: Symbol): ContextVarHelper =
    new ContextVarHelper(sym)

}
