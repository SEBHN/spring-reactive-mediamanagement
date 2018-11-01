package de.hhn.mvs.rest;

import de.hhn.mvs.MediaCreator;
import de.hhn.mvs.model.Media;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "users/{userId}/media")
public class MediaController {

    @GetMapping(value = "/")
    public ResponseEntity<List<Media>> list(@PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(MediaCreator.getInstance().getDummyMedia());
    }


    @GetMapping(value = "/{id}")
    public ResponseEntity<Media> get(@PathVariable String id, @PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(MediaCreator.getInstance().getDummyMedia().get(Integer.parseInt(id)));
    }

    @GetMapping(value = "/{id}/download")
    public ResponseEntity<Media> download(@PathVariable String id, @PathVariable String userId) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }


    @PostMapping
    public ResponseEntity<Media> post(@RequestBody Media body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(null);
    }
}
