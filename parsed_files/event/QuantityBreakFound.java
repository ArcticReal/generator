package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuantityBreakFound implements Event{

	private List<QuantityBreak> quantityBreaks;

	public QuantityBreakFound(List<QuantityBreak> quantityBreaks) {
		this.setQuantityBreaks(quantityBreaks);
	}

	public List<QuantityBreak> getQuantityBreaks()	{
		return quantityBreaks;
	}

	public void setQuantityBreaks(List<QuantityBreak> quantityBreaks)	{
		this.quantityBreaks = quantityBreaks;
	}
}
