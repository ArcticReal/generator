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
import com.skytala.eCommerce.command.AddCustRequestContent;
import com.skytala.eCommerce.command.DeleteCustRequestContent;
import com.skytala.eCommerce.command.UpdateCustRequestContent;
import com.skytala.eCommerce.entity.CustRequestContent;
import com.skytala.eCommerce.entity.CustRequestContentMapper;
import com.skytala.eCommerce.event.CustRequestContentAdded;
import com.skytala.eCommerce.event.CustRequestContentDeleted;
import com.skytala.eCommerce.event.CustRequestContentFound;
import com.skytala.eCommerce.event.CustRequestContentUpdated;
import com.skytala.eCommerce.query.FindCustRequestContentsBy;

@RestController
@RequestMapping("/api/custRequestContent")
public class CustRequestContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestContent
	 * @return a List with the CustRequestContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestContent> findCustRequestContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestContentsBy query = new FindCustRequestContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestContentFound.class,
				event -> sendCustRequestContentsFoundMessage(((CustRequestContentFound) event).getCustRequestContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestContentsFoundMessage(List<CustRequestContent> custRequestContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestContents);
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
	public boolean createCustRequestContent(HttpServletRequest request) {

		CustRequestContent custRequestContentToBeAdded = new CustRequestContent();
		try {
			custRequestContentToBeAdded = CustRequestContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestContent(custRequestContentToBeAdded);

	}

	/**
	 * creates a new CustRequestContent entry in the ofbiz database
	 * 
	 * @param custRequestContentToBeAdded
	 *            the CustRequestContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestContent(CustRequestContent custRequestContentToBeAdded) {

		AddCustRequestContent com = new AddCustRequestContent(custRequestContentToBeAdded);
		int usedTicketId;

		synchronized (CustRequestContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestContentAdded.class,
				event -> sendCustRequestContentChangedMessage(((CustRequestContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestContent(HttpServletRequest request) {

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

		CustRequestContent custRequestContentToBeUpdated = new CustRequestContent();

		try {
			custRequestContentToBeUpdated = CustRequestContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestContent(custRequestContentToBeUpdated);

	}

	/**
	 * Updates the CustRequestContent with the specific Id
	 * 
	 * @param custRequestContentToBeUpdated the CustRequestContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestContent(CustRequestContent custRequestContentToBeUpdated) {

		UpdateCustRequestContent com = new UpdateCustRequestContent(custRequestContentToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestContentUpdated.class,
				event -> sendCustRequestContentChangedMessage(((CustRequestContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestContent from the database
	 * 
	 * @param custRequestContentId:
	 *            the id of the CustRequestContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestContentById(@RequestParam(value = "custRequestContentId") String custRequestContentId) {

		DeleteCustRequestContent com = new DeleteCustRequestContent(custRequestContentId);

		int usedTicketId;

		synchronized (CustRequestContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestContentDeleted.class,
				event -> sendCustRequestContentChangedMessage(((CustRequestContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestContent/\" plus one of the following: "
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