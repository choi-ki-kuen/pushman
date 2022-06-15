package push_man.waitting_room;

public interface WaittingRoomInterface {

	/**
	 * 대기실에 접속 중인 사용자 목록 초기화 게임 입장 시 대기실 목록에서 제외
	 */
	void initUserListTableView();

	/**
	 * ranking table stage별 초기화
	 */
	void initRankingTableView(int stage);

	/**
	 * stage 선택 콤보 박스 stage 선택 후 스테이지 별 랭킹 출력
	 */
	void initComboBox();

	/**
	 * 대기실 화면에서 필요한 정보를 MainThread에서 전달 받아 처리
	 */
	void receiveData(Object obj);

	/**
	 * stage 선택 후 게임시작 선택 시 게임 화면으로 이동
	 */
	void showGameRoom();

	/**
	 * textArea에 넘겨받은 문자열 출력
	 */
	void appendText(String text);
}
