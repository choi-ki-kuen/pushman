package push_man.dao;

import java.util.List;

import push_man.vo.RankingVO;
import push_man.vo.ScoreVO;

public interface ScoreDAO {

	/**
	 * @category 신규 추가
	 * @param stage, score, regDate, memberNum
	 * @return insert row count
	 */
	int insertScore(ScoreVO vo);

	/**
	 * @category 기록 갱신
	 * @param stage, score, regDate, memberNum
	 * @return update row count
	 */
	int updateScore(ScoreVO vo);

	/**
	 * @category 랭킹 정보 검색
	 * @param stage, memberNum
	 * @return ranking, num
	 */
	ScoreVO getScoreVO(int stage, int memberNum);

	/**
	 * @category 스테이지 별 랭킹 리스트
	 * @param int stage
	 * @return List<RankingVO>
	 */
	List<RankingVO> getRankingList(int stage);
}




