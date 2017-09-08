package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class JobInterviewFound implements Event{

	private List<JobInterview> jobInterviews;

	public JobInterviewFound(List<JobInterview> jobInterviews) {
		this.setJobInterviews(jobInterviews);
	}

	public List<JobInterview> getJobInterviews()	{
		return jobInterviews;
	}

	public void setJobInterviews(List<JobInterview> jobInterviews)	{
		this.jobInterviews = jobInterviews;
	}
}
