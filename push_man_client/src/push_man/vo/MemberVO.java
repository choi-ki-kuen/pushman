package push_man.vo;

import java.io.Serializable;
import java.text.SimpleDateFormat;

// 회원정보를 저장하고 통신에 사용할 Class
// Value Object
public class MemberVO implements Serializable {

	private static final long serialVersionUID = -2298886540054589285L;

	// 번호에 따라 idCheck == 중복확인
	// login, join 분류
	private int order; // 명령번호
	// 회원 번호 - 타 테이블과 연관성을 위해
	private int memberNum;
	// 회원 이름
	private String memberName;
	// 회원 아이디
	private String memberId;
	// 회원 비밀번호
	private String memberPw;
	// 회원 가입일
	private long regdate;
	// 명령에 따른 처리 성공 여부
	private boolean success;

	public MemberVO() {
	}

	// 아이디 중복 체크용 생성자
	public MemberVO(String memberId) {
		this.memberId = memberId;
	}

	// 로그인용 생성자
	public MemberVO(String memberId, String memberPw) {
		this.memberId = memberId;
		this.memberPw = memberPw;
	}

	// 회원 가입용 생성자
	public MemberVO(String memberName, String memberId, String memberPw) {
		this.memberName = memberName;
		this.memberId = memberId;
		this.memberPw = memberPw;
	}

	// db에서 검색된 모든 회원 정보를 저장할 생성자
	public MemberVO(int memberNum, String memberName, String memberId, String memberPw, long regdate) {
		this.memberNum = memberNum;
		this.memberName = memberName;
		this.memberId = memberId;
		this.memberPw = memberPw;
		this.regdate = regdate;
	}

	// 이하 getter setter toString

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getMemberNum() {
		return memberNum;
	}

	public void setMemberNum(int memberNum) {
		this.memberNum = memberNum;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getMemberPw() {
		return memberPw;
	}

	public void setMemberPw(String memberPw) {
		this.memberPw = memberPw;
	}

	// 회원 등록 시간은 문자열로 반환
	public String getRegdate() {
		return new SimpleDateFormat("yyyy-MM-dd HH:MM:ss").format(this.regdate);
	}

	public void setRegdate(long regdate) {
		this.regdate = regdate;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return "MemberVO [order=" + order + ", memberNum=" + memberNum + ", memberName=" + memberName + ", memberId="
				+ memberId + ", memberPw=" + memberPw + ", regdate=" + getRegdate() + ", success=" + success + "]";
	}

}
