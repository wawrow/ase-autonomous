package FileRepositoryInterfaces;

import java.util.Vector;

// Provides user methods for storing/retrieving/removing files
public interface IClientCommunication {
	
	//Stores IDataObject in the System
	public abstract void Store(IDataObject object);
	
	//Retrieves IDataObject by name
	public abstract IDataObject Retrieve(String name);
	
	//Retrieves of all available names from the system 
	public abstract Vector<String> List();
	
	//Deletes IDataObject from the System
	public abstract void Delete(String name);
	
}
