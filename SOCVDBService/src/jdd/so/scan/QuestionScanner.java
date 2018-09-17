package jdd.so.scan;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONException;

import jdd.so.CloseVoteFinder;
import jdd.so.api.ApiHandler;
import jdd.so.api.CherryPickResult;
import jdd.so.api.model.ApiResult;
import jdd.so.bot.actions.filter.NumberFilter;
import jdd.so.bot.actions.filter.QuestionsFilter;
import jdd.so.dao.QuestionIndexDao;

public class QuestionScanner {
	
	public ApiResult scan(String tag, int nrDays , int minCvCount) throws JSONException, IOException, SQLException{
		int maxPages = 200;
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -1); //lets move back one day...
		long ed = cal.getTimeInMillis()/1000L;
		if (nrDays>0){
			cal.add(Calendar.DATE, -nrDays);
		}else{
			maxPages = 250;
			cal.add(Calendar.DATE, -4000);
		}
		long sd = cal.getTimeInMillis()/1000L;
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		System.out.println("Scanning from " + df.format(new Date(sd*1000L)) + " to " +  df.format(new Date(ed*1000L)));
		
		ApiResult ar = new ApiHandler().getQuestions(sd,ed, maxPages,tag, false);
		
		System.out.println("Number of question scanned: " + ar.getNrOfQuestionScanned());
		
		QuestionsFilter filter = new QuestionsFilter();
		filter.setNumberOfQuestions(200);
		filter.setCloseVotes(new NumberFilter(">="+minCvCount));
		
		CherryPickResult cpr = new CherryPickResult(ar, 0L, tag, 0);
		cpr.filter(filter);
		
		Connection conn = CloseVoteFinder.getInstance().getConnection();
		
		QuestionIndexDao qid = new QuestionIndexDao();
		System.out.println("Number of question found: " + cpr.getFilterdQuestions().size());
		qid.updateIndex(conn,cpr.getFilterdQuestions(), tag);
		
		return ar;
	}
	
}