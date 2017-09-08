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
import com.skytala.eCommerce.command.AddProtectedView;
import com.skytala.eCommerce.command.DeleteProtectedView;
import com.skytala.eCommerce.command.UpdateProtectedView;
import com.skytala.eCommerce.entity.ProtectedView;
import com.skytala.eCommerce.entity.ProtectedViewMapper;
import com.skytala.eCommerce.event.ProtectedViewAdded;
import com.skytala.eCommerce.event.ProtectedViewDeleted;
import com.skytala.eCommerce.event.ProtectedViewFound;
import com.skytala.eCommerce.event.ProtectedViewUpdated;
import com.skytala.eCommerce.query.FindProtectedViewsBy;

@RestController
@RequestMapping("/api/protectedView")
public class ProtectedViewController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProtectedView>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProtectedViewController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProtectedView
	 * @return a List with the ProtectedViews
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProtectedView> findProtectedViewsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProtectedViewsBy query = new FindProtectedViewsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProtectedViewController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProtectedViewFound.class,
				event -> sendProtectedViewsFoundMessage(((ProtectedViewFound) event).getProtectedViews(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProtectedViewsFoundMessage(List<ProtectedView> protectedViews, int usedTicketId) {
		queryReturnVal.put(usedTicketId, protectedViews);
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
	public boolean createProtectedView(HttpServletRequest request) {

		ProtectedView protectedViewToBeAdded = new ProtectedView();
		try {
			protectedViewToBeAdded = ProtectedViewMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProtectedView(protectedViewToBeAdded);

	}

	/**
	 * creates a new ProtectedView entry in the ofbiz database
	 * 
	 * @param protectedViewToBeAdded
	 *            the ProtectedView thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProtectedView(ProtectedView protectedViewToBeAdded) {

		AddProtectedView com = new AddProtectedView(protectedViewToBeAdded);
		int usedTicketId;

		synchronized (ProtectedViewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProtectedViewAdded.class,
				event -> sendProtectedViewChangedMessage(((ProtectedViewAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProtectedView(HttpServletRequest request) {

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

		ProtectedView protectedViewToBeUpdated = new ProtectedView();

		try {
			protectedViewToBeUpdated = ProtectedViewMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProtectedView(protectedViewToBeUpdated);

	}

	/**
	 * Updates the ProtectedView with the specific Id
	 * 
	 * @param protectedViewToBeUpdated the ProtectedView thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProtectedView(ProtectedView protectedViewToBeUpdated) {

		UpdateProtectedView com = new UpdateProtectedView(protectedViewToBeUpdated);

		int usedTicketId;

		synchronized (ProtectedViewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProtectedViewUpdated.class,
				event -> sendProtectedViewChangedMessage(((ProtectedViewUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProtectedView from the database
	 * 
	 * @param protectedViewId:
	 *            the id of the ProtectedView thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteprotectedViewById(@RequestParam(value = "protectedViewId") String protectedViewId) {

		DeleteProtectedView com = new DeleteProtectedView(protectedViewId);

		int usedTicketId;

		synchronized (ProtectedViewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProtectedViewDeleted.class,
				event -> sendProtectedViewChangedMessage(((ProtectedViewDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProtectedViewChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/protectedView/\" plus one of the following: "
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
