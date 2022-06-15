package push_man.waitting_room;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import push_man.game.GameController;
import push_man.game.GameInterface;
import push_man.main.ClientMain;
import push_man.member.MemberController;
import push_man.vo.MemberVO;
import push_man.vo.RankingVO;

public class WaittingRoomController implements Initializable, WaittingRoomInterface {

	@FXML
	private TableView<MemberVO> userList;
	@FXML
	private TableView<RankingVO> rankingList;
	@FXML
	private ComboBox<Integer> stageBox;
	@FXML
	private TextField inputText;
	@FXML
	private Button btnStart, btnSend;
	@FXML
	private TextArea chatArea;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ClientMain.mainThread.waittingRoomController = this;
		// 스테이지 정보 comboBox 초기화
		initComboBox();
		// stage 1번 정보로 초기화
		initRankingTableView(1);
		// 접속자 목록 초기화
		initUserListTableView();
		Platform.runLater(() -> {
			inputText.requestFocus();
		});
		
		btnStart.setOnAction((e)->{
			Platform.runLater(()->{
				// 대기실에서 삭제
				ClientMain.mainThread.sendData(-1);
				showGameRoom();
			});
		});
		
		inputText.setOnKeyPressed(e->{
			if(e.getCode().equals(KeyCode.ENTER)) {
				btnSend.fire();
			}
		});
		// 대기실 채팅 
		btnSend.setOnAction(e->{
			ClientMain.mainThread.sendData(inputText.getText());
			inputText.clear();
			inputText.requestFocus();
		});
		
		// 게임시작
		btnStart.setOnAction((e)->{
			ClientMain.mainThread.sendData(-1);
			showGameRoom();
		});
	}

	/**
	 * 대기실에 접속 중인 사용자 목록 초기화
	 * 게임 입장 시 대기실 목록에서 제외
	 */
	@Override
	public void initUserListTableView() {
		TableColumn<MemberVO, String> columnName = new TableColumn<>("닉네임");
		columnName.setCellValueFactory(new PropertyValueFactory<>("memberName"));
		TableColumn<MemberVO, String> columnId = new TableColumn<>("아이디");
		columnId.setCellValueFactory(new PropertyValueFactory<>("memberId"));
		userList.getColumns().add(0, columnName);
		userList.getColumns().add(1, columnId);
		for (TableColumn<MemberVO, ?> tc : userList.getColumns()) {
			tc.setStyle("-fx-alignment:center;");
			tc.setPrefWidth(100);
		}
		ClientMain.mainThread.sendData(0);
	}

	/**
	 * ranking table stage별 초기화 TODO 게임 기능 완료 후 구현
	 */
	@Override
	public void initRankingTableView(int stage) {
		// public 접근 제한자를 가진 field 정보를 가지고옴
		// serialVersionID 제외
		Field[] fields = RankingVO.class.getFields();
		System.out.println(fields.length);
		for(int i=0; i<fields.length; i++) {
			TableColumn<RankingVO,?> column = new TableColumn<>(fields[i].getName());
			column.setCellValueFactory(new PropertyValueFactory<>(fields[i].getName()));
			column.setResizable(false);
			column.setStyle("-fx-alignment:center;");
			column.setPrefWidth(70);
			if(i == fields.length -1) {
				// clear time
				column.setPrefWidth(200);
			}else if(i == fields.length - 2) {
				// score
				column.setPrefWidth(100);
			}
			rankingList.getColumns().add(column);
		}
		
		Label label = new Label("아직 등록된 기록이 없습니다. \n지금 도전하세요.");
		label.setStyle("-fx-text-fill:white;-fx-font-size:30;-fx-font-weight:bold;");
		rankingList.setPlaceholder(label);
		
		ClientMain.mainThread.sendData(stage);
	}

	/**
	 * stage 선택 콤보 박스 stage 선택 후 스테이지 별 랭킹 출력
	 */
	@Override
	public void initComboBox() {
		ObservableList<Integer> list = FXCollections.observableArrayList();
		for (int i = 1; i <= GameInterface.MAX_GAME_LEVEL_NUM; i++) {
			list.add(i);
		}
		stageBox.setItems(list);
		stageBox.getSelectionModel().selectFirst();
		stageBox.getSelectionModel().selectedItemProperty().addListener((o, o1, value) -> {
			// TODO 게임 기능 구현 후 완성
			// stage 별 랭킹 리스트 서버에 요청
			ClientMain.mainThread.sendData(value);
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void receiveData(Object obj) {
		if (obj instanceof List<?>) {
			List<?> temp = (List<?>) obj;
			System.out.println("temp : " + temp);
			if (temp.isEmpty()) {
				System.out.println("isEmpty");
				rankingList.setItems(FXCollections.observableArrayList());
				return;
			}

			Object o = temp.get(0);
			System.out.println(o);
			if (o instanceof MemberVO) {
				// 대기실 목록 갱신
				userList.setItems(FXCollections.observableArrayList((ArrayList<MemberVO>) obj));
			}else if(o instanceof RankingVO) {
				System.out.println("RankingVO");
				List<RankingVO> waitList = (ArrayList<RankingVO>)obj;
				System.out.println("waitList : " + waitList);
				rankingList.setItems(FXCollections.observableArrayList(waitList));
				System.out.println("랭킹 세팅 완료");
			}
		}else if(obj instanceof String) {
			// 대기실 채팅
			appendText((String)obj);
		}
	}

	@Override
	public void showGameRoom() {
		try {
			FXMLLoader loader = new FXMLLoader(
				getClass().getResource("/push_man/game/game.fxml")
			);
			Scene scene = new Scene(loader.load());
			scene.setFill(Color.TRANSPARENT);
			Stage stage = new Stage();
			stage.getIcons().add(new Image(getClass().getResource("/resources/system_image/puppy.jpg").toString()));
			stage.setScene(scene);
			int num = stageBox.getSelectionModel().getSelectedItem();
			stage.setTitle(MemberController.user.getMemberName()+"님 반갑습니다-"+num+" LEVEL");
			stage.setResizable(false);
			GameController controller = loader.getController();
			controller.setLevel(num);
			stage.setOnCloseRequest((e)->{
				e.consume();
				controller.showWaittingRoom();
			});
			Stage wattingStage = (Stage)btnStart.getScene().getWindow();
			wattingStage.close();
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void appendText(String text) {
		Platform.runLater(()->{
			chatArea.appendText(text+"\n");
		});
	}
}
