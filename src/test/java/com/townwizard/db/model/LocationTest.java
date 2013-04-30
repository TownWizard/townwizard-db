package com.townwizard.db.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.townwizard.db.test.TestSupport;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;

/**
 * Tests global location hibernate object mappings
 */
public class LocationTest extends TestSupport {

    private Session session;
    
    @Before
    public void beginTransaction() {
        session = getDirectorySessionFactory().openSession();        
        session.beginTransaction();        
    }
    
    @After
    public void rollbackTransaction() {
        try {
            if(session != null) {
                session.getTransaction().rollback();                
            }
        } finally {
            if(session != null) {
                session.close();
            }
        }
    }
    
    @Test
    public void testLocationIngest() {
        try {
            PlaceIngest i = createLocationIngest();
            session.save(i);
            Long id = i.getId();
            assertNotNull("Place ingest id should not be null after save()", id);            
            
            PlaceIngest fromDb = getLocationIngestById(id);
            assertNotNull("Place ingest should be found in db after save() by id", fromDb);
            Date updated = fromDb.getUpdated();
            try { Thread.sleep(100); } catch (Exception e) {}
            fromDb.setDistance(50000);
            session.save(fromDb);
            
            fromDb = getLocationIngestById(id);
            assertNotSame("Place ingest updated should change after update()", updated, fromDb.getUpdated());
            
            session.delete(fromDb);
            fromDb = getLocationIngestById(id);
            assertNull("Place ingest should not be found after delete()", fromDb);
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testLocationCategory() {
        try {
            PlaceCategory c = createLocationCategory();
            session.save(c);
            Long id = c.getId();
            assertNotNull("Place category id should not be null after save()", id);            
            
            PlaceCategory fromDb = getLocationCategoryById(id);
            assertNotNull("Place category should be found in db after save() by id", fromDb);
            
            session.delete(fromDb);
            fromDb = getLocationCategoryById(id);
            assertNull("Place category should not be found after delete()", fromDb);
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testLocation() {
        try {
            Place l = createLocation();            
            session.save(l);
            Long id = l.getId();
            assertNotNull("Place id should not be null after save()", id);
            session.evict(l);
                        
            Place fromDb = getLocationById(id);
            assertNotNull("Place should be found in db after save() by id", fromDb);
            assertTrue("Retrieved place must match the original one after save()", locationsEqual(l, fromDb));
            
            fromDb.setCategory("Unique category name 2");
            session.save(fromDb);
            
            fromDb = getLocationById(id);            
            assertFalse("Place should change after update()", locationsEqual(l, fromDb));
            
            session.delete(fromDb);
            fromDb = getLocationById(id);
            assertNull("Place should not be found after delete()", fromDb);
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    } 
    
    @Test
    public void testLocationCategoryMapping() {
        try {
            Place l = createLocation();
            PlaceCategory c = createLocationCategory();
            l.addCategory(c);
            l.addCategory(c);
            assertEquals("Category must reject duplicate location", 1, c.getLocations().size());
            assertEquals("Place must reject duplicate category", 1, l.getCategories().size());
            
            PlaceCategory c2 = new PlaceCategory();
            c2.setName("Unique category name 3");
            l.addCategory(c2);
            assertEquals("Place must accept non-duplicate category", 2, l.getCategories().size());
            
            session.save(l);
            session.flush();
            
            session.evict(l);
            session.evict(c);
            session.evict(c2);
            
            Place fromDb = (Place)session.load(Place.class, l.getId());            
            assertEquals("Two categories for place must exist", 2, fromDb.getCategories().size());
            for(PlaceCategory cat : fromDb.getCategories()) {
                assertEquals("One Place for category must exist", 1, cat.getLocations().size());
            }
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    private PlaceIngest getLocationIngestById(Long id) {
        Query query = session.createQuery("from PlaceIngest where id = :id").setLong("id", id);
        return (PlaceIngest)query.uniqueResult();
    }
    
    private PlaceCategory getLocationCategoryById(Long id) {
        Query query = session.createQuery("from PlaceCategory where id = :id").setLong("id", id);
        return (PlaceCategory)query.uniqueResult();
    }
    
    private Place getLocationById(Long id) {
        Query query = session.createQuery("from Place where id = :id").setLong("id", id);
        return (Place)query.uniqueResult();
    }

    private PlaceCategory createLocationCategory() {
        PlaceCategory c = new PlaceCategory();
        c.setName("Unique category name");
        return c;
    }
    
    private PlaceIngest createLocationIngest() {
        PlaceIngest i = new PlaceIngest();
        i.setZip("00000");
        i.setDistance(2000);
        i.setCountryCode("US"); 
        return i;
    }
    
    private Place createLocation() {
        Place l = new Place();        
        l.setCity("Staten Island");
        l.setCountryCode("US");
        l.setCategory("Pizza");
        l.setExternalId("123456");
        l.setLatitude(40.552544f);
        l.setLongitude(-74.15088f);
        l.setName("Mario's Pizza");
        l.setPhone("(718) 111 2222");
        l.setSource(Place.Source.YELLOW_PAGES);
        l.setState("NY");
        l.setStreet("1234 Hilan Blvd");
        l.setUrl("http://c.ypcdn.com/2/c/rtd?vrid=e938536c6b70c0c9c1e8d6ffaa32053b&rid=d73bd11c-ccc6-44e9-8a75-da8e6cf99d2d&ptid=943aw4l8qj&ypid=2816456&lid=2816456&tl=7&lsrc=MDM&dest=http%3A%2F%2Fwww.yellowpages.com%2Fstaten-island-ny%2Fmip%2Fnew-york-public-library-2816456%3Ffrom%3Dpubapi_943aw4l8qj");
        l.setZip("00000");
        return l;
    }
    
    private boolean locationsEqual(Place l1, Place l2) {
        boolean result = l1.getCity().equals(l2.getCity());
        if(result) result = l1.getCity().equals(l2.getCity());
        if(result) result = l1.getCountryCode().equals(l2.getCountryCode());
        if(result) result = l1.getCategory().equals(l2.getCategory());
        if(result) result = l1.getExternalId().equals(l2.getExternalId());
        if(result) result = l1.getLatitude().intValue() == l2.getLatitude().intValue();
        if(result) result = l1.getLongitude().intValue() == l2.getLongitude().intValue();
        if(result) result = l1.getName().equals(l2.getName());
        if(result) result = l1.getPhone().equals(l2.getPhone());
        if(result) result = l1.getSource().equals(l2.getSource());
        if(result) result = l1.getState().equals(l2.getState());
        if(result) result = l1.getStreet().equals(l2.getStreet());
        if(result) result = l1.getUrl().equals(l2.getUrl());
        if(result) result = l1.getZip().equals(l2.getZip());
        return result;
    }

}
