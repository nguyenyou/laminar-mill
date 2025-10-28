# Playwright Integration Testing for Floating UI

This document explains the Playwright-based integration testing setup for the Floating UI module.

## Overview

The Floating UI module uses **Playwright** for integration testing to validate positioning calculations in a real browser environment. This is necessary because positioning logic depends on browser layout calculations (e.g., `getBoundingClientRect()`) that are not available in simpler test environments like jsdom.

## Test Environment Comparison

### jsdom (Unit Tests)
- **Location**: `floatingUI/test/src/*.scala` (e.g., `ComputePositionTest.scala`)
- **Environment**: Node.js with jsdom
- **Layout Engine**: ❌ None
- **getBoundingClientRect**: Returns zeros
- **Speed**: ⚡ Very fast
- **Use Case**: API validation, type checking, basic logic
- **CI/CD**: ✅ Easy to set up

### Playwright (Integration Tests)
- **Location**: `floatingUI/test/src/integration/*.scala`
- **Environment**: Real Chrome browser (headless)
- **Layout Engine**: ✅ Full Chromium layout engine
- **getBoundingClientRect**: Returns actual calculated values
- **Speed**: 🐢 Slower (browser startup overhead)
- **Use Case**: Positioning validation, viewport behavior, middleware testing
- **CI/CD**: Requires browser installation

## System Requirements

### One-Time Setup

Before running Playwright tests, you need to install Playwright and its browser dependencies:

```bash
# Install Playwright CLI (if not already installed via package.json)
npm install -D playwright

# Install system dependencies for Chromium
npx playwright install-deps

# Install Chromium browser
npx playwright install chromium
```

### Verification

To verify Playwright is installed correctly:

```bash
npx playwright --version
```

## Running Tests

### Run All Tests (Unit + Integration)

```bash
./mill floatingUI.test
```

This will run both jsdom-based unit tests and Playwright-based integration tests.

### Run Only Integration Tests

To run only the integration tests, you can use ScalaTest's test filtering:

```bash
./mill floatingUI.test -- -n integration
```

(Note: This requires adding `@Tag("integration")` annotations to integration tests)

### Run Specific Test File

```bash
./mill floatingUI.test -- -z "PositioningIntegrationTest"
```

## Test Structure

### Integration Test Files

Integration tests are located in:
```
floatingUI/test/src/integration/
├── PositioningIntegrationTest.scala    # Main positioning tests
└── helpers/
    └── TestHelpers.scala               # DOM element creation utilities
```

### Example Test

```scala
it("should position floating element below reference element") {
  var reference: dom.HTMLElement = null
  var floating: dom.HTMLElement = null

  try {
    // Create test elements
    reference = createReferenceElement(
      left = 100, top = 100,
      width = 100, height = 50
    )
    floating = createFloatingElement(
      width = 150, height = 80
    )

    // Test positioning
    val result = computePosition(
      reference = reference,
      floating = floating,
      placement = "bottom"
    )

    // Verify results
    result.x shouldBe 100.0 +- 1.0
    result.y shouldBe 150.0 +- 1.0

  } finally {
    // Always clean up DOM elements
    cleanup(reference, floating)
  }
}
```

## Configuration

### Mill Build Configuration

The Playwright configuration is in `build.mill`:

```scala
object floatingUI extends ScalaJSModule {
  // ES modules required for Playwright
  def moduleKind = ModuleKind.ESModule
  
  object test extends ScalaJSTests with TestModule.ScalaTest {
    override def jsEnvConfig = Task { 
      JsEnvConfig.Playwright.chrome(
        headless = true,   // Run without visible browser window
        showLogs = false,  // Hide browser console logs
        debug = false      // Hide Playwright debug info
      )
    }
    
    override def moduleKind = ModuleKind.ESModule
  }
}
```

### Debugging Configuration

To debug tests with a visible browser window and console output:

```scala
JsEnvConfig.Playwright.chrome(
  headless = false,  // Show browser window
  showLogs = true,   // Show console.log output
  debug = true       // Show Playwright debug info
)
```

## Troubleshooting

### Error: "Executable doesn't exist"

**Problem**: Playwright browsers not installed.

**Solution**:
```bash
npx playwright install chromium
npx playwright install-deps
```

### Error: "Module kind must be ESModule"

**Problem**: Playwright requires ES modules.

**Solution**: Ensure `moduleKind = ModuleKind.ESModule` is set in both the main module and test object.

### Tests Fail Due to Viewport Size

**Problem**: Some tests assume minimum viewport dimensions (e.g., 800x600).

**Solution**: Playwright's default viewport is typically 1280x720, which should be sufficient. If needed, you can configure viewport size in the Playwright launch options (requires custom configuration).

### Slow Test Execution

**Problem**: Playwright tests are slower than jsdom tests.

**Solution**: This is expected. Playwright starts a real browser, which has overhead. Consider:
- Running integration tests less frequently (e.g., only on CI/CD)
- Using jsdom for fast unit tests
- Running Playwright tests only for positioning-critical functionality

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install Playwright
        run: |
          npm install
          npx playwright install-deps
          npx playwright install chromium
      
      - name: Run Tests
        run: ./mill floatingUI.test
```

## Best Practices

1. **Always clean up DOM elements** in `finally` blocks to prevent test pollution
2. **Use tolerance in assertions** (`shouldBe x +- 1.0`) to account for sub-pixel rendering
3. **Keep integration tests focused** on positioning behavior that requires a real browser
4. **Use unit tests for API validation** to keep test suite fast
5. **Document viewport assumptions** if tests depend on specific viewport dimensions

## Further Reading

- [Mill ScalaJS Documentation](https://mill-build.org/mill/scalalib/web.html)
- [Playwright Documentation](https://playwright.dev/)
- [scala-js-env-playwright](https://github.com/ThijsBroersen/scala-js-env-playwright)
- [Floating UI Documentation](https://floating-ui.com/)

