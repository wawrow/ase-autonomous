# Status #

**Fergus** I have done an initial checkin on CHT which is just a rename to ConsistentHash. This was just because I don't like acronyms unless they are in common usage, and it seems CHT is not according to Google.

So while making changes in there, I would like to know why the getIds() method is public. surely all the hash values are private to the CH and should not be published. Helper methods can be provided if necessary.

**keith** Moved to pure-jgroups rpc. signatures a lot closer to real and class structure better. Tweaked CHT interface to reflect what I think is needed as a minimum (ie treating CHT as a box we can ask questions of and feed updates to but not driving actions).

Sorry about all the revert-war stuff, apart from my client being a bit odd in Idea (and my not understanding update) I hadn't really specified what I thought the structure should be. My thinking was something like:

singleton Node which owns channel (and rpcdescriptor now Wawrzyniec points out that you can only have one). It creates a CHT to keep track of the state of the system and creates a SystemComsServer with itself as the callback class so that it can see all group state changes (which it then uses to update the CHT).

I made the CHT interface very minimal because I didn't want to have any of the filesystem logic (e.g. replicas etc) in the CHT. It seemed that belonged in either the Node or (in case Node was becoming too much of a God class) in a dedicated class that's controlled by the Node.