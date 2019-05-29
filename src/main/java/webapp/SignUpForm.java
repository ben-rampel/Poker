package webapp;

public class SignUpForm {
    private String discordCode;

    public String getDiscordCode() {
        return discordCode;
    }

    public void setDiscordCode(String discordCode) {
        this.discordCode = discordCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String name;
    private String password;
}
