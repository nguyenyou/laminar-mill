# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a learning project for exploring the internals of **Laminar** (Scala.js UI library) and **Airstream** (FRP library). The codebase is a custom implementation of these libraries using Mill as the build tool.

## Build System: Mill

This project uses Mill 1.0.5 as its build tool. The `./mill` script should be used for all build commands.

### Key Commands

**Running Tests:**
```sh
./mill airstream.test          # Run Airstream tests
./mill laminar.test            # Run Laminar tests
./mill __.test                 # Run all tests recursively
```

**Building the Web App:**
```sh
./mill www.fullLinkJS          # Compile Scala.js to JavaScript (full optimization)
yarn install                   # Install frontend dependencies
yarn dev                       # Start Vite dev server
yarn build                     # Production build
```

**IDE Support (Metals):**
```sh
mill __.compiledClassesAndSemanticDbFiles    # Generate SemanticDB for IDE support
```

**Other Useful Commands:**
```sh
./mill resolve __              # List all available tasks
./mill <module>.compile        # Compile a specific module
./mill <module>.console        # Start Scala REPL for a module
./mill show <module>.<task>    # Show output path/value of a task
```

## Module Architecture

The project is organized into five main Scala.js modules defined in [build.mill](build.mill):

1. **ew** - "Extensions for the Web"
   - JavaScript interop utilities with extension methods for `js.Array`, `js.Set`, `js.Map`, `js.String`
   - Small library (~7 Scala files) providing rich wrappers for JavaScript types
   - No dependencies on other modules

2. **airstream** - FRP (Functional Reactive Programming) library
   - Core reactive primitives: `Observable`, `EventStream`, `Signal`, `EventBus`, `Var`
   - ~144 Scala files organized by functionality
   - Key packages:
     - `core/` - Base types and observables
     - `state/` - Stateful observables (Signal, Var)
     - `split/` - Dynamic list operations with macros
     - `extensions/` - Extension methods for common types
     - `ownership/` - Resource management and subscriptions
     - `timing/` - Debounce, throttle, delay operators
   - Depends on: **ew**

3. **laminar** - Reactive UI library
   - ~136 Scala files providing declarative DOM API
   - Key packages:
     - `api/` - Main public API (`L` object, implicits)
     - `nodes/` - DOM node wrappers (elements, text, comments)
     - `modifiers/` - Reactive modifiers for elements
     - `keys/` - Type-safe HTML attributes, properties, styles
     - `inserters/` - Dynamic child insertion (text, nodes, collections)
     - `receivers/` - Event listeners and binders
     - `DomApi.scala` - Low-level DOM operations
   - Depends on: **airstream**

4. **waypoint** - Routing library
   - Built on top of Laminar
   - Uses url-dsl and upickle for route parsing
   - Depends on: **laminar**

5. **www** - Demo/test application
   - Simple counter app in [www/src/www/App.scala](www/src/www/App.scala)
   - Entry point at [www/src/www/index.scala](www/src/www/index.scala)
   - Uses Vite for development and bundling
   - Depends on: **laminar**

## Source Code Organization

- Source files follow package structure: `<module>/src/io/github/nguyenyou/<module>/`
- Test files mirror source structure: `<module>/test/src/`
- All modules use Scala 3.7.2 and Scala.js 1.19.0
- Tests use ScalaTest and jsdom for DOM testing

## Development Workflow

1. **Making changes to Scala code:**
   - Edit source files in the appropriate module
   - Run tests with `./mill <module>.test`
   - For Metals IDE support, run `mill __.compiledClassesAndSemanticDbFiles` after significant changes

2. **Working on the web app:**
   - After changing Scala code, rebuild with `./mill www.fullLinkJS`
   - Vite dev server (`yarn dev`) will automatically reload
   - Generated JS output is in `out/www/fullLinkJS.dest/`

3. **Scala 3 Migration:**
   - The build uses `-source 3.7-migration` and `-rewrite` flags
   - Compiler will automatically rewrite deprecated Scala 2 syntax

## Important Configuration Files

- [build.mill](build.mill) - Module definitions and dependencies
- [.scalafmt.conf](.scalafmt.conf) - Scalafmt 3.7.15 with Scala 3 dialect
- [vite.config.js](vite.config.js) - Vite configuration for dev server
- [package.json](package.json) - Uses Rolldown-based Vite variant and jsdom

## Dependencies

Defined in [build.mill](build.mill) `Versions` object:
- scalajs-dom 2.8.0
- tuplez-full-light 0.4.0
- domtestutils 18.0.0 (for tests)
- ScalaTest 3.2.19 (for tests)