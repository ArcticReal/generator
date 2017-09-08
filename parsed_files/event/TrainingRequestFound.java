package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TrainingRequestFound implements Event{

	private List<TrainingRequest> trainingRequests;

	public TrainingRequestFound(List<TrainingRequest> trainingRequests) {
		this.setTrainingRequests(trainingRequests);
	}

	public List<TrainingRequest> getTrainingRequests()	{
		return trainingRequests;
	}

	public void setTrainingRequests(List<TrainingRequest> trainingRequests)	{
		this.trainingRequests = trainingRequests;
	}
}
