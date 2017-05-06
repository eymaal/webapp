package org.literacyapp.dao.jpa;

import java.util.List;
import javax.persistence.NoResultException;
import org.literacyapp.dao.StoryBookDao;

import org.springframework.dao.DataAccessException;

import org.literacyapp.model.content.StoryBook;
import org.literacyapp.model.enums.Locale;

public class StoryBookDaoJpa extends GenericDaoJpa<StoryBook> implements StoryBookDao {

    @Override
    public StoryBook readByTitle(Locale locale, String title) throws DataAccessException {
        try {
            return (StoryBook) em.createQuery(
                "SELECT book " +
                "FROM StoryBook book " +
                "WHERE book.locale = :locale " +
                "AND book.title = :title")
                .setParameter("locale", locale)
                .setParameter("title", title)
                .getSingleResult();
        } catch (NoResultException e) {
            logger.warn("StoryBook \"" + title + "\" was not found for locale " + locale);
            return null;
        }
    }

    @Override
    public List<StoryBook> readAllOrdered(Locale locale) throws DataAccessException {
        return em.createQuery(
            "SELECT book " +
            "FROM StoryBook book " +
            "WHERE book.locale = :locale " +
            "ORDER BY book.title")
            .setParameter("locale", locale)
            .getResultList();
    }
    
    @Override
    public Long readCount(Locale locale) throws DataAccessException {
        return (Long) em.createQuery(
                "SELECT COUNT(s) " +
                "FROM StoryBook s " +
                "WHERE s.locale = :locale")
                .setParameter("locale", locale)
                .getSingleResult();
    }
}
