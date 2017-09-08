package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyQuestionCategoryFound implements Event{

	private List<SurveyQuestionCategory> surveyQuestionCategorys;

	public SurveyQuestionCategoryFound(List<SurveyQuestionCategory> surveyQuestionCategorys) {
		this.setSurveyQuestionCategorys(surveyQuestionCategorys);
	}

	public List<SurveyQuestionCategory> getSurveyQuestionCategorys()	{
		return surveyQuestionCategorys;
	}

	public void setSurveyQuestionCategorys(List<SurveyQuestionCategory> surveyQuestionCategorys)	{
		this.surveyQuestionCategorys = surveyQuestionCategorys;
	}
}
