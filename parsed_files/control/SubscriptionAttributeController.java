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
import com.skytala.eCommerce.command.AddSubscriptionAttribute;
import com.skytala.eCommerce.command.DeleteSubscriptionAttribute;
import com.skytala.eCommerce.command.UpdateSubscriptionAttribute;
import com.skytala.eCommerce.entity.SubscriptionAttribute;
import com.skytala.eCommerce.entity.SubscriptionAttributeMapper;
import com.skytala.eCommerce.event.SubscriptionAttributeAdded;
import com.skytala.eCommerce.event.SubscriptionAttributeDeleted;
import com.skytala.eCommerce.event.SubscriptionAttributeFound;
import com.skytala.eCommerce.event.SubscriptionAttributeUpdated;
import com.skytala.eCommerce.query.FindSubscriptionAttributesBy;

@RestController
@RequestMapping("/api/subscriptionAttribute")
public class SubscriptionAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SubscriptionAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SubscriptionAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SubscriptionAttribute
	 * @return a List with the SubscriptionAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SubscriptionAttribute> findSubscriptionAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSubscriptionAttributesBy query = new FindSubscriptionAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (SubscriptionAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionAttributeFound.class,
				event -> sendSubscriptionAttributesFoundMessage(((SubscriptionAttributeFound) event).getSubscriptionAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSubscriptionAttributesFoundMessage(List<SubscriptionAttribute> subscriptionAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, subscriptionAttributes);
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
	public boolean createSubscriptionAttribute(HttpServletRequest request) {

		SubscriptionAttribute subscriptionAttributeToBeAdded = new SubscriptionAttribute();
		try {
			subscriptionAttributeToBeAdded = SubscriptionAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSubscriptionAttribute(subscriptionAttributeToBeAdded);

	}

	/**
	 * creates a new SubscriptionAttribute entry in the ofbiz database
	 * 
	 * @param subscriptionAttributeToBeAdded
	 *            the SubscriptionAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSubscriptionAttribute(SubscriptionAttribute subscriptionAttributeToBeAdded) {

		AddSubscriptionAttribute com = new AddSubscriptionAttribute(subscriptionAttributeToBeAdded);
		int usedTicketId;

		synchronized (SubscriptionAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionAttributeAdded.class,
				event -> sendSubscriptionAttributeChangedMessage(((SubscriptionAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSubscriptionAttribute(HttpServletRequest request) {

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

		SubscriptionAttribute subscriptionAttributeToBeUpdated = new SubscriptionAttribute();

		try {
			subscriptionAttributeToBeUpdated = SubscriptionAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSubscriptionAttribute(subscriptionAttributeToBeUpdated);

	}

	/**
	 * Updates the SubscriptionAttribute with the specific Id
	 * 
	 * @param subscriptionAttributeToBeUpdated the SubscriptionAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSubscriptionAttribute(SubscriptionAttribute subscriptionAttributeToBeUpdated) {

		UpdateSubscriptionAttribute com = new UpdateSubscriptionAttribute(subscriptionAttributeToBeUpdated);

		int usedTicketId;

		synchronized (SubscriptionAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionAttributeUpdated.class,
				event -> sendSubscriptionAttributeChangedMessage(((SubscriptionAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SubscriptionAttribute from the database
	 * 
	 * @param subscriptionAttributeId:
	 *            the id of the SubscriptionAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesubscriptionAttributeById(@RequestParam(value = "subscriptionAttributeId") String subscriptionAttributeId) {

		DeleteSubscriptionAttribute com = new DeleteSubscriptionAttribute(subscriptionAttributeId);

		int usedTicketId;

		synchronized (SubscriptionAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionAttributeDeleted.class,
				event -> sendSubscriptionAttributeChangedMessage(((SubscriptionAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSubscriptionAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/subscriptionAttribute/\" plus one of the following: "
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
