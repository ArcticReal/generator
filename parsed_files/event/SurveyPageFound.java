package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyPageFound implements Event{

	private List<SurveyPage> surveyPages;

	public SurveyPageFound(List<SurveyPage> surveyPages) {
		this.setSurveyPages(surveyPages);
	}

	public List<SurveyPage> getSurveyPages()	{
		return surveyPages;
	}

	public void setSurveyPages(List<SurveyPage> surveyPages)	{
		this.surveyPages = surveyPages;
	}
}
