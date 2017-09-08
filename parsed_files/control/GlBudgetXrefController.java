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
import com.skytala.eCommerce.command.AddGlBudgetXref;
import com.skytala.eCommerce.command.DeleteGlBudgetXref;
import com.skytala.eCommerce.command.UpdateGlBudgetXref;
import com.skytala.eCommerce.entity.GlBudgetXref;
import com.skytala.eCommerce.entity.GlBudgetXrefMapper;
import com.skytala.eCommerce.event.GlBudgetXrefAdded;
import com.skytala.eCommerce.event.GlBudgetXrefDeleted;
import com.skytala.eCommerce.event.GlBudgetXrefFound;
import com.skytala.eCommerce.event.GlBudgetXrefUpdated;
import com.skytala.eCommerce.query.FindGlBudgetXrefsBy;

@RestController
@RequestMapping("/api/glBudgetXref")
public class GlBudgetXrefController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlBudgetXref>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlBudgetXrefController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlBudgetXref
	 * @return a List with the GlBudgetXrefs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlBudgetXref> findGlBudgetXrefsBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlBudgetXrefsBy query = new FindGlBudgetXrefsBy(allRequestParams);

		int usedTicketId;

		synchronized (GlBudgetXrefController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlBudgetXrefFound.class,
				event -> sendGlBudgetXrefsFoundMessage(((GlBudgetXrefFound) event).getGlBudgetXrefs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlBudgetXrefsFoundMessage(List<GlBudgetXref> glBudgetXrefs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glBudgetXrefs);
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
	public boolean createGlBudgetXref(HttpServletRequest request) {

		GlBudgetXref glBudgetXrefToBeAdded = new GlBudgetXref();
		try {
			glBudgetXrefToBeAdded = GlBudgetXrefMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlBudgetXref(glBudgetXrefToBeAdded);

	}

	/**
	 * creates a new GlBudgetXref entry in the ofbiz database
	 * 
	 * @param glBudgetXrefToBeAdded
	 *            the GlBudgetXref thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlBudgetXref(GlBudgetXref glBudgetXrefToBeAdded) {

		AddGlBudgetXref com = new AddGlBudgetXref(glBudgetXrefToBeAdded);
		int usedTicketId;

		synchronized (GlBudgetXrefController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlBudgetXrefAdded.class,
				event -> sendGlBudgetXrefChangedMessage(((GlBudgetXrefAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlBudgetXref(HttpServletRequest request) {

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

		GlBudgetXref glBudgetXrefToBeUpdated = new GlBudgetXref();

		try {
			glBudgetXrefToBeUpdated = GlBudgetXrefMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlBudgetXref(glBudgetXrefToBeUpdated);

	}

	/**
	 * Updates the GlBudgetXref with the specific Id
	 * 
	 * @param glBudgetXrefToBeUpdated the GlBudgetXref thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlBudgetXref(GlBudgetXref glBudgetXrefToBeUpdated) {

		UpdateGlBudgetXref com = new UpdateGlBudgetXref(glBudgetXrefToBeUpdated);

		int usedTicketId;

		synchronized (GlBudgetXrefController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlBudgetXrefUpdated.class,
				event -> sendGlBudgetXrefChangedMessage(((GlBudgetXrefUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlBudgetXref from the database
	 * 
	 * @param glBudgetXrefId:
	 *            the id of the GlBudgetXref thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglBudgetXrefById(@RequestParam(value = "glBudgetXrefId") String glBudgetXrefId) {

		DeleteGlBudgetXref com = new DeleteGlBudgetXref(glBudgetXrefId);

		int usedTicketId;

		synchronized (GlBudgetXrefController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlBudgetXrefDeleted.class,
				event -> sendGlBudgetXrefChangedMessage(((GlBudgetXrefDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlBudgetXrefChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glBudgetXref/\" plus one of the following: "
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
