package com.codahale.metrics.health

/**
 * The result of a {@link HealthCheck} being run. It can be healthy (with an optional message)
 * or unhealthy (with either an error message or a thrown exception).
 */
object Result {
  private val HEALTHY = new Result(true, null, null)

  /**
   * Returns a healthy {@link Result} with no additional message.
   *
   * @return a healthy { @link Result} with no additional message
   */
  def healthy: Result = HEALTHY

  /**
   * Returns a healthy {@link Result} with an additional message.
   *
   * @param message an informative message
   * @return a healthy { @link Result} with an additional message
   */
  def healthy(message: String) = Result(healthy = true, message, null)

  /**
   * Returns a healthy {@link Result} with a formatted message.
   * <p/>
   * Message formatting follows the same rules as {@link String#format(String, Object...)}.
   *
   * @param message a message format
   * @param args    the arguments apply to the message format
   * @return a healthy { @link Result} with an additional message
   * @see String#format(String, Object...)
   */
  @deprecated("is this needed?", "3.2.1")
  def healthy(message: String, args: AnyRef*): Result = healthy(String.format(message, args))

  /**
   * Returns an unhealthy {@link Result} with the given message.
   *
   * @param message an informative message describing how the health check failed
   * @return an unhealthy { @link Result} with the given message
   */
  def unhealthy(message: String) = Result(healthy = false, message, null)

  /**
   * Returns an unhealthy {@link Result} with a formatted message.
   * <p/>
   * Message formatting follows the same rules as {@link String#format(String, Object...)}.
   *
   * @param message a message format
   * @param args    the arguments apply to the message format
   * @return an unhealthy { @link Result} with an additional message
   * @see String#format(String, Object...)
   */
  @deprecated("is this needed?", "3.2.1")
  def unhealthy(message: String, args: AnyRef*): Result = unhealthy(String.format(message, args))

  /**
   * Returns an unhealthy {@link Result} with the given error.
   *
   * @param error an exception thrown during the health check
   * @return an unhealthy { @link Result} with the given error
   */
  def unhealthy(error: Throwable) = Result(healthy = false, error.getMessage, error)
}

case class Result(healthy: Boolean, message: String, error: Throwable) {

  /**
   * Returns {@code true} if the result indicates the component is healthy; {@code false}
   * otherwise.
   *
   * @return { @code true} if the result indicates the component is healthy
   */
  @deprecated("is this needed?", "3.2.1")
  def isHealthy: Boolean = healthy

  /**
   * Returns any additional message for the result, or {@code null} if the result has no
   * message.
   *
   * @return any additional message for the result, or { @code null}
   */
  @deprecated("is this needed?", "3.2.1")
  def getMessage: String = message

  /**
   * Returns any exception for the result, or {@code null} if the result has no exception.
   *
   * @return any exception for the result, or { @code null}
   */
  @deprecated("is this needed?", "3.2.1")
  def getError: Throwable = error

  override def toString: String = {
    val builder = new StringBuilder("Result{isHealthy=")
    builder.append(healthy)
    if (message != null) builder.append(", message=").append(message)
    if (error != null) builder.append(", error=").append(error)
    builder.append('}')
    builder.toString()
  }
}

/**
 * A health check for a component of your application.
 */
trait HealthCheck {

  /**
   * Perform a check of the application component.
   *
   * @return if the component is healthy, a healthy { @link Result}; otherwise, an unhealthy { @link
   *         Result} with a descriptive error message or exception
   * @throws Exception if there is an unhandled error during the health check; this will result in
   *                   a failed health check
   */
  @throws(classOf[Exception])
  protected def check(): Result

  /**
   * Executes the health check, catching and handling any exceptions raised by {@link #check()}.
   *
   * @return if the component is healthy, a healthy { @link Result}; otherwise, an unhealthy { @link
   *         Result} with a descriptive error message or exception
   */
  def execute: Result = {
    try check()
    catch {
      case e: Exception => Result.unhealthy(e)
    }
  }
}
