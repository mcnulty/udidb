# Design Document for the udidb #

The userland debugger interface debugger, shortened to udidb, is a
cross-platform debugger, initially targeting native applications
implemented in any source language that compiles to machine code
for a physical processor.

## High Level ##

The core of udidb is an extensible, event-driven engine upon which 
debugging front-ends can be built. The engine provides representations
of processes being debugged, _debuggees_, interfaces and support code
to drop in operations to be performed on debuggees and a basic set of
operations that can be performed on debuggees, agnostic of these
operations are invoked by a user.

The front-ends are responsible for controlling how operations are
invoked and determining the data for these operations. The front-ends
are also responsible for handling events that the engine produces as
well as feeding these events back into the engine. This approach allows
the front-ends to control in what contexts they are event-driven, if at
all.

## Design Goals ##

The following is an incomplete list of design goals for udidb:
- near-uniform user experience on all supported platforms/targets/etc.
- hackable code base
- asynchronous control of multiple debuggees at once
- extensible via:
  - pluggable operations
  - separation operation execution from input/output concerns

## cli for udidb engine ##

The udidb cli is meant to provide a similar experience to existing command line
based debuggers such as gdb, cdb, or dbx.

## REST API engine for udidb ##

The REST API for the udidb engine is meant to facilitate the implementation of
GUI front-ends and scenarios where the debuggee exists on a remote host. The
REST API presents a set of resources that allow interaction with the udidb
engine using HTTP semantics.

The following resources exist in the REST API:

- GET /debuggeeContexts
  - a list of debuggee contexts
- POST /debuggeeContexts
  - create a new debuggee
- GET /debuggeeContexts/operations
  - get all descriptions of all available operations
- GET /debuggeeContexts/{id}
  - information for a specific debuggee context
- GET /debuggeeContexts/{id}/process
  - information about the process of a debuggee context
- GET /debuggeeContexts/{id}/process/threads
  - information about the threads of a debuggee context
- GET /debuggeeContexts/{id}/process/threads/{threadId}
  - information about a specific thread
- POST /debugeeContexts/{id}/process/operation
  - execute an operation against the specified process
- GET /debuggeeContexts/{id}/process/operation
  - get the results of the previous operation for the specified process
- GET /debuggeeContexts/{id}/process/operations
  - metadata describing the operations available for the specified process
- /events
  - WebSocket resource for receiving events about all debuggeeContexts
