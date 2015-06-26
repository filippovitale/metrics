package com.codahale.metrics.health

import java.util
import java.util.concurrent.{Callable, Future, ExecutorService}
import java.util.{Collections, NoSuchElementException}

import org.slf4j.LoggerFactory

class HealthCheckRegistry {
  private val LOGGER = LoggerFactory.getLogger(classOf[HealthCheckRegistry])

  private val healthChecks = new util.concurrent.ConcurrentHashMap[String, HealthCheck]

  /**
   * Registers an application {@link HealthCheck}.
   *
   * @param name        the name of the health check
   * @param healthCheck the { @link HealthCheck} instance
   */
  def register(name: String, healthCheck: HealthCheck): Unit = healthChecks.putIfAbsent(name, healthCheck)

  /**
   * Unregisters the application {@link HealthCheck} with the given name.
   *
   * @param name the name of the { @link HealthCheck} instance
   */
  def unregister(name: String): Unit = healthChecks.remove(name)

  /**
   * Returns a set of the names of all registered health checks.
   *
   * @return the names of all registered health checks
   */
  def getNames: util.SortedSet[String] = util.Collections.unmodifiableSortedSet(new util.TreeSet[String](healthChecks.keySet))

  /**
   * Runs the health check with the given name.
   *
   * @param name    the health check's name
   * @return the result of the health check
   * @throws NoSuchElementException if there is no health check with the given name
   */
  @throws(classOf[NoSuchElementException])
  def runHealthCheck(name: String): Result = {
    val healthCheck: HealthCheck = healthChecks.get(name)
    if (healthCheck == null) throw new NoSuchElementException("No health check named " + name + " exists")
    healthCheck.execute
  }

  /**
   * Runs the registered health checks and returns a map of the results.
   *
   * @return a map of the health check results
   */
  def runHealthChecks: util.SortedMap[String, Result] = {
    val results: util.SortedMap[String, Result] = new util.TreeMap[String, Result]
    import scala.collection.JavaConversions._
    for (entry <- healthChecks.entrySet) {
      results.put(entry.getKey, entry.getValue.execute)
    }
    Collections.unmodifiableSortedMap(results)
  }

  /**
   * Runs the registered health checks in parallel and returns a map of the results.
   * @param   executor object to launch and track health checks progress
   * @return a map of the health check results
   */
  def runHealthChecks(executor: ExecutorService): util.SortedMap[String, Result] = {
    val futures = new util.HashMap[String, Future[Result]]
    import scala.collection.JavaConversions._
    for (entry <- healthChecks.entrySet) {
      futures.put(entry.getKey, executor.submit(new Callable[Result]() {
        @throws(classOf[Exception])
        def call: Result = entry.getValue.execute
      }))
    }
    val results: util.SortedMap[String, Result] = new util.TreeMap[String, Result]
    import scala.collection.JavaConversions._
    for (entry <- futures.entrySet) {
      try results.put(entry.getKey, entry.getValue.get)
      catch {
        case e: Exception => LOGGER.warn(s"Error executing health check ${entry.getKey}", e)
      }
    }
    Collections.unmodifiableSortedMap(results)
  }
}
