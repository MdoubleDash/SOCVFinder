package jdd.so.bot.actions.cmd;

import java.util.Collections;
import java.util.List;

import org.sobotics.chatexchange.chat.event.PingMessageEvent;
import jdd.so.bot.ChatRoom;
import jdd.so.bot.actions.BotCommand;
import jdd.so.bot.actions.BotCommandsRegistry;

public class CommandsCommand extends BotCommand {

	@Override
	public String getMatchCommandRegex() {
		return "(?i)(command|commands)";
	}

	@Override
	public int getRequiredAccessLevel() {
		return BotCommand.ACCESS_LEVEL_NONE;
	}

	@Override
	public String getCommandName() {
		return "Bot commands";
	}

	@Override
	public String getCommandDescription() {
		return "Display this list";
	}

	@Override
	public String getCommandUsage() {
		return "commands";
	}

	@Override
	public void runCommand(ChatRoom room, PingMessageEvent event) {
		List<BotCommand> commands = BotCommandsRegistry.getInstance().getCommands();
		Collections.sort(commands);
		String repMsg = "These are available commands in this room see also [quick guide](https://github.com/jdd-software/SOCVFinder/blob/master/quickGuide.md) for usage.";
		room.replyTo(event.getMessage().getId(), repMsg);
		StringBuilder retMsg = new StringBuilder("");
		int al = -1;
		for (BotCommand bc : commands) {
			if (!room.isAllowed(bc) || bc instanceof AiChatCommand){
				continue;//Do not included this
			}
			
			if (bc.getRequiredAccessLevel()!=al){
				if (al>-1){
					retMsg.append("\n");	
				}
				al = bc.getRequiredAccessLevel();
				
				retMsg.append("    ").append(BotCommand.getAccessLevelName(al));
			}
			retMsg.append("\n        ").append(bc.getCommandUsage()).append(" - ").append(bc.getCommandDescription());
			if (bc instanceof CherryPickCommand){
				retMsg.append("\n              answerType: a=Has answer, aa=Has accepted answer, na=Has no answer, naa=Has no accepted answer, nr=No roomba");
			}
		}
		retMsg.append("");
		room.send(retMsg.toString());
	}


	
	
	

}
