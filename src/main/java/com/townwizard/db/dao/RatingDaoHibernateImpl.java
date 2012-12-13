package com.townwizard.db.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.townwizard.db.model.Content;
import com.townwizard.db.model.Rating;
import com.townwizard.db.model.User;

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
    public Float getAverageRating(Content content) {
        return (Float) getSession().createQuery(
                "select avg(value) from Rating where content = :content and active = true")
            .setEntity("content", content).uniqueResult();
    }
}
