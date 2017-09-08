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
import com.skytala.eCommerce.command.AddGlAccountTypeDefault;
import com.skytala.eCommerce.command.DeleteGlAccountTypeDefault;
import com.skytala.eCommerce.command.UpdateGlAccountTypeDefault;
import com.skytala.eCommerce.entity.GlAccountTypeDefault;
import com.skytala.eCommerce.entity.GlAccountTypeDefaultMapper;
import com.skytala.eCommerce.event.GlAccountTypeDefaultAdded;
import com.skytala.eCommerce.event.GlAccountTypeDefaultDeleted;
import com.skytala.eCommerce.event.GlAccountTypeDefaultFound;
import com.skytala.eCommerce.event.GlAccountTypeDefaultUpdated;
import com.skytala.eCommerce.query.FindGlAccountTypeDefaultsBy;

@RestController
@RequestMapping("/api/glAccountTypeDefault")
public class GlAccountTypeDefaultController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccountTypeDefault>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountTypeDefaultController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccountTypeDefault
	 * @return a List with the GlAccountTypeDefaults
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccountTypeDefault> findGlAccountTypeDefaultsBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountTypeDefaultsBy query = new FindGlAccountTypeDefaultsBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountTypeDefaultController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountTypeDefaultFound.class,
				event -> sendGlAccountTypeDefaultsFoundMessage(((GlAccountTypeDefaultFound) event).getGlAccountTypeDefaults(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountTypeDefaultsFoundMessage(List<GlAccountTypeDefault> glAccountTypeDefaults, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccountTypeDefaults);
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
	public boolean createGlAccountTypeDefault(HttpServletRequest request) {

		GlAccountTypeDefault glAccountTypeDefaultToBeAdded = new GlAccountTypeDefault();
		try {
			glAccountTypeDefaultToBeAdded = GlAccountTypeDefaultMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccountTypeDefault(glAccountTypeDefaultToBeAdded);

	}

	/**
	 * creates a new GlAccountTypeDefault entry in the ofbiz database
	 * 
	 * @param glAccountTypeDefaultToBeAdded
	 *            the GlAccountTypeDefault thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccountTypeDefault(GlAccountTypeDefault glAccountTypeDefaultToBeAdded) {

		AddGlAccountTypeDefault com = new AddGlAccountTypeDefault(glAccountTypeDefaultToBeAdded);
		int usedTicketId;

		synchronized (GlAccountTypeDefaultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountTypeDefaultAdded.class,
				event -> sendGlAccountTypeDefaultChangedMessage(((GlAccountTypeDefaultAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccountTypeDefault(HttpServletRequest request) {

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

		GlAccountTypeDefault glAccountTypeDefaultToBeUpdated = new GlAccountTypeDefault();

		try {
			glAccountTypeDefaultToBeUpdated = GlAccountTypeDefaultMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccountTypeDefault(glAccountTypeDefaultToBeUpdated);

	}

	/**
	 * Updates the GlAccountTypeDefault with the specific Id
	 * 
	 * @param glAccountTypeDefaultToBeUpdated the GlAccountTypeDefault thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccountTypeDefault(GlAccountTypeDefault glAccountTypeDefaultToBeUpdated) {

		UpdateGlAccountTypeDefault com = new UpdateGlAccountTypeDefault(glAccountTypeDefaultToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountTypeDefaultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountTypeDefaultUpdated.class,
				event -> sendGlAccountTypeDefaultChangedMessage(((GlAccountTypeDefaultUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccountTypeDefault from the database
	 * 
	 * @param glAccountTypeDefaultId:
	 *            the id of the GlAccountTypeDefault thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountTypeDefaultById(@RequestParam(value = "glAccountTypeDefaultId") String glAccountTypeDefaultId) {

		DeleteGlAccountTypeDefault com = new DeleteGlAccountTypeDefault(glAccountTypeDefaultId);

		int usedTicketId;

		synchronized (GlAccountTypeDefaultController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountTypeDefaultDeleted.class,
				event -> sendGlAccountTypeDefaultChangedMessage(((GlAccountTypeDefaultDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountTypeDefaultChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccountTypeDefault/\" plus one of the following: "
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
