package fileRepository;

import com.slard.filerepository.Node;

public abstract class BaseNode implements Node{

	public void Start(){
		this.initializeDataStore();
		this.joinTheNetwork();
	  //Start the node
	}
	
}