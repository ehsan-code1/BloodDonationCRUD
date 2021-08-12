/**
 * @author Nouran Nouh
 * @Date 2021-08-09
 */
package bloodbank;

import static bloodbank.utility.MyConstants.APPLICATION_API_VERSION;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER;
import static bloodbank.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static bloodbank.utility.MyConstants.DEFAULT_USER_PREFIX;
import static bloodbank.utility.MyConstants.PERSON_RESOURCE_NAME;
import static bloodbank.utility.MyConstants.PU_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.hamcrest.Matchers;
import org.hamcrest.text.IsEmptyString;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import bloodbank.ejb.BloodBankService;
import bloodbank.entity.BloodBank;
import bloodbank.entity.BloodDonation;
import bloodbank.entity.BloodType;
import bloodbank.entity.DonationRecord;
import bloodbank.entity.Person;
import bloodbank.rest.resource.DonationRecordResource;
import bloodbank.rest.resource.PersonResource;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestDonationRecord {


///	private EntityTransaction et;
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String APPLICATION_CONTEXT_ROOT = "REST-BloodBank";
    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;




    // test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;


    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic("cst8288", "8288");
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient(
            new ClientConfig().register(MyObjectMapperProvider.class).register(new LoggingFeature()));
        webTarget = client.target(uri);

    }

    @Order(1)
    @Test
    public void test_all_donation_records_with_adminrole() throws JsonMappingException, JsonProcessingException{
    	Response response = webTarget
    			  .register(adminAuth)
    			  .path("/donationRecord")
    			  .request()
    			  .get();
    	  assertThat(response.getStatus(), is(200));
    	  List<DonationRecord> donationRecord=response.readEntity(new GenericType<List<DonationRecord>>(){});
    	  assertThat(donationRecord, is(not(empty())));
          int size=donationRecord.size();
    	  assertThat(donationRecord, hasSize(size));


    }

   @Order(2)
   @Test
   public void test_get_donationRecord_By_ID_by_adminRole() {
	   Response response = webTarget
 			  .register(adminAuth)
 			  .path("/donationRecord/2")
 			  .request()
 			  .get();

	   assertThat(response.getStatus(), is(200));

	   assertNotNull(response.getEntity().toString());

   }

   @Order(3)
   @Test
   public void testMediaType() {
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("/donationRecord")
	 			  .request()
	 			  .get();
	   assertThat(response.getStatus(), is(200));
	   assertThat(response.getMediaType().toString(),is(MediaType.APPLICATION_JSON));


   }
   @Order(4)
   @Test
   public void test_donationRecord_post() {
	   //get blood bank response
	   Response responseBloodBankGet= webTarget
	 			  .register(adminAuth)
	 			  .path("/bloodbank")
	 			  .request()
	 			  .get();
	 //get Blood Bank
	   BloodBank getBloodBank=responseBloodBankGet.readEntity(BloodBank.class);


	   //create a new blood donation
	   BloodDonation newBloodDonation= new BloodDonation();
	   BloodType bloodType= new BloodType();
	   bloodType.setType("A","0");
	   newBloodDonation.setBank(getBloodBank);
	   newBloodDonation.setMilliliters(20);
	   newBloodDonation.setBloodType(bloodType);

	   //post bloodDonation
	   Response responseBloodDonationPost= webTarget
	 			  .register(adminAuth)
	 			  .path("bloodDonation/1")
	 			  .request()
	 			  .post(Entity.entity( newBloodDonation, MediaType.APPLICATION_JSON));

	   //get blood donation at new id
	   Response responseBloodDonationGet= webTarget
	 			  .register(adminAuth)
	 			  .path("bloodDonation/"+responseBloodDonationPost.readEntity(BloodDonation.class).getId())
	 			  .request()
	 			  .get();
	   //read blood donation
	   BloodDonation bloodDonation= responseBloodDonationGet.readEntity(BloodDonation.class);

	  //get Person
	  Response responsePerson = webTarget
 			  .register(adminAuth)
 			  .path("/person/1")
 			  .request()
 			  .get();
	 Person p= responsePerson.readEntity(Person.class);
	 logger.debug("status info for post person:{} ",p.getId());
	 p.getId();

	 DonationRecord newRecord=new DonationRecord();
	 newRecord.setOwner(p);
	 newRecord.setDonation(bloodDonation);
	 newRecord.setTested((byte)1);


	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("donationRecord/person/1/bloodDonation/"+bloodDonation.getId())
	 			  .request()
	 			  .post(Entity.entity(newRecord, MediaType.APPLICATION_JSON));
	   logger.debug("status info for post blood donation:{} ",bloodDonation.getId());
	   logger.debug("status info for post:{} ",response.getStatusInfo());

	   assertThat(response.getStatus(), is(200));
	   assertNotNull(response.getEntity().toString());



   }
   @Order(5)
   @Test
   public void test_donationRecord_put() {

	  //update record 2
	  Response responsePerson = webTarget
 			  .register(adminAuth)
 			  .path("/person/1")
 			  .request()
 			  .get();
	 Person p= responsePerson.readEntity(Person.class);
	 logger.debug("status info for post person:{} ",p.getId());

	 Response responseDonationRecord = webTarget
			  .register(adminAuth)
			  .path("/donationRecord/2")
			  .request()
			  .get();

	 DonationRecord record=responseDonationRecord.readEntity(DonationRecord.class);

	  record.setOwner(p);

	  record.setTested((byte)1);


	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("donationRecord/2/person/1")
	 			  .request()
	 			  .put(Entity.entity(record, MediaType.APPLICATION_JSON));
      
       assertEquals(record.getTested(),1);
	   assertThat(response.getStatus(), is(200));
	   assertNotNull(response.getEntity().toString());



   }
  @Order(9)
  @Test
  void  test_delete_donationRecord(){
	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("donationRecord/1")
	 			  .request()
	 			  .delete();

	   Response responseGet = webTarget
	 			  .register(adminAuth)
	 			  .path("donationRecord/1")
	 			  .request()
	 			  .get();
      
       assertThat(responseGet.getStatus(), is(404));//not found as it is deleted
	   assertThat(response.getStatus(), is(200));
	   


   }

  @Order(6)
  @Test
  void  test_Post_constraint_vailoation(){
	  DonationRecord newRecord=new DonationRecord();




	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("donationRecord/person/1/bloodDonation/2")
	 			  .request()
	 			  .post(Entity.entity(newRecord, MediaType.APPLICATION_JSON));

	  // logger.debug("status info for post:{} ",response.getStatusInfo());
	    assertThat(response.getStatus(), is(500));



  }

  @Order(7)
  @Test
  void test_Url_donationRecord() {

	assertEquals(uri,  webTarget.getUri());


  }

  @Order(8)
  @Test
  void test_findIdNotFound() {

	   Response response = webTarget
	 			  .register(adminAuth)
	 			  .path("/donationRecord/100")
	 			  .request()
	 			  .get();

	   assertThat(response.getStatus(), is(404));

	   assertNotEquals(response.readEntity(BloodDonation.class).getId(),100);

  }






}
