package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStorePaymentSettingFound implements Event{

	private List<ProductStorePaymentSetting> productStorePaymentSettings;

	public ProductStorePaymentSettingFound(List<ProductStorePaymentSetting> productStorePaymentSettings) {
		this.setProductStorePaymentSettings(productStorePaymentSettings);
	}

	public List<ProductStorePaymentSetting> getProductStorePaymentSettings()	{
		return productStorePaymentSettings;
	}

	public void setProductStorePaymentSettings(List<ProductStorePaymentSetting> productStorePaymentSettings)	{
		this.productStorePaymentSettings = productStorePaymentSettings;
	}
}
