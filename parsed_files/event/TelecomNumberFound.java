package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TelecomNumberFound implements Event{

	private List<TelecomNumber> telecomNumbers;

	public TelecomNumberFound(List<TelecomNumber> telecomNumbers) {
		this.setTelecomNumbers(telecomNumbers);
	}

	public List<TelecomNumber> getTelecomNumbers()	{
		return telecomNumbers;
	}

	public void setTelecomNumbers(List<TelecomNumber> telecomNumbers)	{
		this.telecomNumbers = telecomNumbers;
	}
}
