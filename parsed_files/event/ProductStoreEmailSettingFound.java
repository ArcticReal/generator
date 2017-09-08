package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreEmailSettingFound implements Event{

	private List<ProductStoreEmailSetting> productStoreEmailSettings;

	public ProductStoreEmailSettingFound(List<ProductStoreEmailSetting> productStoreEmailSettings) {
		this.setProductStoreEmailSettings(productStoreEmailSettings);
	}

	public List<ProductStoreEmailSetting> getProductStoreEmailSettings()	{
		return productStoreEmailSettings;
	}

	public void setProductStoreEmailSettings(List<ProductStoreEmailSetting> productStoreEmailSettings)	{
		this.productStoreEmailSettings = productStoreEmailSettings;
	}
}
