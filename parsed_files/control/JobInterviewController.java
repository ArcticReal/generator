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
import com.skytala.eCommerce.command.AddJobInterview;
import com.skytala.eCommerce.command.DeleteJobInterview;
import com.skytala.eCommerce.command.UpdateJobInterview;
import com.skytala.eCommerce.entity.JobInterview;
import com.skytala.eCommerce.entity.JobInterviewMapper;
import com.skytala.eCommerce.event.JobInterviewAdded;
import com.skytala.eCommerce.event.JobInterviewDeleted;
import com.skytala.eCommerce.event.JobInterviewFound;
import com.skytala.eCommerce.event.JobInterviewUpdated;
import com.skytala.eCommerce.query.FindJobInterviewsBy;

@RestController
@RequestMapping("/api/jobInterview")
public class JobInterviewController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<JobInterview>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public JobInterviewController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a JobInterview
	 * @return a List with the JobInterviews
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<JobInterview> findJobInterviewsBy(@RequestParam Map<String, String> allRequestParams) {

		FindJobInterviewsBy query = new FindJobInterviewsBy(allRequestParams);

		int usedTicketId;

		synchronized (JobInterviewController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobInterviewFound.class,
				event -> sendJobInterviewsFoundMessage(((JobInterviewFound) event).getJobInterviews(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendJobInterviewsFoundMessage(List<JobInterview> jobInterviews, int usedTicketId) {
		queryReturnVal.put(usedTicketId, jobInterviews);
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
	public boolean createJobInterview(HttpServletRequest request) {

		JobInterview jobInterviewToBeAdded = new JobInterview();
		try {
			jobInterviewToBeAdded = JobInterviewMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createJobInterview(jobInterviewToBeAdded);

	}

	/**
	 * creates a new JobInterview entry in the ofbiz database
	 * 
	 * @param jobInterviewToBeAdded
	 *            the JobInterview thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createJobInterview(JobInterview jobInterviewToBeAdded) {

		AddJobInterview com = new AddJobInterview(jobInterviewToBeAdded);
		int usedTicketId;

		synchronized (JobInterviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobInterviewAdded.class,
				event -> sendJobInterviewChangedMessage(((JobInterviewAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateJobInterview(HttpServletRequest request) {

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

		JobInterview jobInterviewToBeUpdated = new JobInterview();

		try {
			jobInterviewToBeUpdated = JobInterviewMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateJobInterview(jobInterviewToBeUpdated);

	}

	/**
	 * Updates the JobInterview with the specific Id
	 * 
	 * @param jobInterviewToBeUpdated the JobInterview thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateJobInterview(JobInterview jobInterviewToBeUpdated) {

		UpdateJobInterview com = new UpdateJobInterview(jobInterviewToBeUpdated);

		int usedTicketId;

		synchronized (JobInterviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobInterviewUpdated.class,
				event -> sendJobInterviewChangedMessage(((JobInterviewUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a JobInterview from the database
	 * 
	 * @param jobInterviewId:
	 *            the id of the JobInterview thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletejobInterviewById(@RequestParam(value = "jobInterviewId") String jobInterviewId) {

		DeleteJobInterview com = new DeleteJobInterview(jobInterviewId);

		int usedTicketId;

		synchronized (JobInterviewController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(JobInterviewDeleted.class,
				event -> sendJobInterviewChangedMessage(((JobInterviewDeleted) event).isSuccess(), usedTicketId));

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

	public void sendJobInterviewChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/jobInterview/\" plus one of the following: "
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
