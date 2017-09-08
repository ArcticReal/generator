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
import com.skytala.eCommerce.command.AddValueLinkKey;
import com.skytala.eCommerce.command.DeleteValueLinkKey;
import com.skytala.eCommerce.command.UpdateValueLinkKey;
import com.skytala.eCommerce.entity.ValueLinkKey;
import com.skytala.eCommerce.entity.ValueLinkKeyMapper;
import com.skytala.eCommerce.event.ValueLinkKeyAdded;
import com.skytala.eCommerce.event.ValueLinkKeyDeleted;
import com.skytala.eCommerce.event.ValueLinkKeyFound;
import com.skytala.eCommerce.event.ValueLinkKeyUpdated;
import com.skytala.eCommerce.query.FindValueLinkKeysBy;

@RestController
@RequestMapping("/api/valueLinkKey")
public class ValueLinkKeyController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ValueLinkKey>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ValueLinkKeyController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ValueLinkKey
	 * @return a List with the ValueLinkKeys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ValueLinkKey> findValueLinkKeysBy(@RequestParam Map<String, String> allRequestParams) {

		FindValueLinkKeysBy query = new FindValueLinkKeysBy(allRequestParams);

		int usedTicketId;

		synchronized (ValueLinkKeyController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValueLinkKeyFound.class,
				event -> sendValueLinkKeysFoundMessage(((ValueLinkKeyFound) event).getValueLinkKeys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendValueLinkKeysFoundMessage(List<ValueLinkKey> valueLinkKeys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, valueLinkKeys);
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
	public boolean createValueLinkKey(HttpServletRequest request) {

		ValueLinkKey valueLinkKeyToBeAdded = new ValueLinkKey();
		try {
			valueLinkKeyToBeAdded = ValueLinkKeyMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createValueLinkKey(valueLinkKeyToBeAdded);

	}

	/**
	 * creates a new ValueLinkKey entry in the ofbiz database
	 * 
	 * @param valueLinkKeyToBeAdded
	 *            the ValueLinkKey thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createValueLinkKey(ValueLinkKey valueLinkKeyToBeAdded) {

		AddValueLinkKey com = new AddValueLinkKey(valueLinkKeyToBeAdded);
		int usedTicketId;

		synchronized (ValueLinkKeyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValueLinkKeyAdded.class,
				event -> sendValueLinkKeyChangedMessage(((ValueLinkKeyAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateValueLinkKey(HttpServletRequest request) {

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

		ValueLinkKey valueLinkKeyToBeUpdated = new ValueLinkKey();

		try {
			valueLinkKeyToBeUpdated = ValueLinkKeyMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateValueLinkKey(valueLinkKeyToBeUpdated);

	}

	/**
	 * Updates the ValueLinkKey with the specific Id
	 * 
	 * @param valueLinkKeyToBeUpdated the ValueLinkKey thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateValueLinkKey(ValueLinkKey valueLinkKeyToBeUpdated) {

		UpdateValueLinkKey com = new UpdateValueLinkKey(valueLinkKeyToBeUpdated);

		int usedTicketId;

		synchronized (ValueLinkKeyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValueLinkKeyUpdated.class,
				event -> sendValueLinkKeyChangedMessage(((ValueLinkKeyUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ValueLinkKey from the database
	 * 
	 * @param valueLinkKeyId:
	 *            the id of the ValueLinkKey thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletevalueLinkKeyById(@RequestParam(value = "valueLinkKeyId") String valueLinkKeyId) {

		DeleteValueLinkKey com = new DeleteValueLinkKey(valueLinkKeyId);

		int usedTicketId;

		synchronized (ValueLinkKeyController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ValueLinkKeyDeleted.class,
				event -> sendValueLinkKeyChangedMessage(((ValueLinkKeyDeleted) event).isSuccess(), usedTicketId));

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

	public void sendValueLinkKeyChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/valueLinkKey/\" plus one of the following: "
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
