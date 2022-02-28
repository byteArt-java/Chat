import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    Server server;
    Socket socket = null;
    DataInputStream in;
    DataOutputStream out;

    private String nick;
    private String login;
    private final StringBuilder privateCorrespondence = new StringBuilder();

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);

                    while (true){
                        String str = in.readUTF();

                        if (str.startsWith("/auth")){
                            String[] token = str.split("\\s");
                            if (token.length < 3){
                                continue;
                            }
                            String newNick = server.getAuthService().getNickByLoginPass(token[1],token[2]);
                            login = newNick;
                            if (newNick != null){
                                if (!server.isLoginAuthorized(login)){
                                    sendMsg("/authok " + newNick);
                                    nick = newNick;
                                    server.subscribe(this);
                                    System.out.printf("Клиент %s подключился \n",nick);
                                    socket.setSoTimeout(0);
//                                    sendMsg(SQLHandler.getMessageForNick(nick));//тут два метода, один из базы,второй из файла
                                    break;
                                }else {
                                    sendMsg("С Этим логином уже авторизовались");
                                }
                            }else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }

                        if (str.startsWith("/reg ")){
                            String[] token = str.split("\\s");
                            if (token.length < 4){
                                continue;
                            }
                            boolean b = server.getAuthService().registration(token[1],token[2],token[3]);
                            if (b){
                                sendMsg("/regresult ok");
                            }else {
                                sendMsg("/regresult failed");
                            }
                        }
                    }

                    while (true){
                        String str = in.readUTF();

                        if (str.startsWith("/")){
                            if (str.equals("/end")){
                                out.writeUTF("/end");
                                break;
                            }

                            if (str.startsWith("/w")){
                                String[] token = str.split("\\s",3);
                                if (token.length < 3){
                                    continue;
                                }
                                server.privateMsg(this,token[1],token[2]);
                                privateCorrespondence.append(this.getNick()).append(" : ").
                                        append(token[1]).append(" ").append(token[2]).append("\n");
                            }

                            if (str.startsWith("/chnick ")){
                                String[] token = str.split(" ",2);
                                if (token.length < 2){
                                    continue;
                                }
                                if (token[1].contains(" ")){
                                    sendMsg("Ник не может содержать пробелы");
                                    continue;
                                }
                                if (server.getAuthService().changeNick(this.nick,token[1])){
                                    sendMsg("/yournickis " + token[1]);
                                    sendMsg("Ваш ник изменен на " + token[1]);
                                    this.nick = token[1];
                                    server.broadcastClientList();//отправка всем список
                                }else {
                                    sendMsg("Не удалось изменить ник. Ник " + token[1] + " уже существует");
                                }
                            }
                        }else {
                            server.broadcastMsg(this,str);
                            System.out.println(this.getNick() + " this.getNick()," + str + " str,108");
                            BufferedWriter writer = new BufferedWriter(new FileWriter("history.txt",true));
                            writer.write(this.getNick() + " : " + str + "\n");
                            writer.flush();
                            writer.close();
                        }
                    }

                } catch (SocketTimeoutException t){
                    sendMsg("/end");
                } catch (IOException s){
                    s.printStackTrace();
                }finally {
                    System.out.printf("Клиент %s отключился.",nick);
                    try {
                        server.unsubscribe(this);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    try {
                        in.close();
                        out.close();
                    }catch (IOException er){
                        er.printStackTrace();
                    }
                    try {
                        socket.close();
                    }catch (IOException g){
                        g.printStackTrace();
                    }
                }
            }).start();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    void sendMsg(String str){
        try {
            out.writeUTF(str);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }

    public StringBuilder getPrivateCorrespondence() {
        return privateCorrespondence;
    }

}
