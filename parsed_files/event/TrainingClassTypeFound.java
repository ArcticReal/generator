package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TrainingClassTypeFound implements Event{

	private List<TrainingClassType> trainingClassTypes;

	public TrainingClassTypeFound(List<TrainingClassType> trainingClassTypes) {
		this.setTrainingClassTypes(trainingClassTypes);
	}

	public List<TrainingClassType> getTrainingClassTypes()	{
		return trainingClassTypes;
	}

	public void setTrainingClassTypes(List<TrainingClassType> trainingClassTypes)	{
		this.trainingClassTypes = trainingClassTypes;
	}
}
