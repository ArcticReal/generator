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
import com.skytala.eCommerce.command.AddTaxAuthorityRateProduct;
import com.skytala.eCommerce.command.DeleteTaxAuthorityRateProduct;
import com.skytala.eCommerce.command.UpdateTaxAuthorityRateProduct;
import com.skytala.eCommerce.entity.TaxAuthorityRateProduct;
import com.skytala.eCommerce.entity.TaxAuthorityRateProductMapper;
import com.skytala.eCommerce.event.TaxAuthorityRateProductAdded;
import com.skytala.eCommerce.event.TaxAuthorityRateProductDeleted;
import com.skytala.eCommerce.event.TaxAuthorityRateProductFound;
import com.skytala.eCommerce.event.TaxAuthorityRateProductUpdated;
import com.skytala.eCommerce.query.FindTaxAuthorityRateProductsBy;

@RestController
@RequestMapping("/api/taxAuthorityRateProduct")
public class TaxAuthorityRateProductController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TaxAuthorityRateProduct>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TaxAuthorityRateProductController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TaxAuthorityRateProduct
	 * @return a List with the TaxAuthorityRateProducts
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TaxAuthorityRateProduct> findTaxAuthorityRateProductsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTaxAuthorityRateProductsBy query = new FindTaxAuthorityRateProductsBy(allRequestParams);

		int usedTicketId;

		synchronized (TaxAuthorityRateProductController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityRateProductFound.class,
				event -> sendTaxAuthorityRateProductsFoundMessage(((TaxAuthorityRateProductFound) event).getTaxAuthorityRateProducts(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTaxAuthorityRateProductsFoundMessage(List<TaxAuthorityRateProduct> taxAuthorityRateProducts, int usedTicketId) {
		queryReturnVal.put(usedTicketId, taxAuthorityRateProducts);
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
	public boolean createTaxAuthorityRateProduct(HttpServletRequest request) {

		TaxAuthorityRateProduct taxAuthorityRateProductToBeAdded = new TaxAuthorityRateProduct();
		try {
			taxAuthorityRateProductToBeAdded = TaxAuthorityRateProductMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTaxAuthorityRateProduct(taxAuthorityRateProductToBeAdded);

	}

	/**
	 * creates a new TaxAuthorityRateProduct entry in the ofbiz database
	 * 
	 * @param taxAuthorityRateProductToBeAdded
	 *            the TaxAuthorityRateProduct thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTaxAuthorityRateProduct(TaxAuthorityRateProduct taxAuthorityRateProductToBeAdded) {

		AddTaxAuthorityRateProduct com = new AddTaxAuthorityRateProduct(taxAuthorityRateProductToBeAdded);
		int usedTicketId;

		synchronized (TaxAuthorityRateProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityRateProductAdded.class,
				event -> sendTaxAuthorityRateProductChangedMessage(((TaxAuthorityRateProductAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTaxAuthorityRateProduct(HttpServletRequest request) {

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

		TaxAuthorityRateProduct taxAuthorityRateProductToBeUpdated = new TaxAuthorityRateProduct();

		try {
			taxAuthorityRateProductToBeUpdated = TaxAuthorityRateProductMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTaxAuthorityRateProduct(taxAuthorityRateProductToBeUpdated);

	}

	/**
	 * Updates the TaxAuthorityRateProduct with the specific Id
	 * 
	 * @param taxAuthorityRateProductToBeUpdated the TaxAuthorityRateProduct thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTaxAuthorityRateProduct(TaxAuthorityRateProduct taxAuthorityRateProductToBeUpdated) {

		UpdateTaxAuthorityRateProduct com = new UpdateTaxAuthorityRateProduct(taxAuthorityRateProductToBeUpdated);

		int usedTicketId;

		synchronized (TaxAuthorityRateProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityRateProductUpdated.class,
				event -> sendTaxAuthorityRateProductChangedMessage(((TaxAuthorityRateProductUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TaxAuthorityRateProduct from the database
	 * 
	 * @param taxAuthorityRateProductId:
	 *            the id of the TaxAuthorityRateProduct thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetaxAuthorityRateProductById(@RequestParam(value = "taxAuthorityRateProductId") String taxAuthorityRateProductId) {

		DeleteTaxAuthorityRateProduct com = new DeleteTaxAuthorityRateProduct(taxAuthorityRateProductId);

		int usedTicketId;

		synchronized (TaxAuthorityRateProductController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TaxAuthorityRateProductDeleted.class,
				event -> sendTaxAuthorityRateProductChangedMessage(((TaxAuthorityRateProductDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTaxAuthorityRateProductChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/taxAuthorityRateProduct/\" plus one of the following: "
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
