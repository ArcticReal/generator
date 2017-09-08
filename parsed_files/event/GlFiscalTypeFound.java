package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlFiscalTypeFound implements Event{

	private List<GlFiscalType> glFiscalTypes;

	public GlFiscalTypeFound(List<GlFiscalType> glFiscalTypes) {
		this.setGlFiscalTypes(glFiscalTypes);
	}

	public List<GlFiscalType> getGlFiscalTypes()	{
		return glFiscalTypes;
	}

	public void setGlFiscalTypes(List<GlFiscalType> glFiscalTypes)	{
		this.glFiscalTypes = glFiscalTypes;
	}
}
