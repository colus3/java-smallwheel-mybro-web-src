package smallwheel.mybro.support.builder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import smallwheel.mybro.common.ClassFileInfo;
import smallwheel.mybro.common.ColumnInfo;
import smallwheel.mybro.common.Constants;
import smallwheel.mybro.common.MapperInterfaceInfo;
import smallwheel.mybro.common.PropertyInfo;
import smallwheel.mybro.common.SqlMapInfo;
import smallwheel.mybro.common.TableInfo;
import smallwheel.mybro.common.SharedInfo;

/**
 * Mybatis�� SqlMapperBuilder Ŭ����
 * 
 * @author yeonhooo@gmail.com
 */
public class SqlMapperBuilderForMybatis extends SqlMapperBuilder {
	
	private final static Logger LOGGER = Logger.getLogger(SqlMapperBuilderForMybatis.class);
	private final SharedInfo sharedInfo = SharedInfo.getInstance();
	
	/** 
	 * SqlMap.xml ������ �����. 
	 * @param table list 
	 * */
	@Override
	public void build() {
		
		TableInfo table;
		ClassFileInfo classFile;
		MapperInterfaceInfo mapperInterfaceFile; 
		
		List<MapperInterfaceInfo> mapperInterfaceInfoList = sharedInfo.getMapperInterfaceInfoList();
		
		for (int i = 0; i < sharedInfo.getTableInfoList().size(); i++) {
			
			table = sharedInfo.getTableInfoList().get(i);
			classFile = sharedInfo.getClassFileInfoList().get(i);
			mapperInterfaceFile = new MapperInterfaceInfo();
			
			String tableName = table.getName();
			String entityName = table.getEntityName();
			mapperInterfaceFile.setName(classFile.getName() + Constants.Mapper.MAPPER_INTERFACE_SUFFIX);
			
			final Element root = new Element("mapper");
			final Element typeAlias = new Element("typeAlias");
			final Element resultMap = new Element("resultMap");
			final Element sql = new Element("sql");
			final Element insert = new Element("insert");
			final Element select = new Element("select");
			final Element selectOne = new Element("select");
			final Element update = new Element("update");
			final Element delete = new Element("delete");
			String sqlMapId;
			
			// root ��� ����
			root.setAttribute(makeAttribute("namespace", table.getEntityName()));
			
			// typeAlias ��� ����
	//		String typeAliasText = "class" + classFile.getClassName();
			final String typeAliasText = classFile.getName();
			typeAlias.setAttribute(makeAttribute("alias", typeAliasText));
			typeAlias.setAttribute(makeAttribute("type", classFile.getName()));		
			
			// resultMap ��� ����
			final String resultMapText = "ret" + classFile.getName();
			resultMap.setAttribute(makeAttribute("type", typeAliasText));
			resultMap.setAttribute(makeAttribute("id", resultMapText));		
			
			// result ��� ����
			for (int j = 0; j < classFile.getPropertyList().size(); j++) {
				Element result = new Element("result");
				result.setAttribute(makeAttribute("property", classFile.getPropertyList().get(j).getName()));
				result.setAttribute(makeAttribute("javaType", classFile.getPropertyList().get(j).getType()));
				result.setAttribute(makeAttribute("column", table.getColumnInfoList().get(j).getName()));
	//			result.setAttribute(makeAttribute("jdbcType", classFile.dbColumnTypeList[i]));
				resultMap.addContent(result);
			}
			
			// dynamicWhere sql map ����
			sql.setAttribute(makeAttribute("id", "dynamicWhere"));
			sql.addContent(makeDynamicWhere(table.getColumnInfoList(), classFile.getPropertyList()));
			
			// insert sql map ����
			sqlMapId = "insert" + entityName;
			mapperInterfaceFile.getSqlMapInfoList().add(new SqlMapInfo(sqlMapId, "int"));
			insert.setAttribute(makeAttribute("id", sqlMapId));
			insert.setAttribute(makeAttribute("parameterType", typeAliasText));
			insert.addContent(makeInsertSqlMap(table, classFile));
			
			// select list sql map ����
			sqlMapId = "select" + entityName + "List";
			mapperInterfaceFile.getSqlMapInfoList().add(new SqlMapInfo(sqlMapId, "List<" + classFile.getName() + ">"));
			select.setAttribute(makeAttribute("id", sqlMapId));
			select.setAttribute(makeAttribute("parameterType", typeAliasText));
			select.setAttribute(makeAttribute("resultType", typeAliasText));
			select.addContent(makeSelectSqlMap(table, classFile));
			// ���� WHERE�� ����
			select.addContent(addDynamicWhere(tableName));
			
			// select sql map ����
			sqlMapId = "select" + entityName;
			mapperInterfaceFile.getSqlMapInfoList().add(new SqlMapInfo(sqlMapId, classFile.getName()));
			selectOne.setAttribute(makeAttribute("id", sqlMapId));
			selectOne.setAttribute(makeAttribute("parameterType", typeAliasText));
			selectOne.setAttribute(makeAttribute("resultType", typeAliasText));
			selectOne.addContent(makeSelectSqlMap(table, classFile));
			selectOne.addContent(makePrimaryKeyWhere(table.getPrimaryKeyColumnNameList(), classFile.getPropertyPrimaryKeyNameList()));
			
			// update sql map ����
			sqlMapId = "update" + entityName;
			mapperInterfaceFile.getSqlMapInfoList().add(new SqlMapInfo(sqlMapId, "int"));
			update.setAttribute(makeAttribute("id", sqlMapId));
			update.setAttribute(makeAttribute("parameterType", typeAliasText));
			update.addContent(makeUpdateSqlMapHead(tableName));
			update.addContent(makeDynamicUpdateSqlMap(table, classFile));
			update.addContent(makePrimaryKeyWhere(table.getPrimaryKeyColumnNameList(), classFile.getPropertyPrimaryKeyNameList()));
			
			// delete sql map ����
			sqlMapId = "delete" + entityName;
			mapperInterfaceFile.getSqlMapInfoList().add(new SqlMapInfo(sqlMapId, "int"));
			delete.setAttribute(makeAttribute("id", sqlMapId));
			delete.setAttribute(makeAttribute("parameterType", typeAliasText));
			delete.addContent(makeDeleteSqlMap(tableName));
			delete.addContent(makePrimaryKeyWhere(table.getPrimaryKeyColumnNameList(), classFile.getPropertyPrimaryKeyNameList()));
			
			// root �� �߰�
			root.addContent(new Comment(" Use type aliases to avoid typing the full class name every time. "));
			root.addContent(typeAlias);
			root.addContent(resultMap);
			root.addContent("\n");
			
			root.addContent(new Comment(" Dynamic Where Condition "));
			root.addContent(sql);
			root.addContent("\n");
			
			root.addContent(new Comment(" Insert " + tableName + " "));
			root.addContent(insert);
			root.addContent("\n");
			
			root.addContent(new Comment(" Select " + tableName + " List "));
			root.addContent(select);
			root.addContent("\n");
			
			root.addContent(new Comment(" Select " + tableName + " "));
			root.addContent(selectOne);
			root.addContent("\n");
			
			root.addContent(new Comment(" Update " + tableName + " "));
			root.addContent(update);
			root.addContent("\n");
			
			root.addContent(new Comment(" Delete " + tableName + " "));
			root.addContent(delete);
			
			/* DTD ���� ��, ���Ϸ� ����
			 * iBatis
			 * 	<!DOCTYPE sqlMap      
			 * 		PUBLIC "-//ibatis.apache.org//DTD SQL Map 2.0//EN"	
			 * 		"http://ibatis.apache.org/dtd/sql-map-2.dtd">
			 * 
			 * MyBatis
			 * 	<!DOCTYPE mapper
			 * 		PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
			 * 		"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
			 */
			DocType docType = new DocType(Constants.Mapper.MYBATIS_ELEMENT_NAME, Constants.Mapper.MYBATIS_PUBLIC_ID, Constants.Mapper.MYBATIS_SYSTEM_ID);
			Document doc = new Document(root, docType);
			try {
				// ������ XML ���� �����Ѵ�.
				FileOutputStream fos = new FileOutputStream(Constants.Path.SQL_MAPPER_DES_DIR + entityName + "Mapper.xml");
				XMLOutputter serializer = new XMLOutputter();
	//			XMLOutputter serializer = new XMLOutputter(Format.getPrettyFormat());
				
				// �⺻ ���� ���¸� �ҷ��� �����Ѵ�.
				Format fm = serializer.getFormat();
				// ���ڵ� ����
				fm.setEncoding("UTF-8");
				// �θ�, �ڽ� �±׸� �����ϱ� ���� �� ������ ���Ѵ�.
				fm.setIndent("\t");
				// �±װ� �ٹٲ��� �����Ѵ�.
				fm.setLineSeparator("\n");
				
				// ������ XML ������ ������ set �Ѵ�.
				serializer.setFormat(fm);
				
				// doc �� ������ fos �Ͽ� ������ �����Ѵ�.
				serializer.output(doc, fos);
				
				fos.flush();
				fos.close();
				
			} catch (FileNotFoundException e) {
				LOGGER.error(e.getMessage(), e);
			} catch (IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
			
			mapperInterfaceInfoList.add(mapperInterfaceFile);
		
		}
	}

	/**
	 * ���� WHERE�� ����
	 * @param columnList 
	 * @param propertyList 
	 * @param tableName
	 * @return
	 */
	private Element makeDynamicWhere(List<ColumnInfo> columnList, List<PropertyInfo> propertyList) {
		Element dynamic = new Element("where");
		Element ifTest = null;
		
		/* if ��� ����
		 * <if test = 'propertyName != null and propertyName != "" >
		 */
		for (int i = 0; i < columnList.size(); i++) {

			if ("INT".equals(columnList.get(i).getType().toUpperCase())) {
				ifTest = new Element("if");
				ifTest.setAttribute(makeAttribute("test", propertyList.get(i).getName() + " > 0"));
				ifTest.addContent("\n\t\t\t\tAND " + columnList.get(i).getName() + " = #{" + propertyList.get(i).getName() + "}\n\t\t\t");
				dynamic.addContent(ifTest);
			} else {
				ifTest = new Element("if");
				ifTest.setAttribute(makeAttribute("test", propertyList.get(i).getName() + " != null and " + propertyList.get(i).getName() + " != ''"));
				ifTest.addContent("\n\t\t\t\tAND " + columnList.get(i).getName() + " = #{" + propertyList.get(i).getName() + "}\n\t\t\t");
				dynamic.addContent(ifTest);
			}
		}
		
		return dynamic;
	}
	
	/**
	 * PK �������� �̷��� WHERE�� ����
	 * @param primaryKeyColumnNameList
	 * @param propertyPrimaryKeyNameList 
	 * @return
	 */
	private String makePrimaryKeyWhere(List<String> primaryKeyColumnNameList, List<String> propertyPrimaryKeyNameList) {
		String sql = "\n\t\t" + "WHERE";

		for (int i = 0; i < primaryKeyColumnNameList.size(); i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + primaryKeyColumnNameList.get(i) + " = #{" + propertyPrimaryKeyNameList.get(i) + "}";
			} else {
				sql = sql + "\n\t\t\t" + "AND " + primaryKeyColumnNameList.get(i) + " = #{" + propertyPrimaryKeyNameList.get(i) + "}";
			}
		}
		sql += "\n\t";
		return sql;
	}
	

	/**
	 * ������ WHERE�� �߰�
	 * @param tableName
	 * @return
	 */
	private Element addDynamicWhere(String tableName) {
		Element include = new Element("include");
		include.setAttribute(makeAttribute("refid", "dynamicWhere"));
		return include;
	}
	
	/** insert ������ �ۼ� */
	private String makeInsertSqlMap(TableInfo table, ClassFileInfo classFile) {
		String sql = "\n\t\tINSERT INTO " + table.getName() + " ( ";
		for (int i = 0; i < classFile.getPropertyList().size(); i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + table.getColumnInfoList().get(i).getName();
			} else {
				sql = sql + "\n\t\t\t" + "," + table.getColumnInfoList().get(i).getName();
			}
		}
		sql += "\n\t\t) VALUES (";
		for (int i = 0; i < classFile.getPropertyList().size(); i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t#{" + classFile.getPropertyList().get(i).getName() + "} ";
			} else {
				sql = sql + "\n\t\t\t," + "#{" + classFile.getPropertyList().get(i).getName() + "} ";
			}
		}
		sql += "\n\t\t);\n\t";
		return sql;
	}
	
	/** select ������ �ۼ� */
	private String makeSelectSqlMap(TableInfo table, ClassFileInfo classFile) {
		String sql = "\n\t\tSELECT ";
		for (int i = 0; i < classFile.getPropertyList().size(); i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + table.getColumnInfoList().get(i).getName() + "\tAS " + classFile.getPropertyList().get(i).getName();
			} else {
				sql = sql + "\n\t\t\t" + "," + table.getColumnInfoList().get(i).getName() + "\tAS " + classFile.getPropertyList().get(i).getName();
			}
		}
		sql = sql + "\n\t\tFROM " + table.getName() + "\t\t";
		return sql;
	}
	
	/** update ������ �ۼ� */
	@SuppressWarnings("unused")
	private String makeUpdateSqlMap(TableInfo table, ClassFileInfo classFile) {
		String sql = "\n\t\tUPDATE " + table.getName() + " \n\t\tSET";
		for (int i = 0; i < classFile.getPropertyList().size(); i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + table.getColumnInfoList().get(i).getName() + " = " + "#{" + classFile.getPropertyList().get(i).getName() + "} ";
			} else {
				sql = sql + "\n\t\t\t" + "," + table.getColumnInfoList().get(i).getName() + " = " + "#{" + classFile.getPropertyList().get(i).getName() + "} ";
			}
		}
		sql += "\n\t\t";
		return sql;
	}
	
	/**
	 * update ������ ���
	 * @param tableName
	 * @return
	 */
	private String makeUpdateSqlMapHead(String tableName) {
		String sql = "\n\t\tUPDATE " + tableName;
		return sql;
	}
	
	/**
	 * ���� update ������ �ۼ� 
	 * ��) <isNotEmpty property="applyName">,APPLY_NAME = #applyName# </isNotEmpty>
	 * 
	 * prepend �� ������� �ʴ� ������ ����
	 * @param tableName
	 * @return
	 */
	private Element makeDynamicUpdateSqlMap(TableInfo table, ClassFileInfo classFile) {
		
		Element dynamic = new Element("trim");
		dynamic.setAttribute(makeAttribute("prefix", "SET"));
		dynamic.setAttribute(makeAttribute("prefixOverrides", ","));
		
		Element ifTest = null;
		
		PropertyListLoop: for (int i = 0; i < classFile.getPropertyList().size(); i++) {
			
			for (String pkPropertyName : classFile.getPropertyPrimaryKeyNameList()) {
				if (pkPropertyName.equals(classFile.getPropertyList().get(i).getName())) {
					continue PropertyListLoop;
				}
			}
			
			if ("INT".equals(table.getColumnInfoList().get(i).getType().toUpperCase())) {
				ifTest = new Element("if");
				ifTest.setAttribute(makeAttribute("test", classFile.getPropertyList().get(i).getName() + " > 0"));
				ifTest.addContent("\n\t\t\t\t, " + table.getColumnInfoList().get(i).getName() + " = #{" + classFile.getPropertyList().get(i).getName() + "}\n\t\t\t");
				dynamic.addContent(ifTest);
			} else {
				ifTest = new Element("if");
				ifTest.setAttribute(makeAttribute("test", classFile.getPropertyList().get(i).getName() + " != null and " + classFile.getPropertyList().get(i).getName() + " != ''"));
				ifTest.addContent("\n\t\t\t\t, " + table.getColumnInfoList().get(i).getName() + " = #{" + classFile.getPropertyList().get(i).getName() + "}\n\t\t\t");
				dynamic.addContent(ifTest);
			}
			
		}
		
		return dynamic;
	}
	
	/** delete ������ �ۼ� */
	private String makeDeleteSqlMap(String tableName) {
		String sql = "\n\t\tDELETE FROM " + tableName + "\t\t";
		return sql;
	}
	
	/**
	 * Attribute �� �����Ͽ� ��ȯ�Ѵ�.
	 * 
	 * @param attributeName
	 * @param attributeValue
	 * @return
	 */
	private Attribute makeAttribute(String attributeName, String attributeValue) {
		Attribute attribute = new Attribute(attributeName, attributeValue); 
		return attribute;
	}
	
}
