package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyResponseFound implements Event{

	private List<SurveyResponse> surveyResponses;

	public SurveyResponseFound(List<SurveyResponse> surveyResponses) {
		this.setSurveyResponses(surveyResponses);
	}

	public List<SurveyResponse> getSurveyResponses()	{
		return surveyResponses;
	}

	public void setSurveyResponses(List<SurveyResponse> surveyResponses)	{
		this.surveyResponses = surveyResponses;
	}
}
