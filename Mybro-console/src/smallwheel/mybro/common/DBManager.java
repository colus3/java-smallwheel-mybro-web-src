package smallwheel.mybro.common;


import java.sql.*;

import org.apache.log4j.Logger;

public class DBManager {
	
	private final static Logger LOGGER = Logger.getLogger(DBManager.class);
	private Connection con_mssql = null;
	private Connection con_mysql = null;
	private Connection con_oracle = null;
	
	private boolean OKMSSQL = false;
	private boolean OKMYSQL = false;
	private boolean OKORACLE = false;
	
	PreparedStatement pstmt = null;

	/**
	 *������ ������ ���� �ִ��� Ȯ���Ѵ�.
	 */
	public void checkConnection(String dbms) {
		LOGGER.info("MSG] " + dbms + " connect");

		if (dbms.equalsIgnoreCase("MSSQL")) {
			setConnectionMSSQL(ENV.serverIp, ENV.port, ENV.userId, ENV.userPass, ENV.dbName);
		} else if (dbms.equalsIgnoreCase("MYSQL")) {
			setConnectionMYSQL(ENV.serverIp, ENV.port, ENV.userId, ENV.userPass, ENV.dbName);
		} else if (dbms.equalsIgnoreCase("ORACLE")) {
			setConnectionOracle(ENV.serverIp, ENV.port, ENV.userId, ENV.userPass, ENV.dbName);
		}
	}

	/**
	 * ������ ������ ���� �ִ��� Ȯ���Ѵ�.
	 * @param ip �����ͺ��̽� IP
	 * @param id ���̵�
	 * @param pw ���
	 * @param dbname �����ͺ��̽���
	 */
	public Connection getConnection(String dbms) {
		Connection con = null;
		if ("MSSQL".equalsIgnoreCase(dbms)) {
			con = con_mssql;
		} else if ("MYSQL".equalsIgnoreCase(dbms)) {
			con = con_mysql;
		} else if ("ORACLE".equalsIgnoreCase(dbms)) {
			con = con_oracle;
		}

		return con;
	}

	// ��� �ݴ´�.
	public void setClose() {
		setCloseConnectionMSSQL();
	}
	
	@SuppressWarnings("finally")
	public Statement getStatement() {
		Statement st = null;
		try {
			st = con_mssql.createStatement();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			return st;
		}
    }

	public void close() {
		destroyConnection(con_mssql);
	}

	/** 
	 * MS-SQL ����
	 * @param ip �����ͺ��̽� IP
	 * @param port �����ͺ��̽� ��Ʈ
	 * @param id ���̵�
	 * @param pw ���
	 * @param dbname �����ͺ��̽���
	 */
	public void setConnectionMSSQL(String ip, String  port, String id, String pw,String dbname) {
		try {
			OKMSSQL = false;
			destroyConnection(con_mssql);
//			String drivername = "com.microsoft.jdbc.sqlserver.SQLServerDriver";
			String drivername = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
			Class.forName(drivername);
//			String url = "jdbc:microsoft:sqlserver://" + ip + ":" + port + ";DatabaseName=" + dbname;
			String url = "jdbc:sqlserver://" + ip + ":" + port + ";databaseName=" + dbname;
			LOGGER.info(url);
			con_mssql = DriverManager.getConnection(url, id, pw);
			OKMSSQL = true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			OKMSSQL = false;
			destroyConnection(con_mssql);
		}
	}
	
	/** 
	 * MYSQL ����
	 * @param ip �����ͺ��̽� IP
	 * @param port �����ͺ��̽� ��Ʈ
	 * @param id ���̵�
	 * @param pw ���
	 * @param dbname �����ͺ��̽���
	 */
	public void setConnectionMYSQL(String ip, String  port, String id, String pw, String dbname) {
		try {
			OKMYSQL = false;
			destroyConnection(con_mysql);

			String drivername = "com.mysql.jdbc.Driver";
			Class.forName(drivername);
			String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbname;
			LOGGER.info("[mysql ����] " + ip + ":" + port + " DB Connectionn �õ� id:" + id + " pass:" + pw);
			con_mysql = DriverManager.getConnection(url, id, pw);
			OKMYSQL = true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			OKMYSQL = false;
			destroyConnection(con_mysql);
		}
	}
    
    /** 
	 * ORACLE ����
	 * @param ip �����ͺ��̽� IP
	 * @param port �����ͺ��̽� ��Ʈ
	 * @param id ���̵�
	 * @param pw ���
	 * @param dbname �����ͺ��̽���
	 */
	public void setConnectionOracle(String ip, String  port, String id, String pw, String dbname) {
		try {
			OKORACLE = false;
			destroyConnection(con_oracle);
			String drivername = "oracle.jdbc.OracleDriver";
			Class.forName(drivername);
			String url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + dbname;
			LOGGER.info("[DB����] " + ip + " DB Connectionn �õ� id:" + id + " pass:" + pw);
			con_oracle = DriverManager.getConnection(url, id, pw);
			OKORACLE = true;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			OKORACLE = false;
			destroyConnection(con_oracle);
		}
	}

	/**
	 MSSQL Statement ����
	 */
	@SuppressWarnings("finally")
	public Statement getStatementMssql() {
		Statement st = null;
		try {
			if (OKMSSQL == true) {
				st = con_mssql.createStatement();
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			return st;
		}
	}

	/**
	 * MSSQL close
	 */
	public void setCloseConnectionMSSQL() {
		try {
			if (con_mssql != null) {
				con_mssql.close();
				con_mssql = null;
				OKMSSQL = false;
			}
		} catch (Exception e) {
			con_mssql = null;
			OKMSSQL = false;
		} finally {
			con_mssql = null;
			OKMSSQL = false;
		}
	}

	/**
	 * MSSQL connection ����
	 */
	public Connection getConnectionMSSQL() {
		return con_mssql;
	}

	/**
	 * MYSQL connection ����
	 * 
	 * @return
	 */
	public Connection getConnectionMYSQL() {
		return con_mysql;
	}

	/**
	 * ORACLE connection ����
	 * 
	 * @return
	 */
	public Connection getConnectionORACLE() {
		return con_oracle;
    }
    
	/**
	 * Connection ���� �ʱ�ȭ
	 */
	public void destroyConnection(Connection con) {
		try {
			if (con != null) {
				con.close();
			}
			con = null;
		} catch (Exception e1) {
			con = null;
		} finally {
			con = null;
		}
	}

	/**
	 * Connection ���� �ʱ�ȭ
	 * 
	 * @param st
	 */
	public void destroyStatement(Statement st) {
		try {
			if (st != null) {
				st.close();
			}
			st = null;
		} catch (Exception e1) {
			st = null;
		} finally {
			st = null;
		}
	}

	/**
	 * Connection ���� �ʱ�ȭ
	 */
	public void destroyResultSet(ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
			rs = null;
		} catch (Exception e1) {
			rs = null;
		} finally {
			rs = null;
		}
	}
}