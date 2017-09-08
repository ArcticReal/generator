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
import com.skytala.eCommerce.command.AddPerfReviewItem;
import com.skytala.eCommerce.command.DeletePerfReviewItem;
import com.skytala.eCommerce.command.UpdatePerfReviewItem;
import com.skytala.eCommerce.entity.PerfReviewItem;
import com.skytala.eCommerce.entity.PerfReviewItemMapper;
import com.skytala.eCommerce.event.PerfReviewItemAdded;
import com.skytala.eCommerce.event.PerfReviewItemDeleted;
import com.skytala.eCommerce.event.PerfReviewItemFound;
import com.skytala.eCommerce.event.PerfReviewItemUpdated;
import com.skytala.eCommerce.query.FindPerfReviewItemsBy;

@RestController
@RequestMapping("/api/perfReviewItem")
public class PerfReviewItemController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<PerfReviewItem>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public PerfReviewItemController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a PerfReviewItem
	 * @return a List with the PerfReviewItems
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<PerfReviewItem> findPerfReviewItemsBy(@RequestParam Map<String, String> allRequestParams) {

		FindPerfReviewItemsBy query = new FindPerfReviewItemsBy(allRequestParams);

		int usedTicketId;

		synchronized (PerfReviewItemController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewItemFound.class,
				event -> sendPerfReviewItemsFoundMessage(((PerfReviewItemFound) event).getPerfReviewItems(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendPerfReviewItemsFoundMessage(List<PerfReviewItem> perfReviewItems, int usedTicketId) {
		queryReturnVal.put(usedTicketId, perfReviewItems);
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
	public boolean createPerfReviewItem(HttpServletRequest request) {

		PerfReviewItem perfReviewItemToBeAdded = new PerfReviewItem();
		try {
			perfReviewItemToBeAdded = PerfReviewItemMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createPerfReviewItem(perfReviewItemToBeAdded);

	}

	/**
	 * creates a new PerfReviewItem entry in the ofbiz database
	 * 
	 * @param perfReviewItemToBeAdded
	 *            the PerfReviewItem thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createPerfReviewItem(PerfReviewItem perfReviewItemToBeAdded) {

		AddPerfReviewItem com = new AddPerfReviewItem(perfReviewItemToBeAdded);
		int usedTicketId;

		synchronized (PerfReviewItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewItemAdded.class,
				event -> sendPerfReviewItemChangedMessage(((PerfReviewItemAdded) event).isSuccess(), usedTicketId));

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
	public boolean updatePerfReviewItem(HttpServletRequest request) {

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

		PerfReviewItem perfReviewItemToBeUpdated = new PerfReviewItem();

		try {
			perfReviewItemToBeUpdated = PerfReviewItemMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updatePerfReviewItem(perfReviewItemToBeUpdated);

	}

	/**
	 * Updates the PerfReviewItem with the specific Id
	 * 
	 * @param perfReviewItemToBeUpdated the PerfReviewItem thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updatePerfReviewItem(PerfReviewItem perfReviewItemToBeUpdated) {

		UpdatePerfReviewItem com = new UpdatePerfReviewItem(perfReviewItemToBeUpdated);

		int usedTicketId;

		synchronized (PerfReviewItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewItemUpdated.class,
				event -> sendPerfReviewItemChangedMessage(((PerfReviewItemUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a PerfReviewItem from the database
	 * 
	 * @param perfReviewItemId:
	 *            the id of the PerfReviewItem thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteperfReviewItemById(@RequestParam(value = "perfReviewItemId") String perfReviewItemId) {

		DeletePerfReviewItem com = new DeletePerfReviewItem(perfReviewItemId);

		int usedTicketId;

		synchronized (PerfReviewItemController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(PerfReviewItemDeleted.class,
				event -> sendPerfReviewItemChangedMessage(((PerfReviewItemDeleted) event).isSuccess(), usedTicketId));

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

	public void sendPerfReviewItemChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/perfReviewItem/\" plus one of the following: "
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
