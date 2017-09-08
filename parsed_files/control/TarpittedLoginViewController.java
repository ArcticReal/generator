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
import com.skytala.eCommerce.command.AddTarpittedLoginView;
import com.skytala.eCommerce.command.DeleteTarpittedLoginView;
import com.skytala.eCommerce.command.UpdateTarpittedLoginView;
import com.skytala.eCommerce.entity.TarpittedLoginView;
import com.skytala.eCommerce.entity.TarpittedLoginViewMapper;
import com.skytala.eCommerce.event.TarpittedLoginViewAdded;
import com.skytala.eCommerce.event.TarpittedLoginViewDeleted;
import com.skytala.eCommerce.event.TarpittedLoginViewFound;
import com.skytala.eCommerce.event.TarpittedLoginViewUpdated;
import com.skytala.eCommerce.query.FindTarpittedLoginViewsBy;

@RestController
@RequestMapping("/api/tarpittedLoginView")
public class TarpittedLoginViewController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TarpittedLoginView>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TarpittedLoginViewController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TarpittedLoginView
	 * @return a List with the TarpittedLoginViews
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TarpittedLoginView> findTarpittedLoginViewsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTarpittedLoginViewsBy query = new FindTarpittedLoginViewsBy(allRequestParams);

		int usedTicketId;

		synchronized (TarpittedLoginViewController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TarpittedLoginViewFound.class,
				event -> sendTarpittedLoginViewsFoundMessage(((TarpittedLoginViewFound) event).getTarpittedLoginViews(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTarpittedLoginViewsFoundMessage(List<TarpittedLoginView> tarpittedLoginViews, int usedTicketId) {
		queryReturnVal.put(usedTicketId, tarpittedLoginViews);
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
	public boolean createTarpittedLoginView(HttpServletRequest request) {

		TarpittedLoginView tarpittedLoginViewToBeAdded = new TarpittedLoginView();
		try {
			tarpittedLoginViewToBeAdded = TarpittedLoginViewMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTarpittedLoginView(tarpittedLoginViewToBeAdded);

	}

	/**
	 * creates a new TarpittedLoginView entry in the ofbiz database
	 * 
	 * @param tarpittedLoginViewToBeAdded
	 *            the TarpittedLoginView thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTarpittedLoginView(TarpittedLoginView tarpittedLoginViewToBeAdded) {

		AddTarpittedLoginView com = new AddTarpittedLoginView(tarpittedLoginViewToBeAdded);
		int usedTicketId;

		synchronized (TarpittedLoginViewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TarpittedLoginViewAdded.class,
				event -> sendTarpittedLoginViewChangedMessage(((TarpittedLoginViewAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTarpittedLoginView(HttpServletRequest request) {

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

		TarpittedLoginView tarpittedLoginViewToBeUpdated = new TarpittedLoginView();

		try {
			tarpittedLoginViewToBeUpdated = TarpittedLoginViewMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTarpittedLoginView(tarpittedLoginViewToBeUpdated);

	}

	/**
	 * Updates the TarpittedLoginView with the specific Id
	 * 
	 * @param tarpittedLoginViewToBeUpdated the TarpittedLoginView thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTarpittedLoginView(TarpittedLoginView tarpittedLoginViewToBeUpdated) {

		UpdateTarpittedLoginView com = new UpdateTarpittedLoginView(tarpittedLoginViewToBeUpdated);

		int usedTicketId;

		synchronized (TarpittedLoginViewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TarpittedLoginViewUpdated.class,
				event -> sendTarpittedLoginViewChangedMessage(((TarpittedLoginViewUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TarpittedLoginView from the database
	 * 
	 * @param tarpittedLoginViewId:
	 *            the id of the TarpittedLoginView thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetarpittedLoginViewById(@RequestParam(value = "tarpittedLoginViewId") String tarpittedLoginViewId) {

		DeleteTarpittedLoginView com = new DeleteTarpittedLoginView(tarpittedLoginViewId);

		int usedTicketId;

		synchronized (TarpittedLoginViewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TarpittedLoginViewDeleted.class,
				event -> sendTarpittedLoginViewChangedMessage(((TarpittedLoginViewDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTarpittedLoginViewChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/tarpittedLoginView/\" plus one of the following: "
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
