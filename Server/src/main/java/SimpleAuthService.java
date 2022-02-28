import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService{
    private List<UserData> users;

    public SimpleAuthService(){
        users = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            users.add(new UserData(String.format("%d",i),String.format("%d",i),"nick" + i));
        }
    }

    @Override public String getNickByLoginPass(String login, String password) {
        for (UserData user : users) {
            if (user.login.equals(login) && user.password.equals(password)){
                return user.nickname;
            }
        }
        return null;
    }

    @Override public boolean registration(String login, String password, String nickname) {
        for (UserData user : users) {
            if (user.nickname.equals(nickname) || user.login.equals(login)){
                return false;
            }
        }
        users.add(new UserData(login, password, nickname));
        return true;
    }

    @Override public boolean changeNick(String oldNick, String newNick) {
        return false;
    }
}
