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
import com.skytala.eCommerce.command.AddSegmentGroupType;
import com.skytala.eCommerce.command.DeleteSegmentGroupType;
import com.skytala.eCommerce.command.UpdateSegmentGroupType;
import com.skytala.eCommerce.entity.SegmentGroupType;
import com.skytala.eCommerce.entity.SegmentGroupTypeMapper;
import com.skytala.eCommerce.event.SegmentGroupTypeAdded;
import com.skytala.eCommerce.event.SegmentGroupTypeDeleted;
import com.skytala.eCommerce.event.SegmentGroupTypeFound;
import com.skytala.eCommerce.event.SegmentGroupTypeUpdated;
import com.skytala.eCommerce.query.FindSegmentGroupTypesBy;

@RestController
@RequestMapping("/api/segmentGroupType")
public class SegmentGroupTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SegmentGroupType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SegmentGroupTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SegmentGroupType
	 * @return a List with the SegmentGroupTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SegmentGroupType> findSegmentGroupTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSegmentGroupTypesBy query = new FindSegmentGroupTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (SegmentGroupTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupTypeFound.class,
				event -> sendSegmentGroupTypesFoundMessage(((SegmentGroupTypeFound) event).getSegmentGroupTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSegmentGroupTypesFoundMessage(List<SegmentGroupType> segmentGroupTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, segmentGroupTypes);
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
	public boolean createSegmentGroupType(HttpServletRequest request) {

		SegmentGroupType segmentGroupTypeToBeAdded = new SegmentGroupType();
		try {
			segmentGroupTypeToBeAdded = SegmentGroupTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSegmentGroupType(segmentGroupTypeToBeAdded);

	}

	/**
	 * creates a new SegmentGroupType entry in the ofbiz database
	 * 
	 * @param segmentGroupTypeToBeAdded
	 *            the SegmentGroupType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSegmentGroupType(SegmentGroupType segmentGroupTypeToBeAdded) {

		AddSegmentGroupType com = new AddSegmentGroupType(segmentGroupTypeToBeAdded);
		int usedTicketId;

		synchronized (SegmentGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupTypeAdded.class,
				event -> sendSegmentGroupTypeChangedMessage(((SegmentGroupTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSegmentGroupType(HttpServletRequest request) {

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

		SegmentGroupType segmentGroupTypeToBeUpdated = new SegmentGroupType();

		try {
			segmentGroupTypeToBeUpdated = SegmentGroupTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSegmentGroupType(segmentGroupTypeToBeUpdated);

	}

	/**
	 * Updates the SegmentGroupType with the specific Id
	 * 
	 * @param segmentGroupTypeToBeUpdated the SegmentGroupType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSegmentGroupType(SegmentGroupType segmentGroupTypeToBeUpdated) {

		UpdateSegmentGroupType com = new UpdateSegmentGroupType(segmentGroupTypeToBeUpdated);

		int usedTicketId;

		synchronized (SegmentGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupTypeUpdated.class,
				event -> sendSegmentGroupTypeChangedMessage(((SegmentGroupTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SegmentGroupType from the database
	 * 
	 * @param segmentGroupTypeId:
	 *            the id of the SegmentGroupType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesegmentGroupTypeById(@RequestParam(value = "segmentGroupTypeId") String segmentGroupTypeId) {

		DeleteSegmentGroupType com = new DeleteSegmentGroupType(segmentGroupTypeId);

		int usedTicketId;

		synchronized (SegmentGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupTypeDeleted.class,
				event -> sendSegmentGroupTypeChangedMessage(((SegmentGroupTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSegmentGroupTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/segmentGroupType/\" plus one of the following: "
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
