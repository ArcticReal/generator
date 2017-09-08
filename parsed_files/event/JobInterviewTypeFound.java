package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class JobInterviewTypeFound implements Event{

	private List<JobInterviewType> jobInterviewTypes;

	public JobInterviewTypeFound(List<JobInterviewType> jobInterviewTypes) {
		this.setJobInterviewTypes(jobInterviewTypes);
	}

	public List<JobInterviewType> getJobInterviewTypes()	{
		return jobInterviewTypes;
	}

	public void setJobInterviewTypes(List<JobInterviewType> jobInterviewTypes)	{
		this.jobInterviewTypes = jobInterviewTypes;
	}
}
