# Autonomic Project Requirements #

## General ##
  * CRUD system for files.
  * System is clustered.
  * Supports multiple cluster nodes per host and/or multiple hosts.
  * Initial target is of file replication factor=2.
  * Clients can direct requests at any cluster node.
  * System should support multiple node failures subject to possible file loss (if a file and all replicas are on the failed nodes)

## Self - Managing ##
> ### Self - Configuring ###
    * It must be possible to stop existing cluster nodes and start-up new ones without any configuration step.
    * When a node is manually added or removed, the system rebalances the file distribution across nodes
    * The time delay before automatically replicating files belonging to failed nodes is automatically chosen based on past failure experience.
> ### Self - Optimising ###
    * System must maintain even even distribution of files across cluster nodes (optimizing performance and availability)
> ### Self - Protecting ###
    * System will automatically recognise badly behaved clients. (e.g.: Too high read/update rate, or quota so can't fill file system)
> ### Self - Healing ###
    * Supports any single cluster node failure without affecting service of any operation.
    * When a node fails, then after a suitable time delay, the system re-replicates files in order to maintain the replication factor for all files.
    * Supports system recovery after failure of entire cluster (e.g.: Data centre failure)

## Self - Describing ##
  * Cluster nodes must be discoverable by clients
  * Cluster nodes communicate with each other with node-alives and
internal state messages (e.g.:disk usage, read rates, write rates etc.)
so that each node is aware of overall system state.

## Stretch Goals ##
  * Supports multiple customer classes where some are guaranteed that "writes always succeed" (i.e: eventual consistency during network partitions) and other
> customer class where writes may fail during network partition because these customer's want guarantees about consistency.
  * Attempt to use information from data loss and node failure to adjust replication and other system parameters to make system more resilient against already experienced failures.