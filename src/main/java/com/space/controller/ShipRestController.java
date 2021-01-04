package com.space.controller;

import com.space.exceptions.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Controller
@RequestMapping("/rest/ships")
public class ShipRestController {
    @Autowired
    private ShipService shipService;

    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> getShip(@PathVariable("id") Long shipId) {
        if (shipId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Ship ship = shipService.getById(shipId);
        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Ship>(ship, HttpStatus.OK);
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Long getShipsCount(@RequestParam(value = "name", required = false) String name,@RequestParam(value = "planet", required = false) String planet,
                                                 @RequestParam(value = "shipType", required = false) ShipType shipType,
                                                 @RequestParam(value = "after", required = false) Long after,
                                                 @RequestParam(value = "before", required = false) Long before,
                                                 @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                                 @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                                 @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                                 @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                                 @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                                 @RequestParam(value = "minRating", required = false) Double minRating,
                                                 @RequestParam(value = "maxRating", required = false) Double maxRating) {
        return shipService.getShipsCount(Specification.where(shipService.filterByName(name)
                .and(shipService.filterByPlanet(planet))
                .and(shipService.filterByShipType(shipType))
                .and(shipService.filterByDate(after, before))
                .and(shipService.filterByUsage(isUsed))
                .and(shipService.filterBySpeed(minSpeed, maxSpeed))
                .and(shipService.filterByCrewSize(minCrewSize, maxCrewSize))
                .and(shipService.filterByRating(minRating, maxRating))));
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        HttpHeaders headers = new HttpHeaders();
        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        shipService.createShip(ship);
        return ResponseEntity.ok(ship);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> updateShip(@PathVariable("id") Long shipId, @RequestBody Ship ship) {
        Ship updatedShip = shipService.updateShip(shipId, ship);

        return new ResponseEntity<>(updatedShip,  HttpStatus.OK);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Ship> deleteShip(@PathVariable("id") Long id) {
        Ship ship = this.shipService.getById(id);
        if (ship == null) {
            return new ResponseEntity<Ship>(HttpStatus.NOT_FOUND);
        }
        this.shipService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<Ship> getAllShips(@RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "planet", required = false) String planet,
                                                  @RequestParam(value = "shipType", required = false) ShipType shipType,
                                                  @RequestParam(value = "after", required = false) Long after,
                                                  @RequestParam(value = "before", required = false) Long before,
                                                  @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                                  @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                                  @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                                  @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                                  @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                                  @RequestParam(value = "minRating", required = false) Double minRating,
                                                  @RequestParam(value = "maxRating", required = false) Double maxRating,
                                                  @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
                                                  @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                  @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        return shipService.gelAllShips(
                Specification
                        .where(shipService.filterByName(name)
                                .and(shipService.filterByPlanet(planet)))
                        .and(shipService.filterByShipType(shipType))
                        .and(shipService.filterByDate(after, before))
                        .and(shipService.filterByUsage(isUsed))
                        .and(shipService.filterBySpeed(minSpeed, maxSpeed))
                        .and(shipService.filterByCrewSize(minCrewSize, maxCrewSize))
                        .and(shipService.filterByRating(minRating, maxRating)), pageable)
                .getContent();
    }

}