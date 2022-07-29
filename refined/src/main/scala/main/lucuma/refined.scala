// Copyright (c) 2016-2022 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma.refined

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.boolean.Not
import eu.timepit.refined.boolean.Or
import eu.timepit.refined.char.Letter
import eu.timepit.refined.collection.Empty
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.numeric.Less
import eu.timepit.refined.numeric.Negative
import eu.timepit.refined.numeric.NonNegative
import eu.timepit.refined.numeric.Positive
import shapeless.Nat
import shapeless.ops.nat.ToInt

import scala.annotation.transparentTrait
import scala.compiletime.constValue
import scala.compiletime.requireConst
import scala.quoted.Expr
import scala.quoted.Quotes

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

  inline given [T, A, B, PA <: Predicate[T, A], PB <: Predicate[T, B]](using
    predA: PA,
    predB: PB
  ): Predicate[T, Or[A, B]] with
    transparent inline def isValid(inline t: T): Boolean = predA.isValid(t) || predB.isValid(t)

  inline given [T, A, B, PA <: Predicate[T, A], PB <: Predicate[T, B]](using
    predA: PA,
    predB: PB
  ): Predicate[T, And[A, B]] with
    transparent inline def isValid(inline t: T): Boolean = predA.isValid(t) && predB.isValid(t)

  inline given Predicate[Int, Positive] with
    transparent inline def isValid(inline t: Int): Boolean = t > 0

  inline given [N <: Int]: Predicate[Int, Greater[N]] with
    transparent inline def isValid(inline t: Int): Boolean = t > constValue[N]

  inline given [N <: Int]: Predicate[Int, Less[N]] with
    transparent inline def isValid(inline t: Int): Boolean = t < constValue[N]

  inline given Predicate[Int, Negative] with
    transparent inline def isValid(inline t: Int): Boolean = t < 0

  inline given Predicate[Long, Positive] with
    transparent inline def isValid(inline t: Long): Boolean = t > 0

  inline given [N <: Long]: Predicate[Long, Greater[N]] with
    transparent inline def isValid(inline t: Long): Boolean = t > constValue[N]

  inline given [N <: Long]: Predicate[Long, Less[N]] with
    transparent inline def isValid(inline t: Long): Boolean = t < constValue[N]

  inline given Predicate[Long, Negative] with
    transparent inline def isValid(inline t: Long): Boolean = t < 0

  inline given Predicate[BigDecimal, Positive] with
    transparent inline def isValid(inline t: BigDecimal): Boolean = ${ positiveBigDecimalMacro('t) }

  private def positiveBigDecimalMacro(expr: Expr[BigDecimal])(using Quotes): Expr[Boolean] =
    expr match {
      case '{ BigDecimal($i: Int) }    => '{ $i > 0 }
      case '{ BigDecimal($s: String) } => Expr(BigDecimal(s.valueOrAbort) > 0)
      case _                           => '{ no }
    }

  inline given Predicate[BigDecimal, NonNegative] with
    transparent inline def isValid(inline t: BigDecimal): Boolean = ${
      nonNegativeBigDecimalMacro('t)
    }

  private def nonNegativeBigDecimalMacro(expr: Expr[BigDecimal])(using Quotes): Expr[Boolean] =
    expr match {
      case '{ BigDecimal($i: Int) }    => '{ !($i < 0) }
      case '{ BigDecimal($s: String) } => Expr(!(BigDecimal(s.valueOrAbort) < 0))
      case _                           => '{ no }
    }

  inline given Predicate[Char, Letter] with
    transparent inline def isValid(inline t: Char): Boolean =
      ('a' <= t && t <= 'z') || ('A' <= t && t <= 'Z')

  inline given [T, A, P <: Predicate[T, A]](using p: P): Predicate[T, Not[A]] with
    transparent inline def isValid(inline t: T): Boolean = !p.isValid(t)

  inline given Predicate[String, Empty] with
    transparent inline def isValid(inline s: String): Boolean =
      ${ emptyStringMacro('s) }

  private def emptyStringMacro(expr: Expr[String])(using Quotes): Expr[Boolean] =
    Expr(expr.valueOrAbort.isEmpty)
}
