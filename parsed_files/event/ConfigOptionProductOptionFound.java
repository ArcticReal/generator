package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ConfigOptionProductOptionFound implements Event{

	private List<ConfigOptionProductOption> configOptionProductOptions;

	public ConfigOptionProductOptionFound(List<ConfigOptionProductOption> configOptionProductOptions) {
		this.setConfigOptionProductOptions(configOptionProductOptions);
	}

	public List<ConfigOptionProductOption> getConfigOptionProductOptions()	{
		return configOptionProductOptions;
	}

	public void setConfigOptionProductOptions(List<ConfigOptionProductOption> configOptionProductOptions)	{
		this.configOptionProductOptions = configOptionProductOptions;
	}
}
