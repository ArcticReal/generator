package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountCategoryTypeFound implements Event{

	private List<GlAccountCategoryType> glAccountCategoryTypes;

	public GlAccountCategoryTypeFound(List<GlAccountCategoryType> glAccountCategoryTypes) {
		this.setGlAccountCategoryTypes(glAccountCategoryTypes);
	}

	public List<GlAccountCategoryType> getGlAccountCategoryTypes()	{
		return glAccountCategoryTypes;
	}

	public void setGlAccountCategoryTypes(List<GlAccountCategoryType> glAccountCategoryTypes)	{
		this.glAccountCategoryTypes = glAccountCategoryTypes;
	}
}
