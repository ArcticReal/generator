package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortSkillStandardFound implements Event{

	private List<WorkEffortSkillStandard> workEffortSkillStandards;

	public WorkEffortSkillStandardFound(List<WorkEffortSkillStandard> workEffortSkillStandards) {
		this.setWorkEffortSkillStandards(workEffortSkillStandards);
	}

	public List<WorkEffortSkillStandard> getWorkEffortSkillStandards()	{
		return workEffortSkillStandards;
	}

	public void setWorkEffortSkillStandards(List<WorkEffortSkillStandard> workEffortSkillStandards)	{
		this.workEffortSkillStandards = workEffortSkillStandards;
	}
}
