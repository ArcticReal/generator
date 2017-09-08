package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PersonTrainingFound implements Event{

	private List<PersonTraining> personTrainings;

	public PersonTrainingFound(List<PersonTraining> personTrainings) {
		this.setPersonTrainings(personTrainings);
	}

	public List<PersonTraining> getPersonTrainings()	{
		return personTrainings;
	}

	public void setPersonTrainings(List<PersonTraining> personTrainings)	{
		this.personTrainings = personTrainings;
	}
}
