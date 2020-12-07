#!/bin/sh
mvn -f jax-ws-cmd/pom.xml package &
mvn -f jax-ws-pv/pom.xml package &