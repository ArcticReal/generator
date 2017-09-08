package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class SurveyQuestionApplDeleted implements Event{

	private boolean success;

	public SurveyQuestionApplDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
