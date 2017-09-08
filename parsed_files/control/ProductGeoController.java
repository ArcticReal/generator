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
import com.skytala.eCommerce.command.AddProductGeo;
import com.skytala.eCommerce.command.DeleteProductGeo;
import com.skytala.eCommerce.command.UpdateProductGeo;
import com.skytala.eCommerce.entity.ProductGeo;
import com.skytala.eCommerce.entity.ProductGeoMapper;
import com.skytala.eCommerce.event.ProductGeoAdded;
import com.skytala.eCommerce.event.ProductGeoDeleted;
import com.skytala.eCommerce.event.ProductGeoFound;
import com.skytala.eCommerce.event.ProductGeoUpdated;
import com.skytala.eCommerce.query.FindProductGeosBy;

@RestController
@RequestMapping("/api/productGeo")
public class ProductGeoController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductGeo>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductGeoController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductGeo
	 * @return a List with the ProductGeos
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductGeo> findProductGeosBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductGeosBy query = new FindProductGeosBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductGeoController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGeoFound.class,
				event -> sendProductGeosFoundMessage(((ProductGeoFound) event).getProductGeos(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductGeosFoundMessage(List<ProductGeo> productGeos, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productGeos);
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
	public boolean createProductGeo(HttpServletRequest request) {

		ProductGeo productGeoToBeAdded = new ProductGeo();
		try {
			productGeoToBeAdded = ProductGeoMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductGeo(productGeoToBeAdded);

	}

	/**
	 * creates a new ProductGeo entry in the ofbiz database
	 * 
	 * @param productGeoToBeAdded
	 *            the ProductGeo thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductGeo(ProductGeo productGeoToBeAdded) {

		AddProductGeo com = new AddProductGeo(productGeoToBeAdded);
		int usedTicketId;

		synchronized (ProductGeoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGeoAdded.class,
				event -> sendProductGeoChangedMessage(((ProductGeoAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductGeo(HttpServletRequest request) {

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

		ProductGeo productGeoToBeUpdated = new ProductGeo();

		try {
			productGeoToBeUpdated = ProductGeoMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductGeo(productGeoToBeUpdated);

	}

	/**
	 * Updates the ProductGeo with the specific Id
	 * 
	 * @param productGeoToBeUpdated the ProductGeo thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductGeo(ProductGeo productGeoToBeUpdated) {

		UpdateProductGeo com = new UpdateProductGeo(productGeoToBeUpdated);

		int usedTicketId;

		synchronized (ProductGeoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGeoUpdated.class,
				event -> sendProductGeoChangedMessage(((ProductGeoUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductGeo from the database
	 * 
	 * @param productGeoId:
	 *            the id of the ProductGeo thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductGeoById(@RequestParam(value = "productGeoId") String productGeoId) {

		DeleteProductGeo com = new DeleteProductGeo(productGeoId);

		int usedTicketId;

		synchronized (ProductGeoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductGeoDeleted.class,
				event -> sendProductGeoChangedMessage(((ProductGeoDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductGeoChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productGeo/\" plus one of the following: "
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
