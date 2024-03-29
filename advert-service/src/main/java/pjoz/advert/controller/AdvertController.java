package pjoz.advert.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pjoz.advert.dto.UserDto;
import pjoz.advert.feign.UserClient;
import pjoz.advert.model.Advert;
import pjoz.advert.model.AdvertRepository;
import pjoz.advert.service.AdvertService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/adverts")
@AllArgsConstructor
public class AdvertController {

    private final AdvertRepository advertRepository;
    private final AdvertService advertService;
    private final UserClient userClient;

    @GetMapping("/{id}")
    ResponseEntity<Advert> getAdvertById(@PathVariable int id) {
        return ResponseEntity.of(advertRepository.findById(id));
    }

    @GetMapping("/all")
    ResponseEntity<List<Advert>> getAdverts() {
        List<Advert> adverts = advertRepository.findAll();
        if(adverts.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(adverts);
    }

    @GetMapping("/users")
    ResponseEntity<?> getAdvertsForLoggedInUser() {
        Optional<UserDto> userDto = userClient.getLoggedInUserDetails();
        if(userDto.isPresent()){
            return ResponseEntity.of(advertRepository.findAllByUserId(userDto.get().getId()));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    @GetMapping("/users/{id}")
    ResponseEntity<?> getAdvertsByUserId(@PathVariable Integer id) {
        return ResponseEntity.of(advertRepository.findAllByUserId(id));
    }

    @PostMapping
    ResponseEntity<String> createAdvert(@RequestBody Advert advert) {
        advertService.saveAdvert(advert);
        return ResponseEntity.ok("Advert: " + advert.getTitle() + " has been successfully added");
    }

    @PutMapping("/{id}")
    ResponseEntity<Integer> updateAdvert(@RequestBody Advert advert, @PathVariable Integer id) {
        return ResponseEntity.of(advertService.updateAdvert(advert, id));
    }
    @Transactional
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteAdvert(@PathVariable int id) {
        boolean isRemoved = advertService.deleteAdvert(id);
        if (!isRemoved) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Advert not found.");
        }
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @DeleteMapping(value = "/user/{id}")
    public ResponseEntity<String> deleteAdvertsByUserId(@PathVariable int id) {
        Optional<List<Advert>> adverts = advertRepository.findAllByUserId(id);
        if(adverts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Adverts not found.");
        }
        adverts.get().forEach(advert -> advertService.deleteAdvert(advert.getId()));
        return new ResponseEntity<>("Adverts deleted for a user: " + id, HttpStatus.OK);
    }
}
