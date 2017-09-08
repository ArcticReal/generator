package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class SurveyResponseAnswerAdded implements Event{

	private boolean success;

	public SurveyResponseAnswerAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
