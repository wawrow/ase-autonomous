package FileRepositoryInterfaces;

import java.util.Vector;


//Provides methods to call by other nodes in the system
public interface ISystemCommunication {
	
	//Returns a list of known nodes - might not be required since we use JGroups - we'll see
	public abstract Vector<INodeDescriptor> SyncNodelist();
	
	//Returns file list of files that are in nodes data storage 
	public abstract Vector<String> SyncFilelist();
	
	//Stores a file in nodes data storage
	public abstract void Store(IDataObject dataObject);
	
	//Retrieve a file form nodes storage
	public abstract IDataObject Retrieve(String name);
}
