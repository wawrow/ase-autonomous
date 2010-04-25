package com.slard.filerepository;

import java.util.Vector;

//Provides internal node interface into Data Store operations
public interface DataStore {
	
	//Gets object by name from storage
	public abstract DataObject GetDataObject(String name);
	
	//Returns whether particular object is contained in storage
	public abstract Boolean Contains(String name);
	
	//Removes object from DataStore 
	public abstract void Delete(String name);
	
	//Stores object in DataStore
	public abstract void StoreDataObject(DataObject dataObject);
	
	//Fetches all Objects
	public Vector<DataObject> GetAllDataObjects();
	
	//We'll probably need also methods for free disk space usage etc. we'll add them as required.
}
