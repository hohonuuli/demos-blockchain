package org.mbari.blockchain

import java.time.Instant

import spray.json.{ JsString, JsValue, JsonFormat }

/**
 *
 *
 * @author Brian Schlining
 * @since 2017-10-22T20:27:00
 */
package object original {

  private[this]type JF[T] = JsonFormat[T] // simple alias for reduced verbosity

  implicit def instantFormat = new JF[Instant] {

    override def write(obj: Instant): JsValue = JsString(obj.toString)

    override def read(json: JsValue): Instant = json match {
      case JsString(s) => Instant.parse(s)
      case x => spray.json.deserializationError("Expected time as JsString, but got " + x)
    }
  }

}
