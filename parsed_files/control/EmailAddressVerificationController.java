package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddEmailAddressVerification;
import com.skytala.eCommerce.command.DeleteEmailAddressVerification;
import com.skytala.eCommerce.command.UpdateEmailAddressVerification;
import com.skytala.eCommerce.entity.EmailAddressVerification;
import com.skytala.eCommerce.entity.EmailAddressVerificationMapper;
import com.skytala.eCommerce.event.EmailAddressVerificationAdded;
import com.skytala.eCommerce.event.EmailAddressVerificationDeleted;
import com.skytala.eCommerce.event.EmailAddressVerificationFound;
import com.skytala.eCommerce.event.EmailAddressVerificationUpdated;
import com.skytala.eCommerce.query.FindEmailAddressVerificationsBy;

@RestController
@RequestMapping("/api/emailAddressVerification")
public class EmailAddressVerificationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<EmailAddressVerification>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public EmailAddressVerificationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a EmailAddressVerification
	 * @return a List with the EmailAddressVerifications
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<EmailAddressVerification> findEmailAddressVerificationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindEmailAddressVerificationsBy query = new FindEmailAddressVerificationsBy(allRequestParams);

		int usedTicketId;

		synchronized (EmailAddressVerificationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmailAddressVerificationFound.class,
				event -> sendEmailAddressVerificationsFoundMessage(((EmailAddressVerificationFound) event).getEmailAddressVerifications(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendEmailAddressVerificationsFoundMessage(List<EmailAddressVerification> emailAddressVerifications, int usedTicketId) {
		queryReturnVal.put(usedTicketId, emailAddressVerifications);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createEmailAddressVerification(HttpServletRequest request) {

		EmailAddressVerification emailAddressVerificationToBeAdded = new EmailAddressVerification();
		try {
			emailAddressVerificationToBeAdded = EmailAddressVerificationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createEmailAddressVerification(emailAddressVerificationToBeAdded);

	}

	/**
	 * creates a new EmailAddressVerification entry in the ofbiz database
	 * 
	 * @param emailAddressVerificationToBeAdded
	 *            the EmailAddressVerification thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createEmailAddressVerification(EmailAddressVerification emailAddressVerificationToBeAdded) {

		AddEmailAddressVerification com = new AddEmailAddressVerification(emailAddressVerificationToBeAdded);
		int usedTicketId;

		synchronized (EmailAddressVerificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmailAddressVerificationAdded.class,
				event -> sendEmailAddressVerificationChangedMessage(((EmailAddressVerificationAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateEmailAddressVerification(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		EmailAddressVerification emailAddressVerificationToBeUpdated = new EmailAddressVerification();

		try {
			emailAddressVerificationToBeUpdated = EmailAddressVerificationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateEmailAddressVerification(emailAddressVerificationToBeUpdated);

	}

	/**
	 * Updates the EmailAddressVerification with the specific Id
	 * 
	 * @param emailAddressVerificationToBeUpdated the EmailAddressVerification thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateEmailAddressVerification(EmailAddressVerification emailAddressVerificationToBeUpdated) {

		UpdateEmailAddressVerification com = new UpdateEmailAddressVerification(emailAddressVerificationToBeUpdated);

		int usedTicketId;

		synchronized (EmailAddressVerificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmailAddressVerificationUpdated.class,
				event -> sendEmailAddressVerificationChangedMessage(((EmailAddressVerificationUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a EmailAddressVerification from the database
	 * 
	 * @param emailAddressVerificationId:
	 *            the id of the EmailAddressVerification thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteemailAddressVerificationById(@RequestParam(value = "emailAddressVerificationId") String emailAddressVerificationId) {

		DeleteEmailAddressVerification com = new DeleteEmailAddressVerification(emailAddressVerificationId);

		int usedTicketId;

		synchronized (EmailAddressVerificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(EmailAddressVerificationDeleted.class,
				event -> sendEmailAddressVerificationChangedMessage(((EmailAddressVerificationDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendEmailAddressVerificationChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/emailAddressVerification/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}
