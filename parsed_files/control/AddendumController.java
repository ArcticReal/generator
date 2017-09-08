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
import com.skytala.eCommerce.command.AddAddendum;
import com.skytala.eCommerce.command.DeleteAddendum;
import com.skytala.eCommerce.command.UpdateAddendum;
import com.skytala.eCommerce.entity.Addendum;
import com.skytala.eCommerce.entity.AddendumMapper;
import com.skytala.eCommerce.event.AddendumAdded;
import com.skytala.eCommerce.event.AddendumDeleted;
import com.skytala.eCommerce.event.AddendumFound;
import com.skytala.eCommerce.event.AddendumUpdated;
import com.skytala.eCommerce.query.FindAddendumsBy;

@RestController
@RequestMapping("/api/addendum")
public class AddendumController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Addendum>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AddendumController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Addendum
	 * @return a List with the Addendums
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Addendum> findAddendumsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAddendumsBy query = new FindAddendumsBy(allRequestParams);

		int usedTicketId;

		synchronized (AddendumController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AddendumFound.class,
				event -> sendAddendumsFoundMessage(((AddendumFound) event).getAddendums(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAddendumsFoundMessage(List<Addendum> addendums, int usedTicketId) {
		queryReturnVal.put(usedTicketId, addendums);
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
	public boolean createAddendum(HttpServletRequest request) {

		Addendum addendumToBeAdded = new Addendum();
		try {
			addendumToBeAdded = AddendumMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAddendum(addendumToBeAdded);

	}

	/**
	 * creates a new Addendum entry in the ofbiz database
	 * 
	 * @param addendumToBeAdded
	 *            the Addendum thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAddendum(Addendum addendumToBeAdded) {

		AddAddendum com = new AddAddendum(addendumToBeAdded);
		int usedTicketId;

		synchronized (AddendumController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AddendumAdded.class,
				event -> sendAddendumChangedMessage(((AddendumAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAddendum(HttpServletRequest request) {

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

		Addendum addendumToBeUpdated = new Addendum();

		try {
			addendumToBeUpdated = AddendumMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAddendum(addendumToBeUpdated);

	}

	/**
	 * Updates the Addendum with the specific Id
	 * 
	 * @param addendumToBeUpdated the Addendum thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAddendum(Addendum addendumToBeUpdated) {

		UpdateAddendum com = new UpdateAddendum(addendumToBeUpdated);

		int usedTicketId;

		synchronized (AddendumController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AddendumUpdated.class,
				event -> sendAddendumChangedMessage(((AddendumUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Addendum from the database
	 * 
	 * @param addendumId:
	 *            the id of the Addendum thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteaddendumById(@RequestParam(value = "addendumId") String addendumId) {

		DeleteAddendum com = new DeleteAddendum(addendumId);

		int usedTicketId;

		synchronized (AddendumController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AddendumDeleted.class,
				event -> sendAddendumChangedMessage(((AddendumDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAddendumChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/addendum/\" plus one of the following: "
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
