package app.web.pavelk.сhat1.client.stage1;

import app.web.pavelk.сhat1.client.stage2.Stage2Client;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class Controller1Chat implements Initializable {
    public TextField msgField;
    public TextArea workTextArea;
    public HBox bottomPanel;
    public VBox authorizationVBox;
    public TextField loginField;
    public PasswordField passwordField;
    public ListView<String> clientsList;
    public CheckMenuItem CheckMenuItemWhite;
    public CheckMenuItem CheckMenuItemBlack;
    public HBox workHBox;
    public VBox basicVBox;
    public Text authorizationText;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    final String IP_ADDRESS = "localhost";
    final int PORT = 8189;
    List<TextArea> textAreas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
            setAuthorized(false);
    }

    public void setAuthorized(boolean isAuthorized) {
        if (!isAuthorized) {
            authorizationVBox.setVisible(true);
            authorizationVBox.setManaged(true);
            workHBox.setVisible(false);
            workHBox.setManaged(false);
            textAreas = new ArrayList<>();
            textAreas.add(workTextArea);

        } else {
            authorizationVBox.setVisible(false);
            authorizationVBox.setManaged(false);
            workHBox.setVisible(true);
            workHBox.setManaged(true);
            Stage stage = (Stage) workHBox.getScene().getWindow();
            stage.setWidth(300);
            stage.setHeight(400);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);

            workTextArea.setPrefSize(100, 100);
            clientsList.setPrefSize(100, 100);
        }
    }

    public void authorization() {
        Thread thread = new Thread(() -> {
            if (connect()) {
                try {
                    out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
                 //   loginField.clear();
                    passwordField.clear();
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/authok")) {
                            setAuthorized(true);
                            work();
                            break;
                        } else {
                            for (TextArea o : textAreas) {
                                o.appendText(str + "\n");
                            }
                        }
                    }
                } catch (IOException e) {
                    authorizationText.setText("ошибка авторизации\n");
                    e.printStackTrace();
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    public boolean connect() {
        if (socket == null || socket.isClosed()) {
            try {
                socket = new Socket(IP_ADDRESS, PORT);
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                authorizationText.setText("ошибка подключения к серверу\n");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void work() {
        try {
            while (true) {
                String str = in.readUTF();
                if (str.startsWith("/")) {
                    if (str.equals("/serverclosed")) break;
                    if (str.startsWith("/clientslist ")) {
                        String[] tokens = str.split(" ");
                        Platform.runLater(() -> {
                            clientsList.getItems().clear();
                            for (int i = 1; i < tokens.length; i++) {
                                clientsList.getItems().add(tokens[i]);
                            }
                        });
                    }
                } else {
                    workTextArea.appendText(str + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Controller1Chat.this.setAuthorized(false);
        }
    }

    public void sendMessage() {
        try {
            out.writeUTF(msgField.getText() + " " + getCurrentTime());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            Stage2Client stage2Client = new Stage2Client(clientsList.getSelectionModel().getSelectedItem(), out, textAreas);
            stage2Client.show();
        }
    }

    public void setBlack() {
        CheckMenuItemWhite.setSelected(false);
        CheckMenuItemBlack.setSelected(true);
        Stage1Chat.setCss("styles1");
    }

    public void setWhite() {
        CheckMenuItemWhite.setSelected(true);
        CheckMenuItemBlack.setSelected(false);
        Stage1Chat.setCss("styles2");
    }

    public void exit() {
        Platform.exit();
    }

    public void stage1About() {
        try {
            if (out != null){
                out.writeUTF("/end");
            }
            Stage1Chat.setFXML("stage1About");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearTextArea() {
        workTextArea.clear();
    }

    public String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }
}

