# SPL Assignment 2 – Linear Algebra Engine

This project is an implementation of a multithreaded Linear Algebra Engine (LAE) written in Java, as part of the SPL course (BGU, 2026-1).

The engine evaluates linear algebra expressions described in a JSON input file, using a custom thread pool and shared data structures.  
Supported operations include matrix addition, multiplication, transpose, and negation, with nested expressions handled recursively.

## Main Features
- Maven-based Java project
- Custom thread pool (`TiredExecutor`) with fatigue-aware scheduling
- Thread-safe shared matrices and vectors
- Parallel execution of vector-level tasks
- JSON input parsing and JSON output generation
- Error handling for illegal operations (e.g. dimension mismatch)

## Build & Run
Requirements:
- Java 21
- Maven

Compile:
```bash
mvn compile
