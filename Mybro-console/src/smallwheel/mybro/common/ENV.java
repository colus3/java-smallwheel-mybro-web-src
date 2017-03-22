package smallwheel.mybro.common;
import java.io.File;

import org.apache.log4j.Logger;

/**
 * ���� ȯ������
 * 
 * @author yeonhooo
 *
 */
public class ENV {

	private final static Logger LOGGER = Logger.getLogger(ENV.class);
	
	public static final String VER = "DAOMaker 20110822.0940"; // ���� ����

	/** Type of Coupling */
	public static String couplingType = "MIDDLE";
	/** mapper type */
	public static String mapperType = Constants.Mapper.MYBATIS;
	/**
	 * ��ƼƼ���� ������ ���ڿ�
	 * <pre>
	 * e.g. ���̺���� NQR_BILL_DETAIL �� ���, �⺻ ��ƼƼ���� NqrBillDetail �� �ȴ�
	 * �� �� PREFIX_EXCEPT �� "NQR_" �� ������ ���, ��ƼƼ���� NqrBillDetail �� �ƴ� BillDetail�� �ȴ�.
	 * </pre>
	 */
	public static String prefixExcept;
	/** �����ͺ��̽� ���� */
	public static String dbms;
	/** �����ͺ��̽� ������ */
	public static String serverIp;
	/** �����ͺ��̽� ��Ʈ */
	public static String port;
	/** �����ͺ��̽� ���̵� */
	public static String userId;
	/** �����ͺ��̽� �н����� */
	public static String userPass;
	/** �����ͺ��̽��� */
	public static String dbName;
	/** ���̺�� ����Ʈ */
	public static String tableNameList; 
	/** 
	 * ���� Ŭ���� ���ϸ� ���̾�
	 * <pre>
	 * e.g. ���̺���� USER_INFO �� ���, �⺻ �ڹ����ϸ��� UserInfo.java�� �ȴ�.
	 * �� �� classNameSuffix�� "Dto"�� ������ ��� �ڹ����ϸ��� UserInfoDto.java�� �Ǹ�,
	 * classNameSuffix�� "Vo"�� ������ ��� �ڹ����ϸ��� UserInfoVo.java�� �ȴ�.
	 * </pre>
	 */
	public static String classNameSuffix;


	/** ���� ���� ��, ������Ƽ ���Ϸκ��� �Ӽ��� �о� ���� ������ �����Ѵ�(�����ͺ��̽� ����). */
	public static void init() {

		if (checkNull("COUPLING_TYPE"))
			couplingType = ContextMaster.getString("COUPLING_TYPE");
		if (checkNull("DBMS"))
			dbms = ContextMaster.getString("DBMS");
		if (checkNull("SERVER_IP"))
			serverIp = ContextMaster.getString("SERVER_IP");
		if (checkNull("PORT"))
			port = ContextMaster.getString("PORT");
		if (checkNull("USER_ID"))
			userId = ContextMaster.getString("USER_ID");
		if (checkNull("USER_PASS"))
			userPass = ContextMaster.getString("USER_PASS");
		if (checkNull("DB_NAME"))
			dbName = ContextMaster.getString("DB_NAME");
		if (checkNull("TABLES"))
			tableNameList = ContextMaster.getString("TABLES");
		if (checkNull("MAPPER_TYPE"))
			mapperType = ContextMaster.getString("MAPPER_TYPE");
		if (checkNull("PREFIX_EXCEPT"))
			prefixExcept = ContextMaster.getString("PREFIX_EXCEPT");
		if (checkNull("CLASS_NAME_SUFFIX"))
			classNameSuffix = ContextMaster.getString("CLASS_NAME_SUFFIX");

		File dir = new File(Constants.Path.DTO_CLASS_DES_DIR);
		if (!dir.isDirectory()) {
			// ���丮�� �������� �ʴ´ٸ� ���丮 ����
			dir.mkdirs();
		}
		dir = new File(Constants.Path.SQL_MAPPER_DES_DIR);
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("############## [ Starting MyBro !!! ] ########################");
			LOGGER.info("#");
			LOGGER.info("#\tDTO class files directory:\t" + Constants.Path.DTO_CLASS_DES_DIR);
			LOGGER.info("#\tSql mapper files directory:\t" + Constants.Path.SQL_MAPPER_DES_DIR);
			LOGGER.info("#\tLog directory:\t" + Constants.Path.LOG_DIR);
			LOGGER.info("#\tCoupling type:\t" + couplingType);
			LOGGER.info("#\tMapper type:\t" + mapperType);
			LOGGER.info("#\tExclude prefix string from entity name:\t" + prefixExcept);
			LOGGER.info("#\tJava class name suffix: \t" + classNameSuffix);
			LOGGER.info("##############################################################\n");
		}
	}

	private static boolean checkNull(String name) {
		if (ContextMaster.getString(name) == null) {
			return false;
		}
		return true;
	}
}