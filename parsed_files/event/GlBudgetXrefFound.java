package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlBudgetXrefFound implements Event{

	private List<GlBudgetXref> glBudgetXrefs;

	public GlBudgetXrefFound(List<GlBudgetXref> glBudgetXrefs) {
		this.setGlBudgetXrefs(glBudgetXrefs);
	}

	public List<GlBudgetXref> getGlBudgetXrefs()	{
		return glBudgetXrefs;
	}

	public void setGlBudgetXrefs(List<GlBudgetXref> glBudgetXrefs)	{
		this.glBudgetXrefs = glBudgetXrefs;
	}
}
