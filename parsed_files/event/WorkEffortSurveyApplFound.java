package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortSurveyApplFound implements Event{

	private List<WorkEffortSurveyAppl> workEffortSurveyAppls;

	public WorkEffortSurveyApplFound(List<WorkEffortSurveyAppl> workEffortSurveyAppls) {
		this.setWorkEffortSurveyAppls(workEffortSurveyAppls);
	}

	public List<WorkEffortSurveyAppl> getWorkEffortSurveyAppls()	{
		return workEffortSurveyAppls;
	}

	public void setWorkEffortSurveyAppls(List<WorkEffortSurveyAppl> workEffortSurveyAppls)	{
		this.workEffortSurveyAppls = workEffortSurveyAppls;
	}
}
