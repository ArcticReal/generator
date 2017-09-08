package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortFixedAssetStdFound implements Event{

	private List<WorkEffortFixedAssetStd> workEffortFixedAssetStds;

	public WorkEffortFixedAssetStdFound(List<WorkEffortFixedAssetStd> workEffortFixedAssetStds) {
		this.setWorkEffortFixedAssetStds(workEffortFixedAssetStds);
	}

	public List<WorkEffortFixedAssetStd> getWorkEffortFixedAssetStds()	{
		return workEffortFixedAssetStds;
	}

	public void setWorkEffortFixedAssetStds(List<WorkEffortFixedAssetStd> workEffortFixedAssetStds)	{
		this.workEffortFixedAssetStds = workEffortFixedAssetStds;
	}
}
