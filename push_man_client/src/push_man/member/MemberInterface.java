package push_man.member;

import push_man.vo.MemberVO;

public interface MemberInterface {
	// 로그인 <-> 회원가입 화면 전환
	public void setHyperLink();

	// 로그인 화면 초기화
	public void initLoginUI();

	// 회원 가입 화면 초기화
	public void initJoinUI();

	// login event 초기화
	public void setLoginEvent();

	// join event 초기화
	public void setJoinEvent();

	// 아이디 중복체크 UI 변경
	public void setJoinIDCheck(boolean isChecked);

	// 회원가입 성공 유무
	public void setJoinCheck(boolean isChecked);

	// 로그인 성공 여부 체크
	public void setLoginCheck(MemberVO vo);

	// receiveData
	// MainThread에서 서버 응답 을 UI controller에 전달
	// 0:아이디 중복체크 1:회원가입 2:로그인
	public void receiveData(MemberVO vo);

	// 로그인 성공 시 대기실 화면 오픈
	public void showWaittingRoom();

}
