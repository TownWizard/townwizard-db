package com.townwizard.db.dao;

import java.util.List;

import com.townwizard.db.model.Content;
import com.townwizard.db.model.Rating;
import com.townwizard.db.model.User;

/**
 * Contains methods to manipulate ratings in the DB
 */
public interface RatingDao extends AbstractDao {
    
    /**
     * Find a user rating for a given content.
     * Return null if no user rating exists for that content. 
     */
    Rating getRating(User user, Content content);
    
    /**
     * Return a list of user ratings for many given contents.
     * If no rating exists for a content, do not include null in the list.
     * If no ratings exist for all contents, return an empty list
     */
    List<Rating> getRatings(User user, List<Content> contents);
    
    /**
     * Return a rating object which represents an average rating for a given content.
     * The returned object will have an average value, and a count of ratings existing for that content.
     * If none exists, return null
     */
    Rating getAverageRating(Content content);
    
    /**
     * Return a list of objects representing average ratings for many given contents.
     * In case when no ratings exist for a given content, do not include null in the result list.
     * Return an empty list if no ratings exists for any given contents
     */
    List<Rating> getAverageRatings(List<Content> content);
    
}
