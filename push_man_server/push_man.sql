DROP SCHEMA `push_man`;
-- schema 생성
CREATE SCHEMA `push_man`;


DROP TABLE IF EXISTS push_man_member;
-- 회원정보 
CREATE TABLE IF NOT EXISTS push_man_member(
	memberNum INT(11) PRIMARY KEY AUTO_INCREMENT,
	memberName VARCHAR(50) NOT NULL,
	memberId VARCHAR(100) UNIQUE NOT NULL,
	memberPw VARCHAR(100) NOT NULL,
	regdate BIGINT DEFAULT 0
);


DROP TABLE IF EXISTS push_man_score;

-- stage 별 회원 클리어 시간 저장
CREATE TABLE IF NOT EXISTS push_man_score(
	stage INT(8) DEFAULT 0,
	score BIGINT(20) DEFAULT 0,
	regDate BIGINT(20) DEFAULT 0,
	memberNum INT(11),
	CONSTRAINT fk_memberNum FOREIGN KEY(memberNum)
	REFERENCES push_man_member(memberNum)
);

-- 해당 회원의 스테이지 별 랭킹 정보
SELECT * FROM (SELECT * , rank() over(ORDER BY score ASC) AS ranking 
FROM push_man_score WHERE stage = 1) AS r WHERE r.memberNum = 1;

-- RankingVO
--  int ranking;			// 순위
--	int num;				//회원번호
--	String memberName;	//회원이름
--	int stage;			// 스테이지 번호
--	long score;			// clear 타임 - 점수
--	long regdate;		// clear한 시간
-- 스테이지별 1~10위 까지 랭킹 정보
SELECT rank() over(order by score ASC) AS ranking,
pmm.memberNum AS num, pmm.memberName AS name,
pms.stage, pms.score, 
pms.regdate FROM 
pushman.push_man_score AS pms , pushman.push_man_member AS pmm 
WHERE pms.memberNum = pmm.memberNum AND pms.stage = 1 
LIMIT 10;










