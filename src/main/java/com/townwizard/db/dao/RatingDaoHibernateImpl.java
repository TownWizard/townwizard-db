package com.townwizard.db.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.townwizard.db.model.Content;
import com.townwizard.db.model.Rating;
import com.townwizard.db.model.User;

/**
 * Hibernamte implementation of RatingDao
 */
@Component("ratingDao")
public class RatingDaoHibernateImpl extends AbstractDaoHibernateImpl implements RatingDao {

    @Override
    public Rating getRating(User user, Content content) {
        return (Rating)getSession().createQuery(
                "from Rating where user = :user and content = :content and active = true")
            .setEntity("user", user)
            .setEntity("content", content).uniqueResult();
    }
    
    @Override
    public List<Rating> getRatings(User user, List<Content> contents) {
        @SuppressWarnings("unchecked")
        List<Rating> ratings = getSession().createQuery(
                "from Rating where user = :user and content in :contents and active = true")
            .setEntity("user", user)
            .setParameterList("contents", contents).list();
        return ratings;
    }    

    @Override
    public Rating getAverageRating(Content content) {        
        Object[] valueAndCount = (Object[])getSession().createQuery(
                "select avg(value), count(value) from Rating where content = :content and active = true")
            .setEntity("content", content).uniqueResult();
        
        int count = ((Long)valueAndCount[1]).intValue();
        if(count != 0){        
            Rating r = new Rating();
            r.setContent(content);
            r.setValue(new Float((Double)valueAndCount[0]));
            r.setCount(count);
            return r;
        }
        return null;
    }
    
    @Override
    public List<Rating> getAverageRatings(List<Content> contents) {
        @SuppressWarnings("unchecked")
        List<Object[]> ratings = getSession().createQuery(
                "select content.id, avg(value), count(value) from Rating " + 
                "where content in :contents and active = true " + 
                "group by content")
            .setParameterList("contents", contents).list();        
        
        List<Rating> result = new ArrayList<>();
        
        if(!ratings.isEmpty()) {
            Map<Long, Content> idToContent = new HashMap<>();
            for(Content c : contents) {
                idToContent.put(c.getId(), c);
            }
        
            for(Object[] r : ratings) {
                Rating rating = new Rating();
                rating.setContent(idToContent.get(r[0]));
                rating.setValue(new Float((Double)r[1]));
                rating.setCount(((Long)r[2]).intValue());
                result.add(rating);
            }
        }
        
        return result;
    }
}
