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
import com.skytala.eCommerce.command.AddFinAccountTransTypeAttr;
import com.skytala.eCommerce.command.DeleteFinAccountTransTypeAttr;
import com.skytala.eCommerce.command.UpdateFinAccountTransTypeAttr;
import com.skytala.eCommerce.entity.FinAccountTransTypeAttr;
import com.skytala.eCommerce.entity.FinAccountTransTypeAttrMapper;
import com.skytala.eCommerce.event.FinAccountTransTypeAttrAdded;
import com.skytala.eCommerce.event.FinAccountTransTypeAttrDeleted;
import com.skytala.eCommerce.event.FinAccountTransTypeAttrFound;
import com.skytala.eCommerce.event.FinAccountTransTypeAttrUpdated;
import com.skytala.eCommerce.query.FindFinAccountTransTypeAttrsBy;

@RestController
@RequestMapping("/api/finAccountTransTypeAttr")
public class FinAccountTransTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccountTransTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountTransTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccountTransTypeAttr
	 * @return a List with the FinAccountTransTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccountTransTypeAttr> findFinAccountTransTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountTransTypeAttrsBy query = new FindFinAccountTransTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountTransTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransTypeAttrFound.class,
				event -> sendFinAccountTransTypeAttrsFoundMessage(((FinAccountTransTypeAttrFound) event).getFinAccountTransTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountTransTypeAttrsFoundMessage(List<FinAccountTransTypeAttr> finAccountTransTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccountTransTypeAttrs);
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
	public boolean createFinAccountTransTypeAttr(HttpServletRequest request) {

		FinAccountTransTypeAttr finAccountTransTypeAttrToBeAdded = new FinAccountTransTypeAttr();
		try {
			finAccountTransTypeAttrToBeAdded = FinAccountTransTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccountTransTypeAttr(finAccountTransTypeAttrToBeAdded);

	}

	/**
	 * creates a new FinAccountTransTypeAttr entry in the ofbiz database
	 * 
	 * @param finAccountTransTypeAttrToBeAdded
	 *            the FinAccountTransTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccountTransTypeAttr(FinAccountTransTypeAttr finAccountTransTypeAttrToBeAdded) {

		AddFinAccountTransTypeAttr com = new AddFinAccountTransTypeAttr(finAccountTransTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (FinAccountTransTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransTypeAttrAdded.class,
				event -> sendFinAccountTransTypeAttrChangedMessage(((FinAccountTransTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccountTransTypeAttr(HttpServletRequest request) {

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

		FinAccountTransTypeAttr finAccountTransTypeAttrToBeUpdated = new FinAccountTransTypeAttr();

		try {
			finAccountTransTypeAttrToBeUpdated = FinAccountTransTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccountTransTypeAttr(finAccountTransTypeAttrToBeUpdated);

	}

	/**
	 * Updates the FinAccountTransTypeAttr with the specific Id
	 * 
	 * @param finAccountTransTypeAttrToBeUpdated the FinAccountTransTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccountTransTypeAttr(FinAccountTransTypeAttr finAccountTransTypeAttrToBeUpdated) {

		UpdateFinAccountTransTypeAttr com = new UpdateFinAccountTransTypeAttr(finAccountTransTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountTransTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransTypeAttrUpdated.class,
				event -> sendFinAccountTransTypeAttrChangedMessage(((FinAccountTransTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccountTransTypeAttr from the database
	 * 
	 * @param finAccountTransTypeAttrId:
	 *            the id of the FinAccountTransTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountTransTypeAttrById(@RequestParam(value = "finAccountTransTypeAttrId") String finAccountTransTypeAttrId) {

		DeleteFinAccountTransTypeAttr com = new DeleteFinAccountTransTypeAttr(finAccountTransTypeAttrId);

		int usedTicketId;

		synchronized (FinAccountTransTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransTypeAttrDeleted.class,
				event -> sendFinAccountTransTypeAttrChangedMessage(((FinAccountTransTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountTransTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccountTransTypeAttr/\" plus one of the following: "
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
