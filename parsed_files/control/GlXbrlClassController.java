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
import com.skytala.eCommerce.command.AddGlXbrlClass;
import com.skytala.eCommerce.command.DeleteGlXbrlClass;
import com.skytala.eCommerce.command.UpdateGlXbrlClass;
import com.skytala.eCommerce.entity.GlXbrlClass;
import com.skytala.eCommerce.entity.GlXbrlClassMapper;
import com.skytala.eCommerce.event.GlXbrlClassAdded;
import com.skytala.eCommerce.event.GlXbrlClassDeleted;
import com.skytala.eCommerce.event.GlXbrlClassFound;
import com.skytala.eCommerce.event.GlXbrlClassUpdated;
import com.skytala.eCommerce.query.FindGlXbrlClasssBy;

@RestController
@RequestMapping("/api/glXbrlClass")
public class GlXbrlClassController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlXbrlClass>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlXbrlClassController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlXbrlClass
	 * @return a List with the GlXbrlClasss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlXbrlClass> findGlXbrlClasssBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlXbrlClasssBy query = new FindGlXbrlClasssBy(allRequestParams);

		int usedTicketId;

		synchronized (GlXbrlClassController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlXbrlClassFound.class,
				event -> sendGlXbrlClasssFoundMessage(((GlXbrlClassFound) event).getGlXbrlClasss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlXbrlClasssFoundMessage(List<GlXbrlClass> glXbrlClasss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glXbrlClasss);
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
	public boolean createGlXbrlClass(HttpServletRequest request) {

		GlXbrlClass glXbrlClassToBeAdded = new GlXbrlClass();
		try {
			glXbrlClassToBeAdded = GlXbrlClassMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlXbrlClass(glXbrlClassToBeAdded);

	}

	/**
	 * creates a new GlXbrlClass entry in the ofbiz database
	 * 
	 * @param glXbrlClassToBeAdded
	 *            the GlXbrlClass thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlXbrlClass(GlXbrlClass glXbrlClassToBeAdded) {

		AddGlXbrlClass com = new AddGlXbrlClass(glXbrlClassToBeAdded);
		int usedTicketId;

		synchronized (GlXbrlClassController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlXbrlClassAdded.class,
				event -> sendGlXbrlClassChangedMessage(((GlXbrlClassAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlXbrlClass(HttpServletRequest request) {

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

		GlXbrlClass glXbrlClassToBeUpdated = new GlXbrlClass();

		try {
			glXbrlClassToBeUpdated = GlXbrlClassMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlXbrlClass(glXbrlClassToBeUpdated);

	}

	/**
	 * Updates the GlXbrlClass with the specific Id
	 * 
	 * @param glXbrlClassToBeUpdated the GlXbrlClass thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlXbrlClass(GlXbrlClass glXbrlClassToBeUpdated) {

		UpdateGlXbrlClass com = new UpdateGlXbrlClass(glXbrlClassToBeUpdated);

		int usedTicketId;

		synchronized (GlXbrlClassController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlXbrlClassUpdated.class,
				event -> sendGlXbrlClassChangedMessage(((GlXbrlClassUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlXbrlClass from the database
	 * 
	 * @param glXbrlClassId:
	 *            the id of the GlXbrlClass thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglXbrlClassById(@RequestParam(value = "glXbrlClassId") String glXbrlClassId) {

		DeleteGlXbrlClass com = new DeleteGlXbrlClass(glXbrlClassId);

		int usedTicketId;

		synchronized (GlXbrlClassController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlXbrlClassDeleted.class,
				event -> sendGlXbrlClassChangedMessage(((GlXbrlClassDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlXbrlClassChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glXbrlClass/\" plus one of the following: "
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
