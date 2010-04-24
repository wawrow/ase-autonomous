package FileRepositoryInterfaces;

import java.util.Vector;

//Provides internal node interface into Data Store operations
public interface IDataStore {
	
	//Gets object by name from storage
	public abstract IDataObject GetDataObject(String name);
	
	//Returns whether particular object is contained in storage
	public abstract Boolean Contains(String name);
	
	//Removes object from DataStore 
	public abstract void Delete(String name);
	
	//Stores object in DataStore
	public abstract void StoreDataObject(IDataObject dataObject);
	
	//Fetches all Objects
	public Vector<IDataObject> GetAllDataObjects();
	
	//We'll probably need also methods for free disk space usage etc. we'll add them as required.
}
