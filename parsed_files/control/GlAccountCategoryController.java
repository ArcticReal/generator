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
import com.skytala.eCommerce.command.AddGlAccountCategory;
import com.skytala.eCommerce.command.DeleteGlAccountCategory;
import com.skytala.eCommerce.command.UpdateGlAccountCategory;
import com.skytala.eCommerce.entity.GlAccountCategory;
import com.skytala.eCommerce.entity.GlAccountCategoryMapper;
import com.skytala.eCommerce.event.GlAccountCategoryAdded;
import com.skytala.eCommerce.event.GlAccountCategoryDeleted;
import com.skytala.eCommerce.event.GlAccountCategoryFound;
import com.skytala.eCommerce.event.GlAccountCategoryUpdated;
import com.skytala.eCommerce.query.FindGlAccountCategorysBy;

@RestController
@RequestMapping("/api/glAccountCategory")
public class GlAccountCategoryController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccountCategory>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountCategoryController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccountCategory
	 * @return a List with the GlAccountCategorys
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccountCategory> findGlAccountCategorysBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountCategorysBy query = new FindGlAccountCategorysBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountCategoryController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountCategoryFound.class,
				event -> sendGlAccountCategorysFoundMessage(((GlAccountCategoryFound) event).getGlAccountCategorys(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountCategorysFoundMessage(List<GlAccountCategory> glAccountCategorys, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccountCategorys);
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
	public boolean createGlAccountCategory(HttpServletRequest request) {

		GlAccountCategory glAccountCategoryToBeAdded = new GlAccountCategory();
		try {
			glAccountCategoryToBeAdded = GlAccountCategoryMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccountCategory(glAccountCategoryToBeAdded);

	}

	/**
	 * creates a new GlAccountCategory entry in the ofbiz database
	 * 
	 * @param glAccountCategoryToBeAdded
	 *            the GlAccountCategory thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccountCategory(GlAccountCategory glAccountCategoryToBeAdded) {

		AddGlAccountCategory com = new AddGlAccountCategory(glAccountCategoryToBeAdded);
		int usedTicketId;

		synchronized (GlAccountCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountCategoryAdded.class,
				event -> sendGlAccountCategoryChangedMessage(((GlAccountCategoryAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccountCategory(HttpServletRequest request) {

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

		GlAccountCategory glAccountCategoryToBeUpdated = new GlAccountCategory();

		try {
			glAccountCategoryToBeUpdated = GlAccountCategoryMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccountCategory(glAccountCategoryToBeUpdated);

	}

	/**
	 * Updates the GlAccountCategory with the specific Id
	 * 
	 * @param glAccountCategoryToBeUpdated the GlAccountCategory thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccountCategory(GlAccountCategory glAccountCategoryToBeUpdated) {

		UpdateGlAccountCategory com = new UpdateGlAccountCategory(glAccountCategoryToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountCategoryUpdated.class,
				event -> sendGlAccountCategoryChangedMessage(((GlAccountCategoryUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccountCategory from the database
	 * 
	 * @param glAccountCategoryId:
	 *            the id of the GlAccountCategory thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountCategoryById(@RequestParam(value = "glAccountCategoryId") String glAccountCategoryId) {

		DeleteGlAccountCategory com = new DeleteGlAccountCategory(glAccountCategoryId);

		int usedTicketId;

		synchronized (GlAccountCategoryController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountCategoryDeleted.class,
				event -> sendGlAccountCategoryChangedMessage(((GlAccountCategoryDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountCategoryChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccountCategory/\" plus one of the following: "
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
