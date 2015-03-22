# Design Notes For Project #

## Suggested separate jobs to be done ##

  1. implement CHT (including joining and leaving).
    * limiting ourselves to LAN might simplify joining/leaving
  1. Design (and implement or just suggest implementation) a group membership and multicast solution
  1. design network comms (protobufs?) and design (rdf?) state + feedback info (e.g. find out how subsystems are doing and export in RDF)
  1. design locking + consistency strategy.
  1. Design actual file transport and store strategy
  1. decide on testing strategy (unittesting easy but how do we integration test feedback based systems? how will we regression test?)
  1. want item 6. before items 5.+4. so we can add features as we go.