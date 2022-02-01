# λRPC

Simple native RPC with high order functions support.

Inspired by [mipt communicator project](https://github.com/mipt-npm/communicator/tree/gh-pages).

## Service as a library

λRPC does not use standalone declarations to generate code (native). It uses user-defined (and project-specific) data
structures and default or custom serializers instead.

Functions can receive and return other functions as first-class objects. Provided lambdas are executed on the client
side, so they can easily capture state and be "sent" to the other language process.

All of it makes multi-process communication smooth enough to recognize remote service as a common library.

## Service-decomposition purposes

- Code execution in different containers or on various hardware (GPU for instance).
- Parallel execution of independent tasks.
- Communication with code written in other language.
- Rerun subtasks in case of failures or resume using some state snapshot.
- Microservice architecture.

## High order functions (HOF) use-cases

- Communication protocol simplification:
    - Service function can easily request additional information in some cases.
    - Reduce service code duplication: make HOF and receive specific operations from the client.
- Interactive computations: receive client lambda, provide information about computation (loss function value for
  instance), and lambda cancels computation (machine learning process) if something is not good.
- Security:
    - Send closures operating on the sensitive data instead of the data itself.
    - Provide computational resources as a library of functions that are parametrized by client lambdas instead of
      receiving client's code and executing it.
- Computation location dynamic choice: compute something using amount of data on a client or send data to the server and
  compute there.
- Load balancing: task is done, request new via client's lambda.
- Stateful streaming computations: nodes provide theirs lambdas for a mapper.

## Run examples

### Basic example

```bash
$ cd LambdaRPC.kt
$ ./gradlew example.basic.service1
$ ./gradlew example.basic.service2
$ ./gradlew example.basic.client  # or example.basic.stress
```

### Lazy pipeline example

```bash
$ cd LambdaRPC.kt
$ ./gradlew example.lazy.service --args=8090
$ ./gradlew example.lazy.service --args=8091
# Any number of services on different ports
$ ./gradlew example.lazy.client --args='8090 8091' # Ports of all services
```

## Repository organization

- `dsl` -- domain-specific language for λRPC library users.
- `examples` -- examples of λRPC usage.
- `exceptions` -- base library exception classes.
- `functions` -- each λRPC function consists of two parts: `backend` that holds original function and deserializes data
  for it, and `frontend` which is a callable proxy object that being called on the client side serializes arguments,
  sends them to the backend function and awaits result from it.
- `serialization` -- serializers of two kinds: data serializers and function serializers.
    - Default data serializer uses `kotlinx.serialization` to serialize data to JSON.
    - Function serialization saves function as backend function to some registry and returns its `access name`. Function
      deserialization creates frontend function that is able to communicate with the corresponding backend function.
- `service` -- lib service implementation and it's connection.
- `utils` -- some useful utils.
