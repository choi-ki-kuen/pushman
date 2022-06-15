package push_man.game;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import push_man.main.ClientMain;
import push_man.member.MemberController;
import push_man.vo.ScoreVO;

public class GameController implements Initializable, GameInterface {

	@FXML private Canvas canvas;
	@FXML private Button btnPrev, btnNext, btnStart, btnExit;
	@FXML private Label lblTime;
	
	// 그리기 도구
	GraphicsContext gc;
	// 클리어 시간
	long clearTime;
	// lblTime 에 시간 text 출력용
	String time;
	// timer flag
	boolean isTimer = true;
	// 현재 게임 스테이지 번호
	int mGameLevelNum;
	// 이미지 배열
	Image[] mImage;
	// 화면에 표시될 이미지 배치도
	int[][] mScreenImageType;
	// canvas mouse X Y 좌표
	double mouseX = -1, mouseY = -1;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 이전 레벨
		btnPrev.setOnAction(event->{
			loadDataFile(mGameLevelNum - 1);
		});
		// 다음 레벨
		btnNext.setOnAction(event->{
			loadDataFile(mGameLevelNum + 1);
		});
		
		// 대기실로 나가기
		btnExit.setOnAction((event)->{
			Platform.runLater(()->{
				showWaittingRoom();
			});
		});
		
		// key event로  canvas 이동
		canvas.setOnKeyPressed(event->{
			// 푸쉬맨 이동 시키기
			// 매개변수로 canvas에서 눌러진  keyCode 전달
			movePushMan(event.getCode());
		});
		
		btnStart.setOnAction(event->{
			canvas.setOnMousePressed(e->moveMouse(e));
			canvas.setOnMouseDragged(e->moveMouse(e));
			canvas.setOnMouseReleased(e->moveMouse(e));
			
			if(btnStart.getText().equals("_START")) {
				startGame();
			}else {
				stopGame();
			}
		});
	}
	
	// 마우스로 push_man 이동
	public void moveMouse(MouseEvent event) {
		if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
			mouseX = event.getX();
			mouseY = event.getY();
		}else if(event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
			double moveX = event.getX() - mouseX;
			double moveY = event.getY() - mouseY;
			if(moveX == -1 && moveY == -1) {
				return;
			}
			// 수평방향 이동
			if(Math.abs(moveX) >= LENGTH_IMAGE_WIDTH) {
				if(moveX < 0) {
					movePushMan(KeyCode.LEFT);
				}else {
					movePushMan(KeyCode.RIGHT);
				}
			// 수직 방향으로 이동 했을 때
			}else if(Math.abs(moveY) >= LENGTH_IMAGE_HEIGHT){
				if(moveY < 0) {
					movePushMan(KeyCode.UP);
				}else {
					movePushMan(KeyCode.DOWN);
				}
			}else {
				return;
			}
			mouseX = event.getX();
			mouseY = event.getY();
		}else if(event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
			mouseX = mouseY = -1;
		}
	}


	// 게임시작
	// canvas만 포커스를 받도록 설정 변경
	@Override
	public void startGame() {
		Platform.runLater(()->{
			btnStart.setText("_STOP");
			// key Event로 발생되는 focus이벤트 대상에서 제외
			btnNext.setFocusTraversable(false);
			btnStart.setFocusTraversable(false);
			btnPrev.setFocusTraversable(false);
			btnExit.setFocusTraversable(false);
			// canvas는 기본적으로 focus대상에서 제외 되어 있으므로
			// focus를 받을 수 있게 설정
			canvas.setFocusTraversable(true);
			canvas.requestFocus();
		});
		// 타이머 실행
		startTimer();
	}
	
	// 게임 종료
	@Override
	public void stopGame() {
		isTimer = false;
		canvas.setOnMousePressed(null);
		canvas.setOnMouseDragged(null);
		canvas.setOnMouseReleased(null);
		canvas.setFocusTraversable(false);
		btnNext.setFocusTraversable(true);
		btnPrev.setFocusTraversable(true);
		btnStart.setFocusTraversable(true);
		btnExit.setFocusTraversable(true);
		Platform.runLater(()->{
			btnStart.setText("_START");
			lblTime.setText("00:00:00");
			btnStart.requestFocus();
			loadDataFile(mGameLevelNum);
		});
	}

	// 1. 멤버 변수 초기화
	@Override
	public void initField() {
		gc = canvas.getGraphicsContext2D();
		// 이미지 배열 초기화
		mImage = new Image[6];
		Image back = new Image(
			getClass().getResource("/push_man/game/game_image/img_back.png").toString()
		);
		Image block = new Image(
			getClass().getResource("/push_man/game/game_image/img_block.png").toString()
		);
		Image stone = new Image(
			getClass().getResource("/push_man/game/game_image/img_stone.png").toString()
		);
		Image houseEmpty = new Image(
			getClass().getResource("/push_man/game/game_image/img_house_empty.png").toString()
		);
		Image houseFull = new Image(
			getClass().getResource("/push_man/game/game_image/img_house_full.png").toString()
		);
		Image pushMan = new Image(
			getClass().getResource("/push_man/game/game_image/img_push_man.png").toString()
		);
		mImage[IMAGE_TYPE_BACK] = back;
		mImage[IMAGE_TYPE_BLOCK] = block;
		mImage[IMAGE_TYPE_STONE] = stone;
		mImage[IMAGE_TYPE_HOUSE_EMPTY] = houseEmpty;
		mImage[IMAGE_TYPE_HOUSE_FULL] = houseFull;
		mImage[IMAGE_TYPE_PUSH_MAN] = pushMan;
		
		// 맵 정보 배열 초기화
		mScreenImageType 
		= new int[COUNT_SCREEN_IMAGE_ROW][COUNT_SCREEN_IMAGE_COL];
		// 게임 레벨 번호 초기화
//		mGameLevelNum = 1;
	}


	@Override
	public String readTextFile(String filePath) {
		String text = null;
		try {
			// 텍스파일의 입력스트림을 구한다.
			InputStream is = new FileInputStream(filePath);
			// 파일내용을 배열에 저장한다.
			
			// 입력스트림에 연결된 파일에서 읽을 수 있는 byte 수를 반환
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			text = new String(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return text;
	}

	
	// 2. 텍스트 파일에서 스테이지 정보를 읽어 들여 
	// 멤버 변수 배열[][]에 저장
	@Override
	public void loadDataFile(int levelNum) {
		// 읽어들인 파일의 모든 정보를 저장하고 있는 문자열
		String bufFile = null;
		// 파일 경로
		String filePath = null;
		
		// 레벨번호가 허용 범위를 벗어나면 탈출
		if(levelNum < 1 || levelNum > MAX_GAME_LEVEL_NUM) {
			return;
		}
		
		String path = String.format("/push_man/game/stage_data/stage_%d.txt",levelNum);
		filePath = getClass().getResource(path).getPath();
		bufFile = readTextFile(filePath);
		
		int last = 0, row = 0, col = 0, length=0, imageType = 0;
		// bufLine == 한줄에 대한 정보(행)
		// bufItem == 한행에 포함이 되어있는 열 정보(숫자를 하나씩 짜름)
		String bufLine,bufItem;
		
		// 파일내용에서 타일정보를 줄바꿈 하면서 정보를 추출해서
		// 멤버변수[][] 저장
		while((last = bufFile.indexOf("\n")) >= 0) {
			bufLine = bufFile.substring(0,last);
			bufFile = bufFile.substring(last+1);
			bufLine.trim();
			
			length = bufLine.length();
			
			if(length > COUNT_SCREEN_IMAGE_COL) {
				length = COUNT_SCREEN_IMAGE_COL;
			}
			
			// 0 ~ 9
			for(col = 0; col < length; col++) {
				bufItem = bufLine.substring(col,col+1);
				imageType = Integer.parseInt(bufItem);
				mScreenImageType[row][col] = imageType;
			}
			row++;
			
			if(COUNT_SCREEN_IMAGE_ROW <= row) {
				break;
			}
		} // End While
		mGameLevelNum = levelNum;
		((Stage)canvas.getScene().getWindow()).setTitle(
			MemberController.user.getMemberName()+"님 반갑습니다-"+mGameLevelNum+" LEVEL"
		);
		// 캔버스 갱신
		initCanvas();
		btnStart.requestFocus();
		// 마우스 좌표값 초기화
		mouseX = mouseY = -1;
	}


	@Override
	public void initCanvas() {
		// 이미지 크길로 캔버스에 전체 크기 지정
		canvas.setWidth(
			COUNT_SCREEN_IMAGE_COL * LENGTH_IMAGE_WIDTH
		);
		canvas.setHeight(
			COUNT_SCREEN_IMAGE_ROW * LENGTH_IMAGE_HEIGHT
		);
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		int imageType = 0;
		int posX = 0 , posY = 0;
		for(int row = 0; row < COUNT_SCREEN_IMAGE_ROW; row++) {
			for(int col = 0; col < COUNT_SCREEN_IMAGE_COL; col++) {
				imageType = mScreenImageType[row][col];
				
				if(imageType == IMAGE_TYPE_PUSH_MAN_IN_HOUSE) {
					imageType = IMAGE_TYPE_PUSH_MAN;
				}
				posX = LENGTH_SCREEN_START_X + col * LENGTH_IMAGE_WIDTH;
				posY = LENGTH_SCREEN_START_Y + row * LENGTH_IMAGE_HEIGHT;
				
				gc.drawImage(
						mImage[imageType], 
						posX, 
						posY,
						LENGTH_IMAGE_WIDTH,
						LENGTH_IMAGE_HEIGHT);
			}
		}
	}

	// 클리어 시간
	@Override
	public void startTimer() {
		// time flag
		isTimer = true;
		
		// clear시간 초기값 지정 
		clearTime = 1000*60*60*15;
		// new Date(0) == 1970 1 1 : 00:00:00
		Thread t = new Thread(()->{
			while(isTimer) {
				clearTime += 1;
				Platform.runLater(()->{
					//if(clearTime % 1000 == 0) {
						time = getTime(clearTime);
						
						lblTime.setText(time);
					//}
				});
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					isTimer = false;
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}
	
	public String getTime(long time) {
		return new SimpleDateFormat("HH:mm:ss:SSS")
				.format(time);
	}

	// 푸쉬맨 현재 위치를 기준으로 이미지 정보 변경
	@Override
	public void movePushMan(KeyCode keyCode) {
		//      현재위치          이동위치      이동될 돌의 위치
		Point poPushMan, poNewPush, poNewBall;
		poPushMan = getPositionPushMan();
		// 생성자로 Point instance를 넘겨받아
		// 자신의 필드에 x,y값을 저장
		poNewPush = new Point(poPushMan);
		poNewBall = new Point(poPushMan);
		
		if(keyCode.equals(KeyCode.UP)) {
			poNewPush.y -= 1;
			poNewBall.y -= 2;
		}else if(keyCode.equals(KeyCode.DOWN)) {
			poNewPush.y += 1;
			poNewBall.y += 2;
		}else if(keyCode.equals(KeyCode.LEFT)) {
			poNewPush.x -= 1;
			poNewBall.x -= 2;
		}else if(keyCode.equals(KeyCode.RIGHT)) {
			poNewPush.x += 1;
			poNewBall.x += 2;
		}else if(keyCode.equals(KeyCode.SPACE)) {
			btnStart.fire();
		}else {
			return;
		}
		
		switch(mScreenImageType[poNewPush.y][poNewPush.x]) {
			// 배경일때
			case IMAGE_TYPE_BACK : 
				// 변경된 위치에 push_man 이미지 저장
				mScreenImageType[poNewPush.y][poNewPush.x]
					= IMAGE_TYPE_PUSH_MAN;
				break;
			// 경계 벽돌일때
			case IMAGE_TYPE_BLOCK :
				return;
			// 돌 구슬 일때
			case IMAGE_TYPE_STONE : 
				// 돌구슬이 이동가능한 상태인지 확인해서 이동
				if(insertStoneToCell(poNewBall)) {
					mScreenImageType[poNewPush.y][poNewPush.x]
							= IMAGE_TYPE_PUSH_MAN;
				}else {
					// 돌구슬이 이동할 수 없을때 는 종료
					return;
				}
			break;
			// 빈집일때
			case IMAGE_TYPE_HOUSE_EMPTY :
				mScreenImageType[poNewPush.y][poNewPush.x]
						= IMAGE_TYPE_PUSH_MAN_IN_HOUSE;
				break;
			// 돌이 들어간 집일때
			case IMAGE_TYPE_HOUSE_FULL :
				// 돌이동 가능
				if(insertStoneToCell(poNewBall)) {
					mScreenImageType[poNewPush.y][poNewPush.x]
							= IMAGE_TYPE_PUSH_MAN_IN_HOUSE;
				}else {
					// 돌구슬이 이동 할 수 없을때는 푸쉬맨도 멈춤
					return;
				}
				break;
		}
		// 기존의 자리에 이미지 지정
		recoverToEmptyCell(poPushMan);
		
		
		// 새로 저장된 배열로 캔버스 새로 그림.
		initCanvas();
		
		// 스페이지가 클리어 되었는지 판단. 클리어 처리
		if(isGameComplate()){
			// 게임 클리어
			isTimer = false;
			//showDialog();
			ScoreVO score = new ScoreVO(
				mGameLevelNum,
				clearTime,
				System.currentTimeMillis(),
				MemberController.user.getMemberNum()				
			);
			ClientMain.mainThread.sendData(score);
		}
	}

	// map 이미지 [][] 안에 push_man의 위치를 반환
	// push_man 현재위치
	@Override
	public Point getPositionPushMan() {
		int row = 0, col = 0;
		Point poPushMan = new Point();
		// map 정보 배열 수직 이미지 개수 만큼 루프 반복
		for(row = 0; row < COUNT_SCREEN_IMAGE_ROW; row++) {
			// map 정보 배열 수평 이미지 개수 만큼 루프 반복
			for(col = 0; col < COUNT_SCREEN_IMAGE_COL; col++) {
				// 푸쉬맨의 위치를 찾았다면 해당 위치를 반환
				if(mScreenImageType[row][col] == IMAGE_TYPE_PUSH_MAN
				    || mScreenImageType[row][col] == IMAGE_TYPE_PUSH_MAN_IN_HOUSE		
				  ) {
					poPushMan.x = col;
					poPushMan.y = row;
					return poPushMan;
				}
			}
		}
		return poPushMan;
	}

	// 넘겨받은 Point에 돌구슬을 집어 넣을 수 있는 판단
	@Override
	public boolean insertStoneToCell(Point poCell) {
		switch(mScreenImageType[poCell.y][poCell.x]) {
			// 배경일때 돌이미지로 변경
			case IMAGE_TYPE_BACK :
				mScreenImageType[poCell.y][poCell.x]
						= IMAGE_TYPE_STONE;
				break;
			// 빈집일때는 돌이 들어간 집으로 변경
			case IMAGE_TYPE_HOUSE_EMPTY :
				mScreenImageType[poCell.y][poCell.x]
					= IMAGE_TYPE_HOUSE_FULL;
				break;
			default :
				// 그 외는 이동불가
				return false;
		}
		return true;
	}

	// 이동하고 난 자리를 비워준다.
	@Override
	public void recoverToEmptyCell(Point poCell) {
		switch(mScreenImageType[poCell.y][poCell.x]) {
			case IMAGE_TYPE_HOUSE_FULL :
			case IMAGE_TYPE_PUSH_MAN_IN_HOUSE: 
				mScreenImageType[poCell.y][poCell.x]
					= IMAGE_TYPE_HOUSE_EMPTY;
				break;
			case IMAGE_TYPE_PUSH_MAN :
			case IMAGE_TYPE_STONE :
				mScreenImageType[poCell.y][poCell.x] 
						= IMAGE_TYPE_BACK;
				break;
		}
	}

	// 스페이지 클리어 체크
	@Override
	public boolean isGameComplate() {
		// map 전체를 검사해서 돌구술 이미지가 존재하는지 체크 없다면 클리어
		for(int row = 0; row<COUNT_SCREEN_IMAGE_ROW;row++) {
			for(int col = 0; col<COUNT_SCREEN_IMAGE_COL;col++) {
				// 돌구슬이 발견되면 false 반환
				if(mScreenImageType[row][col] == IMAGE_TYPE_STONE) {
					return false;
				}
			}
		}
		return true;
	}


	@Override
	public void showDialog(ScoreVO obj) {
		String message = 
			String.format("축하합니다! %d-레벨을 클리어 했어요!\n기록 : %s\n 랭킹 : %d", mGameLevelNum,getTime(obj.getScore()), obj.getRanking());
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.setTitle(mGameLevelNum+" level clear");
		Optional<ButtonType> op = alert.showAndWait();
		op.ifPresent(t->{
			// 사용자가 버튼을 누르면 새로운 스테이지 정보를 로딩 + 1단계
			loadDataFile(mGameLevelNum + 1);
			stopGame();
		});
	}

	// 기록 정보 receive
	public void receiveData(ScoreVO obj) {
		Platform.runLater(()->{
			showDialog(obj);
		});
	}
	
	// 대기실 오픈
	public void showWaittingRoom() {
		try {
			FXMLLoader loader = new FXMLLoader(
			getClass().getResource("/push_man/waitting_room/Room.fxml")
			);
			Parent root = loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			stage.getIcons().add(new Image(getClass().getResource("/resources/system_image/puppy.jpg").toString()));
			stage.setScene(scene);
			stage.setTitle(MemberController.user.getMemberName()+"님 반갑습니다.");
			stage.setResizable(false);
			stage.show();
			Stage gameStage = (Stage)btnStart.getScene().getWindow();
			gameStage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 게임 레벨 초기화
	public void setLevel(Integer selectedItem) {
		if(selectedItem != null) {
			this.mGameLevelNum = selectedItem;
			
		}else {
			this.mGameLevelNum = 1;	
		}
		ClientMain.mainThread.gameController = this;
		// 필드 초기화
		initField();
		loadDataFile(mGameLevelNum);
	}
}





