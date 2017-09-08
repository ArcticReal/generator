package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyResponseAnswerFound implements Event{

	private List<SurveyResponseAnswer> surveyResponseAnswers;

	public SurveyResponseAnswerFound(List<SurveyResponseAnswer> surveyResponseAnswers) {
		this.setSurveyResponseAnswers(surveyResponseAnswers);
	}

	public List<SurveyResponseAnswer> getSurveyResponseAnswers()	{
		return surveyResponseAnswers;
	}

	public void setSurveyResponseAnswers(List<SurveyResponseAnswer> surveyResponseAnswers)	{
		this.surveyResponseAnswers = surveyResponseAnswers;
	}
}
