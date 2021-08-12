/**
 * File: PersonResource.java Course materials (21S) CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * @author (original) Mike Norman
 * 
 * Update by:  Ehsan
 * 
 */
package bloodbank.rest.resource;

import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.CUSTOMER_ADDRESS_SUBRESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response.Status;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.security.enterprise.SecurityContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.Address;
import bloodbank.entity.Contact;
import bloodbank.entity.Person;
import bloodbank.entity.SecurityUser;

@Path(CUSTOMER_ADDRESS_SUBRESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
/**
 * 
 * @author Ehsan
 *
 */
public class AddressResource {

	private static final Logger LOG = LogManager.getLogger();

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;

	@GET
	@RolesAllowed({ADMIN_ROLE})
	public Response getAddresses() {
		LOG.debug("retrieving all Addresses ...");
		List<Address> address = service.getAllAddresses();
		Response response = Response.ok(address).build();
		return response;
	}

	@GET
	@RolesAllowed({ADMIN_ROLE, USER_ROLE})
	@Path(RESOURCE_PATH_ID_PATH)
	public Response getAddressById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug("try to retrieve specific Address " + id);
		Response response = null;
		Address address = null;

		if (sc.isCallerInRole(ADMIN_ROLE) ) {
			address = service.getAddressByID(id);
			response = Response.status(address == null ? Status.NOT_FOUND : Status.OK).entity(address).build();
		}
		else if ( sc.isCallerInRole( USER_ROLE)) {
			WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
			SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
			Person user = sUser.getPerson();
			if ( user != null) {			
				for (Contact c : user.getContacts()) {
					if (c.getAddress().getId()==id) {
						response = Response.status( Status.OK).entity( address).build();
					}
				}
			} else {
				throw new ForbiddenException( "User trying to access resource it does not own (wrong userid)");
			}
		}
		
		 else {
			response = Response.status(Status.BAD_REQUEST).build();
		}
		return response;
	}

	@POST
	@RolesAllowed( { ADMIN_ROLE })
	public Response addAddressRecord(Address newAddress) {
		Address addressRecord= service.persistAddress( newAddress);
		Response response = Response.ok(addressRecord).build();
		return response;
	}
	
	@PUT
	@RolesAllowed( { ADMIN_ROLE })
	@Path("{id}")
	public Response updateAddressRecord( @PathParam(RESOURCE_PATH_ID_ELEMENT) int id, Address recordWithUpdates) {
		Address updatedAddress = service.updateAddressById(id, recordWithUpdates);
		Response response = Response.ok( updatedAddress).build();
		return response;
	}
	
	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path("{id}")
	public Response deleteAddressRecord( @PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {		
		service.deleteAddressById(id);
		Response response = Response.ok().build();
		return response;
	}
	
	
	
}