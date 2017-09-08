package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class SkillTypeUpdated implements Event{

	private boolean success;

	public SkillTypeUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
