package push_man.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import push_man.game.GameController;
import push_man.member.MemberController;
import push_man.vo.MemberVO;
import push_man.vo.ScoreVO;
import push_man.waitting_room.WaittingRoomController;

// 서버와 통신을 담당할 client thread
// 모든 화면에서 스레드를 공유
public class MainThread extends Thread {

	// UI를 제어 할 수 있도록 Main thread에 저장
	public MemberController memberController;
	public WaittingRoomController waittingRoomController;
	public GameController gameController;

	// Server 정보 receive-입력
	@Override
	public void run() {
		try {
			while (true) {
				if (isInterrupted()) {
					break;
				}
				Object o = null;
				ObjectInputStream ois = new ObjectInputStream(
						new BufferedInputStream(ClientMain.socket.getInputStream()));
				// 서버에서 전달된 데이터 전달
				if ((o = ois.readObject()) != null) {
					// 회원 관련 요청 처리
					if (o instanceof MemberVO) {
						memberController.receiveData((MemberVO) o);
					} else if (o instanceof List<?> || o instanceof String) {
						// 대기실 - 랭킹리스트 or 대기실 사용자 리스트
						// or 대기실 채팅
						waittingRoomController.receiveData(o);
					}else if(o instanceof ScoreVO) {
						gameController.receiveData((ScoreVO)o);
					}
				}
			}
		} catch (Exception e) {
			stopClient();
		}
	}

	// Object 타입으로 서버에 데이터 전송
	public void sendData(Object o) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new BufferedOutputStream(ClientMain.socket.getOutputStream()));
			oos.writeObject(o);
			oos.flush();
		} catch (IOException e) {
			stopClient();
		}
	}

	// Client Server와 연결 종료
	public void stopClient() {
		if(Platform.isFxApplicationThread()) {
			Platform.runLater(() -> {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setHeaderText(null);
				alert.setContentText("서버와 연결이 끊겼습니다.");
				alert.showAndWait();
				Platform.exit();
			});
		}
		this.interrupt();
		if (ClientMain.socket != null && !ClientMain.socket.isClosed()) {
			try {
				ClientMain.socket.close();
			} catch (IOException e) {}
		}
	}
}
