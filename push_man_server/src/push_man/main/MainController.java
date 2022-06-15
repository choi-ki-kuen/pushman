package push_man.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import push_man.dao.MemberDAO;
import push_man.dao.MemberDAOImpl;
import push_man.dao.ScoreDAO;
import push_man.dao.ScoreDAOImpl;

public class MainController implements Initializable{
	// 서버 로그
	@FXML private TextArea txtArea;
	
	// 서버 시작 중지
	@FXML private Button btnStartStop;
	
	// Client Socket 연결 관리
	ServerSocket server;
	
	// Client Thread 관리
	public static ExecutorService threadPool;
	public static MainController mc;
	
	// server와 연결된 모든 client 정보 관리
	public static List<Client> clients;

	// 채팅이 가능한 대기실 사용자 정보 관리
	public static List<Client> roomList;
	
	// 회원 관련 db 요청 처리할 
	// DataAccessObject 
	public static MemberDAO memberDAO;

	public static ScoreDAO scoreDAO;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		txtArea.setFocusTraversable(false);
		// 서버 실행 중지 이벤트
		btnStartStop.setOnAction((e)->{
			if(btnStartStop.getText().equals("_Start")) {
				initServer();
				btnStartStop.setText("S_top");
			}else {
				stopServer();
				btnStartStop.setText("_Start");
				appendText("[ 서버 종료 ]");
			}
		});
	}
	
	// 서버 초기화
	public void initServer() {
		threadPool = Executors.newFixedThreadPool(30);
		clients = new Vector<>();
		roomList = new Vector<>();
		mc = this;
		memberDAO = new MemberDAOImpl();
		scoreDAO = new ScoreDAOImpl();
		appendText("[ 서버 생성 중]");
		Runnable run = ()->{
			try {
				server = new ServerSocket(8001);
				while(true) {
					appendText("[ Client 연결 대기중... ]");
					Socket client = server.accept();
					String client_ip = client.getInetAddress().getHostAddress();
					appendText(client_ip+" 연결 완료");
					Client c = new Client(client);
					clients.add(c);
					appendText(clients.size()+" 생성 완료");
				}
			} catch (IOException e) {
				stopServer();
				appendText("[ 서버 종료 "+e.getMessage()+"]");
			}
		};
		threadPool.submit(run);
	}
	
	// 서버 중지
	public void stopServer() {
		try {
			if(clients != null) {
				for(Client c : clients) {
					if(c != null && !c.client.isClosed()) {
						c.client.close();
					}
				}
				clients.clear();
			}
			if(server!= null && !server.isClosed()) {
				server.close();
			}
		} catch (IOException e) {}
		finally {
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdownNow();
			}
		}
	}
	
	// Server Log
	public void appendText(String msg) {
		Platform.runLater(()->{
			txtArea.appendText(msg+"\n");
		});
	}
}





