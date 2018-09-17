package jdd.so.bot.actions.cmd;

import java.util.List;

import org.sobotics.chatexchange.chat.event.PingMessageEvent;
import jdd.so.CloseVoteFinder;
import jdd.so.bot.ChatRoom;
import jdd.so.bot.actions.BotCommand;

public class RoomTagList extends BotCommand {
	
	@Override
	public String getMatchCommandRegex() {
		return "(?i)(list tags)";
	}

	@Override
	public int getRequiredAccessLevel() {
		return BotCommand.ACCESS_LEVEL_HAMMER;
	}

	@Override
	public String getCommandName() {
		return "List tags in room";
	}

	@Override
	public String getCommandDescription() {
		return "List all tags available for opt-in in room";
	}

	@Override
	public String getCommandUsage() {
		return "list tags";
	}

	@Override
	public void runCommand(ChatRoom room, PingMessageEvent event) {

		List<String> tags = CloseVoteFinder.getInstance().getRoomTags().get(room.getRoomId());
		long messageId = event.getMessage().getId();
		if (tags == null||tags.isEmpty()) {
			room.replyTo(messageId, "All tags are available in this room");
			return;
		}
		StringBuilder at = new StringBuilder();
		for (String t : tags) {
			at.append(" [tag:").append(t).append("]");
		}
		room.replyTo(messageId, "In this room these tags are available:" + at.toString());
	}

}
