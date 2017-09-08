package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortKeywordFound implements Event{

	private List<WorkEffortKeyword> workEffortKeywords;

	public WorkEffortKeywordFound(List<WorkEffortKeyword> workEffortKeywords) {
		this.setWorkEffortKeywords(workEffortKeywords);
	}

	public List<WorkEffortKeyword> getWorkEffortKeywords()	{
		return workEffortKeywords;
	}

	public void setWorkEffortKeywords(List<WorkEffortKeyword> workEffortKeywords)	{
		this.workEffortKeywords = workEffortKeywords;
	}
}
