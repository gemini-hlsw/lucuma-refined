// Copyright (c) 2016-2022 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma.refined

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.Not
import eu.timepit.refined.char.Letter
import eu.timepit.refined.collection.Empty
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.numeric.Negative
import eu.timepit.refined.numeric.Positive

import scala.compiletime.constValue
import scala.compiletime.requireConst
import scala.quoted.Expr
import scala.quoted.Quotes
import scala.annotation.transparentTrait

inline def refineMV[T, P](inline t: T)(using inline p: Predicate[T, P]): Refined[T, P] =
  inline if (p.isValid(t)) Refined.unsafeApply(t) else no

inline def no = scala.compiletime.error("no")

extension [T](inline t: T)
  inline def refined[P](using inline p: Predicate[T, P]): Refined[T, P] =
    refineMV(t)

trait Predicate[T, P] {
  transparent inline def isValid(inline t: T): Boolean
}

object Predicate {

  inline given [M <: Int, N <: Int]: Predicate[Int, Interval.Closed[M, N]] with
    transparent inline def isValid(inline t: Int): Boolean =
      constValue[M] <= t && t <= constValue[N]

  inline given Predicate[Int, Positive] with
    transparent inline def isValid(inline t: Int): Boolean = t > 0

  inline given Predicate[BigDecimal, Positive] with
    transparent inline def isValid(inline t: BigDecimal): Boolean                 = ${ isValidMacro('t) }
    private def isValidMacro(expr: Expr[BigDecimal])(using Quotes): Expr[Boolean] =
      expr match {
        case '{ BigDecimal($i: Int) } => '{ $i > 0 }
        case _                        => '{ false }
      }

  inline given Predicate[Int, Negative] with
    transparent inline def isValid(inline t: Int): Boolean = t < 0

  inline given Predicate[Char, Letter] with
    transparent inline def isValid(inline t: Char): Boolean =
      ('a' <= t && t <= 'z') || ('A' <= t && t <= 'Z')

  inline given [T, A, P <: Predicate[T, A]](using p: P): Predicate[T, Not[A]] with
    transparent inline def isValid(inline t: T): Boolean = !p.isValid(t)

  inline given Predicate[String, Empty] with
    transparent inline def isValid(inline s: String): Boolean =
      requireConst(s)
      isValidConst(s)

    private transparent inline def isValidConst(inline s: String): Boolean =
      ${ isValidMacro('s) }

    private def isValidMacro(expr: Expr[String])(using Quotes): Expr[Boolean] =
      expr match {
        case '{ "" } => '{ true }
        case _       => '{ false }
      }
}
