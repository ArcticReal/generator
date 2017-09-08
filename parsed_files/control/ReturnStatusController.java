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
import com.skytala.eCommerce.command.AddReturnStatus;
import com.skytala.eCommerce.command.DeleteReturnStatus;
import com.skytala.eCommerce.command.UpdateReturnStatus;
import com.skytala.eCommerce.entity.ReturnStatus;
import com.skytala.eCommerce.entity.ReturnStatusMapper;
import com.skytala.eCommerce.event.ReturnStatusAdded;
import com.skytala.eCommerce.event.ReturnStatusDeleted;
import com.skytala.eCommerce.event.ReturnStatusFound;
import com.skytala.eCommerce.event.ReturnStatusUpdated;
import com.skytala.eCommerce.query.FindReturnStatussBy;

@RestController
@RequestMapping("/api/returnStatus")
public class ReturnStatusController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnStatus>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnStatusController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnStatus
	 * @return a List with the ReturnStatuss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnStatus> findReturnStatussBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnStatussBy query = new FindReturnStatussBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnStatusController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnStatusFound.class,
				event -> sendReturnStatussFoundMessage(((ReturnStatusFound) event).getReturnStatuss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnStatussFoundMessage(List<ReturnStatus> returnStatuss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnStatuss);
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
	public boolean createReturnStatus(HttpServletRequest request) {

		ReturnStatus returnStatusToBeAdded = new ReturnStatus();
		try {
			returnStatusToBeAdded = ReturnStatusMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnStatus(returnStatusToBeAdded);

	}

	/**
	 * creates a new ReturnStatus entry in the ofbiz database
	 * 
	 * @param returnStatusToBeAdded
	 *            the ReturnStatus thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnStatus(ReturnStatus returnStatusToBeAdded) {

		AddReturnStatus com = new AddReturnStatus(returnStatusToBeAdded);
		int usedTicketId;

		synchronized (ReturnStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnStatusAdded.class,
				event -> sendReturnStatusChangedMessage(((ReturnStatusAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnStatus(HttpServletRequest request) {

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

		ReturnStatus returnStatusToBeUpdated = new ReturnStatus();

		try {
			returnStatusToBeUpdated = ReturnStatusMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnStatus(returnStatusToBeUpdated);

	}

	/**
	 * Updates the ReturnStatus with the specific Id
	 * 
	 * @param returnStatusToBeUpdated the ReturnStatus thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnStatus(ReturnStatus returnStatusToBeUpdated) {

		UpdateReturnStatus com = new UpdateReturnStatus(returnStatusToBeUpdated);

		int usedTicketId;

		synchronized (ReturnStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnStatusUpdated.class,
				event -> sendReturnStatusChangedMessage(((ReturnStatusUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnStatus from the database
	 * 
	 * @param returnStatusId:
	 *            the id of the ReturnStatus thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnStatusById(@RequestParam(value = "returnStatusId") String returnStatusId) {

		DeleteReturnStatus com = new DeleteReturnStatus(returnStatusId);

		int usedTicketId;

		synchronized (ReturnStatusController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnStatusDeleted.class,
				event -> sendReturnStatusChangedMessage(((ReturnStatusDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnStatusChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnStatus/\" plus one of the following: "
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
