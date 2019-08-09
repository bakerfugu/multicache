package com.tvg.cachetests.controller;

import com.tvg.cachetests.model.Contact;
import com.tvg.cachetests.service.ContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/contacts"})
public class ContactController {

    private ContactService service;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    ContactController(ContactService contactService) {
        this.service = contactService;
    }

    @GetMapping
    public List findAll(){
//        log.info("GET request made to find all contacts");
//        String thisClassPackagePath = this.getClass().getName();//.replace(".", "/") + ".java";
//        String justName = this.getClass().getSimpleName();
//
//        log.info("here package path bro: " + thisClassPackagePath);
//        log.info("here simple name dude: " + justName);
        return service.findAll();
    }

    @GetMapping(path = {"/{id}"})
    public ResponseEntity<Contact> findById(@PathVariable Long id) {
        log.info(String.format("GET request made to find contact with id: %d", id));
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping(path = {"friends/{id}"})
    public ResponseEntity<Contact> findFriendById(@PathVariable Long id) {
        log.info(String.format("GET request made to find FRIEND with id: %d", id));
        return ResponseEntity.ok(service.findFriendById(id));
    }

    @PostMapping
    public Contact create(@Valid @RequestBody Contact contact){
        log.info(String.format("POST request made to create new contact with name: %s", contact.getName()));
        return service.create(contact);
    }

    @PutMapping(value="/{id}")
    public ResponseEntity<Contact> update(@PathVariable("id") long id,
                                          @Valid @RequestBody Contact contact){
        log.info(String.format("PUT request made to update contact at id: %d", id));
        return ResponseEntity.ok(service.update(id, contact));
    }

    @DeleteMapping(path ={"/{id}"})
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        log.info(String.format("DELETE request made to delete contact at id: %d", id));
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.warn(String.format("MethodArgumentNotValidException: Invalid input for field: %s cause: %s",
                    fieldName, errorMessage));
        });
        return errors;
    }

    // more CRUD methods...
}
