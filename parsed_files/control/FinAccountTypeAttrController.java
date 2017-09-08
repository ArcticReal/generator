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
import com.skytala.eCommerce.command.AddFinAccountTypeAttr;
import com.skytala.eCommerce.command.DeleteFinAccountTypeAttr;
import com.skytala.eCommerce.command.UpdateFinAccountTypeAttr;
import com.skytala.eCommerce.entity.FinAccountTypeAttr;
import com.skytala.eCommerce.entity.FinAccountTypeAttrMapper;
import com.skytala.eCommerce.event.FinAccountTypeAttrAdded;
import com.skytala.eCommerce.event.FinAccountTypeAttrDeleted;
import com.skytala.eCommerce.event.FinAccountTypeAttrFound;
import com.skytala.eCommerce.event.FinAccountTypeAttrUpdated;
import com.skytala.eCommerce.query.FindFinAccountTypeAttrsBy;

@RestController
@RequestMapping("/api/finAccountTypeAttr")
public class FinAccountTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccountTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccountTypeAttr
	 * @return a List with the FinAccountTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccountTypeAttr> findFinAccountTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountTypeAttrsBy query = new FindFinAccountTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeAttrFound.class,
				event -> sendFinAccountTypeAttrsFoundMessage(((FinAccountTypeAttrFound) event).getFinAccountTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountTypeAttrsFoundMessage(List<FinAccountTypeAttr> finAccountTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccountTypeAttrs);
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
	public boolean createFinAccountTypeAttr(HttpServletRequest request) {

		FinAccountTypeAttr finAccountTypeAttrToBeAdded = new FinAccountTypeAttr();
		try {
			finAccountTypeAttrToBeAdded = FinAccountTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccountTypeAttr(finAccountTypeAttrToBeAdded);

	}

	/**
	 * creates a new FinAccountTypeAttr entry in the ofbiz database
	 * 
	 * @param finAccountTypeAttrToBeAdded
	 *            the FinAccountTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccountTypeAttr(FinAccountTypeAttr finAccountTypeAttrToBeAdded) {

		AddFinAccountTypeAttr com = new AddFinAccountTypeAttr(finAccountTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (FinAccountTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeAttrAdded.class,
				event -> sendFinAccountTypeAttrChangedMessage(((FinAccountTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccountTypeAttr(HttpServletRequest request) {

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

		FinAccountTypeAttr finAccountTypeAttrToBeUpdated = new FinAccountTypeAttr();

		try {
			finAccountTypeAttrToBeUpdated = FinAccountTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccountTypeAttr(finAccountTypeAttrToBeUpdated);

	}

	/**
	 * Updates the FinAccountTypeAttr with the specific Id
	 * 
	 * @param finAccountTypeAttrToBeUpdated the FinAccountTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccountTypeAttr(FinAccountTypeAttr finAccountTypeAttrToBeUpdated) {

		UpdateFinAccountTypeAttr com = new UpdateFinAccountTypeAttr(finAccountTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeAttrUpdated.class,
				event -> sendFinAccountTypeAttrChangedMessage(((FinAccountTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccountTypeAttr from the database
	 * 
	 * @param finAccountTypeAttrId:
	 *            the id of the FinAccountTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountTypeAttrById(@RequestParam(value = "finAccountTypeAttrId") String finAccountTypeAttrId) {

		DeleteFinAccountTypeAttr com = new DeleteFinAccountTypeAttr(finAccountTypeAttrId);

		int usedTicketId;

		synchronized (FinAccountTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTypeAttrDeleted.class,
				event -> sendFinAccountTypeAttrChangedMessage(((FinAccountTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccountTypeAttr/\" plus one of the following: "
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
