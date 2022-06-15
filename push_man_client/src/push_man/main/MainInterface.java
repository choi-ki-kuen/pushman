package push_man.main;

public interface MainInterface {
	// client 초기화 - server와 연결
	void initClient();

	// server와 연결 완료시 - 로그인 회원가입 페이지 이동
	void showMemberStage() throws Exception;

	// 연결 실패 시 경고 알림
	void showAlert(String text);
}
