package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyQuestionTypeFound implements Event{

	private List<SurveyQuestionType> surveyQuestionTypes;

	public SurveyQuestionTypeFound(List<SurveyQuestionType> surveyQuestionTypes) {
		this.setSurveyQuestionTypes(surveyQuestionTypes);
	}

	public List<SurveyQuestionType> getSurveyQuestionTypes()	{
		return surveyQuestionTypes;
	}

	public void setSurveyQuestionTypes(List<SurveyQuestionType> surveyQuestionTypes)	{
		this.surveyQuestionTypes = surveyQuestionTypes;
	}
}
