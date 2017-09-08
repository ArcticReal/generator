package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlAccountCategoryFound implements Event{

	private List<GlAccountCategory> glAccountCategorys;

	public GlAccountCategoryFound(List<GlAccountCategory> glAccountCategorys) {
		this.setGlAccountCategorys(glAccountCategorys);
	}

	public List<GlAccountCategory> getGlAccountCategorys()	{
		return glAccountCategorys;
	}

	public void setGlAccountCategorys(List<GlAccountCategory> glAccountCategorys)	{
		this.glAccountCategorys = glAccountCategorys;
	}
}
