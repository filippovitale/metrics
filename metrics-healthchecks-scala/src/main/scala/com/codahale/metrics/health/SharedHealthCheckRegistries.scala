package com.codahale.metrics.health

import java.util
import java.util.concurrent.ConcurrentHashMap

/**
 * A map of shared, named health registries.
 */
object SharedHealthCheckRegistries {
  private val REGISTRIES = new ConcurrentHashMap[String, HealthCheckRegistry]

  def clear(): Unit = REGISTRIES.clear()

  def names: util.Set[String] = REGISTRIES.keySet

  def remove(key: String): Unit = REGISTRIES.remove(key)

  def add(name: String, registry: HealthCheckRegistry): HealthCheckRegistry =
    REGISTRIES.putIfAbsent(name, registry)

  def getOrCreate(name: String): HealthCheckRegistry = {
    val existing = REGISTRIES.get(name)
    if (existing == null) {
      val created: HealthCheckRegistry = new HealthCheckRegistry
      val raced: HealthCheckRegistry = add(name, created)
      if (raced == null) {
        return created
      }
      return raced
    }
    existing
  }
}