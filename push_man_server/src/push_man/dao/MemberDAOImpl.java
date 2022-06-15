package push_man.dao;

import java.sql.SQLException;

import push_man.vo.MemberVO;

public class MemberDAOImpl extends DAO implements MemberDAO{

	@Override
	public boolean checkID(String id) {
		String sql = "SELECT * FROM push_man_member WHERE memberID = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			// 동일한 아이디 값이 테이블에 존재하면 사용불가
			if(rs.next()) {
				return false;
			}
		} catch (SQLException e) {
			return false;
		}finally {
			super.close(rs,pstmt);
		}
		return true;
	}

	@Override
	public boolean joinMember(MemberVO vo) {
		String sql = "INSERT INTO push_man_member("
				+ "memberName,memberId,memberPw,regdate) "
				+ "VALUES(?,?,?,now())";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, vo.getMemberName());
			pstmt.setString(2, vo.getMemberId());
			pstmt.setString(3, vo.getMemberPw());
			// 삽입된 행이 있으면 회원 가입 성공
			if(pstmt.executeUpdate() > 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}finally {
			super.close(pstmt);
		}
		return false;
	}

	@Override
	public MemberVO loginMember(MemberVO vo) {
		String sql = "SELECT * FROM push_man_member WHERE memberID = ? AND memberPW = ?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, vo.getMemberId());
			pstmt.setString(2, vo.getMemberPw());
			
			rs = pstmt.executeQuery();
			// 회원가입 요청이 들어온 객체를 이용하여
			// 필요한 정보만 검색하여 다시 전달
			if(rs.next()) {
				vo.setMemberNum(rs.getInt("memberNum"));
				vo.setMemberName(rs.getString("memberName"));
				vo.setRegdate(rs.getLong("regdate"));
				vo.setSuccess(true);
				System.out.println();
			}
		} catch (SQLException e) {
		}finally {
			super.close(rs,pstmt);
		}
		return vo;
	}

}






