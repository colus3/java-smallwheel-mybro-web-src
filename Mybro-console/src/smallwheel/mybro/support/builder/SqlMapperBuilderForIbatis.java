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
import smallwheel.mybro.common.Constants;
import smallwheel.mybro.common.SharedInfo;
import smallwheel.mybro.common.TableInfo;

/**
 * 
 * Ibatis�� SqlMapperBuilder Ŭ����
 * 
 * @author yeonhooo@gmail.com
 */
public class SqlMapperBuilderForIbatis extends SqlMapperBuilder {
	
	private final static Logger LOGGER = Logger.getLogger(SqlMapperBuilderForIbatis.class);
	private final SharedInfo sharedInfo = SharedInfo.getInstance();
	
	/** 
	 * SqlMap.xml ������ �����. 
	 * @param table list 
	 * */
	@Override
	public void build() {
		
		TableInfo table;
		ClassFileInfo classFile;
		
		for (int i = 0; i < sharedInfo.getTableInfoList().size(); i++) {
			
			table = sharedInfo.getTableInfoList().get(i);
			classFile = sharedInfo.getClassFileInfoList().get(i);
			
			String tableName = table.getName();
			String entityName = table.getEntityName();
			
			final Element root = new Element("sqlMap");
			final Element typeAlias = new Element("typeAlias");
			final Element resultMap = new Element("resultMap");
			final Element sql = new Element("sql");
			final Element insert = new Element("insert");
			final Element select = new Element("select");
			final Element selectOne = new Element("select");
			final Element update = new Element("update");
			final Element delete = new Element("delete");
			
			// root ��� ����
			root.setAttribute(makeAttribute("namespace", entityName));
			
			// typeAlias ��� ����
			final String typeAliasText = "class" + classFile.getName();
			typeAlias.setAttribute(makeAttribute("alias", typeAliasText));
			typeAlias.setAttribute(makeAttribute("type", classFile.getName()));		
			
			// resultMap ��� ����
			final String resultMapText = "ret" + classFile.getName();
			resultMap.setAttribute(makeAttribute("class", typeAliasText));
			resultMap.setAttribute(makeAttribute("id", resultMapText));		
			
			// result ��� ����
			for (int j = 0; j < classFile.getPropertyList().size(); j++) {
				Element result = new Element("result");
				result.setAttribute(makeAttribute("property", classFile.getPropertyList().get(j).getName()));
				result.setAttribute(makeAttribute("javaType", classFile.getPropertyList().get(j).getType()));
				result.setAttribute(makeAttribute("column", table.getColumnInfoList().get(j).getName()));
				result.setAttribute(makeAttribute("jdbcType", table.getColumnInfoList().get(j).getType()));
				resultMap.addContent(result);
			}
			
			// dynamicWhere sql map ����
			sql.setAttribute(makeAttribute("id", "dynamicWhere"));
			sql.addContent(makeDynamicWhere(table, classFile));
			
			// insert sql map ����
			insert.setAttribute(makeAttribute("id", "insert" + entityName));
			insert.setAttribute(makeAttribute("parameterClass", typeAliasText));
			insert.addContent(makeInsertSqlMap(table, classFile));
			
			// select list sql map ����
			select.setAttribute(makeAttribute("id", "select" + entityName + "List"));
			select.setAttribute(makeAttribute("parameterClass", typeAliasText));
			select.setAttribute(makeAttribute("resultClass", typeAliasText));
			select.addContent(makeSelectSqlMap(table, classFile));
			// ���� WHERE�� ����
			select.addContent(addDynamicWhere(tableName));
			
			// select sql map ����
			selectOne.setAttribute(makeAttribute("id", "select" + entityName));
			selectOne.setAttribute(makeAttribute("parameterClass", typeAliasText));
			selectOne.setAttribute(makeAttribute("resultClass", typeAliasText));
			selectOne.addContent(makeSelectSqlMap(table, classFile));
			selectOne.addContent(makePrimaryKeyWhere(table, classFile));
			
			// update sql map ����
			update.setAttribute(makeAttribute("id", "update" + entityName));
			update.setAttribute(makeAttribute("parameterClass", typeAliasText));
			update.addContent(makeUpdateSqlMapHead(tableName));
			update.addContent(makeDynamicUpdateSqlMap(table, classFile));
			update.addContent(makePrimaryKeyWhere(table, classFile));
			
			// delete sql map ����
			delete.setAttribute(makeAttribute("id", "delete" + entityName));
			delete.setAttribute(makeAttribute("parameterClass", typeAliasText));
			delete.addContent(makeDeleteSqlMap(tableName));
			delete.addContent(makePrimaryKeyWhere(table, classFile));
			
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
			
			// DTD ���� ��, ���Ϸ� ����
			/*
			<!DOCTYPE sqlMap      
    			PUBLIC "-//ibatis.apache.org//DTD SQL Map 2.0//EN"      
    			"http://ibatis.apache.org/dtd/sql-map-2.dtd">
			 */
			docType = new DocType(Constants.Mapper.IBATIS_ELEMENT_NAME, Constants.Mapper.IBATIS_PUBLIC_ID, Constants.Mapper.IBATIS_SYSTEM_ID);
			doc = new Document(root, docType);
			try {
				// ������ XML ���� �����Ѵ�.
				FileOutputStream fos = new FileOutputStream(Constants.Path.SQL_MAPPER_DES_DIR + entityName + ".sqlmap.xml");
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
		}
	}

	/**
	 * ���� WHERE�� ����
	 * @param tableName
	 * @return
	 */
	private Element makeDynamicWhere(TableInfo table, ClassFileInfo classFile) {
		Element dynamic = new Element("dynamic");
		dynamic.setAttribute(makeAttribute("prepend", "WHERE"));
		
		Element isNotEmpty = null;
		Element isGreaterThan = null;
		
		/* isNotEmpty ��� ����
		 * <isNotEmpty property="apprvFlag" prepend="AND">
				APPRV_FLAG = #apprvFlag#
			</isNotEmpty>
		 */
		for (int i = 0; i < classFile.getPropertyList().size(); i++) {
			
			if ("INT".equals(table.getColumnInfoList().get(i).getType().toUpperCase())) {
				isGreaterThan = new Element("isGreaterThan");
				isGreaterThan.setAttribute(makeAttribute("property", classFile.getPropertyList().get(i).getName()));
				isGreaterThan.setAttribute(makeAttribute("prepend", "AND"));
				isGreaterThan.setAttribute(makeAttribute("compareValue", "0"));
				isGreaterThan.addContent("\n\t\t\t\t" + table.getColumnInfoList().get(i).getName() + " = #" + classFile.getPropertyList().get(i).getName() + "#\n\t\t\t");
				dynamic.addContent(isGreaterThan);
			} else {
				isNotEmpty = new Element("isNotEmpty");
				isNotEmpty.setAttribute(makeAttribute("property", classFile.getPropertyList().get(i).getName()));
				isNotEmpty.setAttribute(makeAttribute("prepend", "AND"));
				isNotEmpty.addContent("\n\t\t\t\t" + table.getColumnInfoList().get(i).getName() + " = #" + classFile.getPropertyList().get(i).getName() + "#\n\t\t\t");
				dynamic.addContent(isNotEmpty);
			}
		}
		
		return dynamic;
	}
	
	/**
	 * PK �������� �̷��� WHERE�� ����
	 * @param tableName
	 * @return
	 */
	private String makePrimaryKeyWhere(TableInfo table, ClassFileInfo classFile) {

		List<String> primaryKeyColumnNameList = table.getPrimaryKeyColumnNameList();
		List<String> propertyPrimaryKeyNameList = classFile.getPropertyPrimaryKeyNameList();
		
		String sql = "\n\t\t" + "WHERE";

		for (int i = 0; i < primaryKeyColumnNameList.size(); i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + primaryKeyColumnNameList.get(i) + " = #" + propertyPrimaryKeyNameList.get(i) + "#";
			} else {
				sql = sql + "\n\t\t\t" + "AND " + primaryKeyColumnNameList.get(i) + " = #" + propertyPrimaryKeyNameList.get(i) + "#";
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
				sql = sql + "\n\t\t\t#" + classFile.getPropertyList().get(i).getName() + "# ";
			} else {
				sql = sql + "\n\t\t\t," + "#" + classFile.getPropertyList().get(i).getName() + "# ";
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
		sql = sql + "\n\t\tFROM " + table.getName() + "\n\t\t";
		return sql;
	}
	
	/** update ������ �ۼ� */
	@SuppressWarnings("unused")
	private String makeUpdateSqlMap(TableInfo table, ClassFileInfo classFile) {
		String sql = "\n\t\tUPDATE " + table.getName() + " \n\t\tSET";
		for (int i = 0; i < classFile.getPropertyList().size(); i++) {
			if (i == 0) {
				sql = sql + "\n\t\t\t" + table.getColumnInfoList().get(i).getName() + " = " + "#" + classFile.getPropertyList().get(i).getName() + "# ";
			} else {
				sql = sql + "\n\t\t\t" + "," + table.getColumnInfoList().get(i).getName() + " = " + "#" + classFile.getPropertyList().get(i).getName() + "# ";
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
		String sql = "\n\t\tUPDATE " + tableName + " \n\t\tSET";
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
		
		Element dynamic = new Element("dynamic");
		Element isNotEmpty = null;
		Element isGreaterThan = null;

		for (int i = 0; i < classFile.getPropertyList().size(); i++) {
			
			if ("INT".equals(table.getColumnInfoList().get(i).getType().toUpperCase())) {
				isGreaterThan = new Element("isGreaterThan");
				isGreaterThan.setAttribute(makeAttribute("property", classFile.getPropertyList().get(i).getName()));
//				isGreaterThan.setAttribute(makeAttribute("prepend", ","));
				isGreaterThan.setAttribute(makeAttribute("compareValue", "0"));
				isGreaterThan.addContent("\n\t\t\t\t, " + table.getColumnInfoList().get(i).getName() + " = #" + classFile.getPropertyList().get(i).getName() + "#\n\t\t\t");
				dynamic.addContent(isGreaterThan);
			} else {
				isNotEmpty = new Element("isNotEmpty");
				isNotEmpty.setAttribute(makeAttribute("property", classFile.getPropertyList().get(i).getName()));
//				isNotEmpty.setAttribute(makeAttribute("prepend", ","));
				isNotEmpty.addContent("\n\t\t\t\t, " + table.getColumnInfoList().get(i).getName() + " = #" + classFile.getPropertyList().get(i).getName() + "#\n\t\t\t");
				dynamic.addContent(isNotEmpty);
			}
			
		}
		
		return dynamic;
	}
	
	/** delete ������ �ۼ� */
	private String makeDeleteSqlMap(String tableName) {
		String sql = "\n\t\tDELETE FROM " + tableName + "\n\t\t";
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
