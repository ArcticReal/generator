package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyQuestionApplFound implements Event{

	private List<SurveyQuestionAppl> surveyQuestionAppls;

	public SurveyQuestionApplFound(List<SurveyQuestionAppl> surveyQuestionAppls) {
		this.setSurveyQuestionAppls(surveyQuestionAppls);
	}

	public List<SurveyQuestionAppl> getSurveyQuestionAppls()	{
		return surveyQuestionAppls;
	}

	public void setSurveyQuestionAppls(List<SurveyQuestionAppl> surveyQuestionAppls)	{
		this.surveyQuestionAppls = surveyQuestionAppls;
	}
}
