package push_man.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import push_man.vo.MemberVO;
import push_man.vo.ScoreVO;

// Client 개별 정보 관리
public class Client {

	// 연결된 클라이언트 소켓 정보
	Socket client;

	// 연결된 client가 로그인 완료 시 서버에 저장
	// 로그인된 회원 정보 저장
	MemberVO member;

	public Client(Socket client) {
		this.client = client;
		serverClientReceive();
	}

	// 연결된 client 에서 출력되는 정보를 읽어들임
	public void serverClientReceive() {
		MainController.threadPool.submit(() -> {
			try {
				Object obj = null;
				while (true) {
					ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
					if ((obj = ois.readObject()) != null) {
						if(obj instanceof MemberVO) {
							desposeMember((MemberVO)obj);
						}else if(obj instanceof Integer) {
							// 대기실 정보 요청 처리
							int num = (Integer)obj;
							switch(num) {
								case -1 : 
									// 게임 시작 또는 종료
									// 대기실 룸에서 사용자 정보 삭제
									removeRoomList();
									// 전체 대기실에있는 사용자 / 대기실 목록 갱신
									updateWattingRoomList();
									break;
								case 0 : 
									// 대기실 사용자 목록에 존재하지 않으면
									// 대기실 목록에 사용자 추가
									if(!MainController.roomList.contains(Client.this)) {
										MainController.roomList.add(Client.this);
									}
									// 전체 대기실에있는 사용자 / 대기실 목록 갱신
									updateWattingRoomList();
									break;
								default :
									// 0 or -1 이외에는 stage 번호
									// 해당 스테이지에 랭킹정보를 전달
									wattingRoomRankingInfo(num);
									break;
							}	// end switch
						}else if(obj instanceof String) {
							// 대기실 roomList 에 있는 client 채팅
							String message = (String)obj;
							broadCast(message);
						}else if(obj instanceof ScoreVO) {
							requestGameInfo((ScoreVO)obj);
						}
					}
				}
			} catch (Exception e) {
				removeClient();
			}
		});
	}

	// 회원관련 요청 처리
	private void desposeMember(MemberVO obj) {
		System.out.println("desposeMember + "+obj);
		switch(obj.getOrder()) {
			case 0 :
				// 아이디 중복 체크
				String memberId = obj.getMemberId();
				boolean isCheck = MainController.memberDAO.checkID(memberId);
				obj.setSuccess(isCheck);
				break;
			case 1 :
				// 회원가입 요청 처리
				MainController.mc.appendText("회원가입 요청 -"+obj.getMemberName());
				isCheck = MainController.memberDAO.joinMember(obj);
				obj.setSuccess(isCheck);
				break;
			case 2 :
				// 로그인 요청 처리
				MemberVO vo = MainController.memberDAO.loginMember(obj);
				if(vo.isSuccess()) {
					// 중복 로그인 처리
					// 로그인 성공 시 동일한 아이디로 이미 로그인 중인
					// 사용자가 있으면 기존 사용자의 연결 끊기.
					for(Client c : MainController.clients) {
						if(c != this && c.member.equals(vo)) {
							c.removeClient();
							break;
						}
					}
					// socket에 연결된 회원 정보 저장
					member = obj = vo;
				}
				break;
		}
		System.out.println("send MemebrVO -" + obj);
		sendData(obj);
	}

	// 대기실 전체 채팅
	public void broadCast(String message) {
		String name = this.member.getMemberName();
		String id = member.getMemberId();
		name = name+="("+id+")";
		for(Client c : MainController.roomList) {
			if(c.equals(this)) {
				c.sendData("나 : "+message);
			}else {
				c.sendData(name+" : "+message);
			}
		}
	}
	
	// 다른 client 에서 sendData 호출이 가능하므로
	// 외부 스레드에서 동시에 호출 되지 않도록 동기화
	public synchronized void sendData(Object data) {
		try {
			OutputStream os = client.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(
				new BufferedOutputStream(os)
			);
			oos.writeObject(data);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
			removeClient();
		}
	}

	private void removeClient() {
		String ip = client.getInetAddress().getHostAddress();
		MainController.mc.appendText(ip + "-연결 종료");
		// 연결된 클라이언트 정보에 현재 사용자가 존재하면 삭제
		MainController.clients.remove(this);
		// 대기실 목록에 현재 사용자가 존재하면 삭제
		if (MainController.roomList.contains(this)) {
			// 사용자 목록을 대기실에서 삭제하고
			// 모든 사용자에게 목록 갱신
			removeRoomList();
			updateWattingRoomList();
		}

		if (client != null && !client.isClosed()) {
			try {
				client.close();
			} catch (IOException e) {}
		}
	}

	// 대기실 목록에서 현재 사용자 삭제
	private void removeRoomList() {
		// 로그인 된 상태 확인 후 삭제
		if(this.member != null){
			MainController.roomList.remove(this);
		}
	}

	// 모든 사용자에게 대기실 목록 갱신
	private void updateWattingRoomList() {
		List<Client> roomList = MainController.roomList;
		List<MemberVO> memberList = new ArrayList<>();
		for(Client c : roomList) {
			memberList.add(c.member);
		}
		System.out.println(memberList.size());
		for(Client c : roomList) {
			c.sendData(memberList);
		}
		System.out.println("대기실 갱신 완료");
		System.out.println(memberList);
	}
	
	// 스테이지별 게임 랭킹 정보 전송
	private void wattingRoomRankingInfo(int stage) {
		System.out.println("wattingRoomRankingInfo"+stage);
		sendData(MainController.scoreDAO.getRankingList(stage));
		System.out.println("랭킹정보 전송 완료");
	}
	// 게임 기록 저장 요청 - 기존 기록 있을 시 갱신
	private void requestGameInfo(ScoreVO sv) {
		ScoreVO old = MainController.scoreDAO.getScoreVO(sv.getStage(), sv.getMemberNum());
		if(old == null) {
			// 신규 기록 - 기존 기록이 존재하지 않음
			MainController.scoreDAO.insertScore(sv);
		}else if(sv.getScore() < old.getScore()){
			// 기록 갱신 클리어 시간이 작을 수록 빠른 클리어
			MainController.scoreDAO.updateScore(sv);
		}
		sv = MainController.scoreDAO.getScoreVO(sv.getStage(), sv.getMemberNum());
		System.out.println("sendData = " + sv);
		sendData(sv);
	}
	
}









