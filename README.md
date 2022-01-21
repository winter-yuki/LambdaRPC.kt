# λRPC

## Service as a library

TODO Custom in-project declarations instead of the auto-generated API. Keep code contracts at the same place.

## High order functions use-cases

- Security: send closure instead of sensitive data.
- Protocol simplification: additional data (or some configuration update) can be requested via hof call.
- Manageable data-processing pipelines (continuations).
- Stateful streaming computations.
- Load balancing: task is done, request new.
- Dynamically choose: compute on client or send data to the server and compute there.

## Run examples

```bash
$ cd LambdaRPC.kt
$ ./gradlew example.basic.service
$ ./gradlew example.basic.client
```

## Repository organization
