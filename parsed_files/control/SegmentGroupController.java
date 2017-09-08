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
import com.skytala.eCommerce.command.AddSegmentGroup;
import com.skytala.eCommerce.command.DeleteSegmentGroup;
import com.skytala.eCommerce.command.UpdateSegmentGroup;
import com.skytala.eCommerce.entity.SegmentGroup;
import com.skytala.eCommerce.entity.SegmentGroupMapper;
import com.skytala.eCommerce.event.SegmentGroupAdded;
import com.skytala.eCommerce.event.SegmentGroupDeleted;
import com.skytala.eCommerce.event.SegmentGroupFound;
import com.skytala.eCommerce.event.SegmentGroupUpdated;
import com.skytala.eCommerce.query.FindSegmentGroupsBy;

@RestController
@RequestMapping("/api/segmentGroup")
public class SegmentGroupController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SegmentGroup>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SegmentGroupController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SegmentGroup
	 * @return a List with the SegmentGroups
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SegmentGroup> findSegmentGroupsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSegmentGroupsBy query = new FindSegmentGroupsBy(allRequestParams);

		int usedTicketId;

		synchronized (SegmentGroupController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupFound.class,
				event -> sendSegmentGroupsFoundMessage(((SegmentGroupFound) event).getSegmentGroups(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSegmentGroupsFoundMessage(List<SegmentGroup> segmentGroups, int usedTicketId) {
		queryReturnVal.put(usedTicketId, segmentGroups);
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
	public boolean createSegmentGroup(HttpServletRequest request) {

		SegmentGroup segmentGroupToBeAdded = new SegmentGroup();
		try {
			segmentGroupToBeAdded = SegmentGroupMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSegmentGroup(segmentGroupToBeAdded);

	}

	/**
	 * creates a new SegmentGroup entry in the ofbiz database
	 * 
	 * @param segmentGroupToBeAdded
	 *            the SegmentGroup thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSegmentGroup(SegmentGroup segmentGroupToBeAdded) {

		AddSegmentGroup com = new AddSegmentGroup(segmentGroupToBeAdded);
		int usedTicketId;

		synchronized (SegmentGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupAdded.class,
				event -> sendSegmentGroupChangedMessage(((SegmentGroupAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSegmentGroup(HttpServletRequest request) {

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

		SegmentGroup segmentGroupToBeUpdated = new SegmentGroup();

		try {
			segmentGroupToBeUpdated = SegmentGroupMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSegmentGroup(segmentGroupToBeUpdated);

	}

	/**
	 * Updates the SegmentGroup with the specific Id
	 * 
	 * @param segmentGroupToBeUpdated the SegmentGroup thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSegmentGroup(SegmentGroup segmentGroupToBeUpdated) {

		UpdateSegmentGroup com = new UpdateSegmentGroup(segmentGroupToBeUpdated);

		int usedTicketId;

		synchronized (SegmentGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupUpdated.class,
				event -> sendSegmentGroupChangedMessage(((SegmentGroupUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SegmentGroup from the database
	 * 
	 * @param segmentGroupId:
	 *            the id of the SegmentGroup thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesegmentGroupById(@RequestParam(value = "segmentGroupId") String segmentGroupId) {

		DeleteSegmentGroup com = new DeleteSegmentGroup(segmentGroupId);

		int usedTicketId;

		synchronized (SegmentGroupController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SegmentGroupDeleted.class,
				event -> sendSegmentGroupChangedMessage(((SegmentGroupDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSegmentGroupChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/segmentGroup/\" plus one of the following: "
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
