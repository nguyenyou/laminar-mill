# Laminar Mill

This project is intended to help me learn about the internals of the Laminar and Airstream libraries.

## Unit Tests

```sh
yarn install
./mill airstream.test
./mill laminar.test
```

## WWW

```sh
./mill www.fullLinkJS
yarn install
yarn dev
```

## Metals

```sh
mill __.compiledClassesAndSemanticDbFiles
mill __.semanticDbData
```