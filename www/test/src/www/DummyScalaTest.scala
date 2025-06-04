import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DummyScalaTest extends AnyFunSuite with Matchers {

  test("basic equality assertions") {
    val result = 2 + 2
    assert(result == 4)
    assert(result === 4) // === provides better error messages
    
    // Using shouldBe matcher
    result shouldBe 4
    result should equal(4)
  }
} 