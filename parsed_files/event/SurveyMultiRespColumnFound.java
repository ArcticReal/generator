package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyMultiRespColumnFound implements Event{

	private List<SurveyMultiRespColumn> surveyMultiRespColumns;

	public SurveyMultiRespColumnFound(List<SurveyMultiRespColumn> surveyMultiRespColumns) {
		this.setSurveyMultiRespColumns(surveyMultiRespColumns);
	}

	public List<SurveyMultiRespColumn> getSurveyMultiRespColumns()	{
		return surveyMultiRespColumns;
	}

	public void setSurveyMultiRespColumns(List<SurveyMultiRespColumn> surveyMultiRespColumns)	{
		this.surveyMultiRespColumns = surveyMultiRespColumns;
	}
}
