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
import com.skytala.eCommerce.command.AddReturnItemResponse;
import com.skytala.eCommerce.command.DeleteReturnItemResponse;
import com.skytala.eCommerce.command.UpdateReturnItemResponse;
import com.skytala.eCommerce.entity.ReturnItemResponse;
import com.skytala.eCommerce.entity.ReturnItemResponseMapper;
import com.skytala.eCommerce.event.ReturnItemResponseAdded;
import com.skytala.eCommerce.event.ReturnItemResponseDeleted;
import com.skytala.eCommerce.event.ReturnItemResponseFound;
import com.skytala.eCommerce.event.ReturnItemResponseUpdated;
import com.skytala.eCommerce.query.FindReturnItemResponsesBy;

@RestController
@RequestMapping("/api/returnItemResponse")
public class ReturnItemResponseController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnItemResponse>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnItemResponseController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnItemResponse
	 * @return a List with the ReturnItemResponses
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnItemResponse> findReturnItemResponsesBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnItemResponsesBy query = new FindReturnItemResponsesBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnItemResponseController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemResponseFound.class,
				event -> sendReturnItemResponsesFoundMessage(((ReturnItemResponseFound) event).getReturnItemResponses(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnItemResponsesFoundMessage(List<ReturnItemResponse> returnItemResponses, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnItemResponses);
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
	public boolean createReturnItemResponse(HttpServletRequest request) {

		ReturnItemResponse returnItemResponseToBeAdded = new ReturnItemResponse();
		try {
			returnItemResponseToBeAdded = ReturnItemResponseMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnItemResponse(returnItemResponseToBeAdded);

	}

	/**
	 * creates a new ReturnItemResponse entry in the ofbiz database
	 * 
	 * @param returnItemResponseToBeAdded
	 *            the ReturnItemResponse thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnItemResponse(ReturnItemResponse returnItemResponseToBeAdded) {

		AddReturnItemResponse com = new AddReturnItemResponse(returnItemResponseToBeAdded);
		int usedTicketId;

		synchronized (ReturnItemResponseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemResponseAdded.class,
				event -> sendReturnItemResponseChangedMessage(((ReturnItemResponseAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnItemResponse(HttpServletRequest request) {

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

		ReturnItemResponse returnItemResponseToBeUpdated = new ReturnItemResponse();

		try {
			returnItemResponseToBeUpdated = ReturnItemResponseMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnItemResponse(returnItemResponseToBeUpdated);

	}

	/**
	 * Updates the ReturnItemResponse with the specific Id
	 * 
	 * @param returnItemResponseToBeUpdated the ReturnItemResponse thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnItemResponse(ReturnItemResponse returnItemResponseToBeUpdated) {

		UpdateReturnItemResponse com = new UpdateReturnItemResponse(returnItemResponseToBeUpdated);

		int usedTicketId;

		synchronized (ReturnItemResponseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemResponseUpdated.class,
				event -> sendReturnItemResponseChangedMessage(((ReturnItemResponseUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnItemResponse from the database
	 * 
	 * @param returnItemResponseId:
	 *            the id of the ReturnItemResponse thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnItemResponseById(@RequestParam(value = "returnItemResponseId") String returnItemResponseId) {

		DeleteReturnItemResponse com = new DeleteReturnItemResponse(returnItemResponseId);

		int usedTicketId;

		synchronized (ReturnItemResponseController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnItemResponseDeleted.class,
				event -> sendReturnItemResponseChangedMessage(((ReturnItemResponseDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnItemResponseChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnItemResponse/\" plus one of the following: "
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
