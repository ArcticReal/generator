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
import com.skytala.eCommerce.command.AddSegmentGroupClassification;
import com.skytala.eCommerce.command.DeleteSegmentGroupClassification;
import com.skytala.eCommerce.command.UpdateSegmentGroupClassification;
import com.skytala.eCommerce.entity.SegmentGroupClassification;
import com.skytala.eCommerce.entity.SegmentGroupClassificationMapper;
import com.skytala.eCommerce.event.SegmentGroupClassificationAdded;
import com.skytala.eCommerce.event.SegmentGroupClassificationDeleted;
import com.skytala.eCommerce.event.SegmentGroupClassificationFound;
import com.skytala.eCommerce.event.SegmentGroupClassificationUpdated;
import com.skytala.eCommerce.query.FindSegmentGroupClassificationsBy;

@RestController
@RequestMapping("/api/segmentGroupClassification")
public class SegmentGroupClassificationController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SegmentGroupClassification>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SegmentGroupClassificationController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SegmentGroupClassification
	 * @return a List with the SegmentGroupClassifications
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SegmentGroupClassification> findSegmentGroupClassificationsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSegmentGroupClassificationsBy query = new FindSegmentGroupClassificationsBy(allRequestParams);

		int usedTicketId;

		synchronized (SegmentGroupClassificationController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupClassificationFound.class,
				event -> sendSegmentGroupClassificationsFoundMessage(((SegmentGroupClassificationFound) event).getSegmentGroupClassifications(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSegmentGroupClassificationsFoundMessage(List<SegmentGroupClassification> segmentGroupClassifications, int usedTicketId) {
		queryReturnVal.put(usedTicketId, segmentGroupClassifications);
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
	public boolean createSegmentGroupClassification(HttpServletRequest request) {

		SegmentGroupClassification segmentGroupClassificationToBeAdded = new SegmentGroupClassification();
		try {
			segmentGroupClassificationToBeAdded = SegmentGroupClassificationMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSegmentGroupClassification(segmentGroupClassificationToBeAdded);

	}

	/**
	 * creates a new SegmentGroupClassification entry in the ofbiz database
	 * 
	 * @param segmentGroupClassificationToBeAdded
	 *            the SegmentGroupClassification thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSegmentGroupClassification(SegmentGroupClassification segmentGroupClassificationToBeAdded) {

		AddSegmentGroupClassification com = new AddSegmentGroupClassification(segmentGroupClassificationToBeAdded);
		int usedTicketId;

		synchronized (SegmentGroupClassificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupClassificationAdded.class,
				event -> sendSegmentGroupClassificationChangedMessage(((SegmentGroupClassificationAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSegmentGroupClassification(HttpServletRequest request) {

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

		SegmentGroupClassification segmentGroupClassificationToBeUpdated = new SegmentGroupClassification();

		try {
			segmentGroupClassificationToBeUpdated = SegmentGroupClassificationMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSegmentGroupClassification(segmentGroupClassificationToBeUpdated);

	}

	/**
	 * Updates the SegmentGroupClassification with the specific Id
	 * 
	 * @param segmentGroupClassificationToBeUpdated the SegmentGroupClassification thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSegmentGroupClassification(SegmentGroupClassification segmentGroupClassificationToBeUpdated) {

		UpdateSegmentGroupClassification com = new UpdateSegmentGroupClassification(segmentGroupClassificationToBeUpdated);

		int usedTicketId;

		synchronized (SegmentGroupClassificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupClassificationUpdated.class,
				event -> sendSegmentGroupClassificationChangedMessage(((SegmentGroupClassificationUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SegmentGroupClassification from the database
	 * 
	 * @param segmentGroupClassificationId:
	 *            the id of the SegmentGroupClassification thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesegmentGroupClassificationById(@RequestParam(value = "segmentGroupClassificationId") String segmentGroupClassificationId) {

		DeleteSegmentGroupClassification com = new DeleteSegmentGroupClassification(segmentGroupClassificationId);

		int usedTicketId;

		synchronized (SegmentGroupClassificationController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupClassificationDeleted.class,
				event -> sendSegmentGroupClassificationChangedMessage(((SegmentGroupClassificationDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSegmentGroupClassificationChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/segmentGroupClassification/\" plus one of the following: "
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
