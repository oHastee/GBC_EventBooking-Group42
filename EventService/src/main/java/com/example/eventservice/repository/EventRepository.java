package com.example.eventservice.repository;

import com.example.eventservice.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EventRepository extends MongoRepository<Event, String> {
    public Event findAllByStateEquals(String state);
    public List<Event> findAllByRoomId(long roomId);
}
