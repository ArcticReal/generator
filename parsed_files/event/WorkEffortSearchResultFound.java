package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortSearchResultFound implements Event{

	private List<WorkEffortSearchResult> workEffortSearchResults;

	public WorkEffortSearchResultFound(List<WorkEffortSearchResult> workEffortSearchResults) {
		this.setWorkEffortSearchResults(workEffortSearchResults);
	}

	public List<WorkEffortSearchResult> getWorkEffortSearchResults()	{
		return workEffortSearchResults;
	}

	public void setWorkEffortSearchResults(List<WorkEffortSearchResult> workEffortSearchResults)	{
		this.workEffortSearchResults = workEffortSearchResults;
	}
}
