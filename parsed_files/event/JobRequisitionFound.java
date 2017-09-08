package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class JobRequisitionFound implements Event{

	private List<JobRequisition> jobRequisitions;

	public JobRequisitionFound(List<JobRequisition> jobRequisitions) {
		this.setJobRequisitions(jobRequisitions);
	}

	public List<JobRequisition> getJobRequisitions()	{
		return jobRequisitions;
	}

	public void setJobRequisitions(List<JobRequisition> jobRequisitions)	{
		this.jobRequisitions = jobRequisitions;
	}
}
