# We will write a nice description of this repo soon, we promise.

## Rx model configuration

Rx consumers add `RbacModelSymbols.configuredRbacApiModel` from `rbac-rx-module-api`.
The Rx module acquires this platform-owned model and adds the RBAC API and security reason
schemas as dependencies. Authorization and role-induction metadata live in its `typeOverrides`.
The packaged `rbac-configured-api-model` remains for CX; it is not the Rx runtime configuration.
A fresh platform therefore gets fresh processor bindings even when multiple platforms share a JVM.
