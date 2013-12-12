package cc.nlp.instances;


/**
 * 对于parsed的结果文件，其有四个部分的内容，这四个部分的数据的记录条数应该是一样的
 * 在将parsed结果文件转换为instance的过程中，如果发现有数目不一致的记录则抛出该异常
 * @author cc
 *
 */
public class NotDependencyStringMatchedException extends Exception{
	private static final long serialVersionUID = 1L;
	
	public NotDependencyStringMatchedException(){
		super();
	}
	
	public NotDependencyStringMatchedException(String message){
		super(message);
	}
	
	public NotDependencyStringMatchedException(String message, Throwable cause){
		super(message, cause);
	}
	
	public NotDependencyStringMatchedException(Throwable cause){
		super(cause);
	}
}
