package push_man.main;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;


public class ServerMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader 
			= new FXMLLoader(getClass().getResource("/push_man/main/Server.fxml"));
			VBox root = loader.load();
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			String path = Class.forName("push_man.main.ServerMain").getResource("/resources/system_image/puppy.jpg").toString();
			System.out.println(path);
			primaryStage.getIcons().add(new Image(path));
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.setTitle("Server");
			MainController controller = loader.getController();
			// 서버 무대 닫기 버튼 클릭 시 server 종료
			primaryStage.setOnCloseRequest((e)->{
				controller.stopServer();
			});
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
