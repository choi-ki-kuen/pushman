package push_man.vo;

import java.io.Serializable;

/**
 * 게임 스테이지 에서 개별 랭킹 정보를 클리어 시간에 따라 데이터를 저장할 class use game controller
 */
public class ScoreVO implements Serializable {
	// 객체 직렬화 버전 ID
	private static final long serialVersionUID = 1L;

	// clear한 스테이지 정보
	private int stage;
	// clear한 시간 millisecond
	private long score;
	// clear한 시간
	private long regDate;
	// clear한 회원 번호
	private int memberNum;
	// 랭킹 정보
	private int ranking;

	public ScoreVO() {
	}

	// 스테이지 별 clear한 시간을 저장하여
	// 전송할 생성자
	public ScoreVO(int stage, long score, long regDate, int memberNum) {
		this.stage = stage;
		this.score = score;
		this.regDate = regDate;
		this.memberNum = memberNum;
	}

	// clear 시간 등록 후 랭킹정보를
	// 전달할 생성자
	public ScoreVO(int stage, long score, long regDate, int memberNum, int ranking) {
		this.stage = stage;
		this.score = score;
		this.regDate = regDate;
		this.memberNum = memberNum;
		this.ranking = ranking;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public long getRegDate() {
		return regDate;
	}

	public void setRegDate(long regDate) {
		this.regDate = regDate;
	}

	public int getMemberNum() {
		return memberNum;
	}

	public void setMemberNum(int memberNum) {
		this.memberNum = memberNum;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	@Override
	public String toString() {
		return "ScoreVO [stage=" + stage + ", score=" + score + ", regDate=" + regDate + ", memberNum=" + memberNum
				+ ", ranking=" + ranking + "]";
	}

}
