package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SurveyMultiRespFound implements Event{

	private List<SurveyMultiResp> surveyMultiResps;

	public SurveyMultiRespFound(List<SurveyMultiResp> surveyMultiResps) {
		this.setSurveyMultiResps(surveyMultiResps);
	}

	public List<SurveyMultiResp> getSurveyMultiResps()	{
		return surveyMultiResps;
	}

	public void setSurveyMultiResps(List<SurveyMultiResp> surveyMultiResps)	{
		this.surveyMultiResps = surveyMultiResps;
	}
}
