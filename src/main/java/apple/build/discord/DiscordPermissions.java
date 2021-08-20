package apple.build.discord;

import apple.discord.acd.permission.ACDPermission;
import apple.discord.acd.permission.ACDPermissionsList;
import net.dv8tion.jda.api.entities.Member;

public class DiscordPermissions {
    public static final String BUILD_ADMIN = "admin";

    public static void addAll(ACDPermissionsList permissions) {
        permissions.addPermission(new ACDPermission(BUILD_ADMIN, 5) {
            @Override
            public boolean hasPermission(Member member) {
                return member.getIdLong() == DiscordBot.APPLEPTR16;
            }
        });
    }
}
