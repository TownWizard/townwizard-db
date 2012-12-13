package com.townwizard.db.dao;

import java.util.List;

import com.townwizard.db.model.Content;
import com.townwizard.db.model.Rating;
import com.townwizard.db.model.User;

public interface RatingDao extends AbstractDao {
    
    Rating getRating(User user, Content content);
    
    List<Rating> getRatings(User user, List<Content> contents);
    
    Float getAverageRating(Content content);
    
}
