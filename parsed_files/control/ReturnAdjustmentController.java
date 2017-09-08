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
import com.skytala.eCommerce.command.AddReturnAdjustment;
import com.skytala.eCommerce.command.DeleteReturnAdjustment;
import com.skytala.eCommerce.command.UpdateReturnAdjustment;
import com.skytala.eCommerce.entity.ReturnAdjustment;
import com.skytala.eCommerce.entity.ReturnAdjustmentMapper;
import com.skytala.eCommerce.event.ReturnAdjustmentAdded;
import com.skytala.eCommerce.event.ReturnAdjustmentDeleted;
import com.skytala.eCommerce.event.ReturnAdjustmentFound;
import com.skytala.eCommerce.event.ReturnAdjustmentUpdated;
import com.skytala.eCommerce.query.FindReturnAdjustmentsBy;

@RestController
@RequestMapping("/api/returnAdjustment")
public class ReturnAdjustmentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnAdjustment>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnAdjustmentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnAdjustment
	 * @return a List with the ReturnAdjustments
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnAdjustment> findReturnAdjustmentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnAdjustmentsBy query = new FindReturnAdjustmentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnAdjustmentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnAdjustmentFound.class,
				event -> sendReturnAdjustmentsFoundMessage(((ReturnAdjustmentFound) event).getReturnAdjustments(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnAdjustmentsFoundMessage(List<ReturnAdjustment> returnAdjustments, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnAdjustments);
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
	public boolean createReturnAdjustment(HttpServletRequest request) {

		ReturnAdjustment returnAdjustmentToBeAdded = new ReturnAdjustment();
		try {
			returnAdjustmentToBeAdded = ReturnAdjustmentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnAdjustment(returnAdjustmentToBeAdded);

	}

	/**
	 * creates a new ReturnAdjustment entry in the ofbiz database
	 * 
	 * @param returnAdjustmentToBeAdded
	 *            the ReturnAdjustment thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnAdjustment(ReturnAdjustment returnAdjustmentToBeAdded) {

		AddReturnAdjustment com = new AddReturnAdjustment(returnAdjustmentToBeAdded);
		int usedTicketId;

		synchronized (ReturnAdjustmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnAdjustmentAdded.class,
				event -> sendReturnAdjustmentChangedMessage(((ReturnAdjustmentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnAdjustment(HttpServletRequest request) {

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

		ReturnAdjustment returnAdjustmentToBeUpdated = new ReturnAdjustment();

		try {
			returnAdjustmentToBeUpdated = ReturnAdjustmentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnAdjustment(returnAdjustmentToBeUpdated);

	}

	/**
	 * Updates the ReturnAdjustment with the specific Id
	 * 
	 * @param returnAdjustmentToBeUpdated the ReturnAdjustment thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnAdjustment(ReturnAdjustment returnAdjustmentToBeUpdated) {

		UpdateReturnAdjustment com = new UpdateReturnAdjustment(returnAdjustmentToBeUpdated);

		int usedTicketId;

		synchronized (ReturnAdjustmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnAdjustmentUpdated.class,
				event -> sendReturnAdjustmentChangedMessage(((ReturnAdjustmentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnAdjustment from the database
	 * 
	 * @param returnAdjustmentId:
	 *            the id of the ReturnAdjustment thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnAdjustmentById(@RequestParam(value = "returnAdjustmentId") String returnAdjustmentId) {

		DeleteReturnAdjustment com = new DeleteReturnAdjustment(returnAdjustmentId);

		int usedTicketId;

		synchronized (ReturnAdjustmentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnAdjustmentDeleted.class,
				event -> sendReturnAdjustmentChangedMessage(((ReturnAdjustmentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnAdjustmentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnAdjustment/\" plus one of the following: "
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
