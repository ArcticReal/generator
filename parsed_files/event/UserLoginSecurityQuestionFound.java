package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class UserLoginSecurityQuestionFound implements Event{

	private List<UserLoginSecurityQuestion> userLoginSecurityQuestions;

	public UserLoginSecurityQuestionFound(List<UserLoginSecurityQuestion> userLoginSecurityQuestions) {
		this.setUserLoginSecurityQuestions(userLoginSecurityQuestions);
	}

	public List<UserLoginSecurityQuestion> getUserLoginSecurityQuestions()	{
		return userLoginSecurityQuestions;
	}

	public void setUserLoginSecurityQuestions(List<UserLoginSecurityQuestion> userLoginSecurityQuestions)	{
		this.userLoginSecurityQuestions = userLoginSecurityQuestions;
	}
}
