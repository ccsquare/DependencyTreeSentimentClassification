package cc.nlp.instances;


/**
 * ����parsed�Ľ���ļ��������ĸ����ֵ����ݣ����ĸ����ֵ����ݵļ�¼����Ӧ����һ����
 * �ڽ�parsed����ļ�ת��Ϊinstance�Ĺ����У������������Ŀ��һ�µļ�¼���׳����쳣
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
