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
import com.skytala.eCommerce.command.AddProductMeter;
import com.skytala.eCommerce.command.DeleteProductMeter;
import com.skytala.eCommerce.command.UpdateProductMeter;
import com.skytala.eCommerce.entity.ProductMeter;
import com.skytala.eCommerce.entity.ProductMeterMapper;
import com.skytala.eCommerce.event.ProductMeterAdded;
import com.skytala.eCommerce.event.ProductMeterDeleted;
import com.skytala.eCommerce.event.ProductMeterFound;
import com.skytala.eCommerce.event.ProductMeterUpdated;
import com.skytala.eCommerce.query.FindProductMetersBy;

@RestController
@RequestMapping("/api/productMeter")
public class ProductMeterController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductMeter>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductMeterController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductMeter
	 * @return a List with the ProductMeters
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductMeter> findProductMetersBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductMetersBy query = new FindProductMetersBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductMeterController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMeterFound.class,
				event -> sendProductMetersFoundMessage(((ProductMeterFound) event).getProductMeters(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductMetersFoundMessage(List<ProductMeter> productMeters, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productMeters);
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
	public boolean createProductMeter(HttpServletRequest request) {

		ProductMeter productMeterToBeAdded = new ProductMeter();
		try {
			productMeterToBeAdded = ProductMeterMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductMeter(productMeterToBeAdded);

	}

	/**
	 * creates a new ProductMeter entry in the ofbiz database
	 * 
	 * @param productMeterToBeAdded
	 *            the ProductMeter thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductMeter(ProductMeter productMeterToBeAdded) {

		AddProductMeter com = new AddProductMeter(productMeterToBeAdded);
		int usedTicketId;

		synchronized (ProductMeterController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMeterAdded.class,
				event -> sendProductMeterChangedMessage(((ProductMeterAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductMeter(HttpServletRequest request) {

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

		ProductMeter productMeterToBeUpdated = new ProductMeter();

		try {
			productMeterToBeUpdated = ProductMeterMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductMeter(productMeterToBeUpdated);

	}

	/**
	 * Updates the ProductMeter with the specific Id
	 * 
	 * @param productMeterToBeUpdated the ProductMeter thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductMeter(ProductMeter productMeterToBeUpdated) {

		UpdateProductMeter com = new UpdateProductMeter(productMeterToBeUpdated);

		int usedTicketId;

		synchronized (ProductMeterController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMeterUpdated.class,
				event -> sendProductMeterChangedMessage(((ProductMeterUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductMeter from the database
	 * 
	 * @param productMeterId:
	 *            the id of the ProductMeter thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductMeterById(@RequestParam(value = "productMeterId") String productMeterId) {

		DeleteProductMeter com = new DeleteProductMeter(productMeterId);

		int usedTicketId;

		synchronized (ProductMeterController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductMeterDeleted.class,
				event -> sendProductMeterChangedMessage(((ProductMeterDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductMeterChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productMeter/\" plus one of the following: "
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
