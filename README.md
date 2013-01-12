ch.raffael.contracts
====================

[Design-by-contract](http://en.wikipedia.org/wiki/Design_by_contract) for Java.

This projects aims to add design-by-contract capabilities to Java. It consists of 3 parts:

* Java annotations to annotate classes and methods with contracts using a
  the Cel (Contract Expression Language).

* A compile-time processor to prepare the contracts for deployment.

* A runtime Java agent that deploys the contract at runtime (if enabled).

For details, see the [full Documentation](http://projects.raffael.ch/contracts).

This project is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
