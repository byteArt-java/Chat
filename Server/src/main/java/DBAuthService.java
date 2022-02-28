import java.sql.*;

public class DBAuthService implements AuthService{

    @Override public String getNickByLoginPass(String login, String password) {
        return SQLHandler.getNicknameByLoginAndPassword(login, password);
    }

    @Override public boolean registration(String login, String password, String nickname) {
        return SQLHandler.registration(login, password, nickname);
    }

    @Override public boolean changeNick(String oldNick, String newNick) {
        return SQLHandler.changeNick(oldNick, newNick);
    }
}
