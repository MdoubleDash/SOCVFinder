package jdd.so.bot.actions.cmd;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import org.sobotics.chatexchange.chat.event.PingMessageEvent;
import jdd.so.CloseVoteFinder;
import jdd.so.api.model.Comment;
import jdd.so.bot.ChatRoom;
import jdd.so.bot.actions.BotCommand;
import jdd.so.dao.CommentDAO;
import jdd.so.higgs.FeedBack;
import jdd.so.higgs.Higgs;
import jdd.so.nlp.CommentHeatCategory;

public class CommentReportCommand extends CommentResponseAbstract {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(CommentReportCommand.class);

	@Override
	public String getMatchCommandRegex() {
		return "(?i)(\\sreport\\s)";
	}

	@Override
	public int getRequiredAccessLevel() {
		return BotCommand.ACCESS_LEVEL_REVIEWER;
	}

	@Override
	public String getCommandName() {
		return "Report comment";
	}

	@Override
	public String getCommandDescription() {
		return "Report an offensive comment";
	}

	@Override
	public String getCommandUsage() {
		return "report url to comment";
	}

	@Override
	public void runCommand(ChatRoom room, PingMessageEvent event) {

		String content = event.getMessage().getPlainContent();

		long commentId;
		try {
			commentId = getCommentId(content);
		} catch (RuntimeException e) {
			logger.error("runCommand(ChatRoom, PingMessageEvent)", e);
			room.replyTo(event.getMessage().getId(), "Could not retrive comment id please check link");
			return;
		}
		
		Comment c = getCommentFromApi(commentId);
		if (c==null){
			room.replyTo(event.getMessage().getId(), "Could not find comment in api call, maybe deleted");
			return;
		}
		
		//Get comment from api
		c.setReported(true);
		CommentHeatCategory cc = room.getBot().getCommentCategory();
		if (cc != null) {
			try {
				cc.classifyComment(c);
				if (c.getHiggsReportId()<=0){
					try{
						logger.error("runCommand(ChatRoom, PingMessageEvent) sending comment to Higgs");
						c.setHiggsReportId(Higgs.getInstance().registrerComment(c));
						sendFeedbackToHiggs(c.getHiggsReportId(), event.getUserId(), FeedBack.FN);
					} catch (Throwable e) {
						logger.error("runCommand(ChatRoom, PingMessageEvent) could not send comment to Higgs:" + e.getMessage());
					}
				}
				
				StringBuilder message = room.getBot().getCommentsController().getHeatMessageResult(c, c.getLink());
				room.send(message.toString());
			} catch (Exception e) {
				logger.error("runCommand(ChatRoom, PingMessageEvent)", e);
			}
		}
		
		//save comment
		CommentDAO cdao = new CommentDAO();
		try {
			cdao.insertComment(CloseVoteFinder.getInstance().getConnection(), c);
		} catch (SQLException e) {
			logger.error("runCommand(ChatRoom, PingMessageEvent)", e);
		}
		
	}


}
