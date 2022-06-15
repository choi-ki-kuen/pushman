package push_man.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import push_man.vo.RankingVO;
import push_man.vo.ScoreVO;

public class ScoreDAOImpl extends DAO implements ScoreDAO {

	String sql;

	public int insertScore(ScoreVO vo) {
		sql = "INSERT INTO push_man_score(score,stage,regDate,memberNum) " + " VALUES(?,?,?,?);";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, vo.getScore());
			pstmt.setInt(2, vo.getStage());
			pstmt.setLong(3, vo.getRegDate());
			pstmt.setInt(4, vo.getMemberNum());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			super.close(pstmt);
		}
		return 0;
	}

	public int updateScore(ScoreVO vo) {
		sql = "UPDATE push_man_score SET score = ? " + " WHERE stage = ? AND memberNum = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, vo.getScore());
			pstmt.setInt(2, vo.getStage());
			pstmt.setInt(3, vo.getMemberNum());
			return pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			super.close(pstmt);
		}
		return 0;
	}

	public ScoreVO getScoreVO(int stage, int memberNum) {
		ScoreVO vo = null;
		sql = "SELECT * FROM (SELECT * , rank() over(ORDER BY score ASC) AS ranking "
				+ " FROM push_man_score WHERE stage = ?) AS r WHERE r.memberNum = ?;";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, stage);
			pstmt.setInt(2, memberNum);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				vo = new ScoreVO(rs.getInt("stage"),rs.getLong("score"),  rs.getLong("regDate"),
						rs.getInt("memberNum"), rs.getInt("ranking"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			super.close(rs,pstmt);
		}
		return vo;
	};

	public List<RankingVO> getRankingList(int stage) {
		List<RankingVO> list = new ArrayList<>();
		String sql = "SELECT rank() over(order by score ASC) AS ranking, "
				+ " pmm.memberNum AS num, pmm.memberName AS name," + " pms.stage, pms.score, pms.regdate FROM "
				+ " push_man_score AS pms , push_man_member AS pmm "
				+ " WHERE pms.memberNum = pmm.memberNum AND pms.stage = ? LIMIT 10;";

		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, stage);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(new RankingVO(rs.getInt("ranking"), rs.getInt("num"), rs.getString("name"), rs.getInt("stage"),
						rs.getLong("score"), rs.getLong("regdate")));
			}
		} catch (SQLException e) {
		} finally {
			super.close(rs,pstmt);
		}
		return list;
	}

}
