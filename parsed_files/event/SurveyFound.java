package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyFound implements Event{

	private List<Survey> surveys;

	public SurveyFound(List<Survey> surveys) {
		this.setSurveys(surveys);
	}

	public List<Survey> getSurveys()	{
		return surveys;
	}

	public void setSurveys(List<Survey> surveys)	{
		this.surveys = surveys;
	}
}
