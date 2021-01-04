package com.space.service;

import com.space.exceptions.BadRequestException;
import com.space.exceptions.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

@Service
public class ShipServiceImpl implements ShipService {
    private static final int LEFT_YEAR = 2800;
    private static final int RIGHT_YEAR = 3019;
    private static final int VARCHAR_LENGTH = 50;

    @Autowired
    ShipRepository shipRepository;

    @Override
    public Ship getById(Long id) {
        validateId(id);
        return shipRepository.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Корабль с идентификатором %d не найден!", id)));
    }

    private void validateId(Long id) {
        if (id <= 0) {
            throw new BadRequestException("ID корабля недействителен!");
        }
    }

    // @Override
    public Ship createShip(Ship ship) {
        validateName(ship.getName());
        validatePlanet(ship.getPlanet());
        validateShipType(ship.getShipType());
        validateProdDate(ship.getProdDate());
        validateSpeed(ship.getSpeed());
        validateCrewSize(ship.getCrewSize());
        if (ship.getUsed() == null)
            ship.setUsed(false);
        ship.setRating(calculateRating(ship.getProdDate().getTime(), ship.getSpeed(), ship.getUsed()));
        return shipRepository.saveAndFlush(ship);
    }

    // @Override
    public Ship updateShip(Long id, Ship ship) {
        Ship oldShip = getById(id);
        if (ship.getName() != null) {
            validateName(ship.getName());
            oldShip.setName(ship.getName());
        }
        if (ship.getPlanet() != null) {
            validatePlanet(ship.getPlanet());
            oldShip.setPlanet(ship.getPlanet());
        }
        if (ship.getShipType() != null) {
            validateShipType(ship.getShipType());
            oldShip.setShipType(ship.getShipType());
        }
        if (ship.getProdDate() != null) {
            validateProdDate(ship.getProdDate());
            oldShip.setProdDate(ship.getProdDate());
        }
        if (ship.getUsed() != null) {
            oldShip.setUsed(ship.getUsed());
        }
        if (ship.getSpeed() != null) {
            validateSpeed(ship.getSpeed());
            oldShip.setSpeed(ship.getSpeed());
        }
        if (ship.getCrewSize() != null) {
            validateCrewSize(ship.getCrewSize());
            oldShip.setCrewSize(ship.getCrewSize());
        }
        oldShip.setRating(calculateRating(oldShip.getProdDate().getTime(), oldShip.getSpeed(), oldShip.getUsed()));
        shipRepository.save(oldShip);
        return oldShip;
    }

    private double calculateRating(Long prodDate, Double speed, Boolean isUsed) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(prodDate);
        int year = calendar.get(Calendar.YEAR);
        BigDecimal raiting = BigDecimal.valueOf((80 * speed * (isUsed ? 0.5 : 1)) / (RIGHT_YEAR - year + 1));
        raiting = raiting.setScale(2, RoundingMode.HALF_UP);
        return raiting.doubleValue();
    }

    private void validateCrewSize(Integer crewSize) {
        if (crewSize == null || crewSize < 1 || crewSize > 9999)
            throw new BadRequestException("Неправильный размер экипажа!");
    }

    private void validateSpeed(Double speed) {
        if (speed == null || speed < 0.01D || speed > 0.99D)
            throw new BadRequestException("Неправильная скорость!");
    }

    private void validateName(String name) {
        if (name == null || name.isEmpty() || name.length() > VARCHAR_LENGTH)
            throw new BadRequestException("Неправильное название корабля!");
    }

    private void validatePlanet(String planet) {
        if (planet == null || planet.isEmpty() || planet.length() > VARCHAR_LENGTH)
            throw new BadRequestException("Не тот корабль-планета!");
    }

    private void validateProdDate(Date prodDate) {
        if (prodDate == null)
            throw new BadRequestException("Неправильная дата производства!");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(prodDate.getTime());
        if (calendar.get(Calendar.YEAR) < LEFT_YEAR || calendar.get(Calendar.YEAR) > RIGHT_YEAR)
            throw new BadRequestException("Дата выпуска вышла за рамки!");
    }

    private void validateShipType(ShipType shipType) {
        if (shipType == null)
            throw new BadRequestException("Неправильный тип корабля!");
    }

    @Override
    public void delete(Long id) {
        Ship ship = getById(id);
        shipRepository.delete(ship);
    }

    @Override
    public Page<Ship> gelAllShips(Specification<Ship> specification, Pageable pageable) {
        return shipRepository.findAll(specification, pageable);
    }

    public Long getShipsCount(Specification<Ship> specification) {
        return shipRepository.count(specification);
    }

    @Override
    public Specification<Ship> filterByName(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(root.get("name"), "%" + name + "%");
    }

    @Override
    public Specification<Ship> filterByShipType(ShipType shipType) {
        return (root, query, cb) -> shipType == null ? null : cb.equal(root.get("shipType"), shipType);
    }

    @Override
    public Specification<Ship> filterByDate(Long after, Long before) {
        return (root, query, cb) -> {
            if (after == null && before == null) {
                return null;
            }
            if (after == null) {
                return cb.lessThanOrEqualTo(root.get("prodDate"), new Date(before));
            }
            if (before == null) {
                return cb.greaterThanOrEqualTo(root.get("prodDate"), new Date(after));
            }
            return cb.between(root.get("prodDate"), new Date(after), new Date(before));
        };
    }

    @Override
    public Specification<Ship> filterByUsage(Boolean isUsed) {
        return (root, query, cb) -> {
            if (isUsed == null) {
                return null;
            }
            if (isUsed) {
                return cb.isTrue(root.get("isUsed"));
            }
            return cb.isFalse(root.get("isUsed"));
        };
    }

    @Override
    public Specification<Ship> filterBySpeed(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("speed"), max);
            }
            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("speed"), min);
            }
            return cb.between(root.get("speed"), min, max);
        };
    }

    @Override
    public Specification<Ship> filterByCrewSize(Integer min, Integer max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("crewSize"), max);
            }
            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("crewSize"), min);
            }
            return cb.between(root.get("crewSize"), min, max);
        };
    }

    @Override
    public Specification<Ship> filterByRating(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            if (min == null) {
                return cb.lessThanOrEqualTo(root.get("rating"), max);
            }
            if (max == null) {
                return cb.greaterThanOrEqualTo(root.get("rating"), min);
            }
            return cb.between(root.get("rating"), min, max);
        };
    }

    @Override
    public Specification<Ship> filterByPlanet(String planet) {
        return (root, query, cb) -> planet == null ? null : cb.like(root.get("planet"), "%" + planet + "%");
    }
}
