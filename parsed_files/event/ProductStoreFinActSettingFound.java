package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreFinActSettingFound implements Event{

	private List<ProductStoreFinActSetting> productStoreFinActSettings;

	public ProductStoreFinActSettingFound(List<ProductStoreFinActSetting> productStoreFinActSettings) {
		this.setProductStoreFinActSettings(productStoreFinActSettings);
	}

	public List<ProductStoreFinActSetting> getProductStoreFinActSettings()	{
		return productStoreFinActSettings;
	}

	public void setProductStoreFinActSettings(List<ProductStoreFinActSetting> productStoreFinActSettings)	{
		this.productStoreFinActSettings = productStoreFinActSettings;
	}
}
