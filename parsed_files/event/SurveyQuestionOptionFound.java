package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyQuestionOptionFound implements Event{

	private List<SurveyQuestionOption> surveyQuestionOptions;

	public SurveyQuestionOptionFound(List<SurveyQuestionOption> surveyQuestionOptions) {
		this.setSurveyQuestionOptions(surveyQuestionOptions);
	}

	public List<SurveyQuestionOption> getSurveyQuestionOptions()	{
		return surveyQuestionOptions;
	}

	public void setSurveyQuestionOptions(List<SurveyQuestionOption> surveyQuestionOptions)	{
		this.surveyQuestionOptions = surveyQuestionOptions;
	}
}
