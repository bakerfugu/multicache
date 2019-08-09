package com.tvg.cachetests.service;

import com.tvg.cachetests.exception.ContactNotFoundException;
import com.tvg.cachetests.exception.MismatchedContactIdsException;
import com.tvg.cachetests.model.Contact;
import com.tvg.cachetests.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

//import com.tvg.cache.CacheConfiguration;

@Service
@EnableCaching
public class ContactService {

    private ContactRepository repository;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    //private CacheConfiguration cacheConfiguration;

    ContactService(ContactRepository contactRepository) {
        this.repository = contactRepository;
        //this.cacheConfiguration = cacheConfiguration;
    }

    public List findAll(){
        return repository.findAll();
    }

    @Cacheable(value = "contacts", key = "#p0")
    public Contact findById(@PathVariable long id) {

        if (repository.existsById(id)) {
            log.info(String.format("Contact (%d) not found in cache, retrieving now", id));
            return repository.findById(id).get();
        }

        throw new ContactNotFoundException(id);
    }

    @Cacheable("friend-list")
    public Contact findFriendById(@PathVariable long id) {

        if (repository.existsById(id)) {
            log.info(String.format("Friend (%d) not found in cache, retrieving now", id));
            return repository.findById(id).get();
        }

        throw new ContactNotFoundException(id);
    }

    public Contact create(@RequestBody Contact contact) {
        return repository.save(contact);
    }

    @CacheEvict(value = "contacts", key = "#p0")
    public Contact update(@PathVariable("id") Long id,
                                          @RequestBody Contact contact) {

        final Long bodyId = contact.getId();
        if (id.equals(bodyId)) {
            throw new MismatchedContactIdsException(id, bodyId);
        }

        if (repository.existsById(id)) {
            return repository.findById(id).map(record -> {
                record.setName(contact.getName());
                record.setEmail(contact.getEmail());
                record.setPhone(contact.getPhone());
                Contact updated = repository.save(record);
                return updated;
            }).get();
        }

        throw new ContactNotFoundException(id);
    }

    @CacheEvict(value = "contacts", key = "#p0")
    public void delete(@PathVariable("id") long id) {
        if (repository.existsById(id)) {
            repository.findById(id).map(record -> {
                repository.deleteById(id);
                return null;
            });
        }
    }

    @CacheEvict(value = "contacts", allEntries = true)
    public void clearCache() {
        log.info("Cache cleared");
    }

}
