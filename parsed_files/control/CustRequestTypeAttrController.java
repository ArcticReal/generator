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
import com.skytala.eCommerce.command.AddCustRequestTypeAttr;
import com.skytala.eCommerce.command.DeleteCustRequestTypeAttr;
import com.skytala.eCommerce.command.UpdateCustRequestTypeAttr;
import com.skytala.eCommerce.entity.CustRequestTypeAttr;
import com.skytala.eCommerce.entity.CustRequestTypeAttrMapper;
import com.skytala.eCommerce.event.CustRequestTypeAttrAdded;
import com.skytala.eCommerce.event.CustRequestTypeAttrDeleted;
import com.skytala.eCommerce.event.CustRequestTypeAttrFound;
import com.skytala.eCommerce.event.CustRequestTypeAttrUpdated;
import com.skytala.eCommerce.query.FindCustRequestTypeAttrsBy;

@RestController
@RequestMapping("/api/custRequestTypeAttr")
public class CustRequestTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<CustRequestTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public CustRequestTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a CustRequestTypeAttr
	 * @return a List with the CustRequestTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<CustRequestTypeAttr> findCustRequestTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindCustRequestTypeAttrsBy query = new FindCustRequestTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (CustRequestTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestTypeAttrFound.class,
				event -> sendCustRequestTypeAttrsFoundMessage(((CustRequestTypeAttrFound) event).getCustRequestTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendCustRequestTypeAttrsFoundMessage(List<CustRequestTypeAttr> custRequestTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, custRequestTypeAttrs);
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
	public boolean createCustRequestTypeAttr(HttpServletRequest request) {

		CustRequestTypeAttr custRequestTypeAttrToBeAdded = new CustRequestTypeAttr();
		try {
			custRequestTypeAttrToBeAdded = CustRequestTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createCustRequestTypeAttr(custRequestTypeAttrToBeAdded);

	}

	/**
	 * creates a new CustRequestTypeAttr entry in the ofbiz database
	 * 
	 * @param custRequestTypeAttrToBeAdded
	 *            the CustRequestTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createCustRequestTypeAttr(CustRequestTypeAttr custRequestTypeAttrToBeAdded) {

		AddCustRequestTypeAttr com = new AddCustRequestTypeAttr(custRequestTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (CustRequestTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestTypeAttrAdded.class,
				event -> sendCustRequestTypeAttrChangedMessage(((CustRequestTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateCustRequestTypeAttr(HttpServletRequest request) {

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

		CustRequestTypeAttr custRequestTypeAttrToBeUpdated = new CustRequestTypeAttr();

		try {
			custRequestTypeAttrToBeUpdated = CustRequestTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateCustRequestTypeAttr(custRequestTypeAttrToBeUpdated);

	}

	/**
	 * Updates the CustRequestTypeAttr with the specific Id
	 * 
	 * @param custRequestTypeAttrToBeUpdated the CustRequestTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateCustRequestTypeAttr(CustRequestTypeAttr custRequestTypeAttrToBeUpdated) {

		UpdateCustRequestTypeAttr com = new UpdateCustRequestTypeAttr(custRequestTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (CustRequestTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestTypeAttrUpdated.class,
				event -> sendCustRequestTypeAttrChangedMessage(((CustRequestTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a CustRequestTypeAttr from the database
	 * 
	 * @param custRequestTypeAttrId:
	 *            the id of the CustRequestTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletecustRequestTypeAttrById(@RequestParam(value = "custRequestTypeAttrId") String custRequestTypeAttrId) {

		DeleteCustRequestTypeAttr com = new DeleteCustRequestTypeAttr(custRequestTypeAttrId);

		int usedTicketId;

		synchronized (CustRequestTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(CustRequestTypeAttrDeleted.class,
				event -> sendCustRequestTypeAttrChangedMessage(((CustRequestTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendCustRequestTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/custRequestTypeAttr/\" plus one of the following: "
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
