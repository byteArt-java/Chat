import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;
    private static final int MAX_LINE = 5;//сколько из истории максимально подгружается количество строк

    public AuthService getAuthService() {
        return authService;
    }

    public Server(){
        clients = new Vector<>();
        if (!SQLHandler.connect()){
            throw new RuntimeException("Не удалось подключиться к БД");
        }

        authService = new DBAuthService();

        ServerSocket server = null;
        Socket socket;

        final int PORT = 8081;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");

            while (true){
                socket = server.accept();
                System.out.println("Клиент Подключился");
                System.out.println("socket.getRemoteSocketAddress() " + socket.getRemoteSocketAddress());
                System.out.println("socket.getLocalSocketAddress() " + socket.getLocalSocketAddress());
                new ClientHandler(this,socket);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            SQLHandler.disconnect();
            try {
                server.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    void broadcastMsg(ClientHandler sender,String msg){
        String message = String.format("%s : %s",sender.getNick(),msg);
        Date date = new Date();
        SQLHandler.addMessage(sender.getNick(),"null",msg,date.toString());

        for (ClientHandler client : clients) {
            client.sendMsg(message);
        }
    }

    void privateMsg(ClientHandler sender,String receiver,String msg){
        String message = String.format("[%s] private [%s] : %s",sender.getNick(),receiver,msg);

        for (ClientHandler client : clients) {
            if (client.getNick().equals(receiver)){
                client.sendMsg(message);
                Date date = new Date();
                SQLHandler.addMessage(sender.getNick(),receiver,msg,date.toString());
                if (!sender.getNick().equals(receiver)){
                    sender.sendMsg(message);
                }
                return;
            }
        }
        sender.sendMsg(String.format("Clients %s not found",receiver));
    }

    public void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
        readHistory(clientHandler,MAX_LINE);
        broadcastClientList();//отправка всем список
    }

    public void unsubscribe(ClientHandler clientHandler) throws IOException {
        clients.remove(clientHandler);
        writePrivateHistory(clientHandler);
        broadcastClientList();//отправка всем список
    }

    public boolean isLoginAuthorized(String login){
        for (ClientHandler client : clients) {
            if (client.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }

    void broadcastClientList(){
        StringBuilder sb = new StringBuilder("/clientlist ");

        for (ClientHandler client : clients) {
            sb.append(client.getNick()).append(" ");
        }

//        for (ClientHandler client : clients) {
//            if (client.getNick().equals(receiver)){
//                client.sendMsg(sb.toString());
//                return;
//            }
//        }
        for (ClientHandler client : clients) {
            client.sendMsg(sb.toString());
        }
    }

    private void readHistory(ClientHandler client,int maxLine) throws IOException {
        if (!Files.exists(Paths.get("history_" + client.getLogin() + ".txt"))){
            Files.createFile(Paths.get("history_" + client.getLogin() + ".txt"));
        }
        if (!Files.exists(Paths.get("history.txt"))){
            Files.createFile(Paths.get("history.txt"));
        }

        int temp1 = maxLine;
        List<String> list = new ArrayList<>();
        BufferedReader inPrivate = new BufferedReader(new FileReader("history_" + client.getLogin() + ".txt"));
        while (inPrivate.ready()){
            list.add(inPrivate.readLine());
        }
        for (int i = 0; i < list.size(); i++) {
            if (temp1 == 0){
                break;
            }
            client.sendMsg(list.get(i));
            temp1--;
        }

        int temp2 = maxLine;
        List<String> list2 = new ArrayList<>();
        BufferedReader inCommon = new BufferedReader(new FileReader("history.txt"));
        while (inCommon.ready()){
            list2.add(inCommon.readLine());
        }
        for (int i = 0; i < list2.size(); i++) {
            if (temp2 == 0){
                break;
            }
            client.sendMsg(list2.get(i));
            temp2--;
        }
    }

    private void writePrivateHistory(ClientHandler client) throws IOException {
        String[] str = client.getPrivateCorrespondence().toString().split("\\s");
        System.out.println(Arrays.toString(str) + " Arrays.toString(str) 153");
        if (str.length < 2){
            System.out.println("Зашли в if.writeHistory");
            System.out.println("Приватная переписка пуста");
        }else if (str.length > 2 && str[0].equals(client.getNick())){
            System.out.println("Зашли в else If.writeHistory");
            String receiver = str[2].trim();
            BufferedWriter out1 = new BufferedWriter(new FileWriter("history_" + receiver + ".txt",true));
            out1.write(client.getPrivateCorrespondence().toString());
            out1.flush();
            out1.close();

            String sender = str[0].trim();
            BufferedWriter out = new BufferedWriter(new FileWriter("history_" + sender + ".txt",true));
            out.write(client.getPrivateCorrespondence().toString());
            out.flush();
            out.close();
        }
    }
}
