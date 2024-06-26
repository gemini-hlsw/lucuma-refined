// Copyright (c) 2016-2023 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma.refined

import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.boolean.Not
import eu.timepit.refined.boolean.Or
import eu.timepit.refined.char.Letter
import eu.timepit.refined.collection.Empty
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.numeric.Less
import eu.timepit.refined.numeric.Negative
import eu.timepit.refined.numeric.Positive

import scala.compiletime.constValue
import scala.compiletime.summonFrom
import scala.quoted.Expr
import scala.quoted.FromExpr
import scala.quoted.Quotes

inline def refineMV[T, P](inline t: T)(using inline p: Predicate[T, P]): Refined[T, P] =
  inline if (p.isValid(t)) Refined.unsafeApply(t) else no

inline def no = scala.compiletime.error("no")

extension [T](inline t: T)
  inline def refined[P](using inline p: Predicate[T, P]): Refined[T, P] =
    refineMV(t)

trait Predicate[T, P] {
  inline def isValid(inline t: T): Boolean
}

object Predicate {

  inline given [T, A, B]: Predicate[T, Or[A, B]] with
    inline def isValid(inline t: T): Boolean =
      summonFrom { case predA: Predicate[T, A] =>
        summonFrom { case predB: Predicate[T, B] =>
          predA.isValid(t) || predB.isValid(t)
        }
      }

  inline given [T, A, B]: Predicate[T, And[A, B]] with
    inline def isValid(inline t: T): Boolean =
      summonFrom { case predA: Predicate[T, A] =>
        summonFrom { case predB: Predicate[T, B] =>
          predA.isValid(t) && predB.isValid(t)
        }
      }

  inline given Predicate[Int, Positive] with
    inline def isValid(inline t: Int): Boolean = t > 0

  inline given [N <: Int]: Predicate[Int, Greater[N]] with
    inline def isValid(inline t: Int): Boolean = t > constValue[N]

  inline given [N <: Int]: Predicate[Int, Less[N]] with
    inline def isValid(inline t: Int): Boolean = t < constValue[N]

  inline given Predicate[Int, Negative] with
    inline def isValid(inline t: Int): Boolean = t < 0

  inline given Predicate[Long, Positive] with
    inline def isValid(inline t: Long): Boolean = t > 0

  inline given [N <: Long]: Predicate[Long, Greater[N]] with
    inline def isValid(inline t: Long): Boolean = t > constValue[N]

  inline given [N <: Long]: Predicate[Long, Less[N]] with
    inline def isValid(inline t: Long): Boolean = t < constValue[N]

  inline given Predicate[Long, Negative] with
    inline def isValid(inline t: Long): Boolean = t < 0

  inline given Predicate[BigDecimal, Positive] with
    inline def isValid(inline t: BigDecimal): Boolean = ${ positiveBigDecimalMacro('t) }

  private given FromExpr[BigDecimal] with
    def unapply(value: Expr[BigDecimal])(using Quotes): Option[BigDecimal] =
      PartialFunction.condOpt(value) {
        case '{ BigDecimal(${ Expr(x) }: String) } =>
          BigDecimal(x)
        case '{ BigDecimal(${ Expr(x) }: Int) }    =>
          BigDecimal(x)
        case '{ BigDecimal(${ Expr(x) }: Long) }   =>
          BigDecimal(x)
        case '{ BigDecimal(${ Expr(x) }: Double) } =>
          BigDecimal(x)
      }

  private def positiveBigDecimalMacro(expr: Expr[BigDecimal])(using Quotes): Expr[Boolean] =
    expr match
      case '{ ${ Expr(x) }: BigDecimal } =>
        if x > 0 then '{ true }
        else '{ false }
      case _                             =>
        '{ no }

  inline given Predicate[BigDecimal, Negative] with
    inline def isValid(inline t: BigDecimal): Boolean = ${
      negativeBigDecimalMacro('t)
    }

  private def negativeBigDecimalMacro(expr: Expr[BigDecimal])(using Quotes): Expr[Boolean] =
    expr match
      case '{ ${ Expr(x) }: BigDecimal } =>
        if x < 0 then '{ true }
        else '{ false }
      case _                             =>
        '{ no }

  inline given Predicate[Char, Letter] with
    inline def isValid(inline t: Char): Boolean =
      ('a' <= t && t <= 'z') || ('A' <= t && t <= 'Z')

  inline given [T, A]: Predicate[T, Not[A]] with
    inline def isValid(inline t: T): Boolean =
      summonFrom { case p: Predicate[T, A] =>
        !p.isValid(t)
      }

  inline given Predicate[String, Empty] with
    inline def isValid(inline s: String): Boolean =
      ${ emptyStringMacro('s) }

  private def emptyStringMacro(expr: Expr[String])(using Quotes): Expr[Boolean] =
    Expr(expr.valueOrAbort.isEmpty)
}
