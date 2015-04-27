package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DAO {

	protected Connection conn = null;
	protected Statement stmt = null;
	protected ResultSet rs = null;

	private String database;
	private String user = "root";
	private String password = "";

	public DAO(String database) {
		this.database = database;
		try {
			Class.forName("com.mysql.jdbc.Driver"); // 加载mysq驱动
		} catch (ClassNotFoundException e) {
			System.out.println("JDBC error");
			e.printStackTrace();// 打印出错详细信息
		}
	}

	protected void connect() {
		try {
			conn = DriverManager.getConnection(database, user, password);
		} catch (SQLException e) {
			System.out.println("Database connection error");
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception e) {
			System.out.println("Close error");
			e.printStackTrace();
		}
	}
}
