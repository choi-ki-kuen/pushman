package push_man.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import push_man.main.MainController;

public abstract class DAO {
	// java - DATABASE 연결 객체
	protected Connection conn;
	// 질의를 실행할 객체
	protected Statement stmt;
	protected PreparedStatement pstmt;
	// 질의에 대한 결과를 저장할 객체
	protected ResultSet rs;
	
	// DB 연결
	public DAO() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(
				"jdbc:mysql://192.168.1.32:3306/pushman",
				"pushman",
				"12345"
			);
		} catch (ClassNotFoundException e) {
			MainController.mc.appendText("driver class가 존재하지 않음");
		} catch (SQLException e) {
			MainController.mc.appendText("db 연결 정보가 일치하지 않습니다.");
		}
	}
	
	// 자원해제
	protected void close(AutoCloseable... closer) {
		for(AutoCloseable c : closer) {
			if(c != null) {
				try {
					c.close();
				} catch (Exception e) {}
			}
		}
	}
}









