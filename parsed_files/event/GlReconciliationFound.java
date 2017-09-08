package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlReconciliationFound implements Event{

	private List<GlReconciliation> glReconciliations;

	public GlReconciliationFound(List<GlReconciliation> glReconciliations) {
		this.setGlReconciliations(glReconciliations);
	}

	public List<GlReconciliation> getGlReconciliations()	{
		return glReconciliations;
	}

	public void setGlReconciliations(List<GlReconciliation> glReconciliations)	{
		this.glReconciliations = glReconciliations;
	}
}
