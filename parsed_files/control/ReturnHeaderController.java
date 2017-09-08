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
import com.skytala.eCommerce.command.AddReturnHeader;
import com.skytala.eCommerce.command.DeleteReturnHeader;
import com.skytala.eCommerce.command.UpdateReturnHeader;
import com.skytala.eCommerce.entity.ReturnHeader;
import com.skytala.eCommerce.entity.ReturnHeaderMapper;
import com.skytala.eCommerce.event.ReturnHeaderAdded;
import com.skytala.eCommerce.event.ReturnHeaderDeleted;
import com.skytala.eCommerce.event.ReturnHeaderFound;
import com.skytala.eCommerce.event.ReturnHeaderUpdated;
import com.skytala.eCommerce.query.FindReturnHeadersBy;

@RestController
@RequestMapping("/api/returnHeader")
public class ReturnHeaderController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ReturnHeader>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ReturnHeaderController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ReturnHeader
	 * @return a List with the ReturnHeaders
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ReturnHeader> findReturnHeadersBy(@RequestParam Map<String, String> allRequestParams) {

		FindReturnHeadersBy query = new FindReturnHeadersBy(allRequestParams);

		int usedTicketId;

		synchronized (ReturnHeaderController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnHeaderFound.class,
				event -> sendReturnHeadersFoundMessage(((ReturnHeaderFound) event).getReturnHeaders(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendReturnHeadersFoundMessage(List<ReturnHeader> returnHeaders, int usedTicketId) {
		queryReturnVal.put(usedTicketId, returnHeaders);
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
	public boolean createReturnHeader(HttpServletRequest request) {

		ReturnHeader returnHeaderToBeAdded = new ReturnHeader();
		try {
			returnHeaderToBeAdded = ReturnHeaderMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createReturnHeader(returnHeaderToBeAdded);

	}

	/**
	 * creates a new ReturnHeader entry in the ofbiz database
	 * 
	 * @param returnHeaderToBeAdded
	 *            the ReturnHeader thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createReturnHeader(ReturnHeader returnHeaderToBeAdded) {

		AddReturnHeader com = new AddReturnHeader(returnHeaderToBeAdded);
		int usedTicketId;

		synchronized (ReturnHeaderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnHeaderAdded.class,
				event -> sendReturnHeaderChangedMessage(((ReturnHeaderAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateReturnHeader(HttpServletRequest request) {

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

		ReturnHeader returnHeaderToBeUpdated = new ReturnHeader();

		try {
			returnHeaderToBeUpdated = ReturnHeaderMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateReturnHeader(returnHeaderToBeUpdated);

	}

	/**
	 * Updates the ReturnHeader with the specific Id
	 * 
	 * @param returnHeaderToBeUpdated the ReturnHeader thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateReturnHeader(ReturnHeader returnHeaderToBeUpdated) {

		UpdateReturnHeader com = new UpdateReturnHeader(returnHeaderToBeUpdated);

		int usedTicketId;

		synchronized (ReturnHeaderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnHeaderUpdated.class,
				event -> sendReturnHeaderChangedMessage(((ReturnHeaderUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ReturnHeader from the database
	 * 
	 * @param returnHeaderId:
	 *            the id of the ReturnHeader thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletereturnHeaderById(@RequestParam(value = "returnHeaderId") String returnHeaderId) {

		DeleteReturnHeader com = new DeleteReturnHeader(returnHeaderId);

		int usedTicketId;

		synchronized (ReturnHeaderController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ReturnHeaderDeleted.class,
				event -> sendReturnHeaderChangedMessage(((ReturnHeaderDeleted) event).isSuccess(), usedTicketId));

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

	public void sendReturnHeaderChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/returnHeader/\" plus one of the following: "
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
