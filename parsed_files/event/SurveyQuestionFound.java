package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyQuestionFound implements Event{

	private List<SurveyQuestion> surveyQuestions;

	public SurveyQuestionFound(List<SurveyQuestion> surveyQuestions) {
		this.setSurveyQuestions(surveyQuestions);
	}

	public List<SurveyQuestion> getSurveyQuestions()	{
		return surveyQuestions;
	}

	public void setSurveyQuestions(List<SurveyQuestion> surveyQuestions)	{
		this.surveyQuestions = surveyQuestions;
	}
}
