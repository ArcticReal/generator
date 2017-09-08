package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ValidResponsibilityFound implements Event{

	private List<ValidResponsibility> validResponsibilitys;

	public ValidResponsibilityFound(List<ValidResponsibility> validResponsibilitys) {
		this.setValidResponsibilitys(validResponsibilitys);
	}

	public List<ValidResponsibility> getValidResponsibilitys()	{
		return validResponsibilitys;
	}

	public void setValidResponsibilitys(List<ValidResponsibility> validResponsibilitys)	{
		this.validResponsibilitys = validResponsibilitys;
	}
}
