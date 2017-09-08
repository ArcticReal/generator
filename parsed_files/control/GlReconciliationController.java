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
import com.skytala.eCommerce.command.AddGlReconciliation;
import com.skytala.eCommerce.command.DeleteGlReconciliation;
import com.skytala.eCommerce.command.UpdateGlReconciliation;
import com.skytala.eCommerce.entity.GlReconciliation;
import com.skytala.eCommerce.entity.GlReconciliationMapper;
import com.skytala.eCommerce.event.GlReconciliationAdded;
import com.skytala.eCommerce.event.GlReconciliationDeleted;
import com.skytala.eCommerce.event.GlReconciliationFound;
import com.skytala.eCommerce.event.GlReconciliationUpdated;
import com.skytala.eCommerce.query.FindGlReconciliationsBy;

@RestController
@RequestMapping("/api/glReconciliation")
public class GlReconciliationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlReconciliation>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlReconciliationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlReconciliation
	 * @return a List with the GlReconciliations
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlReconciliation> findGlReconciliationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlReconciliationsBy query = new FindGlReconciliationsBy(allRequestParams);

		int usedTicketId;

		synchronized (GlReconciliationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlReconciliationFound.class,
				event -> sendGlReconciliationsFoundMessage(((GlReconciliationFound) event).getGlReconciliations(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlReconciliationsFoundMessage(List<GlReconciliation> glReconciliations, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glReconciliations);
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
	public boolean createGlReconciliation(HttpServletRequest request) {

		GlReconciliation glReconciliationToBeAdded = new GlReconciliation();
		try {
			glReconciliationToBeAdded = GlReconciliationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlReconciliation(glReconciliationToBeAdded);

	}

	/**
	 * creates a new GlReconciliation entry in the ofbiz database
	 * 
	 * @param glReconciliationToBeAdded
	 *            the GlReconciliation thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlReconciliation(GlReconciliation glReconciliationToBeAdded) {

		AddGlReconciliation com = new AddGlReconciliation(glReconciliationToBeAdded);
		int usedTicketId;

		synchronized (GlReconciliationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlReconciliationAdded.class,
				event -> sendGlReconciliationChangedMessage(((GlReconciliationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlReconciliation(HttpServletRequest request) {

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

		GlReconciliation glReconciliationToBeUpdated = new GlReconciliation();

		try {
			glReconciliationToBeUpdated = GlReconciliationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlReconciliation(glReconciliationToBeUpdated);

	}

	/**
	 * Updates the GlReconciliation with the specific Id
	 * 
	 * @param glReconciliationToBeUpdated the GlReconciliation thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlReconciliation(GlReconciliation glReconciliationToBeUpdated) {

		UpdateGlReconciliation com = new UpdateGlReconciliation(glReconciliationToBeUpdated);

		int usedTicketId;

		synchronized (GlReconciliationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlReconciliationUpdated.class,
				event -> sendGlReconciliationChangedMessage(((GlReconciliationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlReconciliation from the database
	 * 
	 * @param glReconciliationId:
	 *            the id of the GlReconciliation thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglReconciliationById(@RequestParam(value = "glReconciliationId") String glReconciliationId) {

		DeleteGlReconciliation com = new DeleteGlReconciliation(glReconciliationId);

		int usedTicketId;

		synchronized (GlReconciliationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlReconciliationDeleted.class,
				event -> sendGlReconciliationChangedMessage(((GlReconciliationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlReconciliationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glReconciliation/\" plus one of the following: "
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
