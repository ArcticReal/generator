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
import com.skytala.eCommerce.command.AddJobRequisition;
import com.skytala.eCommerce.command.DeleteJobRequisition;
import com.skytala.eCommerce.command.UpdateJobRequisition;
import com.skytala.eCommerce.entity.JobRequisition;
import com.skytala.eCommerce.entity.JobRequisitionMapper;
import com.skytala.eCommerce.event.JobRequisitionAdded;
import com.skytala.eCommerce.event.JobRequisitionDeleted;
import com.skytala.eCommerce.event.JobRequisitionFound;
import com.skytala.eCommerce.event.JobRequisitionUpdated;
import com.skytala.eCommerce.query.FindJobRequisitionsBy;

@RestController
@RequestMapping("/api/jobRequisition")
public class JobRequisitionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<JobRequisition>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public JobRequisitionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a JobRequisition
	 * @return a List with the JobRequisitions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<JobRequisition> findJobRequisitionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindJobRequisitionsBy query = new FindJobRequisitionsBy(allRequestParams);

		int usedTicketId;

		synchronized (JobRequisitionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobRequisitionFound.class,
				event -> sendJobRequisitionsFoundMessage(((JobRequisitionFound) event).getJobRequisitions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendJobRequisitionsFoundMessage(List<JobRequisition> jobRequisitions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, jobRequisitions);
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
	public boolean createJobRequisition(HttpServletRequest request) {

		JobRequisition jobRequisitionToBeAdded = new JobRequisition();
		try {
			jobRequisitionToBeAdded = JobRequisitionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createJobRequisition(jobRequisitionToBeAdded);

	}

	/**
	 * creates a new JobRequisition entry in the ofbiz database
	 * 
	 * @param jobRequisitionToBeAdded
	 *            the JobRequisition thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createJobRequisition(JobRequisition jobRequisitionToBeAdded) {

		AddJobRequisition com = new AddJobRequisition(jobRequisitionToBeAdded);
		int usedTicketId;

		synchronized (JobRequisitionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobRequisitionAdded.class,
				event -> sendJobRequisitionChangedMessage(((JobRequisitionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateJobRequisition(HttpServletRequest request) {

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

		JobRequisition jobRequisitionToBeUpdated = new JobRequisition();

		try {
			jobRequisitionToBeUpdated = JobRequisitionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateJobRequisition(jobRequisitionToBeUpdated);

	}

	/**
	 * Updates the JobRequisition with the specific Id
	 * 
	 * @param jobRequisitionToBeUpdated the JobRequisition thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateJobRequisition(JobRequisition jobRequisitionToBeUpdated) {

		UpdateJobRequisition com = new UpdateJobRequisition(jobRequisitionToBeUpdated);

		int usedTicketId;

		synchronized (JobRequisitionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobRequisitionUpdated.class,
				event -> sendJobRequisitionChangedMessage(((JobRequisitionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a JobRequisition from the database
	 * 
	 * @param jobRequisitionId:
	 *            the id of the JobRequisition thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletejobRequisitionById(@RequestParam(value = "jobRequisitionId") String jobRequisitionId) {

		DeleteJobRequisition com = new DeleteJobRequisition(jobRequisitionId);

		int usedTicketId;

		synchronized (JobRequisitionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobRequisitionDeleted.class,
				event -> sendJobRequisitionChangedMessage(((JobRequisitionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendJobRequisitionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/jobRequisition/\" plus one of the following: "
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
