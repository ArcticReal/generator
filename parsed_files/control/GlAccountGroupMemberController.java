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
import com.skytala.eCommerce.command.AddGlAccountGroupMember;
import com.skytala.eCommerce.command.DeleteGlAccountGroupMember;
import com.skytala.eCommerce.command.UpdateGlAccountGroupMember;
import com.skytala.eCommerce.entity.GlAccountGroupMember;
import com.skytala.eCommerce.entity.GlAccountGroupMemberMapper;
import com.skytala.eCommerce.event.GlAccountGroupMemberAdded;
import com.skytala.eCommerce.event.GlAccountGroupMemberDeleted;
import com.skytala.eCommerce.event.GlAccountGroupMemberFound;
import com.skytala.eCommerce.event.GlAccountGroupMemberUpdated;
import com.skytala.eCommerce.query.FindGlAccountGroupMembersBy;

@RestController
@RequestMapping("/api/glAccountGroupMember")
public class GlAccountGroupMemberController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<GlAccountGroupMember>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public GlAccountGroupMemberController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a GlAccountGroupMember
	 * @return a List with the GlAccountGroupMembers
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<GlAccountGroupMember> findGlAccountGroupMembersBy(@RequestParam Map<String, String> allRequestParams) {

		FindGlAccountGroupMembersBy query = new FindGlAccountGroupMembersBy(allRequestParams);

		int usedTicketId;

		synchronized (GlAccountGroupMemberController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupMemberFound.class,
				event -> sendGlAccountGroupMembersFoundMessage(((GlAccountGroupMemberFound) event).getGlAccountGroupMembers(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendGlAccountGroupMembersFoundMessage(List<GlAccountGroupMember> glAccountGroupMembers, int usedTicketId) {
		queryReturnVal.put(usedTicketId, glAccountGroupMembers);
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
	public boolean createGlAccountGroupMember(HttpServletRequest request) {

		GlAccountGroupMember glAccountGroupMemberToBeAdded = new GlAccountGroupMember();
		try {
			glAccountGroupMemberToBeAdded = GlAccountGroupMemberMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createGlAccountGroupMember(glAccountGroupMemberToBeAdded);

	}

	/**
	 * creates a new GlAccountGroupMember entry in the ofbiz database
	 * 
	 * @param glAccountGroupMemberToBeAdded
	 *            the GlAccountGroupMember thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createGlAccountGroupMember(GlAccountGroupMember glAccountGroupMemberToBeAdded) {

		AddGlAccountGroupMember com = new AddGlAccountGroupMember(glAccountGroupMemberToBeAdded);
		int usedTicketId;

		synchronized (GlAccountGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupMemberAdded.class,
				event -> sendGlAccountGroupMemberChangedMessage(((GlAccountGroupMemberAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateGlAccountGroupMember(HttpServletRequest request) {

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

		GlAccountGroupMember glAccountGroupMemberToBeUpdated = new GlAccountGroupMember();

		try {
			glAccountGroupMemberToBeUpdated = GlAccountGroupMemberMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateGlAccountGroupMember(glAccountGroupMemberToBeUpdated);

	}

	/**
	 * Updates the GlAccountGroupMember with the specific Id
	 * 
	 * @param glAccountGroupMemberToBeUpdated the GlAccountGroupMember thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateGlAccountGroupMember(GlAccountGroupMember glAccountGroupMemberToBeUpdated) {

		UpdateGlAccountGroupMember com = new UpdateGlAccountGroupMember(glAccountGroupMemberToBeUpdated);

		int usedTicketId;

		synchronized (GlAccountGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupMemberUpdated.class,
				event -> sendGlAccountGroupMemberChangedMessage(((GlAccountGroupMemberUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a GlAccountGroupMember from the database
	 * 
	 * @param glAccountGroupMemberId:
	 *            the id of the GlAccountGroupMember thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteglAccountGroupMemberById(@RequestParam(value = "glAccountGroupMemberId") String glAccountGroupMemberId) {

		DeleteGlAccountGroupMember com = new DeleteGlAccountGroupMember(glAccountGroupMemberId);

		int usedTicketId;

		synchronized (GlAccountGroupMemberController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(GlAccountGroupMemberDeleted.class,
				event -> sendGlAccountGroupMemberChangedMessage(((GlAccountGroupMemberDeleted) event).isSuccess(), usedTicketId));

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

	public void sendGlAccountGroupMemberChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/glAccountGroupMember/\" plus one of the following: "
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
