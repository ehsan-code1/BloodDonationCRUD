package bloodbank.rest.resource;



import static bloodbank.utility.MyConstants.ADMIN_ROLE;
import static bloodbank.utility.MyConstants.DONATION_RECORD_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static bloodbank.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static bloodbank.utility.MyConstants.USER_ROLE;
import static bloodbank.utility.MyConstants.DONATION_RECORD_RESOURCE_PATH;

import java.util.List;

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
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.soteria.WrappingCallerPrincipal;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.DonationRecord;
import bloodbank.entity.Person;
import bloodbank.entity.SecurityUser;
import bloodbank.rest.resource.PersonResource;


@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DonationRecordResource {
	
	private static final Logger LOG = LogManager.getLogger();

	

	@EJB
	protected BloodBankService service;

	@Inject
	protected SecurityContext sc;
	

	@GET
	@RolesAllowed({ADMIN_ROLE})
	@Path(DONATION_RECORD_RESOURCE_NAME)
	public Response getDonationRecord() {
		LOG.debug( "retrieving all donation records");
		
		List<DonationRecord> donationRecords = service.getAllDonationRecord();
		Response response = Response.ok( donationRecords).build();
		return response;
		
	}
	
	

	@GET
	@RolesAllowed( { ADMIN_ROLE, USER_ROLE })
	@Path("donationRecord/{id}")
	public Response getRecordById( @PathParam( RESOURCE_PATH_ID_ELEMENT) int id) {
		LOG.debug( "try to retrieve specific record with id:  " + id);
		Response response = null;
		DonationRecord donationRecord = service.getDonationRecordById(id);

		if ( sc.isCallerInRole( ADMIN_ROLE)) {
			response = Response.status( donationRecord == null ? Status.NOT_FOUND : Status.OK).entity( donationRecord).build();
		} else if ( sc.isCallerInRole( USER_ROLE)) {
			WrappingCallerPrincipal wCallerPrincipal = (WrappingCallerPrincipal) sc.getCallerPrincipal();
			SecurityUser sUser = (SecurityUser) wCallerPrincipal.getWrapped();
			Person user = sUser.getPerson();
			if ( user != null) {			
				for (DonationRecord d : user.getDonations()) {
					if (d.getDonation().getId()==id) {
						response = Response.status( Status.OK).entity( donationRecord).build();
					}
				}
			} else {
				throw new ForbiddenException( "User trying to access resource it does not own (wrong userid)");
			}
		} else {
			response = Response.status( Status.BAD_REQUEST).build();
		}
		return response;
	}
	
	@POST
	@RolesAllowed( { ADMIN_ROLE })
	//@Path("{id}/{bloodDonationID}/donationRecord")
	@Path("donationRecord/person/{id}/bloodDonation/{bloodDonationId}")
	public Response addDonationRecord(@PathParam( RESOURCE_PATH_ID_ELEMENT) int id,@PathParam("bloodDonationId") int bloodDonationId,DonationRecord newDonationRecord) {
		LOG.debug("Adding a new donation record= {}", newDonationRecord);
		Response response = null;
        
		DonationRecord donationRecord= service.persistDonationRecord(id, bloodDonationId,newDonationRecord);
		response = Response.ok(donationRecord).build();
		return response;
	}
	
	//update donation record for  existing person
	@PUT
	@RolesAllowed( { ADMIN_ROLE })
	@Path("donationRecord/{id}/person/{personId}")
	public Response updateDonationRecord( @PathParam("id") int recordId,@PathParam("personId") int personId,DonationRecord recordWithUpdates) {
		LOG.debug("Updating a specific donation record with id = {}", recordId);
		DonationRecord updatedRecord = service.updateDonationRecord(recordId,personId ,recordWithUpdates);
		Response response = Response.ok( updatedRecord).build();
		return response;
	}
	
	@DELETE
	@RolesAllowed( { ADMIN_ROLE })
	@Path("donationRecord/{id}")
	public Response deleteDonationRecord( @PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
		service.deleteDonationRecordById(id);
		Response response = Response.ok().build();
		return response;
	}
	
	

}
