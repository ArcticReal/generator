package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyApplTypeFound implements Event{

	private List<SurveyApplType> surveyApplTypes;

	public SurveyApplTypeFound(List<SurveyApplType> surveyApplTypes) {
		this.setSurveyApplTypes(surveyApplTypes);
	}

	public List<SurveyApplType> getSurveyApplTypes()	{
		return surveyApplTypes;
	}

	public void setSurveyApplTypes(List<SurveyApplType> surveyApplTypes)	{
		this.surveyApplTypes = surveyApplTypes;
	}
}
