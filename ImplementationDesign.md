# Implementation Design #

High Level design implementation of the system (very uncomplete)

## Node Design ##

  * Will have multiple IDs
    * Id will be calculated on hashing (MAC address + process count + id amount count(hardcoded to 4 initially)) in that way we'll have persistent but unique Ids for particular computer.
  * Will have data store
    * That will hold NameValue pairs of FileName, File Object that Nodes Store
    * File Object
      * Will store information about as well as data of the file
      * Will store current replica count of the file – for consideration
      * Will store informantion whether this is a master file or replica
  * Will have a List of known nodes
    * Collection of NodeIds/address (IP + port)
  * Will have User Services (With simple UI – web ui probably i the simplest)
    * For simplicity, let's use web services (profided by some Java framework – something like WCF in .net)
    * STORE filename data - stores file, overwrite if one exist
    * RETRIEVE filename - Downloads file
    * LIST – lists all files
    * DELETE – deletes a file
  * Will have Internal communication system
    * Again same framework as above
    * SYNC NODELIST
    * SYNC FILELIST
    * STORE
    * RETRIEVE – might return a file if doesn't have the file – will query it's own nodelist for closest node and would return this nodeid/address to inquirer

## Internal Node Responsibilities / Functions ##

  * Bootstraping, joining the network
    * Broadcast/Multicast message, first one to respond will be the joining node
  * Rejoining the network
    * Join the network,
    * Basing on the new node list reorganise the possession of his files by storing them into new nodes if required
  * Finding and serving the file
    * Find the owner node and ask for file
    * if node doesn't have the file, will sync the node list and attempt again
    * if node is dead will try neighbour nodes
  * Keep the replica count
    * will periodically check neighbour nodes if they hold copies of the files and are alive
    * if dead then will find next neighbour node and copy the file
  * Check for master file nodes
    * will periodically check if master nodes for files are still alive, if not, then will search the node for new master node or become one
  * Provide a joining point for others
    * answer the multicast/broadcast messages
  * Storing the file
    * accept the file
    * send and store the file to owner node
    * respond ok
  * Delete the file
    * find the owner node
    * send delete message
  * Keep the Deleted files replicas consistent
    * if file from Data Store is deleted delete it from replicas

## Functional Behaviour ##

## Client Operations ##
  * Common
    * Clients can connect to any cluster node through discovery
  * Store
    * Client sends file to entry node with store request
    * Entry node determines who is the owner node
    * Entry node sends the file for storage to owner node
    * Owner node stores file and the replicas synchronouslu
    * Owner node updates directory file
    * Ownder node response to entry node who reponds to client

  * Retrieve
    * Client sends retrieve request to entry node
    * Entry node checks if it has the file (owner or a replica) and responds with the file it it does.
    * Entry node determines the owner node and asks it for the file
    * Owner node returns the file to entry node who returns it to client

  * Delete
    * Client sends delete request to entry node
    * Entry node determines who is the owner node
    * Entry node requests that owner node deletes the file
    * Owner node deletes replicas and its own master copy
    * Owner node updates the directory file
    * Ownder node returns to entry node who returns to client

  * Update
    * Implement as a delete and store
    * Consider as stretch goal supporting append

## Directory List Operations ##
  * Add file
  * Remove file
  * (stretch goal) search by prefix

## Cluster Joining ##
  * Joiner discovers the network and gets a list of nodes
  * Joiner adds itself to the cluster
  * Adds all files in its repository to the directory list(any failures because the file is already in the directory list can be just ignored)

## Cluster Join Event ##
  * Event is received on all existing cluster nodes and they each determine that for each file for wwhich they are the master, whether joining node needs to become new master or whether it will become a replica
    * If it is to become the new master then ask joiner if they already have the file and if so, subject to CRC checking, the joiner is finished but otherwise the file needs to be sent to the joiner.
    * If it is to become a replica perform the MasterReplicationProcess.

## Cluster Leave Event ##
  * Each node upon receiving the leave event checks all its own files and determines that for each such file:
    * If it is a replica of a master file in the leaving node check if we need to become the new master and if I should then perform the MasterReplicationProcess
    * If it is a master file and there was a replica in the the leaving node then we need perform the MasterReplicationProcess.