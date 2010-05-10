Using the distributed file store
================================


Requirements
------------

All servers on the same lan and all capable of multicast IP (we use a
UDP/multicast layer in jgroups).
A java6 installation (the libraries are built for java6 and we use a couple of
java6 methods in one of the calls).


To run a server
---------------

java -jar FileRepository.jar

This will start a server and create a local datastore directory 'store'.  You
can run any number of servers on each node but you should run them in different
directories since the datastore directories must be separate. The server is
ready to participate fully when the second GMS log line (for the client
interaction cluster) is visible. You can look in the store directories for the
files for a given server. This is a handy way to confirm that all files are
stored twice (eventually, some replication may not be current if nodes are
joining or leaving) and that they aren't all stored on the same server.


To run a client
---------------

java -jar FileRepositoryClient.jar

This will give you prompt. Enter 'help' to see a list of commands.  Some
commands have convenient aliases (e.g. rm for delete, ls for list).

Use 'cluster' to find the current state of the cluster (ie list of clients and
servers participating). Note that it can take a while for some servers to join
the cluster so it is best to give it a little time to stabilise.

Use 'capacity' to find the current free space in the cluster (and a measure of
total disk space in cluster which may not be totally useful). This command takes
a while to complete as it gathers all the values from each server and reduces
them to a total (counting each server machine once where possible).

Use 'list' so find all files in the cluster. This takes an optional argument, a
regular expression (Java version, close to pcre) which is used to filter the
files (on the server side). This command takes a while to complete as it gathers
the file list from all the servers (rather than maintaining a centralised file
list).

Use 'store' to save a file in the repository. Note that the command only takes
one argument and it will strip off any directory information since the
repository is a flat filestore.

'cat' can be used to examine the contents of a file without saving it. This
should only be used on a text file for obvious reasons.

'delete' gives quite a soft guarantee. If the master for the file (ie the node
responsible for guaranteeing it is replicated) dies before all replicas are gone
then it will receive the file from a replica when it comes back (or the replica
will send it to another replica). We considered implementing a journaling system
but this seemed riskier than simply keeping some files too long.

We do not support partial update of a file, the entire file must be replaced
with a new one.

The rest of the commands are reasonably clear.


Suggested Demonstration
-----------------------

Create a series of node directories under /tmp 'mkdir /tmp/node{1,2,3,4,5,6}'
Create a client directory under /tmp as well 'mkdir /tmp/client'
For each node directory, cd to the directory and run the java command above
(obviously referencing the location of the .jar file). This is usually best done
in a separate window/tab for each node.
When the servers look like they are stable (they log to screen) cd to the client
directory and run the client (see above).

Now try the commands:
'cluster'
'list'
You should see 6 servers (sometimes the servers can get a little stuck and take
a while to recover). There should be no files.
'put /etc/hosts'
to put that file there.
'list'
You should see the file listed.
from another window do 'ls /tmp/node?/store' and you should see that the file
exists in the store directories. You might need to wait a bit for full
replication (there is a 30s replication thread to clean up any stragglers).

repeat for more files and see that they don't all end up on the same nodes.
'retrieve hosts'
You should have a 'hosts' file in your client directory.



Please contact us if you need any further information on the demo (we could
build you a shell script if you wish) or have problems running it (IPv6 can
cause issues in some cases, depending on your stack).
