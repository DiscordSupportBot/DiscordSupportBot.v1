package github.scarsz.discordsupportbot;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No bot token provided");
            System.exit(1);
        }

        new DiscordSupportBot(args[0]);
    }

}
