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
import com.skytala.eCommerce.command.AddFinAccountAttribute;
import com.skytala.eCommerce.command.DeleteFinAccountAttribute;
import com.skytala.eCommerce.command.UpdateFinAccountAttribute;
import com.skytala.eCommerce.entity.FinAccountAttribute;
import com.skytala.eCommerce.entity.FinAccountAttributeMapper;
import com.skytala.eCommerce.event.FinAccountAttributeAdded;
import com.skytala.eCommerce.event.FinAccountAttributeDeleted;
import com.skytala.eCommerce.event.FinAccountAttributeFound;
import com.skytala.eCommerce.event.FinAccountAttributeUpdated;
import com.skytala.eCommerce.query.FindFinAccountAttributesBy;

@RestController
@RequestMapping("/api/finAccountAttribute")
public class FinAccountAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccountAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccountAttribute
	 * @return a List with the FinAccountAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccountAttribute> findFinAccountAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountAttributesBy query = new FindFinAccountAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountAttributeFound.class,
				event -> sendFinAccountAttributesFoundMessage(((FinAccountAttributeFound) event).getFinAccountAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountAttributesFoundMessage(List<FinAccountAttribute> finAccountAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccountAttributes);
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
	public boolean createFinAccountAttribute(HttpServletRequest request) {

		FinAccountAttribute finAccountAttributeToBeAdded = new FinAccountAttribute();
		try {
			finAccountAttributeToBeAdded = FinAccountAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccountAttribute(finAccountAttributeToBeAdded);

	}

	/**
	 * creates a new FinAccountAttribute entry in the ofbiz database
	 * 
	 * @param finAccountAttributeToBeAdded
	 *            the FinAccountAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccountAttribute(FinAccountAttribute finAccountAttributeToBeAdded) {

		AddFinAccountAttribute com = new AddFinAccountAttribute(finAccountAttributeToBeAdded);
		int usedTicketId;

		synchronized (FinAccountAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountAttributeAdded.class,
				event -> sendFinAccountAttributeChangedMessage(((FinAccountAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccountAttribute(HttpServletRequest request) {

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

		FinAccountAttribute finAccountAttributeToBeUpdated = new FinAccountAttribute();

		try {
			finAccountAttributeToBeUpdated = FinAccountAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccountAttribute(finAccountAttributeToBeUpdated);

	}

	/**
	 * Updates the FinAccountAttribute with the specific Id
	 * 
	 * @param finAccountAttributeToBeUpdated the FinAccountAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccountAttribute(FinAccountAttribute finAccountAttributeToBeUpdated) {

		UpdateFinAccountAttribute com = new UpdateFinAccountAttribute(finAccountAttributeToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountAttributeUpdated.class,
				event -> sendFinAccountAttributeChangedMessage(((FinAccountAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccountAttribute from the database
	 * 
	 * @param finAccountAttributeId:
	 *            the id of the FinAccountAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountAttributeById(@RequestParam(value = "finAccountAttributeId") String finAccountAttributeId) {

		DeleteFinAccountAttribute com = new DeleteFinAccountAttribute(finAccountAttributeId);

		int usedTicketId;

		synchronized (FinAccountAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountAttributeDeleted.class,
				event -> sendFinAccountAttributeChangedMessage(((FinAccountAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccountAttribute/\" plus one of the following: "
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
