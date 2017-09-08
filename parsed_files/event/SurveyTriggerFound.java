package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyTriggerFound implements Event{

	private List<SurveyTrigger> surveyTriggers;

	public SurveyTriggerFound(List<SurveyTrigger> surveyTriggers) {
		this.setSurveyTriggers(surveyTriggers);
	}

	public List<SurveyTrigger> getSurveyTriggers()	{
		return surveyTriggers;
	}

	public void setSurveyTriggers(List<SurveyTrigger> surveyTriggers)	{
		this.surveyTriggers = surveyTriggers;
	}
}
