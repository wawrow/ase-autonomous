package FileRepositoryInterfaces;

public interface INode {
	
	public abstract long[] GetIds();
	
	//Below will be used in Template Methods in abstract class for Node
	public abstract void Start();
	public abstract void InitializeDataStore();
	public abstract void JoinTheNetwork();
	
	//This method will loop to keep check the replica nodes and keep the replica count
	public abstract void RelpicaGuard();

}
