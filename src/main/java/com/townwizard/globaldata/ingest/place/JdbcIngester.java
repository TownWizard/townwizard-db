package com.townwizard.globaldata.ingest.place;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import com.townwizard.db.dao.AbstractDao;
import com.townwizard.db.dao.AbstractDaoHibernateImpl;
import com.townwizard.db.logger.Log;
import com.townwizard.globaldata.model.directory.Ingest;
import com.townwizard.globaldata.model.directory.Place;
import com.townwizard.globaldata.model.directory.PlaceCategory;
import com.townwizard.globaldata.model.directory.PlaceIngest;

public class JdbcIngester extends AbstractIngester {
    
    private static final DateFormat MYSQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private AbstractDao dao;
    private Session session;
    
    public JdbcIngester(String zipCode, String countryCode, List<PlaceCategory> categories, 
            String categoryOrTerm, AbstractDao dao) {
        super(zipCode, countryCode, categories, categoryOrTerm);
        this.dao = dao;
    }

    @Override
    protected void markIngestInProgress(PlaceIngest ingest) {
        String now = MYSQL_DATE_FORMAT.format(new Date());

        //INSERT INTO Ingest (created, status, zip, country_code, category_id) 
        //VALUES ('2013-05-07 17:26:20', 'I', '10001', 'US', 1)
        //ON DUPLICATE KEY UPDATE id = id
        
        boolean hasCategory = ingest.getPlaceCategory() != null;
        StringBuilder sb = new StringBuilder();
        if(hasCategory) {
            sb.append("INSERT INTO Ingest (created, status, zip, country_code, category_id) ");
        } else {
            sb.append("INSERT INTO Ingest (created, status, zip, country_code, term) ");
        }
        sb.append("VALUES (");
        appendString(sb, now).append(", ");
        appendString(sb, Ingest.Status.I.toString()).append(", ");
        appendString(sb, ingest.getZip()).append(", ");
        appendString(sb, ingest.getCountryCode()).append(", ");
        if(hasCategory) {
            sb.append(ingest.getPlaceCategory().getId());
        } else {
            appendEscapedString(sb, ingest.getTerm());
        }
        sb.append(") ");
        sb.append("ON DUPLICATE KEY UPDATE id = id");
        executeSQL(sb.toString());        
    }
    
    @Override
    protected void mergePlaces(Collection<Place> places) {
        String now = MYSQL_DATE_FORMAT.format(new Date());
        
        //INSERT INTO Location (created, external_id, name, category, street, city, state, zip, country_code, phone, latitude, longitude, url, source) 
        //VALUES ('2013-05-07 17:26:20', '12345', 'Mike''s Pizza', 'Italian Restaurants', '123 Main Street', 'Maraphon', '12345', 'US', '(456) 111-2222'), 40.715874, -73.99052, 'http://mikspizza', 1) 
        //ON DUPLICATE KEY UPDATE id = id
        
        for(Place p : places) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO Location ");
            sb.append("(created, external_id, name, category, street, city, state, zip, country_code, phone, latitude, longitude, url, source) ");
            sb.append("VALUES (");
            appendString(sb, now).append(", ");
            appendString(sb, p.getExternalId()).append(", ");
            appendEscapedString(sb, p.getName()).append(", ");
            appendEscapedString(sb, p.getCategory()).append(", ");
            appendEscapedString(sb, p.getStreet()).append(", ");
            appendEscapedString(sb, p.getCity()).append(", ");
            appendString(sb, p.getState()).append(", ");
            appendString(sb, p.getZip()).append(", ");            
            appendString(sb, p.getCountryCode()).append(", ");
            appendString(sb, p.getPhone()).append(", ");
            sb.append(p.getLatitude()).append(", ");
            sb.append(p.getLongitude()).append(", ");
            appendString(sb, p.getUrl()).append(", ");
            sb.append(p.getSource().getId());
            sb.append(") ");
            sb.append("ON DUPLICATE KEY UPDATE id = id");
            executeSQL(sb.toString());
        }
    }
    
    @Override
    protected void mapPlacesToIngest(PlaceIngest ingest) {
        //INSERT INTO Location_Ingest (location_id, ingest_id)
        //VALUES (
        //  (SELECT id FROM Location WHERE external_id = '123456' AND source = 1),
        //  (SELECT id FROM Ingest WHERE zip = 'zip' AND country_code = 'US' AND category_id = 1))
        //ON DUPLICATE KEY UPDATE id = id
        
        boolean hasCategory = ingest.getPlaceCategory() != null;
        
        for(Place p : ingest.getPlaces()) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO Location_Ingest (location_id, ingest_id) ");
            sb.append("VALUES (");
            sb.append("(SELECT id FROM Location WHERE external_id = ");
            appendString(sb, p.getExternalId()).append(" AND source = ").append(p.getSource().getId()).append("), ");
            sb.append("(SELECT id FROM Ingest WHERE zip = ");
            appendString(sb, ingest.getZip()).append(" AND country_code = ");
            appendString(sb, ingest.getCountryCode());
            if(hasCategory) {
                sb.append(" AND category_id = ").append(ingest.getPlaceCategory().getId()).append(")");
            } else {
                sb.append(" AND term = ");
                appendEscapedString(sb, ingest.getTerm()).append(")");
            }
            sb.append(") ");
            sb.append("ON DUPLICATE KEY UPDATE id = id");
            executeSQL(sb.toString());
        }
    }
    
    @Override
    protected void addNewCategories(Set<String> newCategoryNames) {
        //INSERT INTO Category (name) VALUES ('pizza') ON DUPLICATE KEY UPDATE id = id
        
        for(String name : newCategoryNames) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO Category (name) ");
            sb.append("VALUES (");
            appendEscapedString(sb, name);
            sb.append(") ");
            sb.append("ON DUPLICATE KEY UPDATE id = id");
            executeSQL(sb.toString());
        }
    }
    
    @Override
    protected void mapPlacesToCategories(Map<String, Set<Place>> categoryToPlaces) {
        //INSERT INTO Location_Category (location_id, category_id)
        //VALUES ((SELECT id FROM Location WHERE external_id = '123456' AND source = 1), 
        //        (SELECT id FROM Category WHERE name = 'Pizza'))
        //ON DUPLICATE KEY UPDATE category_id = 1
        
        for(Map.Entry<String, Set<Place>> e : categoryToPlaces.entrySet()) {
            String c = e.getKey();
            for(Place p : e.getValue()) {
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO Location_Category (location_id, category_id) ");
                sb.append("VALUES (");
                sb.append("(SELECT id FROM Location WHERE external_id = ");
                appendString(sb, p.getExternalId()).append(" AND source = ").append(p.getSource().getId()).append("), ");
                sb.append("(SELECT id FROM Category WHERE name = ");
                appendEscapedString(sb, c).append(")");
                sb.append(") ");
                sb.append("ON DUPLICATE KEY UPDATE id = id");
                executeSQL(sb.toString());
            }
        }
    }
    
    @Override
    protected void markIngestReady(PlaceIngest ingest) {
        //UPDATE Ingest WHERE zip = '11223' AND country_code = 'US' AND category_id = 1 SET status = 'R'
        
        boolean hasCategory = ingest.getPlaceCategory() != null;
        
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE Ingest SET status = ");
        appendString(sb, Ingest.Status.R.toString());
        sb.append(" WHERE zip = ");
        appendString(sb, ingest.getZip()).append(" AND country_code = ");
        appendString(sb, ingest.getCountryCode());

        if(hasCategory) {
            sb.append(" AND category_id = ").append(ingest.getPlaceCategory().getId());
        } else {
            sb.append(" AND term = ");
            appendEscapedString(sb, ingest.getTerm());
        }
        executeSQL(sb.toString());
    }
    
    @Override
    protected void beforeIngest() {
        if(session == null || !session.isOpen()) {
            session = ((AbstractDaoHibernateImpl)dao).getSessionFactory().openSession();
        }
    }
    
    @Override
    protected void afterIngest() {
        if(session != null) {
            session.close();
        }
    }
    
    @Override
    protected void onError(Exception e, IngestTask task) {
        Log.error("Error processing ingest for (" + 
                task.getZipCode() + ", " + task.getCategory() + ") :" + e.getMessage());
    }
    
    private StringBuilder appendString(StringBuilder sb, String s) {
        return sb.append("'").append(s).append("'");
    }
    
    private StringBuilder appendEscapedString(StringBuilder sb, String s) {
        String str = "'" + s.replace("'", "''") + "'";
        str = str.replace("\\\'", "\\\\'");
        sb.append(str);
        return sb;
    }
    
    private void executeSQL(String sql) {
        try {
            session.createSQLQuery(sql).executeUpdate();
        } catch (Exception e) {
            Log.error("Error executing SQL:\n" + sql);
            throw e;
        }
    }

}
