public interface AuthService {
    String getNickByLoginPass(String login,String password);
    boolean registration(String login,String password,String nickname);
    boolean changeNick(String oldNick,String newNick);
}
