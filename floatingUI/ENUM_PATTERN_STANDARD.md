# Enum Pattern Standard for floatingUI Module

## Overview

This document defines the **standard pattern** for implementing Scala 3 enums in the floatingUI module. All enums should follow this pattern for consistency, performance, and maintainability.

## Standard Pattern

### Template

```scala
/** [Brief description of the enum]
  *
  * [Detailed description explaining the purpose and usage]
  *
  * Matches TypeScript: type EnumName = 'value1' | 'value2' | 'value3'
  *
  * @see
  *   [Link to upstream Floating UI documentation]
  */
enum EnumName(val toValue: String) {

  /** [Description of case 1]
    *
    * [Additional details about when/how to use this case]
    */
  case Case1 extends EnumName("value1")

  /** [Description of case 2]
    *
    * [Additional details about when/how to use this case]
    */
  case Case2 extends EnumName("value2")

  /** [Description of case 3]
    *
    * [Additional details about when/how to use this case]
    */
  case Case3 extends EnumName("value3")
}

object EnumName {

  /** Parse EnumName from string value.
    *
    * @param value
    *   String value ("value1", "value2", or "value3")
    * @return
    *   Corresponding EnumName enum value
    * @throws IllegalArgumentException
    *   if value is not a valid EnumName
    */
  def fromString(value: String): EnumName = value match {
    case "value1" => Case1
    case "value2" => Case2
    case "value3" => Case3
    case _ => throw new IllegalArgumentException(
      s"Invalid EnumName: $value. Valid values are: 'value1', 'value2', 'value3'"
    )
  }
}
```

## Key Requirements

### 1. Enum Parameter for `toValue`

**✅ DO:**
```scala
enum Strategy(val toValue: String) {
  case Absolute extends Strategy("absolute")
  case Fixed extends Strategy("fixed")
}
```

**❌ DON'T:**
```scala
enum Strategy {
  case Absolute, Fixed
  
  def toValue: String = this match {
    case Absolute => "absolute"
    case Fixed    => "fixed"
  }
}
```

**Rationale:**
- **DRY**: String literal defined once per case
- **Performance**: Direct field access (no pattern matching overhead)
- **Clarity**: Immediately visible what string each case maps to
- **Type Safety**: Compile-time constant

### 2. Exception-Throwing `fromString`

**✅ DO:**
```scala
def fromString(value: String): Strategy = value match {
  case "absolute" => Absolute
  case "fixed"    => Fixed
  case _ => throw new IllegalArgumentException(
    s"Invalid Strategy: $value. Valid values are: 'absolute', 'fixed'"
  )
}
```

**❌ DON'T:**
```scala
def fromString(s: String): Option[Strategy] = s match {
  case "absolute" => Some(Absolute)
  case "fixed"    => Some(Fixed)
  case _          => None
}
```

**Rationale:**
- **Simpler API**: No unwrapping needed (no `.get`, `.getOrElse`, pattern matching)
- **Better Error Messages**: Lists all valid values in exception message
- **TypeScript Parity**: Matches TypeScript behavior (runtime error for invalid values)
- **Use Case**: Parsing from TypeScript/JSON where invalid values indicate bugs

### 3. Comprehensive ScalaDoc

**Required Elements:**

1. **Enum-level documentation:**
   - Brief description (first line)
   - Detailed description (purpose and usage)
   - TypeScript type reference: `Matches TypeScript: type Foo = 'a' | 'b'`
   - `@see` link to upstream Floating UI documentation

2. **Case-level documentation:**
   - Description of each case
   - When/how to use this case
   - Any special behavior or considerations

3. **`fromString` documentation:**
   - `@param` describing the input
   - `@return` describing the output
   - `@throws` documenting the exception

**Example:**
```scala
/** Element context for overflow detection.
  *
  * Specifies which element (floating or reference) to check for overflow relative to a boundary.
  *
  * Matches TypeScript: type ElementContext = 'reference' | 'floating'
  *
  * @see
  *   https://floating-ui.com/docs/detectOverflow#elementcontext
  */
enum ElementContext(val toValue: String) {

  /** Check overflow of the reference element.
    *
    * Used when you want to detect if the reference element itself is overflowing its boundary.
    */
  case Reference extends ElementContext("reference")

  /** Check overflow of the floating element (default).
    *
    * Used when you want to detect if the floating element is overflowing its boundary.
    */
  case Floating extends ElementContext("floating")
}
```

### 4. Naming Conventions

- **Enum name**: PascalCase (e.g., `ElementContext`, `HideStrategy`)
- **Case names**: PascalCase (e.g., `Reference`, `ReferenceHidden`)
- **String values**: Match TypeScript exactly (e.g., `"reference"`, `"referenceHidden"`)
- **Parameter name**: Use `value` (not `s` or `str`)

## Complete Example

```scala
/** Hide strategy for determining when to hide the floating element.
  *
  * Specifies which hiding detection strategy to use when checking if the floating element should be hidden.
  *
  * Matches TypeScript: type HideStrategy = 'referenceHidden' | 'escaped'
  *
  * @see
  *   https://floating-ui.com/docs/hide
  */
enum HideStrategy(val toValue: String) {

  /** Detect if the reference element is hidden or fully clipped.
    *
    * Checks if the reference element is not visible within its clipping boundary. This is useful for hiding the floating element when the
    * reference element is scrolled out of view or otherwise hidden.
    *
    * When this strategy is used, the middleware checks overflow with `elementContext = 'reference'`.
    */
  case ReferenceHidden extends HideStrategy("referenceHidden")

  /** Detect if the floating element has escaped its boundary.
    *
    * Checks if the floating element has overflowed outside its allowed boundary. This is useful for hiding the floating element when it
    * would appear outside the viewport or other boundary constraints.
    *
    * When this strategy is used, the middleware checks overflow with `altBoundary = true`.
    */
  case Escaped extends HideStrategy("escaped")
}

object HideStrategy {

  /** Parse HideStrategy from string value.
    *
    * @param value
    *   String value ("referenceHidden" or "escaped")
    * @return
    *   Corresponding HideStrategy enum value
    * @throws IllegalArgumentException
    *   if value is not a valid HideStrategy
    */
  def fromString(value: String): HideStrategy = value match {
    case "referenceHidden" => ReferenceHidden
    case "escaped"         => Escaped
    case _ => throw new IllegalArgumentException(
      s"Invalid HideStrategy: $value. Valid values are: 'referenceHidden', 'escaped'"
    )
  }
}
```

## Benefits of This Pattern

### 1. Performance
- **Zero runtime overhead** for `toValue` (direct field access vs pattern matching)
- **Compile-time constant** (can be inlined by compiler)

### 2. Maintainability
- **DRY**: String literal defined once (not repeated in `toValue` and `fromString`)
- **Single source of truth**: Each case explicitly declares its string value
- **Easy to add cases**: Just add a new case with its string value

### 3. Type Safety
- **Compile-time verification**: Invalid string values caught at compile time
- **Exhaustive matching**: Compiler ensures all cases are handled

### 4. Documentation
- **Comprehensive**: Forces developers to document purpose and usage
- **TypeScript parity**: Clear mapping to upstream TypeScript types
- **Discoverable**: IDE shows documentation on hover

### 5. Consistency
- **Uniform pattern**: All enums follow the same structure
- **Predictable API**: Developers know what to expect

## Current Status

### Enums Following Standard Pattern (2/9)

✅ `ElementContext` (2 cases)
✅ `HideStrategy` (2 cases)

### Enums Needing Refactoring (7/9)

❌ `Alignment` (2 cases)
❌ `Side` (4 cases)
❌ `Placement` (12 cases)
❌ `Strategy` (2 cases)
❌ `Axis` (2 cases)
❌ `Length` (2 cases)
❌ `FallbackStrategy` (2 cases)

**Note:** Refactoring is **safe and non-breaking** because `fromString` is not used anywhere in the codebase for these enums.

## Migration Guide

### For Existing Enums

**Step 1:** Change enum definition:
```scala
// Before
enum Alignment {
  case Start, End
  def toValue: String = this match {
    case Start => "start"
    case End   => "end"
  }
}

// After
enum Alignment(val toValue: String) {
  case Start extends Alignment("start")
  case End extends Alignment("end")
}
```

**Step 2:** Change `fromString`:
```scala
// Before
def fromString(s: String): Option[Alignment] = s match {
  case "start" => Some(Start)
  case "end"   => Some(End)
  case _       => None
}

// After
def fromString(value: String): Alignment = value match {
  case "start" => Start
  case "end"   => End
  case _ => throw new IllegalArgumentException(
    s"Invalid Alignment: $value. Valid values are: 'start', 'end'"
  )
}
```

**Step 3:** Add comprehensive ScalaDoc (see template above)

### For New Enums

Always use the standard pattern from the start. See the template at the top of this document.

## Exceptions

There are **no exceptions** to this pattern. All enums in the floatingUI module should follow this standard.

If you encounter a use case that seems to require a different pattern, discuss with the team before deviating from the standard.

## References

- [Scala 3 Enum Documentation](https://docs.scala-lang.org/scala3/reference/enums/enums.html)
- [Floating UI TypeScript Source](https://github.com/floating-ui/floating-ui)
- [Floating UI Documentation](https://floating-ui.com/docs/getting-started)

