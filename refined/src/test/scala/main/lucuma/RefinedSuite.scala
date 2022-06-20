// Copyright (c) 2016-2022 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package lucuma.refined

import eu.timepit.refined.boolean.Not
import eu.timepit.refined.char.Letter
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.numeric.Negative
import eu.timepit.refined.numeric.Positive
import munit.FunSuite
import eu.timepit.refined.collection.Empty
import eu.timepit.refined.collection.NonEmpty

class RefinedSuite extends FunSuite {

  inline def assertRefineError(code: String) =
    assert(compileErrors(code).contains("error: no"))

  test("closed interval") {
    0.refined[Interval.Closed[0, 2]]
    1.refined[Interval.Closed[0, 2]]
    2.refined[Interval.Closed[0, 2]]
    assertRefineError("-1.refined[Interval.Closed[0, 2]]")
  }

  test("positive integer") {
    1.refined[Positive]
    Int.MaxValue.refined[Positive]
    assertRefineError("0.refined[Positive]")
    assertRefineError("-1.refined[Positive]")
  }

  test("positive bigdecimal") {
    BigDecimal(1).refined[Positive]
    BigDecimal(Int.MaxValue).refined[Positive]
    assertRefineError("BigDecimal(0).refined[Positive]")
    assertRefineError("BigDecimal(-1).refined[Positive]")
    assertRefineError("BigDecimal(scala.util.Random.nextLong()).refined[Not[Positive]]")
  }

  test("negative integer") {
    -1.refined[Negative]
    Int.MinValue.refined[Negative]
    assertRefineError("0.refined[Negative]")
    assertRefineError("1.refined[Negative]")
  }

  test("letter char") {
    'a'.refined[Letter]
    'z'.refined[Letter]
    assertRefineError("'0'.refined[Letter]")
    assertRefineError("'!'.refined[Letter]")
  }

  test("not") {
    -1.refined[Not[Positive]]
    1.refined[Not[Negative]]
    'a'.refined[Not[Not[Letter]]]
    '!'.refined[Not[Letter]]
    assertRefineError("-1.refined[Not[Negative]]")
    assertRefineError("1.refined[Not[Positive]]")
    assertRefineError("'a'.refined[Not[Letter]]")
  }

  test("empty string") {
    "".refined[Empty]
    "carlos".refined[NonEmpty]
    assertRefineError(""" "".refined[NonEmpty] """)
    assertRefineError(""" "carlos".refined[Empty] """)
    assert(
      compileErrors("""???.toString.refined[NonEmpty]""")
        .contains("error: expected a constant value but found")
    )
  }

}
