package push_man.member;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import push_man.main.ClientMain;
import push_man.vo.MemberVO;

// 회원 정보를 다룰 화면 controller
public class MemberController implements Initializable, MemberInterface {

	@FXML
	private WebView webView;
	@FXML
	private ImageView imageView;
	@FXML
	private AnchorPane joinAnchor, loginAnchor;
	@FXML
	private TextField loginID, joinID, joinName;
	@FXML
	private PasswordField loginPW, joinPW, joinRePW;
	@FXML
	private Button btnLogin, btnJoin;
	@FXML
	private Hyperlink loginLinkBtn, joinLinkBtn;
	@FXML
	private Label checkID;

	// 로그인 된 사용자 정보
	// 전체 화면 공유 null == 비로그인
	public static MemberVO user;

	// 중복 아이디 체크
	boolean isJoin; // true 중복 체크 통과

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// thread에서 receive 받을 수 있도록
		// memberController 초기화
		ClientMain.mainThread.memberController = this;

		// 로그인 화면에 유튜브 동영상 추가
		// https://www.youtube.com/embed/SNT6IEcw3-w
		WebEngine wg = webView.getEngine();
		wg.load("https://www.youtube.com/embed/SNT6IEcw3-w");
		wg.getLoadWorker().stateProperty().addListener((target, old, newValue) -> {
			// URL 사이트 로드가 완료된 상태
			if (newValue.equals(State.SUCCEEDED)) {
				// 이미지 뷰 보이기 속성 해제 - 숨김
				imageView.setVisible(false);
			}
		});
		setHyperLink();
		setJoinEvent();
		setLoginEvent();

		System.out.println("initialize");
	}

	@Override
	public void setHyperLink() {

		joinLinkBtn.setOnAction(e -> {
			Platform.runLater(() -> {
				loginAnchor.setVisible(false);
				loginAnchor.setDisable(true);
				joinAnchor.setVisible(true);
				joinAnchor.setDisable(false);
				joinID.requestFocus();
			});
		});

		loginLinkBtn.setOnAction(e -> {
			Platform.runLater(() -> {
				loginAnchor.setVisible(true);
				loginAnchor.setDisable(false);
				joinAnchor.setVisible(false);
				joinAnchor.setDisable(true);
				loginID.requestFocus();
			});
		});
	}

	@Override
	public void initLoginUI() {
		Platform.runLater(() -> {
			loginID.clear();
			loginPW.clear();
			loginID.requestFocus();
		});
	}

	@Override
	public void initJoinUI() {
		Platform.runLater(() -> {
			joinID.clear();
			joinPW.clear();
			joinRePW.clear();
			joinName.clear();
			joinID.requestFocus();
		});
	}

	@Override
	public void setLoginEvent() {
		loginID.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				loginPW.requestFocus();
			}
		});

		loginPW.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				btnLogin.fire();
			}
		});

		btnLogin.setOnAction(e -> {
			String id = loginID.getText().trim();
			String pw = loginPW.getText().trim();
			if (id.equals("")) {
				loginID.requestFocus();
			} else if (pw.equals("")) {
				loginPW.requestFocus();
			} else {
				System.out.println("Send");
				MemberVO member = new MemberVO(id, pw);
				member.setOrder(2);
				ClientMain.mainThread.sendData(member);
			}
		});
	}

	@Override
	public void setJoinEvent() {
		// joinID textField 작성 시
		// 아이디 중복 체크 후 사용자에게 결과 알림.
		joinID.textProperty().addListener((t, o, n) -> {
			System.out.println(n);
			if (!n.trim().equals("")) {
				MemberVO member = new MemberVO(n);
				// 아이디 중복 체크
				member.setOrder(0);
				ClientMain.mainThread.sendData(member);
			} else {
				// 잘못된 아이디
				setJoinIDCheck(false);
			}
		});

		joinID.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				joinPW.requestFocus();
			}
		});

		joinPW.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				joinRePW.requestFocus();
			}
		});

		joinRePW.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				joinName.requestFocus();
			}
		});

		joinName.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				btnJoin.fire();
			}
		});

		// 회원가입 server에 요청
		btnJoin.setOnAction(e -> {
			String id = joinID.getText().trim();
			String pw = joinPW.getText().trim();
			String repw = joinRePW.getText().trim();
			String nick = joinName.getText().trim();

			// 아이디가 공백이거나 중복체크가 안된상태
			if (id.equals("") || !isJoin) {
				// 아이디 작성 유도
				joinID.clear();
				joinID.requestFocus();
			} else if (!pw.equals(repw)) {
				// 비밀번호가 일치하지 않음
				joinPW.clear();
				joinRePW.clear();
				joinPW.requestFocus();
				checkID.setText("비밀번호가 일치하지 않습니다.");
			} else {
				// 회원 가입용 객체 생성
				MemberVO member = new MemberVO(nick, id, pw);
				// order : 0 = 중복아이디 체크
				// order : 1 = 회원가입
				// order : 2 = 로그인 요청
				member.setOrder(1);
				ClientMain.mainThread.sendData(member);
			}
		});
	}

	// checkID Label 중복체크 UI 변경
	@Override
	public void setJoinIDCheck(boolean isChecked) {
		Platform.runLater(() -> {
			String style = isChecked ? "-fx-text-fill:green;" : "-fx-text-fill:red;";
			String msg = isChecked ? "사용가능합니다." : "사용할 수 없는 아이디입니다.";
			checkID.setStyle(style);
			checkID.setText(msg);
		});
	}

	@Override
	public void setJoinCheck(boolean isChecked) {
		if (isChecked) {
			System.out.println("회원가입 성공");
			checkID.setText(null);
			initJoinUI();
			joinAnchor.setVisible(false);
			joinAnchor.setDisable(true);
			loginAnchor.setVisible(true);
			loginAnchor.setDisable(false);
			initLoginUI();
		} else {
			System.out.println("회원 가입 실패");
			initJoinUI();
		}
	}

	@Override
	public void setLoginCheck(MemberVO vo) {
		if (vo.isSuccess()) {
			System.out.println("로그인 성공");
			Platform.runLater(() -> {
				// 로그인 완료된 사용자 정보 저장
				user = vo;
				// 대기실 창 띄우기
				showWaittingRoom();
			});
		} else {
			// 로그인 실패
			// TextField 초기화
			initLoginUI();
		}
	}

	@Override
	public void receiveData(MemberVO vo) {
		System.out.println("Member Receive Data");
		System.out.println(vo);

		// success 결과
		// 0 아이디 중복 체크 결과 true 사용가능 , false 사용불가
		// 1 회원가입 결과 true 회원가입 성공, false 회원가입 실패
		// 2 로그인 결과 true 로그인 성공, false 로그인 실패
		switch (vo.getOrder()) {
		case 0:
			System.out.println("아이디 중복 체크");
			isJoin = vo.isSuccess();
			setJoinIDCheck(isJoin);
			break;
		case 1:
			System.out.println("회원 가입 요청 처리 결과");
			setJoinCheck(vo.isSuccess());
			break;
		case 2:
			System.out.println("로그인 요청 처리 결과");
			setLoginCheck(vo);
			break;
		}
	}

	@Override
	public void showWaittingRoom() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/push_man/waitting_room/room.fxml"));
			Parent root = loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			stage.getIcons().add(new Image(getClass().getResource("/resources/system_image/puppy.jpg").toString()));
			stage.setScene(scene);
			stage.setTitle(user.getMemberName() + "님 반갑습니다.");
			stage.setResizable(false);
			stage.show();
			((Stage) checkID.getScene().getWindow()).close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void setCloseEvent() {
		checkID.getScene().getWindow().setOnCloseRequest((e) -> {
			ClientMain.mainThread.stopClient();
		});
	}
}
