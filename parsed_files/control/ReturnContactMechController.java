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
import com.skytala.eCommerce.command.AddReturnContactMech;
import com.skytala.eCommerce.command.DeleteReturnContactMech;
import com.skytala.eCommerce.command.UpdateReturnContactMech;
import com.skytala.eCommerce.entity.ReturnContactMech;
import com.skytala.eCommerce.entity.ReturnContactMechMapper;
import com.skytala.eCommerce.event.ReturnContactMechAdded;
import com.skytala.eCommerce.event.ReturnContactMechDeleted;
import com.skytala.eCommerce.event.ReturnContactMechFound;
import com.skytala.eCommerce.event.ReturnContactMechUpdated;
import com.skytala.eCommerce.query.FindReturnContactMechsBy;

@RestController
@RequestMapping("/api/returnContactMech")
public class ReturnContactMechController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnContactMech>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnContactMechController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnContactMech
	 * @return a List with the ReturnContactMechs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnContactMech> findReturnContactMechsBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnContactMechsBy query = new FindReturnContactMechsBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnContactMechController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnContactMechFound.class,
				event -> sendReturnContactMechsFoundMessage(((ReturnContactMechFound) event).getReturnContactMechs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnContactMechsFoundMessage(List<ReturnContactMech> returnContactMechs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnContactMechs);
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
	public boolean createReturnContactMech(HttpServletRequest request) {

		ReturnContactMech returnContactMechToBeAdded = new ReturnContactMech();
		try {
			returnContactMechToBeAdded = ReturnContactMechMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnContactMech(returnContactMechToBeAdded);

	}

	/**
	 * creates a new ReturnContactMech entry in the ofbiz database
	 * 
	 * @param returnContactMechToBeAdded
	 *            the ReturnContactMech thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnContactMech(ReturnContactMech returnContactMechToBeAdded) {

		AddReturnContactMech com = new AddReturnContactMech(returnContactMechToBeAdded);
		int usedTicketId;

		synchronized (ReturnContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnContactMechAdded.class,
				event -> sendReturnContactMechChangedMessage(((ReturnContactMechAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnContactMech(HttpServletRequest request) {

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

		ReturnContactMech returnContactMechToBeUpdated = new ReturnContactMech();

		try {
			returnContactMechToBeUpdated = ReturnContactMechMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnContactMech(returnContactMechToBeUpdated);

	}

	/**
	 * Updates the ReturnContactMech with the specific Id
	 * 
	 * @param returnContactMechToBeUpdated the ReturnContactMech thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnContactMech(ReturnContactMech returnContactMechToBeUpdated) {

		UpdateReturnContactMech com = new UpdateReturnContactMech(returnContactMechToBeUpdated);

		int usedTicketId;

		synchronized (ReturnContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnContactMechUpdated.class,
				event -> sendReturnContactMechChangedMessage(((ReturnContactMechUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnContactMech from the database
	 * 
	 * @param returnContactMechId:
	 *            the id of the ReturnContactMech thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnContactMechById(@RequestParam(value = "returnContactMechId") String returnContactMechId) {

		DeleteReturnContactMech com = new DeleteReturnContactMech(returnContactMechId);

		int usedTicketId;

		synchronized (ReturnContactMechController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnContactMechDeleted.class,
				event -> sendReturnContactMechChangedMessage(((ReturnContactMechDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnContactMechChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnContactMech/\" plus one of the following: "
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
