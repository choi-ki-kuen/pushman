package push_man.vo;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * score & member table의 랭킹정보를 join 하여 저장할 class use rankingTableView
 */
public class RankingVO implements Serializable {

	private static final long serialVersionUID = 6745744790536988858L;

	public int ranking; // 랭킹 순위
	public int num; // 회원번호
	public String name; // 회원이름
	public int stage; // stage 번호
	public long score; // clear 타임 - 점수
	public long regdate; // clear한 시간

	public RankingVO() {}

	// 랭킹 정보를 전달받을 생성자
	public RankingVO(int ranking, int num, String name, int stage, long score, long regdate) {
		this.ranking = ranking;
		this.num = num;
		this.name = name;
		this.stage = stage;
		this.score = score;
		this.regdate = regdate;
	}

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public String getScore() {
		return new SimpleDateFormat("HH:mm:ss.SSS").format(this.score);
	}

	public void setScore(long score) {
		this.score = score;
	}

	public String getRegdate() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.regdate);
	}

	public void setRegdate(long regdate) {
		this.regdate = regdate;
	}

	@Override
	public String toString() {
		return "RankingVO [ranking=" + ranking + ", num=" + num + ", name=" + name + ", stage=" + stage + ", score="
				+ getScore() + ", regdate=" + getRegdate() + "]";
	}

}
