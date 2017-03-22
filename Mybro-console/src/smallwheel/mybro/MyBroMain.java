package smallwheel.mybro;
import org.apache.log4j.Logger;

import smallwheel.mybro.common.ENV;
import smallwheel.mybro.common.SharedInfo;
import smallwheel.mybro.support.SqlMapperBuilderFactory;
import smallwheel.mybro.support.builder.DtoClassBuilder;
import smallwheel.mybro.support.builder.MapperInterfaceBuilder;
import smallwheel.mybro.support.builder.SqlMapperBuilder;

/**
 * MyBro ���� Ŭ����
 * 
 * TODO: DB Connection ���� ==> Connection Pool
 * TODO: ��Ű�� ��� ���� �Է¹޾�, ��Ű�� ���� �� �ش� ��Ű�� ��ο� ���� �߰�
 * TODO: Mapper.java ���� ���� ��� 
 *
 * @author yeonhooo@gmail.com
 */
public class MyBroMain {
	
	private final static Logger LOGGER = Logger.getLogger(MyBroMain.class);
	
	private DtoClassBuilder dtoClassBuilder;
	private SqlMapperBuilder sqlMapperBuilder;
	private MapperInterfaceBuilder mapperInterfaceBuilder;

	public static void main(String[] args) {
		MyBroMain main = new MyBroMain();
		main.run();
	}

	private void init() {
		// ȯ���ʱ�ȭ , ȯ�����Ͽ��� ������ �ε�
		ENV.init();
		
		// dtoClassBuilder ����
		dtoClassBuilder = new DtoClassBuilder();
		
		// Ÿ�Ժ�(ibatis, mybatis) sqlMapperBuilder ����
		SqlMapperBuilderFactory factory = new SqlMapperBuilderFactory();
		sqlMapperBuilder = factory.createSqlMapperBuilder(ENV.mapperType);
		
		// mapperInterfaceBuilder ����
		mapperInterfaceBuilder = new MapperInterfaceBuilder();
	}

	private void run() {
		
		init();
		SharedInfo.getInstance().load();

		// step1. �� Ŭ���� ������ �����Ѵ�.
		dtoClassBuilder.build();
		
		// step2. SqlMap.xml ������ �����Ѵ�.
		sqlMapperBuilder.build();
		
		// step3. mapper interface ������ �����Ѵ�.
		mapperInterfaceBuilder.build();
		
		LOGGER.info("### " + SharedInfo.getInstance().getTableInfoList().size() + "�� ���̺� ���� �۾��� �Ϸ�Ǿ����ϴ�." );
	}
}
