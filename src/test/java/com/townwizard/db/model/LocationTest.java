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
import com.townwizard.globaldata.model.Location;
import com.townwizard.globaldata.model.LocationCategory;
import com.townwizard.globaldata.model.LocationIngest;

/**
 * Tests global location hibernate object mappings
 */
public class LocationTest extends TestSupport {

    private Session session;
    
    @Before
    public void beginTransaction() {
        session = getSessionFactory().openSession();        
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
            LocationIngest i = createLocationIngest();
            session.save(i);
            Long id = i.getId();
            assertNotNull("Location ingest id should not be null after save()", id);            
            
            LocationIngest fromDb = getLocationIngestById(id);
            assertNotNull("Location ingest should be found in db after save() by id", fromDb);
            Date updated = fromDb.getUpdated();
            try { Thread.sleep(100); } catch (Exception e) {}
            fromDb.setDistance(50000);
            session.save(fromDb);
            
            fromDb = getLocationIngestById(id);
            assertNotSame("Location ingest updated should change after update()", updated, fromDb.getUpdated());
            
            session.delete(fromDb);
            fromDb = getLocationIngestById(id);
            assertNull("Location ingest should not be found after delete()", fromDb);
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testLocationCategory() {
        try {
            LocationCategory c = createLocationCategory();
            session.save(c);
            Long id = c.getId();
            assertNotNull("Location category id should not be null after save()", id);            
            
            LocationCategory fromDb = getLocationCategoryById(id);
            assertNotNull("Location category should be found in db after save() by id", fromDb);
            
            session.delete(fromDb);
            fromDb = getLocationCategoryById(id);
            assertNull("Location category should not be found after delete()", fromDb);
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testLocation() {
        try {
            Location l = createLocation();            
            session.save(l);
            Long id = l.getId();
            assertNotNull("Location id should not be null after save()", id);
            session.evict(l);
                        
            Location fromDb = getLocationById(id);
            assertNotNull("Location should be found in db after save() by id", fromDb);
            assertTrue("Retrieved location must match the original one after save()", locationsEqual(l, fromDb));
            
            fromDb.setCategory("Unique category name 2");
            session.save(fromDb);
            
            fromDb = getLocationById(id);            
            assertFalse("Location should change after update()", locationsEqual(l, fromDb));
            
            session.delete(fromDb);
            fromDb = getLocationById(id);
            assertNull("Location should not be found after delete()", fromDb);
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    } 
    
    @Test
    public void testLocationCategoryMapping() {
        try {
            Location l = createLocation();
            LocationCategory c = createLocationCategory();
            l.addCategory(c);
            l.addCategory(c);
            assertEquals("Category must reject duplicate location", 1, c.getLocations().size());
            assertEquals("Location must reject duplicate category", 1, l.getCategories().size());
            
            LocationCategory c2 = new LocationCategory();
            c2.setName("Unique category name 3");
            l.addCategory(c2);
            assertEquals("Location must accept non-duplicate category", 2, l.getCategories().size());
            
            session.save(l);
            session.flush();
            
            session.evict(l);
            session.evict(c);
            session.evict(c2);
            
            Location fromDb = (Location)session.load(Location.class, l.getId());            
            assertEquals("Two categories for location must exist", 2, fromDb.getCategories().size());
            for(LocationCategory cat : fromDb.getCategories()) {
                assertEquals("One location for category must exist", 1, cat.getLocations().size());
            }
        } catch(Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    private LocationIngest getLocationIngestById(Long id) {
        Query query = session.createQuery("from LocationIngest where id = :id").setLong("id", id);
        return (LocationIngest)query.uniqueResult();
    }
    
    private LocationCategory getLocationCategoryById(Long id) {
        Query query = session.createQuery("from LocationCategory where id = :id").setLong("id", id);
        return (LocationCategory)query.uniqueResult();
    }
    
    private Location getLocationById(Long id) {
        Query query = session.createQuery("from Location where id = :id").setLong("id", id);
        return (Location)query.uniqueResult();
    }

    private LocationCategory createLocationCategory() {
        LocationCategory c = new LocationCategory();
        c.setName("Unique category name");
        return c;
    }
    
    private LocationIngest createLocationIngest() {
        LocationIngest i = new LocationIngest();
        i.setZip("00000");
        i.setDistance(2000);
        i.setCountryCode("US"); 
        return i;
    }
    
    private Location createLocation() {
        Location l = new Location();        
        l.setCity("Staten Island");
        l.setCountryCode("US");
        l.setCategory("Pizza");
        l.setExternalId("123456");
        l.setLatitude(40.552544f);
        l.setLongitude(-74.15088f);
        l.setName("Mario's Pizza");
        l.setPhone("(718) 111 2222");
        l.setSource(Location.Source.YELLOW_PAGES);
        l.setState("NY");
        l.setStreet("1234 Hilan Blvd");
        l.setUrl("http://c.ypcdn.com/2/c/rtd?vrid=e938536c6b70c0c9c1e8d6ffaa32053b&rid=d73bd11c-ccc6-44e9-8a75-da8e6cf99d2d&ptid=943aw4l8qj&ypid=2816456&lid=2816456&tl=7&lsrc=MDM&dest=http%3A%2F%2Fwww.yellowpages.com%2Fstaten-island-ny%2Fmip%2Fnew-york-public-library-2816456%3Ffrom%3Dpubapi_943aw4l8qj");
        l.setZip("00000");
        return l;
    }
    
    private boolean locationsEqual(Location l1, Location l2) {
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
