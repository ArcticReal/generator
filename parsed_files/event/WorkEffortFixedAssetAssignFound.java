package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortFixedAssetAssignFound implements Event{

	private List<WorkEffortFixedAssetAssign> workEffortFixedAssetAssigns;

	public WorkEffortFixedAssetAssignFound(List<WorkEffortFixedAssetAssign> workEffortFixedAssetAssigns) {
		this.setWorkEffortFixedAssetAssigns(workEffortFixedAssetAssigns);
	}

	public List<WorkEffortFixedAssetAssign> getWorkEffortFixedAssetAssigns()	{
		return workEffortFixedAssetAssigns;
	}

	public void setWorkEffortFixedAssetAssigns(List<WorkEffortFixedAssetAssign> workEffortFixedAssetAssigns)	{
		this.workEffortFixedAssetAssigns = workEffortFixedAssetAssigns;
	}
}
