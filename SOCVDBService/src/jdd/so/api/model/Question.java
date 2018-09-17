package jdd.so.api.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The SO question
 * 
 * @author Petter Friberg
 *
 */
public class Question {

	private long questionId;
	private long creationDate;
	private String title;
	private String link;
	private String searchTag;
	private List<String> tags;
	private int closeVoteCount;
	private long closedDate;
	private String closedReason;
	private int score;
	private long viewCount;
	private int commentsCount;
	private int answerCount;
	private long acceptedAnswerId;
	private int deleteVoteCount;

	private long ownerId;
	private long reputation;

	private List<Comment> comments;
	private int duplicateCommentIndex = -1;
	private int duplicateResponseCommentIndex = -1;

	public Question() {
		super();
	}

	public static Question getQuestion(JSONObject item, String mainTag) throws JSONException {
		Question q = new Question();
		q.setQuestionId(item.getLong("question_id"));
		q.setCreationDate(item.getLong("creation_date"));
		q.setTitle(item.getString("title"));
		q.setLink(item.getString("link"));
		q.setSearchTag(mainTag);
		if (item.has("tags")) {
			List<String> tagsList = new ArrayList<String>();
			JSONArray tg = item.getJSONArray("tags");
			for (int i = 0; i < tg.length(); i++) {
				tagsList.add(tg.getString(i));
			}
			q.setTags(tagsList);
		}

		// q.setTags(getTags); we need to loop them
		q.setCloseVoteCount(item.getInt("close_vote_count"));
		if (item.has("closed_date")) {
			q.setClosedDate(item.getLong("closed_date"));
		}
		if (item.has("closed_reason")) {
			q.setClosedReason(item.getString("closed_reason"));
		}
		q.setScore(item.getInt("score"));
		q.setViewCount(item.getLong("view_count"));
		if (item.has("owner")) {
			JSONObject owner = item.getJSONObject("owner");
			if (owner.has("user_id")) {
				q.setOwnerId(owner.getLong("user_id"));
			}
			if (owner.has("reputation")) {
				q.setReputation(owner.getLong("reputation"));
			}
		}

		// comments
		if (item.has("comments")) {
			JSONArray comments = item.getJSONArray("comments");
			int comCnt = comments.length();
			q.setCommentsCount(comCnt);
			List<Comment> cList = new ArrayList<Comment>();
			q.setComments(cList);
			for (int n = 0; n < comCnt; n++) {
				JSONObject jc = comments.getJSONObject(n);
				Comment c = Comment.getComment(jc);
				cList.add(c);
				// index of first duplicate comment
				if (q.getDuplicateCommentIndex() < 0 && c.isPossibleDuplicateComment()) {
					q.setDuplicateCommentIndex(n);
				}
				// check if any response from op
				if (q.getDuplicateCommentIndex() >= 0 && q.getDuplicateResponseCommentIndex() <= 0 && q.getOwnerId() == c.getUserId()) {
					q.setDuplicateResponseCommentIndex(n);
				}
			}
		}

		q.setAnswerCount(item.getInt("answer_count"));
		if (item.has("accepted_answer_id")) {
			q.setAcceptedAnswerId(item.getLong("accepted_answer_id"));
		}
		if (item.has("delete_vote_count")) {
			q.setDeleteVoteCount(item.getInt("delete_vote_count"));
		}
		return q;
	}

	public JSONArray getJSONObject(int nr) throws JSONException {
		JSONArray json = new JSONArray();
		JSONObject tle = new JSONObject();
		tle.put("id", "title");
		tle.put("name", this.title);
		tle.put("value", this.link);
		tle.put("type", "link");
		json.put(tle);
		JSONObject sc = new JSONObject();
		sc.put("id", "score");
		sc.put("name", "Votes");
		sc.put("value", this.score);
		json.put(sc);
		JSONObject postage = new JSONObject();
		postage.put("id", "postage");
		postage.put("name", "Posted");
		Calendar cal = new GregorianCalendar(Locale.ENGLISH);
		cal.setTime(new Date(creationDate*1000));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.ENGLISH);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		postage.put("value", df.format(cal.getTime()));
		postage.put("type", "date");
		json.put(postage);
		JSONObject at = new JSONObject();
		at.put("id", "answercount");
		at.put("name", "Answer");
		at.put("value", getHasAnswer() + answerCount);
		at.put("type", "answers");
		json.put(at);
		
		JSONObject cv = new JSONObject();
		cv.put("id", "cv");
		cv.put("name", "Close votes");
		cv.put("value", getCloseVoteCount());
		json.put(cv);
		return json;
	}

	private String getHasAnswer() {
		if (acceptedAnswerId > 0) {
			return "A";
		}
		return "";
	}

	public boolean isMonitor() {
		return (closeVoteCount > 0 || duplicateCommentIndex >= 0) && (closedDate == 0);
	}

	public long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(long questionId) {
		this.questionId = questionId;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSearchTag() {
		return searchTag;
	}

	public void setSearchTag(String mainTag) {
		this.searchTag = mainTag;
	}

	public String getTagsAsString() {
		if (tags == null) {
			return "";
		}
		String retVal = "";
		for (String t : tags) {
			retVal += "[" + t + "]";
		}
		return retVal;
	}

	public List<String> getTags() {
		return this.tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public int getCloseVoteCount() {
		return closeVoteCount;
	}

	public void setCloseVoteCount(int closeVoteCount) {
		this.closeVoteCount = closeVoteCount;
	}

	public long getClosedDate() {
		return closedDate;
	}

	public void setClosedDate(long closedDate) {
		this.closedDate = closedDate;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public long getViewCount() {
		return viewCount;
	}

	public void setViewCount(long viewCount) {
		this.viewCount = viewCount;
	}

	public int getCommentsCount() {
		return commentsCount;
	}

	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}

	public int getAnswerCount() {
		return answerCount;
	}

	public void setAnswerCount(int answerCount) {
		this.answerCount = answerCount;
	}

	public boolean isAnswerAccepted() {
		return acceptedAnswerId > 0;
	}

	public boolean isPossibleDuplicate() {
		if (duplicateCommentIndex < 0) {
			return false;
		}
		Comment c = comments.get(duplicateCommentIndex);
		return c.getDuplicateQuestionID() > 0;
	}

	public String getPossibibleDuplicateComment() {
		Comment c = getDuplicatedComment();
		if (c != null) {
			return c.getBody();
		}
		return null;
	}

	public int getDeleteVoteCount() {
		return deleteVoteCount;
	}

	public void setDeleteVoteCount(int deleteVoteCount) {
		this.deleteVoteCount = deleteVoteCount;
	}

	public boolean isAlmostRoomba() {
		return score < 1 && answerCount == 0;
	}

	public boolean isRoomba() {
		return score < 0 && answerCount == 0;
	}

	public String getClosedReason() {
		return closedReason;
	}

	public void setClosedReason(String closedReason) {
		this.closedReason = closedReason;
	}

	public boolean isClosed() {
		return closedDate > 0;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(long owner) {
		this.ownerId = owner;
	}

	public long getReputation() {
		return reputation;
	}

	public void setReputation(long reputation) {
		this.reputation = reputation;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public int getDuplicateCommentIndex() {
		return duplicateCommentIndex;
	}

	public void setDuplicateCommentIndex(int duplicateCommentIndex) {
		this.duplicateCommentIndex = duplicateCommentIndex;
	}

	public int getDuplicateResponseCommentIndex() {
		return duplicateResponseCommentIndex;
	}

	public void setDuplicateResponseCommentIndex(int ownerResponseCommentIndex) {
		this.duplicateResponseCommentIndex = ownerResponseCommentIndex;
	}

	public Comment getDuplicatedComment() {
		if (duplicateCommentIndex >= 0) {
			return comments.get(duplicateCommentIndex);
		}
		return null;
	}

	public long getDuplicateQuestionID() {
		Comment c = getDuplicatedComment();
		if (c != null) {
			return c.getDuplicateQuestionID();
		}
		return 0L;
	}

	private String formatScore(int value) {
		String retVal = doubleNum(Math.abs(value));
		if (value > 0) {
			retVal = "+" + retVal;
		} else {
			retVal = "-" + retVal;
		}
		return retVal;
	}

	private String getFixedYesNo(boolean value) {
		if (value) {
			return "YES";
		}
		return "NO ";
	}

	public String getTimeAgo() {
		long diff = System.currentTimeMillis() - (creationDate * 1000);
		long minutes = diff / (60 * 1000) % 60;
		long hours = diff / (60 * 60 * 1000) % 24;
		int days = (int) diff / (1000 * 60 * 60 * 24);
		return days + "d " + doubleNum(hours) + "h " + doubleNum(minutes) + "m";
	}

	private String doubleNum(long value) {
		String retVal = String.valueOf(value);
		if (retVal.length() < 2) {
			retVal = "0" + retVal;
		}
		return retVal;
	}

	@Override
	public String toString() {
		String retVal = questionId + ": CV" + closeVoteCount + " DUP=" + getFixedYesNo(isPossibleDuplicate()) + " DV" + deleteVoteCount + " " + getTimeAgo()
				+ ", " + formatScore(score) + " ";
		if (isRoomba()) {
			retVal += "(R) ";
		} else if (isAlmostRoomba()) {
			retVal += "(r) ";
		}
		retVal += title;
		return retVal;
	}

	public String getHTML(int nr) {
		StringBuilder html = new StringBuilder();
		html.append("<tr>");
		html.append("<td>" + nr + "</td>");
		html.append("<td><a href=\"http://stackoverflow.com/questions/" + questionId + "\" target=\"_blank\">" + title + "</a>");
		html.append("<td align=\"center\">" + getTimeAgo() + "</td>");
		html.append("<td" + getStyleCVCount() + " align=\"center\">" + closeVoteCount + "</td>");
		html.append("<td style=\"align-center\">" + score + "</td>");
		html.append("<td" + getStyleAnswers() + " align=\"center\">" + answerCount + "</td>");
		html.append("<td align=\"center\">" + viewCount + "</td>");
		html.append("<td align=\"center\">" + commentsCount + "</td>");
		html.append("</tr>\n");
		if (duplicateCommentIndex >= 0) {
			Comment cd = comments.get(duplicateCommentIndex);
			html.append("<tr><td>&nbsp;</td><td colspan=7 style=\"font-size:70%\">&nbsp;&nbsp;<sup>" + cd.getScore() + "</sup> " + cd.getBody());
			if (cd.getDuplicateTargetTitle() != null) {
				html.append("--> <b>" + cd.getDuplicateTargetTitle() + " " + formatScore(cd.getDuplicateTargetScore()) + "</b>");
			}
			if (duplicateResponseCommentIndex > 0) {
				Comment crd = comments.get(duplicateResponseCommentIndex);
				html.append("<br/>&nbsp;&nbsp;" + crd.getBody());
			}
			html.append("</td></tr>\n");
		}

		return html.toString();
	}

	private String getStyleAnswers() {
		if (isAnswerAccepted()) {
			return " style=\"background-color:green !Important\"";
		}
		return "";
	}

	private String getStyleCVCount() {
		if (isRoomba()) {
			return " style=\"background-color:red !Important\"";
		}
		if (isAlmostRoomba()) {
			return " style=\"background-color:yellow !Important\"";
		}
		return "";
	}

	public long getAcceptedAnswerId() {
		return acceptedAnswerId;
	}

	public void setAcceptedAnswerId(long acceptedAnswerId) {
		this.acceptedAnswerId = acceptedAnswerId;
	}

	@Override
	public boolean equals(Object q) {
		if (q instanceof Question) {
			return this.getQuestionId() == ((Question) q).getQuestionId();
		}
		return true;
	}

	@Override
	public int hashCode() {
		return ((Long) getQuestionId()).hashCode();
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

}
