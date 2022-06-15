package push_man.main;

import java.io.IOException;
import java.net.Socket;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import push_man.member.MemberController;

public class ClientMain extends Application implements MainInterface {

	// main 무대
	private Stage primaryStage;

	// loading flag
	boolean isRun = true;

	// 연결된 서버 정보
	public static Socket socket;

	// 서버에서 전달된 정보를 관리할 thread
	public static MainThread mainThread;

	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.getIcons().add(new Image(getClass().getResource("/resources/system_image/puppy.png").toString()));
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/push_man/main/Main.fxml"));
			VBox root = (VBox) loader.load();
			Scene scene = new Scene(root);
			scene.setFill(Color.TRANSPARENT);
			System.setProperty("prism.lcdtext", "false");
			String strFamily = 
					Font.loadFont(getClass().getResource("/resources/font/Maplestory_Bold.ttf").toString(), 0).getFamily();
			strFamily = 
					Font.loadFont(getClass().getResource("/resources/font/Maplestory_Light.ttf").toString(), 0).getFamily();
			System.out.println(strFamily);
			primaryStage.setScene(scene);
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			primaryStage.setResizable(false);
			this.primaryStage = primaryStage;
			primaryStage.show();
			initClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initClient() {
		// Loading label UI 처리
		Thread loadingThread = new Thread(() -> {
			Label label = (Label) primaryStage.getScene().getRoot().lookup(".loading");
			String[] strs = { "loading", "loading.", "loading..", "loading..." };
			while (isRun) {
				for (int i = 0; i < strs.length; i++) {
					final int j = i;
					Platform.runLater(() -> {
						label.setText(strs[j]);
					});
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {}
				}
			}
		});
		loadingThread.setDaemon(true);
		loadingThread.start();

		// ImageView 크기 확장 애니메이션
		ImageView img = (ImageView) primaryStage.getScene().lookup("ImageView");
		img.setFitWidth(30);
		Timeline timeLine = new Timeline();
		KeyValue keyValue = new KeyValue(img.fitWidthProperty(), 100);
		KeyFrame keyFrame = new KeyFrame(Duration.millis(3000), keyValue);
		timeLine.getKeyFrames().add(keyFrame);
		timeLine.play();
		
		
		Button btn = (Button)primaryStage.getScene().lookup("Button");
		TextField tf = (TextField)primaryStage.getScene().lookup("TextField");
		tf.setOnKeyPressed(e->{
			if(e.getCode().equals(KeyCode.ENTER) || e.getCode().equals(KeyCode.TAB)) {
				btn.fire();
			}
		});
		
		btn.setOnAction(event->{
			final String ip = tf.getText();
			tf.clear();
			// Server와 연결 시도
			Thread t = new Thread(() -> {
				try {
					// IP , PORT 로 서버와 연결 시도
					socket = new Socket(ip, 8001);
					System.out.println("연결 완료");
					// 연결 정보로 receive 중앙 관리 스레드 생성
					// Main Thread 분리 처리
					mainThread = new MainThread();
					mainThread.setDaemon(true);
					mainThread.start();
					isRun = false;
					// 정상적으로 연결 완료 시
					// 회원 관련 페이지 이동
					Platform.runLater(() -> {
						try {
							showMemberStage();
						} catch (Exception e) {}
					});
				} catch (IOException e) {
					// 연결 실 패 시 경고 창
					System.out.println("연결 실패");
					Platform.runLater(() -> {
						isRun = false;
						primaryStage.close();
						showAlert("연결실패\n서버와 연결할수 없습니다.\n다시 실행해주세요.");
					});
				}
			});
			t.setDaemon(true);
			t.start();
		});
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void showMemberStage() throws Exception {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/push_man/member/Member.fxml"));
			Parent root = loader.load();
			Stage stage = new Stage();
			stage.getIcons().add(new Image(getClass().getResource("/resources/system_image/puppy.jpg").toString()));
			Scene scene = new Scene(root);
			stage.setTitle("회원관리");
			stage.setScene(scene);
			stage.show();
			primaryStage.close();
			isRun = false;
			MemberController cont = loader.getController();
			cont.setCloseEvent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void showAlert(String text) {
		// 확인 버튼 클릭시 재연결 시도
		// 취소 버튼 클릭시 앱 종료
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("알림");
		alert.setHeaderText(text);
		alert.setContentText("확인 : 재시도\n취소:종료");
		alert.showAndWait();
		if (alert.getResult() == ButtonType.OK) {
			// 무대 띄운 후 다시 연결시도
			primaryStage.show();
			if (!isRun) {
				isRun = true;
			}
			initClient();
		} else if (alert.getResult() == ButtonType.CANCEL) {
			// UI 스레드 종료 == 프로그램 종료
			Platform.exit();
		}
	}
}
