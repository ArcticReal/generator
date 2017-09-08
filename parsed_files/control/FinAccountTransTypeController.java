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
import com.skytala.eCommerce.command.AddFinAccountTransType;
import com.skytala.eCommerce.command.DeleteFinAccountTransType;
import com.skytala.eCommerce.command.UpdateFinAccountTransType;
import com.skytala.eCommerce.entity.FinAccountTransType;
import com.skytala.eCommerce.entity.FinAccountTransTypeMapper;
import com.skytala.eCommerce.event.FinAccountTransTypeAdded;
import com.skytala.eCommerce.event.FinAccountTransTypeDeleted;
import com.skytala.eCommerce.event.FinAccountTransTypeFound;
import com.skytala.eCommerce.event.FinAccountTransTypeUpdated;
import com.skytala.eCommerce.query.FindFinAccountTransTypesBy;

@RestController
@RequestMapping("/api/finAccountTransType")
public class FinAccountTransTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccountTransType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountTransTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccountTransType
	 * @return a List with the FinAccountTransTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccountTransType> findFinAccountTransTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountTransTypesBy query = new FindFinAccountTransTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountTransTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransTypeFound.class,
				event -> sendFinAccountTransTypesFoundMessage(((FinAccountTransTypeFound) event).getFinAccountTransTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountTransTypesFoundMessage(List<FinAccountTransType> finAccountTransTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccountTransTypes);
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
	public boolean createFinAccountTransType(HttpServletRequest request) {

		FinAccountTransType finAccountTransTypeToBeAdded = new FinAccountTransType();
		try {
			finAccountTransTypeToBeAdded = FinAccountTransTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccountTransType(finAccountTransTypeToBeAdded);

	}

	/**
	 * creates a new FinAccountTransType entry in the ofbiz database
	 * 
	 * @param finAccountTransTypeToBeAdded
	 *            the FinAccountTransType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccountTransType(FinAccountTransType finAccountTransTypeToBeAdded) {

		AddFinAccountTransType com = new AddFinAccountTransType(finAccountTransTypeToBeAdded);
		int usedTicketId;

		synchronized (FinAccountTransTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransTypeAdded.class,
				event -> sendFinAccountTransTypeChangedMessage(((FinAccountTransTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccountTransType(HttpServletRequest request) {

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

		FinAccountTransType finAccountTransTypeToBeUpdated = new FinAccountTransType();

		try {
			finAccountTransTypeToBeUpdated = FinAccountTransTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccountTransType(finAccountTransTypeToBeUpdated);

	}

	/**
	 * Updates the FinAccountTransType with the specific Id
	 * 
	 * @param finAccountTransTypeToBeUpdated the FinAccountTransType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccountTransType(FinAccountTransType finAccountTransTypeToBeUpdated) {

		UpdateFinAccountTransType com = new UpdateFinAccountTransType(finAccountTransTypeToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountTransTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransTypeUpdated.class,
				event -> sendFinAccountTransTypeChangedMessage(((FinAccountTransTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccountTransType from the database
	 * 
	 * @param finAccountTransTypeId:
	 *            the id of the FinAccountTransType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountTransTypeById(@RequestParam(value = "finAccountTransTypeId") String finAccountTransTypeId) {

		DeleteFinAccountTransType com = new DeleteFinAccountTransType(finAccountTransTypeId);

		int usedTicketId;

		synchronized (FinAccountTransTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransTypeDeleted.class,
				event -> sendFinAccountTransTypeChangedMessage(((FinAccountTransTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountTransTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccountTransType/\" plus one of the following: "
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
