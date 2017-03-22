package smallwheel.mybro.support;
import smallwheel.mybro.common.Constants;
import smallwheel.mybro.support.builder.SqlMapperBuilder;
import smallwheel.mybro.support.builder.SqlMapperBuilderForIbatis;
import smallwheel.mybro.support.builder.SqlMapperBuilderForMybatis;

/**
 * SqlMapperBuilderFactory
 * 
 * @author yeonhooo
 */
public class SqlMapperBuilderFactory {

	/**
	 * mapperType�� �ش��ϴ� SqlMapperBuilder�� �����Ѵ�.
	 * 
	 * @param mapperType
	 * @return mapperType�� SqlMapperBuilder
	 */
	public SqlMapperBuilder createSqlMapperBuilder(String mapperType) {

		// �⺻ ���۴� MyBatis�� �����Ѵ�
		SqlMapperBuilder builder = new SqlMapperBuilderForMybatis();

		if (mapperType.equalsIgnoreCase(Constants.Mapper.IBATIS)) {
			builder = new SqlMapperBuilderForIbatis();
		}

		return builder;
	}
}
