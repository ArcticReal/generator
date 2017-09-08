package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmailAddressVerificationFound implements Event{

	private List<EmailAddressVerification> emailAddressVerifications;

	public EmailAddressVerificationFound(List<EmailAddressVerification> emailAddressVerifications) {
		this.setEmailAddressVerifications(emailAddressVerifications);
	}

	public List<EmailAddressVerification> getEmailAddressVerifications()	{
		return emailAddressVerifications;
	}

	public void setEmailAddressVerifications(List<EmailAddressVerification> emailAddressVerifications)	{
		this.emailAddressVerifications = emailAddressVerifications;
	}
}
