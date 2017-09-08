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
import com.skytala.eCommerce.command.AddUnemploymentClaim;
import com.skytala.eCommerce.command.DeleteUnemploymentClaim;
import com.skytala.eCommerce.command.UpdateUnemploymentClaim;
import com.skytala.eCommerce.entity.UnemploymentClaim;
import com.skytala.eCommerce.entity.UnemploymentClaimMapper;
import com.skytala.eCommerce.event.UnemploymentClaimAdded;
import com.skytala.eCommerce.event.UnemploymentClaimDeleted;
import com.skytala.eCommerce.event.UnemploymentClaimFound;
import com.skytala.eCommerce.event.UnemploymentClaimUpdated;
import com.skytala.eCommerce.query.FindUnemploymentClaimsBy;

@RestController
@RequestMapping("/api/unemploymentClaim")
public class UnemploymentClaimController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<UnemploymentClaim>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public UnemploymentClaimController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a UnemploymentClaim
	 * @return a List with the UnemploymentClaims
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<UnemploymentClaim> findUnemploymentClaimsBy(@RequestParam Map<String, String> allRequestParams) {

		FindUnemploymentClaimsBy query = new FindUnemploymentClaimsBy(allRequestParams);

		int usedTicketId;

		synchronized (UnemploymentClaimController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UnemploymentClaimFound.class,
				event -> sendUnemploymentClaimsFoundMessage(((UnemploymentClaimFound) event).getUnemploymentClaims(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendUnemploymentClaimsFoundMessage(List<UnemploymentClaim> unemploymentClaims, int usedTicketId) {
		queryReturnVal.put(usedTicketId, unemploymentClaims);
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
	public boolean createUnemploymentClaim(HttpServletRequest request) {

		UnemploymentClaim unemploymentClaimToBeAdded = new UnemploymentClaim();
		try {
			unemploymentClaimToBeAdded = UnemploymentClaimMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createUnemploymentClaim(unemploymentClaimToBeAdded);

	}

	/**
	 * creates a new UnemploymentClaim entry in the ofbiz database
	 * 
	 * @param unemploymentClaimToBeAdded
	 *            the UnemploymentClaim thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createUnemploymentClaim(UnemploymentClaim unemploymentClaimToBeAdded) {

		AddUnemploymentClaim com = new AddUnemploymentClaim(unemploymentClaimToBeAdded);
		int usedTicketId;

		synchronized (UnemploymentClaimController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UnemploymentClaimAdded.class,
				event -> sendUnemploymentClaimChangedMessage(((UnemploymentClaimAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateUnemploymentClaim(HttpServletRequest request) {

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

		UnemploymentClaim unemploymentClaimToBeUpdated = new UnemploymentClaim();

		try {
			unemploymentClaimToBeUpdated = UnemploymentClaimMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateUnemploymentClaim(unemploymentClaimToBeUpdated);

	}

	/**
	 * Updates the UnemploymentClaim with the specific Id
	 * 
	 * @param unemploymentClaimToBeUpdated the UnemploymentClaim thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateUnemploymentClaim(UnemploymentClaim unemploymentClaimToBeUpdated) {

		UpdateUnemploymentClaim com = new UpdateUnemploymentClaim(unemploymentClaimToBeUpdated);

		int usedTicketId;

		synchronized (UnemploymentClaimController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UnemploymentClaimUpdated.class,
				event -> sendUnemploymentClaimChangedMessage(((UnemploymentClaimUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a UnemploymentClaim from the database
	 * 
	 * @param unemploymentClaimId:
	 *            the id of the UnemploymentClaim thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteunemploymentClaimById(@RequestParam(value = "unemploymentClaimId") String unemploymentClaimId) {

		DeleteUnemploymentClaim com = new DeleteUnemploymentClaim(unemploymentClaimId);

		int usedTicketId;

		synchronized (UnemploymentClaimController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(UnemploymentClaimDeleted.class,
				event -> sendUnemploymentClaimChangedMessage(((UnemploymentClaimDeleted) event).isSuccess(), usedTicketId));

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

	public void sendUnemploymentClaimChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/unemploymentClaim/\" plus one of the following: "
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
