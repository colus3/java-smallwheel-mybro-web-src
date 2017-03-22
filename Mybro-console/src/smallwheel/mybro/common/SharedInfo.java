package smallwheel.mybro.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import smallwheel.mybro.support.builder.DtoClassBuilder;

/**
 * ���̺� ���� �޸� Ŭ����
 * 
 * <pre>
 * DB���� ���̺� ������ ��ȸ�Ͽ� �޸𸮿� ����
 * </pre>
 * 
 * @author yeonhooo@gmail.com
 */
public class SharedInfo {
	private final Logger logger = Logger.getLogger(DtoClassBuilder.class);

	private static SharedInfo sharedInfo = new SharedInfo();
	private List<TableInfo> tableInfoList = new ArrayList<>();
	private List<ClassFileInfo> classFileInfoList = new ArrayList<>();
	private List<MapperInterfaceInfo> mapperInterfaceInfoList = new ArrayList<>();

	// ���̺� ���� ���� ����
	private final String tables = ContextMaster.getString("TABLES");

	// singleton
	private SharedInfo() {
	}

	public static SharedInfo getInstance() {
		return sharedInfo;
	}

	public void load() {

		// DB ����
		DBManager dbm = new DBManager();
		dbm.checkConnection(ENV.dbms);
		Connection con = dbm.getConnection(ENV.dbms);
		PreparedStatement pstmt;
		DatabaseMetaData databaseMetaData;
		ResultSet rs;
		ResultSetMetaData rm;

		try {
			for (String tableName : tables.split(",")) {

				tableName = tableName.trim();
				TableInfo tableInfo = new TableInfo();
				ClassFileInfo classInfo = new ClassFileInfo();
				tableInfo.setName(tableName);

				pstmt = con.prepareStatement("select * from " + tableInfo.getName() + " where 1=0");
				databaseMetaData = con.getMetaData();
				rs = pstmt.executeQuery();
				rm = rs.getMetaData();

				// Table�� Column ������ �����´�.
				for (int i = 1; i <= rm.getColumnCount(); i++) {
					tableInfo.getColumnInfoList().add(new ColumnInfo(rm.getColumnName(i), rm.getColumnTypeName(i)));
				}

				// Table�� PK ������ �����´�.
				ResultSet keys = databaseMetaData.getPrimaryKeys(null, null, tableInfo.getName());
				while (keys.next()) {
					tableInfo.getPrimaryKeyColumnNameList().add(keys.getString("COLUMN_NAME"));
					classInfo.getPropertyPrimaryKeyNameList().add(makePropertyName(keys.getString("COLUMN_NAME")));
				}

				logger.info("[Table Name: " + tableInfo.getName() + " / Column Count: " + rm.getColumnCount() + "]");
				logger.info("PK Columns");
				for (String key : tableInfo.getPrimaryKeyColumnNameList()) {
					logger.info("\t" + key);
				}

				// EntityName �� �����
				tableInfo.setEntityName(makeEntityName(tableInfo.getName(), ENV.prefixExcept));

				// ClassName �� �����.
				classInfo.setName(makeClassName(tableInfo.getEntityName()));

				for (int i = 0; i < rm.getColumnCount(); i++) {
					classInfo.getPropertyList().add(
							new PropertyInfo(makePropertyName(tableInfo.getColumnInfoList().get(i).getName()), makePropertyType(tableInfo
									.getColumnInfoList().get(i).getType())));
				}

				tableInfoList.add(tableInfo);
				classFileInfoList.add(classInfo);

				rs.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * ��ƼƼ���� �����.
	 * 
	 * @param prefixExcept ��ƼƼ���� ������ ���ڿ�
	 */
	private String makeEntityName(String tableName, String prefixExcept) {

		// prefixExcept�� ��ƼƼ���� �����Ѵ�.
		tableName = tableName.replaceAll(prefixExcept, "");
		tableName = tableName.toLowerCase(Locale.ENGLISH);

		while (true) {
			if (tableName.indexOf("_") > -1) {
				tableName = (tableName.substring(0, tableName.indexOf("_"))
						+ tableName.substring(tableName.indexOf("_") + 1, tableName.indexOf("_") + 2).toUpperCase() + tableName
						.substring(tableName.indexOf("_") + 2)).trim();
			} else {
				break;
			}
		}

		// ù ���ڸ� �빮�ڷ� �����Ѵ�.
		tableName = tableName.substring(0, 1).toUpperCase() + tableName.substring(1);
		return tableName;
	}

	/** Ŭ�������� �����. */
	private String makeClassName(String entityName) {
		return entityName + ENV.classNameSuffix;
	}

	/**
	 * Ŭ�������� ������Ƽ��(property)�� �����.
	 * 
	 * @param ��ȯ �� ���� DB �÷���
	 * @return ��ȯ �� ������Ƽ��(DB �÷���� ��Ī)
	 * */
	private String makePropertyName(String columnName) {
		columnName = columnName.toLowerCase(Locale.ENGLISH);

		while (true) {
			if (columnName.indexOf("_") > -1) {
				columnName = (columnName.substring(0, columnName.indexOf("_"))
						+ columnName.substring(columnName.indexOf("_") + 1, columnName.indexOf("_") + 2).toUpperCase() + columnName
						.substring(columnName.indexOf("_") + 2)).trim();
			} else {
				break;
			}
		}

//		// ����� ������Ƽ���� �ڹ� ������̰ų�, ���������� ��쿡 ���� ó��		
//		if (columnName.equals("continue")) {
//			columnName = "continues";
//		} else if (columnName.equals("r")) {
//			columnName = "run";
//		} else if (columnName.equals("w")) {
//			columnName = "win";
//		} else if (columnName.equals("l")) {
//			columnName = "lose";
//		} else if (columnName.equals("d")) {
//			columnName = "draw";
//		} else if (columnName.equals("s")) {
//			columnName = "save";
//		}

		return columnName;
	}

	/**
	 * ȯ�� ���� ���Ͽ� ������ ���յ��� ���� Ŭ������ ������Ƽ Ÿ���� �����.<br />
	 * <br />
	 * <strong>���յ� Ÿ��</strong><br />
	 * 
	 * ����(<code>HIGH</code>): DB�� ������ Ÿ���� �ڹ� ������Ƽ Ÿ�� ��ȯ<br />
	 * ����(<code>MIDDLE</code>): DB Ÿ�� �� �������� ��¥���� ����. �� �� Ÿ���� ���ڿ� Ÿ������ ��ȯ<br />
	 * ����(<code>LOW</code>): DB Ÿ�� �� �������� ��ȯ. �� �� Ÿ���� ���ڿ� Ÿ������ ��ȯ<br />
	 * ����(<code>NO</code>): ��� DB Ÿ���� ���ڿ� Ÿ������ ��ȯ
	 * 
	 * @param ��ȯ �� ���� DB �÷� Ÿ��
	 * @return ��ȯ �� ������Ƽ Ÿ��(DB �÷� Ÿ�԰� ��Ī)
	 * */
	private String makePropertyType(String columnType) {
		String propertyType = columnType.toUpperCase();

		if ("HIGH".equals(ENV.couplingType)) {
			// ���յ� ����
			if (propertyType.equals("TINYINT") || propertyType.equals("SMALLINT") || propertyType.equals("MEDIUMINT")
					|| propertyType.equals("INT") || propertyType.equals("BIGINT")) {
				propertyType = "int";
			} else if (propertyType.equals("FLOAT")) {
				propertyType = "float";
			} else if (propertyType.equals("DOUBLE") || propertyType.equals("DECIMAL")) {
				propertyType = "double";
			} else if (propertyType.equals("CHAR") || propertyType.equals("VARCHAR") || propertyType.equals("TEXT")
					|| propertyType.indexOf("BLOB") > -1) {
				propertyType = "String";
			} else if (propertyType.equals("DATE") || propertyType.equals("DATETIME") || propertyType.equals("TIMESTAMP")) {
				propertyType = "Date";
			} else {
				propertyType = "String";
			}

		} else if ("MIDDLE".equals(ENV.couplingType)) {
			// ���յ� ����
			if (propertyType.equals("TINYINT") || propertyType.equals("SMALLINT") || propertyType.equals("MEDIUMINT")
					|| propertyType.equals("INT") || propertyType.equals("BIGINT")) {
				propertyType = "int";
			} else if (propertyType.equals("DATE") || propertyType.equals("DATETIME") || propertyType.equals("TIMESTAMP")) {
				propertyType = "Date";
			} else {
				propertyType = "String";
			}

		} else if ("LOW".equals(ENV.couplingType)) {
			// ���յ� ����
			if (propertyType.equals("TINYINT") || propertyType.equals("SMALLINT") || propertyType.equals("MEDIUMINT")
					|| propertyType.equals("INT") || propertyType.equals("BIGINT")) {
				propertyType = "int";
			} else {
				propertyType = "String";
			}

		} else if ("NO".equals(ENV.couplingType)) {
			// ���յ� ����
			propertyType = "String";
		}

		return propertyType;
	}

	public List<TableInfo> getTableInfoList() {
		return tableInfoList;
	}

	public List<ClassFileInfo> getClassFileInfoList() {
		return classFileInfoList;
	}

	public List<MapperInterfaceInfo> getMapperInterfaceInfoList() {
		return mapperInterfaceInfoList;
	}

}